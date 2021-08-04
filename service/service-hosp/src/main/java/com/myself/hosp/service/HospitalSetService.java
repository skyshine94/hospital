package com.myself.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myself.model.hosp.HospitalSet;
import com.myself.vo.order.SignInfoVo;


public interface HospitalSetService extends IService<HospitalSet> {

    String getSignKey(String hoscode);

    SignInfoVo getSignInfoVo(String hoscode);
}
