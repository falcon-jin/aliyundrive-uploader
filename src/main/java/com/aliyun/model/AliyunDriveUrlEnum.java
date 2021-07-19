package com.aliyun.model;

/**
 * @Author: qy
 * @Date: 2021/7/19 15:13
 * @Description:
 */
public enum AliyunDriveUrlEnum {
    /**
     * 刷新token
     */
    REFRESH_TOKEN("https://websv.aliyundrive.com/token/refresh","刷新token"),
    CREATE_FILE("https://api.aliyundrive.com/v2/file/create","创建文件"),
    FILE_LIST("https://api.aliyundrive.com/v2/file/list","创建文件"),
    CREATE_FOLDERr("https://api.aliyundrive.com/v2/file/create","创建文件"),
    GET_DOWNLOAD_URL("https://api.aliyundrive.com/v2/file/get_download_url","创建文件"),
    GET_UPLOAD_URL("https://api.aliyundrive.com/v2/file/get_upload_url","创建文件"),
    COMPLETE_UPLOAD("https://api.aliyundrive.com/v2/file/get_upload_url","创建文件"),

    ;
    private String url;
    private String desc;

    AliyunDriveUrlEnum(String url,String desc) {
        this.url =url;
        this.desc =desc;
    }
    public String getUrl() {
        return url;
    }

    public String getDesc() {
        return desc;
    }

}
