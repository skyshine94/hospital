package com.myself.common.utils;

import com.myself.common.helper.JwtHelper;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取当前用户工具类
 *
 * @author Wei
 * @since 2021/7/7
 */
public class AuthContextHolder {

    //获取当前用户id
    public static Long getUserId(HttpServletRequest request) {
        //获取token
        String token = request.getHeader("token");
        //从token中获取id
        Long userId = JwtHelper.getUserId(token);
        return userId;
    }

    //获取当前用户名称
    public static String getUserName(HttpServletRequest request){
        //获取token
        String token = request.getHeader("token");
        //从token中获取id
        String userName = JwtHelper.getUserName(token);
        return userName;
    }
}
