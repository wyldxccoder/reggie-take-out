package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.commen.BaseContext;
import com.itheima.reggie.commen.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 处理员工登录请求
     *
     * @param request  HTTP请求对象
     * @param employee 包含登录信息的员工对象
     * @return 响应结果，包含员工信息或错误信息
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 对密码加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 根据页面提交的用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //用户名存在
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);


        if (emp == null) {
            return R.error("登录失败");
        }

        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }

        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        // 将员工ID存储在会话中
        request.getSession().setAttribute("employee", emp.getId());


        //响应结果
        return R.success(emp);
    }

    /**
     * 退出登录接口
     *
     * @param request HttpServletRequest对象
     * @return R<String> 包含退出成功消息的响应结果
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 从Session中移除名为"employee"的属性
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 添加员工接口
     *
     * @param request  HTTP请求对象
     * @param employee 包含登录信息的员工对象
     * @return 响应结果，包含员工添加成功信息
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("输出员工:{}", employee.toString());
        //添加密码加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//       //获取当前登录用户的id
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 分页查询员工 搜索员工
     *
     * @param page     页数
     * @param pageSize 每页多少数据
     * @param name     搜索名字
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //构造分页构造器
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件 如果名字不为空,执行后面方法(将名字赋值给getname)
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //添加排序条件 根据更新时间降序排列
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 更新员工状态
     *
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update( @RequestBody Employee employee) {

        employeeService.updateById(employee);
        return R.success("员工修改成功");
    }

    /**
     * 更新员工信息 和更新员工状态共有一个页面
     * 根据id查询员工信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    private R<Employee> getById(@PathVariable Long id) {

        Employee employee = employeeService.getById(id);
        if (employee == null) {
            return R.error("没有查询到员工");
        }

        return R.success(employee);
    }
}
