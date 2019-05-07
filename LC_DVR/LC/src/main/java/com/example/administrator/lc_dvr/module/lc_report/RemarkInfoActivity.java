package com.example.administrator.lc_dvr.module.lc_report;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Config;
import com.bumptech.glide.Glide;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.common.adapter.CommonRecyclerAdapter;
import com.example.administrator.lc_dvr.common.adapter.RecyclerViewHolder;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.AppCaseData;

public class RemarkInfoActivity extends Activity implements View.OnClickListener {

    private TextView tv_cancel;
    private TextView tv_ok;
    private RecyclerView rv_remark_info; // 备注列表
    private EditText et_new_remark; // 新的备注内容
    private RelativeLayout rl_remark;
    private TextView tv_remark_tip; // 暂无备注信息提示

    private CommonRecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setViewId());
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        findView();
        init();
        initEvents();
        loadData();

    }

    protected int setViewId() {
        //表示设置当前的Activity无Title并且全屏,调用这个方法有个限制,即必须在setContentView(R.layout.main)之前调用,否则会抛出异常
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        return R.layout.activity_remark_info;
    }

    protected void findView() {
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
        et_new_remark = (EditText) findViewById(R.id.et_new_remark);
        rv_remark_info = (RecyclerView) findViewById(R.id.rv_remark_info);
        rl_remark = (RelativeLayout) findViewById(R.id.rl_remark);
        tv_remark_tip = (TextView) findViewById(R.id.tv_remark_tip);
    }

    protected void init() {

        recyclerAdapter = new CommonRecyclerAdapter(RemarkInfoActivity.this,R.layout.remark_info_item,AppCaseData.remarkList) {
            @Override
            public void convert(RecyclerViewHolder holder, Object t, int position) {
                String logoid = AppCaseData.remarkList.get(position).getLogoid();
                if (null != logoid && !"".equals(logoid)
                        && !"null".equals(logoid)) {
                    Glide.with(RemarkInfoActivity.this).load(Config.QINIU_BASE_URL+logoid).into((ImageView) holder.getView(R.id.riv_icon));
                }
                // 名字
                holder.setText(R.id.tv_name,Utils.parseStr(AppCaseData.remarkList.get(position).getUsername()));
                // 时间
                holder.setText(R.id.tv_time,Utils.parseStr(AppCaseData.remarkList.get(position).getCtime()));
                // 内容
                holder.setText(R.id.tv_remark, Utils.parseStr(AppCaseData.remarkList.get(position).getContent()));
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(RemarkInfoActivity.this, LinearLayoutManager.VERTICAL, false);
        rv_remark_info.setLayoutManager(layoutManager);

        rv_remark_info.setAdapter(recyclerAdapter);
        if(!AppCaseData.caseIsEdit) { // 如果不可编辑则隐藏 编辑框
            rl_remark.setVisibility(View.GONE);
        }else {
            rl_remark.setVisibility(View.VISIBLE);
        }

        // 如果案件列表为空，则显示提示，否则隐藏提示
        if(null == AppCaseData.remarkList || 0 == AppCaseData.remarkList.size()) {
            tv_remark_tip.setVisibility(View.VISIBLE);
        }else {
            tv_remark_tip.setVisibility(View.GONE);
        }
    }

    protected void initEvents() {
        tv_cancel.setOnClickListener(this);
        tv_ok.setOnClickListener(this);
    }

    protected void loadData() {
        et_new_remark.setText(AppCaseData.new_remark);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.tv_ok:
                AppCaseData.new_remark = et_new_remark.getText().toString();
                finish();
                break;
        }
    }

}
