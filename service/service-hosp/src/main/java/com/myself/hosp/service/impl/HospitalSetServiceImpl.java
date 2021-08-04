package com.myself.hosp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myself.common.exception.HospitalException;
import com.myself.common.result.ResultCodeEnum;
import com.myself.hosp.mapper.HospitalSetMapper;
import com.myself.hosp.service.HospitalSetService;
import com.myself.model.hosp.HospitalSet;
import com.myself.vo.order.SignInfoVo;
import org.springframework.stereotype.Service;

@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {

    //获取签名
    @Override
    public String getSignKey(String hoscode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode", hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        return hospitalSet.getSignKey();
    }

    //获取医院签名
    @Override
    public SignInfoVo getSignInfoVo(String hoscode) {
        HospitalSet hospitalSet = baseMapper.selectOne(new QueryWrapper<HospitalSet>().eq("hoscode", hoscode));
        if (null == hospitalSet) {
            throw new HospitalException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        signInfoVo.setSignKey(hospitalSet.getSignKey());
        return signInfoVo;
    }
}
