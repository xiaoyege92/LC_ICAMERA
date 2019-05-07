package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.bean.Casemsg;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.ListDataSave;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.AppCaseData;
import com.example.administrator.lc_dvr.module.lc_dvr.ReportHelp;
import com.example.administrator.lc_dvr.module.login_registration.InsuranceCompanyList;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by yangboru on 2018/1/10.
 * 开始报案
 */

public class ImmediatelyReport extends BaseActivity implements OnDateSetListener {

    private TextView reportTime;  // 报案日期时间
    private TextView accidentTime;  // 事故日期时间
    private EditText informantName;  // 报案人姓名
    private EditText informantPhone;  // 报案人电话

    private int unitkind;
    private ListDataSave dataSave;
    private RelativeLayout timeSelecte; // 时间选择框
    private TimePickerDialog mDialogAll;
    private boolean isDemo;//是否为模拟报案，是true 否 false
    private TextView tv_title;  // title 标题

    private RelativeLayout rl_remark; // 备注信息
    private RelativeLayout rl_report_info; // 报案信息
    private RelativeLayout rl_accident_info; // 事故信息
    private ImageView iv_take_phone; // 事故专员电话

    private TextView tv_insurance_company; // 投保公司
    private TextView tv_unit_name; // 单位名称
    private TextView tv_server_name; // 事故专员

    private KProgressHUD kProgressHUD;

    @Override
    protected void onResume() {
        super.onResume();
        //获得保险公司名字
        if (!"".equals(PreferenceUtil.getString("insuranceCompany", ""))) {
            AppCaseData.inscode = VLCApplication.insuranceCompanyDictionary.get(PreferenceUtil.getString("insuranceCompany", ""));
            tv_insurance_company.setText(PreferenceUtil.getString("insuranceCompany", ""));
            if ("空".equals(PreferenceUtil.getString("insuranceCompany", ""))) {
                tv_insurance_company.setText("请选择投保公司");
            }
        } else {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int setViewId() {
        return R.layout.immediately_report_layout;
    }

    @Override
    protected void findView() {
        iv_take_phone = (ImageView) findViewById(R.id.iv_take_phone);
        rl_remark = (RelativeLayout) findViewById(R.id.rl_remark);
        rl_report_info = (RelativeLayout) findViewById(R.id.rl_report_info);
        rl_accident_info = (RelativeLayout) findViewById(R.id.rl_accident_info);

        reportTime = (TextView) findViewById(R.id.reportTime);
        accidentTime = (TextView) findViewById(R.id.accidentTime);
        informantName = (EditText) findViewById(R.id.informantName);
        informantPhone = (EditText) findViewById(R.id.informantPhone);
        timeSelecte = (RelativeLayout) findViewById(R.id.timeSelecte);
        tv_title = (TextView) findViewById(R.id.tv_title);

        tv_insurance_company = (TextView) findViewById(R.id.tv_insurance_company);
        tv_unit_name = (TextView) findViewById(R.id.tv_unit_name);
        tv_server_name = (TextView) findViewById(R.id.tv_server_name);

    }

    @Override
    protected void init() {

        PreferenceUtil.getString("insuranceCompany", null);
        // 初始化报案内容
        AppCaseData.new_remark = "";
        AppCaseData.inscode = "";
        AppCaseData.remarkList.clear();
        AppCaseData.carCount = 1;
        AppCaseData.accidentResponsibility = 1;
        AppCaseData.isPhysicalDamage = 2;
        AppCaseData.isWounded = 2;
        AppCaseData.isNormalDriving = 1;
        AppCaseData.isScene = 1;
        AppCaseData.caseIsEdit = true;// 可编辑

        // 是否为模拟报案
        isDemo = "1".equals(PreferenceUtil.getString("isdemo", "0"));
        if (isDemo) {
            tv_title.setText("模拟报案");
        } else {
            tv_title.setText("报案");
        }

        //初始化时间选择器
        long tenYears = 10L * 365 * 1000 * 60 * 60 * 24L;
        mDialogAll = new TimePickerDialog.Builder()
                .setCallBack(this)
                .setTitleStringId(null)
                .setCancelStringId("取消")
                .setSureStringId("确定")
                .setYearText("年")
                .setMonthText("月")
                .setDayText("日")
                .setHourText("时")
                .setMinuteText("分")
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis() - tenYears)
                .setMaxMillseconds(System.currentTimeMillis())
                .setCurrentMillseconds(System.currentTimeMillis())
                .setThemeColor(getResources().getColor(R.color.theme_background))
                .setType(Type.ALL)
                .setWheelItemTextNormalColor(getResources().getColor(R.color.black))
                .setWheelItemTextSelectorColor(getResources().getColor(R.color.black))
                .setWheelItemTextSize(14)
                .build();

        //用来保存list到本地
        dataSave = new ListDataSave(this, "baiyu");

        //先清空一下数据
        VLCApplication.casepictures.clear();
        VLCApplication.more_a.clear();
        VLCApplication.more_b.clear();
        VLCApplication.more_c.clear();
        VLCApplication.more_d.clear();
        VLCApplication.more_e.clear();
        VLCApplication.video.clear();
        VLCApplication.one_a.clear();
        VLCApplication.one_b.clear();
        VLCApplication.one_c.clear();

        //先清空一下数据，下面这些数据，当状态为事故照片有问题，需要您重新提交，上传图片时有用
        dataSave.setDataList("reportPhotoArr1", new ArrayList<String>());
        dataSave.setDataList("reportPhotoArr2", new ArrayList<String>());
        dataSave.setDataList("reportPhotoArr4", new ArrayList<String>());
        dataSave.setDataList("reportPhotoArr5", new ArrayList<String>());
        dataSave.setDataList("wholeCarArr", new ArrayList<String>());
        dataSave.setDataList("realVideoNameArr", new ArrayList<String>());

        //设置报案时间和事故时间
        SimpleDateFormat formatterdate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date curDate = new Date(System.currentTimeMillis());
        String format = formatterdate.format(curDate);
        reportTime.setText(format);
        accidentTime.setText(format);
    }

    @Override
    protected void initEvents() {
        //点击事故时间时的响应
        timeSelecte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出时间选择器
                mDialogAll.show(getSupportFragmentManager(), "all");
            }
        });

        // 选择投保公司
        tv_insurance_company.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断是否联网
                if (!NetUtils.isNetworkConnected(ImmediatelyReport.this)) {
                    ToastUtils.showNomalShortToast(ImmediatelyReport.this, getString(R.string.network_off));
                    return;
                }
                //跳转到保险公司列表界面
                Intent intent = new Intent(ImmediatelyReport.this, InsuranceCompanyList.class);
                startActivity(intent);
            }
        });

        // 事故专员电话
        iv_take_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImmediatelyReport.this, ReportHelp.class);
                intent.putExtra("phoneText", PreferenceUtil.getString("user_mobile", ""));
                intent.putExtra("isSeekHelp", true);
                startActivity(intent);
            }
        });
        // 备注信息
        rl_remark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImmediatelyReport.this, RemarkInfoActivity.class);
                startActivity(intent);
            }
        });
        // 报案信息
        rl_report_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImmediatelyReport.this, ReportInfoActivity.class);
                startActivity(intent);
            }
        });
        // 事故信息
        rl_accident_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImmediatelyReport.this, AccidentInfoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void loadData() {
        getProsonConfigInfo();
    }

    /**
     * 退出当前的页面
     *
     * @param view
     */
    public void closeImmediatelyReport(View view) {
        finish();
    }

    /**
     * 点击开始报案btn时的响应
     *
     * @param view
     */
    public void startReportTap(View view) {

        if (!isFieldNull()) {//如果必填字段有空的
            return;
        }
        if (!NetUtils.isNetworkConnected(ImmediatelyReport.this)) {//判断是否联网
            ToastUtils.showNomalShortToast(ImmediatelyReport.this, getString(R.string.network_off));
            return;
        }
        showProgress("拼命上传中...");
        Map<String, String> map = new HashMap<>();
        // 订单类型
        map.put("casetype", Config.ICAMERA_A);
        // 报案时间
        map.put("casedate", reportTime.getText().toString());
        // 事故时间
        map.put("accidentdate", accidentTime.getText().toString());
        //报案人联系方式
        map.put("mobile", informantPhone.getText().toString());
        //报案人姓名
        map.put("username", informantName.getText().toString());
        // 投保公司
        map.put("inscode", AppCaseData.inscode);
        // 客户类型
        map.put("clientType", "0");
        /***   报案信息  ***/
        //出险地点
        map.put("accidentaddress", AppCaseData.geographicalPosition);
        //车牌号
        map.put("carnumber", AppCaseData.plateNumber);
        // 厂牌车型
        map.put("cartype", AppCaseData.label_models);
        // 三者车牌号
        map.put("threeCarnumber", AppCaseData.other_plate_number);
        // 三者厂牌车型
        map.put("threeCartype", AppCaseData.other_label_models);
        // 三者姓名
        map.put("threeUsername", AppCaseData.other_name);
        // 三者手机号
        map.put("threeMobile", AppCaseData.other_phone_number);

        /***   事故信息  ***/
        //事故类型
        if (AppCaseData.carCount == 1) {
            map.put("accidentkind", "单车");
        } else {
            map.put("accidentkind", "多车");
        }
        // 事故责任类型
        if (AppCaseData.accidentResponsibility == 1) {
            map.put("accident", "全部责任");
        } else if (AppCaseData.accidentResponsibility == 2) {
            map.put("accident", "没有责任");
        } else if (AppCaseData.accidentResponsibility == 3) {
            map.put("accident", "主要责任");
        } else if (AppCaseData.accidentResponsibility == 4) {
            map.put("accident", "同等责任");
        } else if (AppCaseData.accidentResponsibility == 5) {
            map.put("accident", "次要责任");
        }
        // 是否有物损
        if (AppCaseData.isPhysicalDamage == 1) {
            map.put("goodinjure", "是");
        } else {
            map.put("goodinjure", "否");
        }
        // 是否有人伤
        if (AppCaseData.isWounded == 1) {
            map.put("personinjure", "是");
        } else {
            map.put("personinjure", "否");
        }
        //是否事故现场
        if (AppCaseData.isScene == 1) {
            map.put("accidentscene", "是");
        } else {
            map.put("accidentscene", "否");
        }
        // 是否能正常行驶
        if (AppCaseData.isNormalDriving == 1) {
            map.put("carcanmove", "是");
        } else {
            map.put("carcanmove", "否");
        }
        //是否为模拟数据
        map.put("isdemo", PreferenceUtil.getString("isdemo", "0"));

        Call<ResponseBody> call = RetrofitManager.getInstance().create().stepOne(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        try {
                            // 把备注内容上传
                            if (!"".equals(AppCaseData.new_remark) && AppCaseData.new_remark != null) { // 只有备注内容不为空才添加备注
                                addCaseMsg(jsonObject.getJSONObject("datas").getJSONObject("case").getString("caseid"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //保存事故ID
                        if (jsonObject.getJSONObject("datas").getJSONObject("case").getString("caseid") != null) {
                            PreferenceUtil.commitString("caseid", jsonObject.getJSONObject("datas").getJSONObject("case").getString("caseid"));
                        }
                        if (!isDemo) {// 如果非模拟报案
                            // 调用发送短信接口
                            sendSMS(jsonObject.getJSONObject("datas").getJSONObject("case").getString("caseid"), 0);
                        }
                        //从当前的界面跳转到报案2界面
                        Intent intent = new Intent(ImmediatelyReport.this, OnlineSurvey2.class);
                        intent.putExtra("ImmediatelyReport", "reportPhoto");
                        startActivity(intent);
                        finish();
                    } else {
                        if (!isDemo) {
                            if (VLCApplication.configsDictionary.get("APP-C-070") != null) {
                                Toast.makeText(ImmediatelyReport.this, VLCApplication.configsDictionary.get("APP-C-070"), Toast.LENGTH_SHORT).show();
                            } else {
                                ToastUtils.showNomalShortToast(ImmediatelyReport.this, getString(R.string.report_start_failure));
                            }
                        } else {
                            if (VLCApplication.configsDictionary.get("APP-C-146") != null) {
                                Toast.makeText(ImmediatelyReport.this, VLCApplication.configsDictionary.get("APP-C-146"), Toast.LENGTH_SHORT).show();
                            } else {
                                ToastUtils.showNomalShortToast(ImmediatelyReport.this, getString(R.string.report_start_demo_failure));
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (kProgressHUD != null) {
                    kProgressHUD.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (kProgressHUD != null) {
                    kProgressHUD.dismiss();
                }
                ToastUtils.showNomalShortToast(ImmediatelyReport.this, "服务器异常，请稍后再试");
            }
        });
    }

    /**
     * 新增报案备注信息
     */
    private void addCaseMsg(String caseid){

        Casemsg casemsg = new Casemsg();
        casemsg.setCaseid(caseid);
        casemsg.setUsercode(PreferenceUtil.getString("personcode", ""));
        casemsg.setContent(AppCaseData.new_remark);
        casemsg.setUserkind("2");

        Call<ResponseBody> call = RetrofitManager.getInstance().create().addCaseMsg(NetUtils.getHeaders(), casemsg);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    /**
     * 报案成功之后调用发送短信接口
     *
     * @param caseid
     */
    private void sendSMS(String caseid, int type) {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().sendSMS(NetUtils.getHeaders(),caseid,type);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    /**
     * 判断报案时 字段是否为空
     *
     * @return false 有字段为空  true没字段为空
     */
    public boolean isFieldNull() {
        //报案人联系方式
        if (!Utils.isMatchered(informantPhone.getText())) {
            ToastUtils.showNomalShortToast(ImmediatelyReport.this, getString(R.string.register_mobile_err));
            informantPhone.requestFocus();
            return false;
            //事故类型
        } else {
            return true;
        }

    }

    /**
     * 得到选中的日期
     *
     * @param timePickerView
     * @param millseconds
     */
    @Override
    public void onDateSet(TimePickerDialog timePickerView, long millseconds) {

        //设置事故时间
        SimpleDateFormat formatterdate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date curDate = new Date(millseconds);
        String format = formatterdate.format(curDate);
        accidentTime.setText(format);

    }

    /**
     * 获取车主配置信息
     */
    private void getProsonConfigInfo() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().findMyCar(NetUtils.getHeaders(), PreferenceUtil.getString("personcode", ""));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    String msg = jsonObject.getString("msg");
                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        JSONObject datas = jsonObject.getJSONObject("datas");
                        JSONArray msgs = datas.getJSONArray("msgs");

                        PreferenceUtil.commitString("inskind", msgs.getJSONObject(0).getJSONObject("unit").getString("inskind"));
                        // 如果是保险公司则不可点击
                        if (2 == msgs.getJSONObject(0).getJSONObject("unit").getInt("unitkind")) {
                            tv_insurance_company.setClickable(false);
                        }
                        //单位名称
                        tv_unit_name.setText(msgs.getJSONObject(0).getJSONObject("unit").getString("unitname"));
                        // 事故专员姓名
                        tv_server_name.setText(msgs.getJSONObject(0).getJSONObject("serviceperson").getString("username"));
                        // 车牌号
                        AppCaseData.plateNumber = msgs.getJSONObject(0).getJSONObject("car").getString("carnumber");
                        if (AppCaseData.plateNumber == null || "".equals(AppCaseData.plateNumber)) {
                            if (isDemo) {
                                AppCaseData.plateNumber = "沪A888888";
                            }
                        }
                        // 厂牌车型描述
                        AppCaseData.label_models = msgs.getJSONObject(0).getJSONObject("car").getString("cartype");
                        if (AppCaseData.label_models == null || "".equals(AppCaseData.label_models)) {
                            if (isDemo) {
                                AppCaseData.label_models = "劳斯莱斯银魅";
                            }
                        }
                        // 车主手机号
                        AppCaseData.mobile = msgs.getJSONObject(0).getJSONObject("car").getString("carermobile");
                        if (AppCaseData.mobile == null || "".equals(AppCaseData.mobile) || "null".equals(AppCaseData.mobile)) {
                            AppCaseData.mobile = PreferenceUtil.getString("user_mobile", "");
                            informantPhone.setText(PreferenceUtil.getString("user_mobile", ""));
                        } else {
                            informantPhone.setText(Utils.parseStr(AppCaseData.mobile));
                        }
                        // 车主姓名
                        AppCaseData.username = msgs.getJSONObject(0).getJSONObject("car").getString("carername");
                        if (isDemo) {
                            if (AppCaseData.username == null || "".equals(AppCaseData.username)) {
                                informantName.setText("旅橙小伙伴");
                            } else {
                                informantName.setText(Utils.parseStr(AppCaseData.username));
                            }
                        } else {
                            informantName.setText(Utils.parseStr(AppCaseData.username));
                        }
                        if (msgs.getJSONObject(0).getJSONObject("ins") != null) {
                            // 投保公司
                            tv_insurance_company.setText(msgs.getJSONObject(0).getJSONObject("ins").getString("companyname"));
                            // 投保公司
                            AppCaseData.inscode = msgs.getJSONObject(0).getJSONObject("ins").getString("companycode");
                        }
                    } else {
                        ToastUtils.showNomalShortToast(ImmediatelyReport.this, msg);
                    }
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (kProgressHUD != null) {
                    kProgressHUD.dismiss();
                }
                ToastUtils.showNomalShortToast(ImmediatelyReport.this, "数据加载异常，请稍后再试");
            }
        });
    }

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label) {
        kProgressHUD = KProgressHUD.create(ImmediatelyReport.this);
        kProgressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        kProgressHUD.setLabel(label);
        kProgressHUD.setCancellable(true);
        kProgressHUD.setAnimationSpeed(2);
        kProgressHUD.setDimAmount(0.5f);
        kProgressHUD.show();
    }
}