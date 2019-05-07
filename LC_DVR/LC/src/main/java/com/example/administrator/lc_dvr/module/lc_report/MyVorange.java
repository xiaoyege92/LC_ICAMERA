package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Config;
import com.bumptech.glide.Glide;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.bean.Comment;
import com.example.administrator.lc_dvr.common.adapter.CommonRecyclerAdapter;
import com.example.administrator.lc_dvr.common.adapter.RecyclerViewHolder;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.TimeUtils;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.AppCaseData;
import com.example.administrator.lc_dvr.module.lc_dvr.ReportHelp;
import com.example.administrator.lc_dvr.module.lc_help.FastCompensate;
import com.makeramen.roundedimageview.RoundedImageView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/11/19
 *   desc   :
 *  version :
 * </pre>
 */

public class MyVorange extends BaseFragment implements View.OnClickListener {

    private ImageView iv_red_circler; // 是否升级的红点
    private RoundedImageView riv_person_icon; // 用户头像
    private TextView tv_person_name; // 用户昵称
    private TextView tv_person_remark; // 备注
    private RecyclerView rv_case_list; // 案件列表
    private RelativeLayout rl_case_empty; // 空案件时显示
    private TextView tv_loading; // 加载中
    private ImageView iv_case_empty; // 空图片

    private CommonRecyclerAdapter adapter;
    private List<String> reportStateArr; // 报案状态
    private List<String> paymentStateArr; // 理赔状态
    private List<String> commentStateArr; // 评价状态
    private List<String> repairStateArr; // 维修状态
    private List<String> lotusStatusArr; // 申请状态

    private List<String> reportTimeArr; // 报案时间
    private List<String> reportLocaleArr; // 报案地址
    private List<String> reportPlateArr; // 报案车牌号
    private List<String> reportHaoArr;  // 报案号
    private List<String> reportServiceArr; // 报案事故专员
    private List<String> reportwxinUrlArr; // 报案微信分享链接
    private List<String> reportUnitNameArr; // 报案单位名称
    private List<String> reportAccidentTimeArr; // 事故时间
    private List<String> reportInsArr;// 投保公司
    private List<String> checkBackTimeArr;// 审核驳回时间
    private List<String> checkSuccessTimeArr;// 审核成功时间
    private List<String> checkFailTimeArr;// 审核失败时间
    private List<String> cancelTimeArr;// 销案时间
    private List<Comment> commentList; // 评论列表

    private RefreshLayout refreshLayout;
    private ImageView iv_setting; // 我的配置

    private String[] mStringBts;
    //开始条数
    private int page = 1;

    //返回记录数
    private int pagesize = 5;
    private List<String> caseidArr;
    private List<Integer> unitkindArr;
    private List<String[]> userList;

    private View touchView;
    private int touchPosition;

    private boolean isUpdate; // 是否需要升级

    @Override
    public void onResume() {
        super.onResume();
        // 如果缓存中昵称为空，接口获取内容，否则显示缓存内容
        if ("".equals(Utils.parseStr(AppCaseData.nick_name))) {
            getPersonInfo();
        } else {
            if (null != AppCaseData.headURL && !"".equals(AppCaseData.headURL)
                    && !"null".equals(AppCaseData.headURL)
                    && !(Config.QINIU_BASE_URL + "null").equals(AppCaseData.headURL)) {
                if (AppCaseData.headURL.contains("http")) {
                    Glide.with(getActivity()).load(AppCaseData.headURL).into(riv_person_icon);
                } else {
                    riv_person_icon.setImageBitmap(BitmapFactory.decodeFile(AppCaseData.headURL));
                }
            }
            tv_person_name.setText(AppCaseData.nick_name);
            tv_person_remark.setText(AppCaseData.user_remark);
        }
        //开始刷新
        refreshLayout.autoRefresh();
        refreshData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int setViewId() {
        return R.layout.my_vorange_fragment;
    }

    @Override
    protected void findView(View view) {
        iv_red_circler = (ImageView) view.findViewById(R.id.iv_red_circler);
        riv_person_icon = (RoundedImageView) view.findViewById(R.id.riv_person_icon);
        tv_person_name = (TextView) view.findViewById(R.id.tv_person_name);
        tv_person_remark = (TextView) view.findViewById(R.id.tv_person_remark);
        rv_case_list = (RecyclerView) view.findViewById(R.id.rv_case_list);
        rl_case_empty = (RelativeLayout) view.findViewById(R.id.rl_case_empty);
        refreshLayout = (RefreshLayout) view.findViewById(R.id.refreshLayout);
        iv_setting = (ImageView) view.findViewById(R.id.iv_setting);
        tv_loading = (TextView) view.findViewById(R.id.tv_loading);
        iv_case_empty = (ImageView) view.findViewById(R.id.iv_case_empty);
        // 禁用下拉刷新
        refreshLayout.setEnableRefresh(false);
    }

    @Override
    protected void init() {

        //先在这个页面保存默认图片，因为保存需要时间，这样进入到报案页面2就马上可以显示出来了
        try {
            saveMyBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.report_image), "report_image");
        } catch (IOException e) {
            e.printStackTrace();
        }

        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);

        //保存用户的设置的list
        userList = new ArrayList<>();
        unitkindArr = new ArrayList<Integer>();//单位类型arr
        caseidArr = new ArrayList<String>();//报案号arr
        reportStateArr = new ArrayList<String>();//报案状态arr
        paymentStateArr = new ArrayList<>();
        commentStateArr = new ArrayList<>();
        repairStateArr = new ArrayList<>();
        lotusStatusArr = new ArrayList<String>();//定损状态arr
        reportTimeArr = new ArrayList<String>();//报案时间arr
        reportLocaleArr = new ArrayList<String>();//报案地点arr
        reportPlateArr = new ArrayList<String>();//报案车牌号arr
        reportHaoArr = new ArrayList<String>();//报案号arr
        reportServiceArr = new ArrayList<String>();//事故专员arr
        reportwxinUrlArr = new ArrayList<String>();// 微信分享URL
        reportUnitNameArr = new ArrayList<String>(); // 报案单位名称
        reportAccidentTimeArr = new ArrayList<String>(); // 事故时间
        reportInsArr = new ArrayList<String>(); // 单位icon
        checkBackTimeArr = new ArrayList<String>();// 审核驳回时间
        checkSuccessTimeArr = new ArrayList<String>();// 审核成功时间
        checkFailTimeArr = new ArrayList<String>();// 审核失败时间
        cancelTimeArr = new ArrayList<String>();// 销案时间
        commentList = new ArrayList<>();

        adapter = new CommonRecyclerAdapter(getActivity(), R.layout.report_item, reportStateArr) {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void convert(RecyclerViewHolder helper, Object item, final int position) {
                // 先判断什么时候隐藏，再判断什么时候显示什么
                // 维修单位并且维修完毕 显示维修状态字段
                if (unitkindArr.get(position) == 1 && "6".equals(repairStateArr.get(position))) {
                    helper.getView(R.id.view2).setVisibility(View.VISIBLE);
                    helper.getView(R.id.tv_repair_state).setVisibility(View.VISIBLE);
                } else {
                    helper.getView(R.id.view2).setVisibility(View.GONE);
                    helper.getView(R.id.tv_repair_state).setVisibility(View.GONE);
                }
                // 如果理赔状态为已结案则 隐藏 定损或报案区段
                if ("2".equals(paymentStateArr.get(position))) {
                    helper.getView(R.id.tv_report_state).setVisibility(View.GONE);
                    helper.getView(R.id.view1).setVisibility(View.GONE);
                }
                // 如果定损或报案状态为 已销案则 隐藏 理赔和维修区段
                if ("9".equals(reportStateArr.get(position)) || "9".equals(lotusStatusArr.get(position))) {
                    helper.getView(R.id.view1).setVisibility(View.GONE);
                    helper.getView(R.id.tv_payment_state).setVisibility(View.GONE);
                    helper.getView(R.id.view2).setVisibility(View.GONE);
                    helper.getView(R.id.tv_repair_state).setVisibility(View.GONE);
                }
                TextView tv_report_state = helper.getView(R.id.tv_report_state); // 定损 或 报案
                TextView tv_payment_state = helper.getView(R.id.tv_payment_state); // 理赔
                TextView tv_repair_state = helper.getView(R.id.tv_repair_state); // 维修
                // 报案 或定损状态分别显示
                if (("1".equals(reportStateArr.get(position)) || "2".equals(reportStateArr.get(position)))
                        || ("1".equals(lotusStatusArr.get(position)) || "2".equals(lotusStatusArr.get(position)))) {
                    tv_report_state.setText("未定损");
                }
                if ("5".equals(reportStateArr.get(position)) || "5".equals(lotusStatusArr.get(position))) {
                    tv_report_state.setText("定损失败");
                }
                if ("6".equals(reportStateArr.get(position)) || "6".equals(lotusStatusArr.get(position))) {
                    tv_report_state.setText("定损成功");
                }
                if ("9".equals(reportStateArr.get(position)) || "9".equals(lotusStatusArr.get(position))) {
                    tv_report_state.setText("已销案");
                }
                // 理赔状态显示
                if ("0".equals(paymentStateArr.get(position))) {
                    tv_payment_state.setText("未提交赔付资料");
                } else if ("1".equals(paymentStateArr.get(position))) {
                    tv_payment_state.setText("已提交赔付资料");
                } else if ("2".equals(paymentStateArr.get(position))) {
                    tv_payment_state.setText("已结案");
                }
                // （（|报案状态|非空 同时<>已销案） 或者 （|定损状态|非空 同时<>已销案）） 同时  |理赔状态|<>已结案 时，才显示并可操作
                if (((!"0".equals(reportStateArr.get(position)) && !"9".equals(reportStateArr.get(position)))
                        || (!"0".equals(lotusStatusArr.get(position)) && !"9".equals(lotusStatusArr.get(position))))
                        && !"2".equals(paymentStateArr.get(position))) {
                    helper.getView(R.id.btn_case_revoke).setVisibility(View.VISIBLE);
                } else {
                    helper.getView(R.id.btn_case_revoke).setVisibility(View.GONE);
                }
                // 1、|报案状态|非空时2、|评价状态|=待评价 或 已评价1时  符合以上2条件，才显示并可操作
                if (!"0".equals(reportStateArr.get(position)) && "1".equals(commentStateArr.get(position))) {
                    helper.getView(R.id.btn_case_evaluate).setVisibility(View.VISIBLE);
                    helper.getView(R.id.btn_case_evaluate_car).setVisibility(View.GONE);
                } else if (!"0".equals(reportStateArr.get(position)) && "2".equals(commentStateArr.get(position))) {
                    helper.setText(R.id.btn_case_evaluate, "追评");
                    helper.getView(R.id.btn_case_evaluate).setVisibility(View.VISIBLE);
                    helper.getView(R.id.btn_case_evaluate_car).setVisibility(View.GONE);
                } else if (!"0".equals(reportStateArr.get(position)) && ("0".equals(commentStateArr.get(position)) || "3".equals(commentStateArr.get(position)))) {
                    helper.getView(R.id.btn_case_evaluate).setVisibility(View.GONE);
                    helper.getView(R.id.btn_case_evaluate_car).setVisibility(View.GONE);
                }
                // 1、|定损状态|非空 时 2、|评价状态|=待评价  符合以上2条件，才显示并可操作
                if (!"0".equals(lotusStatusArr.get(position)) && "1".equals(commentStateArr.get(position))) {
                    helper.getView(R.id.btn_case_evaluate_car).setVisibility(View.VISIBLE);
                    helper.getView(R.id.btn_case_evaluate).setVisibility(View.GONE);
                } else if (!"0".equals(lotusStatusArr.get(position)) && "2".equals(commentStateArr.get(position))) {
                    helper.setText(R.id.btn_case_evaluate, "追评");
                    helper.getView(R.id.btn_case_evaluate).setVisibility(View.VISIBLE);
                    helper.getView(R.id.btn_case_evaluate_car).setVisibility(View.GONE);
                } else if (!"0".equals(lotusStatusArr.get(position)) && ("0".equals(commentStateArr.get(position)) || "3".equals(commentStateArr.get(position)))) {
                    helper.getView(R.id.btn_case_evaluate_car).setVisibility(View.GONE);
                    helper.getView(R.id.btn_case_evaluate).setVisibility(View.GONE);
                }
                String insIcon = PreferenceUtil.getString(reportInsArr.get(position), "");
                // 报案对应图片
                if (!"".equals(insIcon)) {
                    Glide.with(getActivity()).load(Config.QINIU_BASE_URL + insIcon).into((ImageView) helper.getView(R.id.iv_unit_icon));
                }

                // 单位名称
                helper.setText(R.id.tv_unit_name, reportUnitNameArr.get(position));
                // 事故专员
                if (reportServiceArr.get(position).equals("")) {
                    //隐藏item布局中的第五栏
                    helper.getView(R.id.ll_server).setVisibility(View.GONE);
                } else {
                    helper.setText(R.id.tv_server_name, reportServiceArr.get(position));
                    ImageView report5Btn = helper.getView(R.id.iv_service_phone_call);
                    //点击拨打电话号码时的响应
                    report5Btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //保存事故ID
                            PreferenceUtil.commitString("caseid", caseidArr.get(position));
                            Intent intent = new Intent(getContext(), ReportHelp.class);
                            intent.putExtra("caseid", caseidArr.get(position));
                            startActivity(intent);
                        }
                    });
                }
                // 报案号
                if (null == reportHaoArr.get(position) || "".equals(reportHaoArr.get(position))) {
                    //隐藏报案号
                    helper.getView(R.id.ll_case_number).setVisibility(View.GONE);
                } else {
                    helper.setText(R.id.tv_caseid, reportHaoArr.get(position));
                }
                // 报案时间
                helper.setText(R.id.tv_case_report_date, reportTimeArr.get(position));
                // 事故时间
                helper.setText(R.id.tv_case_date, reportAccidentTimeArr.get(position));
                // 事故地点
                helper.setText(R.id.tv_case_address, Utils.parseStr(reportLocaleArr.get(position)));

                helper.getView(R.id.btn_case_revoke).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(getActivity(), RevokeActivity.class);
                        //保存事故ID
                        PreferenceUtil.commitString("caseid", caseidArr.get(touchPosition));
                        intent.putExtra("caseid", caseidArr.get(position));
                        startActivity(intent);

                    }
                });

                helper.getView(R.id.rd_wxin_share).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 微信分享
                        wXinShare(position);
                    }
                });

                // 评价
                helper.getView(R.id.btn_case_evaluate).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), CommentActivity.class);
                        intent.putExtra("caseid", caseidArr.get(position));
                        intent.putExtra("unitkind", unitkindArr.get(position));
                        //保存事故ID
                        PreferenceUtil.commitString("caseid", caseidArr.get(touchPosition));
                        startActivity(intent);
                    }
                });
                // 确认收车并评价
                helper.getView(R.id.btn_case_evaluate_car).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), CommentActivity.class);
                        intent.putExtra("caseid", caseidArr.get(position));
                        intent.putExtra("unitkind", unitkindArr.get(position));
                        //保存事故ID
                        PreferenceUtil.commitString("caseid", caseidArr.get(touchPosition));
                        startActivity(intent);
                    }
                });
                // 进入报案索引页
                helper.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (TimeUtils.isFastClick(2000)) {
                            return;
                        }
                        // 如果没网就提示
                        if (!NetUtils.isNetworkConnected(getActivity())) {
                            ToastUtils.showNomalShortToast(getActivity(), getString(R.string.network_off));
                            return;
                        }
                        touchPosition = position;
                        touchView = view;
                        openOnlineSurvey2();//打开报案快赔页面
                    }
                });
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv_case_list.setLayoutManager(layoutManager);
        //设置适配器
        rv_case_list.setAdapter(adapter);
    }

    @Override
    protected void initEvents() {

        riv_person_icon.setOnClickListener(this);
        iv_setting.setOnClickListener(this);
        //底部刷新
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                //每次底部刷新就加20
                page++;
                pagesize += 5;

                Call<ResponseBody> call = RetrofitManager.getInstance().create().getCaseList(NetUtils.getHeaders(), page, pagesize);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());

                            JSONObject datas = jsonObject.getJSONObject("datas");
                            JSONObject pages = datas.getJSONObject("pages");
                            JSONArray rows = pages.getJSONArray("list");
                            for (int i = 0; i < rows.length(); i++) {
                                JSONObject value = (JSONObject) rows.get(i);

                                //获得用户这个案件的设置
                                String[] userArr = new String[25];
                                //出险地点
                                userArr[0] = value.getString("accidentaddress");
                                //是否事故现场
                                userArr[1] = value.getString("accidentscene");
                                //车牌号
                                userArr[2] = value.getString("carnumber");
                                //报案人联系方式
                                userArr[3] = value.getString("mobile");
                                //报案人
                                userArr[4] = value.getString("username");
                                //事故类型
                                userArr[5] = value.getString("accidentkind");
                                //事故责任
                                userArr[6] = value.getString("accident");
                                //是否有人伤
                                userArr[7] = value.getString("personinjure");
                                //车辆能否正常行驶
                                userArr[8] = value.getString("carcanmove");
                                //事故日期时间
                                userArr[9] = value.getString("accidentdate");
                                //报案时间
                                userArr[10] = value.getString("casedate");
                                // 微信分享链接
                                userArr[11] = value.getString("wxshareurl");
                                // 单位名称
                                userArr[12] = value.getString("unitname");
                                // 事故专员姓名
                                userArr[13] = value.getString("servicename");
                                // 报案号
                                userArr[14] = value.getString("insnumber");

                                // 定损日期
                                userArr[15] = value.getString("lossdate");
                                // 定损金额
                                userArr[16] = value.getString("losssum");
                                // 结案日期
                                userArr[17] = value.getString("finishdate");
                                // 结案金额
                                userArr[18] = value.getString("finishsum");
                                // 预计交车日期
                                userArr[19] = value.getString("prerepairdate");
                                // 实际交车日期
                                userArr[20] = value.getString("finishrepairdate");
                                // 定损状态
                                userArr[21] = value.getString("losestatus");
                                // 理赔状态
                                userArr[22] = value.getString("paylossstatus");
                                //保存这个userArr
                                userList.add(userArr);

                                if (value.toString().contains("unitkind")) {
                                    //赋值给单位类型数组
                                    int unitkind = value.getInt("unitkind");
                                    unitkindArr.add(unitkind);
                                } else {
                                    unitkindArr.add(-1);
                                }

                                if (value.toString().contains("caseid")) {
                                    //赋值给报案号数组
                                    String caseid = value.getString("caseid");
                                    caseidArr.add(caseid);
                                } else {
                                    caseidArr.add("");
                                }

                                if (value.toString().contains("reportstatus")) {
                                    //赋值给报案状态数组
                                    String reportstatus = value.getString("reportstatus");
                                    reportStateArr.add(reportstatus);
                                } else {
                                    reportStateArr.add("0");
                                }
                                if (value.toString().contains("paylossstatus")) {
                                    //赋值给报案状态数组
                                    String reportstatus = value.getString("paylossstatus");
                                    paymentStateArr.add(reportstatus);
                                } else {
                                    paymentStateArr.add("0");
                                }
                                if (value.toString().contains("commentstatus")) {
                                    //赋值给报案状态数组
                                    String reportstatus = value.getString("commentstatus");
                                    commentStateArr.add(reportstatus);
                                } else {
                                    commentStateArr.add("0");
                                }
                                if (value.toString().contains("repairstatus")) {
                                    //赋值给报案状态数组
                                    String reportstatus = value.getString("repairstatus");
                                    repairStateArr.add(reportstatus);
                                } else {
                                    repairStateArr.add("0");
                                }
                                if (value.toString().contains("losestatus")) {
                                    //赋值给定损状态数组
                                    String losestatus = value.getString("losestatus");
                                    lotusStatusArr.add(losestatus);
                                } else {
                                    lotusStatusArr.add("0");
                                }

                                if (value.toString().contains("accidentdate")) {
                                    //赋值给报案时间数组
                                    String accidentdate = value.getString("accidentdate");
                                    reportTimeArr.add(accidentdate);
                                } else {
                                    reportTimeArr.add("");
                                }

                                if (value.toString().contains("accidentaddress")) {
                                    //赋值给报案地点数组
                                    String accidentaddress = value.getString("accidentaddress");
                                    reportLocaleArr.add(accidentaddress);
                                } else {
                                    reportLocaleArr.add("");
                                }

                                if (value.toString().contains("carnumber")) {
                                    //赋值给报案车牌号数组
                                    String carnumber = value.getString("carnumber");
                                    reportPlateArr.add(carnumber);
                                } else {
                                    reportPlateArr.add("");
                                }

                                if (value.toString().contains("insnumber")) {
                                    //赋值给报案号数组
                                    String insnumber = value.getString("insnumber");
                                    reportHaoArr.add(insnumber);
                                } else {
                                    reportHaoArr.add("");
                                }

                                if (value.toString().contains("servicename")) {
                                    //赋值给事故专员数组
                                    String wxshareurl = value.getString("servicename");
                                    reportServiceArr.add(wxshareurl);
                                } else {
                                    reportServiceArr.add("");
                                }

                                if (value.toString().contains("wxshareurl")) {
                                    //赋值给微信分享链接
                                    String servicename = value.getString("wxshareurl");
                                    reportwxinUrlArr.add(servicename);
                                } else {
                                    reportwxinUrlArr.add("");
                                }


                                if (value.toString().contains("unitname")) {
                                    //赋值给报案单位名称
                                    String unitname = value.getString("unitname");
                                    reportUnitNameArr.add(unitname);
                                } else {
                                    reportUnitNameArr.add("");
                                }

                                if (value.toString().contains("accidentdate")) {
                                    //赋值给事故时间
                                    String accidentdate = value.getString("accidentdate");
                                    reportAccidentTimeArr.add(accidentdate);
                                } else {
                                    reportAccidentTimeArr.add("");
                                }

                                if (value.toString().contains("inscode")) {
                                    //赋值给单位icon
                                    String unitIcon = value.getString("inscode");
                                    reportInsArr.add(unitIcon);
                                } else {
                                    reportInsArr.add("");
                                }
                                if (value.toString().contains("checkbacktime")) {
                                    //赋值给审核驳回时间
                                    String unitIcon = value.getString("checkbacktime");
                                    checkBackTimeArr.add(unitIcon);
                                } else {
                                    checkBackTimeArr.add("");
                                }
                                if (value.toString().contains("checksuccesstime")) {
                                    //赋值给审核成功时间
                                    String unitIcon = value.getString("checksuccesstime");
                                    checkSuccessTimeArr.add(unitIcon);
                                } else {
                                    checkSuccessTimeArr.add("");
                                }
                                if (value.toString().contains("checkfailtime")) {
                                    // 赋值给审核失败时间
                                    String unitIcon = value.getString("checkfailtime");
                                    checkFailTimeArr.add(unitIcon);
                                } else {
                                    checkFailTimeArr.add("");
                                }
                                if (value.toString().contains("canceltime")) {
                                    //赋值给单位icon
                                    String unitIcon = value.getString("canceltime");
                                    cancelTimeArr.add(unitIcon);
                                } else {
                                    cancelTimeArr.add("");
                                }
                            }
                            refreshLayout.finishLoadMore();
                            //刷新列表
                            adapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        refreshLayout.finishLoadMore();
                    }
                });
            }
        });
    }

    @Override
    protected void loadData() {
        if (PreferenceUtil.getBoolean("isUpgrade", false)) {
            iv_red_circler.setVisibility(View.VISIBLE);
            isUpdate = true;
        } else {
            iv_red_circler.setVisibility(View.GONE);
            isUpdate = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_setting:
                if (TimeUtils.isFastClick()) {
                    return;
                }
                Intent intent = new Intent(getActivity(), CarConfig.class);
                startActivity(intent);
                break;
            case R.id.riv_person_icon:
                if (TimeUtils.isFastClick()) {
                    return;
                }
                Intent iconIntent = new Intent(getActivity(), Personal.class);
                iconIntent.putExtra("isUpdate", isUpdate);
                startActivity(iconIntent);
                break;
        }
    }

    /**
     * 刷新数据
     */
    private void refreshData() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getCaseList(NetUtils.getHeaders(), 1, 5);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    //恢复默认值
                    page = 1;
                    pagesize = 5;

                    //刷新之前先清空数据
                    //报案状态arr
                    reportStateArr.clear();
                    paymentStateArr.clear();
                    commentStateArr.clear();
                    repairStateArr.clear();
                    //定损状态arr
                    lotusStatusArr.clear();
                    //报案时间arr
                    reportTimeArr.clear();
                    //报案地点arr
                    reportLocaleArr.clear();
                    //报案车牌号arr
                    reportPlateArr.clear();
                    //报案号arr
                    reportHaoArr.clear();
                    //事故专员字典
                    reportServiceArr.clear();
                    // 微信分享链接
                    reportwxinUrlArr.clear();
                    //单位类型数组
                    unitkindArr.clear();
                    reportUnitNameArr.clear(); // 报案单位名称
                    reportAccidentTimeArr.clear(); // 事故时间
                    reportInsArr.clear();// 单位icon
                    checkBackTimeArr.clear();// 审核驳回时间
                    checkSuccessTimeArr.clear();// 审核成功时间
                    checkFailTimeArr.clear();// 审核失败时间
                    cancelTimeArr.clear();// 销案时间
                    commentList.clear();
                    //报案号数组
                    caseidArr.clear();
                    //保存用户设置数组
                    userList.clear();

                    JSONObject datas = jsonObject.getJSONObject("datas");
                    JSONObject pages = datas.getJSONObject("pages");
                    JSONArray rows = pages.getJSONArray("list");

                    //判断数据是否为空
                    if (rows.length() == 0) {
                        //显示空数据时的提示
                        rl_case_empty.setVisibility(View.VISIBLE);
                        iv_case_empty.setVisibility(View.VISIBLE);
                        tv_loading.setVisibility(View.GONE);
                        refreshLayout.finishRefresh();
                    } else {
                        //隐藏空数据时的提示
                        rl_case_empty.setVisibility(View.GONE);

                        for (int i = 0; i < rows.length(); i++) {
                            JSONObject value = (JSONObject) rows.get(i);

                            //获得用户这个案件的设置
                            String[] userArr = new String[25];
                            //出险地点
                            userArr[0] = value.getString("accidentaddress");
                            //是否事故现场
                            userArr[1] = value.getString("accidentscene");
                            //车牌号
                            userArr[2] = value.getString("carnumber");
                            //报案人联系方式
                            userArr[3] = value.getString("mobile");
                            //报案人
                            userArr[4] = value.getString("username");
                            //事故类型
                            userArr[5] = value.getString("accidentkind");
                            //事故责任
                            userArr[6] = value.getString("accident");
                            //是否有人伤
                            userArr[7] = value.getString("personinjure");
                            //车辆能否正常行驶
                            userArr[8] = value.getString("carcanmove");
                            //事故日期时间
                            userArr[9] = value.getString("accidentdate");
                            //报案时间
                            userArr[10] = value.getString("casedate");
                            // 微信分享链接
                            userArr[11] = value.getString("wxshareurl");
                            // 单位名称
                            userArr[12] = value.getString("unitname");
                            // 事故专员姓名
                            userArr[13] = value.getString("servicename");
                            // 报案号
                            userArr[14] = value.getString("insnumber");
                            // 定损日期
                            userArr[15] = value.getString("lossdate");
                            // 定损金额
                            userArr[16] = value.getString("losssum");
                            // 结案日期
                            userArr[17] = value.getString("finishdate");
                            // 结案金额
                            userArr[18] = value.getString("finishsum");
                            // 预计交车日期
                            userArr[19] = value.getString("prerepairdate");
                            // 实际交车日期
                            userArr[20] = value.getString("finishrepairdate");
                            // 定损状态
                            userArr[21] = value.getString("losestatus");
                            // 理赔状态
                            userArr[22] = value.getString("paylossstatus");
                            //保存这个userArr
                            userList.add(userArr);

                            if (value.toString().contains("unitkind")) {
                                //赋值给单位类型数组
                                int unitkind = value.getInt("unitkind");
                                unitkindArr.add(unitkind);
                            } else {
                                unitkindArr.add(-1);
                            }

                            if (value.toString().contains("caseid")) {
                                //赋值给报案号数组
                                String caseid = value.getString("caseid");
                                caseidArr.add(caseid);
                            } else {
                                caseidArr.add("");
                            }

                            if (value.toString().contains("reportstatus")) {
                                //赋值给报案状态数组
                                String reportstatus = value.getString("reportstatus");
                                reportStateArr.add(reportstatus);
                            } else {
                                reportStateArr.add("");
                            }
                            if (value.toString().contains("paylossstatus")) {
                                //赋值给报案状态数组
                                String reportstatus = value.getString("paylossstatus");
                                paymentStateArr.add(reportstatus);
                            } else {
                                paymentStateArr.add("");
                            }
                            if (value.toString().contains("commentstatus")) {
                                //赋值给报案状态数组
                                String reportstatus = value.getString("commentstatus");
                                commentStateArr.add(reportstatus);
                            } else {
                                commentStateArr.add("");
                            }
                            if (value.toString().contains("repairstatus")) {
                                //赋值给报案状态数组
                                String reportstatus = value.getString("repairstatus");
                                repairStateArr.add(reportstatus);
                            } else {
                                repairStateArr.add("0");
                            }
                            if (value.toString().contains("losestatus")) {
                                //赋值给定损状态数组
                                String losestatus = value.getString("losestatus");
                                lotusStatusArr.add(losestatus);
                            } else {
                                lotusStatusArr.add("");
                            }

                            if (value.toString().contains("accidentdate")) {
                                //赋值给报案时间数组
                                String accidentdate = value.getString("accidentdate");
                                reportTimeArr.add(accidentdate);
                            } else {
                                reportTimeArr.add("");
                            }

                            if (value.toString().contains("accidentaddress")) {
                                //赋值给报案地点数组
                                String accidentaddress = value.getString("accidentaddress");
                                reportLocaleArr.add(accidentaddress);
                            } else {
                                reportLocaleArr.add("");
                            }

                            if (value.toString().contains("carnumber")) {
                                //赋值给报案车牌号数组
                                String carnumber = value.getString("carnumber");
                                reportPlateArr.add(carnumber);
                            } else {
                                reportPlateArr.add("");
                            }

                            if (value.toString().contains("insnumber")) {
                                //赋值给报案号数组
                                String insnumber = value.getString("insnumber");
                                reportHaoArr.add(insnumber);
                            } else {
                                reportHaoArr.add("");
                            }

                            if (value.toString().contains("servicename")) {
                                //赋值给事故专员数组
                                String servicename = value.getString("servicename");
                                reportServiceArr.add(servicename);
                            } else {
                                reportServiceArr.add("");
                            }
                            if (value.toString().contains("wxshareurl")) {
                                //赋值给事故专员数组
                                String wxshareurl = value.getString("wxshareurl");
                                reportwxinUrlArr.add(wxshareurl);
                            } else {
                                reportwxinUrlArr.add("");
                            }

                            if (value.toString().contains("unitname")) {
                                //赋值给报案单位名称
                                String unitname = value.getString("unitname");
                                reportUnitNameArr.add(unitname);
                            } else {
                                reportUnitNameArr.add("");
                            }

                            if (value.toString().contains("accidentdate")) {
                                //赋值给事故时间
                                String accidentdate = value.getString("accidentdate");
                                reportAccidentTimeArr.add(accidentdate);
                            } else {
                                reportAccidentTimeArr.add("");
                            }

                            if (value.toString().contains("inscode")) {
                                //赋值给单位icon
                                String unitIcon = value.getString("inscode");
                                reportInsArr.add(unitIcon);
                            } else {
                                reportInsArr.add("");
                            }
                            if (value.toString().contains("checkbacktime")) {
                                //赋值给审核驳回时间
                                String unitIcon = value.getString("checkbacktime");
                                checkBackTimeArr.add(unitIcon);
                            } else {
                                checkBackTimeArr.add("");
                            }
                            if (value.toString().contains("checksuccesstime")) {
                                //赋值给审核成功时间
                                String unitIcon = value.getString("checksuccesstime");
                                checkSuccessTimeArr.add(unitIcon);
                            } else {
                                checkSuccessTimeArr.add("");
                            }
                            if (value.toString().contains("checkfailtime")) {
                                // 赋值给审核失败时间
                                String unitIcon = value.getString("checkfailtime");
                                checkFailTimeArr.add(unitIcon);
                            } else {
                                checkFailTimeArr.add("");
                            }
                            if (value.toString().contains("canceltime")) {
                                //赋值给单位icon
                                String unitIcon = value.getString("canceltime");
                                cancelTimeArr.add(unitIcon);
                            } else {
                                cancelTimeArr.add("");
                            }
                        }
                        refreshLayout.finishRefresh();
                        //刷新列表
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                refreshLayout.finishRefresh();
            }
        });
    }

    /**
     * 点击列表进入报案快赔页面
     */
    private void openOnlineSurvey2() {

        //判断是否是点击列表进来这个页面的
        PreferenceUtil.commitBoolean("isComeFromList", true);

        // 保存审核失败和撤销两个状态
        if ("5".equals(lotusStatusArr.get(touchPosition).toString())) {
            PreferenceUtil.commitString("lotusStatus", "审核失败");
        } else if ("9".equals(lotusStatusArr.get(touchPosition).toString())) {
            PreferenceUtil.commitString("lotusStatus", "销案");
        } else {
            PreferenceUtil.commitString("lotusStatus", "");
        }

        PreferenceUtil.commitString("isdemo", "0");//点击列表进来的都是正式报案
        // 保存定损状态
        PreferenceUtil.commitString("loss_status", lotusStatusArr.get(touchPosition).toString());
        //保存事故ID
        PreferenceUtil.commitString("caseid", caseidArr.get(touchPosition));
        //保存报案号
        PreferenceUtil.commitString("insnumber", reportHaoArr.get(touchPosition));
        //保存单位类型，1 4s 、 2 保险公司
        PreferenceUtil.commitInt("unitkind", unitkindArr.get(touchPosition));
        //跳转到报案2页面
        Intent intent = new Intent(getContext(), FastCompensate.class);
        intent.putExtra("userArr", userList.get(touchPosition));
        startActivity(intent);
    }

    /**
     * 保存bitmap到SD卡
     *
     * @param bmp
     * @param bitName
     * @return
     * @throws IOException
     */
    public void saveMyBitmap(final Bitmap bmp, final String bitName) throws IOException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (bmp != null) {
                    File dirFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/");
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }
                    File f = new File(BitmapUtils.getSDPath() + "/VOC/Cache/" + bitName + ".png");
                    if (f.exists()) {
                        f.delete();
                    }
                    try {
                        f.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    FileOutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(f);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        fOut.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * 微信分享图标
     */
    public void wXinShare(int position) {
        if (!NetUtils.isNetworkConnected(getActivity())) {//是否联网
            ToastUtils.showNomalShortToast(getActivity(), getString(R.string.network_off));
            return;
        }
        weChatSharing(position);
    }

    /**
     * 微信分享的代码
     */
    private void weChatSharing(int position) {
        //初始化一个WXWebpageObject对象，填写url
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = reportwxinUrlArr.get(position);

        //用WXWebpageObject对象初始化一个WXMediaMessage对象，填写标题、描述
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = "旅橙iCamera";
        msg.description = "报案号、报案日期时间、事故日期时间、出现地点、事故照片、小视频等的在线分享。";

        //设置微信分享的预览图
        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.wx_icon);
        msg.thumbData = bmpToByteArray(thumb, true);

        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");//transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;//分享到微信好友
        //调用api接口发送数据到微信
        VLCApplication.wxapi.sendReq(req);

    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void getPersonInfo() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getPersonalInfo(NetUtils.getHeaders());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    AppCaseData.nick_name = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("username"));
                    AppCaseData.user_remark = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("demo"));

                    tv_person_name.setText(AppCaseData.nick_name);
                    tv_person_remark.setText(AppCaseData.user_remark);
                    String headUrl = jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("logoid");
                    if (!"".equals(headUrl) && null != headUrl && !"null".equals(headUrl)) {
                        Glide.with(getActivity()).load(Config.QINIU_BASE_URL + headUrl).into(riv_person_icon);
                        AppCaseData.headURL = Config.QINIU_BASE_URL + headUrl;
                    } else {
                        riv_person_icon.setImageResource(R.mipmap.person_icon);
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

}
