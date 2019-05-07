package com.example.administrator.lc_dvr.module.lc_help;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.bean.CaseRepair;
import com.example.administrator.lc_dvr.bean.CaseRepairFinish;
import com.example.administrator.lc_dvr.common.adapter.CommonRecyclerAdapter;
import com.example.administrator.lc_dvr.common.adapter.RecyclerViewHolder;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.ListDataSave;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.lc_report.LookImageList;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
 *   time   : 2018/11/15
 *   desc   :
 *  version :
 * </pre>
 */
public class RepairDetail extends BaseActivity implements View.OnClickListener {

    private RadioButton rb_back; // 左上角返回键
    private RadioButton rb_report_detail;
    private TextView tv_plan_hand_car_date; // 预计交车日期
    private TextView tv_finish_date; // 维修完毕日期
    private TextView tv_real_hand_car_date; // 实际交车日期
    private TextView tv_repair_money_tip; // 维修金额说明
    private TextView tv_repair_money; // 维修金额

    private RecyclerView rv_before_picture; // 维修前照片
    private RecyclerView rv_after_picture; // 维修后照片
    private RecyclerView rv_invoice_picture; // 发票与维修清单

    private LinearLayout ll_repair; // 维修进度

    private ArrayList<String> beforeRepairpicturesArr; // 维修前照片
    private ArrayList<String> afterRepairpicturesArr; // 维修后照片
    private ArrayList<String> invoicePicturesArr;// 发票清单

    private CommonRecyclerAdapter beforeRepairAdapter;
    private CommonRecyclerAdapter afterRepairAdapter;
    private CommonRecyclerAdapter invoiceAdapter;

    private ListDataSave dataSave;

    private ArrayList<CaseRepair> planHandCarArr;
    private ArrayList<CaseRepairFinish> finishRepairArr;

    @Override
    protected int setViewId() {
        return R.layout.repair_detail_activity;
    }

    @Override
    protected void findView() {
        rb_back = (RadioButton) findViewById(R.id.rb_back);
        rb_report_detail = (RadioButton) findViewById(R.id.rb_report_detail);
        tv_plan_hand_car_date = (TextView) findViewById(R.id.tv_plan_hand_car_date);
        tv_finish_date = (TextView) findViewById(R.id.tv_finish_date);
        tv_real_hand_car_date = (TextView) findViewById(R.id.tv_real_hand_car_date);

        tv_repair_money_tip = (TextView) findViewById(R.id.tv_repair_money_tip);
        tv_repair_money = (TextView) findViewById(R.id.tv_repair_money);

        rv_before_picture = (RecyclerView) findViewById(R.id.rv_before_picture);
        rv_after_picture = (RecyclerView) findViewById(R.id.rv_after_picture);
        rv_invoice_picture = (RecyclerView) findViewById(R.id.rv_invoice_picture);

        ll_repair = (LinearLayout) findViewById(R.id.ll_repair);
    }

    @Override
    protected void init() {
        beforeRepairpicturesArr = new ArrayList<>();
        afterRepairpicturesArr = new ArrayList<>();
        invoicePicturesArr = new ArrayList<>();

        planHandCarArr = new ArrayList<>();
        finishRepairArr = new ArrayList<>();
        //用来保存list到本地
        dataSave = new ListDataSave(RepairDetail.this, "baiyu");

        beforeRepairAdapter = new CommonRecyclerAdapter(RepairDetail.this, R.layout.repairpictures_item, beforeRepairpicturesArr) {
            @Override
            public void convert(RecyclerViewHolder holder, Object t, final int position) {
                ImageView repair_report_image = holder.getView(R.id.repair_report_image);

                holder.setImageByUrl(R.id.repair_report_image, beforeRepairpicturesArr.get(position), RepairDetail.this);

                repair_report_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //判断是那个list
                        PreferenceUtil.commitInt("whichList", -1);
                        //判断是否要隐藏删除按钮
                        PreferenceUtil.commitBoolean("isEditReport", false);
                        //保存当前的reportPhotoArr1
                        dataSave.setDataList("LookImageList", beforeRepairpicturesArr);
                        Intent intent = new Intent(RepairDetail.this, LookImageList.class);
                        intent.putExtra("current", position);

                        startActivity(intent);
                        PreferenceUtil.commitInt("numOfPages", beforeRepairpicturesArr.size() - 1);
                    }
                });
            }
        };
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(RepairDetail.this, LinearLayoutManager.HORIZONTAL, false);
        rv_before_picture.setLayoutManager(layoutManager1);
        rv_before_picture.setAdapter(beforeRepairAdapter);

        afterRepairAdapter = new CommonRecyclerAdapter(RepairDetail.this, R.layout.repairpictures_item, afterRepairpicturesArr) {
            @Override
            public void convert(RecyclerViewHolder holder, Object t, final int position) {
                ImageView repair_report_image = holder.getView(R.id.repair_report_image);

                holder.setImageByUrl(R.id.repair_report_image, afterRepairpicturesArr.get(position), RepairDetail.this);

                repair_report_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //判断是那个list
                        PreferenceUtil.commitInt("whichList", -2);
                        //判断是否要隐藏删除按钮
                        PreferenceUtil.commitBoolean("isEditReport", false);
                        //保存当前的reportPhotoArr1
                        dataSave.setDataList("LookImageList", afterRepairpicturesArr);
                        Intent intent = new Intent(RepairDetail.this, LookImageList.class);
                        intent.putExtra("current", position);

                        startActivity(intent);
                        PreferenceUtil.commitInt("numOfPages", afterRepairpicturesArr.size() - 1);
                    }
                });
            }
        };
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(RepairDetail.this, LinearLayoutManager.HORIZONTAL, false);
        rv_after_picture.setLayoutManager(layoutManager2);
        rv_after_picture.setAdapter(afterRepairAdapter);

        invoiceAdapter = new CommonRecyclerAdapter(RepairDetail.this, R.layout.repairpictures_item, invoicePicturesArr) {
            @Override
            public void convert(RecyclerViewHolder holder, Object t, final int position) {
                ImageView repair_report_image = holder.getView(R.id.repair_report_image);

                holder.setImageByUrl(R.id.repair_report_image, invoicePicturesArr.get(position), RepairDetail.this);

                repair_report_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //判断是那个list
                        PreferenceUtil.commitInt("whichList", -2);
                        //判断是否要隐藏删除按钮
                        PreferenceUtil.commitBoolean("isEditReport", false);
                        //保存当前的reportPhotoArr1
                        dataSave.setDataList("LookImageList", invoicePicturesArr);
                        Intent intent = new Intent(RepairDetail.this, LookImageList.class);
                        intent.putExtra("current", position);

                        startActivity(intent);
                        PreferenceUtil.commitInt("numOfPages", invoicePicturesArr.size() - 1);
                    }
                });
            }
        };
        LinearLayoutManager layoutManager3 = new LinearLayoutManager(RepairDetail.this, LinearLayoutManager.HORIZONTAL, false);
        rv_invoice_picture.setLayoutManager(layoutManager3);
        rv_invoice_picture.setAdapter(invoiceAdapter);
    }

    @Override
    protected void initEvents() {
        rb_back.setOnClickListener(this);
        ll_repair.setOnClickListener(this);
    }

    @Override
    protected void loadData() {
        getCasedetail(); // 获取案子详情
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_back:
                finish();
                break;
            case R.id.ll_repair:
                Intent intent = new Intent(RepairDetail.this, RepairProgressActivity.class);

                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("finishRepairArr", finishRepairArr);
                bundle.putParcelableArrayList("planHandCarArr", planHandCarArr);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
    }

    /**
     * 获得事故ID的具体内容
     */
    private void getCasedetail() {
        beforeRepairpicturesArr.clear();
        afterRepairpicturesArr.clear();
        invoicePicturesArr.clear();
        planHandCarArr.clear();
        finishRepairArr.clear();

        Map<String, String> map = new HashMap<>();
        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getCaseDetail(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    String msg = jsonObject.getString("msg");
                    if (jsonObject.getBoolean(Config.SUCCESS)) {

                        JSONObject datas = jsonObject.getJSONObject("datas");
                        tv_plan_hand_car_date.setText(Utils.parseStr(datas.getJSONObject("case").getString("prerepairdate")));
                        tv_finish_date.setText(Utils.parseStr(datas.getJSONObject("case").getString("finishrepairdate")));
                        tv_real_hand_car_date.setText(Utils.parseStr(datas.getJSONObject("case").getString("finishrepairdate")));

                        tv_repair_money_tip.setText(Utils.parseStr(datas.getJSONObject("case").getString("repairmsg"))); // 维修金额说明
                        tv_repair_money.setText(Utils.parseStr(datas.getJSONObject("case").getString("repairmoney"))); // 维修金额
                        //维修前照片
                        JSONArray repairpictures = datas.getJSONArray("repairpictures");
                        for (int i = 0; i < repairpictures.length(); i++) {
                            JSONObject value = (JSONObject) repairpictures.get(i);
                            String piclink = value.getString("attachid");
                            beforeRepairpicturesArr.add(Config.QINIU_BASE_URL + piclink);
                        }
                        beforeRepairAdapter.notifyDataSetChanged();
                        //维修后照片
                        JSONArray pictures = datas.getJSONArray("repairpictures2");
                        for (int i = 0; i < pictures.length(); i++) {
                            JSONObject value = (JSONObject) pictures.get(i);
                            String piclink = value.getString("attachid");
                            afterRepairpicturesArr.add(Config.QINIU_BASE_URL + piclink);
                        }
                        afterRepairAdapter.notifyDataSetChanged();

                        //发票清单
                        JSONArray invoicePictures = datas.getJSONArray("invoicepictures");
                        for (int i = 0; i < invoicePictures.length(); i++) {
                            JSONObject value = (JSONObject) invoicePictures.get(i);
                            String piclink = value.getString("attachid");
                            invoicePicturesArr.add(Config.QINIU_BASE_URL + piclink);
                        }
                        invoiceAdapter.notifyDataSetChanged();

                        // 预计交车记录
                        JSONArray listCaseRepair = datas.getJSONArray("listCaseRepair");
                        for (int i = 0; i < listCaseRepair.length(); i++) {
                            Gson gson = new Gson();
                            CaseRepair caseRepair = gson.fromJson(listCaseRepair.get(i).toString(), CaseRepair.class);
                            planHandCarArr.add(caseRepair);
                        }
                        // 维修完毕记录
                        JSONArray listCaseRepairFinish = datas.getJSONArray("listCaseRepairFinish");
                        for (int i = 0; i < listCaseRepairFinish.length(); i++) {
                            Gson gson = new Gson();
                            CaseRepairFinish caseRepairFinish = gson.fromJson(listCaseRepairFinish.get(i).toString(), CaseRepairFinish.class);
                            finishRepairArr.add(caseRepairFinish);
                        }
                        // 如果预计交车记录或维修完毕记录有一条数据大于1 的，维修进度区域可点击，右边箭头显示；否则不可点击，不显示
                        if (planHandCarArr.size() > 1 || finishRepairArr.size() > 1) {
                            ll_repair.setClickable(true);
                            rb_report_detail.setVisibility(View.VISIBLE);
                        } else {
                            ll_repair.setClickable(false);
                            rb_report_detail.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(RepairDetail.this, "获取数据失败", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RepairDetail.this, "获取数据失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
