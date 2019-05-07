package com;

/**
 *  Android 常量配置
 */
public class Config {

    /**************************************  接口配置 *********************************************/
    public static final int TIME_OUT = 5000; // 请求超时时间
    public static final int REQUEST_TIME = 0; // VOLLEY重复请求次数
    public static final String HEADER_ACCEPT_KEY = "Accept"; // 请求头Accpet
    public static final String HEADER_ACCEPT_VALUE = "application/json"; // 请求头Accept值
    public static final String HEADER_CONTENT_KEY = "Content-Type"; // 请求头content键
    public static final String HEADER_CONTENT_VALUE = "application/json; charset=UTF-8"; // 请求头content值
    public static final String HEADER_TOKENID_KEY = "tokenid"; // 请求头tokenid

    public static final String SUCCESS = "success";
    // 服务器域名地址
    //    public static final String BASE_URL = "https://ic.vorange.cn/jap/";
    public static final String BASE_URL = "https://test1.vorange.cn/";
    // 获取数据字典
    public static final String CONFIG_URL = BASE_URL+"ins/api/web/configs";
    // 获取系统和单位欢迎图
    public static final String WELCOME_INFO_URL = BASE_URL+"ins/api/web/welcomeinfo";
    //获取人员信息
    public static final String APPGET_PERSION_BY_MOBILE_URL = BASE_URL+"ins/api/app/appgetpersonbymobile";
    //获取个人信息
    public static final String APPGET_PERSION_URL = BASE_URL+"ins/api/app/appgetperson";
    //获取事故ID详细内容
    public static final String CASE_DETAIL_URL = BASE_URL+"ins/api/app/case/casedetail";
    //
    public static final String ATTACH_DOWNLOAD_URL = BASE_URL+"ins/api/attach/download/";
    // 七牛的外链接 baseURL  http://icqn.vorange.cn/
    public static final String QINIU_BASE_URL = "http://icqn.vorange.cn/";
    // 七牛视频缩略图参数
    public static final String QINIU_VIDEO_THUMB = "?vframe/png/offset/0/w/720/h/480";
    //
    public static final String ATTACH_UPLOAD_URL = BASE_URL+"ins/api/attach/upload";
    // 开始报案
    public static final String CASE_STEP_ONE_URL = BASE_URL+"ins/api/app/case/stepone";
    //退出登录
    public static final String APP_LOGOUT_URL = BASE_URL+"ins/api/applogout";
    //
    public static final String CASE_STEP_FOUR_URL = BASE_URL+"ins/api/app/stepfour";
    //
    public static final String CASE_STEP_TWO_URL = BASE_URL+"ins/api/app/case/steptwo";
    //撤销
    public static final String CASE_CANCEL_URL = BASE_URL+"ins/api/app/case/cancel";
    //
    public static final String ATTACH_VIDEO_URL = BASE_URL+"ins/insattach/snap/";
    //
    public static final String CASE_STEP_THREE_URL = BASE_URL+"ins/api/app/case/stepthree";
    //下载APK
    public static final String CLIENT_INFO_URL = BASE_URL+"ins/api/web/clientinfo";
    //查询单位
    public static final String SEARCH_UNIT_URL = BASE_URL+"ins/api/web/searchunit";
    //个人信息更新
    public static final String APP_UPDATE_PERSON_URL = BASE_URL+"ins/api/app/appupdateperson";
    //是否删除
    public static final String CASE_IS_DELETE_URL = BASE_URL+"ins/api/app/isDelete";
    //删除本条记录
    public static final String CASE_DELETE_URL = BASE_URL+"ins/api/app/case/delete";
    //获取保险公司列表
    public static final String INSURANCE_URL = BASE_URL+"ins/api/web/insurance";
    //获取验证码
    public static final String APP_SMS_CODE_URL = BASE_URL+"ins/api/web/appsmscode";
    //登录接口
    public static final String APP_LOGIN_URL = BASE_URL+"ins/api/web/applogin";
    //获取协议
    public static final String AGREEMENT_URL = BASE_URL+"ins/api/web/agreement";
    //注册接口
    public static final String APP_REGISTER_URL = BASE_URL+"ins/api/web/appregister";
    // 获取七牛token
    public static final String QINIU_TOKEN = BASE_URL + "qiniuToken";
    // 获得单位通知消息
    public static final String GET_USER_MSG = BASE_URL + "ins/api/app/getusermsg";
    //单位消息已读
    public static final String READ_USER_MSG = BASE_URL + "ins/api/app/readusermsg";
    // 查询车辆表
    public static final String FIND_CAR = BASE_URL+"ins/api/app/car/findcar";
    // 保存车辆表
    public static final String SAVE_CAR = BASE_URL+"ins/api/app/car/savecar";
    // 获取车辆配置列表
    public static final String FIND_MY_CAR = BASE_URL + "ins/api/app/car/findmycar?personcode=";
    // 查询机器串码是否已存在
    public static final String SEARCH_MACHINE_CODE = BASE_URL + "ins/api/web/searchmachinecode?machineCode=";
    // 保存机器串码
    public static final String SAVE_MACHINE_CODE = BASE_URL + "ins/api/app/savemachinecode";
    // 新建拨打电话记录
    public static final String CONTACT_ADD = BASE_URL + "ins/api/app/contact/add";
    // 新增报案备注信息
    public static final String CASEMSG_ADD = BASE_URL + "ins/api/app/casemsg/add";
    // 修改默认配置信息
    public static final String CHANGE_DEFAULT = BASE_URL + "ins/api/app/car/changedefault?id=";
    // 删除配置信息
    public static final String DELETE_CAR = BASE_URL + "ins/api/app/car/deletecar?id=";
    // 提交评论 /传id表示修改，不传id代表新增评论
    public static final String COMMENT_SAVE = BASE_URL +  "ins/api/app/comment/save";
    // 获取评论列表接口
    public static final String COMMENT_LIST = BASE_URL +  "ins/api/app/comment/list?caseId=";

    /******************       微信分享         **********************/
    public static final String WEIXIN_APPID = "wxcac14fdb469eaf3a";// 微信APPID

    /*********************        EventBus发送的消息         ********************************************/

    public static final String FILE_DOWNLOAD_EXIT_B1 = "9";//小B行车记录仪界面是否停止下载
    public static final String FILE_DOWNLOAD_EXIT_C1 = "10";//小C行车记录仪界面是否停止下载
    public static final String FILE_DOWNLOAD_FINISH_C1 = "11";//小C行车记录仪界面文件下载完毕
    public static final String FILE_DOWNLOAD_FINISH_B1 = "12";//小B行车记录仪界面文件下载完毕

    public static final String ICAMERA_A = "iCameraA";
}
