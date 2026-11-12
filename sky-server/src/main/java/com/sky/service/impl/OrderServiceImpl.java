package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.properties.MockProperties;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private MockProperties mockProperties;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserMapper userMapper;

    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 处理各种业务异常（地址为空，购物车数据为空）
        AddressBook addressBook = addressBookMapper
                .getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            // 地址为空无法正常下单
            throw new AddressBookBusinessException(
                    MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 查询用户的购物车
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper
                .list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            // 购物车为空无法下单
            throw new ShoppingCartBusinessException(
                    MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        setOrderFields(orders);
        orders.setPhone(addressBook.getPhone()); // 设置电话
        orders.setConsignee(addressBook.getConsignee()); // 设置收货人
        orders.setUserId(BaseContext.getCurrentId()); // 设置用户id

        orderMapper.insert(orders);

        // 向订单明细表插入多条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            // 按照购物车信息准备订单明细
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        // 下单成功清空购物车
        shoppingCartMapper.cleanAll(BaseContext.getCurrentId());

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .build();
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        // 通过websocket向客户端浏览器推送消息 type orderId content
        Map map = new HashMap();
        map.put("type", 1); // 1表示来单提醒 2表示催单
        map.put("orderId", orders.getId());
        map.put("content", "订单号" + ordersDB.getNumber());
        String json = JSON.toJSONString(map); // 发送json字符串
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 分页查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 设置分页查询的页码和页面大小
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<OrderVO> page = orderMapper.pageQueryWithCondition(ordersPageQueryDTO);

        PageResult result = new PageResult(page.getTotal(), page.getResult());

        return result;
    }

    /**
     * 查询订单详情
     * @param orders
     * @return
     */
    @Override
    public OrderVO orderDetail(Orders orders) {
        return orderMapper.orderDetail(orders);
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void cancel(Long id) {
        // 查询订单数据，并且进行业务校验
        Orders orders = orderMapper.getById(id);
        if (orders  == null) {
            // 订单不存在
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        } else if (orders.getStatus() == Orders.CANCELLED
                || orders.getStatus() == Orders.COMPLETED) {
            // 已完成和已取消的订单不能取消
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 如果订单已支付则发起退款请求
        if (orders.getPayStatus() == Orders.PAID) {
            // 向微信支付平台发起退款请求
            JSONObject refundResponse;
            try {
                refundResponse = refund(orders);
                if (refundResponse.getString("status").equals("ABNORMAL")) {
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 订单退款Mock接口
     * @param id
     */
    @Override
    public void cancelMock(Long id) {
        Orders orders = orderMapper.getById(id);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        if (orders.getPayStatus() == Orders.PAID) {
            orders.setPayStatus(Orders.REFUND);
        }
        orderMapper.update(orders);
    }

    /**
     * 退款成功，修改订单状态
     * @param outTradeNo
     */
    @Override
    public void refundSuccess(String outTradeNo) {
        Orders orders = orderMapper.getByNumber(outTradeNo);
        orders.setPayStatus(Orders.REFUND);
        orderMapper.update(orders);
    }

    /**
     * 订单支付的mock接口
     * @param ordersPaymentDTO
     */
    @Override
    public void paymentMock(OrdersPaymentDTO ordersPaymentDTO) {
        paySuccess(ordersPaymentDTO.getOrderNumber());
    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    @Transactional
    public void repetition(Long id) {
        // 根据订单id查出之前的订单详细数据
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // 修改部分字段
        setOrderFields(orders);

        // 清除部分字段
        clearOrderFields(orders);

        // 插入订单
        orderMapper.insert(orders);

        // 修改订单详情订单号
        orderDetailList.forEach( (orderDetail) -> {orderDetail.setOrderId(orders.getId());} );
        orderDetailMapper.insertBatch(orderDetailList);
    }

    /**
     * 获取订单商户状态的数量
     * @return
     */
    @Override
    public OrderStatisticsVO getStatistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(
                orderMapper.getCountByStatus(Orders.CONFIRMED));
        orderStatisticsVO.setToBeConfirmed(
                orderMapper.getCountByStatus(Orders.TO_BE_CONFIRMED));
        orderStatisticsVO.setDeliveryInProgress(
                orderMapper.getCountByStatus(Orders.DELIVERY_IN_PROGRESS));

        return orderStatisticsVO;
    }

    /**
     * 商户接单
     * @param id
     */
    @Override
    public void confirm(Long id) {
        Orders orders = orderMapper.getById(id);
        orders.setStatus(Orders.CONFIRMED);

        orderMapper.update(orders);
    }

    /**
     * 商户拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        // 更新订单状态
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());

        // 商家拒单退款
        refundByAdmin(orders);

        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orderMapper.update(orders);


    }

    /**
     * 商家取消订单
     *
     * @param ordersCancelDTO
     */
    @Override
    public void cancelByAdmin(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setStatus(Orders.CANCELLED);

        // 商家取消订单退款
        refundByAdmin(orders);

        orderMapper.update(orders);
    }

    /**
     * 指定订单号派送
     * @param id
     */
    @Override
    public void delivery(String id) {
        Orders orders = orderMapper.getById(Long.parseLong(id));
        // 将商品状态改为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    /**
     * 完成指定订单号订单
     * @param id
     */
    @Override
    public void complete(String id) {
        Orders orders = orderMapper.getById(Long.parseLong(id));
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 用户催单
     *
     * @param id
     */
    @Override
    public void reminder(Long id) {
        // 根据id查询订单
        Orders orders = orderMapper.getById(id);

        // 校验订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 通过websocket向客户端浏览器推送消息 type orderId content
        Map map = new HashMap();
        map.put("type", 2); // 1表示来单提醒 2表示催单
        map.put("orderId", orders.getId());
        map.put("content", "订单号" + orders.getNumber());
        String json = JSON.toJSONString(map); // 发送json字符串
        webSocketServer.sendToAllClient(json);
    }

    private void setOrderFields(Orders orders) {
        orders.setOrderTime(LocalDateTime.now()); // 设置订单时间
        orders.setPayStatus(Orders.UN_PAID); // 设置支付状态
        orders.setStatus(Orders.PENDING_PAYMENT); // 设置订单状态
        orders.setNumber(String.valueOf(System.currentTimeMillis())); // 设置订单号
        orders.setPayMethod(Orders.WX_PAY); // 设置支付方式
        orders.setDeliveryStatus(Orders.DELIVERY_NOW);
    }

    private void clearOrderFields(Orders orders) {
        orders.setCheckoutTime(null);
        orders.setCancelReason(null);
        orders.setRejectionReason(null);
        orders.setCancelTime(null);
        orders.setEstimatedDeliveryTime(null);
        orders.setDeliveryTime(null);
    }

    private JSONObject refund(Orders orders) throws Exception {
        String response = weChatPayUtil.refund(
                orders.getNumber(),
                String.valueOf(System.currentTimeMillis()),
                orders.getAmount(),
                orders.getAmount()
        );

        return JSONObject.parseObject(response);
    }

    private void refundByAdmin(Orders orders) {
        // 如果订单已支付则发起退款请求
        if (orders.getPayStatus() == Orders.PAID && !mockProperties.isPayMock()) {
            // 向微信支付平台发起退款请求
            JSONObject refundResponse;
            try {
                refundResponse = refund(orders);
                if (refundResponse.getString("status").equals("ABNORMAL")) {
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            orders.setStatus(Orders.REFUND);
        }
    }
}
