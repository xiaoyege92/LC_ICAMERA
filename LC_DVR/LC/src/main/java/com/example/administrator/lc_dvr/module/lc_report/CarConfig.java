package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;

import com.Config;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.bumptech.glide.Glide;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.bean.Car;
import com.example.administrator.lc_dvr.bean.InsCompany;
import com.example.administrator.lc_dvr.bean.MachineCode;
import com.example.administrator.lc_dvr.bean.ServicePerson;
import com.example.administrator.lc_dvr.bean.Unit;
import com.example.administrator.lc_dvr.common.adapter.SwipeMenuListViewAdapter;
import com.example.administrator.lc_dvr.common.adapter.ViewHolder;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.TimeUtils;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.google.gson.Gson;

import org.json.JSONArray;
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

/**
 * 车主配置页面
 */
public class CarConfig extends BaseActivity implements View.OnClickListener {

    private RadioButton rb_back; // 返回

    private SwipeMenuListView rv_config; // 配置列表
    private Button btn_add_config; // 新增配置

    private static List<Car>  carList; // 车辆配置
    private static List<Unit> unitListArr; // 单位名称
    private static List<ServicePerson> servicePersonList; // 事故专员列表
    private static List<InsCompany> insCompanyList; // 投保公司

    private static Map<Integer, ArrayList<MachineCode>> machineMap; // 每个配置对应一个串码列表

    private SwipeMenuListViewAdapter carAdpater;

    private NormalDialog dialog;
    private String[] mStringBts;


    @Override
    protected void onResume() {
        super.onResume();
        findMyCar();
    }

    @Override
    protected int setViewId() {
        return R.layout.car_config_activity;
    }

    @Override
    protected void findView() {
        rb_back = (RadioButton) findViewById(R.id.rb_back);
        rv_config = (SwipeMenuListView) findViewById(R.id.rv_config);
        btn_add_config = (Button) findViewById(R.id.btn_add_config);
    }

    @Override
    protected void init() {

        dialog = new NormalDialog(CarConfig.this);
        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);

        if(carList == null) {
            carList = new ArrayList<>();
        }
        if(unitListArr == null) {
            unitListArr = new ArrayList<>();
        }
        if(machineMap == null) {
            machineMap = new HashMap<Integer, ArrayList<MachineCode>>();
        }
        if(servicePersonList == null) {
            servicePersonList = new ArrayList<>();
        }
        if(insCompanyList == null) {
            insCompanyList = new ArrayList<>();
        }

        carAdpater = new SwipeMenuListViewAdapter(CarConfig.this, carList, R.layout.car_config_item) {
            @Override
            public void convert(ViewHolder helper, final int position, Object item) {
                // 车牌号码
                helper.setText(R.id.tv_plate_number, carList.get(position).getCarnumber());
                // 厂牌车型
                helper.setText(R.id.tv_car_type, carList.get(position).getCartype());
                // 单位名称
                helper.setText(R.id.tv_unit_name, unitListArr.get(position).getUnitname());
                // 默认配置
                // logo
                ImageView imageView = helper.getView(R.id.iv_icon);
                if (insCompanyList.get(position).getLogo() != null && !"".equals(insCompanyList.get(position).getLogo())) {
                    Glide.with(CarConfig.this).load(Config.QINIU_BASE_URL + insCompanyList.get(position).getLogo()).into(imageView);
                }
                final Switch swtich_default_config = helper.getView(R.id.swtich_default_config);
                if (1 == carList.get(position).getIsDefault()) { // 如果是默认配置就 设置不可点击
                    swtich_default_config.setChecked(true);
                    swtich_default_config.setEnabled(false);
                } else {
                    swtich_default_config.setChecked(false);
                }
                // 更改默认配置
                swtich_default_config.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTipDialog("是否要更改您的默认配置？");
                        dialog.setOnBtnClickL(
                                new OnBtnClickL() {
                                    @Override
                                    public void onBtnClick() {
                                        dialog.dismiss();
                                        // 更改默认配置
                                        changeDefault(carList.get(position).getId());
                                    }
                                },
                                new OnBtnClickL() {
                                    @Override
                                    public void onBtnClick() {
                                        swtich_default_config.setChecked(false);
                                        dialog.dismiss();
                                    }
                                });
                    }
                });
                // 点击中的项
                helper.getConvertView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TimeUtils.isFastClick()) {
                            return;
                        }
                        Intent intent = new Intent(CarConfig.this, CarConfigDetail.class);
                        intent.putExtra("CarDetail", carList.get(position));
                        intent.putExtra("UnitDetail", unitListArr.get(position));
                        intent.putExtra("ServicePersonDetail", servicePersonList.get(position));
                        intent.putExtra("InsCompanyListDetail", insCompanyList.get(position));
                        intent.putParcelableArrayListExtra("MachineCodeList", machineMap.get(position));
                        startActivity(intent);
                    }
                });
            }
        };

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                switch (menu.getViewType()) {
                    case 0:
                        break;
                    case 1:
                        // 创建“删除”项
                        SwipeMenuItem deleteItem = new SwipeMenuItem(CarConfig.this);
                        deleteItem.setBackground(new ColorDrawable(Color.RED));
                        deleteItem.setWidth(dp2px(70));
                        deleteItem.setTitle(R.string.lc_delete);
                        deleteItem.setTitleSize(18);
                        deleteItem.setTitleColor(Color.WHITE);
                        // 将创建的菜单项添加进菜单中
                        menu.addMenuItem(deleteItem);
                        break;
                    default:
                        break;
                }
            }
        };
        rv_config.setMenuCreator(creator);
        rv_config.setAdapter(carAdpater);
        carAdpater.notifyDataSetChanged();
    }

    @Override
    protected void initEvents() {
        rb_back.setOnClickListener(this);
        btn_add_config.setOnClickListener(this);

        rv_config.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        showTipDialog("您确认要删除该条配置信息吗？");
                        dialog.setOnBtnClickL(
                                new OnBtnClickL() {
                                    @Override
                                    public void onBtnClick() {
                                        dialog.dismiss();
                                        //删除配置
                                        deleteCar(carList.get(position).getId(), position);
                                    }
                                },
                                new OnBtnClickL() {
                                    @Override
                                    public void onBtnClick() {
                                        dialog.dismiss();
                                    }
                                });
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void loadData() {

    }

    /**
     * 将dp转换为px
     *
     * @param value
     * @return
     */
    private int dp2px(int value) {
        // 第一个参数为我们待转的数据的单位，此处为 dp（dip）
        // 第二个参数为我们待转的数据的值的大小
        // 第三个参数为此次转换使用的显示量度（Metrics），它提供屏幕显示密度（density）和缩放信息
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    /**
     * 获取车主配置列表
     */
    private void findMyCar() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getCarList(NetUtils.getHeaders(), PreferenceUtil.getString("personcode", ""));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    carList.clear();
                    unitListArr.clear();
                    servicePersonList.clear();
                    insCompanyList.clear();
                    machineMap.clear();

                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        JSONObject datas = jsonObject.getJSONObject("datas");
                        JSONArray msgs = datas.getJSONArray("msgs");

                        Gson gson = new Gson();
                        for (int i = 0; i < msgs.length(); i++) {
                            Car car = gson.fromJson(msgs.getJSONObject(i).getJSONObject("car").toString(), Car.class);
                            carList.add(car);
                            Unit unit = gson.fromJson(msgs.getJSONObject(i).getJSONObject("unit").toString(), Unit.class);
                            unitListArr.add(unit);
                            ServicePerson servicePerson = gson.fromJson(msgs.getJSONObject(i).getJSONObject("serviceperson").toString(), ServicePerson.class);
                            servicePersonList.add(servicePerson);
                            try {
                                InsCompany insCompany = gson.fromJson(msgs.getJSONObject(i).getJSONObject("ins").toString(), InsCompany.class);
                                insCompanyList.add(insCompany);
                            } catch (Exception e) {
                                // 如果投保公司解析错误跳过,增加一条空数据
                                insCompanyList.add(new InsCompany());
                            }
                            JSONArray jsonArray = msgs.getJSONObject(i).getJSONArray("machine");
                            ArrayList<MachineCode> machineCodeList = new ArrayList<>();
                            if (jsonArray.length() != 0) {
                                for (int j = 0; j < jsonArray.length(); j++) {
                                    MachineCode machineCode = gson.fromJson(jsonArray.get(j).toString(), MachineCode.class);
                                    machineCodeList.add(machineCode);
                                }
                            }
                            machineMap.put(i, machineCodeList);
                        }
                        carAdpater.notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                ToastUtils.showNomalShortToast(CarConfig.this, "数据加载异常，请稍后再试");
            }
        });

    }

    /**
     * 改变默认配置
     *
     * @param id
     */
    private void changeDefault(int id) {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().changeDefault(NetUtils.getHeaders(), id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (jsonObject.getBoolean(Config.SUCCESS)) {

                        findMyCar();// 重新加载数据
                        getUnitInfo(PreferenceUtil.getString("user_mobile", ""));
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
                        String unitName = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitname");
                        String unitIconUrl = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("iconurl");
                        String address = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("address");
                        String serviceTime = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("servicetime");
                        String helpTip = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("helptip");
                        String helpURL = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("helpurl");

                        VLCApplication.unitName = unitName;
                        VLCApplication.unitIconUrl = unitIconUrl;
                        VLCApplication.unitAddress = address;
                        VLCApplication.unitServiceTime = serviceTime;
                        VLCApplication.unitHelpTip = helpTip;
                        VLCApplication.unitHelpURL = helpURL;
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
     * 删除默认配置表中对应的ID
     *
     * @param id
     */
    private void deleteCar(int id, final int position) {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().deleteCar(NetUtils.getHeaders(), id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        carList.remove(position);
                        carAdpater.notifyDataSetChanged();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_back:
                finish();
                break;
            case R.id.btn_add_config:

                Intent intent = new Intent(CarConfig.this, CarConfigDetail.class);
                intent.putExtra("isNewCar", true);
                startActivity(intent);
                break;
            default:
                break;
        }
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
                .btnTextColor(new int[]{ContextCompat.getColor(CarConfig.this, R.color.primary), ContextCompat.getColor(CarConfig.this, R.color.alphablack)})
                .show();
        dialog.setCanceledOnTouchOutside(false);
    }

}
