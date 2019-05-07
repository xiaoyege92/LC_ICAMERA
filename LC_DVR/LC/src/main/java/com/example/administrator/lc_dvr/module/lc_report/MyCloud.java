package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.listener.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.bumptech.glide.Glide;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.module.login_registration.InsuranceCompanyList;
import com.example.administrator.lc_dvr.module.login_registration.Scan;
import com.example.administrator.lc_dvr.module.login_registration.UnitCode;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.makeramen.roundedimageview.RoundedImageView;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/11/19
 *   desc   :
 *  version :
 * </pre>
 */
public class MyCloud extends BaseActivity implements View.OnClickListener {

    private RadioButton rb_back; // 返回键
    private RoundedImageView riv_person_icon; // 用户头像
    private RadioButton rb_upload_cloud; // 上传
    private TextView tv_person_name; // 用户名

    private EditText et_nick_name; // 用户昵称
    private EditText et_person_remark; // 用户备注
    private TextView et_string_code; // 机身串码
    private TextView et_record_date; // 登记日期
    private TextView et_repair_date; // 返修日期
    private ImageView iv_string_code; // 机身串码二维码
    private RelativeLayout rl_unit;
    private TextView et_unit_code; // 单位快捷码
    private TextView et_unit_name; // 单位名称
    private RelativeLayout rl_insurance_company;
    private TextView et_insurance_company; // 投保公司
    private TextView et_insurance_company_phone; // 投保公司电话

    private Bitmap head;// 头像Bitmap
    private static String path = "/sdcard/myHead/";// sd路径

    private String unitCode;
    private int unitkind;
    private KProgressHUD kProgressHUD;
    private String baseUrl;
    private String qiniuToken;
    private String headUrl;

    private NormalDialog dialog;
    private String[] mStringBts;

    @Override
    protected void onResume() {
        super.onResume();
        //获得扫描的结果
        if (PreferenceUtil.getString("scan_result", null) != null) {
            et_string_code.setText(PreferenceUtil.getString("scan_result", ""));
            //设置完之后就清空数据
            PreferenceUtil.commitString("scan_result", null);
        }

        //获得保险公司名字
        if (!"".equals(PreferenceUtil.getString("insuranceCompany", ""))) {
            et_insurance_company.setText(PreferenceUtil.getString("insuranceCompany", ""));
        } else {
        }
        //获得保险公司电话
        if (!"".equals(PreferenceUtil.getString("insuranceCompanyMobile", ""))) {
            et_insurance_company_phone.setText(PreferenceUtil.getString("insuranceCompanyMobile", ""));
            PreferenceUtil.commitString("insuranceCompanyMobile", "");
        } else {
        }
        // 单位名称
        if (!"".equals(PreferenceUtil.getString("unitName", ""))) {
            et_unit_name.setText(PreferenceUtil.getString("unitName", ""));
        } else {

        }
        // 单位类型，如果为保险公司则不可点击，如果是维修单位就可点击
        if (2 == PreferenceUtil.getInt("unitkind", 0)) {
            rl_insurance_company.setClickable(false);
        } else if (1 == PreferenceUtil.getInt("unitkind", 0)) {
            rl_insurance_company.setClickable(true);
        }
        // 单位ID
        if (!"".equals(PreferenceUtil.getString("unitcode", ""))) {
            unitCode = PreferenceUtil.getString("unitcode", "");
            et_unit_code.setText(PreferenceUtil.getString("unitCodeShortCude", ""));
        }
    }

    @Override
    protected int setViewId() {
        return R.layout.my_cloud_activity;
    }

    @Override
    protected void findView() {
        rb_back = (RadioButton) findViewById(R.id.rb_back);
        riv_person_icon = (RoundedImageView) findViewById(R.id.riv_person_icon);
        rb_upload_cloud = (RadioButton) findViewById(R.id.rb_upload_cloud);
        tv_person_name = (TextView) findViewById(R.id.tv_person_name);
        et_nick_name = (EditText) findViewById(R.id.et_nick_name);
        et_person_remark = (EditText) findViewById(R.id.et_person_remark);
        et_string_code = (TextView) findViewById(R.id.et_string_code);
        et_record_date = (TextView) findViewById(R.id.et_record_date);
        et_repair_date = (TextView) findViewById(R.id.et_repair_date);
        iv_string_code = (ImageView) findViewById(R.id.iv_string_code);
        et_unit_code = (TextView) findViewById(R.id.et_unit_code);
        et_unit_name = (TextView) findViewById(R.id.et_unit_name);
        et_insurance_company = (TextView) findViewById(R.id.et_insurance_company);
        et_insurance_company_phone = (TextView) findViewById(R.id.et_insurance_company_phone);
        rl_unit = (RelativeLayout) findViewById(R.id.rl_unit);
        rl_insurance_company = (RelativeLayout) findViewById(R.id.rl_insurance_company);
    }

    @Override
    protected void init() {
        dialog = new NormalDialog(MyCloud.this);
        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);
    }

    @Override
    protected void initEvents() {
        rb_back.setOnClickListener(this);
        iv_string_code.setOnClickListener(this);
        rl_unit.setOnClickListener(this);
        rl_insurance_company.setOnClickListener(this);
        riv_person_icon.setOnClickListener(this);
        rb_upload_cloud.setOnClickListener(this);
    }

    @Override
    protected void loadData() {
        getPersonalInformation(); // 获得个人信息
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_back:
                finish();
                break;
            case R.id.iv_string_code: // 扫描机身串码
                Intent intent = new Intent(MyCloud.this, Scan.class);
                startActivity(intent);
                break;
            case R.id.rl_unit:
                Intent unitIntent = new Intent(MyCloud.this, UnitCode.class);
                startActivity(unitIntent);
                break;
            case R.id.rl_insurance_company:
                //是4S店时才跳转界面
                if (unitkind == 1) {
                    //跳转到保险公司列表界面
                    Intent intent2 = new Intent(MyCloud.this, InsuranceCompanyList.class);
                    startActivity(intent2);
                }
                break;
            case R.id.riv_person_icon: // 点击查看并更换头像
                showTypeDialog();
                break;
            case R.id.rb_upload_cloud:
                if (!NetUtils.isNetworkConnected(MyCloud.this)) {
                    Toast.makeText(MyCloud.this, R.string.network_off, Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgress("拼命上传中...");
                // 现将头像上传到七牛，获取完整URL，再上传到服务器
                getQiniuToken();
                break;
        }
    }

    private void showTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        View view = View.inflate(this, R.layout.dialog_select_photo, null);
        TextView tv_select_gallery = (TextView) view.findViewById(R.id.pop_pic);
        TextView tv_select_camera = (TextView) view.findViewById(R.id.pop_camera);
        TextView tv_pop_cancel = (TextView) view.findViewById(R.id.pop_cancel);
        tv_select_gallery.setOnClickListener(new View.OnClickListener() {// 在相册中选取
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent1, 1);
                dialog.dismiss();
            }
        });
        tv_select_camera.setOnClickListener(new View.OnClickListener() {// 调用照相机
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent2.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "head.jpg")));
                startActivityForResult(intent2, 2);// 采用ForResult打开
                dialog.dismiss();
            }
        });
        tv_pop_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(view);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    cropPhoto(data.getData());// 裁剪图片
                }

                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    File temp = new File(Environment.getExternalStorageDirectory() + "/head.jpg");
                    cropPhoto(Uri.fromFile(temp));// 裁剪图片
                }

                break;
            case 3:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    head = extras.getParcelable("data");
                    if (head != null) {
                        /**
                         * 上传服务器代码
                         */
                        setPicToView(head);// 保存在SD卡中
                        riv_person_icon.setImageBitmap(head);// 用ImageView显示出来
                    }
                }
                break;
            default:
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 调用系统的裁剪功能
     *
     * @param uri
     */
    public void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 3);
    }

    private void setPicToView(Bitmap mBitmap) {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            return;
        }
        FileOutputStream b = null;
        File file = new File(path);
        file.mkdirs();// 创建文件夹
        String fileName = path + "head.jpg";// 图片名字
        try {
            b = new FileOutputStream(fileName);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭流
                b.flush();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获得个人信息
     */
    private void getPersonalInformation() {
        Map<String, String> map = new HashMap<>();
        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, Config.APPGET_PERSION_URL, jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    //赋值给用户名
                    if (jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("loginid") != null) {
                        tv_person_name.setText(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("loginid"));
                    }
                    // 用户昵称
                    if (jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("username") != null) {
                        et_nick_name.setText(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("username"));
                    }
                    // 用户备注
                    if (jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("demo") != null) {
                        et_person_remark.setText(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("demo"));
                    }
                    //机器串码
                    if (jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("machinecode") != null) {
                        et_string_code.setText(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("machinecode"));
                    }
                    //赋值给登记日期
                    if (jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("machinelogintime") != null) {
                        et_record_date.setText(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("machinelogintime"));
                    }
                    //赋值给返修日期
                    if (jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("machinebacktime") != null) {
                        et_repair_date.setText(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("machinebacktime"));
                    }
                    //赋值给单位快捷码
                    if (jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("shortcode") != null) {
                        et_unit_code.setText(jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("shortcode"));
                    }
                    //赋值给单位名称
                    if (jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitname") != null) {
                        et_unit_name.setText(jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitname"));
                    }
                    //赋值给投保公司
                    if (jsonObject.getJSONObject("datas").getJSONObject("insinfo").getString("unitname") != null) {
                        et_insurance_company.setText(jsonObject.getJSONObject("datas").getJSONObject("insinfo").getString("unitname"));
                        PreferenceUtil.commitString("InsuranceCompanyName2", jsonObject.getJSONObject("datas").getJSONObject("insinfo").getString("unitname"));
                    }
                    //赋值给投保公司电话
                    if (jsonObject.getJSONObject("datas").getJSONObject("insinfo").getString("companymobile") != null) {
                        et_insurance_company_phone.setText(jsonObject.getJSONObject("datas").getJSONObject("insinfo").getString("companymobile"));
                    }
                    // 单位类型
                    unitkind = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getInt("unitkind");
                    if (unitkind == 1) {
                        //获得单位编号
                        if (jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitcode") != null) {
                            unitCode = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitcode");
                            PreferenceUtil.commitString("unitcode", unitCode);
                        }
                    } else {
                        //获得单位编号
                        if (jsonObject.getJSONObject("datas").getJSONObject("insinfo").getString("unitcode") != null) {
                            unitCode = jsonObject.getJSONObject("datas").getJSONObject("insinfo").getString("unitcode");
                            PreferenceUtil.commitString("unitcode", unitCode);
                        }
                    }
                    if (jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("logoid") != null) {
                        headUrl = jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("logoid");
                        if(!"".equals(headUrl)) {
                            Glide.with(MyCloud.this).load(headUrl).into(riv_person_icon);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");

                //上传授权码
                headers.put("tokenid", PreferenceUtil.getString("tokenid", null));

                return headers;
            }
        };
        VLCApplication.queue.add(jsonRequest);
    }

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label) {
        kProgressHUD = KProgressHUD.create(MyCloud.this);
        kProgressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        kProgressHUD.setLabel(label);
        kProgressHUD.setCancellable(true);
        kProgressHUD.setAnimationSpeed(2);
        kProgressHUD.setDimAmount(0.5f);
        kProgressHUD.show();
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
                .btnTextColor(new int[]{ContextCompat.getColor(MyCloud.this,R.color.primary),ContextCompat.getColor(MyCloud.this,R.color.alphablack)})
                .show();
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 获取七牛token
     */
    private void getQiniuToken() {

        JSONObject jsonObject = new JSONObject();
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.QINIU_TOKEN + "?n=1", jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    if (200 == jsonObject.getInt("code")) {
                        baseUrl = jsonObject.getJSONObject("data").getString("baseUrl");
                        qiniuToken = jsonObject.getJSONObject("data").getString("token");
                        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("fileName");

                        String fileName = jsonArray.get(0).toString();
                        upLoadHead(fileName);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");

                //上传授权码
                headers.put("tokenid", PreferenceUtil.getString("tokenid", null));

                return headers;
            }
        };
        VLCApplication.queue.add(jsonRequest);

    }

    /**
     * 头像照片上传到七牛云
     *
     * @param fileName
     */
    private void upLoadHead(String fileName) {
        //头像照片
        File headFile = new File(path + "head.jpg");
        if (headFile.exists()) {
            VLCApplication.uploadManager.put(headFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            if(kProgressHUD != null) {
                                kProgressHUD.dismiss();
                            }
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    headUrl = Config.QINIU_BASE_URL + attachid;
                                    // 将信息上传到服务器
                                    uploadPersonInfo();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                            }
                        }
                    }, null);
        } else {
            uploadPersonInfo();
        }
    }

    /**
     * 将内容信息上传到服务器
     */
    private void uploadPersonInfo() {
        Map<String, String> map = new HashMap<>();
        map.put("unitcode", unitCode);

        if (unitkind == 1) {
            if (VLCApplication.insuranceCompanyDictionary.get(et_insurance_company.getText().toString()) != null) {
                //保险公司编号
                map.put("inscode", VLCApplication.insuranceCompanyDictionary.get(et_insurance_company.getText().toString()));
            } else {
                //保险公司编号
                map.put("inscode", "");
            }
        } else {
            //保险公司编号
            map.put("inscode", unitCode);
        }
        // 用户备注
        map.put("demo", et_person_remark.getText().toString());
        // 用户头像
        map.put("logoid", headUrl);// 用户头像
        // 机身串码
        map.put("machinecode", et_string_code.getText().toString());
        // 用户昵称
        map.put("username", et_nick_name.getText().toString());

        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, Config.APP_UPDATE_PERSON_URL, jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    if(kProgressHUD != null) {
                        kProgressHUD.dismiss();
                    }
                    //设置上传按钮能点击
                    rb_upload_cloud.setEnabled(true);
                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {
                        //上传完图片头像，就删除
                        File headFile = new File(path + "head.jpg");
                        if(headFile.exists()) {
                            headFile.delete();
                        }
                        if (VLCApplication.configsDictionary.get("app-c-170") != null) {
                            Toast.makeText(MyCloud.this, VLCApplication.configsDictionary.get("app-c-170"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyCloud.this, R.string.personal_info_upload_success, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MyCloud.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (VLCApplication.configsDictionary.get("app-c-171") != null) {
                        Toast.makeText(MyCloud.this, VLCApplication.configsDictionary.get("app-c-171"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MyCloud.this, R.string.personal_info_upload_faild, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                if(kProgressHUD != null) {
                    kProgressHUD.dismiss();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");

                //上传授权码
                headers.put("tokenid", PreferenceUtil.getString("tokenid", null));

                return headers;
            }
        };
        VLCApplication.queue.add(jsonRequest);

    }
}
