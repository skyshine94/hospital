package com.myself.msm.service;

import com.myself.vo.msm.MsmVo;

import java.util.Map;

public interface MsmService {

    boolean send(String phone, Map<String, Object> param);

    boolean send(MsmVo msmVo);
}
