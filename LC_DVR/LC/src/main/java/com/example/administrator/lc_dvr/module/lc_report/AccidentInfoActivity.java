package com.example.administrator.lc_dvr.module.lc_report;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.MessageEvent;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.AppCaseData;

import org.greenrobot.eventbus.EventBus;
import org.videolan.libvlc.VLCApplication;

public class AccidentInfoActivity extends Activity implements View.OnClickListener {

    private TextView tv_cancel; // 取消
    private TextView tv_ok;

    private Button btn_accident_type_one; // 单车
    private Button btn_accident_type_more;// 多车

    private LinearLayout ll_accident_responsibility; // 责任类型

    private Button btn_responsibility_all; // 全部责任
    private Button btn_responsibility_no; // 没有责任
    private Button btn_responsibility_most; // 主要责任
    private Button btn_responsibility_equal; // 同等责任
    private Button btn_responsibility_little; // 次要责任

    private Button btn_physical_damage_yes; // 有物损
    private Button btn_physical_damage_no; // 无物损

    private Button btn_wound_yes; // 有人伤
    private Button btn_wound_no; // 无人伤

    private Button btn_scene_yes; // 在事故现场
    private Button btn_scene_no; // 不在事故现场

    private Button btn_drivering_yes; // 能正常行驶
    private Button btn_drivering_no; // 能正常行驶

    private String inskind; //

    private int carCount;
    private int accidentResponsibility;
    private int isPhysicalDamage;
    private int isWounded;
    private int isScene;
    private int isNormalDriving;

    private boolean isReport; // 是否是已经报过的案件

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
        return R.layout.activity_accident_info;
    }

    protected void findView() {
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
        btn_accident_type_one = (Button) findViewById(R.id.btn_accident_type_one);
        btn_accident_type_more = (Button) findViewById(R.id.btn_accident_type_more);
        btn_responsibility_all = (Button) findViewById(R.id.btn_responsibility_all);
        btn_responsibility_no = (Button) findViewById(R.id.btn_responsibility_no);
        btn_responsibility_most = (Button) findViewById(R.id.btn_responsibility_most);
        btn_responsibility_equal = (Button) findViewById(R.id.btn_responsibility_equal);
        btn_responsibility_little = (Button) findViewById(R.id.btn_responsibility_little);
        btn_physical_damage_yes = (Button) findViewById(R.id.btn_physical_damage_yes);
        btn_physical_damage_no = (Button) findViewById(R.id.btn_physical_damage_no);
        btn_wound_yes = (Button) findViewById(R.id.btn_wound_yes);
        btn_wound_no = (Button) findViewById(R.id.btn_wound_no);
        btn_scene_yes = (Button) findViewById(R.id.btn_scene_yes);
        btn_scene_no = (Button) findViewById(R.id.btn_scene_no);
        btn_drivering_yes = (Button) findViewById(R.id.btn_drivering_yes);
        btn_drivering_no = (Button) findViewById(R.id.btn_drivering_no);
        ll_accident_responsibility = (LinearLayout) findViewById(R.id.ll_accident_responsibility);
    }

    protected void init() {

        Intent intent = getIntent();
        isReport = intent.getBooleanExtra("isReport",false);
        // 初始化数据
        carCount = AppCaseData.carCount;
        accidentResponsibility = AppCaseData.accidentResponsibility;
        isPhysicalDamage = AppCaseData.isPhysicalDamage;
        isWounded = AppCaseData.isWounded;
        isScene = AppCaseData.isScene;
        isNormalDriving = AppCaseData.isNormalDriving;

        inskind = PreferenceUtil.getString("inskind","");

        if(AppCaseData.carCount == 1) {
            btn_accident_type_one.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_accident_type_one.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));
            // 单车隐藏
            ll_accident_responsibility.setVisibility(View.GONE);
        }else {
            btn_accident_type_more.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_accident_type_more.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

            // 多车显示
            ll_accident_responsibility.setVisibility(View.VISIBLE);
        }

        if(AppCaseData.accidentResponsibility == 1) {
            btn_responsibility_all.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_responsibility_all.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));
        }else if(AppCaseData.accidentResponsibility == 2){
            btn_responsibility_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_responsibility_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));
        }else if(AppCaseData.accidentResponsibility == 3){
            btn_responsibility_most.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_responsibility_most.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));
        }else if(AppCaseData.accidentResponsibility == 4){
            btn_responsibility_equal.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_responsibility_equal.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));
        }else if(AppCaseData.accidentResponsibility == 5){
            btn_responsibility_little.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_responsibility_little.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));
        }

        if(AppCaseData.isPhysicalDamage == 1) {
            btn_physical_damage_yes.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_physical_damage_yes.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));
        }else {
            btn_physical_damage_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_physical_damage_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));
        }

        if(AppCaseData.isWounded == 1) {
            btn_wound_yes.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_wound_yes.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));
        }else {
            btn_wound_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_wound_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));
        }

        if(AppCaseData.isScene == 1) {
            btn_scene_yes.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_scene_yes.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));
        }else {
            btn_scene_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_scene_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));
        }

        if(AppCaseData.isNormalDriving == 1) {
            btn_drivering_yes.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_drivering_yes.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));
        }else {
            btn_drivering_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
            btn_drivering_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));
        }

        if(!AppCaseData.caseIsEdit) {
            btn_accident_type_one.setEnabled(false);
            btn_accident_type_more.setEnabled(false);
            btn_responsibility_all.setEnabled(false);
            btn_responsibility_no.setEnabled(false);
            btn_responsibility_most.setEnabled(false);
            btn_responsibility_equal.setEnabled(false);
            btn_responsibility_little.setEnabled(false);
            btn_physical_damage_yes.setEnabled(false);
            btn_physical_damage_no.setEnabled(false);
            btn_wound_yes.setEnabled(false);
            btn_wound_no.setEnabled(false);
            btn_scene_yes.setEnabled(false);
            btn_scene_no.setEnabled(false);
            btn_drivering_yes.setEnabled(false);
            btn_drivering_no.setEnabled(false);
        }else {
            btn_accident_type_one.setEnabled(true);
            btn_accident_type_more.setEnabled(true);
            btn_responsibility_all.setEnabled(true);
            btn_responsibility_no.setEnabled(true);
            btn_responsibility_most.setEnabled(true);
            btn_responsibility_equal.setEnabled(true);
            btn_responsibility_little.setEnabled(true);
            btn_physical_damage_yes.setEnabled(true);
            btn_physical_damage_no.setEnabled(true);
            btn_wound_yes.setEnabled(true);
            btn_wound_no.setEnabled(true);
            btn_scene_yes.setEnabled(true);
            btn_scene_no.setEnabled(true);
            btn_drivering_yes.setEnabled(true);
            btn_drivering_no.setEnabled(true);
        }
    }

    protected void initEvents() {
        tv_cancel.setOnClickListener(this);
        tv_ok.setOnClickListener(this);

        //单车
        btn_accident_type_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(carCount != 1) {
                    carCount = 1;

                    btn_accident_type_one.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_accident_type_one.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_accident_type_more.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_accident_type_more.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));

                    // 单车隐藏
                    ll_accident_responsibility.setVisibility(View.GONE);
                    if(!inskind.contains(getResources().getString(R.string.oneCar))) {
                        Toast.makeText(AccidentInfoActivity.this, "您所属单位暂不支持单车事故类型的在线定损哦", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
        // 多车
        btn_accident_type_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(carCount != 2) {
                    carCount = 2;

                    btn_accident_type_more.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_accident_type_more.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_accident_type_one.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_accident_type_one.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));

                    // 多车显示
                    ll_accident_responsibility.setVisibility(View.VISIBLE);

                    if(!inskind.contains(getResources().getString(R.string.manyCar))) {
                        if (VLCApplication.configsDictionary.get("APP-C-005") != null) {
                            Toast.makeText(AccidentInfoActivity.this, VLCApplication.configsDictionary.get("APP-C-005"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AccidentInfoActivity.this, "您所属单位暂不支持多车事故类型的在线定损哦", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        // 全部责任
        btn_responsibility_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accidentResponsibility != 1) {
                    accidentResponsibility = 1;
                    btn_responsibility_all.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_responsibility_all.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_responsibility_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_most.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_most.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_equal.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_equal.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_little.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_little.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                }
            }
        });
        // 没有责任
        btn_responsibility_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accidentResponsibility != 2) {
                    accidentResponsibility = 2;
                    btn_responsibility_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_responsibility_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_responsibility_all.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_all.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_most.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_most.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_equal.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_equal.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_little.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_little.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                }
            }
        });
        // 主要责任
        btn_responsibility_most.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accidentResponsibility != 3) {
                    accidentResponsibility = 3;
                    btn_responsibility_most.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_responsibility_most.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_responsibility_all.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_all.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_equal.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_equal.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_little.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_little.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                }
            }
        });
        // 同等责任
        btn_responsibility_equal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accidentResponsibility != 4) {
                    accidentResponsibility = 4;
                    btn_responsibility_equal.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_responsibility_equal.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_responsibility_all.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_all.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_most.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_most.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_little.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_little.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                }
            }
        });
        // 次要责任
        btn_responsibility_little.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accidentResponsibility != 5) {
                    accidentResponsibility = 5;
                    btn_responsibility_little.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_responsibility_little.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_responsibility_all.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_all.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_most.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_most.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                    btn_responsibility_equal.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_responsibility_equal.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                }
            }
        });

        // 有物损
        btn_physical_damage_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPhysicalDamage != 1) {
                    isPhysicalDamage = 1;
                    btn_physical_damage_yes.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_physical_damage_yes.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_physical_damage_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_physical_damage_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));

                    if(!inskind.contains("物损")) {
                        Toast.makeText(AccidentInfoActivity.this, "您所属单位暂不支持有物损事故类型的在线定损哦", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        // 无物损
        btn_physical_damage_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPhysicalDamage != 2) {
                    isPhysicalDamage = 2;
                    btn_physical_damage_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_physical_damage_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_physical_damage_yes.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_physical_damage_yes.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                }
            }
        });

        // 有人伤
        btn_wound_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isWounded != 1) {
                    isWounded = 1;
                    btn_wound_yes.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_wound_yes.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_wound_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_wound_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));

                    if(!inskind.contains("人伤")) {
                        Toast.makeText(AccidentInfoActivity.this, "您所属单位暂不支持有人伤事故类型的在线定损哦", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        // 无人伤
        btn_wound_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isWounded != 2) {
                    isWounded = 2;
                    btn_wound_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_wound_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_wound_yes.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_wound_yes.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                }
            }
        });

        // 在现场
        btn_scene_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isScene != 1) {
                    isScene = 1;
                    btn_scene_yes.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_scene_yes.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_scene_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_scene_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                }
            }
        });
        // 不在现场
        btn_scene_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isScene != 2) {
                    isScene = 2;
                    btn_scene_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_scene_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_scene_yes.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_scene_yes.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                }
            }
        });

        // 能正常行驶
        btn_drivering_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNormalDriving != 1) {
                    isNormalDriving = 1;
                    btn_drivering_yes.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_drivering_yes.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_drivering_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_drivering_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                }
            }
        });
        // 不能正常行驶
        btn_drivering_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNormalDriving != 2) {
                    isNormalDriving = 2;
                    btn_drivering_no.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.primary_dark));
                    btn_drivering_no.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.mipmap.accident_blue));

                    btn_drivering_yes.setTextColor(ContextCompat.getColor(AccidentInfoActivity.this,R.color.normal_black));
                    btn_drivering_yes.setBackground(ContextCompat.getDrawable(AccidentInfoActivity.this,R.color.gray_back));
                } else {

                }
            }
        });
    }

    protected void loadData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.tv_ok:
                // 保存修改的内容
                if(carCount != AppCaseData.carCount) {// 如果单车多车有进行切换
                    AppCaseData.carCount = carCount;
                    if(isReport) { // 如果是已经报过的案子，非新建案件，进行回调切换单车多车
                        Utils.doOneMoreSwitch();
                    }
                }

                AppCaseData.accidentResponsibility = accidentResponsibility;
                AppCaseData.isPhysicalDamage = isPhysicalDamage;
                AppCaseData.isWounded = isWounded;
                AppCaseData.isScene = isScene;
                AppCaseData.isNormalDriving = isNormalDriving;

                finish();
                break;
        }
    }

}
