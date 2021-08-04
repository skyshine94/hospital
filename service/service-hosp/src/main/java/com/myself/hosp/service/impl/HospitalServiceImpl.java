package com.myself.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.myself.cmn.client.DictFeignClient;
import com.myself.hosp.repository.HospitalRepository;
import com.myself.hosp.service.HospitalService;
import com.myself.model.hosp.Hospital;
import com.myself.vo.hosp.HospitalQueryVo;
import com.myself.vo.order.SignInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private DictFeignClient dictFeignClient;

    //上传医院接口
    @Override
    public void save(Map<String, Object> map) {
        //集合转换成Hospital对象
        String mapString = JSONObject.toJSONString(map);
        Hospital hospital = JSONObject.parseObject(mapString, Hospital.class);
        //判断是否存在的数据
        String hoscode = hospital.getHoscode();
        Hospital hospitalExist = hospitalRepository.getHospitalByHoscode(hoscode);
        if (null != hospitalExist) {
            //修改数据
            //处理部分数据
            hospitalExist.setStatus(hospital.getStatus());
            hospitalExist.setUpdateTime(new Date());
            hospitalExist.setIsDeleted(0);
            hospitalRepository.save(hospitalExist);
        } else {
            //添加数据
            //处理部分数据
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }
    }

    //查询医院接口
    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospital;
    }

    //分页带条件获取医院列表
    @Override
    public Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
        Pageable pageable = PageRequest.of(page - 1, limit);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<Hospital> example = Example.of(hospital, matcher);
        Page<Hospital> pageHospital = hospitalRepository.findAll(example, pageable);
        //获取list集合，遍历并封装医院类型、省、市、地区
        pageHospital.getContent().stream().forEach(item -> {
            this.setHosType(item);
        });
        return pageHospital;
    }

    //更新医院上线状态
    @Override
    public void updateStatus(String id, Integer status) {
        //根据id查询医院信息
        Hospital hospital = hospitalRepository.findById(id).get();
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
    }

    //获取医院详情
    @Override
    public Map<String, Object> showHospDetail(String id) {
        Map<String, Object> map = new HashMap<>();
        Hospital hospital = setHosType(hospitalRepository.findById(id).get());
        //医院基本信息
        map.put("hospital", hospital);
        //单独封装预约规则
        map.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return map;
    }

    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        if (null != hospital) {
            return hospital.getHosname();
        }
        return null;
    }

    //根据hosname获取医院信息
    @Override
    public List<Hospital> findByHosname(String hosname) {
        List<Hospital> list = hospitalRepository.getHospitalByHosnameLike(hosname);
        return list;
    }

    //根据hoscode获取医院详情信息
    @Override
    public Map<String, Object> item(String hoscode) {
        Map<String, Object> map = new HashMap<>();
        Hospital hospital = setHosType(getByHoscode(hoscode));
        //医院基本信息
        map.put("hospital", hospital);
        //单独封装预约规则
        map.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return map;
    }

    private Hospital setHosType(Hospital hospital) {
        //根据dictCode和value获取医院等级名称
        String hostypeString = dictFeignClient.getName("Hostype", hospital.getHostype());
        //查询省、市、地区
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        String districtString = dictFeignClient.getName(hospital.getDistrictCode());
        hospital.getParam().put("hostypeString", hostypeString);
        hospital.getParam().put("fullAddress", provinceString + cityString + districtString);
        return hospital;
    }

}
