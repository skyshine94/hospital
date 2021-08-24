package com.myself.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.myself.common.exception.HospitalException;
import com.myself.common.result.ResultCodeEnum;
import com.myself.hosp.repository.ScheduleRepository;
import com.myself.hosp.service.DepartmentService;
import com.myself.hosp.service.HospitalService;
import com.myself.hosp.service.ScheduleService;
import com.myself.model.hosp.BookingRule;
import com.myself.model.hosp.Department;
import com.myself.model.hosp.Hospital;
import com.myself.model.hosp.Schedule;
import com.myself.vo.hosp.BookingScheduleRuleVo;
import com.myself.vo.hosp.ScheduleOrderVo;
import com.myself.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;

    //上传排班接口
    @Override
    public void save(Map<String, Object> paramMap) {
        //集合转换成Schedule对象
        String mapString = JSONObject.toJSONString(paramMap);
        Schedule schedule = JSONObject.parseObject(mapString, Schedule.class);
        //判断是否存在的数据
        String hoscode = schedule.getHoscode();
        String hosScheduleId = schedule.getHosScheduleId();
        Schedule scheduleExist = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (null != scheduleExist) {
            //修改数据
            scheduleExist.setUpdateTime(new Date());
            scheduleExist.setStatus(1);
            scheduleExist.setIsDeleted(0);
            scheduleRepository.save(scheduleExist);
        } else {
            //添加数据
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setStatus(1);
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }
    }

    //查询排班接口
    @Override
    public Page<Schedule> getPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(schedule, scheduleQueryVo);
        schedule.setIsDeleted(0);
        schedule.setStatus(1);
        //创建Pageable对象
        Pageable pageable = PageRequest.of(page, limit);
        //创建Example对象
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<Schedule> example = Example.of(schedule, matcher);
        Page<Schedule> pageSchedule = scheduleRepository.findAll(example, pageable);
        return pageSchedule;
    }

    //删除排班接口
    @Override
    public void remove(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (null != schedule) {
            //删除数据
            scheduleRepository.deleteById(schedule.getId());
        }
    }

    //根据hoscode和depcode分页查询所有排班
    @Override
    public Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode) {
        //根据hoscode和depcode查询科室
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //根据workDate进行分组
        Aggregation aggregation = Aggregation.newAggregation(
                //条件匹配
                Aggregation.match(criteria),
                //分组
                Aggregation.group("workDate")
                        //取出组中第一行数据
                        .first("workDate").as("workDate")
                        //统计可预约总数数量
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                //排序
                Aggregation.sort(Sort.Direction.DESC, "workDate"),
                //分页
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );
        //执行聚合
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();

        //分组查询的总记录数
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggregation = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);
        int total = totalAggregation.getMappedResults().size();

        //将日期转换成对应星期
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        //封装数据
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleList", bookingScheduleRuleVoList);
        result.put("total", total);

        //获取医院名称
        String hosname = hospitalService.getHospName(hoscode);
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname", hosname);
        result.put("baseMap", baseMap);
        return result;
    }

    //根据hoscode、depcode和workDate查询排班信息
    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        List<Schedule> list = scheduleRepository.getScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate).toDate());
        //获取list集合，遍历并封装医院名称、科室名称、日期对应星期
        list.stream().forEach(item -> {
            this.packageSchedule(item);
        });
        return list;
    }

    //获取可预约排班数据
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        //获取预约规则
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if (hospital == null) {
            throw new HospitalException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        //停挂时间
        String stopTime = bookingRule.getStopTime();

        //分页获取可预约日期数据
        IPage<Date> iPage = this.getDateList(page, limit, bookingRule);
        List<Date> dateList = iPage.getRecords();
        //是否为预约首日所在页
        Boolean isFirstDate = (page == 1) ? true : false;
        //是否为预约最后一日所在页
        Boolean isLastDate = (page == iPage.getPages()) ? true : false;

        //根据可预约日期、hoscode和depcode获取可预约排班规则信息
        Criteria criteria = Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode)
                .and("workDate").in(dateList);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber"),
                //排序
                Aggregation.sort(Sort.Direction.ASC, "workDate")
        );
        AggregationResults<BookingScheduleRuleVo> aggregateResult = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleRuleVoList = aggregateResult.getMappedResults();

        //使用stream流封装可预约日期、日期对应星期、预约状态
        scheduleRuleVoList.stream().forEach(item -> {
            this.packageScheduleVo(item, dateList, isLastDate, isFirstDate, stopTime);
        });

        //可预约日期规则数据
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleList", scheduleRuleVoList);
        result.put("total", iPage.getTotal());

        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        //科室
        Department department = departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停挂时间
        baseMap.put("stopTime", bookingRule.getStopTime());

        result.put("baseMap", baseMap);
        return result;
    }

    private void packageScheduleVo(BookingScheduleRuleVo bookingScheduleRuleVo, List<Date> dateList, Boolean isLastDate, Boolean isFirstDate, String stopTime) {
        //当天没有排班医生
        if (null == bookingScheduleRuleVo) {
            //就诊医生人数
            bookingScheduleRuleVo.setDocCount(0);
            //科室剩余预约数
            bookingScheduleRuleVo.setAvailableNumber(-1);
        }

        //设置可预约日期
        bookingScheduleRuleVo.setWorkDateMd(bookingScheduleRuleVo.getWorkDate());
        //设置日期对应星期
        String dayOfWeek = this.getDayOfWeek(new DateTime(bookingScheduleRuleVo.getWorkDate()));
        bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

        //设置状态
        bookingScheduleRuleVo.setStatus(0);
        //超过停挂时间不能预约
        if (isFirstDate && bookingScheduleRuleVo.getWorkDate().equals(dateList.get(0))) {
            DateTime stopDateTime = this.getDateTime(new Date(), stopTime);
            if (stopDateTime.isBeforeNow()) {
                //停止预约
                bookingScheduleRuleVo.setStatus(-1);
            }
        }
        //设置最后一页最后一条记录为即将预约
        if (isLastDate && bookingScheduleRuleVo.getWorkDate().equals(dateList.get(dateList.size() - 1 ))) {
            bookingScheduleRuleVo.setStatus(1);
        }
    }

    //根据scheduleId获取排班详情
    @Override
    public Schedule getSchedule(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        //封装医院名称、科室名称、日期对应星期
        this.packageSchedule(schedule);
        return schedule;
    }

    //根据scheduleId获取预约下单数据
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        //获取排班信息
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        if (null == schedule) {
            throw new HospitalException(ResultCodeEnum.PARAM_ERROR);
        }
        //获取预约规则信息
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if (null == hospital) {
            throw new HospitalException(ResultCodeEnum.PARAM_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if (null == bookingRule) {
            throw new HospitalException(ResultCodeEnum.PARAM_ERROR);
        }
        //将数据封装到ScheduleOrderVo中
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //处理退号时间
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //处理挂号开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //处理挂号结束时间
        int cycle = bookingRule.getCycle();
        DateTime endTime = this.getDateTime(new DateTime().plusDays(cycle).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //处理当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());

        return scheduleOrderVo;
    }

    //更新排班信息（用于rabbitmq）
    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }

    private IPage getDateList(Integer page, Integer limit, BookingRule bookingRule) {
        //获取当天放号时间
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //获取预约周期
        Integer cycle = bookingRule.getCycle();
        //如果过了当天放号时间，预约周期从后一天开始计算，周期加1
        if (releaseTime.isBeforeNow()) {
            cycle += 1;
        }
        //获取所有可预约日期
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            DateTime dateTime = new DateTime().plusDays(i);
            //格式化日期
            String dateString = dateTime.toString("yyyy-MM-dd");
            //将DateTime转换成Date
            dateList.add(new DateTime(dateString).toDate());
        }

        int start = (page - 1) * limit;
        int end = (page - 1) * limit + limit;
        List<Date> pageDateList = dateList.stream().skip(start).limit(end).collect(Collectors.toList());

        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page(page, limit, dateList.size());
        iPage.setRecords(pageDateList);
        return iPage;
    }

    private void packageSchedule(Schedule schedule) {
        schedule.getParam().put("hosname", hospitalService.getHospName(schedule.getHoscode()));
        schedule.getParam().put("depname", departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        schedule.getParam().put("dayOfWeek", getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }

    //将日期和时间转换成DateTime格式
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    //将日期转换成对应星期
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

}
