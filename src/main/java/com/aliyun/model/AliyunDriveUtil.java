package com.aliyun.model;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.utils.OkHttpUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
    private String refreshToken = "66dca14e3fff483bbf78a0af6a0cafcd";
    private String accessToken;
    private String driveId = "469891";
    private String rootPath = "root";
    private Map<String, String> headers = new HashMap<>();

    //上传的文件信息
    private String realpath;
    private String filepath;
    private String filepathHash;
    private String filename;
    private String hash;
    private Long filesize;
    private List<Map<String, Object>> partInfoList;
    private List partUploadUrlList;
    private String fileId;
    private String uploadId;


    /**
     * 刷新token
     *
     * @return
     */
    public String refreshToken() {
        String jsonStr = OkHttpUtils.builder().url(AliyunDriveUrlEnum.REFRESH_TOKEN.getUrl())
                // 有参数的话添加参数，可多个
                .addParam("refresh_token", refreshToken)
                // 也可以添加多个
                .addHeader("content-type", "application/json;charset=UTF-8")
                // 如果是true的话，会类似于postman中post提交方式的raw，用json的方式提交，不是表单
                // 如果是false的话传统的表单提交
                .post(true).sync();
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        accessToken = jsonObject.getString("access_token");
        headers.put("authorization", accessToken);
        headers.put("accept", "application/json, text/plain, */*");
        headers.put("origin", "https://www.aliyundrive.com");
        headers.put("referer", "https://www.aliyundrive.com/");
        headers.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.128 Safari/537.36");
        return accessToken;
    }

    /**
     * 加载文件
     *
     * @param file 要上传的文件
     */
    private void loadFile(File file) {
        //文件相对路径
        filepath = file.getAbsolutePath();
        //文件相对路径16进制哈希值
        filepathHash = DigestUtil.sha1Hex(filepath);
        //真实路径
        realpath = file.getPath();
        //文件名称
        filename = file.getName();
        //文件16进制哈希值
        hash = DigestUtil.sha1Hex(file).toLowerCase();
        //文件大小
        filesize = file.length();
        //分段上传
        partInfoList = new ArrayList<>();
        //分成几段上传
        double ceil = Math.ceil(filesize / (1024 * 1024 * 100));
        for (int i = 0; i <= ceil; i++) {
            HashMap<String, Object> e = new HashMap<>();
            e.put("part_number", i + 1);
            partInfoList.add(e);
        }
    }

    /**
     * 预长传文件
     *
     * @param parentFileId
     * @return
     */
    private JSONObject preUploadFile(String parentFileId) {
        Map<String, Object> map = new HashMap<>();
        map.put("drive_id", driveId);
        map.put("part_info_list", partInfoList);
        map.put("parent_file_id", parentFileId);
        map.put("name", filename);
        map.put("type", "file");
        map.put("check_name_mode", "auto_rename");
        map.put("size", filesize);
        map.put("content_hash", hash);
        map.put("content_hash_name", "sha1");
        String jsonStr = OkHttpUtils.builder().url(AliyunDriveUrlEnum.CREATE_FILE.getUrl())
                .addParams(map)
                .addHeaders(headers)
                .post(true).sync();
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Boolean aBoolean = checkAuth(jsonObject);
        if (!aBoolean) {
            throw new RuntimeException("认证异常");
        }
        partUploadUrlList = jsonObject.getJSONArray("part_info_list");
        fileId = jsonObject.getString("file_id");
        uploadId = jsonObject.getString("upload_id");
        return jsonObject;
    }

    /**
     * 获取文件上传路径
     */
    private void getUploadUrl() {
        Map<String, Object> map = new HashMap<>();
        map.put("drive_id", driveId);
        map.put("part_info_list", partInfoList);
        map.put("file_id", fileId);
        map.put("upload_id", uploadId);
        String jsonStr = OkHttpUtils.builder().url(AliyunDriveUrlEnum.GET_UPLOAD_URL.getUrl())
                .addParams(map)
                .addHeaders(headers)
                .post(true).sync();
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Boolean aBoolean = checkAuth(jsonObject);
        if (!aBoolean) {
            throw new RuntimeException("认证异常");
        }
        partUploadUrlList = jsonObject.getJSONArray("part_info_list");
    }

    /**
     * 上传文件
     *
     * @param parentFileId 父级目录id
     * @param file 要上传的文件
     */
    public void doUpload(String parentFileId,File file) throws Exception {
        //刷新token
        refreshToken();
        //加载文解析文件
        loadFile(file);
        //预上传文件
        JSONObject jsonObject = preUploadFile(parentFileId);
        Boolean rapidUpload = jsonObject.getBoolean("rapid_upload");
        //如果rapidUpload =true 妙传成功
        if (Objects.nonNull(rapidUpload) && rapidUpload) {
            System.out.println("上传成功");
            return;
        }
        if (CollectionUtil.isNotEmpty(partUploadUrlList)) {
            FileInputStream fis = new FileInputStream(file);
            //分段上传文件
            for (Object o : partUploadUrlList) {

                JSONObject json = (JSONObject) o;
                String uploadUrl = json.getString("upload_url");
            }
        }
        System.out.println("上传成功");
    }


    /**
     * 根据文件id获取文件下载路径
     *
     * @param fileId
     * @return 文件下载路径
     */
    public String getDownloadUrl(String fileId) {
        Map<String, Object> map = new HashMap<>();
        map.put("drive_id", driveId);
        map.put("file_id", fileId);
        String jsonStr = OkHttpUtils.builder().url(AliyunDriveUrlEnum.GET_DOWNLOAD_URL.getUrl())
                .addParams(map)
                .addHeaders(headers)
                .post(true).sync();
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        String url = jsonObject.getString("url");
        return url;
    }

    /**
     * 下载文件
     *
     * @param fileId   要下载的文件id
     * @param filePath 文件下载保存路径  建议传绝对路径
     * @throws IOException
     */
    public void downFile(String fileId, String filePath) throws Exception {
        if(StrUtil.isBlank(fileId)){
            throw new Exception("文件id不能为空");
        }
       if(StrUtil.isNotBlank(filePath)){
           filePath= filePath.replace("\\","/");
           if(!filePath.endsWith("/")){
               filePath+="/";
           }
       }
       //获取token
        refreshToken();
       //获取文件下载路径
        String downloadUrl = getDownloadUrl(fileId);
        //解析downloadUrl 获取文件名称
        String downFileName = getDownFileName(downloadUrl);
        InputStream inputStream = OkHttpUtils.builder().url(downloadUrl)
                .addHeaders(headers)
                .get().syncDown();
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        BufferedInputStream bis = null;
        try {
            fos = new FileOutputStream(filePath+downFileName); //没有下载完毕就将文件的扩展名命名.bak
            dos = new DataOutputStream(fos);
            bis = new BufferedInputStream(inputStream);
            System.out.println("正在接收文件...");
            int len = 0;
            byte[] bt = new byte[1024];
            while ((len = bis.read(bt)) > 0) {//循环获取文件
                fos.write(bt, 0, len);
            }
            fos.flush();
            System.out.println("文件接收完毕...");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (dos != null) {
                dos.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * 阿里云创建目录
     *
     * @param folderName     创建的目录名称
     * @param parentFolderId 上级目录id
     */
    public Boolean createFolder(String folderName, String parentFolderId) {
        String jsonStr = OkHttpUtils.builder().url(AliyunDriveUrlEnum.CREATE_FOLDERr.getUrl())
                // 有参数的话添加参数，可多个
                .addParam("drive_id", driveId)
                .addParam("parent_file_id", parentFolderId)
                .addParam("name", folderName)
                .addParam("check_name_mode", "refuse")
                .addParam("type", "folder")
                // 也可以添加多个
                .addHeader("content-type", "application/json;charset=UTF-8").addHeader("authorization", headers.get("authorization"))
                // 如果是true的话，会类似于postman中post提交方式的raw，用json的方式提交，不是表单
                // 如果是false的话传统的表单提交
                .post(true).sync();

        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Boolean aBoolean = checkAuth(jsonObject);
        return aBoolean;
    }

    /**
     * 检查阿里云权限是否过期
     *
     * @param jsonObject 调用api返回值
     * @return
     */
    public Boolean checkAuth(JSONObject jsonObject) {
        Boolean flag = true;
        if (StrUtil.equals(jsonObject.getString("code"), "AccessTokenInvalid")) {
            //'AccessToken已失效，尝试刷新AccessToken中'
            flag = false;
            refreshToken();
        }
        return flag;
    }

    /**
     * 根据文件夹id获取网盘文件列表
     *
     * @param parentFolderId 当前目录id
     * @return
     */
    public JSONArray getFileList(String parentFolderId) {

        if (StrUtil.isBlank(parentFolderId)) {
            parentFolderId = "root";
        }
        JSONArray maps = new JSONArray();
        String jsonStr = OkHttpUtils.builder().url(AliyunDriveUrlEnum.FILE_LIST.getUrl())
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
                .addHeader("content-type", "application/json;charset=UTF-8").addHeaders(headers)
                .post(true).sync();
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Boolean aBoolean = checkAuth(jsonObject);

        if (aBoolean) {
            maps = (JSONArray) jsonObject.get("items");
            System.out.println(maps);
        }
        return maps;
    }

    /**
     * 从文件下载路径获取文件名称
     * @param url
     * @return
     */
    private String getDownFileName(String url){
        String fileName = "";
        String decode = URLDecoder.decode(url,StandardCharsets.UTF_8);
        String[] split = decode.split("''");
        if(split.length>1){
            String[] split1 = split[1].split("&");
            if(split1.length>0){
                    fileName=split1[0];
            }
        }
        return fileName;
    }


    public static void main(String[] args) throws Exception {
        AliyunDriveUtil aliyunDriveUtil = new AliyunDriveUtil();
        aliyunDriveUtil.doUpload("root",new File("AliyunDrive.py"));
        //aliyunDriveUtil.downFile("60bc41cc548c162fd7d54c289a40c6c020046eb4", "D:/javaCode/myCode/gitHub/aliyundrive-uploader/");
        //System.out.println(s);


    }

}
