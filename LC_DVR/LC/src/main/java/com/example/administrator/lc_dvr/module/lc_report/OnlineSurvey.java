package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.bean.Case;
import com.example.administrator.lc_dvr.bean.Casemsg;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.ListDataSave;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.AppCaseData;
import com.example.administrator.lc_dvr.module.lc_dvr.ReportHelp;
import com.example.administrator.lc_dvr.module.login_registration.InsuranceCompanyList;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.google.gson.Gson;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vov.vitamio.utils.Log;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by yangboru on 2018/1/10.
 * <p>
 * 报案页面
 */
public class OnlineSurvey extends BaseFragment implements OnDateSetListener {
    private final int HANDLER_MESSAGE_UPLOAD_SUCCESS = 1;
    private final int HANDLER_MESSAGE_REWOKE_SUCCESS = 2;
    private final int HANDLER_MESSAGE_UPLOAD_FAIL = 3;
    private final int HANDLER_MESSAGE_UPLOAD_NET_FAIL = 4;

    private final int MAX_UPLOAD_REPORT = 3;// 一键上传/一键报案的数

    private Button startReportBtn;
    private int unitkind;
    private ListDataSave dataSave;
    private LocalBroadcastManager localBroadcastManager;
    private RelativeLayout timeSelecte;
    private TimePickerDialog mDialogAll;
    private RelativeLayout reportNumberView;
    private View reportNumberLine;

    private TextView tv_case_orderid; // 订单编号
    private TextView tv_insurance_company; // 投保公司
    private TextView reportNumber; // 报案号
    private TextView tv_unit_name; // 单位名称
    private TextView tv_server_name; // 事故专员
    private ImageView iv_take_phone; // 打电话
    private EditText informantName;  // 报案人姓名
    private EditText informantPhone;  // 报案人电话
    private TextView reportTime; // 报案时间
    private TextView accidentTime; // 事故时间
    private RelativeLayout rl_report_info; // 报案信息
    private RelativeLayout rl_accident_info; // 事故信息
    private RelativeLayout rl_remark; // 备注信息
    private TextView tv_remark_lately; // 最新的备注信息
    private TextView tv_remark_lately_date; // 最新的备注信息日期


    private RelativeLayout startReportBtn_layout; // 最底部一键上传按钮

    private NormalDialog dialog;
    private String[] mStringBts;

    private List<String> reportPhotoUrlArr1;
    private List<String> reportPhotoUrlArr2;
    private List<String> reportPhotoUrlArr4;
    private List<String> reportPhotoUrlArr5;
    private List<String> wholeCarUrlArr;
    private List<String> realVideoUrlArr;
    private KProgressHUD kProgressHUD;

    private String caseid;

    private String baseUrl;
    private String qiniuToken;
    private int onCarCount;
    private int moreCarCount;

    private ArrayList<String> arrReport1;
    private ArrayList<String> arrReport2;
    private ArrayList<String> arrReport4;
    private ArrayList<String> arrReport5;
    private String strWholeCar;
    private String strVideo;
    private int currentProgress;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_MESSAGE_UPLOAD_SUCCESS:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    break;
                case HANDLER_MESSAGE_REWOKE_SUCCESS:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    break;
                case HANDLER_MESSAGE_UPLOAD_FAIL:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    if (VLCApplication.configsDictionary.get("app-c-171") != null) {
                        Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-c-171"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "未上传成功，请稍后再试", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case HANDLER_MESSAGE_UPLOAD_NET_FAIL:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    if (VLCApplication.configsDictionary.get("app-z-010") != null) {
                        Toast.makeText(getActivity(), VLCApplication.configsDictionary.get("app-z-010"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "连接失败，请检查您的网络连接", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
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
    public void onPause() {
        super.onPause();
        //报案时间
        PreferenceUtil.commitString("text_casedate", reportTime.getText().toString());
        //事故日期时间
        PreferenceUtil.commitString("text_accidentdate", accidentTime.getText().toString());

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // 当页面隐藏时将页面内容保存起来，便于拍照页面一键上传
        AppCaseData.reportTime = reportTime.getText().toString();
        AppCaseData.accidentTime = accidentTime.getText().toString();
        AppCaseData.informantName = informantName.getText().toString();
        AppCaseData.informantPhone = informantPhone.getText().toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int setViewId() {
        return R.layout.onlinesurvey_layout;
    }

    @Override
    protected void findView(View view) {
        reportTime = (TextView) view.findViewById(R.id.reportTime2);
        accidentTime = (TextView) view.findViewById(R.id.accidentTime2);
        startReportBtn = (Button) view.findViewById(R.id.startReportBtn2);
        timeSelecte = (RelativeLayout) view.findViewById(R.id.timeSelecte2);
        reportNumberView = (RelativeLayout) view.findViewById(R.id.reportNumberView);
        reportNumberLine = (View) view.findViewById(R.id.reportNumberLine);
        reportNumber = (TextView) view.findViewById(R.id.reportNumber);
        tv_remark_lately = (TextView) view.findViewById(R.id.tv_remark_lately);
        tv_remark_lately_date = (TextView) view.findViewById(R.id.tv_remark_lately_date);

        startReportBtn_layout = (RelativeLayout) view.findViewById(R.id.startReportBtn_layout);

        informantName = (EditText) view.findViewById(R.id.informantName);
        informantPhone = (EditText) view.findViewById(R.id.informantPhone);
        tv_case_orderid = (TextView) view.findViewById(R.id.tv_case_orderid);
        tv_insurance_company = (TextView) view.findViewById(R.id.tv_insurance_company);
        tv_unit_name = (TextView) view.findViewById(R.id.tv_unit_name);
        tv_server_name = (TextView) view.findViewById(R.id.tv_server_name);
        iv_take_phone = (ImageView) view.findViewById(R.id.iv_take_phone);
        rl_report_info = (RelativeLayout) view.findViewById(R.id.rl_report_info);
        rl_accident_info = (RelativeLayout) view.findViewById(R.id.rl_accident_info);
        rl_remark = (RelativeLayout) view.findViewById(R.id.rl_remark);
    }

    @Override
    protected void init() {
        // 初始化数据
        AppCaseData.inscode = "";
        AppCaseData.new_remark = "";
        AppCaseData.inscode = "";
        AppCaseData.remarkList.clear();

        arrReport1 = new ArrayList<>();
        arrReport2 = new ArrayList<>();
        arrReport4 = new ArrayList<>();
        arrReport5 = new ArrayList<>();

        caseid = PreferenceUtil.getString("caseid", "");
        dialog = new NormalDialog(getActivity());
        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);

        //初始化时间选择器
        initTimePickerDialog();

        //得到本地广播管理器的实例
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());

        //用来保存list到本地
        dataSave = new ListDataSave(getActivity(), "baiyu");

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

        //初始化列表
        reportPhotoUrlArr1 = new ArrayList<>();
        reportPhotoUrlArr2 = new ArrayList<>();
        reportPhotoUrlArr4 = new ArrayList<>();
        reportPhotoUrlArr5 = new ArrayList<>();
        wholeCarUrlArr = new ArrayList<>();
        //录屏视频的真实名字
        realVideoUrlArr = new ArrayList();

    }

    /**
     * 各点击事件
     */
    @Override
    protected void initEvents() {
        //点击事故时间时的响应
        timeSelecte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出时间选择器
                mDialogAll.show(getActivity().getSupportFragmentManager(), "all");
            }
        });
        // 选择投保公司
        tv_insurance_company.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断是否联网
                if (!NetUtils.isNetworkConnected(getActivity())) {
                    ToastUtils.showNomalShortToast(getActivity(), getString(R.string.network_off));
                    return;
                }
                //跳转到保险公司列表界面
                Intent intent = new Intent(getActivity(), InsuranceCompanyList.class);
                startActivity(intent);
            }
        });
        // 事故专员电话
        iv_take_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReportHelp.class);
                startActivity(intent);
            }
        });
        // 备注信息
        rl_remark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RemarkInfoActivity.class);
                startActivity(intent);
            }
        });
        // 报案信息
        rl_report_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReportInfoActivity.class);
                startActivity(intent);
            }
        });
        // 事故信息
        rl_accident_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AccidentInfoActivity.class);
                intent.putExtra("isReport", true);
                startActivity(intent);
            }
        });
        // 一键上传
        startReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetUtils.isNetworkConnected(getActivity())) {//是否联网
                    ToastUtils.showNomalShortToast(getActivity(), getString(R.string.network_off));
                    return;
                }
                if (!isFieldNull()) {//判断字段是否全填写了
                    return;
                }
                //获得保存在本地的事故图片url数组
                reportPhotoUrlArr1 = dataSave.getDataList("reportPhotoArr1");
                reportPhotoUrlArr2 = dataSave.getDataList("reportPhotoArr2");
                reportPhotoUrlArr4 = dataSave.getDataList("reportPhotoArr4");
                reportPhotoUrlArr5 = dataSave.getDataList("reportPhotoArr5");
                wholeCarUrlArr = dataSave.getDataList("wholeCarArr");
                realVideoUrlArr = dataSave.getDataList("realVideoNameArr");

                onCarCount = +(reportPhotoUrlArr1.size() - 1) + (reportPhotoUrlArr2.size() - 1)
                        + wholeCarUrlArr.size() + realVideoUrlArr.size();

                moreCarCount = (reportPhotoUrlArr1.size() - 1) + (reportPhotoUrlArr2.size() - 1)
                        + (reportPhotoUrlArr4.size() - 1) + (reportPhotoUrlArr5.size() - 1)
                        + wholeCarUrlArr.size() + realVideoUrlArr.size();

                showTipDialog("请再次确认您所有的报案信息，继续吗？");
                dialog.setOnBtnClickL(
                        new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                                // 先上传字段，再上传图片
                                if (AppCaseData.carCount == 2) {
                                    showProgress("拼命上传中...", MAX_UPLOAD_REPORT + moreCarCount);
                                } else {
                                    showProgress("拼命上传中...", MAX_UPLOAD_REPORT + onCarCount);
                                }
                                uploadField();
                            }
                        },
                        new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                            }
                        });
            }
        });
    }

    @Override
    protected void loadData() {
        // 获取报案信息
        getCaseDetail();
    }

    /**
     * 查询个人信息接口，获取是inskind
     */
    private void getCaseDetail() {
        Map<String, String> map = new HashMap<>();
        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getCaseDetail(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    String msg = jsonObject.getString("msg");
                    if (jsonObject.getBoolean(Config.SUCCESS)) {

                        PreferenceUtil.commitString("inskind", jsonObject.getJSONObject("datas").getJSONObject("case").getString("inskind"));
                        // 订单编号
                        tv_case_orderid.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("caseid")));
                        // 投保公司
                        tv_insurance_company.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("inscompanyname")));
                        if ("".equals(tv_insurance_company.getText().toString())) {
                            tv_insurance_company.setText("请选择投保公司");
                        }
                        // 定损和报案 状态
                        String lossStatus = jsonObject.getJSONObject("datas").getJSONObject("case").getString("losestatus");
                        String reportStatus = jsonObject.getJSONObject("datas").getJSONObject("case").getString("reportstatus");
                        // 理赔状态
                        String paylossStatus = jsonObject.getJSONObject("datas").getJSONObject("case").getString("paylossstatus");
                        // ((|报案状态|非空 || <>已销案) || (|定损状态|非空 || <>已销案)) &&  |理赔状态|<>已结案
                        if (((!"0".equals(lossStatus) && !"9".equals(lossStatus)) || (!"0".equals(reportStatus) && !"9".equals(reportStatus)))
                                && !"2".equals(paylossStatus)) {
                            // 维修单位
                            if (jsonObject.getJSONObject("datas").getJSONObject("case").getInt("unitkind") == 1) {
                                tv_insurance_company.setClickable(true);
                            } else {
                                tv_insurance_company.setClickable(false);
                            }

                            AppCaseData.caseIsEdit = true;
                        } else {
                            tv_insurance_company.setClickable(false);
                            AppCaseData.caseIsEdit = false;
                            // 隐藏 一键上传
                            startReportBtn_layout.setVisibility(View.GONE);
                            accidentTime.setEnabled(false);
                            informantName.setEnabled(false);
                            informantPhone.setEnabled(false);

                            accidentTime.setTextColor(ContextCompat.getColor(getActivity(), R.color.bottom_text));
                            informantName.setTextColor(ContextCompat.getColor(getActivity(), R.color.bottom_text));
                            informantPhone.setTextColor(ContextCompat.getColor(getActivity(), R.color.bottom_text));
                        }
                        // 投保公司code
                        AppCaseData.inscode = jsonObject.getJSONObject("datas").getJSONObject("case").getString("inscode");

                        // 报案号
                        String insnumber = jsonObject.getJSONObject("datas").getJSONObject("case").getString("insnumber");
                        if (null == insnumber || "".equals(insnumber)) {
                            reportNumberView.setVisibility(View.GONE);
                            reportNumberLine.setVisibility(View.GONE);
                        } else {
                            reportNumberView.setVisibility(View.VISIBLE);
                            reportNumberLine.setVisibility(View.VISIBLE);
                            reportNumber.setText(insnumber);
                        }
                        // 单位名称
                        tv_unit_name.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("unitname")));
                        // 事故专员
                        tv_server_name.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("servicename")));
                        //报案时间
                        reportTime.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("casedate")));
                        // 事故时间
                        accidentTime.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("accidentdate")));
                        // 报案人姓名
                        informantName.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("username")));
                        // 报案人电话
                        informantPhone.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("mobile")));

                        // 出险地点
                        AppCaseData.geographicalPosition = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("accidentaddress"));
                        // 车牌号
                        AppCaseData.plateNumber = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("carnumber"));
                        // label_models 厂型车牌
                        AppCaseData.label_models = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("cartype"));
                        // 对方车牌
                        AppCaseData.other_plate_number = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("threeCarnumber"));
                        // 对方厂牌车型
                        AppCaseData.other_label_models = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("threeCartype"));
                        // 对方姓名
                        AppCaseData.other_name = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("threeUsername"));
                        // 对方手机号
                        AppCaseData.other_phone_number = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("threeMobile"));

                        // 事故信息
                        //事故类型
                        if ("单车".equals(jsonObject.getJSONObject("datas").getJSONObject("case").getString("accidentkind"))) {
                            AppCaseData.carCount = 1;
                        } else {
                            AppCaseData.carCount = 2;
                        }
                        // 事故责任类型
                        if ("全部责任".equals(jsonObject.getJSONObject("datas").getJSONObject("case").getString("accident"))) {
                            AppCaseData.accidentResponsibility = 1;
                        } else if ("没有责任".equals(jsonObject.getJSONObject("datas").getJSONObject("case").getString("accident"))) {
                            AppCaseData.accidentResponsibility = 2;
                        } else if ("主要责任".equals(jsonObject.getJSONObject("datas").getJSONObject("case").getString("accident"))) {
                            AppCaseData.accidentResponsibility = 3;
                        } else if ("同等责任".equals(jsonObject.getJSONObject("datas").getJSONObject("case").getString("accident"))) {
                            AppCaseData.accidentResponsibility = 4;
                        } else if ("次要责任".equals(jsonObject.getJSONObject("datas").getJSONObject("case").getString("accident"))) {
                            AppCaseData.accidentResponsibility = 5;
                        }
                        // 是否有物损
                        if ("是".equals(jsonObject.getJSONObject("datas").getJSONObject("case").getString("goodinjure"))) {
                            AppCaseData.isPhysicalDamage = 1;
                        } else {
                            AppCaseData.isPhysicalDamage = 2;
                        }
                        // 是否有人伤
                        if ("是".equals(jsonObject.getJSONObject("datas").getJSONObject("case").getString("personinjure"))) {
                            AppCaseData.isWounded = 1;
                        } else {
                            AppCaseData.isWounded = 2;
                        }
                        //是否事故现场
                        if ("是".equals(jsonObject.getJSONObject("datas").getJSONObject("case").getString("accidentscene"))) {
                            AppCaseData.isScene = 1;
                        } else {
                            AppCaseData.isScene = 2;
                        }
                        // 是否能正常行驶
                        if ("是".equals(jsonObject.getJSONObject("datas").getJSONObject("case").getString("carcanmove"))) {
                            AppCaseData.isNormalDriving = 1;
                        } else {
                            AppCaseData.isNormalDriving = 2;
                        }

                        // 备注列表
                        JSONArray jsonArray = jsonObject.getJSONObject("datas").getJSONArray("listCaseMsg");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Gson gson = new Gson();
                            Casemsg casemsg = gson.fromJson(jsonArray.get(i).toString(), Casemsg.class);
                            AppCaseData.remarkList.add(casemsg);
                        }
                        // 显示最新一条备注
                        if (AppCaseData.remarkList.size() > 0) {
                            tv_remark_lately.setText(AppCaseData.remarkList.get(0).getContent());
                            tv_remark_lately_date.setText(AppCaseData.remarkList.get(0).getCtime());
                        }
                    } else {
                        ToastUtils.showNomalShortToast(getActivity(), msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    /**
     * 判断一键上传时 字段是否为空
     *
     * @return false 有字段为空  true没字段为空
     */
    private boolean isFieldNull() {
        if (!Utils.isMatchered(informantPhone.getText())) {
            ToastUtils.showNomalShortToast(getActivity(), getString(R.string.register_mobile_err));
            informantPhone.requestFocus();
            return false;
            //事故类型
        } else {
            return true;
        }
    }

    /**
     * 初始化时间选择器
     */
    private void initTimePickerDialog() {
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
                .setWheelItemTextSize(17)
                .build();
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
     * 上传字段
     */
    private void uploadField() {

        Map<String, String> map = new HashMap<>();
        //post参数
        map.put("casetype", Config.ICAMERA_A);
        // 案子ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));
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
                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);
                        try {
                            // 把备注内容上传
                            if (!"".equals(AppCaseData.new_remark) && AppCaseData.new_remark != null) { // 只有备注内容不为空才添加备注
                                addCaseMsg(jsonObject.getJSONObject("datas").getJSONObject("case").getString("caseid"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (AppCaseData.carCount == 2) {
                            getQiniuTokenForMoreCar();
                        } else {
                            getQiniuTokenForOneCar();
                        }
                    } else {
                        Message message = new Message();
                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                        handler.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        });

    }

    /**
     * 上传步骤2
     */
    private void uploadTap2() {

        Case casedetail = new Case();
        casedetail.setCaseid(PreferenceUtil.getString("caseid", null));
        casedetail.setCasepictures(VLCApplication.casepictures);

        Call<ResponseBody> call = RetrofitManager.getInstance().create().stepTwo(NetUtils.getHeaders(), casedetail);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);
                        currentProgress = 0;

                        AppCaseData.paymentStatus = jsonObject.getJSONObject("datas").getJSONObject("case").getString("paylossstatus");

                        if ("1".equals(PreferenceUtil.getString("isdemo", "0"))) {
                        } else {
                            // 一键上传结束后，调用发送短信接口
                            sendSMS(jsonObject.getJSONObject("datas").getJSONObject("case").getString("caseid"), 2);
                        }
                        kProgressHUD.dismiss();
                        if (realVideoUrlArr.size() == 1) {
                            PreferenceUtil.commitBoolean("isHadVideo", true);
                        } else {
                            PreferenceUtil.commitBoolean("isHadVideo", false);
                        }
                        Intent intent = new Intent("OnlineSurvey2");
                        localBroadcastManager.sendBroadcast(intent);

                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);

                        // 刷新报案号
                        if (!"".equals(jsonObject.getJSONObject("datas").getJSONObject("case").getString("insnumber"))) {
                            reportNumberView.setVisibility(View.VISIBLE);
                            reportNumberLine.setVisibility(View.VISIBLE);
                            reportNumber.setText(jsonObject.getJSONObject("datas").getJSONObject("case").getString("insnumber"));
                        }
                    } else {
                        //关闭进度框
                        kProgressHUD.dismiss();
                        //设置报案按钮能点击
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //关闭进度框
                if (kProgressHUD != null) {
                    kProgressHUD.dismiss();
                }
                //设置报案按钮能点击
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        });
    }

    /**
     * 弹出提示框
     *
     * @param lable
     */
    private void showTipDialog(String lable) {
        dialog.title(getString(R.string.tip));
        dialog.isTitleShow(true)//
                .cornerRadius(5)//
                .content(lable)//
                .contentGravity(Gravity.CENTER)//
                .btnTextSize(15.5f, 15.5f)//
                .widthScale(0.85f)//
                .btnText(mStringBts)
                .btnTextColor(new int[]{ContextCompat.getColor(getActivity(), R.color.primary), ContextCompat.getColor(getActivity(), R.color.alphablack)})
                .show();
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label, int maxProgress) {
        kProgressHUD = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.ANNULAR_DETERMINATE)
                .setLabel(label)
                .setMaxProgress(maxProgress)
                .show();
    }

    /**
     * 单车的一键上传
     */
    private void getQiniuTokenForOneCar() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getQiniuToken(NetUtils.getHeaders(), onCarCount);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (200 == jsonObject.getInt("code")) {
                        baseUrl = jsonObject.getJSONObject("data").getString("baseUrl");
                        qiniuToken = jsonObject.getJSONObject("data").getString("token");
                        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("fileName");

                        arrReport1.clear();
                        arrReport2.clear();
                        if (0 == jsonArray.length()) {
                            strWholeCar = "";
                            strVideo = "";
                        } else {
                            for (int i = 0; i < reportPhotoUrlArr1.size() - 1; i++) {
                                arrReport1.add(jsonArray.get(i).toString());
                            }
                            for (int j = reportPhotoUrlArr1.size() - 1; j < reportPhotoUrlArr2.size() - 1 + reportPhotoUrlArr1.size() - 1; j++) {
                                arrReport2.add(jsonArray.get(j).toString());
                            }

                            if (realVideoUrlArr.size() == 0) {
                                strWholeCar = jsonArray.get(jsonArray.length() - 1).toString();
                                strVideo = "";
                            } else {
                                strWholeCar = jsonArray.get(jsonArray.length() - 2).toString();
                                strVideo = jsonArray.get(jsonArray.length() - 1).toString();
                            }
                        }
                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);

                        upLoadReportArr1ForOneCar(arrReport1);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    /**
     * 上传单车定损图1
     *
     * @param list
     */
    private void upLoadReportArr1ForOneCar(final ArrayList<String> list) {
        VLCApplication.casepictures.clear();
        VLCApplication.one_b.clear();
        if (null == list || list.size() == 0) {
            VLCApplication.casepictures.put("one_b", VLCApplication.one_b);
            uploadReportArr2ForOneCar(arrReport2);
        } else {
            for (int i = 0; i < list.size(); i++) {

                File reportPhotoArr1File = new File(reportPhotoUrlArr1.get(i).substring(7));
                if (reportPhotoArr1File.exists()) {
                    VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key, ResponseInfo info, JSONObject res) {
                                    //res包含hash、key等信息，具体字段取决于上传策略的设置
                                    if (info.isOK()) {
                                        try {
                                            String attachid = res.getString("key");
                                            Map<String, String> one_bMap = new HashMap<>();
                                            one_bMap.put("attachid", attachid);
                                            VLCApplication.one_b.add(one_bMap);

                                            currentProgress += 1;
                                            kProgressHUD.setProgress(currentProgress);

                                            if (VLCApplication.one_b.size() == list.size()) {//more_a上传完毕
                                                VLCApplication.casepictures.put("one_b", VLCApplication.one_b);
                                                uploadReportArr2ForOneCar(arrReport2);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Log.i("qiniu", "Upload Fail");
                                        //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    }
                                    Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                                }
                            }, null);

                } else {
                    Map<String, String> one_bMap = new HashMap<>();
                    one_bMap.put("attachid", reportPhotoUrlArr1.get(i).replace(Config.QINIU_BASE_URL, ""));
                    VLCApplication.one_b.add(one_bMap);

                    currentProgress += 1;
                    kProgressHUD.setProgress(currentProgress);

                    if (VLCApplication.one_b.size() == list.size()) {//more_a上传完毕
                        VLCApplication.casepictures.put("one_b", VLCApplication.one_b);
                        uploadReportArr2ForOneCar(arrReport2);
                    }
                }
            }
        }
    }

    /**
     * 上传单车定损图2
     *
     * @param list
     */
    private void uploadReportArr2ForOneCar(final ArrayList<String> list) {
        VLCApplication.one_c.clear();
        if (null == list || 0 == list.size()) {
            VLCApplication.casepictures.put("one_c", VLCApplication.one_c);
            uploadWholeForOneCar(strWholeCar);
        } else {
            for (int i = 0; i < list.size(); i++) {
                File reportPhotoArr1File = new File(reportPhotoUrlArr2.get(i).substring(7));
                if (reportPhotoArr1File.exists()) {
                    VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key, ResponseInfo info, JSONObject res) {
                                    //res包含hash、key等信息，具体字段取决于上传策略的设置
                                    if (info.isOK()) {
                                        Log.i("qiniu", "Upload Success");
                                        try {
                                            String attachid = res.getString("key");
                                            Map<String, String> one_cMap = new HashMap<>();
                                            one_cMap.put("attachid", attachid);
                                            VLCApplication.one_c.add(one_cMap);

                                            currentProgress += 1;
                                            kProgressHUD.setProgress(currentProgress);

                                            if (VLCApplication.one_c.size() == list.size()) {//more_a上传完毕
                                                VLCApplication.casepictures.put("one_c", VLCApplication.one_c);
                                                uploadWholeForOneCar(strWholeCar);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Log.i("qiniu", "Upload Fail");
                                        //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    }
                                    Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                                }
                            }, null);
                } else {
                    Map<String, String> one_cMap = new HashMap<>();
                    one_cMap.put("attachid", reportPhotoUrlArr2.get(i).replace(Config.QINIU_BASE_URL, ""));
                    VLCApplication.one_c.add(one_cMap);

                    currentProgress += 1;
                    kProgressHUD.setProgress(currentProgress);

                    if (VLCApplication.one_c.size() == list.size()) {//more_a上传完毕
                        VLCApplication.casepictures.put("one_c", VLCApplication.one_c);
                        uploadWholeForOneCar(strWholeCar);
                    }
                }
            }
        }
    }

    /**
     * 上传全车照
     *
     * @param fileName
     */
    private void uploadWholeForOneCar(String fileName) {
        VLCApplication.one_a.clear();
        if (null == fileName || "".equals(fileName)) {
            VLCApplication.casepictures.put("one_a", VLCApplication.one_a);
            uploadVieo(strVideo);
        } else {
            File takeWholeCarPhoto0File = new File(wholeCarUrlArr.get(0).substring(7));
            if (takeWholeCarPhoto0File.exists() && wholeCarUrlArr.get(0).substring(7).contains("report_image")) {
                VLCApplication.casepictures.put("one_a", VLCApplication.one_a);
                uploadVieo(strVideo);
            } else if (takeWholeCarPhoto0File.exists() && !wholeCarUrlArr.get(0).substring(7).contains("report_image")) {
                VLCApplication.uploadManager.put(takeWholeCarPhoto0File, fileName, qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                    try {
                                        String attachid = res.getString("key");

                                        Map<String, String> one_aMap = new HashMap<>();
                                        one_aMap.put("attachid", attachid);

                                        VLCApplication.one_a.add(one_aMap);
                                        VLCApplication.casepictures.put("one_a", VLCApplication.one_a);

                                        currentProgress += 1;
                                        kProgressHUD.setProgress(currentProgress);

                                        uploadVieo(strVideo);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            }
                        }, null);
            } else {
                Map<String, String> one_aMap = new HashMap<>();
                one_aMap.put("attachid", wholeCarUrlArr.get(0).replace(Config.QINIU_BASE_URL, ""));
                VLCApplication.one_a.add(one_aMap);
                VLCApplication.casepictures.put("one_a", VLCApplication.one_a);

                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);

                uploadVieo(strVideo);
            }
        }
    }

    /**
     * 一键上传
     */
    private void getQiniuTokenForMoreCar() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getQiniuToken(NetUtils.getHeaders(), moreCarCount);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (200 == jsonObject.getInt("code")) {
                        baseUrl = jsonObject.getJSONObject("data").getString("baseUrl");
                        qiniuToken = jsonObject.getJSONObject("data").getString("token");
                        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("fileName");

                        arrReport1.clear();
                        arrReport2.clear();
                        arrReport4.clear();
                        arrReport5.clear();
                        if (0 == jsonArray.length()) {
                            strWholeCar = "";
                            strVideo = "";
                        } else {
                            for (int i = 0; i < reportPhotoUrlArr1.size() - 1; i++) {
                                arrReport1.add(jsonArray.get(i).toString());
                            }
                            for (int j = reportPhotoUrlArr1.size() - 1; j < reportPhotoUrlArr2.size() - 1 + reportPhotoUrlArr1.size() - 1; j++) {
                                arrReport2.add(jsonArray.get(j).toString());
                            }
                            for (int k = reportPhotoUrlArr2.size() + reportPhotoUrlArr1.size() - 2; k < reportPhotoUrlArr2.size() + reportPhotoUrlArr1.size() + reportPhotoUrlArr4.size() - 3; k++) {
                                arrReport4.add(jsonArray.get(k).toString());
                            }

                            for (int l = reportPhotoUrlArr2.size() + reportPhotoUrlArr1.size() + reportPhotoUrlArr4.size() - 3;
                                 l < reportPhotoUrlArr2.size() + reportPhotoUrlArr1.size() + reportPhotoUrlArr4.size() + reportPhotoUrlArr5.size() - 4; l++) {
                                arrReport5.add(jsonArray.get(l).toString());
                            }
                            if (realVideoUrlArr.size() == 0) {
                                strWholeCar = jsonArray.get(jsonArray.length() - 1).toString();
                                strVideo = "";
                            } else {
                                strWholeCar = jsonArray.get(jsonArray.length() - 2).toString();
                                strVideo = jsonArray.get(jsonArray.length() - 1).toString();
                            }
                        }
                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);

                        upLoadReportArr1(arrReport1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    /**
     * 上传多车定损图片 Arr1
     */
    private void upLoadReportArr1(final ArrayList<String> list) {
        VLCApplication.casepictures.clear();
        VLCApplication.more_a.clear();
        if (null == list || 0 == list.size()) {
            VLCApplication.casepictures.put("more_a", VLCApplication.more_a);
            uploadReportArr2(arrReport2);
        } else {
            for (int i = 0; i < list.size(); i++) {
                File reportPhotoArr1File = new File(reportPhotoUrlArr1.get(i).substring(7));
                if (reportPhotoArr1File.exists()) {
                    VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key, ResponseInfo info, JSONObject res) {
                                    //res包含hash、key等信息，具体字段取决于上传策略的设置
                                    if (info.isOK()) {
                                        Log.i("qiniu", "Upload Success");
                                        try {
                                            String attachid = res.getString("key");
                                            // 获取图片的url
                                            String url = Config.QINIU_BASE_URL + attachid;
                                            Map<String, String> more_aMap = new HashMap<>();
                                            more_aMap.put("attachid", attachid);
                                            VLCApplication.more_a.add(more_aMap);

                                            currentProgress += 1;
                                            kProgressHUD.setProgress(currentProgress);

                                            if (VLCApplication.more_a.size() == list.size()) {//more_a上传完毕
                                                VLCApplication.casepictures.put("more_a", VLCApplication.more_a);
                                                uploadReportArr2(arrReport2);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Log.i("qiniu", "Upload Fail");
                                        //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    }
                                    Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                                }
                            }, null);

                } else {
                    Map<String, String> more_aMap = new HashMap<>();
                    more_aMap.put("attachid", reportPhotoUrlArr1.get(i).replace(Config.QINIU_BASE_URL, ""));
                    VLCApplication.more_a.add(more_aMap);

                    currentProgress += 1;
                    kProgressHUD.setProgress(currentProgress);

                    if (VLCApplication.more_a.size() == list.size()) {//more_a上传完毕
                        VLCApplication.casepictures.put("more_a", VLCApplication.more_a);
                        uploadReportArr2(arrReport2);
                    }
                }
            }
        }
    }

    /**
     * 上传多车定损图片 Arr2
     */
    private void uploadReportArr2(final ArrayList<String> list) {
        VLCApplication.more_c.clear();
        if (null == list || 0 == list.size()) {
            VLCApplication.casepictures.put("more_c", VLCApplication.more_c);
            uploadReportArr4(arrReport4);
        } else {
            for (int i = 0; i < list.size(); i++) {
                File reportPhotoArr1File = new File(reportPhotoUrlArr2.get(i).substring(7));
                if (reportPhotoArr1File.exists()) {
                    VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key, ResponseInfo info, JSONObject res) {
                                    //res包含hash、key等信息，具体字段取决于上传策略的设置
                                    if (info.isOK()) {
                                        Log.i("qiniu", "Upload Success");
                                        try {
                                            String attachid = res.getString("key");
                                            // 获取图片的url
                                            String url = Config.QINIU_BASE_URL + attachid;
                                            Map<String, String> more_cMap = new HashMap<>();
                                            more_cMap.put("attachid", attachid);
                                            VLCApplication.more_c.add(more_cMap);

                                            currentProgress += 1;
                                            kProgressHUD.setProgress(currentProgress);

                                            if (VLCApplication.more_c.size() == list.size()) {//more_a上传完毕
                                                VLCApplication.casepictures.put("more_c", VLCApplication.more_c);
                                                uploadReportArr4(arrReport4);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Log.i("qiniu", "Upload Fail");
                                        //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    }
                                    Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                                }
                            }, null);

                } else {
                    Map<String, String> more_cMap = new HashMap<>();
                    more_cMap.put("attachid", reportPhotoUrlArr2.get(i).replace(Config.QINIU_BASE_URL, ""));
                    VLCApplication.more_c.add(more_cMap);

                    currentProgress += 1;
                    kProgressHUD.setProgress(currentProgress);

                    if (VLCApplication.more_c.size() == list.size()) {//more_a上传完毕
                        VLCApplication.casepictures.put("more_c", VLCApplication.more_c);
                        uploadReportArr4(arrReport4);
                    }
                }
            }
        }
    }

    /**
     * 上传多车定损图片 Arr4
     */
    private void uploadReportArr4(final ArrayList<String> list) {
        VLCApplication.more_d.clear();
        if (null == list || 0 == list.size()) {
            VLCApplication.casepictures.put("more_d", VLCApplication.more_d);
            uploadReportArr5(arrReport5);
        } else {
            for (int i = 0; i < list.size(); i++) {
                File reportPhotoArr1File = new File(reportPhotoUrlArr4.get(i).substring(7));
                if (reportPhotoArr1File.exists()) {
                    VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key, ResponseInfo info, JSONObject res) {
                                    //res包含hash、key等信息，具体字段取决于上传策略的设置
                                    if (info.isOK()) {
                                        Log.i("qiniu", "Upload Success");
                                        try {
                                            String attachid = res.getString("key");
                                            // 获取图片的url
                                            String url = Config.QINIU_BASE_URL + attachid;
                                            Map<String, String> more_dMap = new HashMap<>();
                                            more_dMap.put("attachid", attachid);
                                            VLCApplication.more_d.add(more_dMap);

                                            currentProgress += 1;
                                            kProgressHUD.setProgress(currentProgress);

                                            if (VLCApplication.more_d.size() == list.size()) {//more_d上传完毕
                                                VLCApplication.casepictures.put("more_d", VLCApplication.more_d);
                                                uploadReportArr5(arrReport5);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Log.i("qiniu", "Upload Fail");
                                        //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    }
                                    Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                                }
                            }, null);

                } else {
                    Map<String, String> more_dMap = new HashMap<>();
                    more_dMap.put("attachid", reportPhotoUrlArr4.get(i).replace(Config.QINIU_BASE_URL, ""));
                    VLCApplication.more_d.add(more_dMap);

                    currentProgress += 1;
                    kProgressHUD.setProgress(currentProgress);

                    if (VLCApplication.more_d.size() == list.size()) {//more_d上传完毕
                        VLCApplication.casepictures.put("more_d", VLCApplication.more_d);
                        uploadReportArr5(arrReport5);
                    }
                }
            }
        }
    }

    /**
     * 上传多车定损图片 Arr5
     */
    private void uploadReportArr5(final ArrayList<String> list) {
        VLCApplication.more_e.clear();
        if (null == list || 0 == list.size()) {
            VLCApplication.casepictures.put("more_e", VLCApplication.more_e);
            uploadWholeCar(strWholeCar);
        } else {
            for (int i = 0; i < list.size(); i++) {
                File reportPhotoArr1File = new File(reportPhotoUrlArr5.get(i).substring(7));
                if (reportPhotoArr1File.exists()) {
                    VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key, ResponseInfo info, JSONObject res) {
                                    //res包含hash、key等信息，具体字段取决于上传策略的设置
                                    if (info.isOK()) {
                                        Log.i("qiniu", "Upload Success");
                                        try {
                                            String attachid = res.getString("key");
                                            Map<String, String> more_eMap = new HashMap<>();
                                            more_eMap.put("attachid", attachid);
                                            VLCApplication.more_e.add(more_eMap);

                                            currentProgress += 1;
                                            kProgressHUD.setProgress(currentProgress);

                                            if (VLCApplication.more_e.size() == list.size()) {//more_e上传完毕
                                                VLCApplication.casepictures.put("more_e", VLCApplication.more_e);
                                                uploadWholeCar(strWholeCar);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Log.i("qiniu", "Upload Fail");
                                        //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    }
                                    Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                                }
                            }, null);
                } else {
                    Map<String, String> more_eMap = new HashMap<>();
                    more_eMap.put("attachid", reportPhotoUrlArr5.get(i).replace(Config.QINIU_BASE_URL, ""));
                    VLCApplication.more_e.add(more_eMap);

                    currentProgress += 1;
                    kProgressHUD.setProgress(currentProgress);

                    if (VLCApplication.more_e.size() == list.size()) {//more_e上传完毕
                        VLCApplication.casepictures.put("more_e", VLCApplication.more_e);
                        uploadWholeCar(strWholeCar);
                    }
                }
            }
        }
    }

    /**
     * 上传全车照
     *
     * @param fileName
     */
    private void uploadWholeCar(String fileName) {
        VLCApplication.more_b.clear();
        if (null == fileName || "".equals(fileName)) {
            VLCApplication.casepictures.put("more_b", VLCApplication.more_b);
            uploadVieo(strVideo);
        } else {
            File takeWholeCarPhoto0File = new File(wholeCarUrlArr.get(0).substring(7));
            if (takeWholeCarPhoto0File.exists() && wholeCarUrlArr.get(0).substring(7).contains("report_image")) {
                VLCApplication.casepictures.put("more_b", VLCApplication.more_b);
                uploadVieo(strVideo);
            } else if (takeWholeCarPhoto0File.exists() && !wholeCarUrlArr.get(0).substring(7).contains("report_image")) {
                VLCApplication.uploadManager.put(takeWholeCarPhoto0File, fileName, qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                    try {
                                        String attachid = res.getString("key");
                                        // 获取图片的url
                                        String url = Config.QINIU_BASE_URL + attachid;

                                        Map<String, String> license_bMap = new HashMap<>();
                                        license_bMap.put("attachid", attachid);

                                        VLCApplication.more_b.add(license_bMap);
                                        VLCApplication.casepictures.put("more_b", VLCApplication.more_b);

                                        currentProgress += 1;
                                        kProgressHUD.setProgress(currentProgress);

                                        uploadVieo(strVideo);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            }
                        }, null);
            } else {
                Map<String, String> more_bMap = new HashMap<>();
                more_bMap.put("attachid", wholeCarUrlArr.get(0).replace(Config.QINIU_BASE_URL, ""));
                VLCApplication.more_b.add(more_bMap);
                VLCApplication.casepictures.put("more_b", VLCApplication.more_b);

                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);

                uploadVieo(strVideo);
            }
        }
    }

    /**
     * 上传小视频
     *
     * @param fileName
     */
    private void uploadVieo(String fileName) {
        VLCApplication.video.clear();
        if (null == realVideoUrlArr || 0 == realVideoUrlArr.size()) {
            VLCApplication.casepictures.put("video", VLCApplication.video);
            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);
            uploadTap2();
        } else {
            File realVideoNameArrFile = new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/" + realVideoUrlArr.get(0));

            if (realVideoNameArrFile.exists()) {
                VLCApplication.uploadManager.put(realVideoNameArrFile, fileName + ".mp4", qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                    try {
                                        String attachid = res.getString("key");
                                        // 获取图片的url
                                        String url = Config.QINIU_BASE_URL + attachid;

                                        Map<String, String> videoMap = new HashMap<>();
                                        videoMap.put("attachid", attachid);

                                        VLCApplication.video.add(videoMap);
                                        VLCApplication.casepictures.put("video", VLCApplication.video);

                                        currentProgress += 1;
                                        kProgressHUD.setProgress(currentProgress);

                                        uploadTap2();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            }
                        }, null);
            } else {
                Map<String, String> videoMap = new HashMap<>();
                videoMap.put("attachid", realVideoUrlArr.get(0).replace(Config.QINIU_BASE_URL, ""));

                VLCApplication.video.add(videoMap);
                VLCApplication.casepictures.put("video", VLCApplication.video);

                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);

                uploadTap2();
            }
        }
    }

    /**
     * 新增报案备注信息
     */
    private void addCaseMsg(String caseid) throws JSONException {

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
     * 调用发送短信接口
     *
     * @param caseid
     */
    private void sendSMS(String caseid, int type) {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().sendSMS(NetUtils.getHeaders(), caseid, type);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

}