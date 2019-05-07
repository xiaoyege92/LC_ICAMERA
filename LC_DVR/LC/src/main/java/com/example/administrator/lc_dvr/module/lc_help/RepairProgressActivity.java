package com.example.administrator.lc_dvr.module.lc_help;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.bean.CaseRepair;
import com.example.administrator.lc_dvr.bean.CaseRepairFinish;
import com.example.administrator.lc_dvr.bean.RevokeReason;
import com.example.administrator.lc_dvr.common.adapter.CommonRecyclerAdapter;
import com.example.administrator.lc_dvr.common.adapter.RecyclerViewHolder;
import com.example.administrator.lc_dvr.common.utils.Utils;

import org.videolan.libvlc.VLCApplication;

import java.util.ArrayList;
import java.util.List;

public class RepairProgressActivity extends Activity implements View.OnClickListener {

    private ImageView iv_close; // 关闭页面

    private TextView tv_plan_hand_car_info; // 预计交车记录
    private View plan_hand_car_selector;
    private TextView tv_finish_repair_info; // 维修完毕记录
    private View finish_repair_selector;

    private RecyclerView rv_plan_hand_car; // 预计交车记录
    private RecyclerView rv_finish_repair; // 维修完毕记录

    private CommonRecyclerAdapter planHandCarAdapter;
    private CommonRecyclerAdapter finishRepairAdapter;

    private ArrayList<CaseRepair> planHandCarArr;
    private ArrayList<CaseRepairFinish> finishRepairArr;
    private List<RevokeReason> revokeReasonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //表示设置当前的Activity无Title并且全屏,调用这个方法有个限制,即必须在setContentView(R.layout.main)之前调用,否则会抛出异常
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.activity_repair_progress);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        findView();

        init();

        initEvent();

        loadData();
    }

    private void findView() {
        iv_close = (ImageView) findViewById(R.id.iv_close);
        tv_plan_hand_car_info = (TextView) findViewById(R.id.tv_plan_hand_car_info);
        plan_hand_car_selector = findViewById(R.id.plan_hand_car_selector);
        tv_finish_repair_info = (TextView) findViewById(R.id.tv_finish_repair_info);
        finish_repair_selector = findViewById(R.id.finish_repair_selector);
        rv_plan_hand_car = (RecyclerView) findViewById(R.id.rv_plan_hand_car);
        rv_finish_repair = (RecyclerView) findViewById(R.id.rv_finish_repair);
    }

    private void init() {
        planHandCarArr = new ArrayList<>();
        finishRepairArr = new ArrayList<>();
        revokeReasonList = new ArrayList<>();

        String[] reasonTypes;

        if (VLCApplication.configsDictionary.get("app-c-226") != null) {
            reasonTypes = VLCApplication.configsDictionary.get("app-c-226").split("，");
        } else {
            reasonTypes = "1-维修车辆任务繁忙，2-缺少车辆配件需要采购，3-车主增加新的服务需求，0-其他".split("，");
        }

        for (int i = 0; i < reasonTypes.length; i++) {
            RevokeReason revokeReason = new RevokeReason();
            String number = reasonTypes[i].split("-")[0];
            revokeReason.setNumber(number);
            revokeReason.setReason(reasonTypes[i].split("-")[1]);
            revokeReasonList.add(revokeReason);

        }
        planHandCarAdapter = new CommonRecyclerAdapter(RepairProgressActivity.this, R.layout.repair_progress_item, planHandCarArr) {
            @Override
            public void convert(RecyclerViewHolder holder, Object t, final int position) {

                for(int i = 0 ; i < revokeReasonList.size() ; i++) {
                    if(revokeReasonList.get(i).getNumber() != null && revokeReasonList.get(i).getNumber().equals(planHandCarArr.get(position).getReason())) {
                        // 原因类型
                        holder.setText(R.id.tv_title, Utils.parseStr(revokeReasonList.get(i).getReason()));
                    }
                }
                // 时间
                holder.setText(R.id.tv_date, Utils.parseStr(planHandCarArr.get(position).getPredate()));
                // 原因
                holder.setText(R.id.tv_content, Utils.parseStr(planHandCarArr.get(position).getIdea()));
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(RepairProgressActivity.this, LinearLayoutManager.VERTICAL, false);
        rv_plan_hand_car.setLayoutManager(layoutManager);
        rv_plan_hand_car.setAdapter(planHandCarAdapter);

        finishRepairAdapter = new CommonRecyclerAdapter(RepairProgressActivity.this, R.layout.repair_progress_item, finishRepairArr) {
            @Override
            public void convert(RecyclerViewHolder holder, Object t, final int position) {
//                // 原因类型
//                holder.setText(R.id.tv_title, Utils.parseStr(finishRepairArr.get(position).getReason()));

                for(int i = 0 ; i < revokeReasonList.size() ; i++) {
                    if(revokeReasonList.get(i).getNumber() != null && revokeReasonList.get(i).getNumber().equals(finishRepairArr.get(position).getReason())) {
                        // 原因类型
                        holder.setText(R.id.tv_title, Utils.parseStr(revokeReasonList.get(i).getReason()));
                    }
                }
                // 时间
                holder.setText(R.id.tv_date, Utils.parseStr(finishRepairArr.get(position).getFinishdate()));
                // 原因
                holder.setText(R.id.tv_content, Utils.parseStr(finishRepairArr.get(position).getIdea()));
            }
        };
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(RepairProgressActivity.this, LinearLayoutManager.VERTICAL, false);
        rv_finish_repair.setLayoutManager(layoutManager2);
        rv_finish_repair.setAdapter(finishRepairAdapter);
    }

    private void initEvent() {
        iv_close.setOnClickListener(this);
    }

    private void loadData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        List<CaseRepair> planCarArr = bundle.getParcelableArrayList("planHandCarArr");
        List<CaseRepairFinish> finishCarArr = bundle.getParcelableArrayList("finishRepairArr");
        // 如果数据为空，则刷新数据
        if(null != planCarArr ) {
            for(int i = 0 ; i < planCarArr.size(); i++) {
                planHandCarArr.add(planCarArr.get(i));
            }
        }
        if(null != finishCarArr ) {
            for(int i = 0 ; i < finishCarArr.size(); i++) {
                finishRepairArr.add(finishCarArr.get(i));
            }
        }

        planHandCarAdapter.notifyDataSetChanged();
        finishRepairAdapter.notifyDataSetChanged();
    }

    /**
     * 点击选择器时的响应  选择显示哪个RecyclerView
     *
     * @param v
     */
    public void onlineSurveySelector(View v) {

        // 先是更改上面的变化，再更改下面的Fragment显示
        switch (v.getId()) {
            case R.id.rl_basic_info:
                tv_plan_hand_car_info.setTextColor(ContextCompat.getColor(RepairProgressActivity.this, R.color.orange)); // 预计交车记录
                plan_hand_car_selector.setVisibility(View.VISIBLE);

                tv_finish_repair_info.setTextColor(ContextCompat.getColor(RepairProgressActivity.this, R.color.normal_black)); // 维修完毕记录
                finish_repair_selector.setVisibility(View.GONE);

                rv_plan_hand_car.setVisibility(View.VISIBLE);
                rv_finish_repair.setVisibility(View.GONE);
                break;
            case R.id.rl_ins_info:
                tv_plan_hand_car_info.setTextColor(ContextCompat.getColor(RepairProgressActivity.this, R.color.normal_black)); // 预计交车记录
                plan_hand_car_selector.setVisibility(View.GONE);

                tv_finish_repair_info.setTextColor(ContextCompat.getColor(RepairProgressActivity.this, R.color.orange)); // 维修完毕记录
                finish_repair_selector.setVisibility(View.VISIBLE);

                rv_plan_hand_car.setVisibility(View.GONE);
                rv_finish_repair.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                finish();
                break;
        }
    }

}
