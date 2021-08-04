package com.myself.user.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.myself.common.helper.JwtHelper;
import com.myself.common.result.Result;
import com.myself.model.user.UserInfo;
import com.myself.user.service.UserInfoService;
import com.myself.user.utils.ConstantWxPropertiesUtil;
import com.myself.user.utils.HttpClientUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信服务模块
 *
 * @author Wei
 * @since 2021/7/5
 */
@Api(tags = "微信服务")
@Controller //方便页面跳转
@RequestMapping("/api/ucenter/wx")
public class WeixinApiController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation(value = "生成微信二维码")
    @GetMapping("getLoginParam")
    @ResponseBody
    public Result genQrConnect() {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("appid", ConstantWxPropertiesUtil.WX_OPEN_APP_ID); //应用标识
            map.put("scope", "snsapi_login"); //应用授权作用域
            String wxOpenRedirectUrl = ConstantWxPropertiesUtil.WX_OPEN_REDIRECT_URL;
            wxOpenRedirectUrl = URLEncoder.encode(wxOpenRedirectUrl, "utf-8");
            map.put("redirect_uri", wxOpenRedirectUrl); //重定向地址
            map.put("state", System.currentTimeMillis() + ""); //用于保持请求和回调状态，防止csrf攻击，可设置为简单随机数
            return Result.ok(map);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @ApiOperation(value = "微信登录回调")
    @GetMapping("callback")
    public String callback(String code, String state) {
        //根据临时票据code、微信id和秘钥请求微信固定地址https://api.weixin.qq.com/sns/oauth2/access_token?参数
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s") //%s表示占位符
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        //设置占位符的值
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantWxPropertiesUtil.WX_OPEN_APP_ID,
                ConstantWxPropertiesUtil.WX_OPEN_APP_SECRET,
                code);
        try {
            //使用HttpClient请求地址
            String accessTokenInfo = HttpClientUtil.get(accessTokenUrl);
            //从accessTokenInfo中解析openid和access_token
            JSONObject accessTokenObject = JSONObject.parseObject(accessTokenInfo);
            String openid = accessTokenObject.getString("openid");
            String access_token = accessTokenObject.getString("access_token");

            //判断数据库中是否存在扫码人信息
            UserInfo userInfo = userInfoService.getByOpenid(openid);
            if (null == userInfo) {
                //根据openid和access_token请求微信固定地址https://api.weixin.qq.com/sns/userinfo?参数，获取扫码人信息
                StringBuffer baseUserInfoUrl = new StringBuffer()
                        .append("https://api.weixin.qq.com/sns/userinfo")
                        .append("?access_token=%s")
                        .append("&openid=%s");
                String userInfoUrl = String.format(baseUserInfoUrl.toString(), access_token, openid);
                //使用HttpClient请求地址
                String resultInfo = HttpClientUtil.get(userInfoUrl);

                //从userInfo中解析扫码人信息
                JSONObject userObject = JSONObject.parseObject(resultInfo);
                String nickname = userObject.getString("nickname"); //昵称
                String headimgurl = userObject.getString("headimgurl"); //头像

                //将信息添加到数据库中
                userInfo = new UserInfo();
                userInfo.setNickName(nickname);
                userInfo.setOpenid(openid);
                userInfo.setStatus(1);
                userInfoService.save(userInfo);
            }

            //登录
            String name = userInfo.getName();
            //如果名称为空使用昵称
            if (StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();
            }
            //如果昵称为空使用手机号
            if (StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }
            //用于前端判断，如果openid不为空，绑定手机号
            if (!StringUtils.isEmpty(userInfo.getPhone())) {
                openid = "";
            }
            //使用jwt生成token字符串
            String token = JwtHelper.createToken(userInfo.getId(), name);
            //重定向到前端页面
            return "redirect:" + ConstantWxPropertiesUtil.YYGH_BASE_URL +
                    "/weixin/callback?token=" + token +
                    "&openid=" + openid +
                    "&name=" + URLEncoder.encode(name, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
