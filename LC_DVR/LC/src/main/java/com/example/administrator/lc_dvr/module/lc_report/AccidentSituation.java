package com.example.administrator.lc_dvr.module.lc_report;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.TimeUtils;

import org.videolan.libvlc.VLCApplication;

/**
 * Created by yangboru on 2018/1/11.
 */

public class AccidentSituation extends Activity {

    private TextView accidentSituationTitle;
    private int titleTap = 0;
    private Button accidentBtn1;
    private Button accidentBtn2;
    private Button accidentBtn3;
    private Button accidentBtn4;
    private Button accidentBtn5;
    private Button accidentBtn6;
    private Button switchLeft;
    private Button switchRight;
    private RelativeLayout ivCloseAccidentSituation;

    private String inskind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //表示设置当前的Activity无Title并且全屏,调用这个方法有个限制,即必须在setContentView(R.layout.main)之前调用,否则会抛出异常
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.accident_situation_layout);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        accidentSituationTitle = (TextView) findViewById(R.id.accidentSituationTitle);
        accidentBtn1 = (Button) findViewById(R.id.accidentBtn1);
        accidentBtn2 = (Button) findViewById(R.id.accidentBtn2);
        accidentBtn3 = (Button) findViewById(R.id.accidentBtn3);
        accidentBtn4 = (Button) findViewById(R.id.accidentBtn4);
        accidentBtn5 = (Button) findViewById(R.id.accidentBtn5);
        accidentBtn6 = (Button) findViewById(R.id.accidentBtn6);
        switchLeft = (Button) findViewById(R.id.switchLeft);
        switchRight = (Button) findViewById(R.id.switchRight);
        ivCloseAccidentSituation = (RelativeLayout) findViewById(R.id.closeAccidentSituation);
        //设置对应的内容
        Intent intent = getIntent();
        accidentSituationTitle.setText(intent.getStringExtra("accidentSituationTitle"));

        accidentBtn1.setText(intent.getStringExtra("accidentBtn1"));
        accidentBtn2.setText(intent.getStringExtra("accidentBtn2"));
        accidentBtn3.setText(intent.getStringExtra("accidentBtn3"));
        accidentBtn4.setText(intent.getStringExtra("accidentBtn4"));
        accidentBtn5.setText(intent.getStringExtra("accidentBtn5"));
        accidentBtn6.setText(intent.getStringExtra("accidentBtn6"));

        if (intent.getBooleanExtra("accidentResponsibilityBtn", false)) {
            //显示事故责任的按钮
            accidentBtn3.setVisibility(View.VISIBLE);
            accidentBtn4.setVisibility(View.VISIBLE);
            accidentBtn5.setVisibility(View.VISIBLE);
            accidentBtn6.setVisibility(View.VISIBLE);
        } else {
            //隐藏事故责任的按钮
            accidentBtn3.setVisibility(View.GONE);
            accidentBtn4.setVisibility(View.GONE);
            accidentBtn5.setVisibility(View.GONE);
            accidentBtn6.setVisibility(View.GONE);
        }

        switch (intent.getIntExtra("switchBtn", 0)) {
            case 1:
                //是否要隐藏某些按钮
                switchLeft.setVisibility(View.GONE);
                switchRight.setVisibility(View.VISIBLE);
                break;
            case 2:
                //是否要隐藏某些按钮
                switchLeft.setVisibility(View.VISIBLE);
                switchRight.setVisibility(View.VISIBLE);
                break;
            case 3:
                //是否要隐藏某些按钮
                switchLeft.setVisibility(View.VISIBLE);
                switchRight.setVisibility(View.GONE);
                break;
        }

        inskind = PreferenceUtil.getString("inskind","");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //记录当前的title
        switch (accidentSituationTitle.getText().toString()) {
            case "选择事故类型":
                titleTap = 1;
                break;
            case "选择事故责任":
                titleTap = 2;
                break;
            case "选择是否有人伤":
                titleTap = 3;
                break;
            case "选择车辆是否能正常行驶":
                titleTap = 4;
                break;
            default:
                break;
        }
        //显示用户设置的状态
        showPersonSelete();
    }

    /**
     * 显示用户的选择
     */
    private void showPersonSelete() {
        if (accidentSituationTitle.getText().equals("选择事故责任")) {
            //显示事故责任的按钮
            if (PreferenceUtil.getInt("carCount", 0) == 1) {
                accidentBtn3.setVisibility(View.GONE);
                accidentBtn4.setVisibility(View.GONE);
                accidentBtn5.setVisibility(View.GONE);
                accidentBtn6.setVisibility(View.GONE);
            } else {
                accidentBtn3.setVisibility(View.VISIBLE);
                accidentBtn4.setVisibility(View.VISIBLE);
                accidentBtn5.setVisibility(View.VISIBLE);
                accidentBtn6.setVisibility(View.VISIBLE);
            }


            switch (PreferenceUtil.getInt("accidentResponsibility", 0)) {
                case 1:
                    accidentBtn3.setBackgroundResource(R.drawable.textview_button_press);
                    accidentBtn3.setTextColor(Color.WHITE);
                    //其他的按钮恢复没有选中的状态
                    accidentBtn4.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn4.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn5.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn5.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn6.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn6.setTextColor(Color.parseColor("#2196F3"));
                    break;
                case 2:
                    accidentBtn4.setBackgroundResource(R.drawable.textview_button_press);
                    accidentBtn4.setTextColor(Color.WHITE);
                    //其他的按钮恢复没有选中的状态
                    accidentBtn3.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn3.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn5.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn5.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn6.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn6.setTextColor(Color.parseColor("#2196F3"));
                    break;
                case 3:
                    accidentBtn5.setBackgroundResource(R.drawable.textview_button_press);
                    accidentBtn5.setTextColor(Color.WHITE);
                    //其他的按钮恢复没有选中的状态
                    accidentBtn4.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn4.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn3.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn3.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn6.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn6.setTextColor(Color.parseColor("#2196F3"));
                    break;
                case 4:
                    accidentBtn6.setBackgroundResource(R.drawable.textview_button_press);
                    accidentBtn6.setTextColor(Color.WHITE);
                    //其他的按钮恢复没有选中的状态
                    accidentBtn4.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn4.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn5.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn5.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn3.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn3.setTextColor(Color.parseColor("#2196F3"));
                    break;
                default:
                    accidentBtn6.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn6.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn4.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn4.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn5.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn5.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn3.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn3.setTextColor(Color.parseColor("#2196F3"));
                    break;
            }
        } else {
            //隐藏事故责任的按钮
            accidentBtn3.setVisibility(View.GONE);
            accidentBtn4.setVisibility(View.GONE);
            accidentBtn5.setVisibility(View.GONE);
            accidentBtn6.setVisibility(View.GONE);
        }

        switch (accidentSituationTitle.getText().toString()) {
            case "选择是否有人伤":
                //是否要隐藏某些按钮
                switchLeft.setVisibility(View.VISIBLE);
                switchRight.setVisibility(View.VISIBLE);
                if (PreferenceUtil.getInt("isWounded", 0) == 1) {
                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_press);
                    accidentBtn1.setTextColor(Color.WHITE);
                    //其他的按钮恢复没有选中的状态
                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn2.setTextColor(Color.parseColor("#2196F3"));
                } else if (PreferenceUtil.getInt("isWounded", 0) == 2) {
                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_press);
                    accidentBtn2.setTextColor(Color.WHITE);
                    //其他的按钮恢复没有选中的状态
                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn1.setTextColor(Color.parseColor("#2196F3"));
                } else {
                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn1.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn2.setTextColor(Color.parseColor("#2196F3"));
                }
                break;
            case "选择车辆是否能正常行驶":
                //是否要隐藏某些按钮
                switchLeft.setVisibility(View.VISIBLE);
                switchRight.setVisibility(View.GONE);
                if (PreferenceUtil.getInt("isNormalDriving", 0) == 1) {
                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_press);
                    accidentBtn1.setTextColor(Color.WHITE);
                    //其他的按钮恢复没有选中的状态
                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn2.setTextColor(Color.parseColor("#2196F3"));
                } else if (PreferenceUtil.getInt("isNormalDriving", 0) == 2) {
                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_press);
                    accidentBtn2.setTextColor(Color.WHITE);
                    //其他的按钮恢复没有选中的状态
                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn1.setTextColor(Color.parseColor("#2196F3"));
                } else {
                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn1.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn2.setTextColor(Color.parseColor("#2196F3"));
                }
                break;
            case "选择事故类型":
                //是否要隐藏某些按钮
                switchLeft.setVisibility(View.GONE);
                switchRight.setVisibility(View.VISIBLE);
                if (PreferenceUtil.getInt("carCount", 0) == 1) {
                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_press);
                    accidentBtn1.setTextColor(Color.WHITE);
                    //其他的按钮恢复没有选中的状态
                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn2.setTextColor(Color.parseColor("#2196F3"));
                } else if (PreferenceUtil.getInt("carCount", 0) == 2) {
                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_press);
                    accidentBtn2.setTextColor(Color.WHITE);
                    //其他的按钮恢复没有选中的状态
                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn1.setTextColor(Color.parseColor("#2196F3"));
                } else {
                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn1.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn2.setTextColor(Color.parseColor("#2196F3"));
                }
                break;
            case "选择事故责任":
                //是否要隐藏某些按钮
                switchLeft.setVisibility(View.VISIBLE);
                switchRight.setVisibility(View.VISIBLE);
                if (PreferenceUtil.getInt("carCount", 0) == 1) {
                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_press);
                    accidentBtn1.setTextColor(Color.WHITE);
                    //其他的按钮恢复没有选中的状态
                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn2.setTextColor(Color.parseColor("#2196F3"));
                } else if (PreferenceUtil.getInt("carCount", 0) == 2) {
                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_press);
                    accidentBtn2.setTextColor(Color.WHITE);
                    //其他的按钮恢复没有选中的状态
                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn1.setTextColor(Color.parseColor("#2196F3"));
                } else {
                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn1.setTextColor(Color.parseColor("#2196F3"));
                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn2.setTextColor(Color.parseColor("#2196F3"));
                }
                break;
            default:
                break;
        }
    }

    /**
     * 关闭当前的页面
     *
     * @param view
     */
    public void closeAccidentSituation(View view) {
        if(TimeUtils.isFastClick()) {
            return;
        }
        finish();
    }

    /**
     * 右上角的叉，关闭当前页
     *
     * @param view
     */
    public void ivCloseAccidentSituation(View view) {
        if(TimeUtils.isFastClick()) {
            return;
        }
        finish();
    }

    /**
     * 点击右边事故切换btn时的响应
     *
     * @param view
     */
    public void accidentSwitchRight(View view) {
        if(accidentSituationTitle.getText().equals("选择事故类型") && PreferenceUtil.getInt("carCount", 0) == 1) {
            titleTap += 1;
        }
        titleTap += 1;
        switch (titleTap) {
            case 1:
                accidentBtn1.setText(getResources().getString(R.string.oneCar));
                accidentBtn2.setText(getResources().getString(R.string.manyCar));
                accidentBtn3.setText("");
                accidentBtn4.setText("");
                accidentBtn5.setText("");
                accidentBtn6.setText("");
                accidentSituationTitle.setText(getResources().getString(R.string.accidentTypeSelect));

                accidentBtn3.setVisibility(View.GONE);
                accidentBtn4.setVisibility(View.GONE);
                accidentBtn5.setVisibility(View.GONE);
                accidentBtn6.setVisibility(View.GONE);
                break;
            case 2:
                accidentBtn1.setText(getResources().getString(R.string.oneCar));
                accidentBtn2.setText(getResources().getString(R.string.manyCar));
                accidentBtn3.setText(getResources().getString(R.string.fullResponsibility));
                accidentBtn4.setText(getResources().getString(R.string.mainResponsibility));
                accidentBtn5.setText(getResources().getString(R.string.sameResponsibility));
                accidentBtn6.setText(getResources().getString(R.string.secondaryResponsibility));
                accidentSituationTitle.setText(getResources().getString(R.string.accidentResponsibilitySelect));
                if (PreferenceUtil.getInt("carCount", 0) == 1) {
                    accidentBtn3.setVisibility(View.GONE);
                    accidentBtn4.setVisibility(View.GONE);
                    accidentBtn5.setVisibility(View.GONE);
                    accidentBtn6.setVisibility(View.GONE);
                } else if (PreferenceUtil.getInt("carCount", 0) == 2) {
                    accidentBtn3.setVisibility(View.VISIBLE);
                    accidentBtn4.setVisibility(View.VISIBLE);
                    accidentBtn5.setVisibility(View.VISIBLE);
                    accidentBtn6.setVisibility(View.VISIBLE);
                }
                break;
            case 3:
                accidentBtn1.setText(getResources().getString(R.string.yes));
                accidentBtn2.setText(getResources().getString(R.string.no));
                accidentBtn3.setText("");
                accidentBtn4.setText("");
                accidentBtn5.setText("");
                accidentBtn6.setText("");
                accidentSituationTitle.setText(getResources().getString(R.string.isWoundedSelect));
                break;
            case 4:
                accidentBtn1.setText(getResources().getString(R.string.yes));
                accidentBtn2.setText(getResources().getString(R.string.no));
                accidentBtn3.setText("");
                accidentBtn4.setText("");
                accidentBtn5.setText("");
                accidentBtn6.setText("");
                accidentSituationTitle.setText(getResources().getString(R.string.isNormalDrivingSelect));
                break;
            default:
                break;
        }

        //显示用户设置的状态
        showPersonSelete();
    }


    /**
     * 点击左边事故切换btn时的响应
     *
     * @param view
     */
    public void accidentSwitchLeft(View view) {
        if(accidentSituationTitle.getText().equals("选择是否有人伤") && PreferenceUtil.getInt("carCount", 0) == 1) {
            titleTap -= 1;
        }
        titleTap -= 1;
        switch (titleTap) {
            case 1:
                accidentBtn1.setText(getResources().getString(R.string.oneCar));
                accidentBtn2.setText(getResources().getString(R.string.manyCar));
                accidentBtn3.setText("");
                accidentBtn4.setText("");
                accidentBtn5.setText("");
                accidentBtn6.setText("");
                accidentSituationTitle.setText(getResources().getString(R.string.accidentTypeSelect));

                accidentBtn3.setVisibility(View.GONE);
                accidentBtn4.setVisibility(View.GONE);
                accidentBtn5.setVisibility(View.GONE);
                accidentBtn6.setVisibility(View.GONE);
                break;
            case 2:
                accidentBtn1.setText(getResources().getString(R.string.oneCar));
                accidentBtn2.setText(getResources().getString(R.string.manyCar));
                accidentBtn3.setText(getResources().getString(R.string.fullResponsibility));
                accidentBtn4.setText(getResources().getString(R.string.mainResponsibility));
                accidentBtn5.setText(getResources().getString(R.string.sameResponsibility));
                accidentBtn6.setText(getResources().getString(R.string.secondaryResponsibility));
                accidentSituationTitle.setText(getResources().getString(R.string.accidentResponsibilitySelect));
                if (PreferenceUtil.getInt("carCount", 0) == 1) {
                    accidentBtn3.setVisibility(View.GONE);
                    accidentBtn4.setVisibility(View.GONE);
                    accidentBtn5.setVisibility(View.GONE);
                    accidentBtn6.setVisibility(View.GONE);
                } else if (PreferenceUtil.getInt("carCount", 0) == 2) {
                    accidentBtn3.setVisibility(View.VISIBLE);
                    accidentBtn4.setVisibility(View.VISIBLE);
                    accidentBtn5.setVisibility(View.VISIBLE);
                    accidentBtn6.setVisibility(View.VISIBLE);
                }

                break;
            case 3:
                accidentBtn1.setText(getResources().getString(R.string.yes));
                accidentBtn2.setText(getResources().getString(R.string.no));
                accidentBtn3.setText("");
                accidentBtn4.setText("");
                accidentBtn5.setText("");
                accidentBtn6.setText("");
                accidentSituationTitle.setText(getResources().getString(R.string.isWoundedSelect));
                break;
            case 4:
                accidentBtn1.setText(getResources().getString(R.string.yes));
                accidentBtn2.setText(getResources().getString(R.string.no));
                accidentBtn3.setText("");
                accidentBtn4.setText("");
                accidentBtn5.setText("");
                accidentBtn6.setText("");
                accidentSituationTitle.setText(getResources().getString(R.string.isNormalDrivingSelect));
                break;
            default:
                break;
        }

        //显示用户设置的状态
        showPersonSelete();
    }


    /**
     * 点击选择之后跳到下个页面 和 点右边箭头效果一样
     */
    private void nextAccidentSwitch() {
        titleTap += 1;
        switch (titleTap) {
            case 1:
                accidentBtn1.setText(getResources().getString(R.string.oneCar));
                accidentBtn2.setText(getResources().getString(R.string.manyCar));
                accidentBtn3.setText("");
                accidentBtn4.setText("");
                accidentBtn5.setText("");
                accidentBtn6.setText("");
                accidentSituationTitle.setText(getResources().getString(R.string.accidentTypeSelect));

                accidentBtn3.setVisibility(View.GONE);
                accidentBtn4.setVisibility(View.GONE);
                accidentBtn5.setVisibility(View.GONE);
                accidentBtn6.setVisibility(View.GONE);
                break;
            case 2:
                accidentBtn1.setText(getResources().getString(R.string.oneCar));
                accidentBtn2.setText(getResources().getString(R.string.manyCar));
                accidentBtn3.setText(getResources().getString(R.string.fullResponsibility));
                accidentBtn4.setText(getResources().getString(R.string.mainResponsibility));
                accidentBtn5.setText(getResources().getString(R.string.sameResponsibility));
                accidentBtn6.setText(getResources().getString(R.string.secondaryResponsibility));
                accidentSituationTitle.setText(getResources().getString(R.string.accidentResponsibilitySelect));
                if (PreferenceUtil.getInt("carCount", 0) == 1) {
                    accidentBtn3.setVisibility(View.GONE);
                    accidentBtn4.setVisibility(View.GONE);
                    accidentBtn5.setVisibility(View.GONE);
                    accidentBtn6.setVisibility(View.GONE);
                } else if (PreferenceUtil.getInt("carCount", 0) == 2) {
                    accidentBtn3.setVisibility(View.VISIBLE);
                    accidentBtn4.setVisibility(View.VISIBLE);
                    accidentBtn5.setVisibility(View.VISIBLE);
                    accidentBtn6.setVisibility(View.VISIBLE);
                }
                break;
            case 3:
                accidentBtn1.setText(getResources().getString(R.string.yes));
                accidentBtn2.setText(getResources().getString(R.string.no));
                accidentBtn3.setText("");
                accidentBtn4.setText("");
                accidentBtn5.setText("");
                accidentBtn6.setText("");
                accidentSituationTitle.setText(getResources().getString(R.string.isWoundedSelect));
                break;
            case 4:
                accidentBtn1.setText(getResources().getString(R.string.yes));
                accidentBtn2.setText(getResources().getString(R.string.no));
                accidentBtn3.setText("");
                accidentBtn4.setText("");
                accidentBtn5.setText("");
                accidentBtn6.setText("");
                accidentSituationTitle.setText(getResources().getString(R.string.isNormalDrivingSelect));
                break;
            default:
                break;
        }

        //显示用户设置的状态
        showPersonSelete();
    }


    /**
     * 点击事故btn6时的响应
     *
     * @param view
     */
    public void accidentBtn6Tap(View view) {
        if (accidentSituationTitle.getText().equals("选择事故责任")) {
            if (PreferenceUtil.getInt("accidentResponsibility", 0) != 4) {
                accidentBtn6.setBackgroundResource(R.drawable.textview_button_press);
                accidentBtn6.setTextColor(Color.WHITE);
                PreferenceUtil.commitInt("accidentResponsibility", 4);
                //其他的按钮恢复没有选中的状态
                accidentBtn4.setBackgroundResource(R.drawable.textview_button_normal_gray);
                accidentBtn4.setTextColor(Color.parseColor("#2196F3"));
                accidentBtn5.setBackgroundResource(R.drawable.textview_button_normal_gray);
                accidentBtn5.setTextColor(Color.parseColor("#2196F3"));
                accidentBtn3.setBackgroundResource(R.drawable.textview_button_normal_gray);
                accidentBtn3.setTextColor(Color.parseColor("#2196F3"));

                nextAccidentSwitch();// 跳转到下一页
            } else {
//                accidentBtn6.setBackgroundResource(R.drawable.textview_button_normal_gray);
//                accidentBtn6.setTextColor(Color.parseColor("#2196F3"));
//                PreferenceUtil.commitInt("accidentResponsibility", 0);

                nextAccidentSwitch();// 跳转到下一页
            }
        }
    }


    /**
     * 点击事故btn5时的响应
     *
     * @param view
     */
    public void accidentBtn5Tap(View view) {
        if (accidentSituationTitle.getText().equals("选择事故责任")) {
            if (PreferenceUtil.getInt("accidentResponsibility", 0) != 3) {
                accidentBtn5.setBackgroundResource(R.drawable.textview_button_press);
                accidentBtn5.setTextColor(Color.WHITE);
                PreferenceUtil.commitInt("accidentResponsibility", 3);
                //其他的按钮恢复没有选中的状态
                accidentBtn4.setBackgroundResource(R.drawable.textview_button_normal_gray);
                accidentBtn4.setTextColor(Color.parseColor("#2196F3"));
                accidentBtn6.setBackgroundResource(R.drawable.textview_button_normal_gray);
                accidentBtn6.setTextColor(Color.parseColor("#2196F3"));
                accidentBtn3.setBackgroundResource(R.drawable.textview_button_normal_gray);
                accidentBtn3.setTextColor(Color.parseColor("#2196F3"));

                nextAccidentSwitch();// 跳转到下一页
            } else {
//                accidentBtn5.setBackgroundResource(R.drawable.textview_button_normal_gray);
//                accidentBtn5.setTextColor(Color.parseColor("#2196F3"));
//                PreferenceUtil.commitInt("accidentResponsibility", 0);
                nextAccidentSwitch();// 跳转到下一页
            }
        }
    }


    /**
     * 点击事故btn4时的响应
     *
     * @param view
     */
    public void accidentBtn4Tap(View view) {
        if (accidentSituationTitle.getText().equals("选择事故责任")) {
            if (PreferenceUtil.getInt("accidentResponsibility", 0) != 2) {
                accidentBtn4.setBackgroundResource(R.drawable.textview_button_press);
                accidentBtn4.setTextColor(Color.WHITE);
                PreferenceUtil.commitInt("accidentResponsibility", 2);
                //其他的按钮恢复没有选中的状态
                accidentBtn5.setBackgroundResource(R.drawable.textview_button_normal_gray);
                accidentBtn5.setTextColor(Color.parseColor("#2196F3"));
                accidentBtn6.setBackgroundResource(R.drawable.textview_button_normal_gray);
                accidentBtn6.setTextColor(Color.parseColor("#2196F3"));
                accidentBtn3.setBackgroundResource(R.drawable.textview_button_normal_gray);
                accidentBtn3.setTextColor(Color.parseColor("#2196F3"));

                nextAccidentSwitch();// 跳转到下一页
            } else {
//                accidentBtn4.setBackgroundResource(R.drawable.textview_button_normal_gray);
//                accidentBtn4.setTextColor(Color.parseColor("#2196F3"));
//                PreferenceUtil.commitInt("accidentResponsibility", 0);
                nextAccidentSwitch();// 跳转到下一页
            }
        }
    }


    /**
     * 点击事故btn3时的响应
     *
     * @param view
     */
    public void accidentBtn3Tap(View view) {
        if (accidentSituationTitle.getText().equals("选择事故责任")) {
            if (PreferenceUtil.getInt("accidentResponsibility", 0) != 1) {
                accidentBtn3.setBackgroundResource(R.drawable.textview_button_press);
                accidentBtn3.setTextColor(Color.WHITE);
                PreferenceUtil.commitInt("accidentResponsibility", 1);
                //其他的按钮恢复没有选中的状态
                accidentBtn5.setBackgroundResource(R.drawable.textview_button_normal_gray);
                accidentBtn5.setTextColor(Color.parseColor("#2196F3"));
                accidentBtn6.setBackgroundResource(R.drawable.textview_button_normal_gray);
                accidentBtn6.setTextColor(Color.parseColor("#2196F3"));
                accidentBtn4.setBackgroundResource(R.drawable.textview_button_normal_gray);
                accidentBtn4.setTextColor(Color.parseColor("#2196F3"));

                nextAccidentSwitch();// 跳转到下一页
            } else {
//                accidentBtn3.setBackgroundResource(R.drawable.textview_button_normal_gray);
//                accidentBtn3.setTextColor(Color.parseColor("#2196F3"));
//                PreferenceUtil.commitInt("accidentResponsibility", 0);

                nextAccidentSwitch();// 跳转到下一页
            }
        }
    }


    /**
     * 点击事故btn2时的响应
     *
     * @param view
     */
    public void accidentBtn2Tap(View view) {
        switch (accidentBtn2.getText().toString()) {
            case "多车":
                if (accidentSituationTitle.getText().toString().equals("选择事故责任")) {// 如果全部责任显示了，就全显示
                    accidentBtn3.setVisibility(View.VISIBLE);
                    accidentBtn4.setVisibility(View.VISIBLE);
                    accidentBtn5.setVisibility(View.VISIBLE);
                    accidentBtn6.setVisibility(View.VISIBLE);
                } else {
                    accidentBtn3.setVisibility(View.GONE);
                    accidentBtn4.setVisibility(View.GONE);
                    accidentBtn5.setVisibility(View.GONE);
                    accidentBtn6.setVisibility(View.GONE);
                }
                if (PreferenceUtil.getInt("carCount", 0) != 2) {
                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_press);
                    accidentBtn2.setTextColor(Color.WHITE);
                    PreferenceUtil.commitInt("carCount", 2);
                    //其他的按钮恢复没有选中的状态
                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn1.setTextColor(Color.parseColor("#2196F3"));

                    if (titleTap == 1) {// 如果选择只是事故类型才跳转
                        nextAccidentSwitch();// 跳转到下一页
                    }
                    if(!inskind.contains(getResources().getString(R.string.manyCar))) {
                        if (VLCApplication.configsDictionary.get("APP-C-005") != null) {
                            Toast.makeText(this, VLCApplication.configsDictionary.get("APP-C-005"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "您所属单位暂不支持多车事故类型的在线定损哦", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
//                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_normal_gray);
//                    accidentBtn2.setTextColor(Color.parseColor("#2196F3"));
//                    PreferenceUtil.commitInt("carCount", 0);
                    if (titleTap == 1) {// 如果选择只是事故类型才跳转
                        nextAccidentSwitch();// 跳转到下一页
                    }
                    if(!inskind.contains(getResources().getString(R.string.manyCar))) {
                        if (VLCApplication.configsDictionary.get("APP-C-005") != null) {
                            Toast.makeText(this, VLCApplication.configsDictionary.get("APP-C-005"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "您所属单位暂不支持多车事故类型的在线定损哦", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            case "否":
                if (accidentSituationTitle.getText().equals("选择是否有人伤")) {
                    if (PreferenceUtil.getInt("isWounded", 0) != 2) {
                        accidentBtn2.setBackgroundResource(R.drawable.textview_button_press);
                        accidentBtn2.setTextColor(Color.WHITE);
                        PreferenceUtil.commitInt("isWounded", 2);
                        //其他的按钮恢复没有选中的状态
                        accidentBtn1.setBackgroundResource(R.drawable.textview_button_normal_gray);
                        accidentBtn1.setTextColor(Color.parseColor("#2196F3"));

                        nextAccidentSwitch();// 跳转到下一页

                    } else {
//                        accidentBtn2.setBackgroundResource(R.drawable.textview_button_normal_gray);
//                        accidentBtn2.setTextColor(Color.parseColor("#2196F3"));
//                        PreferenceUtil.commitInt("isWounded", 0);
                        nextAccidentSwitch();// 跳转到下一页
                    }
                } else if (accidentSituationTitle.getText().equals("选择车辆是否能正常行驶")) {
                    if (PreferenceUtil.getInt("isNormalDriving", 0) != 2) {
                        accidentBtn2.setBackgroundResource(R.drawable.textview_button_press);
                        accidentBtn2.setTextColor(Color.WHITE);
                        PreferenceUtil.commitInt("isNormalDriving", 2);
                        //其他的按钮恢复没有选中的状态
                        accidentBtn1.setBackgroundResource(R.drawable.textview_button_normal_gray);
                        accidentBtn1.setTextColor(Color.parseColor("#2196F3"));

                        nextAccidentSwitch();// 跳转到下一页
                        finish();
                    } else {
//                        accidentBtn2.setBackgroundResource(R.drawable.textview_button_normal_gray);
//                        accidentBtn2.setTextColor(Color.parseColor("#2196F3"));
//                        PreferenceUtil.commitInt("isNormalDriving", 0);
                        finish();
                    }
                }
                break;
            default:
                break;
        }
    }


    /**
     * 点击事故btn1时的响应
     *
     * @param view
     */
    public void accidentBtn1Tap(View view) {
        switch (accidentBtn1.getText().toString()) {
            case "单车":
                if (accidentSituationTitle.getText().toString().equals("选择事故责任")) {
                    accidentBtn3.setVisibility(View.GONE);
                    accidentBtn4.setVisibility(View.GONE);
                    accidentBtn5.setVisibility(View.GONE);
                    accidentBtn6.setVisibility(View.GONE);
                }else{
                    // 如果是选择事故责任
                    if(titleTap == 1) {
                        titleTap += 1;
                    }
                }

                if (PreferenceUtil.getInt("carCount", 0) != 1) {
                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_press);
                    accidentBtn1.setTextColor(Color.WHITE);
                    PreferenceUtil.commitInt("carCount", 1);
                    //其他的按钮恢复没有选中的状态
                    accidentBtn2.setBackgroundResource(R.drawable.textview_button_normal_gray);
                    accidentBtn2.setTextColor(Color.parseColor("#2196F3"));

                    nextAccidentSwitch();// 跳转到下一页

                    if(!inskind.contains(getResources().getString(R.string.oneCar))) {
                        if (VLCApplication.configsDictionary.get("APP-C-005") != null) {
                            Toast.makeText(this, VLCApplication.configsDictionary.get("APP-C-005"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "您所属单位暂不支持单车事故类型的在线定损哦", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
//                    accidentBtn1.setBackgroundResource(R.drawable.textview_button_normal_gray);
//                    accidentBtn1.setTextColor(Color.parseColor("#2196F3"));
//                    PreferenceUtil.commitInt("carCount", 0);
                    nextAccidentSwitch();// 跳转到下一页

                    if(!inskind.contains(getResources().getString(R.string.oneCar))) {
                        if (VLCApplication.configsDictionary.get("APP-C-005") != null) {
                            Toast.makeText(this, VLCApplication.configsDictionary.get("APP-C-005"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "您所属单位暂不支持单车事故类型的在线定损哦", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            case "是":
                if (accidentSituationTitle.getText().equals("选择是否有人伤")) {
                    if (PreferenceUtil.getInt("isWounded", 0) != 1) {
                        accidentBtn1.setBackgroundResource(R.drawable.textview_button_press);
                        accidentBtn1.setTextColor(Color.WHITE);
                        PreferenceUtil.commitInt("isWounded", 1);
                        //其他的按钮恢复没有选中的状态
                        accidentBtn2.setBackgroundResource(R.drawable.textview_button_normal_gray);
                        accidentBtn2.setTextColor(Color.parseColor("#2196F3"));

                        if(!inskind.contains("人伤")) {
                            if (VLCApplication.configsDictionary.get("APP-C-005") != null) {
                                Toast.makeText(this, VLCApplication.configsDictionary.get("APP-C-005"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "您所属单位暂不支持人伤事故的在线定损哦", Toast.LENGTH_SHORT).show();
                            }
                        }
                        nextAccidentSwitch();// 跳转到下一页

                    } else {
//                        accidentBtn1.setBackgroundResource(R.drawable.textview_button_normal_gray);
//                        accidentBtn1.setTextColor(Color.parseColor("#2196F3"));
//                        PreferenceUtil.commitInt("isWounded", 0);
                        nextAccidentSwitch();// 跳转到下一页
                    }
                } else if (accidentSituationTitle.getText().equals("选择车辆是否能正常行驶")) {
                    if (PreferenceUtil.getInt("isNormalDriving", 0) != 1) {
                        accidentBtn1.setBackgroundResource(R.drawable.textview_button_press);
                        accidentBtn1.setTextColor(Color.WHITE);
                        PreferenceUtil.commitInt("isNormalDriving", 1);
                        //其他的按钮恢复没有选中的状态
                        accidentBtn2.setBackgroundResource(R.drawable.textview_button_normal_gray);
                        accidentBtn2.setTextColor(Color.parseColor("#2196F3"));

                        nextAccidentSwitch();// 跳转到下一页
                        finish();
                    } else {
//                        accidentBtn1.setBackgroundResource(R.drawable.textview_button_normal_gray);
//                        accidentBtn1.setTextColor(Color.parseColor("#2196F3"));
//                        PreferenceUtil.commitInt("isNormalDriving", 0);
                        nextAccidentSwitch();// 跳转到下一页
                        finish();
                    }
                }
                break;
            default:
                break;
        }
    }

}
