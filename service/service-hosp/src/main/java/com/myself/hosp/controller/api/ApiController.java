package com.myself.hosp.controller.api;

import com.myself.common.exception.HospitalException;
import com.myself.common.result.Result;
import com.myself.common.result.ResultCodeEnum;
import com.myself.hosp.service.DepartmentService;
import com.myself.hosp.service.HospitalService;
import com.myself.hosp.service.HospitalSetService;
import com.myself.hosp.service.ScheduleService;
import com.myself.model.hosp.Department;
import com.myself.model.hosp.Hospital;
import com.myself.model.hosp.Schedule;
import com.myself.service.helper.HttpRequestHelper;
import com.myself.service.utils.MD5;
import com.myself.vo.hosp.DepartmentQueryVo;
import com.myself.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 数据接口模块
 *
 * @author Wei
 * @since 2021/7/1
 */
@Api(tags = "数据接口")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private HospitalSetService hospitalSetService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation("上传医院接口")
    @PostMapping("saveHospital")
    public Result saveHosp(HttpServletRequest request) {
        //获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        //转换Map集合
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //获取签名(签名进行了加密)
        String hospSign = (String) paramMap.get("sign");
        //获取医院编号
        String hoscode = (String) paramMap.get("hoscode");
        //查询签名
        String signKey = hospitalSetService.getSignKey(hoscode);
        //加密签名
        String signKeyMD5 = MD5.encrypt(signKey);
        //判断签名是否一致
        if (!hospSign.equals(signKeyMD5)) {
            throw new HospitalException(ResultCodeEnum.SIGN_ERROR);
        }
        //项目中的图片采用base64编码转化为字符串，字符串中的加号在服务器解析数据时会转化为空格，需要进行还原处理。
        String logoDataString = (String) paramMap.get("logoData");
        if (!StringUtils.isEmpty(logoDataString)) {
            String logoData = logoDataString.replaceAll("", "+");
            paramMap.put("logoData", logoData);
        }
        //添加或修改数据
        hospitalService.save(paramMap);
        return Result.ok();
    }

    @ApiOperation("查询医院接口")
    @PostMapping("hospital/show")
    public Result getHospital(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        String hospSign = (String) paramMap.get("sign");
        String hoscode = (String) paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        String signKeyMD5 = MD5.encrypt(signKey);
        //判断签名是否一致
        if (!hospSign.equals(signKeyMD5)) {
            throw new HospitalException(ResultCodeEnum.SIGN_ERROR);
        }
        //根据编号查询
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }

    @ApiOperation("上传科室接口")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        String hospSign = (String) paramMap.get("sign");
        String hoscode = (String) paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        String signKeyMD5 = MD5.encrypt(signKey);
        //判断签名是否一致
        if (!hospSign.equals(signKeyMD5)) {
            throw new HospitalException(ResultCodeEnum.SIGN_ERROR);
        }
        //添加或修改数据
        departmentService.save(paramMap);
        return Result.ok();
    }

    @ApiOperation("查询科室接口")
    @PostMapping("department/list")
    public Result getDepartment(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        String hospSign = (String) paramMap.get("sign");
        String hoscode = (String) paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        String signKeyMD5 = MD5.encrypt(signKey);
        //判断签名是否一致
        if (!hospSign.equals(signKeyMD5)) {
            throw new HospitalException(ResultCodeEnum.SIGN_ERROR);
        }
        //获取当前页和每页记录数
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 1 : Integer.parseInt((String) paramMap.get("limit"));
        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        //根据编号查询
        Page<Department> pageDepartment = departmentService.getPageDepartment(page, limit, departmentQueryVo);
        return Result.ok(pageDepartment);
    }

    @ApiOperation("删除科室接口")
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        String hospSign = (String) paramMap.get("sign");
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        String signKeyMD5 = MD5.encrypt(signKey);
        //判断签名是否一致
        if (!hospSign.equals(signKeyMD5)) {
            throw new HospitalException(ResultCodeEnum.SIGN_ERROR);
        }
        //删除数据
        departmentService.remove(hoscode, depcode);
        return Result.ok();
    }

    @ApiOperation("上传排班接口")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        String hospSign = (String) paramMap.get("sign");
        String hoscode = (String) paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        String signKeyMD5 = MD5.encrypt(signKey);
        //判断签名是否一致
        if (!hospSign.equals(signKeyMD5)) {
            throw new HospitalException(ResultCodeEnum.SIGN_ERROR);
        }
        //添加或修改数据
        scheduleService.save(paramMap);
        return Result.ok();
    }

    @ApiOperation("查询排班接口")
    @PostMapping("schedule/list")
    public Result getSchedule(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        String hospSign = (String) paramMap.get("sign");
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");
        //获取当前页和每页记录数
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 1 : Integer.parseInt((String) paramMap.get("limit"));
        String signKey = hospitalSetService.getSignKey(hoscode);
        String signKeyMD5 = MD5.encrypt(signKey);
        //判断签名是否一致
        if (!hospSign.equals(signKeyMD5)) {
            throw new HospitalException(ResultCodeEnum.SIGN_ERROR);
        }
        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        //根据编号查询
        Page<Schedule> pageSchedule = scheduleService.getPageSchedule(page, limit, scheduleQueryVo);
        return Result.ok(pageSchedule);
    }

    @ApiOperation("删除排班接口")
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        String hospSign = (String) paramMap.get("sign");
        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String) paramMap.get("hosScheduleId");
        String signKey = hospitalSetService.getSignKey(hoscode);
        String signKeyMD5 = MD5.encrypt(signKey);
        //判断签名是否一致
        if (!hospSign.equals(signKeyMD5)) {
            throw new HospitalException(ResultCodeEnum.SIGN_ERROR);
        }
        //删除数据
        scheduleService.remove(hoscode, hosScheduleId);
        return Result.ok();
    }

}
