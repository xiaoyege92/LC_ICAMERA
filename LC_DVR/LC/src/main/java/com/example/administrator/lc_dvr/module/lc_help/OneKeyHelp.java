package com.example.administrator.lc_dvr.module.lc_help;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.bumptech.glide.Glide;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.bean.UnitMessage;
import com.example.administrator.lc_dvr.common.adapter.CommonRecyclerAdapter;
import com.example.administrator.lc_dvr.common.adapter.RecyclerViewHolder;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.TimeUtils;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.lc_dvr.Advertisement;
import com.example.administrator.lc_dvr.module.lc_dvr.ReportHelp;
import com.example.administrator.lc_dvr.module.lc_report.ImmediatelyReport;
import com.flyco.dialog.entity.DialogMenuItem;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.NormalListDialog;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/11/07
 *   desc   :
 *  version :
 * </pre>
 */
public class OneKeyHelp extends BaseFragment implements View.OnClickListener {

    private LinearLayout ll_unit_detail; // 单位详情
    private TextView tv_unit_name; // 单位名称
    private TextView tv_unit_server_time;  // 单位服务时间
    private TextView tv_unit_address; // 单位地址
    private ImageView iv_message_empty; // 暂无消息
    private ImageView iv_unit_icon; // 单位头像
    private ImageView iv_unit_navigation;  //单位地址导航
    private ImageView iv_simulation_report; // 模拟报案
    private ImageView iv_one_key_help; // 拨打电话
    private ImageView iv_formal_report; // 正式报案
    private RecyclerView message_recycler; // 消息列表
    private TextView tv_message_number; // 消息数
    private TextView tv_lc_news; // 单位求助TIP

    private String unitName; // 单位名称
    private String unitIconUrl; // 单位头像地址
    private String address; // 单位地址
    private String serviceTime; // 单位服务时间
    private String helpURL; // 求助tip链接
    private String helpTip; // 求助tip

    private int unRead = 0; // 未读条数
    private int msgNum; // 消息条数

    private static ArrayList<UnitMessage> msgs;
    private CommonRecyclerAdapter recyclerAdapter;

    @Override
    public void onResume() {
        super.onResume();
        if(msgs != null) {
            msgNum = msgs.size();
            tv_message_number.setText("消息通知(" + msgNum + ")");
        }
        getUnitMessage();
    }

    @Override
    protected int setViewId() {
        return R.layout.one_key_help_activity;
    }

    @Override
    protected void findView(View view) {
        ll_unit_detail = (LinearLayout) view.findViewById(R.id.ll_unit_detail);
        tv_unit_name = (TextView) view.findViewById(R.id.tv_unit_name);
        tv_unit_server_time = (TextView) view.findViewById(R.id.tv_unit_server_time);
        tv_unit_address = (TextView) view.findViewById(R.id.tv_unit_address);
        iv_message_empty = (ImageView) view.findViewById(R.id.iv_message_empty);
        iv_unit_icon = (ImageView) view.findViewById(R.id.iv_unit_icon);
        iv_unit_navigation = (ImageView) view.findViewById(R.id.iv_unit_navigation);
        iv_simulation_report = (ImageView) view.findViewById(R.id.iv_simulation_report);
        iv_one_key_help = (ImageView) view.findViewById(R.id.iv_one_key_help);
        iv_formal_report = (ImageView) view.findViewById(R.id.iv_formal_report);
        message_recycler = (RecyclerView) view.findViewById(R.id.message_recycler);
        tv_message_number = (TextView) view.findViewById(R.id.tv_message_number);
        tv_lc_news = (TextView) view.findViewById(R.id.tv_lc_news);
    }

    @Override
    protected void init() {
        if(null == msgs) {
            msgs = new ArrayList<>();
        }
        recyclerAdapter = new CommonRecyclerAdapter(getActivity(), R.layout.message_item, msgs) {
            @Override
            public void convert(RecyclerViewHolder holder, Object t, final int position) {
                ImageView iv_message_read = holder.getView(R.id.iv_message_read);
                TextView tv_message_title = holder.getView(R.id.tv_message_title);
                TextView tv_message_date = holder.getView(R.id.tv_message_date);

                if (msgs.get(position).getStatus() == 0) {
                    iv_message_read.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.blue_small_bell));
                } else {
                    iv_message_read.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.gray_small_bell));
                }

                tv_message_title.setText(msgs.get(position).getTitle());
                tv_message_date.setText(msgs.get(position).getMsgtime());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // recyclerView的点击事件
                        Intent intent = new Intent(getActivity(), UnitMessageDetail.class);
                        intent.putExtra("MessageDetail", msgs.get(position));
                        startActivity(intent);
                    }
                });
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        message_recycler.setLayoutManager(layoutManager);
        message_recycler.setAdapter(recyclerAdapter);
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initEvents() {
        ll_unit_detail.setOnClickListener(this);
        iv_message_empty.setOnClickListener(this);
        iv_unit_icon.setOnClickListener(this);
        iv_unit_navigation.setOnClickListener(this);
        iv_simulation_report.setOnClickListener(this);
        iv_one_key_help.setOnClickListener(this);
        iv_formal_report.setOnClickListener(this);
        tv_lc_news.setOnClickListener(this);
    }

    @Override
    protected void loadData() {

        // 如果刚进入APP时没有获取到数据，则重新获取
        if(VLCApplication.unitName == null || "".equals(VLCApplication.unitName)) {
            getUnitInfo(PreferenceUtil.getString("user_mobile", ""));
        }else{

            unitName = VLCApplication.unitName;
            unitIconUrl = VLCApplication.unitIconUrl;
            address = VLCApplication.unitAddress;
            serviceTime = VLCApplication.unitServiceTime;
            helpTip = VLCApplication.unitHelpTip;
            helpURL = VLCApplication.unitHelpURL;

            tv_unit_name.setText(Utils.parseStr(unitName));
            tv_unit_server_time.setText(Utils.parseStr(serviceTime));
            tv_unit_address.setText(Utils.parseStr(address));
            // 如果单位地址为空，隐藏导航图标
            if("".equals(address) || "null".equals(address)){
                iv_unit_navigation.setVisibility(View.GONE);
            }
            tv_lc_news.setText(Utils.parseStr(helpTip));
            if (unitIconUrl != null && !"".equals(unitIconUrl)) {
                Glide.with(getActivity()).load(unitIconUrl).into(iv_unit_icon);// 加载单位头像
            }else{
                iv_unit_icon.setImageResource(R.mipmap.unit_icon);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.ll_unit_detail:// 单位详情
                if (TimeUtils.isFastClick()) {
                    return;
                }
                intent = new Intent(getActivity(), UnitDetail.class);

                break;
            case R.id.iv_unit_navigation:// 单位导航
                if (!NetUtils.isNetworkConnected(getActivity())) {//是否联网
                    ToastUtils.showNomalShortToast(getActivity(), getString(R.string.network_off));
                    return;
                }
                // 先判断手机是否安装百度，高德，腾讯地图。然后显示对应的列表
                final ArrayList<DialogMenuItem> items = new ArrayList<>();
                int count = 0;
                // 腾讯地图
                if (Utils.isInstallApp(getActivity(), "com.tencent.map")) {
                    DialogMenuItem dialogMenuItem = new DialogMenuItem("腾讯地图", count);
                    items.add(dialogMenuItem);
                    count++;
                }
                // 百度地图b
                if (Utils.isInstallApp(getActivity(), "com.baidu.baidumap")) {
                    DialogMenuItem dialogMenuItem = new DialogMenuItem("百度地图", count);
                    items.add(dialogMenuItem);
                    count++;
                }
                // 高德地图
                if (Utils.isInstallApp(getActivity(), "com.autonavi.minimap")) {
                    DialogMenuItem dialogMenuItem = new DialogMenuItem("高德地图", count);
                    items.add(dialogMenuItem);
                    count++;
                }
                if (items.size() == 0) {
                    Toast.makeText(getActivity(), "请先安装地图应用", Toast.LENGTH_SHORT).show();
                    return;
                }
                final NormalListDialog normalListDialog = new NormalListDialog(getActivity(), items);
                normalListDialog.isTitleShow(false);
                normalListDialog.create();
                normalListDialog.show();
                normalListDialog.setOnOperItemClickL(new OnOperItemClickL() {
                    @Override
                    public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if ("高德地图".equals(items.get(position).mOperName)) {
                            startNative_Gaode(getActivity(), tv_unit_address.getText().toString());
                        } else if ("百度地图".equals(items.get(position).mOperName)) {
                            openBaiduMap(getActivity(), tv_unit_address.getText().toString());
                        } else if ("腾讯地图".equals(items.get(position).mOperName)) {
                            goToTencentMap(getActivity(), tv_unit_address.getText().toString());
                        }

                        normalListDialog.dismiss();
                    }
                });

                break;
            case R.id.iv_simulation_report:// 模拟报案
                //记录是否是模拟报案
                PreferenceUtil.commitString("isdemo", "1");
                intent = new Intent(getContext(), ImmediatelyReport.class);

                break;
            case R.id.iv_one_key_help: // 拨打电话
                intent = new Intent(getActivity(), ReportHelp.class);
                intent.putExtra("isSeekHelp", true);
                intent.putExtra("phoneText", PreferenceUtil.getString("user_mobile", ""));

                break;
            case R.id.iv_formal_report:// 正式报案
                //记录是否是模拟报案
                PreferenceUtil.commitString("isdemo", "0");
                intent = new Intent(getContext(), ImmediatelyReport.class);
                break;
            case R.id.tv_lc_news: // 单位TIP 外链跳转
                Intent newsIntent = new Intent(getContext(), Advertisement.class);
                //传送当前广告图的标题和链接
                newsIntent.putExtra("adsTitle", helpTip);
                newsIntent.putExtra("adsLink", helpURL);
                startActivity(newsIntent);

                break;
        }
        if (intent != null) {
            startActivity(intent);
        }

    }

    /**
     * 获取单位信息
     */
    private void getUnitInfo(String user_mobile) {
        Map<String, String> map = new HashMap<>();
        // 设置用户电话
        map.put("mobile", user_mobile);

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getPersonInfoByMobile(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        // 如果单位被删除，显示公司的电话
                        unitName = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitname");
                        unitIconUrl = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("iconurl");
                        address = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("address");
                        serviceTime = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("servicetime");
                        helpTip = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("helptip");
                        helpURL = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("helpurl");
                        // 重新获取的数据放入内存
                        VLCApplication.unitName = unitName;
                        VLCApplication.unitIconUrl = unitIconUrl;
                        VLCApplication.unitAddress = address;
                        VLCApplication.unitServiceTime = serviceTime;
                        VLCApplication.unitHelpTip = helpTip;
                        VLCApplication.unitHelpURL = helpURL;

                        tv_unit_name.setText(Utils.parseStr(unitName));
                        tv_unit_server_time.setText(Utils.parseStr(serviceTime));
                        tv_unit_address.setText(Utils.parseStr(address));
                        // 如果单位地址为空，隐藏导航图标
                        if("".equals(address) || "null".equals(address)){
                            iv_unit_navigation.setVisibility(View.GONE);
                        }
                        tv_lc_news.setText(Utils.parseStr(helpTip));
                        if (unitIconUrl != null && !"".equals(unitIconUrl)) {
                            Glide.with(getActivity()).load(unitIconUrl).into(iv_unit_icon);// 加载单位头像
                        }else{
                            iv_unit_icon.setImageResource(R.mipmap.unit_icon);
                        }
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

    private void getUnitMessage() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getUnitMessage(NetUtils.getHeaders(), PreferenceUtil.getString("personcode", ""));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {
                        msgs.clear();// 先清空消息
                        JSONArray jsonArray = jsonObject.getJSONObject("datas").getJSONArray("msgs");
                        int status;
                        msgNum = jsonArray.length();
                        for (int i = 0; i < msgNum; i++) {
                            Gson gson = new Gson();
                            UnitMessage unitMessage = (UnitMessage) gson.fromJson(jsonArray.get(i).toString(), UnitMessage.class);
                            msgs.add(unitMessage);
                            status = jsonArray.getJSONObject(i).getInt("status");
                            if (status == 0) {
                                unRead++;
                            }
                        }
                        recyclerAdapter.notifyDataSetChanged();
                        if (msgNum > 0) {
                            iv_message_empty.setVisibility(View.GONE);
                            message_recycler.setVisibility(View.VISIBLE);
                            tv_message_number.setText("消息通知(" + msgNum + ")");

                        } else {
                            iv_message_empty.setVisibility(View.VISIBLE);
                            message_recycler.setVisibility(View.GONE);
                        }
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
     * 调起腾讯地图
     */
    private void goToTencentMap(Context context, String loc) {
        if (loc == null) {
            return;
        }
        try {
            StringBuffer stringBuffer = new StringBuffer("qqmap://map/routeplan?type=drive")
                    .append("&tocoord=").append("").append(",").append("").append("&to=" + loc);
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(stringBuffer.toString()));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "地址解析错误", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 调起百度地图
     *
     * @param context
     * @param loc
     */
    private void openBaiduMap(Context context, String loc) {
        if (loc == null) {
            return;
        }
        try {

            Intent intent = new Intent();
            // 驾车导航
            intent.setData(Uri.parse("baidumap://map/navi?query=" + loc));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "地址解析错误", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 调起高德地图
     *
     * @param context
     * @param loc
     */
    public static void startNative_Gaode(Context context, String loc) {
        if (loc == null) {
            return;
        }
        try {
            //地理编码
            Intent inten1 = new Intent("android.intent.action.VIEW"
                    , android.net.Uri.parse("androidamap://viewGeo?sourceApplication=某某公司&addr=" + loc));
            inten1.setPackage("com.autonavi.minimap");// pkg=com.autonavi.minimap
            inten1.addCategory("android.intent.category.DEFAULT");
            context.startActivity(inten1);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "地址解析错误", Toast.LENGTH_SHORT).show();
        }
    }

}
