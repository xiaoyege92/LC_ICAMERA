package com.example.administrator.lc_dvr.module.lc_report;


import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.bean.Car;
import com.example.administrator.lc_dvr.bean.InsCompany;
import com.example.administrator.lc_dvr.bean.Unit;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.common.utils.observer.ObserverListener;
import com.example.administrator.lc_dvr.common.utils.observer.ObserverManager;
import com.example.administrator.lc_dvr.module.AppCaseData;
import com.example.administrator.lc_dvr.module.login_registration.UnitCode;

import org.videolan.libvlc.VLCApplication;

/**
 *
 */
public class BasicInfoFragment extends BaseFragment implements View.OnClickListener,ObserverListener{

    private TextView tv_unit_code; // 单位快捷码
    private TextView tv_unit_name; // 单位名称
    private TextView tv_service_name; // 事故专员姓名
    private TextView tv_service_status; // 事故专员工作状态

    private EditText et_plate_number; // 车牌号
    private EditText et_label_models; // 厂牌车型
    private EditText et_name; // 驾驶员姓名
    private EditText et_phone_number; // 驾驶员手机号


    private String unitcode;// 单位code

    @Override
    public void onResume() {
        super.onResume();
        // 单位名称
        if (!"".equals(PreferenceUtil.getString("unitName", ""))) {
            tv_unit_name.setText(PreferenceUtil.getString("unitName", ""));
            tv_unit_name.setVisibility(View.VISIBLE);
        }
        // 单位快捷码
        if (!"".equals(PreferenceUtil.getString("unitcode", ""))) {
            unitcode = PreferenceUtil.getString("unitcode", "");
            tv_unit_code.setText(PreferenceUtil.getString("unitCodeShortCude", ""));
            AppCaseData.carDetail.setUnitcode(PreferenceUtil.getString("unitcode", ""));
        }
        // 事故专员姓名
        if (!"".equals(PreferenceUtil.getString("servicepersonname", ""))) {
            tv_service_name.setText(PreferenceUtil.getString("servicepersonname", ""));
            AppCaseData.carDetail.setServicecode(PreferenceUtil.getString("servicecode", ""));
        }
        // 事故专员工作状态
        if (!"".equals(PreferenceUtil.getString("serviceworkstatus", ""))) {
            tv_service_status.setText(PreferenceUtil.getString("serviceworkstatus", ""));
        }

    }

    @Override
    protected int setViewId() {
        return R.layout.fragment_basic_info;
    }

    @Override
    protected void findView(View view) {
        tv_unit_code = (TextView) view.findViewById(R.id.tv_unit_code);
        tv_unit_name = (TextView) view.findViewById(R.id.tv_unit_name);
        tv_service_name = (TextView) view.findViewById(R.id.tv_service_name);
        tv_service_status = (TextView) view.findViewById(R.id.tv_service_status);

        et_plate_number = (EditText) view.findViewById(R.id.et_plate_number);
        et_label_models = (EditText) view.findViewById(R.id.et_label_models);
        et_name = (EditText) view.findViewById(R.id.et_name);
        et_phone_number = (EditText) view.findViewById(R.id.et_phone_number);

    }

    @Override
    protected void init() {
        // 初始化数据
        PreferenceUtil.commitString("unitName", "");
        PreferenceUtil.commitString("unitcode", "");
        PreferenceUtil.commitString("unitCodeShortCude", "");
        PreferenceUtil.commitString("servicecode", "");
        PreferenceUtil.commitString("servicepersonname", "");
        PreferenceUtil.commitString("serviceworkstatus", "");

        // 初始化保险公司数据
        PreferenceUtil.commitString("insuranceCompany", "");
        PreferenceUtil.commitString("insuranceCompanyMobile", "");

        ObserverManager.getInstance().add(this);
        if(null == AppCaseData.unitDetail) {
            AppCaseData.unitDetail = new Unit();
        }
        if(null == AppCaseData.carDetail) {
            AppCaseData.carDetail = new Car();
        }
        if(null == AppCaseData.insCompanyDetail) {
            AppCaseData.insCompanyDetail = new InsCompany();
        }

    }

    @Override
    protected void initEvents() {
        tv_unit_code.setOnClickListener(this);
    }

    @Override
    protected void loadData() {
        if(null != AppCaseData.unitDetail ) {
            tv_unit_code.setText(Utils.parseStr(AppCaseData.unitDetail.getShortcode()));
            tv_unit_name.setText(Utils.parseStr(AppCaseData.unitDetail.getUnitname()));
        }
        if(null != AppCaseData.servicePersonDetail) {
            tv_service_name.setText(AppCaseData.servicePersonDetail.getUsername());

            if (AppCaseData.servicePersonDetail.getWorkstatus() == 0) {
                tv_service_status.setText("闲");
            } else if (AppCaseData.servicePersonDetail.getWorkstatus() == 1) {
                tv_service_status.setText("忙");
            } else if (AppCaseData.servicePersonDetail.getWorkstatus() == 2) {
                tv_service_status.setText("休");
            }
        }
       if(null != AppCaseData.carDetail) {
           et_plate_number.setText(Utils.parseStr(AppCaseData.carDetail.getCarnumber()));
           et_label_models.setText(Utils.parseStr(AppCaseData.carDetail.getCartype()));
           et_name.setText(Utils.parseStr(AppCaseData.carDetail.getCarername()));
           et_phone_number.setText(AppCaseData.carDetail.getCarermobile());
       }
       // 如果是新增的配置，清空工作状态，增加手机号
        if(CarConfigDetail.isNewCar) {
            tv_service_status.setText("");
            et_phone_number.setText(PreferenceUtil.getString("user_mobile",""));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_unit_code:
                Intent intent = new Intent(getActivity(), UnitCode.class);
                startActivity(intent);
                break;
        }
    }

    /**
     *  将内容赋予 APPCaseData.carDetail 用于 内容上传
     * @param content
     */
    @Override
    public void observerUpData(String content) {
        // 单位code
        if (!"".equals(PreferenceUtil.getString("unitcode", ""))) {
            AppCaseData.carDetail.setUnitcode(PreferenceUtil.getString("unitcode", ""));
        }
        // 事故专员code
        if (!"".equals(PreferenceUtil.getString("servicepersonname", ""))) {
            AppCaseData.carDetail.setServicecode(PreferenceUtil.getString("servicecode", ""));
        }
        // 车牌号
        AppCaseData.carDetail.setCarnumber(et_plate_number.getText().toString());
        // 厂牌车型
        AppCaseData.carDetail.setCartype(et_label_models.getText().toString());
        // 车主姓名
        AppCaseData.carDetail.setCarername(et_name.getText().toString());
        // 车主电话
        AppCaseData.carDetail.setCarermobile(et_phone_number.getText().toString());
    }
}
