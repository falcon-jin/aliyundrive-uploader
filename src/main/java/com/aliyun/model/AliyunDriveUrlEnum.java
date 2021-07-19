package com.aliyun.model;

/**
 * 阿里云盘接口枚举类
 *
 * @Author: qy
 * @Date: 2021/7/19 15:13
 * @Description:
 */
public enum AliyunDriveUrlEnum {
    /**
     * 刷新token
     */
    REFRESH_TOKEN("https://websv.aliyundrive.com/token/refresh", "刷新token"),
    /**
     * 创建文件
     */
    CREATE_FILE("https://api.aliyundrive.com/v2/file/create", "创建文件"),
    /**
     * 获取指定目录下的文件列表
     */
    FILE_LIST("https://api.aliyundrive.com/v2/file/list", "获取指定目录下的文件列表"),
    /**
     * 创建文件夹
     */
    CREATE_FOLDER("https://api.aliyundrive.com/v2/file/create", "创建文件夹"),
    /**
     * 获取文件下载地址
     */
    GET_DOWNLOAD_URL("https://api.aliyundrive.com/v2/file/get_download_url", "获取文件下载地址"),
    /**
     * 获取文件上传地址
     */
    GET_UPLOAD_URL("https://api.aliyundrive.com/v2/file/get_upload_url", "获取文件上传地址"),
    /**
     * 结束分片上传
     */
    COMPLETE_UPLOAD("https://api.aliyundrive.com/v2/file/get_upload_url", "结束分片上传");
    /**
     * 请求路径
     */
    private String url;
    /**
     * 枚举描述信息
     */
    private String desc;

    AliyunDriveUrlEnum(String url, String desc) {
        this.url = url;
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public String getDesc() {
        return desc;
    }

}
