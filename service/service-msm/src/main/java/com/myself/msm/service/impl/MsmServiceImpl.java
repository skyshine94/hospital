package com.myself.msm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.myself.msm.service.MsmService;
import com.myself.msm.utils.ConstantPropertiesUtil;
import com.myself.vo.msm.MsmVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class MsmServiceImpl implements MsmService {

    //发送手机验证码
    @Override
    public boolean send(String phone, Map<String, Object> param) {
//        if (StringUtils.isEmpty(phone)) {
//            return false;
//        }
//        //设置阿里云短信服务
//        DefaultProfile profile = DefaultProfile.getProfile(ConstantPropertiesUtil.REGION_ID,
//                ConstantPropertiesUtil.ACCESS_KEY_ID,
//                ConstantPropertiesUtil.SECRET);
//        IAcsClient client = new DefaultAcsClient(profile);
//        CommonRequest request = new CommonRequest();
//        //request.setProtocol(ProtocolType.HTTPS);
//        request.setSysDomain("dysmsapi.aliyuncs.com");
//        request.setVersion("2017-05-25");
//        request.setAction("SendSms");
//        //手机号
//        request.putQueryParameter("PhoneNumbers", phone);
//        //签名名称
//        request.putQueryParameter("SignName", "预约挂号平台");
//        //模板code
//        request.putQueryParameter("TemplateCode", "SMS_123456");
//        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(param));
//
//        //发送短信
//        try {
//            CommonResponse response = client.getCommonResponse(request);
//            System.out.println(response.getData());
//            return response.getHttpResponse().isSuccess();
//        } catch (ClientException e) {
//            e.printStackTrace();
//        }
//        return false;
        return true;
    }

    //使用rabbitMQ发送短信
    @Override
    public boolean send(MsmVo msmVo) {
        if (!StringUtils.isEmpty(msmVo.getPhone())) {
            boolean isSend = this.send(msmVo.getPhone(), msmVo.getParam());
            return isSend;
        }
        return false;
    }

}
