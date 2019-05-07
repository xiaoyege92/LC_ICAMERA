package com.example.administrator.lc_dvr.common.retrofit;


import com.example.administrator.lc_dvr.bean.Car;
import com.example.administrator.lc_dvr.bean.Case;
import com.example.administrator.lc_dvr.bean.CasePayment;
import com.example.administrator.lc_dvr.bean.Casemsg;
import com.example.administrator.lc_dvr.bean.Comment;
import com.example.administrator.lc_dvr.bean.Contact;
import com.example.administrator.lc_dvr.bean.MachineCode;
import com.example.administrator.lc_dvr.bean.Unit;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2019/03/29
 *   desc   :
 *  version :
 * </pre>
 */
public interface Api {
    /**
     * 获取获得单位通知消息
     *
     * @param headers
     * @param personcde
     * @return
     */
    @GET("ins/api/app/getusermsg")
    Call<ResponseBody> getUnitMessage(@HeaderMap Map<String, String> headers, @Query("personcode") String personcde);

    /**
     * 获取案件列表
     *
     * @param headers
     * @param page
     * @param pagesize
     * @return
     */
    @GET("ins/api/app/case/mycaselist.do")
    Call<ResponseBody> getCaseList(@HeaderMap Map<String, String> headers, @Query("page") int page, @Query("pagesize") int pagesize);

    /**
     * 获取车辆配置列表
     *
     * @param headers
     * @param personcde
     * @return
     */
    @GET("ins/api/app/car/findmycar")
    Call<ResponseBody> getCarList(@HeaderMap Map<String, String> headers, @Query("personcode") String personcde);

    /**
     * 获取案件详情
     *
     * @param headers
     * @param map
     * @return
     */
    @POST("ins/api/app/case/casedetail")
    Call<ResponseBody> getCaseDetail(@HeaderMap Map<String, String> headers, @Body Map<String, String> map);


    /**
     * 通过手机号获取个人信息
     *
     * @param headers
     * @param map
     * @return
     */
    @POST("ins/api/app/appgetpersonbymobile")
    Call<ResponseBody> getPersonInfoByMobile(@HeaderMap Map<String, String> headers, @Body Map<String, String> map);

    /**
     * 获取个人信息
     *
     * @param headers
     * @return
     */
    @POST("ins/api/app/appgetperson")
    Call<ResponseBody> getPersonalInfo(@HeaderMap Map<String, String> headers);


    /**
     * 获取七牛token
     *
     * @param
     * @return
     */
    @POST("qiniuToken")
    Call<ResponseBody> getQiniuToken(@HeaderMap Map<String, String> headers, @Query("n") int n);

    /**
     * 上传理赔资料接口
     *
     * @param headers
     * @return
     */
    @POST("ins/api/app/case/stepthree")
    Call<ResponseBody> stepThree(@HeaderMap Map<String, String> headers, @Body CasePayment casePayment);

    /**
     * 上传定损资料接口
     *
     * @param headers
     * @return
     */
    @POST("ins/api/app/case/stepone")
    Call<ResponseBody> stepOne(@HeaderMap Map<String, String> headers,@Body Map<String, String> map);

    /**
     * 上传定损照片
     * @param headers
     * @param map
     * @return
     */
    @POST("ins/api/app/case/steptwo")
    Call<ResponseBody> stepTwo(@HeaderMap Map<String, String> headers,@Body Case map);

    /**
     * 发送短信
     * @param headers
     * @param caseId
     * @param type
     * @return
     */
    @GET("ins/api/sendsmstip")
    Call<ResponseBody> sendSMS(@HeaderMap Map<String, String> headers, @Query("caseId") String caseId,@Query("type") int type);

    /**
     *  单位通知消息改为已读
     *
     * @param headers
     * @param id
     * @return
     */
    @GET("ins/api/app/readusermsg")
    Call<ResponseBody> readUserMSG(@HeaderMap Map<String, String> headers, @Query("id") int id);

    /**
     * 更改默认配置信息
     * @param headers
     * @param id
     * @return
     */
    @POST("ins/api/app/car/changedefault")
    Call<ResponseBody> changeDefault(@HeaderMap Map<String, String> headers, @Query("id") int id);

    /**
     * 删除车辆配置
     * @param headers
     * @param id
     * @return
     */
    @POST("ins/api/app/car/deletecar")
    Call<ResponseBody> deleteCar(@HeaderMap Map<String, String> headers, @Query("id") int id);

    /**
     * 保车辆配置信息
     * @param headers
     * @param car
     * @return
     */
    @POST("ins/api/app/car/savecar")
    Call<ResponseBody> saveCar(@HeaderMap Map<String, String> headers, @Body Car car);

    /**
     * 保存机身串码
     * @param headers
     * @param machineCode
     * @return
     */
    @POST("ins/api/app/savemachinecode")
    Call<ResponseBody> saveMachineCode(@HeaderMap Map<String, String> headers, @Body MachineCode machineCode);

    /**
     * 保存评论
     * @param headers
     * @param comment
     * @return
     */
    @POST("ins/api/app/comment/save")
    Call<ResponseBody> saveComment(@HeaderMap Map<String, String> headers, @Body Comment comment);

    /**
     *  获取车辆配置信息
     *
     * @param headers
     * @param personcode
     * @return
     */
    @POST("ins/api/app/car/findmycar")
    Call<ResponseBody> findMyCar(@HeaderMap Map<String, String> headers, @Query("personcode") String personcode);

    /**
     * 新增备注信息
     * @param headers
     * @param casemsg
     * @return
     */
    @POST("ins/api/app/casemsg/add")
    Call<ResponseBody> addCaseMsg(@HeaderMap Map<String, String> headers, @Body Casemsg casemsg);

    /**
     * 获取数据字典
     * @param headers
     * @return
     */
    @POST("ins/api/web/configs")
    Call<ResponseBody> getConfigs(@HeaderMap Map<String,String> headers);

    /**
     * 更新个人信息
     * @param headers
     * @return
     */
    @POST("ins/api/app/appupdateperson")
    Call<ResponseBody> appUpdatePerson(@HeaderMap Map<String,String> headers, @Body Map<String,String> map);

    /**
     * 对案件进行销案
     * @param headers
     * @param map
     * @return
     */
    @POST("ins/api/app/case/cancel")
    Call<ResponseBody> revokeCase(@HeaderMap Map<String,String> headers, @Body Map<String,String> map);

    /**
     * 更新
     * @param headers
     * @param map
     * @return
     */
    @POST("ins/api/web/clientinfo")
    Call<ResponseBody> updateAPP(@HeaderMap Map<String,String> headers, @Body Map<String,String> map);

    /**
     *  获取保险公司列表
     * @param headers
     * @return
     */
    @POST("ins/api/web/insurance")
    Call<ResponseBody> getInsuranceCompanyList(@HeaderMap Map<String,String> headers);

    /**
     * 获取单位列表
     * @param headers
     * @return
     */
    @POST("ins/api/web/searchunit")
    Call<ResponseBody> searchUnit(@HeaderMap Map<String,String> headers,@Body Map<String,String> map);

    /**
     * 获取短信验证码
     * @param headers
     * @param map
     * @return
     */
    @POST("ins/api/web/appsmscode")
    Call<ResponseBody> getSMSCode(@HeaderMap Map<String,String> headers,@Body Map<String,String> map);

    /**
     *  登录
     * @param headers
     * @param map
     * @return
     */
    @POST("ins/api/web/applogin")
    Call<ResponseBody> appLogin(@HeaderMap Map<String,String> headers,@Body Map<String,String> map);

    /**
     * 注册
     * @param headers
     * @param map
     * @return
     */
    @POST("ins/api/web/appregister")
    Call<ResponseBody> appRegister(@HeaderMap Map<String,String> headers,@Body Map<String,String> map);

    /**
     * 获取协议内容
     * @param headers
     * @return
     */
    @POST("ins/api/web/agreement")
    Call<ResponseBody> getAgreement(@HeaderMap Map<String,String> headers);

    /**
     * 获取欢迎图
     * @param headers
     * @param map
     * @return
     */
    @POST("ins/api/web/welcomeinfo")
    Call<ResponseBody> getWelcomInfo(@HeaderMap Map<String,String> headers,@Body Map<String,String> map);

    /**
     * 新增聊天记录
     * @param headers
     * @param contact
     * @return
     */
    @POST("ins/api/app/contact/add")
    Call<ResponseBody> addContact(@HeaderMap Map<String,String> headers, @Body Contact contact);

    /**
     * 获取评论列表
     * @param headers
     * @param caseid
     * @return
     */
    @POST("ins/api/app/comment/list")
    Call<ResponseBody> getCaseCommentList(@HeaderMap Map<String,String> headers, @Query("caseId") String caseid);

}
