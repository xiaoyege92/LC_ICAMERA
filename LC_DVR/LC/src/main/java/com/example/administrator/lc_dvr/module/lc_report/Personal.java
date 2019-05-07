package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Config;
import com.bumptech.glide.Glide;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.ListDataSave;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.TimeUtils;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.AppCaseData;
import com.example.administrator.lc_dvr.module.LocalVideoFileActivity;
import com.example.administrator.lc_dvr.module.lc_help.UnitDetail;
import com.example.administrator.lc_dvr.module.login_registration.Landing;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.makeramen.roundedimageview.RoundedImageView;
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
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/11/19
 *   desc   :
 *  version :
 * </pre>
 */
public class Personal extends BaseActivity implements View.OnClickListener {

    private RadioButton rb_back; // 返回

    private RoundedImageView riv_person_icon; // 用户头像
    private LinearLayout ll_user_info; // 用户信息
    private TextView tv_user_name; // 用户名
    private TextView tv_nick_name; // 用户昵称
    private TextView tv_user_remark; // 用户备注

    private RelativeLayout rl_my_service; // 服务
    private RelativeLayout rl_my_local_file; // 文件
    private RelativeLayout rl_my_version; // 版本
    private ImageView iv_red_circler; // 升级小红点
    private Button btn_logout; // 退出当前用户

    private boolean isUpdate;// 是否要升级
    private String[] mStringBts;
    private NormalDialog dialog;

    private ListDataSave dataSave;
    private String headUrl; // 头像URL
    private String baseUrl;
    private String qiniuToken;

    private String unitCode; //单位unitcode
    private String insCode; // 保险公司insCode

    @Override
    protected void onResume() {
        super.onResume();

        tv_user_name.setText(PreferenceUtil.getString("user_mobile", ""));
        tv_nick_name.setText(AppCaseData.nick_name);
        tv_user_remark.setText(AppCaseData.user_remark);

        if (null != AppCaseData.headURL && !"".equals(AppCaseData.headURL)
                && !"null".equals(AppCaseData.headURL)
                && !(Config.QINIU_BASE_URL + "null").equals(AppCaseData.headURL)) {

            if (AppCaseData.headURL.contains("http")) {
                Glide.with(Personal.this).load(AppCaseData.headURL).into(riv_person_icon);
            } else {
                riv_person_icon.setImageBitmap(BitmapFactory.decodeFile(AppCaseData.headURL));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getQiniuToken();
    }

    @Override
    protected int setViewId() {
        return R.layout.personal_activity;
    }

    @Override
    protected void findView() {
        rb_back = (RadioButton) findViewById(R.id.rb_back);

        riv_person_icon = (RoundedImageView) findViewById(R.id.riv_person_icon);
        ll_user_info = (LinearLayout) findViewById(R.id.ll_user_info);
        tv_user_name = (TextView) findViewById(R.id.tv_user_name);
        tv_nick_name = (TextView) findViewById(R.id.tv_nick_name);
        tv_user_remark = (TextView) findViewById(R.id.tv_user_remark);

        rl_my_service = (RelativeLayout) findViewById(R.id.rl_my_service);
        rl_my_local_file = (RelativeLayout) findViewById(R.id.rl_my_local_file);
        rl_my_version = (RelativeLayout) findViewById(R.id.rl_my_version);
        iv_red_circler = (ImageView) findViewById(R.id.iv_red_circler);
        btn_logout = (Button) findViewById(R.id.btn_logout);
    }

    @Override
    protected void init() {
        // 初始化数据
        Intent intent = getIntent();
        isUpdate = intent.getBooleanExtra("isUpdate", false);
        if (isUpdate) {
            iv_red_circler.setVisibility(View.VISIBLE);
        } else {
            iv_red_circler.setVisibility(View.GONE);
        }

        // 构建dialog实例
        dialog = new NormalDialog(Personal.this);
        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);

        //用来保存list到本地
        dataSave = new ListDataSave(Personal.this, "baiyu");
    }

    @Override
    protected void initEvents() {
        rb_back.setOnClickListener(this);
        ll_user_info.setOnClickListener(this);
        rl_my_service.setOnClickListener(this);
        rl_my_local_file.setOnClickListener(this);
        rl_my_version.setOnClickListener(this);
        riv_person_icon.setOnClickListener(this);
        btn_logout.setOnClickListener(this);
    }

    @Override
    protected void loadData() {
        //获得服务器的所有字典
        getConfigs();
        // 获取个人信息
        getPersonalInformation();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_back:// 返回
                finish();
                break;
            case R.id.ll_user_info: // 弹出修改昵称和备注对话框

                Intent userInfoIntent = new Intent(Personal.this, UserInfoActivity.class);
                startActivity(userInfoIntent);
                break;
            case R.id.rl_my_service: // 服务
                if (TimeUtils.isFastClick()) {
                    return;
                }
                Intent serviceIntent = new Intent(Personal.this, UnitDetail.class);
                startActivity(serviceIntent);
                break;
            case R.id.rl_my_local_file: // 本地文件
                if (TimeUtils.isFastClick()) {
                    return;
                }
                Intent fileIntent = new Intent(Personal.this, LocalVideoFileActivity.class);
                startActivity(fileIntent);
                break;
            case R.id.rl_my_version: // 版本更新
                if (TimeUtils.isFastClick()) {
                    return;
                }
                Intent versionIntent = new Intent(Personal.this, Upgrade2.class);
                startActivity(versionIntent);
                break;
            case R.id.btn_logout:
                // 显示提示框
                showTipDialog(getString(R.string.personal_info_exit_sure));

                dialog.setOnBtnClickL(
                        new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                                openLanding();
                            }
                        },
                        new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                            }
                        });

                break;
            case R.id.riv_person_icon:
                Intent iconIntent = new Intent(Personal.this, LookIconImage.class);
                iconIntent.putExtra("headUrl", AppCaseData.headURL);
                startActivity(iconIntent);
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
                .btnTextColor(new int[]{ContextCompat.getColor(Personal.this, R.color.primary), ContextCompat.getColor(Personal.this, R.color.alphablack)})
                .show();
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 获得服务器的所有字典
     */
    private void getConfigs() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getConfigs(NetUtils.getHeaders());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    JSONObject datas = jsonObject.getJSONObject("datas");
                    JSONArray dicts = datas.getJSONArray("dicts");
                    //临时保存configs的数组
                    Map<String, String> configsArr = new HashMap();
                    for (int i = 0; i < dicts.length(); i++) {
                        JSONObject value = (JSONObject) dicts.get(i);
                        configsArr.put(value.getString("keyname"), value.getString("keyvalue"));
                    }
                    // 数据字典保存到APPlication的静态变量中
                    VLCApplication.configsDictionary.clear();
                    VLCApplication.configsDictionary.putAll(configsArr);
                    //把configsArr保存到本地
                    List<Map<String, String>> configsList = new ArrayList();
                    configsList.add(configsArr);
                    dataSave.setDataList("configsList", configsList);

                    ToastUtils.showNomalShortToast(Personal.this, "数据字典已更新至最新版本");

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
     * 获得个人信息
     */
    private void getPersonalInformation() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getPersonalInfo(NetUtils.getHeaders());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    //赋值给用户名
                    tv_user_name.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("loginid")));
                    // 用户昵称
                    tv_nick_name.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("username")));
                    AppCaseData.nick_name = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("username"));
                    // 用户备注
                    tv_user_remark.setText(Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("demo")));
                    AppCaseData.user_remark = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("demo"));

                    headUrl = jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("logoid");
                    if (!"".equals(headUrl) && null != headUrl && !"null".equals(headUrl)) {
                        Glide.with(Personal.this).load(Config.QINIU_BASE_URL + headUrl).into(riv_person_icon);
                        AppCaseData.headURL = Config.QINIU_BASE_URL + headUrl;
                    } else {
                        riv_person_icon.setImageResource(R.mipmap.person_icon);
                    }
                    // 单位类型
                    int unitkind = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getInt("unitkind");
                    //获得单位编号
                    if (jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitcode") != null) {
                        unitCode = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitcode");
                    }
                    if (unitkind == 1) {
                        try {
                            //获得单位编号
                            if (jsonObject.getJSONObject("datas").getJSONObject("insinfo").getString("unitcode") != null) {
                                insCode = jsonObject.getJSONObject("datas").getJSONObject("insinfo").getString("unitcode");
                            }
                        } catch (Exception e) {

                        }
                    } else {
                        insCode = unitCode;
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
     * 打开登录页面
     */
    private void openLanding() {
        //移除所有的activity
        VLCApplication.removeALLActivity();
        //清空授权码
        PreferenceUtil.commitString("tokenid", "");
        //清空用户的手机号码
        PreferenceUtil.commitString("user_mobile", "");
        // 清空车主code
        PreferenceUtil.commitString("personcode", "");
        //跳转到登录界面
        Intent intent = new Intent(Personal.this, Landing.class);
        intent.putExtra("selectIndex","lc_report");
        startActivity(intent);
    }

    /**
     * 获取七牛token
     */
    private void getQiniuToken() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getQiniuToken(NetUtils.getHeaders(), 1);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if (200 == jsonObject.getInt("code")) {
                        baseUrl = jsonObject.getJSONObject("data").getString("baseUrl");
                        qiniuToken = jsonObject.getJSONObject("data").getString("token");
                        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("fileName");

                        String fileName = jsonArray.get(0).toString();
                        upLoadHead(fileName);
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
     * 头像照片上传到七牛云
     *
     * @param fileName
     */
    private void upLoadHead(final String fileName) {
        //头像照片
        if (null != AppCaseData.headURL && !"".equals(AppCaseData.headURL)
                && !"null".equals(AppCaseData.headURL)
                && !(Config.QINIU_BASE_URL + "null").equals(AppCaseData.headURL)) {

            File headFile = new File(AppCaseData.headURL);
            if (headFile.exists()) {
                VLCApplication.uploadManager.put(headFile, fileName, qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
                                    try {
                                        String attachid = res.getString("key");
                                        // 获取图片的url
                                        headUrl = Config.QINIU_BASE_URL + attachid;

                                        //上传完图片头像，就删除
                                        File headFile = new File(AppCaseData.headURL);
                                        if (headFile.exists()) {
                                            headFile.delete();
                                        }
                                        AppCaseData.headURL = headUrl;
                                        // 将信息上传到服务器
                                        uploadPersonInfo(fileName);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                }
                            }
                        }, null);
            } else {
                uploadPersonInfo(AppCaseData.headURL.replace(Config.QINIU_BASE_URL, ""));
            }
        } else {
            uploadPersonInfo("");
        }
    }

    /**
     * 将内容信息上传到服务器
     */
    private void uploadPersonInfo(String url) {
        Map<String, String> map = new HashMap<>();
        // 单位unitcode
        map.put("unitcode", unitCode);
        //保险公司编号
        map.put("inscode", insCode);
        // 用户备注
        map.put("demo", tv_user_remark.getText().toString());
        // 用户头像
        map.put("logoid", url);// 用户头像
        // 用户昵称
        map.put("username", tv_nick_name.getText().toString());

        Call<ResponseBody> call = RetrofitManager.getInstance().create().appUpdatePerson(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        AppCaseData.nick_name = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("username"));
                        AppCaseData.user_remark = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("demo"));
                        String headUrl = jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("logoid");
                        if (!"".equals(headUrl) && null != headUrl && !"null".equals(headUrl)) {
                            AppCaseData.headURL = Config.QINIU_BASE_URL + headUrl;
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

            }
        });
    }
}
