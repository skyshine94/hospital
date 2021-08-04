package com.myself.hosp.controller.api;

import com.myself.common.result.Result;
import com.myself.hosp.service.DepartmentService;
import com.myself.hosp.service.HospitalService;
import com.myself.hosp.service.HospitalSetService;
import com.myself.hosp.service.ScheduleService;
import com.myself.model.hosp.Hospital;
import com.myself.model.hosp.Schedule;
import com.myself.vo.hosp.DepartmentVo;
import com.myself.vo.hosp.HospitalQueryVo;
import com.myself.vo.hosp.ScheduleOrderVo;
import com.myself.vo.order.SignInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台数据接口模块
 *
 * @author Wei
 * @since 2021/7/3
 */
@Api(tags = "前台数据接口")
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalApiController {

    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private HospitalSetService hospitalSetService;


    @ApiOperation(value = "分页带条件获取医院列表")
    @GetMapping("findHospList/{page}/{limit}")
    public Result findHospList(@PathVariable Integer page, @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> hospitals = hospitalService.selectHospPage(page, limit, hospitalQueryVo);
        return Result.ok(hospitals);
    }

    @ApiOperation(value = "根据hosname获取医院信息")
    @GetMapping("findByHosname/{hosname}")
    public Result findByHosname(@PathVariable String hosname) {
        List<Hospital> list = hospitalService.findByHosname(hosname);
        return Result.ok(list);
    }

    @ApiOperation(value = "根据hoscode获取科室信息")
    @GetMapping("department/{hoscode}")
    public Result index(@PathVariable String hoscode) {
        List<DepartmentVo> deptList = departmentService.getDeptList(hoscode);
        return Result.ok(deptList);
    }

    @ApiOperation(value = "根据hoscode获取医院详情信息")
    @GetMapping("findHospDetail/{hoscode}")
    public Result item(@PathVariable String hoscode) {
        Map<String, Object> map = hospitalService.item(hoscode);
        return Result.ok(map);
    }

    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingScheduleRule(@PathVariable Integer page,
                                         @PathVariable Integer limit,
                                         @PathVariable String hoscode,
                                         @PathVariable String depcode) {
        Map<String, Object> map = scheduleService.getBookingScheduleRule(page, limit, hoscode, depcode);
        return Result.ok(map);
    }

    @ApiOperation(value = "获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(@PathVariable String hoscode,
                                   @PathVariable String depcode,
                                   @PathVariable String workDate) {
        List<Schedule> list = scheduleService.getScheduleDetail(hoscode, depcode, workDate);
        return Result.ok(list);
    }

    @ApiOperation(value = "根据scheduleId获取排班详情")
    @GetMapping("getSchedule/{scheduleId}")
    public Result getSchedule(@PathVariable String scheduleId) {
        Schedule schedule = scheduleService.getSchedule(scheduleId);
        return Result.ok(schedule);
    }

    @ApiOperation(value = "根据scheduleId获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = scheduleService.getScheduleOrderVo(scheduleId);
        return scheduleOrderVo;
    }

    @ApiOperation(value = "获取医院签名")
    @GetMapping("inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@PathVariable String hoscode) {
        SignInfoVo signInfoVo = hospitalSetService.getSignInfoVo(hoscode);
        return signInfoVo;
    }

}
