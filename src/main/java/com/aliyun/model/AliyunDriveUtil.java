package com.aliyun.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.utils.OkHttpUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author falcon
 * @version 1.0.0
 * @ClassName AliyunDrive
 * @Description TODO
 * @createTime 2021年07月17日 15:11:00
 */
public class AliyunDriveUtil {
    private String refreshToken="66dca14e3fff483bbf78a0af6a0cafcd";
    private String accessToken;
    private String realpath;
    private String driveId="469891";
    private String rootPath ="test";
    private Map<String,Object> headers = new HashMap<>();

    /**
     * 刷新token
     * @return
     */
    public  String refreshToken(){
        String jsonStr = OkHttpUtils.builder().url("https://websv.aliyundrive.com/token/refresh")
                // 有参数的话添加参数，可多个
                .addParam("refresh_token", refreshToken)
                // 也可以添加多个
                .addHeader("content-type", "application/json;charset=UTF-8")
                // 如果是true的话，会类似于postman中post提交方式的raw，用json的方式提交，不是表单
                // 如果是false的话传统的表单提交
                .post(true).sync();
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        accessToken = jsonObject.getString("access_token");
        headers.put("authorization",accessToken);
        headers.put("content-type","application/json;charset=UTF-8");
        return accessToken;
    }

    public static void main(String[] args) {
        AliyunDriveUtil aliyunDriveUtil = new AliyunDriveUtil();
        String s = aliyunDriveUtil.refreshToken();
        System.out.println(s);
    }

}
