package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 员工新增
     * @param employeeDTO
     */
    void regist(EmployeeDTO employeeDTO);

    /**
     * 分页查询员工
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用禁用账号
     * @param id
     * @param status
     */
    void changeStatus(Long id, Integer status);

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    Employee queryById(Long id);

    /**
     * 编辑修改员工信息
     * @param employeeDTO
     */
    void updateEmployee(EmployeeDTO employeeDTO);

    /**
     * 修改员工密码
     * @param passwordEditDTO
     */
    void editPassword(PasswordEditDTO passwordEditDTO);
}
