package com.myself.hosp.controller;

import com.myself.common.result.Result;
import com.myself.hosp.service.DepartmentService;
import com.myself.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医院管理模块
 * @author Wei
 * @since 2021/7/1
 */
@Api(tags = "医院科室")
@RestController
@RequestMapping("/admin/hosp/department")
//@CrossOrigin
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation(value = "根据hoscode查询所有科室")
    @GetMapping("getDeptList/{hoscode}")
    public Result getDeptList(@PathVariable String hoscode){
        List<DepartmentVo> list = departmentService.getDeptList(hoscode);
        return Result.ok(list);
    }

}
