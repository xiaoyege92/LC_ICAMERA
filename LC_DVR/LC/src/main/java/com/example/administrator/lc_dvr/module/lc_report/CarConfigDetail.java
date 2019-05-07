package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Config;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.bean.Car;
import com.example.administrator.lc_dvr.bean.InsCompany;
import com.example.administrator.lc_dvr.bean.MachineCode;
import com.example.administrator.lc_dvr.bean.ServicePerson;
import com.example.administrator.lc_dvr.bean.Unit;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.observer.ObserverManager;
import com.example.administrator.lc_dvr.module.AppCaseData;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import io.vov.vitamio.utils.Log;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarConfigDetail extends BaseActivity {

    private FragmentManager v4FragmentManager;
    private RadioButton rb_back; // 返回
    private Button btn_upload;

    private TextView tv_basic_info; // 基本信息
    private View basic_info_selector;

    private TextView tv_ins_info; // 投保信息
    private View ins_info_selector;

    private TextView tv_machine_code; // 投保信息
    private View machine_code_selector;

    private RelativeLayout rl_basic_info; // 基本信息
    private HashMap<Object, Object> fragmentMap; // 存放创建的fragment
    private BaseFragment lastFragment; // 正在显示的fragment

    private String qiniuToken;
    private ArrayList<String> arrPayment;

    private KProgressHUD kProgressHUD;
    public static boolean isNewCar;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fragmentMap != null) {  // 清空存放的fragment
            fragmentMap.clear();
            fragmentMap = null;
        }
    }

    @Override
    protected int setViewId() {
        return R.layout.activity_car_config_detail;
    }

    @Override
    protected void findView() {
        rb_back = (RadioButton) findViewById(R.id.rb_back);
        btn_upload = (Button) findViewById(R.id.btn_upload);
        tv_basic_info = (TextView) findViewById(R.id.tv_basic_info);
        basic_info_selector = findViewById(R.id.basic_info_selector);
        tv_ins_info = (TextView) findViewById(R.id.tv_ins_info);
        ins_info_selector = findViewById(R.id.ins_info_selector);
        tv_machine_code = (TextView) findViewById(R.id.tv_machine_code);
        machine_code_selector = findViewById(R.id.machine_code_selector);
        rl_basic_info = (RelativeLayout) findViewById(R.id.rl_basic_info);
    }

    @Override
    protected void init() {
        v4FragmentManager = getSupportFragmentManager();
        // 初始化数据
        fragmentMap = new HashMap<>();
        arrPayment = new ArrayList<>();

        rl_basic_info.performClick();
        // 先清空上次打开的内容
        AppCaseData.carDetail = new Car();
        AppCaseData.unitDetail = new Unit();
        AppCaseData.servicePersonDetail = new ServicePerson();
        AppCaseData.insCompanyDetail = new InsCompany();
        // 再从传递过来的页面中获取
        Intent intent = getIntent();
        AppCaseData.carDetail = intent.getParcelableExtra("CarDetail");
        AppCaseData.unitDetail = intent.getParcelableExtra("UnitDetail");
        AppCaseData.servicePersonDetail = intent.getParcelableExtra("ServicePersonDetail");
        AppCaseData.insCompanyDetail = intent.getParcelableExtra("InsCompanyListDetail");
        AppCaseData.machineCodeList = intent.getParcelableArrayListExtra("MachineCodeList");

        if(null == AppCaseData.unitDetail) {
            AppCaseData.unitDetail = new Unit();
        }
        if(null == AppCaseData.carDetail) {
            AppCaseData.carDetail = new Car();
        }
        if(null == AppCaseData.insCompanyDetail) {
            AppCaseData.insCompanyDetail = new InsCompany();
        }
        if(null == AppCaseData.servicePersonDetail) {
            AppCaseData.servicePersonDetail = new ServicePerson();
        }
        if(null == AppCaseData.machineCodeList) {
            AppCaseData.machineCodeList = new ArrayList<>();
        }
        isNewCar = getIntent().getBooleanExtra("isNewCar",false);
    }

    @Override
    protected void initEvents() {
        rb_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObserverManager.getInstance().notifyObserver("upload");

                if (null == AppCaseData.carDetail.getServicecode() || "".equals(AppCaseData.carDetail.getServicecode())) {
                    ToastUtils.showNomalShortToast(CarConfigDetail.this, "请先选择单位快捷码");
                    return;
                }
                if(!NetUtils.isNetworkConnected(CarConfigDetail.this)) {
                    ToastUtils.showNomalShortToast(CarConfigDetail.this,getString(R.string.network_off));
                    return;
                }
                showProgress("拼命上传中...");

                getQiniuToken();
            }
        });
    }

    @Override
    protected void loadData() {
    }

    /**
     * 获取七牛token
     */
    private void getQiniuToken() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getQiniuToken(NetUtils.getHeaders(),6);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if (200 == jsonObject.getInt("code")) {
                        qiniuToken = jsonObject.getJSONObject("data").getString("token");
                        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("fileName");

                        arrPayment.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            arrPayment.add(jsonArray.get(i).toString());
                        }

                        upLoadIdOpposite(arrPayment.get(0));
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
     * 上传身份证反面照片
     *
     * @param fileName
     */
    private void upLoadIdOpposite(final String fileName) {

        if(null == AppCaseData.carDetail.getLicenseB() || "".equals(AppCaseData.carDetail.getLicenseB())) {
            upLoadIdPositive(arrPayment.get(1));
            return;
        }
        //上传身份证反面照片
        final File IDOppositeImageFile = new File(AppCaseData.carDetail.getLicenseB());
        if (IDOppositeImageFile.exists()) {
            VLCApplication.uploadManager.put(IDOppositeImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                IDOppositeImageFile.delete();
                                AppCaseData.carDetail.setLicenseB(fileName);
                                upLoadIdPositive(arrPayment.get(1));

                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
            upLoadIdPositive(arrPayment.get(1));
        }
    }

    /**
     * 上传身份证正面照片
     *
     * @param fileName
     */
    private void upLoadIdPositive(final String fileName) {
        if(null == AppCaseData.carDetail.getLicenseA() || "".equals(AppCaseData.carDetail.getLicenseA())) {
            upLoadBank(arrPayment.get(2));
            return;
        }
        //上传身份证正面照片
       final File IDPositiveImageFile = new File(AppCaseData.carDetail.getLicenseA());
        if (IDPositiveImageFile.exists()) {

            VLCApplication.uploadManager.put(IDPositiveImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                IDPositiveImageFile.delete();
                                AppCaseData.carDetail.setLicenseA(fileName);
                                upLoadBank(arrPayment.get(2));
                            } else {
                                Log.i("qiniu", "Upload Fail");
                            }
                        }
                    }, null);
        } else {
            upLoadBank(arrPayment.get(2));
        }
    }

    /**
     * 上传银行卡照片
     *
     * @param fileName
     */
    private void upLoadBank(final String fileName) {
        if(null == AppCaseData.carDetail.getCard() || "".equals(AppCaseData.carDetail.getCard())) {
            upLoadDrivingLicense(arrPayment.get(3));
            return;
        }
        //上传银行卡照片
        final File bankImageFile = new File(AppCaseData.carDetail.getCard());
        if (bankImageFile.exists()) {
            VLCApplication.uploadManager.put(bankImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                bankImageFile.delete();
                                AppCaseData.carDetail.setCard(fileName);
                                upLoadDrivingLicense(arrPayment.get(3));

                            } else {
                                Log.i("qiniu", "Upload Fail");
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
            upLoadDrivingLicense(arrPayment.get(3));
        }
    }

    /**
     * 上传行驶证照片
     *
     * @param fileName
     */
    private void upLoadDrivingLicense(final String fileName) {
        if(null == AppCaseData.carDetail.getPermit() || "".equals(AppCaseData.carDetail.getPermit())) {
            upLoadDriverLicense(arrPayment.get(4));
            return;
        }
        //上传行驶证照片
        final File drivingLicenseImageFile = new File(AppCaseData.carDetail.getPermit());
        if (drivingLicenseImageFile.exists()) {
            VLCApplication.uploadManager.put(drivingLicenseImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                drivingLicenseImageFile.delete();
                                AppCaseData.carDetail.setPermit(fileName);
                                upLoadDriverLicense(arrPayment.get(4));

                            } else {
                                Log.i("qiniu", "Upload Fail");
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
            upLoadDriverLicense(arrPayment.get(4));
        }
    }

    /**
     * 上传驾驶证照片
     *
     * @param fileName
     */
    private void upLoadDriverLicense(final String fileName) {
        if(null == AppCaseData.carDetail.getDrivelicense() || "".equals(AppCaseData.carDetail.getDrivelicense())) {
            upLoadFrameNumber(arrPayment.get(5));
            return;
        }
        //上传驾驶证照片
        final File driverLicenseImageFile = new File(AppCaseData.carDetail.getDrivelicense());
        if (driverLicenseImageFile.exists()) {
            VLCApplication.uploadManager.put(driverLicenseImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                driverLicenseImageFile.delete();
                                AppCaseData.carDetail.setDrivelicense(fileName);
                                upLoadFrameNumber(arrPayment.get(5));
                            } else {
                                Log.i("qiniu", "Upload Fail");
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
            upLoadFrameNumber(arrPayment.get(5));
        }
    }

    /**
     * 上传车架号照片
     *
     * @param fileName
     */
    private void upLoadFrameNumber(final String fileName) {
        if(null == AppCaseData.carDetail.getCarframe() || "".equals(AppCaseData.carDetail.getCarframe())) {
            saveCar();
            return;
        }
        //上传车架号照片
        final File frameNumberImageFile = new File(AppCaseData.carDetail.getCarframe());
        if (frameNumberImageFile.exists()) {
            VLCApplication.uploadManager.put(frameNumberImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                frameNumberImageFile.delete();
                                AppCaseData.carDetail.setCarframe(fileName);
                                saveCar();
                            } else {
                                Log.i("qiniu", "Upload Fail");
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
            saveCar();
        }
    }

    /**
     * 点击选择器时的响应  选择显示哪个Fragment
     *
     * @param v
     */
    public void onlineSurveySelector(View v) {

        // 先是更改上面的变化，再更改下面的Fragment显示
        switch (v.getId()) {
            case R.id.rl_basic_info: // 基本信息
                tv_basic_info.setTextColor(ContextCompat.getColor(CarConfigDetail.this, R.color.orange));
                basic_info_selector.setVisibility(View.VISIBLE);

                tv_ins_info.setTextColor(ContextCompat.getColor(CarConfigDetail.this, R.color.normal_black));
                ins_info_selector.setVisibility(View.GONE);

                tv_machine_code.setTextColor(ContextCompat.getColor(CarConfigDetail.this, R.color.normal_black));
                machine_code_selector.setVisibility(View.GONE);
                break;
            case R.id.rl_ins_info: // 投保信息
                tv_basic_info.setTextColor(ContextCompat.getColor(CarConfigDetail.this, R.color.normal_black));
                basic_info_selector.setVisibility(View.GONE);

                tv_ins_info.setTextColor(ContextCompat.getColor(CarConfigDetail.this, R.color.orange));
                ins_info_selector.setVisibility(View.VISIBLE);

                tv_machine_code.setTextColor(ContextCompat.getColor(CarConfigDetail.this, R.color.normal_black));
                machine_code_selector.setVisibility(View.GONE);
                break;
            case R.id.rl_machine_code: // 机身串码
                tv_basic_info.setTextColor(ContextCompat.getColor(CarConfigDetail.this, R.color.normal_black));
                basic_info_selector.setVisibility(View.GONE);

                tv_ins_info.setTextColor(ContextCompat.getColor(CarConfigDetail.this, R.color.normal_black));
                ins_info_selector.setVisibility(View.GONE);

                tv_machine_code.setTextColor(ContextCompat.getColor(CarConfigDetail.this, R.color.orange));
                machine_code_selector.setVisibility(View.VISIBLE);
                break;
        }

        BaseFragment show = null;
        FragmentTransaction v4Transaction = v4FragmentManager.beginTransaction();

        if (fragmentMap.containsKey(v.getId())) {
            if (lastFragment != null) {
                v4Transaction.hide(lastFragment);
            }
            show = (BaseFragment) fragmentMap.get(v.getId());
            lastFragment = show;
            v4Transaction.show(show);
        } else {
            switch (v.getId()) {
                case R.id.rl_basic_info://此处也可以和id比较的
                    show = new BasicInfoFragment();
                    break;
                case R.id.rl_ins_info:
                    show = new InsInfoFragment();
                    break;
                case R.id.rl_machine_code:
                    show = new MachineCodeFragment();
                    break;
            }
            v4Transaction.add(R.id.config_content, show);
            fragmentMap.put(v.getId(), show);
            if (lastFragment != null) {
                v4Transaction.hide(lastFragment);
            }
            lastFragment = show;
        }
        v4Transaction.commitAllowingStateLoss();
    }

    /**
     * 一键上传配置信息内容
     */
    private void saveCar() {
        AppCaseData.carDetail.setPersoncode(PreferenceUtil.getString("personcode", ""));

        Call<ResponseBody> call = RetrofitManager.getInstance().create().saveCar(NetUtils.getHeaders(),AppCaseData.carDetail);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        ToastUtils.showNomalShortToast(CarConfigDetail.this, "上传成功");
                        uploadMachineCode(jsonObject.getString("msg"));
                        finish();

                    } else {
                        ToastUtils.showNomalShortToast(CarConfigDetail.this, "上传失败，请稍后再试");
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
     * 上传机身串码
     */
    private void uploadMachineCode(String carid) {
        MachineCode machineCode = new MachineCode();
        machineCode.setPersoncode(PreferenceUtil.getString("personcode",""));
        machineCode.setCarid(carid);
        machineCode.setMachinecode(AppCaseData.machineCode.getMachinecode());
        machineCode.setStatus(AppCaseData.machineCode.getStatus());

        Call<ResponseBody> call = RetrofitManager.getInstance().create().saveMachineCode(NetUtils.getHeaders(),machineCode);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        ToastUtils.showNomalShortToast(CarConfigDetail.this, "上传成功");
                        finish();
                    } else {
                        ToastUtils.showNomalShortToast(CarConfigDetail.this, "上传失败，请稍后再试");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(kProgressHUD != null){
                    kProgressHUD.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if(kProgressHUD != null){
                    kProgressHUD.dismiss();
                }
            }
        });

//        Map map = new HashMap<>();
//        map.put("personcode ",PreferenceUtil.getString("personcode",""));
//        map.put("carid",carid);
//        map.put("machinecode",AppCaseData.machineCode.getMachinecode());
//        map.put("status",AppCaseData.machineCode.getStatus());
//        JSONObject jsonObject = new JSONObject(map);
//        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.SAVE_MACHINE_CODE, jsonObject, new Listener<JSONObject>() {
//            @Override
//            public void onSuccess(JSONObject jsonObject) {
//                if (kProgressHUD != null) {
//                    kProgressHUD.dismiss();
//                }
//                try {
//                    if (jsonObject.getBoolean(Config.SUCCESS)) {
//                        ToastUtils.showNomalShortToast(CarConfigDetail.this, "上传成功");
//                        finish();
//                    } else {
//                        ToastUtils.showNomalShortToast(CarConfigDetail.this, "上传失败，请稍后再试");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(VolleyError volleyError) {
//                super.onError(volleyError);
//                if (kProgressHUD != null) {
//                    kProgressHUD.dismiss();
//                }
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() {
//                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Accept", "application/json");
//                headers.put("Content-Type", "application/json; charset=UTF-8");
//                //上传授权码
//                headers.put("tokenid", PreferenceUtil.getString("tokenid", null));
//                return headers;
//            }
//        };
//        VLCApplication.queue.add(jsonRequest);
    }

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label) {
        kProgressHUD = KProgressHUD.create(this);
        kProgressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        kProgressHUD.setLabel(label);
        kProgressHUD.setCancellable(true);
        kProgressHUD.setAnimationSpeed(2);
        kProgressHUD.setDimAmount(0.5f);
        kProgressHUD.show();
    }
}
