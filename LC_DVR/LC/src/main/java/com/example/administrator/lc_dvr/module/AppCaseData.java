package com.example.administrator.lc_dvr.module;

import com.example.administrator.lc_dvr.bean.Car;
import com.example.administrator.lc_dvr.bean.Casemsg;
import com.example.administrator.lc_dvr.bean.InsCompany;
import com.example.administrator.lc_dvr.bean.MachineCode;
import com.example.administrator.lc_dvr.bean.ServicePerson;
import com.example.administrator.lc_dvr.bean.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2019/01/08
 *   desc   :
 *  version :
 * </pre>
 */
public class AppCaseData {

    // 用户信息
    public static String nick_name;  // 用户昵称
    public static String user_remark; // 用户备注
    public static String headURL; // 用户头像

    public static String mobile; // 用户手机号
    public static String username; // 用户名
    public static String logoid; // 用户头像URL
    public static String personcode; // 用户唯一ID
    // 单位信息
    public static String unitcode; // 单位唯一识别ID
    public static String unitname; // 单位名称
    public static int unitkind; // 单位类型
    public static String shortcode; // 单位快捷码
    public static String inscode; // 投保公司code

    public static boolean caseIsEdit = true; // 案子是否可编辑

    public static String serverPersonName; // 事故专员名称
    public static String serverPersonMobile; // 事故专员手机号
    public static String caseId; // 案子ID
    public static String reportStatus; // 报案状态
    public static String lossStatus; // 定损状态
    public static String paymentStatus; // 理赔状态


    /****************报案主页面内容*****************/
    public static String reportTime; // 报案时间
    public static String accidentTime; // 事故时间
    public static String informantName; // 报案人姓名
    public static String informantPhone; // 报案人电话

    /*************** 默认事故详情*********/
    public static int carCount = 1; //   1,是单车，2是多车
    public static int accidentResponsibility = 1; // 事故责任类型：1 全部责任，2没有责任，3主要责任，4 同等责任，5 次要责任
    public static int isPhysicalDamage = 2 ; // 是否有物损，1有物损，2 没有物损
    public static int isWounded = 2; // 是否有人伤 1有人伤，2 没有人伤
    public static int isNormalDriving = 1; // 是否能正常行驶 1 可正常行驶，2不能正常行驶
    public static int isScene = 1; // 是否在事故现场：1 在事故现场，2 不在事故现场

    /************* 报案字段 ************/

    public static String geographicalPosition; // 位置
    public static String plateNumber;  // 车牌号
    public static String label_models; // 厂牌车型
    public static String other_plate_number; // 对方车牌号
    public static String other_label_models; // 对方车厂牌车型
    public static String other_name; // 对方姓名
    public static String other_phone_number; // 对方手机号

    public static String new_remark ; // 新增备注内容
    public static List<Casemsg> remarkList = new ArrayList(); // 备注历史内容

    /*********************打开的配置索引内容**********************/

    public static Car carDetail = new Car(); // 打开的索引内容
    public static Unit unitDetail = new Unit(); // 打开的配置对应单位内容
    public static ServicePerson servicePersonDetail = new ServicePerson(); // 打开的配置对应的事故专员内容
    public static InsCompany insCompanyDetail = new InsCompany(); // 打开的配置对应的投保公司内容
    public static List<MachineCode> machineCodeList = new ArrayList<>(); // 要显示的列表
    public static MachineCode machineCode = new MachineCode(); // 要上传的MachineCode

    /*********************   **************************/



















}
