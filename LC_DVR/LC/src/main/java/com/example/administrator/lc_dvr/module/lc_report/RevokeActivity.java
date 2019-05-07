package com.example.administrator.lc_dvr.module.lc_report;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.bean.RevokeReason;
import com.example.administrator.lc_dvr.common.adapter.CommonRecyclerAdapter;
import com.example.administrator.lc_dvr.common.adapter.RecyclerViewHolder;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;

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

public class RevokeActivity extends Activity {

    private TextView tv_cancel;
    private TextView tv_ok;

    private EditText et_revoke; // 销案原因
    private RecyclerView rv_revoke_type; // 销案原因类型

    private List<RevokeReason> revokeTypeList; // 销案原因类型列表

    private CommonRecyclerAdapter adapter;
    private String caseid;
    private int mPosition; // 选中的那一项

    private NormalDialog dialog;
    private String[] mStringBts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //表示设置当前的Activity无Title并且全屏,调用这个方法有个限制,即必须在setContentView(R.layout.main)之前调用,否则会抛出异常
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.activity_revoke);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        findView();
        init();
        initEvents();
        loadData();

    }

    private void findView() {
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
        et_revoke = (EditText) findViewById(R.id.et_revoke);
        rv_revoke_type = (RecyclerView) findViewById(R.id.rv_revoke_type);
    }

    private void init() {

        dialog = new NormalDialog(RevokeActivity.this);
        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);
        caseid = getIntent().getStringExtra("caseid");
        // 初始化数据
        revokeTypeList = new ArrayList<RevokeReason>();

        adapter = new CommonRecyclerAdapter(RevokeActivity.this, R.layout.revoke_item, revokeTypeList) {
            @Override
            public void convert(RecyclerViewHolder holder, Object t, final int position) {
                holder.setText(R.id.tv_type, revokeTypeList.get(position).getReason());
                RadioButton radioButton = holder.getView(R.id.rb_type);
                radioButton.setChecked(revokeTypeList.get(position).isChecked());


                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPosition = position;
                        for (int i = 0; i < revokeTypeList.size(); i++) {
                            revokeTypeList.get(i).setChecked(false);
                        }
                        revokeTypeList.get(position).setChecked(true);

                        notifyDataSetChanged();
                    }
                });
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(RevokeActivity.this, LinearLayoutManager.VERTICAL, false);
        rv_revoke_type.setLayoutManager(layoutManager);
        rv_revoke_type.setAdapter(adapter);
    }

    private void initEvents() {
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 选择其他原因时，必须手动输入原因才可销案
                if ("0".equals(revokeTypeList.get(mPosition).getNumber()) && "".equals(et_revoke.getText().toString())) {
                    ToastUtils.showNomalShortToast(RevokeActivity.this, "请您输入销案原因");
                    return;
                }
                showTipDialog("您确认要销案吗？");
                dialog.setOnBtnClickL(
                        new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                                // 销案
                                revoke(mPosition);
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

    private void loadData() {
        String[] reasonTypes;

        if (VLCApplication.configsDictionary.get("app-c-227") != null) {
            reasonTypes = VLCApplication.configsDictionary.get("app-c-227").split("，");
        } else {
            reasonTypes = "1-案件信息填写错了，2-已送至其他店维修，0-其他".split("，");
        }

        for (int i = 0; i < reasonTypes.length; i++) {
            RevokeReason revokeReason = new RevokeReason();
            String number = reasonTypes[i].split("-")[0];
            revokeReason.setNumber(number);
            revokeReason.setReason(reasonTypes[i].split("-")[1]);
            if ("0".equals(number)) {
                revokeReason.setChecked(true);
                mPosition = i;
            } else {
                revokeReason.setChecked(false);
            }
            revokeTypeList.add(revokeReason);

        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 撤销
     */
    private void revoke(int position) {
        Map<String, String> map = new HashMap<>();
        //设置事故ID
        map.put("caseid", caseid);
        map.put("cancelreason", revokeTypeList.get(position).getNumber());
        map.put("cancelreasondetial", et_revoke.getText().toString());

        Call<ResponseBody> call = RetrofitManager.getInstance().create().revokeCase(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        finish();
                    } else {
                        if (VLCApplication.configsDictionary.get("app-c-206") != null) {
                            Toast.makeText(RevokeActivity.this, VLCApplication.configsDictionary.get("app-c-206"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RevokeActivity.this, "未销案成功，请稍后再试", Toast.LENGTH_SHORT).show();
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
                if (VLCApplication.configsDictionary.get("app-c-206") != null) {
                    Toast.makeText(RevokeActivity.this, VLCApplication.configsDictionary.get("app-c-206"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RevokeActivity.this, "未销案成功，请稍后再试", Toast.LENGTH_SHORT).show();
                }
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
                .btnTextColor(new int[]{ContextCompat.getColor(RevokeActivity.this, R.color.primary), ContextCompat.getColor(RevokeActivity.this, R.color.alphablack)})
                .show();
        dialog.setCanceledOnTouchOutside(false);
    }
}
