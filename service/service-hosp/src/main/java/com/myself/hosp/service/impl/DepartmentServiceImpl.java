package com.myself.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.myself.hosp.repository.DepartmentRepository;
import com.myself.hosp.service.DepartmentService;
import com.myself.model.hosp.Department;
import com.myself.vo.hosp.DepartmentQueryVo;
import com.myself.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    //上传科室接口
    @Override
    public void save(Map<String, Object> paramMap) {
        //集合转换成Department对象
        String mapString = JSONObject.toJSONString(paramMap);
        Department department = JSONObject.parseObject(mapString, Department.class);
        //判断是否存在的数据
        String hoscode = department.getHoscode();
        String depcode = department.getDepcode();
        Department departmentExist = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (null != departmentExist) {
            //修改数据
            departmentExist.setUpdateTime(new Date());
            departmentExist.setIsDeleted(0);
            departmentRepository.save(departmentExist);
        } else {
            //添加数据
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    //查询科室接口
    @Override
    public Page<Department> getPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);
        //创建Pageable对象
        Pageable pageable = PageRequest.of(page - 1, limit); //0是第一页
        //创建Example对象
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //设置模糊查询
                .withIgnoreCase(true); //设置忽略大小写
        Example<Department> example = Example.of(department, matcher);
        Page<Department> pageDepartment = departmentRepository.findAll(example, pageable);
        return pageDepartment;
    }

    //删除科室接口
    @Override
    public void remove(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (null != department) {
            //删除数据
            departmentRepository.deleteById(department.getId());
        }
    }

    //根据hoscode获取所有科室信息
    @Override
    public List<DepartmentVo> getDeptList(String hoscode) {
        List<DepartmentVo> result = new ArrayList<>();
        Department dept = new Department();
        dept.setHoscode(hoscode);
        //创建Example对象
        Example<Department> example = Example.of(dept);
        //所有科室信息
        List<Department> list = departmentRepository.findAll(example);
        //根据大科室编号分组
        Map<String, List<Department>> departmentMap = list.stream().collect(Collectors.groupingBy(Department::getBigcode));
        for (Map.Entry<String, List<Department>> entry : departmentMap.entrySet()) {
            //大科室编号
            String bigcode = entry.getKey();
            //大科室编号对应的数据
            List<Department> departmentList = entry.getValue();
            //封装大科室
            DepartmentVo bigDepartmentVo = new DepartmentVo();
            bigDepartmentVo.setDepcode(bigcode);
            bigDepartmentVo.setDepname(departmentList.get(0).getBigname());
            //封装小科室
            List<DepartmentVo> children = new ArrayList<>();
            for (Department department : departmentList) {
                DepartmentVo smallDepartmentVo = new DepartmentVo();
                smallDepartmentVo.setDepcode(department.getDepcode());
                smallDepartmentVo.setDepname(department.getDepname());
                //封装到list
                children.add(smallDepartmentVo);
            }
            bigDepartmentVo.setChildren(children);
            result.add(bigDepartmentVo);
        }
        return result;
    }

    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (null != department) {
            return department.getDepname();
        }
        return null;
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        return department;
    }

}
