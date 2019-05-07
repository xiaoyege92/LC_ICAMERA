package com.example.administrator.lc_dvr.module.lc_help;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Config;
import com.bumptech.glide.Glide;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.bean.CaseLoss;
import com.example.administrator.lc_dvr.bean.Comment;
import com.example.administrator.lc_dvr.common.customview.RatingBar;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.MyCallBack;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.TimeUtils;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.AppCaseData;
import com.example.administrator.lc_dvr.module.lc_dvr.ReportHelp;
import com.example.administrator.lc_dvr.module.lc_report.CommentActivity;
import com.example.administrator.lc_dvr.module.lc_report.OnlineSurvey2;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/11/12
 *   desc   :
 *  version :
 * </pre>
 */
public class FastCompensate extends BaseActivity implements View.OnClickListener, MyCallBack {

    private RadioButton rb_back; // 左上角返回键
    private LinearLayout ll_report; // 打开报案页
    private RelativeLayout ll_payment; // 赔付页
    private RelativeLayout rl_repair; // 维修页
    private RelativeLayout ll_comment; // 评论页面=
    private LinearLayout ll_insnumber; // 报案号
    private TextView tv_unit_name; // 单位名称
    private TextView tv_server_name; // 事故专员姓名
    private ImageView iv_server_phone_call; // 事故专员电话
    private ImageView iv_report_icon; // 事故icon
    private TextView tv_report_number; // 报案号
    private TextView tv_report_time; // 报案时间
    private TextView tv_accident_time; // 事故时间
    private TextView tv_accident_place;  // 事故地点
    private TextView tv_check_status; // 案子状态
    private TextView tv_payment_tip; // 理赔状态
    private TextView tv_assessment_date; // 定损日期时间
    private TextView tv_assessment_money; // 定损金额
    private TextView tv_close_date; // 结案日期
    private TextView tv_close_money; // 结案金额
    private TextView tv_repair_money_tip; // 维修金额说明
    private TextView tv_repair_money; // 维修金额

    private LinearLayout ll_success; // 定损成功
    private LinearLayout ll_failure; // 定损失败
    private LinearLayout ll_dropped; // 销案

    private TextView tv_assessment_date_failure; // 失败定损日期时间
    private TextView tv_assessment_money_failure; // 定损失败原因类型
    private TextView tv_close_date_failure; // 定损失败原因

    private TextView tv_assessment_date_dropped; // 销案时间
    private TextView tv_assessment_money_dropped; // 销案原因类型
    private TextView tv_close_date_dropped; // 销案原因
    private TextView tv_plan_hand_car_date; // 预计交车日期
    private TextView tv_finish_repair_date; // 维修完毕日期
    private TextView tv_real_hand_car_date; // 实际交车日期
    private TextView tv_comment_status; // 评价提示
    private ImageView iv_report_status; // 报案状态
    private ImageView iv_payment_status; // 赔付状态
    private ImageView iv_repair_status; // 维修状态
    private ImageView iv_comment_status; // 评价状态
    private RadioButton rb_comment_detail; // 评价状态

    private RatingBar quality_rating_bar;
    private RatingBar attitude_rating_bar;
    private Comment mComment;  // 评价

    private int unitKind;

    @Override
    protected void onResume() {
        super.onResume();
        getCasedetail(); // 获取案子的内容
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int setViewId() {
        return R.layout.fast_compensate_activity;
    }

    @Override
    protected void findView() {
        rb_back = (RadioButton) findViewById(R.id.rb_back);
        ll_report = (LinearLayout) findViewById(R.id.ll_report);
        ll_payment = (RelativeLayout) findViewById(R.id.ll_payment);
        rl_repair = (RelativeLayout) findViewById(R.id.rl_repair);
        ll_comment = (RelativeLayout) findViewById(R.id.ll_comment);
        ll_insnumber = (LinearLayout) findViewById(R.id.ll_insnumber);
        iv_server_phone_call = (ImageView) findViewById(R.id.iv_server_phone_call);
        iv_report_icon = (ImageView) findViewById(R.id.iv_report_icon);

        tv_unit_name = (TextView) findViewById(R.id.tv_unit_name);
        tv_server_name = (TextView) findViewById(R.id.tv_server_name);
        tv_report_number = (TextView) findViewById(R.id.tv_report_number);
        tv_report_time = (TextView) findViewById(R.id.tv_report_time);
        tv_accident_time = (TextView) findViewById(R.id.tv_accident_time);
        tv_accident_place = (TextView) findViewById(R.id.tv_accident_place);
        tv_check_status = (TextView) findViewById(R.id.tv_check_status);
        tv_repair_money_tip = (TextView) findViewById(R.id.tv_repair_money_tip);
        tv_repair_money = (TextView) findViewById(R.id.tv_repair_money);

        tv_payment_tip = (TextView) findViewById(R.id.tv_payment_tip);
        tv_assessment_date = (TextView) findViewById(R.id.tv_assessment_date);
        tv_assessment_money = (TextView) findViewById(R.id.tv_assessment_money);
        tv_close_date = (TextView) findViewById(R.id.tv_close_date);
        tv_close_money = (TextView) findViewById(R.id.tv_close_money);

        ll_success = (LinearLayout) findViewById(R.id.ll_success); // 定损成功
        ll_failure = (LinearLayout) findViewById(R.id.ll_failure); // 定损失败
        ll_dropped = (LinearLayout) findViewById(R.id.ll_dropped); // 销案

        tv_assessment_date_failure = (TextView) findViewById(R.id.tv_assessment_date_failure); // 失败定损日期时间
        tv_assessment_money_failure = (TextView) findViewById(R.id.tv_assessment_money_failure); // 定损失败原因类型
        tv_close_date_failure = (TextView) findViewById(R.id.tv_close_date_failure); // 定损失败原因

        tv_assessment_date_dropped = (TextView) findViewById(R.id.tv_assessment_date_dropped); // 销案时间
        tv_assessment_money_dropped = (TextView) findViewById(R.id.tv_assessment_money_dropped); // 销案原因类型
        tv_close_date_dropped = (TextView) findViewById(R.id.tv_close_date_dropped); // 销案原因
        tv_plan_hand_car_date = (TextView) findViewById(R.id.tv_plan_hand_car_date);
        tv_real_hand_car_date = (TextView) findViewById(R.id.tv_real_hand_car_date);
        tv_finish_repair_date = (TextView) findViewById(R.id.tv_finish_repair_date);

        tv_comment_status = (TextView) findViewById(R.id.tv_comment_status);
        iv_report_status = (ImageView) findViewById(R.id.iv_report_status);
        iv_payment_status = (ImageView) findViewById(R.id.iv_payment_status);
        iv_repair_status = (ImageView) findViewById(R.id.iv_repair_status);
        iv_comment_status = (ImageView) findViewById(R.id.iv_comment_status);
        rb_comment_detail = (RadioButton) findViewById(R.id.rb_comment_detail);

        quality_rating_bar = (RatingBar) findViewById(R.id.quality_rating_bar);
        attitude_rating_bar = (RatingBar) findViewById(R.id.attitude_rating_bar);
    }

    @Override
    protected void init() {
        Utils.setCallBack(this);

    }

    @Override
    protected void initEvents() {
        rb_back.setOnClickListener(this);
        ll_report.setOnClickListener(this);
        ll_payment.setOnClickListener(this);
        rl_repair.setOnClickListener(this);
        ll_comment.setOnClickListener(this);
        iv_server_phone_call.setOnClickListener(this);
        // 评价星不可点击
        quality_rating_bar.setClickable(false);
        attitude_rating_bar.setClickable(false);
    }

    @Override
    protected void loadData() {
        unitKind = PreferenceUtil.getInt("unitkind",0);
        if(1 == unitKind) {
            rl_repair.setVisibility(View.VISIBLE);
        }else {
            rl_repair.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_back:
                finish();
                break;
            case R.id.iv_server_phone_call:
                Intent intent = new Intent(FastCompensate.this, ReportHelp.class);
                intent.putExtra("caseid", PreferenceUtil.getString("caseid", null));
                startActivity(intent);
                break;
            case R.id.ll_report:
                if (TimeUtils.isFastClick()) {
                    return;
                }
                Intent reportIntent = new Intent(FastCompensate.this, OnlineSurvey2.class);
                startActivity(reportIntent);
                break;
            case R.id.ll_payment:
                if (TimeUtils.isFastClick()) {
                    return;
                }
                Intent paymentIntent = new Intent(FastCompensate.this, PaymentDetail.class);
                startActivity(paymentIntent);
                break;
            case R.id.rl_repair:
                if (TimeUtils.isFastClick()) {
                    return;
                }
                Intent repairIntent = new Intent(FastCompensate.this, RepairDetail.class);
                startActivity(repairIntent);
                break;
            case R.id.ll_comment:
                if (TimeUtils.isFastClick()) {
                    return;
                }
                Intent commentIntent = new Intent(FastCompensate.this, CommentActivity.class);
                commentIntent.putExtra("caseid", PreferenceUtil.getString("caseid", ""));
                commentIntent.putExtra("comment", mComment);
                commentIntent.putExtra("unitkind", unitKind);
                startActivity(commentIntent);
                break;
            default:
                break;
        }
    }

    /**
     * 获得事故ID的具体内容
     */
    private void getCasedetail() {
        Map<String, String> map = new HashMap<>();

        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getCaseDetail(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        unitKind = jsonObject.getJSONObject("datas").getJSONObject("case").getInt("unitkind");
                        tv_unit_name.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("unitname")));
                        String strServiceName = jsonObject.getJSONObject("datas").getJSONObject("case").getString("servicename");
                        tv_server_name.setText(Utils.parseStr(strServiceName)); // 事故专员姓名
                        String insnumber = jsonObject.getJSONObject("datas").getJSONObject("case").getString("insnumber");
                        if (insnumber != null || "".equals(insnumber)) {
                            ll_insnumber.setVisibility(View.GONE);
                        } else {
                            ll_insnumber.setVisibility(View.VISIBLE);
                            tv_report_number.setText(Utils.parseStr(insnumber)); // 报案号
                        }
                        tv_report_time.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("casedate"))); // 报案时间
                        tv_accident_time.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("accidentdate"))); // 事故时间
                        tv_accident_place.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("accidentaddress")));  // 事故地点

                        tv_plan_hand_car_date.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("prerepairdate"))); // 预计交车日期
                        tv_finish_repair_date.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("finishrepairdate"))); // 维修完毕日期
                        tv_real_hand_car_date.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("finishrepairdate"))); // 实际交车日期
                        tv_repair_money_tip.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("repairmsg"))); // 维修金额说明
                        tv_repair_money.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("repairmoney"))); // 维修金额

                        // 定损和报案 状态
                        String lossStatus = jsonObject.getJSONObject("datas").getJSONObject("case").getString("losestatus");
                        String reportStatus = jsonObject.getJSONObject("datas").getJSONObject("case").getString("reportstatus");

                        // 已销案
                        if ("9".equals(lossStatus) || "9".equals(reportStatus)) {
                            ll_dropped.setVisibility(View.VISIBLE);
                            ll_failure.setVisibility(View.GONE);
                            ll_success.setVisibility(View.GONE);
                            tv_assessment_date_dropped.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("canceltime"))); // 销案时间
                            tv_close_date_dropped.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("cancelreasondetial"))); // 销案原因
                            String[] reasonTypes;
                            if (VLCApplication.configsDictionary.get("app-c-227") != null) {
                                reasonTypes = VLCApplication.configsDictionary.get("app-c-227").split("，");
                            } else {
                                reasonTypes = "1-案件信息填写错了，2-已送至其他店维修，0-其他".split("，");
                            }
                            for (int i = 0; i < reasonTypes.length; i++) {
                                String number = reasonTypes[i].split("-")[0];
                                if (number.equals(jsonObject.getJSONObject("datas").getJSONObject("case").getString("cancelreason"))) {
                                    tv_assessment_money_dropped.setText(reasonTypes[i].split("-")[1]);
                                }
                            }
                        } else if ("5".equals(lossStatus) || "5".equals(reportStatus)) {// 以失败
                            ll_dropped.setVisibility(View.GONE);
                            ll_failure.setVisibility(View.VISIBLE);
                            ll_success.setVisibility(View.GONE);

                            tv_assessment_date_failure.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("lossdate"))); // 失败定损日期时间
                            tv_close_date_failure.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("failreasondetial"))); // 定损失败原因

                            String[] reasonTypes;
                            if (VLCApplication.configsDictionary.get("app-c-229") != null) {
                                reasonTypes = VLCApplication.configsDictionary.get("app-c-229").split("，");
                            } else {
                                reasonTypes = "1-未上传定损照片，2-定损照片不合规，3-超出定损限额，4-车辆需拆解，0-其他".split("，");
                            }
                            for (int i = 0; i < reasonTypes.length; i++) {
                                String number = reasonTypes[i].split("-")[0];
                                if (number.equals(jsonObject.getJSONObject("datas").getJSONObject("case").getString("failreason"))) {
                                    tv_assessment_money_failure.setText(reasonTypes[i].split("-")[1]);
                                }
                            }
                        } else {
                            // 定损日期
                            if (null != jsonObject.getJSONObject("datas").getJSONObject("case").getString("lossdate")) {
                                tv_assessment_date.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("lossdate")));
                            }
                            // 定损金额
                            if (null != jsonObject.getJSONObject("datas").getJSONObject("case").getString("losssum")) {
                                tv_assessment_money.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("losssum")));
                            }
                            // 结案日期
                            if (null != jsonObject.getJSONObject("datas").getJSONObject("case").getString("finishdate")) {
                                tv_close_date.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("finishdate")));
                            }
                            // 结案金额
                            if (null != jsonObject.getJSONObject("datas").getJSONObject("case").getString("finishsum")) {
                                tv_close_money.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("finishsum")));
                            }
                        }
                        // 维修区段  |定损状态|非空时显示
                        if ("0".equals(lossStatus)) {
                            rl_repair.setVisibility(View.GONE);
                        } else {
                            rl_repair.setVisibility(View.VISIBLE);
                        }
                        if ("1".equals(lossStatus) || "1".equals(reportStatus)) { // 当定损或报案  为已报案  显示报案小提示内容
                            tv_check_status.setVisibility(View.VISIBLE);
                        } else {
                            tv_check_status.setVisibility(View.GONE);
                        }
                        // 定损状态列表
                        List<CaseLoss> listCaseLoss = new ArrayList<>();
                        // 是否已拍照
                        boolean isPic = false;
                        JSONArray jsonArray = jsonObject.getJSONObject("datas").getJSONArray("listCaseLoss");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Gson gson = new Gson();
                            CaseLoss caseLoss = gson.fromJson(jsonArray.getJSONObject(i).toString(), CaseLoss.class);
                            listCaseLoss.add(caseLoss);
                            // 如果定损状态记录中有已拍照的
                            if ("2".equals(caseLoss.getLossstatus())) {
                                isPic = true;
                            }
                        }
                        // 如果已拍照，则显示已拍照，否则显示已报案
                        if (isPic) {
                            iv_report_status.setImageResource(R.mipmap.report2);
                            iv_report_status.setVisibility(View.VISIBLE);
                        } else {
                            iv_report_status.setImageResource(R.mipmap.report1);
                            iv_report_status.setVisibility(View.VISIBLE);
                        }
                        AppCaseData.paymentStatus = jsonObject.getJSONObject("datas").getJSONObject("case").getString("paylossstatus");
                        // 理赔状态
                        String paylossStatus = jsonObject.getJSONObject("datas").getJSONObject("case").getString("paylossstatus");
                        if ("0".equals(paylossStatus)) {         // 理赔小提示
                            tv_payment_tip.setText("等待您提交赔付资料");
                            tv_payment_tip.setVisibility(View.VISIBLE);
                        } else if ("1".equals(paylossStatus)) {
                            tv_payment_tip.setText("您已提交赔付资料");
                            tv_payment_tip.setVisibility(View.VISIBLE);
                        } else if ("2".equals(paylossStatus)) {
                            tv_payment_tip.setText("已结案");
                            tv_payment_tip.setVisibility(View.VISIBLE);
                        }
                        if ("5".equals(lossStatus) || "5".equals(reportStatus)) {  // 右下角显示定损状态
                            iv_payment_status.setImageResource(R.mipmap.report5);
                            iv_payment_status.setVisibility(View.VISIBLE);
                        } else if ("6".equals(lossStatus) || "6".equals(reportStatus)) {
                            iv_payment_status.setImageResource(R.mipmap.report6);
                            iv_payment_status.setVisibility(View.VISIBLE);
                        } else if ("9".equals(lossStatus) || "9".equals(reportStatus)) {
                            iv_payment_status.setImageResource(R.mipmap.report9);
                            iv_payment_status.setVisibility(View.VISIBLE);
                        } else {
                            iv_payment_status.setVisibility(View.GONE);
                        }
                        // 维修状态
                        String repairStatus = jsonObject.getJSONObject("datas").getJSONObject("case").getString("repairstatus");
                        if ("6".equals(repairStatus)) {      // 显示对应的维修状态
                            iv_repair_status.setImageResource(R.mipmap.repair6);
                            iv_repair_status.setVisibility(View.VISIBLE);
                        } else if ("7".equals(repairStatus)) {
                            iv_repair_status.setImageResource(R.mipmap.repair7);
                            iv_report_status.setVisibility(View.VISIBLE);
                        } else {
                            iv_repair_status.setVisibility(View.GONE);
                        }
                        // 评价状态
                        String commentStatus = jsonObject.getJSONObject("datas").getJSONObject("case").getString("commentstatus");
                        if ("1".equals(commentStatus)) {
                            iv_comment_status.setImageResource(R.mipmap.comment1);
                            iv_comment_status.setVisibility(View.VISIBLE);
                        } else if ("2".equals(commentStatus)) {
                            iv_comment_status.setImageResource(R.mipmap.comment3);
                            iv_comment_status.setVisibility(View.VISIBLE);
                        } else if ("3".equals(commentStatus)) {
                            iv_comment_status.setImageResource(R.mipmap.comment2);
                            iv_comment_status.setVisibility(View.VISIBLE);
                        } else {
                            iv_comment_status.setVisibility(View.GONE);
                            rb_comment_detail.setVisibility(View.GONE);
                        }
                        // 1、|定损状态|非空2、|评价状态|=空 符合以上2条件，才显示
                        if (!"0".equals(lossStatus) && "0".equals(commentStatus)) {
                            tv_comment_status.setText("维修完毕才可以评价哦");
                            ll_comment.setClickable(false);
                            tv_comment_status.setVisibility(View.VISIBLE);
                            // 1、|定损状态|非空2、|评价状态|=待评价  符合以上2条件，才显示并可操作
                        } else if (!"0".equals(lossStatus) && "1".equals(commentStatus)) {
                            tv_comment_status.setText("确认收车并评价");
                            tv_comment_status.setVisibility(View.VISIBLE);
                        }

                        // 1、|报案状态|非空2、|评价状态|=待评价 符合以上2条件，才显示并可操作
                        if (!"0".equals(reportStatus) && "1".equals(commentStatus)) {
                            tv_comment_status.setText("立即评价");
                            tv_comment_status.setVisibility(View.VISIBLE);
                        }
                        String insIcon = PreferenceUtil.getString(jsonObject.getJSONObject("datas").getJSONObject("case").getString("inscode"), "");
                        // 报案对应图片
                        if (!"".equals(insIcon)) {
                            Glide.with(FastCompensate.this).load(Config.QINIU_BASE_URL + insIcon).into(iv_report_icon);
                        }
                        Gson gson = new Gson();
                        Comment comment = null;
                        if (0 < jsonObject.getJSONObject("datas").getJSONArray("listComment").length()) {
                            comment = gson.fromJson(jsonObject.getJSONObject("datas").getJSONArray("listComment").get(0).toString(), Comment.class);
                        } else {
                            comment = new Comment();
                        }
                        quality_rating_bar.setSelectedNumber(comment.getStar1());
                        attitude_rating_bar.setSelectedNumber(comment.getStar2());

                        mComment = comment;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ToastUtils.showNomalShortToast(FastCompensate.this, "获取数据失败");
            }
        });
    }

    @Override
    public void doSomeThing() {
        finish();
    }

}
