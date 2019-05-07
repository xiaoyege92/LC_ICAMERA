package com.example.administrator.lc_dvr.module.lc_report;


import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.bean.Car;
import com.example.administrator.lc_dvr.bean.InsCompany;
import com.example.administrator.lc_dvr.bean.MachineCode;
import com.example.administrator.lc_dvr.bean.Unit;
import com.example.administrator.lc_dvr.common.adapter.CommonRecyclerAdapter;
import com.example.administrator.lc_dvr.common.adapter.RecyclerViewHolder;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.observer.ObserverListener;
import com.example.administrator.lc_dvr.common.utils.observer.ObserverManager;
import com.example.administrator.lc_dvr.module.AppCaseData;
import com.example.administrator.lc_dvr.module.login_registration.Scan;

import java.util.ArrayList;

/**
 *
 */
public class MachineCodeFragment extends BaseFragment implements ObserverListener{

    private TextView tv_machine_code; // 机身串码
    private TextView tv_machine_status; // 状态

    private LinearLayout ll_install; // 安装
    private LinearLayout ll_uninstall; // 拆机

    private RecyclerView recyclerView; // 安装拆机记录
    private CommonRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;

    private int isInstall; // 1是安装 0是拆机


    @Override
    public void onResume() {
        super.onResume();
        //获得扫描的结果
        if (PreferenceUtil.getString("scan_result", null) != null) {
            tv_machine_code.setText(PreferenceUtil.getString("scan_result", ""));
            //设置完之后就清空数据
            PreferenceUtil.commitString("scan_result", null);

            if(isInstall == 1) {
                tv_machine_status.setText("安装");
            }else if(isInstall == 0) {
                tv_machine_status.setText("拆机");
            }
        }
    }

    @Override
    protected int setViewId() {
        return R.layout.fragment_machine_code;
    }

    @Override
    protected void findView(View view) {
        tv_machine_code = (TextView) view.findViewById(R.id.tv_machine_code);
        tv_machine_status = (TextView) view.findViewById(R.id.tv_machine_status);

        ll_install = (LinearLayout) view.findViewById(R.id.ll_install);
        ll_uninstall = (LinearLayout) view.findViewById(R.id.ll_uninstall);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
    }

    @Override
    protected void init() {

        if(null == AppCaseData.unitDetail) {
            AppCaseData.unitDetail = new Unit();
        }
        if(null == AppCaseData.carDetail) {
            AppCaseData.carDetail = new Car();
        }
        if(null == AppCaseData.insCompanyDetail) {
            AppCaseData.insCompanyDetail = new InsCompany();
        }
        if(null == AppCaseData.machineCode) {
            AppCaseData.machineCode = new MachineCode();
        }
        if(null == AppCaseData.machineCodeList) {
            AppCaseData.machineCodeList = new ArrayList<>();
        }
        ObserverManager.getInstance().add(this);

        recyclerAdapter = new CommonRecyclerAdapter(getActivity(),R.layout.machine_item,AppCaseData.machineCodeList) {
            @Override
            public void convert(RecyclerViewHolder helper, Object item, int position) {
                helper.setText(R.id.tv_ctime,AppCaseData.machineCodeList.get(position).getCtime());
                helper.setText(R.id.tv_machine_code,AppCaseData.machineCodeList.get(position).getMachinecode());
                if(0 == AppCaseData.machineCodeList.get(position).getStatus()) {
                    helper.setText(R.id.tv_machine_status,"拆机");
                }else if(1 == AppCaseData.machineCodeList.get(position).getStatus()){
                    helper.setText(R.id.tv_machine_status,"安装");
                }else{
                    helper.setText(R.id.tv_machine_status,"状态");
                }
            }
        };
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        //设置RecyclerView管理器
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    protected void initEvents() {
        // 安装二维码
        ll_install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Scan.class);
                startActivity(intent);
                isInstall = 1;
            }
        });
        // 拆机二维码
        ll_uninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Scan.class);
                startActivity(intent);
                isInstall = 0;
            }
        });
    }

    @Override
    protected void loadData() {

    }

    /**
     *  将内容赋予 APPCaseData.carDetail 用于 内容上传
     * @param content
     */
    @Override
    public void observerUpData(String content) {
        // && tv_machine_code.getText().toString().contains("iC") TODO 机身串码校验
        if(null != tv_machine_code.getText() && !"".equals(tv_machine_code.getText().toString()) ) {
            AppCaseData.machineCode.setMachinecode(tv_machine_code.getText().toString());
            AppCaseData.machineCode.setStatus(isInstall);
        }
    }
}
