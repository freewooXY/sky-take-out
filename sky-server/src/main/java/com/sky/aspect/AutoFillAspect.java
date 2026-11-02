package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.constant.ExceptionMsgConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充
 */
@Component
@Aspect
@Slf4j
public class AutoFillAspect {

    /**
     * 插入公共字段的切入点
     * 拦截 所有返回值类型 com.sky.mapper包下面所有类的所有方法 所有参数类型
     * 拦截 有AutoFill注解的
     */
    @Pointcut("execution(* com.sky.mapper.*.* (..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() { }

    /**
     * 插入公共字段的前置通知
     * 进行公共字段赋值
     */
    @Before("autoFillPointCut()")
    public void preAutoFillAdvisor(JoinPoint joinPoint) throws Throwable {
        log.info("开始进行公共字段的自动填充：{} {}",
                joinPoint.getTarget(),joinPoint.getSignature().getName());

        // 获取到当前拦截方法的数据操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 向下转型为方法签名
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class); // 获取方法的注解
        OperationType operationType = annotation.value(); // 获取数据库操作类型

        // 获取到拦截方法的参数，也就是实体对象
        Object[] args = joinPoint.getArgs(); // 约定需要自动填充的方法的参数为第一个
        Assert.notNull(args, ExceptionMsgConstant.NULL_EXCEPTION); // 参数列表不能为null
        Assert.notEmpty(args, ExceptionMsgConstant.EMPTY_EXCEPTION); // 参数列表不能为空

        Object entity = args[0];

        // 准备公共字段数据
        LocalDateTime currentTime = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 根据不同的操作类型为对应的属性赋值
        try {
            if (operationType == OperationType.INSERT) {
                // 为4个公共字段赋值
                Method setCreateTime = entity.getClass().getDeclaredMethod(
                        AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(
                        AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(
                        AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(
                        AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(entity, LocalDateTime.now());
                setUpdateTime.invoke(entity, LocalDateTime.now());
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);
            } else if (operationType == OperationType.UPDATE) {
                // 为2个公共字段赋值
                Method setUpdateTime = entity.getClass().getDeclaredMethod(
                        AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(
                        AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(entity, LocalDateTime.now());
                setUpdateUser.invoke(entity, currentId);
            }
        } catch (Exception ex) {
            log.error("插入公共字段错误：{}", ex.getMessage());
        }
    }
}
