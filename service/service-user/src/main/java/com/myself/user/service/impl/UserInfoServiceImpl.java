package com.myself.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myself.common.exception.HospitalException;
import com.myself.common.helper.JwtHelper;
import com.myself.common.result.ResultCodeEnum;
import com.myself.enums.AuthStatusEnum;
import com.myself.model.user.Patient;
import com.myself.model.user.UserInfo;
import com.myself.user.mapper.UserInfoMapper;
import com.myself.user.service.PatientService;
import com.myself.user.service.UserInfoService;
import com.myself.vo.user.LoginVo;
import com.myself.vo.user.UserAuthVo;
import com.myself.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private PatientService patientService;

    //用户登录
    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //获取输入的手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        //判断手机号和验证码是否为空
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new HospitalException(ResultCodeEnum.PARAM_ERROR);
        }
        //判断手机验证码和输入的验证码是否匹配
        String redisCode = redisTemplate.opsForValue().get(phone);
        if (!code.equals(redisCode)) {
            throw new HospitalException(ResultCodeEnum.CODE_ERROR);
        }

        UserInfo userInfo = null;

        //使用微信扫码后，绑定手机号
        if (!StringUtils.isEmpty(loginVo.getOpenid())) {
            userInfo = getByOpenid(loginVo.getOpenid());
            if (null != userInfo) {
                userInfo.setPhone(loginVo.getPhone());
                baseMapper.updateById(userInfo);
            } else {
                throw new HospitalException(ResultCodeEnum.DATA_ERROR);
            }
        }

        //如果userInfo为空，进行手机登录
        if (userInfo == null) {
            //判断是否是第一次登录
            userInfo = baseMapper.selectOne(new QueryWrapper<UserInfo>().eq("phone", phone));
            if (null == userInfo) {
                //注册
                userInfo = new UserInfo();
                userInfo.setName("");
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                baseMapper.insert(userInfo);
            }
        }

        //判断用户是否被禁用
        if (userInfo.getStatus() == 0) {
            throw new HospitalException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }
        //直接登录
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        //如果名称为空使用昵称
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        //如果昵称为空使用手机号
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        //返回token和登录信息
        map.put("name", name);
        map.put("token", JwtHelper.createToken(userInfo.getId(), name));
        return map;
    }

    //根据openid获取用户信息
    @Override
    public UserInfo getByOpenid(String openid) {
        UserInfo userInfo = baseMapper.selectOne(new QueryWrapper<UserInfo>().eq("openid", openid));
        return userInfo;
    }

    //用户认证
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //根据用户id获取用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        //设置认证信息
        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //更新信息
        baseMapper.updateById(userInfo);
    }

    //分页带条件获取用户列表
    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        //获取条件值
        String name = userInfoQueryVo.getKeyword();
        Integer status = userInfoQueryVo.getStatus();
        Integer authStatus = userInfoQueryVo.getAuthStatus();
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin();
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(name)) {
            queryWrapper.like("name", name);
        }
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("status", status);
        }
        if (!StringUtils.isEmpty(authStatus)) {
            queryWrapper.eq("auth_status", authStatus);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            queryWrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            queryWrapper.le("create_time", createTimeEnd);
        }
        Page<UserInfo> pages = baseMapper.selectPage(pageParam, queryWrapper);
        //获取list集合，遍历并封装认证状态、锁定状态
        pages.getRecords().stream().forEach(item -> {
            this.packageUserInfo(item);
        });
        return pages;
    }

    //用户锁定
    @Override
    public void lock(Long userId, Integer status) {
        if (status.intValue() == 0 || status.intValue() == 1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }
    }

    //根据id获取用户详情
    @Override
    public Map<String, Object> show(Long userId) {
        Map<String, Object> map = new HashMap<>();
        //获取用户信息
        UserInfo userInfo = this.packageUserInfo(baseMapper.selectById(userId));
        map.put("userInfo", userInfo);
        //获取就诊人信息
        List<Patient> patientList = patientService.findAllUserId(userId);
        map.put("patientList", patientList);
        return map;
    }

    //认证审批
    @Override
    public void approval(Long userId, Integer authStatus) {
        if (authStatus.intValue() == 2 || authStatus.intValue() == -1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }

    private UserInfo packageUserInfo(UserInfo userInfo) {
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        String statusString = userInfo.getStatus().intValue() == 0 ? "锁定" : "正常";
        userInfo.getParam().put("statusString", statusString);
        return userInfo;
    }
}
