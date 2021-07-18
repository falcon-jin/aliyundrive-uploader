package com.aliyun.model;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.utils.OkHttpUtils;

import java.io.File;
import java.security.SecureRandom;
import java.util.*;

/**
 * @author falcon
 * @version 1.0.0
 * @ClassName AliyunDrive
 * @Description TODO
 * @createTime 2021年07月17日 15:11:00
 */
public class AliyunDriveUtil {
    //刷新token
    private String refreshToken="66dca14e3fff483bbf78a0af6a0cafcd";
    private String accessToken;
    private String driveId="469891";
    private String rootPath ="test";
    private Map<String,String> headers = new HashMap<>();

    //上传的文件信息
    private String realpath;
    private String filepath;
    private String filepathHash;
    private String filename;
    private String hash;
    private Long filesize;
    private List<Map<String,Object>> partInfoList;


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

    /**
     * 加载文件
     * @param file 要上传的文件
     */
    public void loadFile(File file){
        filepath = file.getAbsolutePath();
        filepathHash = DigestUtil.sha1Hex(filepath);
        realpath = file.getPath();
        filename = file.getName();
        hash = DigestUtil.sha1Hex(file);
        filesize = file.length();
        partInfoList = new ArrayList<>();
        for (int i = 0; i < Math.ceil(filesize / 104857600); i++) {
            HashMap<String, Object> e = new HashMap<>();
            e.put("part_number",i+1);
            partInfoList.add(e);
        }
    }

    /**
     * 阿里云创建目录
     * @param folderName 创建的目录名称
     * @param parentFolderId 上级目录id
     */
    public Boolean createFolder(String folderName,String parentFolderId){
        String jsonStr = OkHttpUtils.builder().url("https://api.aliyundrive.com/v2/file/create")
                // 有参数的话添加参数，可多个
                .addParam("drive_id", driveId)
                .addParam("parent_file_id", parentFolderId)
                .addParam("name", folderName)
                .addParam("check_name_mode", "refuse")
                .addParam("type", "folder")
                // 也可以添加多个
                .addHeader("content-type", "application/json;charset=UTF-8").addHeader("authorization",headers.get("authorization"))
                // 如果是true的话，会类似于postman中post提交方式的raw，用json的方式提交，不是表单
                // 如果是false的话传统的表单提交
                .post(true).sync();

        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Boolean aBoolean = checkAuth(jsonObject);
        return aBoolean;
    }

    public Boolean checkAuth(JSONObject  jsonObject){
        Boolean flag = true;
        if(StrUtil.equals(jsonObject.getString("code"),"AccessTokenInvalid")){
            //'AccessToken已失效，尝试刷新AccessToken中'
            flag=false;
           refreshToken();
        }
        return flag;
    }

    /**
     * 根据文件夹id获取网盘文件列表
     * @param parentFolderId
     * @return
     */
    public JSONArray getFileList(String parentFolderId){

        if(StrUtil.isBlank(parentFolderId)){
            parentFolderId="root";
        }
        JSONArray maps = new JSONArray();
        String jsonStr = OkHttpUtils.builder().url("https://api.aliyundrive.com/v2/file/list")
                // 有参数的话添加参数，可多个
                .addParam("drive_id", driveId)
                .addParam("parent_file_id", parentFolderId)
                .addParam("all", false)
                .addParam("fields", "*")
                .addParam("image_thumbnail_process", "image/resize,w_1920/format,jpeg")
                .addParam("limit", 100)
                .addParam("order_by", "updated_at")
                .addParam("order_direction", "DESC")
                .addParam("url_expire_sec", 1600)
                .addParam("video_thumbnail_process", "video/snapshot,t_0,f_jpg,ar_auto,w_800")
                // 也可以添加多个
                .addHeader("content-type", "application/json;charset=UTF-8").addHeader("authorization",headers.get("authorization"))
                // 如果是true的话，会类似于postman中post提交方式的raw，用json的方式提交，不是表单
                // 如果是false的话传统的表单提交
                .post(true).sync();
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Boolean aBoolean = checkAuth(jsonObject);

        if(aBoolean){
            maps = (JSONArray) jsonObject.get("items");
            System.out.println(maps);
        }
        return maps;
    }

    public static void main(String[] args) {
        AliyunDriveUtil aliyunDriveUtil = new AliyunDriveUtil();
        //更新token
        String s = aliyunDriveUtil.refreshToken();
        //加载要上传的文件
        //aliyunDriveUtil.loadFile(new File("D:\\yangdengjin\\test\\aliyundrive-uploader\\example.config.json"));
        //创建目录
        //aliyunDriveUtil.createFolder();
        JSONArray root = aliyunDriveUtil.getFileList("root");
        System.out.println(s);
    }

}
