package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.Config;
import com.android.volley.VolleyError;
import com.android.volley.listener.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.ListDataSave;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import io.vov.vitamio.utils.Log;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

/**
 * Created by yangboru on 2018/1/24.
 * <p>
 * 赔付页面
 */

public class Payment extends BaseFragment {
    private final int HANDLER_MESSAGE_UPLOAD_SUCCESS = 1;
    private final int HANDLER_MESSAGE_REWOKE_SUCCESS = 2;
    private final int HANDLER_MESSAGE_UPLOAD_FAIL = 3;
    private final int HANDLER_MESSAGE_UPLOAD_NET_FAIL = 4;

    private final int MAX_UPLOAD_PAYMENT = 8;// 理赔上传时的数
    private final int MAX_UPLOAD_REPORT = 11;// 一键上传/一键报案的数

    private ImageView IDOpposite;
    private ImageView IDPositive;
    private ImageView bank;
    private ImageView drivingLicense;
    private ImageView driverLicense;
    private ImageView frameNumber;
    private ImageView switchIDOpposite;
    private ImageView switchIDPositive;
    private ImageView switchBank;
    private ImageView switchDrivingLicense;
    private ImageView switchDriverLicense;
    private ImageView switchFrameNumber;

    //判断是点击了那张图片进入的图片查看界面
    private String lookImage = "";

    private static int RESULT_LOAD_IMAGE = 10;

    //判断是点击了那张图片
    private String whichImage = "";
    private Button payment_left_btn;
    private Button payment_middle_btn;
    //    private Button payment_right_btn;
    private KProgressHUD kProgressHUD;
    private int trueFinish;

    private int unitkind;
    private RelativeLayout payment_btn_layout;
    private String examineState;
    private String license_bUrl;
    private String license_aUrl;
    private String cardUrl;
    private String drivelicenseUrl;
    private String permitUrl;
    private String frameUrl;

    private ListDataSave dataSave;

    private List<String> reportPhotoUrlArr1;
    private List<String> reportPhotoUrlArr2;
    private List<String> reportPhotoUrlArr4;
    private List<String> reportPhotoUrlArr5;
    private List<String> wholeCarUrlArr;
    private List<String> realVideoUrlArr;
    private String idOppositeUrl;
    private String idPositiveUrl;
    private String bankUrl;
    private String drivingLicenseUrl;
    private String frameNumberUrl;
    private String driverLicenseUrl;
    private LocalBroadcastManager localBroadcastManager;

    private String lotusStatus;
    private NormalDialog dialog;
    private String[] mStringBts;

    private String reportTime;
    private String accidentTime;
    private String geographicalPosition;
    private String isAccidentScene;
    private String plateNumber;
    private String informantPhone;
    private String informantName;
    private String accidentType;
    private String accidentResponsibility;
    private String isWounded;
    private String isNormalDriving;

    private String baseUrl;
    private String qiniuToken;
    private int onCarCount;
    private int moreCarCount;
    private ArrayList<String> arrPayment;
    private ArrayList<String> arrReport1;
    private ArrayList<String> arrReport2;
    private ArrayList<String> arrReport4;
    private ArrayList<String> arrReport5;
    private String strWholeCar;
    private String strVideo;
    private boolean isOneKeyReport = false;

    private String IDOppositeURL;
    private String IDPositiveURL;
    private String bankURL;
    private String drivingLicenseURL;
    private String frameNumberURL;
    private String driverLicenseURL;

    private int currentProgress;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_MESSAGE_UPLOAD_SUCCESS:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    payment_left_btn.setEnabled(true);
                    break;
                case HANDLER_MESSAGE_REWOKE_SUCCESS:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    payment_middle_btn.setEnabled(true);
                    break;
                case HANDLER_MESSAGE_UPLOAD_FAIL:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    payment_left_btn.setEnabled(true);
                    payment_middle_btn.setEnabled(true);
                    if (VLCApplication.configsDictionary.get("app-c-171") != null) {
                        Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-c-171"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "未上传成功，请稍后再试", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case HANDLER_MESSAGE_UPLOAD_NET_FAIL:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    payment_left_btn.setEnabled(true);
                    payment_middle_btn.setEnabled(true);
                    if (VLCApplication.configsDictionary.get("app-z-010") != null) {
                        Toast.makeText(getActivity(), VLCApplication.configsDictionary.get("app-z-010"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "连接失败，请检查您的网络连接", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    protected int setViewId() {
        return R.layout.payment_layout;
    }

    @Override
    protected void findView(View view) {
        IDOpposite = (ImageView) view.findViewById(R.id.IDOppositePayment);
        IDPositive = (ImageView) view.findViewById(R.id.IDPositivePayment);
        bank = (ImageView) view.findViewById(R.id.bankPayment);
        drivingLicense = (ImageView) view.findViewById(R.id.drivingLicensePayment);
        driverLicense = (ImageView) view.findViewById(R.id.driverLicensePayment);
        frameNumber = (ImageView) view.findViewById(R.id.frameNumberPayment);
        switchIDOpposite = (ImageView) view.findViewById(R.id.switchIDOppositePayment);
        switchIDPositive = (ImageView) view.findViewById(R.id.switchIDPositivePayment);
        switchBank = (ImageView) view.findViewById(R.id.switchBankPayment);
        switchDrivingLicense = (ImageView) view.findViewById(R.id.switchDrivingLicensePayment);
        switchDriverLicense = (ImageView) view.findViewById(R.id.switchDriverLicensePayment);
        switchFrameNumber = (ImageView) view.findViewById(R.id.switchFrameNumberPayment);
        payment_btn_layout = (RelativeLayout) view.findViewById(R.id.payment_btn_layout);
        payment_middle_btn = (Button) view.findViewById(R.id.payment_middle_btn);
        payment_left_btn = (Button) view.findViewById(R.id.payment_left_btn);
    }

    @Override
    protected void init() {

        IDOppositeURL = BitmapUtils.getSDPath() + "/VOC/Cache/IDOpposite.png";
        IDPositiveURL = BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png";
        bankURL = BitmapUtils.getSDPath() + "/VOC/Cache/bank.png";
        drivingLicenseURL = BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png";
        frameNumberURL = BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png";
        driverLicenseURL = BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png";

        reportPhotoUrlArr1 = new ArrayList<>();
        reportPhotoUrlArr2 = new ArrayList<>();
        reportPhotoUrlArr4 = new ArrayList<>();
        reportPhotoUrlArr5 = new ArrayList<>();
        wholeCarUrlArr = new ArrayList<>();
        realVideoUrlArr = new ArrayList<>();

        arrPayment = new ArrayList<>();
        arrReport1 = new ArrayList<>();
        arrReport2 = new ArrayList<>();
        arrReport4 = new ArrayList<>();
        arrReport5 = new ArrayList<>();

        dialog = new NormalDialog(getActivity());
        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);
        // 获取审核状态
        lotusStatus = PreferenceUtil.getString("lotusStatus", "");
        //得到本地广播管理器的实例
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());

        //用来保存list到本地
        dataSave = new ListDataSave(getContext(), "baiyu");

        //获得单位类型信息，1 4s 、 2 保险公司，为4s时就显示定损界面，为保险公司时就显示报案页面
        unitkind = PreferenceUtil.getInt("unitkind", 1);

        //用来解决上传时发生的错误，就是那个完成接口会调用两次
        trueFinish = 0;

        //获得列表中某项的审核状态
        examineState = PreferenceUtil.getString("examineState", "");

        if (unitkind == 1) {

            if (examineState.equals("事故照片有问题，需要您重新提交")) {//审核驳回
                //给按钮设置指定的文字
                payment_left_btn.setText("一键上传");
                payment_middle_btn.setText("销案");
            } else if (examineState.equals("事故照片已通过审核")) {// 审核成功
                //给按钮设置指定的文字
                payment_left_btn.setText("理赔上传");
                payment_middle_btn.setVisibility(View.GONE);
            } else if (examineState.equals("事故照片正在审核中")) { // 待审核
                //给按钮设置指定的文字
                payment_left_btn.setText("理赔上传");
                payment_middle_btn.setText("销案");
            } else if (examineState.equals("您已报案，等待您提交事故照片")) {//未申请
                payment_left_btn.setText("一键上传");
                payment_middle_btn.setVisibility(View.GONE);
            } else {
                payment_left_btn.setVisibility(View.GONE);
                payment_middle_btn.setVisibility(View.GONE);
            }

        } else {
            //给按钮设置指定的文字
            payment_left_btn.setText("一键上传");
            payment_middle_btn.setVisibility(View.GONE);
//            payment_right_btn.setText("微信分享");
        }
        //点击列表进来也显示个人信息里保存的照片
        getUserLoadPhoto();

        //获得事故ID的具体内容
        getCasedetail();

    }

    /**
     * 显示个人信息里保存的图片，如果是审核失败或强制撤销状态，切换的图标不显示
     */
    private void getUserLoadPhoto() {
        PreferenceUtil.commitBoolean("isDeleteImage", false);

        //判断本地缓存中是否存在图片
        File IDOppositeFile = new File(IDOppositeURL);
        if (IDOppositeFile.exists()) {
            //从缓存中获得图片
//            IDOpposite.setImageURI(Uri.fromFile(new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDOpposite.png")));
            IDOpposite.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(IDOppositeURL));
            if ("审核失败".equals(lotusStatus) || "销案".equals(lotusStatus)) {
                switchIDOpposite.setVisibility(View.GONE);
            } else {
                //显示切换图标
                switchIDOpposite.setVisibility(View.VISIBLE);
            }
        }
        //判断本地缓存中是否存在图片
        File IDPositiveFile = new File(IDPositiveURL);
        if (IDPositiveFile.exists()) {
            //从缓存中获得图片
//            IDPositive.setImageURI(Uri.fromFile(new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png")));
            IDPositive.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(IDPositiveURL));
            if ("审核失败".equals(lotusStatus) || "销案".equals(lotusStatus)) {
                switchIDPositive.setVisibility(View.GONE);
            } else {
                //显示切换图标
                switchIDPositive.setVisibility(View.VISIBLE);
            }
        }
        //判断本地缓存中是否存在图片
        File bankFile = new File(bankURL);
        if (bankFile.exists()) {
            //从缓存中获得图片
//            bank.setImageURI(Uri.fromFile(new File(BitmapUtils.getSDPath() + "/VOC/Cache/bank.png")));
            bank.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(bankURL));
            if ("审核失败".equals(lotusStatus) || "销案".equals(lotusStatus)) {
                switchBank.setVisibility(View.GONE);
            } else {
                //显示切换图标
                switchBank.setVisibility(View.VISIBLE);
            }
        }
        //判断本地缓存中是否存在图片
        File drivingLicenseFile = new File(drivingLicenseURL);
        if (drivingLicenseFile.exists()) {
            //从缓存中获得图片
//            drivingLicense.setImageURI(Uri.fromFile(new File(BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png")));
            drivingLicense.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(drivingLicenseURL));
            if ("审核失败".equals(lotusStatus) || "销案".equals(lotusStatus)) {
                switchDrivingLicense.setVisibility(View.GONE);
            } else {
                //显示切换图标
                switchDrivingLicense.setVisibility(View.VISIBLE);
            }
        }
        //判断本地缓存中是否存在图片
        File frameNumberFile = new File(frameNumberURL);
        if (frameNumberFile.exists()) {
            //从缓存中获得图片
//            frameNumber.setImageURI(Uri.fromFile(new File(BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png")));
            frameNumber.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(frameNumberURL));
            if ("审核失败".equals(lotusStatus) || "销案".equals(lotusStatus)) {
                switchFrameNumber.setVisibility(View.GONE);
            } else {
                //显示切换图标
                switchFrameNumber.setVisibility(View.VISIBLE);
            }
        }
        //判断本地缓存中是否存在图片
        File driverLicenseFile = new File(driverLicenseURL);
        if (driverLicenseFile.exists()) {
            //从缓存中获得图片
//            driverLicense.setImageURI(Uri.fromFile(new File(BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png")));
            driverLicense.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(driverLicenseURL));
            if ("审核失败".equals(lotusStatus) || "销案".equals(lotusStatus)) {
                switchDriverLicense.setVisibility(View.GONE);
            } else {
                //显示切换图标
                switchDriverLicense.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 获得事故ID的具体内容
     */
    private void getCasedetail() {
        Map<String, String> map = new HashMap<>();

        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));

        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.CASE_DETAIL_URL, jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {

                        JSONObject datas = jsonObject.getJSONObject("datas");
                        JSONObject casepictures = datas.getJSONObject("compensatepictures");

                        //被保险人身份证反面照片
                        JSONArray license_b = casepictures.getJSONArray("license_b");
                        for (int i = 0; i < license_b.length(); i++) {
                            JSONObject value = (JSONObject) license_b.get(i);
                            String piclink = value.getString("attachid");
                            //保存attachid，上传图片时有用
                            Map<String, String> license_bMap = new HashMap<>();
                            license_bMap.put("attachid", piclink);
                            VLCApplication.license_b.add(license_bMap);
//                            license_bUrl = Config.ATTACH_DOWNLOAD_URL + piclink;
                            license_bUrl = Config.QINIU_BASE_URL + piclink;
                            //从网络中获得图片
                            if (piclink != null && !piclink.equals("") && !piclink.equals("null")) {
                                ImageLoader.getInstance().displayImage(license_bUrl, IDOpposite);
                            }
                            if (piclink != null && !piclink.equals("") && (!"审核失败".equals(lotusStatus) || !"销案".equals(lotusStatus))) {
                                //显示switchIDOpposite
                                switchIDOpposite.setVisibility(View.VISIBLE);
                            }
                        }


                        //被保险人身份证正面照片
                        JSONArray license_a = casepictures.getJSONArray("license_a");
                        for (int i = 0; i < license_a.length(); i++) {
                            JSONObject value = (JSONObject) license_a.get(i);
                            String piclink = value.getString("attachid");
                            //保存attachid，上传图片时有用
                            Map<String, String> license_aMap = new HashMap<>();
                            license_aMap.put("attachid", piclink);
                            VLCApplication.license_a.add(license_aMap);
//                            license_aUrl = Config.ATTACH_DOWNLOAD_URL + piclink;
                            license_aUrl = Config.QINIU_BASE_URL + piclink;
                            //从网络中获得图片
                            if (piclink != null && !piclink.equals("") && !piclink.equals("null")) {
                                ImageLoader.getInstance().displayImage(license_aUrl, IDPositive);
                            }
                            if (piclink != null && !piclink.equals("") && (!"审核失败".equals(lotusStatus) || !"销案".equals(lotusStatus))) {
                                //显示switchIDPositive
                                switchIDPositive.setVisibility(View.VISIBLE);
                            }
                        }

                        //被保险人银行卡照片
                        JSONArray card = casepictures.getJSONArray("card");
                        for (int i = 0; i < card.length(); i++) {
                            JSONObject value = (JSONObject) card.get(i);
                            String piclink = value.getString("attachid");
                            //保存attachid，上传图片时有用
                            Map<String, String> cardMap = new HashMap<>();
                            cardMap.put("attachid", piclink);
                            VLCApplication.card.add(cardMap);
//                            cardUrl = Config.ATTACH_DOWNLOAD_URL + piclink;
                            cardUrl = Config.QINIU_BASE_URL + piclink;
                            //从网络中获得图片
                            if (piclink != null && !piclink.equals("") && !piclink.equals("null")) {
                                ImageLoader.getInstance().displayImage(cardUrl, bank);
                            }
                            if (piclink != null && !piclink.equals("") && (!"审核失败".equals(lotusStatus) || !"销案".equals(lotusStatus))) {
                                //显示switchBank
                                switchBank.setVisibility(View.VISIBLE);
                            }
                        }


                        //驾驶证照片
                        JSONArray drivelicense = casepictures.getJSONArray("drivelicense");
                        for (int i = 0; i < drivelicense.length(); i++) {
                            JSONObject value = (JSONObject) drivelicense.get(i);
                            String piclink = value.getString("attachid");
                            //保存attachid，上传图片时有用
                            Map<String, String> drivelicenseMap = new HashMap<>();
                            drivelicenseMap.put("attachid", piclink);
                            VLCApplication.drivelicense.add(drivelicenseMap);
//                            drivelicenseUrl = Config.ATTACH_DOWNLOAD_URL + piclink;
                            drivelicenseUrl = Config.QINIU_BASE_URL + piclink;
                            //从网络中获得图片

                            if (piclink != null && !piclink.equals("") && !piclink.equals("null")) {
                                ImageLoader.getInstance().displayImage(drivelicenseUrl, driverLicense);
                            }
                            if (piclink != null && !piclink.equals("") && (!"审核失败".equals(lotusStatus) || !"销案".equals(lotusStatus))) {
                                //显示switchDriverLicense
                                switchDriverLicense.setVisibility(View.VISIBLE);
                            }
                        }


                        //行驶证照片
                        JSONArray permit = casepictures.getJSONArray("permit");
                        for (int i = 0; i < permit.length(); i++) {
                            JSONObject value = (JSONObject) permit.get(i);
                            String piclink = value.getString("attachid");
                            //保存attachid，上传图片时有用
                            Map<String, String> permitMap = new HashMap<>();
                            permitMap.put("attachid", piclink);
                            VLCApplication.permit.add(permitMap);
//                            permitUrl = Config.ATTACH_DOWNLOAD_URL + piclink;
                            permitUrl = Config.QINIU_BASE_URL + piclink;
                            //从网络中获得图片
                            if (piclink != null && !piclink.equals("") && !piclink.equals("null")) {
                                ImageLoader.getInstance().displayImage(permitUrl, drivingLicense);
                            }
                            if (piclink != null && !piclink.equals("") && (!"审核失败".equals(lotusStatus) || !"销案".equals(lotusStatus))) {
                                //显示switchDrivingLicense
                                switchDrivingLicense.setVisibility(View.VISIBLE);
                            }
                        }


                        //车架号照片
                        JSONArray frame = casepictures.getJSONArray("frame");
                        for (int i = 0; i < frame.length(); i++) {
                            JSONObject value = (JSONObject) frame.get(i);
                            String piclink = value.getString("attachid");
                            //保存attachid，上传图片时有用
                            Map<String, String> frameMap = new HashMap<>();
                            frameMap.put("attachid", piclink);
                            VLCApplication.frame.add(frameMap);
//                            frameUrl = Config.ATTACH_DOWNLOAD_URL + piclink;
                            frameUrl = Config.QINIU_BASE_URL + piclink;
                            //从网络中获得图片

                            if (piclink != null && !piclink.equals("") && !piclink.equals("null")) {
                                ImageLoader.getInstance().displayImage(frameUrl, frameNumber);
                            }
                            if (piclink != null && !piclink.equals("") && (!"审核失败".equals(lotusStatus) || !"销案".equals(lotusStatus))) {
                                //显示switchFrameNumber
                                switchFrameNumber.setVisibility(View.VISIBLE);
                            }
                        }

                    } else {
                        Toast.makeText(getContext(), "获取数据失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "获取数据失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                if (VLCApplication.configsDictionary.get("app-z-010") != null) {
                    Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-z-010"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "连接失败，请检查您的网络连接", Toast.LENGTH_SHORT).show();
                }
                android.util.Log.e("Payment-589", "连接失败，请检查您的网络连接");
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

    @Override
    public void onResume() {
        super.onResume();
        if ("审核失败".equals(lotusStatus) || "销案".equals(lotusStatus)) {
            switchIDOpposite.setVisibility(View.GONE);
            switchIDPositive.setVisibility(View.GONE);
            switchBank.setVisibility(View.GONE);
            switchDrivingLicense.setVisibility(View.GONE);
            switchFrameNumber.setVisibility(View.GONE);
            switchDriverLicense.setVisibility(View.GONE);
        }

        if (PreferenceUtil.getBoolean("isDeleteImage", false)) {
            switch (lookImage) {
                case "IDOpposite":
                    //赋值，图片视图显示图片
                    IDOpposite.setImageResource(R.mipmap.id_opposite);
                    //隐藏switchIDOppositeImage
                    switchIDOpposite.setVisibility(View.GONE);
                    break;
                case "IDPositive":
                    //赋值，图片视图显示图片
                    IDPositive.setImageResource(R.mipmap.id_positive);
                    //隐藏switchIDPositiveImage
                    switchIDPositive.setVisibility(View.GONE);
                    break;
                case "bank":
                    //赋值，图片视图显示图片
                    bank.setImageResource(R.mipmap.bank);
                    //隐藏switchBankImage
                    switchBank.setVisibility(View.GONE);
                    break;
                case "drivingLicense":
                    //赋值，图片视图显示图片
                    drivingLicense.setImageResource(R.mipmap.driving_license);
                    //隐藏switchDrivingLicenseImage
                    switchDrivingLicense.setVisibility(View.GONE);
                    break;
                case "frameNumber":
                    //赋值，图片视图显示图片
                    frameNumber.setImageResource(R.mipmap.frame_number);
                    //隐藏switchFrameNumberImage
                    switchFrameNumber.setVisibility(View.GONE);
                    break;
                case "driverLicense":
                    //赋值，图片视图显示图片
                    driverLicense.setImageResource(R.mipmap.driver_license);
                    //隐藏switchDriverLicenseImage
                    switchDriverLicense.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
            PreferenceUtil.commitBoolean("isDeleteImage", false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:

                    // 图片选择结果回调
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的

                    for (int i = 0; i < selectList.size(); i++) {
                        //如何图片压缩成功了就取得压缩后的图片地址
                        if (selectList.get(i).isCompressed()) {
                            String picturePath = selectList.get(i).getCompressPath();
                            switch (whichImage) {
                                case "IDOpposite":
                                    //赋值，图片视图显示图片
                                    IDOpposite.setImageDrawable(null);
                                    //缓存图片
                                    try {
                                        saveMyBitmap2(BitmapFactory.decodeFile(picturePath), "IDOpposite");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "IDPositive":
                                    //赋值，图片视图显示图片
                                    IDPositive.setImageDrawable(null);
                                    //缓存图片
                                    try {
                                        saveMyBitmap2(BitmapFactory.decodeFile(picturePath), "IDPositive");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "bank":
                                    //赋值，图片视图显示图片
                                    bank.setImageDrawable(null);
                                    //缓存图片
                                    try {
                                        saveMyBitmap2(BitmapFactory.decodeFile(picturePath), "bank");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "drivingLicense":
                                    //赋值，图片视图显示图片
                                    drivingLicense.setImageDrawable(null);
                                    //缓存图片
                                    try {
                                        saveMyBitmap2(BitmapFactory.decodeFile(picturePath), "drivingLicense");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "frameNumber":
                                    //赋值，图片视图显示图片
                                    frameNumber.setImageDrawable(null);
                                    //缓存图片
                                    try {
                                        saveMyBitmap2(BitmapFactory.decodeFile(picturePath), "frameNumber");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "driverLicense":
                                    //赋值，图片视图显示图片
                                    driverLicense.setImageDrawable(null);
                                    //缓存图片
                                    try {
                                        saveMyBitmap2(BitmapFactory.decodeFile(picturePath), "driverLicense");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    break;
            }
        }

        //包括裁剪和压缩后的缓存，要在上传成功后调用，注意：需要系统sd卡权限
        PictureFileUtils.deleteCacheDirFile(Payment.this.getContext());

    }

    /**
     * 保存bitmap到SD卡
     *
     * @param bmp
     * @param bitName
     * @return
     * @throws IOException
     */
    public void saveMyBitmap2(final Bitmap bmp, final String bitName) throws IOException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (bmp != null) {
                    File dirFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/");
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }
                    File f = new File(BitmapUtils.getSDPath() + "/VOC/Cache/" + bitName + ".png");
                    if (f.exists()) {
                        f.delete();
                    }
                    try {
                        f.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    FileOutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(f);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        fOut.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Message msg = new Message();
                    msg.obj = bitName;
                    refreshHandler.sendMessage(msg);

                }
            }
        });
        thread.start();
    }

    //更新图片的handler
    Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.obj.toString()) {
                case "IDOpposite":
                    //赋值，图片视图显示图片
                    IDOpposite.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/IDOpposite.png"));
                    //显示switchIDOppositeImage
                    switchIDOpposite.setVisibility(View.VISIBLE);
                    //设置网络url为null
                    license_bUrl = null;
                    //当状态为事故照片有问题，需要您重新提交，上传图片时有用
                    PreferenceUtil.commitString("IDOpposite", "IDOpposite");
                    //删除对应已存在的attachid
                    VLCApplication.license_b.clear();
                    break;
                case "IDPositive":
                    //赋值，图片视图显示图片
                    IDPositive.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png"));
                    //显示switchIDPositiveImage
                    switchIDPositive.setVisibility(View.VISIBLE);
                    //设置网络url为null
                    license_aUrl = null;
                    //当状态为事故照片有问题，需要您重新提交，上传图片时有用
                    PreferenceUtil.commitString("IDPositive", "IDPositive");
                    //删除对应已存在的attachid
                    VLCApplication.license_a.clear();
                    break;
                case "bank":
                    //赋值，图片视图显示图片
                    bank.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/bank.png"));
                    //显示switchBankImage
                    switchBank.setVisibility(View.VISIBLE);
                    //设置网络url为null
                    cardUrl = null;
                    //当状态为事故照片有问题，需要您重新提交，上传图片时有用
                    PreferenceUtil.commitString("bank", "bank");
                    //删除对应已存在的attachid
                    VLCApplication.card.clear();
                    break;
                case "drivingLicense":
                    //赋值，图片视图显示图片
                    drivingLicense.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png"));
                    //显示switchDrivingLicenseImage
                    switchDrivingLicense.setVisibility(View.VISIBLE);
                    //设置网络url为null
                    permitUrl = null;
                    //当状态为事故照片有问题，需要您重新提交，上传图片时有用
                    PreferenceUtil.commitString("drivingLicense", "drivingLicense");
                    //删除对应已存在的attachid
                    VLCApplication.permit.clear();
                    break;
                case "frameNumber":
                    //赋值，图片视图显示图片
                    frameNumber.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png"));
                    //显示switchFrameNumberImage
                    switchFrameNumber.setVisibility(View.VISIBLE);
                    //设置网络url为null
                    frameUrl = null;
                    //当状态为事故照片有问题，需要您重新提交，上传图片时有用
                    PreferenceUtil.commitString("frameNumber", "frameNumber");
                    //删除对应已存在的attachid
                    VLCApplication.frame.clear();
                    break;
                case "driverLicense":
                    //赋值，图片视图显示图片
                    driverLicense.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png"));
                    //显示switchDriverLicenseImage
                    switchDriverLicense.setVisibility(View.VISIBLE);
                    //设置网络url为null
                    drivelicenseUrl = null;
                    //当状态为事故照片有问题，需要您重新提交，上传图片时有用
                    PreferenceUtil.commitString("driverLicense", "driverLicense");
                    //删除对应已存在的attachid
                    VLCApplication.drivelicense.clear();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 保存bitmap到SD卡
     *
     * @param bmp
     * @param bitName
     * @return
     * @throws IOException
     */
    public void saveMyBitmap(final Bitmap bmp, final String bitName) throws IOException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (bmp != null) {
                    File dirFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/");
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }
                    File f = new File(BitmapUtils.getSDPath() + "/VOC/Cache/" + bitName + ".png");
                    if (f.exists()) {
                        f.delete();
                    }
                    try {
                        f.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    FileOutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(f);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        fOut.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    protected void initEvents() {
        //点击身份证反面照片时的响应
        IDOpposite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchIDOpposite.getVisibility() == View.GONE) {
                    // 如果审核失败或撤销状态，查看照片
                    if ("审核失败".equals(lotusStatus) || "销案".equals(lotusStatus)) {
                        whichImage = "IDOpposite";
                        lookImage = "IDOpposite";
                        //从当前的界面跳转到图片查看界面
                        Intent intent = new Intent(getContext(), LookPhoto.class);

                        if (license_bUrl != null) {
                            intent.putExtra("whichImage", license_bUrl);
                        } else {
                            intent.putExtra("whichImage", lookImage);
                        }
                        intent.putExtra("lookImage", lookImage);
                        intent.putExtra("lotusStatus", lotusStatus);
                        startActivity(intent);
                    } else {
                        whichImage = "IDOpposite";
                        //弹出图片选择页面
                        PictureSelector.create(getActivity())
                                .openGallery(PictureMimeType.ofImage())
                                .selectionMode(PictureConfig.SINGLE)//设置为单选
                                .compress(true)// 是否压缩 true or false
                                .forResult(PictureConfig.CHOOSE_REQUEST);
                    }

                } else {
                    whichImage = "IDOpposite";
                    lookImage = "IDOpposite";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(getContext(), LookPhoto.class);

                    if (license_bUrl != null) {
                        intent.putExtra("whichImage", license_bUrl);
                    } else {
                        intent.putExtra("whichImage", lookImage);
                    }
                    intent.putExtra("lookImage", lookImage);
                    intent.putExtra("lotusStatus", lotusStatus);
                    startActivity(intent);
                }
            }
        });
        //点击身份证正面照片时的响应
        IDPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchIDPositive.getVisibility() == View.GONE) {
                    if ("审核失败".equals(lotusStatus) || "销案".equals(lotusStatus)) {
                        whichImage = "IDPositive";
                        lookImage = "IDPositive";
                        //从当前的界面跳转到图片查看界面
                        Intent intent = new Intent(getContext(), LookPhoto.class);

                        if (license_aUrl != null) {
                            intent.putExtra("whichImage", license_aUrl);
                        } else {
                            intent.putExtra("whichImage", lookImage);
                        }
                        intent.putExtra("lookImage", lookImage);
                        intent.putExtra("lotusStatus", lotusStatus);
                        startActivity(intent);
                    } else {
                        whichImage = "IDPositive";
                        //弹出图片选择页面
                        PictureSelector.create(getActivity())
                                .openGallery(PictureMimeType.ofImage())
                                .selectionMode(PictureConfig.SINGLE)//设置为单选
                                .compress(true)// 是否压缩 true or false
                                .forResult(PictureConfig.CHOOSE_REQUEST);
                    }

                } else {
                    whichImage = "IDPositive";
                    lookImage = "IDPositive";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(getContext(), LookPhoto.class);

                    if (license_aUrl != null) {
                        intent.putExtra("whichImage", license_aUrl);
                    } else {
                        intent.putExtra("whichImage", lookImage);
                    }
                    intent.putExtra("lookImage", lookImage);
                    intent.putExtra("lotusStatus", lotusStatus);
                    startActivity(intent);
                }
            }
        });
        //点击银行卡照片时的响应
        bank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchBank.getVisibility() == View.GONE) {
                    if ("审核失败".equals(lotusStatus) || "销案".equals(lotusStatus)) {
                        whichImage = "bank";
                        lookImage = "bank";
                        //从当前的界面跳转到图片查看界面
                        Intent intent = new Intent(getContext(), LookPhoto.class);

                        if (cardUrl != null) {
                            intent.putExtra("whichImage", cardUrl);
                        } else {
                            intent.putExtra("whichImage", lookImage);
                        }
                        intent.putExtra("lookImage", lookImage);
                        intent.putExtra("lotusStatus", lotusStatus);
                        startActivity(intent);
                    } else {
                        whichImage = "bank";
                        //弹出图片选择页面
                        PictureSelector.create(getActivity())
                                .openGallery(PictureMimeType.ofImage())
                                .selectionMode(PictureConfig.SINGLE)//设置为单选
                                .compress(true)// 是否压缩 true or false
                                .forResult(PictureConfig.CHOOSE_REQUEST);
                    }

                } else {
                    whichImage = "bank";
                    lookImage = "bank";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(getContext(), LookPhoto.class);

                    if (cardUrl != null) {
                        intent.putExtra("whichImage", cardUrl);
                    } else {
                        intent.putExtra("whichImage", lookImage);
                    }
                    intent.putExtra("lookImage", lookImage);
                    intent.putExtra("lotusStatus", lotusStatus);
                    startActivity(intent);
                }
            }
        });
        //点击行驶证照片时的响应
        drivingLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchDrivingLicense.getVisibility() == View.GONE) {
                    if ("审核失败".equals(lotusStatus) || "销案".equals(lotusStatus)) {
                        whichImage = "drivingLicense";
                        lookImage = "drivingLicense";
                        //从当前的界面跳转到图片查看界面
                        Intent intent = new Intent(getContext(), LookPhoto.class);

                        if (permitUrl != null) {
                            intent.putExtra("whichImage", permitUrl);
                        } else {
                            intent.putExtra("whichImage", lookImage);
                        }
                        intent.putExtra("lookImage", lookImage);
                        intent.putExtra("lotusStatus", lotusStatus);
                        startActivity(intent);
                    } else {
                        whichImage = "drivingLicense";
                        //弹出图片选择页面
                        PictureSelector.create(getActivity())
                                .openGallery(PictureMimeType.ofImage())
                                .selectionMode(PictureConfig.SINGLE)//设置为单选
                                .compress(true)// 是否压缩 true or false
                                .forResult(PictureConfig.CHOOSE_REQUEST);
                    }

                } else {
                    whichImage = "drivingLicense";
                    lookImage = "drivingLicense";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(getContext(), LookPhoto.class);

                    if (permitUrl != null) {
                        intent.putExtra("whichImage", permitUrl);
                    } else {
                        intent.putExtra("whichImage", lookImage);
                    }
                    intent.putExtra("lookImage", lookImage);
                    intent.putExtra("lotusStatus", lotusStatus);
                    startActivity(intent);
                }
            }
        });
        //点击驾驶证照片时的响应
        driverLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchDriverLicense.getVisibility() == View.GONE) {
                    if ("审核失败".equals(lotusStatus) || "销案".equals(lotusStatus)) {
                        whichImage = "driverLicense";
                        lookImage = "driverLicense";
                        //从当前的界面跳转到图片查看界面
                        Intent intent = new Intent(getContext(), LookPhoto.class);

                        if (drivelicenseUrl != null) {
                            intent.putExtra("whichImage", drivelicenseUrl);
                        } else {
                            intent.putExtra("whichImage", lookImage);
                        }
                        intent.putExtra("lookImage", lookImage);
                        intent.putExtra("lotusStatus", lotusStatus);
                        startActivity(intent);
                    } else {
                        whichImage = "driverLicense";
                        //弹出图片选择页面
                        PictureSelector.create(getActivity())
                                .openGallery(PictureMimeType.ofImage())
                                .selectionMode(PictureConfig.SINGLE)//设置为单选
                                .compress(true)// 是否压缩 true or false
                                .forResult(PictureConfig.CHOOSE_REQUEST);
                    }

                } else {
                    whichImage = "driverLicense";
                    lookImage = "driverLicense";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(getContext(), LookPhoto.class);

                    if (drivelicenseUrl != null) {
                        intent.putExtra("whichImage", drivelicenseUrl);
                    } else {
                        intent.putExtra("whichImage", lookImage);
                    }
                    intent.putExtra("lookImage", lookImage);
                    intent.putExtra("lotusStatus", lotusStatus);
                    startActivity(intent);
                }
            }
        });
        //点击车架号照片时的响应
        frameNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchFrameNumber.getVisibility() == View.GONE) {
                    if ("审核失败".equals(lotusStatus) || "销案".equals(lotusStatus)) {
                        whichImage = "frameNumber";
                        lookImage = "frameNumber";
                        //从当前的界面跳转到图片查看界面
                        Intent intent = new Intent(getContext(), LookPhoto.class);

                        if (frameUrl != null) {
                            intent.putExtra("whichImage", frameUrl);
                        } else {
                            intent.putExtra("whichImage", lookImage);
                        }
                        intent.putExtra("lookImage", lookImage);
                        intent.putExtra("lotusStatus", lotusStatus);
                        startActivity(intent);
                    } else {
                        whichImage = "frameNumber";
                        //弹出图片选择页面
                        PictureSelector.create(getActivity())
                                .openGallery(PictureMimeType.ofImage())
                                .selectionMode(PictureConfig.SINGLE)//设置为单选
                                .compress(true)// 是否压缩 true or false
                                .forResult(PictureConfig.CHOOSE_REQUEST);
                    }

                } else {
                    whichImage = "frameNumber";
                    lookImage = "frameNumber";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(getContext(), LookPhoto.class);

                    if (frameUrl != null) {
                        intent.putExtra("whichImage", frameUrl);
                    } else {
                        intent.putExtra("whichImage", lookImage);
                    }
                    intent.putExtra("lookImage", lookImage);
                    intent.putExtra("lotusStatus", lotusStatus);
                    startActivity(intent);
                }
            }
        });
        //点击切换身份证反面照片时的响应
        switchIDOpposite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "IDOpposite";
                //弹出图片选择页面
                PictureSelector.create(getActivity())
                        .openGallery(PictureMimeType.ofImage())
                        .selectionMode(PictureConfig.SINGLE)//设置为单选
                        .compress(true)// 是否压缩 true or false
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
        });
        //点击切换身份证正面照片时的响应
        switchIDPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "IDPositive";
                //弹出图片选择页面
                PictureSelector.create(getActivity())
                        .openGallery(PictureMimeType.ofImage())
                        .selectionMode(PictureConfig.SINGLE)//设置为单选
                        .compress(true)// 是否压缩 true or false
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
        });
        //点击切换银行卡照片时的响应
        switchBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "bank";
                //弹出图片选择页面
                PictureSelector.create(getActivity())
                        .openGallery(PictureMimeType.ofImage())
                        .selectionMode(PictureConfig.SINGLE)//设置为单选
                        .compress(true)// 是否压缩 true or false
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
        });
        //点击切换行驶证照片时的响应
        switchDrivingLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "drivingLicense";
                //弹出图片选择页面
                PictureSelector.create(getActivity())
                        .openGallery(PictureMimeType.ofImage())
                        .selectionMode(PictureConfig.SINGLE)//设置为单选
                        .compress(true)// 是否压缩 true or false
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
        });
        //点击切换驾驶证照片时的响应
        switchDriverLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "driverLicense";
                //弹出图片选择页面
                PictureSelector.create(getActivity())
                        .openGallery(PictureMimeType.ofImage())
                        .selectionMode(PictureConfig.SINGLE)//设置为单选
                        .compress(true)// 是否压缩 true or false
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
        });
        //点击切换车架号照片时的响应
        switchFrameNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "frameNumber";
                //弹出图片选择页面
                PictureSelector.create(getActivity())
                        .openGallery(PictureMimeType.ofImage())
                        .selectionMode(PictureConfig.SINGLE)//设置为单选
                        .compress(true)// 是否压缩 true or false
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
        });
//        //点击理赔页面底下右边按钮时的响应
//        payment_right_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!NetUtils.isNetworkConnected(getActivity())) {//判断是否联网
//                    ToastUtils.showNomalShortToast(getActivity(), getString(R.string.network_off));
//                    return;
//                }
//                if (payment_right_btn.getText().equals("微信分享")) {
//                    //如果最底下的右边按钮的文字是微信分享时的执行代码
//                    weChatSharing();
//                }
//            }
//        });
        //点击理赔页面底下左边按钮时的响应
        payment_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetUtils.isNetworkConnected(getActivity())) {//判断是否联网
                    ToastUtils.showNomalShortToast(getActivity(), getString(R.string.network_off));
                    return;
                }
                if (payment_left_btn.getText().equals("一键上传")) {
                    Utils.doCallBackMethod();
                    reportPhotoUrlArr1 = dataSave.getDataList("reportPhotoArr1");
                    reportPhotoUrlArr2 = dataSave.getDataList("reportPhotoArr2");
                    reportPhotoUrlArr4 = dataSave.getDataList("reportPhotoArr4");
                    reportPhotoUrlArr5 = dataSave.getDataList("reportPhotoArr5");
                    wholeCarUrlArr = dataSave.getDataList("wholeCarArr");
                    realVideoUrlArr = dataSave.getDataList("realVideoNameArr");

                    onCarCount = +(reportPhotoUrlArr1.size() - 1) + (reportPhotoUrlArr2.size() - 1)
                            + wholeCarUrlArr.size() + realVideoUrlArr.size();
                    moreCarCount = (reportPhotoUrlArr1.size() - 1) + (reportPhotoUrlArr2.size() - 1)
                            + (reportPhotoUrlArr4.size() - 1) + (reportPhotoUrlArr5.size() - 1)
                            + wholeCarUrlArr.size() + realVideoUrlArr.size();
                    //先判断用户有没有填写完全了
                    if (!isPhotoFieldFull()) {
                        return;
                    }
                    showTipDialog("请再次确认您所有的报案信息，继续吗？");
                    dialog.setOnBtnClickL(
                            new OnBtnClickL() {
                                @Override
                                public void onBtnClick() {
                                    dialog.dismiss();
                                    getReportField();//获取报案页面字段内容
                                    if (!isReportFieldFull()) { // 判断报案页面内容是否都填了
                                        return;
                                    }
                                    if ("多车".equals(PreferenceUtil.getString("accidentkind", ""))) {
                                        showProgress("拼命上传中", MAX_UPLOAD_REPORT + moreCarCount);
                                    } else {
                                        showProgress("拼命上传中", MAX_UPLOAD_REPORT + onCarCount);
                                    }
//                                    showProgress("拼命上传中");
                                    uploadField();

                                }
                            },
                            new OnBtnClickL() {
                                @Override
                                public void onBtnClick() {
                                    dialog.dismiss();
                                }
                            });

                } else if (payment_left_btn.getText().equals("理赔上传")) {

//                    if (!isFieldFull()) {//先判断用户有没有填写完全了
//                        return;
//                    }
                    //获得保存在本地的赔付图片url
                    idOppositeUrl = PreferenceUtil.getString("IDOpposite", null);
                    idPositiveUrl = PreferenceUtil.getString("IDPositive", null);
                    bankUrl = PreferenceUtil.getString("bank", null);
                    drivingLicenseUrl = PreferenceUtil.getString("drivingLicense", null);
                    frameNumberUrl = PreferenceUtil.getString("frameNumber", null);
                    driverLicenseUrl = PreferenceUtil.getString("driverLicense", null);

                    showTipDialog("请再次确认您的理赔信息是否正确无误？");
                    dialog.setOnBtnClickL(
                            new OnBtnClickL() {
                                @Override
                                public void onBtnClick() {
                                    dialog.dismiss();

                                    showProgress("拼命上传中", MAX_UPLOAD_PAYMENT);
                                    getQiniuToken();
                                }
                            },
                            new OnBtnClickL() {
                                @Override
                                public void onBtnClick() {
                                    dialog.dismiss();
                                }
                            });

                }

            }
        });
        // 撤销
        payment_middle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetUtils.isNetworkConnected(getActivity())) {//是否联网
                    ToastUtils.showNomalShortToast(getActivity(), getString(R.string.network_off));
                    android.util.Log.e("Payment-1402", "连接失败，请检查您的网络连接");
                    return;
                }
                if (payment_middle_btn.getText().toString().equals("销案")) {//撤销
                    showTipDialog("您确认要销案吗？");

                    dialog.setOnBtnClickL(
                            new OnBtnClickL() {
                                @Override
                                public void onBtnClick() {
                                    dialog.dismiss();
                                    revoke();//撤销
                                }
                            },
                            new OnBtnClickL() {
                                @Override
                                public void onBtnClick() {
                                    dialog.dismiss();
                                }
                            });
                }
            }
        });
    }

    /**
     * 理赔上传步骤 1
     */
    private void oneKeyPayment() {
        String url = Config.ATTACH_UPLOAD_URL;

        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.addHeader("tokenid", PreferenceUtil.getString("tokenid", null));
        builder.url(url);

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();

        //设置一键上传理赔按钮不能点击
        payment_left_btn.setEnabled(false);
        //弹出一个进度框
//        if (VLCApplication.configsDictionary.get("app-z-005") != null) {
//            showProgress(VLCApplication.configsDictionary.get("app-z-005"));
//        } else {
//            showProgress("正在拼命上传中");
//        }

        if (switchIDOpposite.getVisibility() == View.VISIBLE) {
            //上传身份证反面照片
            File IDOppositeImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDOpposite.png");
            bodyBuilder.addFormDataPart("license_b", "license_b.png", RequestBody.create(MediaType.parse("image/png"), IDOppositeImageFile));
        }

        if (switchIDPositive.getVisibility() == View.VISIBLE) {
            //上传身份证正面照片
            File IDPositiveImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png");
            bodyBuilder.addFormDataPart("license_a", "license_a.png", RequestBody.create(MediaType.parse("image/png"), IDPositiveImageFile));
        }

        if (switchBank.getVisibility() == View.VISIBLE) {
            //上传银行卡照片
            File bankImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/bank.png");
            bodyBuilder.addFormDataPart("card", "card.png", RequestBody.create(MediaType.parse("image/png"), bankImageFile));
        }

        if (switchDrivingLicense.getVisibility() == View.VISIBLE) {
            //上传行驶证照片
            File drivingLicenseImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png");
            bodyBuilder.addFormDataPart("Permit", "Permit.png", RequestBody.create(MediaType.parse("image/png"), drivingLicenseImageFile));
        }

        if (switchDriverLicense.getVisibility() == View.VISIBLE) {
            //上传驾驶证照片
            File driverLicenseImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png");
            bodyBuilder.addFormDataPart("drivelicense", "drivelicense.png", RequestBody.create(MediaType.parse("image/png"), driverLicenseImageFile));
        }

        if (switchFrameNumber.getVisibility() == View.VISIBLE) {
            //上传车架号照片
            File frameNumberImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png");
            bodyBuilder.addFormDataPart("frame", "frame.png", RequestBody.create(MediaType.parse("image/png"), frameNumberImageFile));
        }

        MultipartBody build = bodyBuilder.build();

        //注意onUIProgressFinish会执行两次，会造成一些错误，所以用trueFinish这个变量来解决
        RequestBody requestBody = ProgressHelper.withProgress(build, new ProgressUIListener() {

            @Override
            public void onUIProgressStart(long totalBytes) {
            }

            @Override
            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                if (trueFinish == 1) {
                    //设置进度
                    kProgressHUD.setProgress((int) (((double) (numBytes) / (double) (totalBytes)) * 100));
                }
            }

            @Override
            public void onUIProgressFinish() {

                if (trueFinish == 1) {
                    //设置为初始值
                    trueFinish = 0;
                }
                trueFinish += 1;

            }
        });
        builder.post(requestBody);

        Call call = okHttpClient.newCall(builder.build());

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //关闭进度框
                kProgressHUD.dismiss();
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
                if (VLCApplication.configsDictionary.get("app-c-171") != null) {
                    Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-c-171"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "未上传成功，请稍后再试", Toast.LENGTH_SHORT).show();
                }
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //注意，这里有一个巨坑，要获得返回的json数据，一定要写成response.body().string()，不然返回的不是json数据
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONObject datas = jsonObject.getJSONObject("datas");
                    JSONArray attachs = datas.getJSONArray("attachs");

                    for (int i = 0; i < attachs.length(); i++) {
                        JSONObject value = (JSONObject) attachs.get(i);
                        String filename = value.getString("filename");
                        String attachid = value.getString("attachid");

                        if (filename.contains("license_b")) {
                            Map<String, String> license_bMap = new HashMap<>();
                            license_bMap.put("attachid", attachid);
                            VLCApplication.license_b.add(license_bMap);
                        } else if (filename.contains("frame")) {
                            Map<String, String> frameMap = new HashMap<>();
                            frameMap.put("attachid", attachid);
                            VLCApplication.frame.add(frameMap);
                        } else if (filename.contains("license_a")) {
                            Map<String, String> license_aMap = new HashMap<>();
                            license_aMap.put("attachid", attachid);
                            VLCApplication.license_a.add(license_aMap);
                        } else if (filename.contains("Permit")) {
                            Map<String, String> permitMap = new HashMap<>();
                            permitMap.put("attachid", attachid);
                            VLCApplication.permit.add(permitMap);
                        } else if (filename.contains("card")) {
                            Map<String, String> cardMap = new HashMap<>();
                            cardMap.put("attachid", attachid);
                            VLCApplication.card.add(cardMap);
                        } else if (filename.contains("drivelicense")) {
                            Map<String, String> drivelicenseMap = new HashMap<>();
                            drivelicenseMap.put("attachid", attachid);
                            VLCApplication.drivelicense.add(drivelicenseMap);
                        }
                    }

                    //给casepictures集合赋值
                    VLCApplication.compensatepictures.put("license_b", VLCApplication.license_b);
                    VLCApplication.compensatepictures.put("frame", VLCApplication.frame);
                    VLCApplication.compensatepictures.put("license_a", VLCApplication.license_a);
                    VLCApplication.compensatepictures.put("permit", VLCApplication.permit);
                    VLCApplication.compensatepictures.put("card", VLCApplication.card);
                    VLCApplication.compensatepictures.put("drivelicense", VLCApplication.drivelicense);

                    //理赔步骤2
                    oneKeyPayment2();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 理赔步骤2
     */
    private void oneKeyPayment2() {
        Map<String, Object> map = new HashMap<>();

        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));
        //设置照片数组
        Gson gson = new Gson();
        //把Map转为json字符串
        String jsonStr = gson.toJson(VLCApplication.compensatepictures);
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            map.put("compensatepictures", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.CASE_STEP_THREE_URL, jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {

                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);
                        currentProgress = 0;

                        if (!isOneKeyReport) {
                            if ("1".equals(PreferenceUtil.getString("isdemo", "0"))) {
                                Toast.makeText(getContext(), "提交成功，请耐心等待后台审核", Toast.LENGTH_SHORT).show();
                            } else {
                                if (VLCApplication.configsDictionary.get("app-c-170") != null) {
                                    Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-c-170"), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "以上信息已经为您成功上传", Toast.LENGTH_SHORT).show();
                                }
                                // 理赔上传结束后，调用发送短信接口
                                sendSMS(jsonObject.getJSONObject("datas").getJSONObject("case").getString("caseid"), 3);
                            }

                        } else {
                            unitkind = PreferenceUtil.getInt("unitkind", 0);
                            if (unitkind == 1) {
                                if (VLCApplication.configsDictionary.get("app-c-170") != null) {
                                    Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-c-170"), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "以上信息已经为您成功上传", Toast.LENGTH_SHORT).show();
                                }

                                if (!PreferenceUtil.getString("isdemo", "0").equals("1")) {//如果不是模拟报案保存信息到本地
                                    //保存个人信息到本地
                                    if (PreferenceUtil.getString("driverName", "").equals("")) {
                                        PreferenceUtil.commitString("driverName", informantName);
                                    }
                                    if (PreferenceUtil.getString("driverPhone", "").equals("")) {
                                        PreferenceUtil.commitString("driverPhone", informantPhone);
                                    }
                                    PreferenceUtil.commitString("plateNumber", plateNumber);

                                    // 一键上传结束后，调用发送短信接口
                                    sendSMS(jsonObject.getJSONObject("datas").getJSONObject("case").getString("caseid"), 2);
                                }

                                PreferenceUtil.commitString("examineState", "事故照片正在审核中");
                            } else {
                                if (VLCApplication.configsDictionary.get("app-c-170") != null) {
                                    Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-c-170"), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "以上信息已经为您成功上传", Toast.LENGTH_SHORT).show();
                                }
                                // 一键上传结束后，调用发送短信接口
                                sendSMS(jsonObject.getJSONObject("datas").getJSONObject("case").getString("caseid"), 2);
                            }
                        }
                        isOneKeyReport = false;
                        //发送本地广播
                        Intent intent = new Intent("OnlineSurvey2");
                        localBroadcastManager.sendBroadcast(intent);

                        Message message = new Message();
                        message.what = HANDLER_MESSAGE_UPLOAD_SUCCESS;
                        handler.sendMessage(message);
                    } else {
                        isOneKeyReport = false;
                        Message message = new Message();
                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                        handler.sendMessage(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    isOneKeyReport = false;
                    Message message = new Message();
                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                isOneKeyReport = false;
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_NET_FAIL;
                handler.sendMessage(message);
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
     * 上传字段
     */
    private void uploadField() {
        Map<String, String> map = new HashMap<>();
        //post参数

        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));
        //报案时间
        map.put("casedate", reportTime);
        //事故日期时间
        map.put("accidentdate", accidentTime);
        //出险地点
        map.put("accidentaddress", geographicalPosition);
        //是否事故现场
        map.put("accidentscene", isAccidentScene);
        //车牌号
        map.put("carnumber", plateNumber);
        //报案人联系方式
        map.put("mobile", informantPhone);
        //报案人
        map.put("username", informantName);
        //事故类型
        map.put("accidentkind", accidentType);
        //事故责任
        map.put("accident", accidentResponsibility);
        //是否有人伤
        map.put("personinjure", isWounded);
        //车辆能否正常行驶
        map.put("carcanmove", isNormalDriving);
        //是否为模拟数据
        map.put("isdemo", PreferenceUtil.getString("isdemo", "0"));

        //设置报案按钮不能点击
        payment_left_btn.setEnabled(false);

        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.CASE_STEP_ONE_URL, jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {

                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {

                        if (!PreferenceUtil.getString("isdemo", "0").equals("1")) {//如果不是模拟报案保存信息到本地
                            //保存个人信息到本地
                            if (PreferenceUtil.getString("driverName", "").equals("")) {
                                PreferenceUtil.commitString("driverName", informantName);
                            }
                            if (PreferenceUtil.getString("driverPhone", "").equals("")) {
                                PreferenceUtil.commitString("driverPhone", informantPhone);
                            }
                            PreferenceUtil.commitString("plateNumber", plateNumber);
                        }
                        //保存事故类型，多车还是单车
                        PreferenceUtil.commitString("accidentkind", accidentType);

                        //保存单位类型，1 4s 、 2 保险公司
                        unitkind = jsonObject.getJSONObject("datas").getJSONObject("case").getInt("unitkind");
                        PreferenceUtil.commitInt("unitkind", unitkind);

                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);

                        if ("多车".equals(PreferenceUtil.getString("accidentkind", ""))) {
                            getQiniuTokenForMoreCar();
                        } else {
                            getQiniuTokenForOneCar();
                        }

                    } else {
                        Message message = new Message();
                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                        handler.sendMessage(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_NET_FAIL;
                handler.sendMessage(message);
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
     * 上传步骤 1
     */
    private void uploadTap() {
        String url = Config.ATTACH_UPLOAD_URL;

        OkHttpClient okHttpClient = new OkHttpClient();
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        builder.addHeader("tokenid", PreferenceUtil.getString("tokenid", null));
        builder.url(url);

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();

        //分多车和单车两种情况
        if (PreferenceUtil.getString("accidentkind", "").equals("多车")) {

            //设置报案按钮不能点击
            payment_left_btn.setEnabled(false);
            //弹出一个进度框
//            if (VLCApplication.configsDictionary.get("app-z-005") != null) {
//                showProgress(VLCApplication.configsDictionary.get("app-z-005"));
//            } else {
//                showProgress("正在拼命上传中");
//            }

            if (reportPhotoUrlArr1.size() != 0) {
                //上传定损图片1
                for (int i = 0; i < reportPhotoUrlArr1.size() - 1; i++) {
                    if (!reportPhotoUrlArr1.get(i).contains("https")) {
                        File reportPhotoArr1File = new File(reportPhotoUrlArr1.get(i).substring(7));
                        bodyBuilder.addFormDataPart("more_a" + i, "more_a" + i + ".JPEG", RequestBody.create(MediaType.parse("image/jpeg"), reportPhotoArr1File));
                    }
                }
            }

            if (reportPhotoUrlArr2.size() != 0) {
                //上传定损图片2
                for (int i = 0; i < reportPhotoUrlArr2.size() - 1; i++) {
                    if (!reportPhotoUrlArr2.get(i).contains("https")) {
                        File reportPhotoArr2File = new File(reportPhotoUrlArr2.get(i).substring(7));
                        bodyBuilder.addFormDataPart("more_c" + i, "more_c" + i + ".JPEG", RequestBody.create(MediaType.parse("image/jpeg"), reportPhotoArr2File));
                    }
                }
            }

            if (reportPhotoUrlArr4.size() != 0) {
                //上传定损图片4
                for (int i = 0; i < reportPhotoUrlArr4.size() - 1; i++) {
                    if (!reportPhotoUrlArr4.get(i).contains("https")) {
                        File reportPhotoArr4File = new File(reportPhotoUrlArr4.get(i).substring(7));
                        bodyBuilder.addFormDataPart("more_d" + i, "more_d" + i + ".JPEG", RequestBody.create(MediaType.parse("image/jpeg"), reportPhotoArr4File));
                    }
                }
            }

            if (reportPhotoUrlArr5.size() != 0) {
                //上传定损图片5
                for (int i = 0; i < reportPhotoUrlArr5.size() - 1; i++) {
                    if (!reportPhotoUrlArr5.get(i).contains("https")) {
                        File reportPhotoArr5File = new File(reportPhotoUrlArr5.get(i).substring(7));
                        bodyBuilder.addFormDataPart("more_e" + i, "more_e" + i + ".JPEG", RequestBody.create(MediaType.parse("image/jpeg"), reportPhotoArr5File));
                    }
                }
            }


            if (wholeCarUrlArr.size() != 0) {
                //上传全车45度图片
                for (int i = 0; i < wholeCarUrlArr.size(); i++) {
                    if (!wholeCarUrlArr.get(i).contains("https")) {
                        File takeWholeCarPhoto0File = new File(wholeCarUrlArr.get(i).substring(7));
                        bodyBuilder.addFormDataPart("more_b", "more_b.JPEG", RequestBody.create(MediaType.parse("image/jpeg"), takeWholeCarPhoto0File));
                    }
                }
            }

            if (realVideoUrlArr.size() != 0) {
                //上传事故小视频
                for (int i = 0; i < realVideoUrlArr.size(); i++) {
                    if (!realVideoUrlArr.get(i).contains("https")) {
                        File realVideoNameArrFile = new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/" + realVideoUrlArr.get(i));
                        bodyBuilder.addFormDataPart("video" + i, "video" + i + ".mp4", RequestBody.create(MediaType.parse("video/mp4"), realVideoNameArrFile));
                    }
                }
            }

        } else {

            //设置报案按钮不能点击
            payment_left_btn.setEnabled(false);
            //弹出一个进度框
//            if (VLCApplication.configsDictionary.get("app-z-005") != null) {
//                showProgress(VLCApplication.configsDictionary.get("app-z-005"));
//            } else {
//                showProgress("正在拼命上传中");
//            }

            if (reportPhotoUrlArr1.size() != 0) {
                //上传定损图片1
                for (int i = 0; i < reportPhotoUrlArr1.size() - 1; i++) {
                    if (!reportPhotoUrlArr1.get(i).contains("https")) {
                        File reportPhotoArr1File = new File(reportPhotoUrlArr1.get(i).substring(7));
                        bodyBuilder.addFormDataPart("one_b" + i, "one_b" + i + ".JPEG", RequestBody.create(MediaType.parse("image/jpeg"), reportPhotoArr1File));
                    }
                }
            }

            if (reportPhotoUrlArr2.size() != 0) {
                //上传定损图片2
                for (int i = 0; i < reportPhotoUrlArr2.size() - 1; i++) {
                    if (!reportPhotoUrlArr2.get(i).contains("https")) {
                        File reportPhotoArr2File = new File(reportPhotoUrlArr2.get(i).substring(7));
                        bodyBuilder.addFormDataPart("one_c" + i, "one_c" + i + ".JPEG", RequestBody.create(MediaType.parse("image/jpeg"), reportPhotoArr2File));
                    }
                }
            }

            if (wholeCarUrlArr.size() != 0) {
                //上传全车45度图片
                for (int i = 0; i < wholeCarUrlArr.size(); i++) {
                    if (!wholeCarUrlArr.get(i).contains("https")) {
                        File takeWholeCarPhoto0File = new File(wholeCarUrlArr.get(i).substring(7));
                        bodyBuilder.addFormDataPart("one_a", "one_a.JPEG", RequestBody.create(MediaType.parse("image/jpeg"), takeWholeCarPhoto0File));
                    }
                }
            }

            if (realVideoUrlArr.size() != 0) {
                //上传事故小视频
                for (int i = 0; i < realVideoUrlArr.size(); i++) {
                    if (!realVideoUrlArr.get(i).contains("https")) {
                        File realVideoNameArrFile = new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/" + realVideoUrlArr.get(i));
                        bodyBuilder.addFormDataPart("video" + i, "video" + i + ".mp4", RequestBody.create(MediaType.parse("video/mp4"), realVideoNameArrFile));
                    }
                }
            }

        }

        if (idOppositeUrl != null) {
            //上传身份证反面照片
            File IDOppositeImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDOpposite.png");
            bodyBuilder.addFormDataPart("license_b", "license_b.png", RequestBody.create(MediaType.parse("image/png"), IDOppositeImageFile));
        }

        if (idPositiveUrl != null) {
            //上传身份证正面照片
            File IDPositiveImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png");
            bodyBuilder.addFormDataPart("license_a", "license_a.png", RequestBody.create(MediaType.parse("image/png"), IDPositiveImageFile));
        }

        if (bankUrl != null) {
            //上传银行卡照片
            File bankImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/bank.png");
            bodyBuilder.addFormDataPart("card", "card.png", RequestBody.create(MediaType.parse("image/png"), bankImageFile));
        }

        if (drivingLicenseUrl != null) {
            //上传行驶证照片
            File drivingLicenseImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png");
            bodyBuilder.addFormDataPart("Permit", "Permit.png", RequestBody.create(MediaType.parse("image/png"), drivingLicenseImageFile));
        }

        if (driverLicenseUrl != null) {
            //上传驾驶证照片
            File driverLicenseImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png");
            bodyBuilder.addFormDataPart("drivelicense", "drivelicense.png", RequestBody.create(MediaType.parse("image/png"), driverLicenseImageFile));
        }

        if (frameNumberUrl != null) {
            //上传车架号照片
            File frameNumberImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png");
            bodyBuilder.addFormDataPart("frame", "frame.png", RequestBody.create(MediaType.parse("image/png"), frameNumberImageFile));
        }

        MultipartBody build = bodyBuilder.build();

        //注意onUIProgressFinish会执行两次，会造成一些错误，所以用trueFinish这个变量来解决
        RequestBody requestBody = ProgressHelper.withProgress(build, new ProgressUIListener() {

            @Override
            public void onUIProgressStart(long totalBytes) {
            }

            @Override
            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                if (trueFinish == 1) {
                    //设置进度
                    kProgressHUD.setProgress((int) (((double) (numBytes) / (double) (totalBytes)) * 100));
                }
            }

            @Override
            public void onUIProgressFinish() {
                //onUIProgressFinish会执行两次，第一次是错误的，第二次才是真正的完成
                if (trueFinish == 1) {
                    //设置为初始值
                    trueFinish = 0;
                }
                trueFinish += 1;
            }

        });
        builder.post(requestBody);

        Call call = okHttpClient.newCall(builder.build());

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //注意，这里有一个巨坑，要获得返回的json数据，一定要写成response.body().string()，不然返回的不是json数据
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONObject datas = jsonObject.getJSONObject("datas");
                    JSONArray attachs = datas.getJSONArray("attachs");

                    //分多车和单车两种情况
                    if (PreferenceUtil.getString("accidentkind", "").equals("多车")) {

                        for (int i = 0; i < attachs.length(); i++) {
                            JSONObject value = (JSONObject) attachs.get(i);
                            String filename = value.getString("filename");
                            String attachid = value.getString("attachid");

                            if (filename.contains("more_a")) {
                                Map<String, String> more_aMap = new HashMap<>();
                                more_aMap.put("attachid", attachid);
                                VLCApplication.more_a.add(more_aMap);
                            } else if (filename.contains("more_b")) {
                                Map<String, String> more_bMap = new HashMap<>();
                                more_bMap.put("attachid", attachid);
                                VLCApplication.more_b.add(more_bMap);
                            } else if (filename.contains("more_c")) {
                                Map<String, String> more_cMap = new HashMap<>();
                                more_cMap.put("attachid", attachid);
                                VLCApplication.more_c.add(more_cMap);
                            } else if (filename.contains("more_d")) {
                                Map<String, String> more_dMap = new HashMap<>();
                                more_dMap.put("attachid", attachid);
                                VLCApplication.more_d.add(more_dMap);
                            } else if (filename.contains("more_e")) {
                                Map<String, String> more_eMap = new HashMap<>();
                                more_eMap.put("attachid", attachid);
                                VLCApplication.more_e.add(more_eMap);
                            } else if (filename.contains("video")) {
                                Map<String, String> videoMap = new HashMap<>();
                                videoMap.put("attachid", attachid);
                                VLCApplication.video.add(videoMap);
                            }
                        }

                        //给casepictures集合赋值
                        VLCApplication.casepictures.put("more_a", VLCApplication.more_a);
                        VLCApplication.casepictures.put("more_b", VLCApplication.more_b);
                        VLCApplication.casepictures.put("more_c", VLCApplication.more_c);
                        VLCApplication.casepictures.put("more_d", VLCApplication.more_d);
                        VLCApplication.casepictures.put("more_e", VLCApplication.more_e);
                        VLCApplication.casepictures.put("video", VLCApplication.video);

                    } else {

                        for (int i = 0; i < attachs.length(); i++) {
                            JSONObject value = (JSONObject) attachs.get(i);
                            String filename = value.getString("filename");
                            String attachid = value.getString("attachid");

                            if (filename.contains("one_a")) {
                                Map<String, String> one_aMap = new HashMap<>();
                                one_aMap.put("attachid", attachid);
                                VLCApplication.one_a.add(one_aMap);
                            } else if (filename.contains("one_b")) {
                                Map<String, String> one_bMap = new HashMap<>();
                                one_bMap.put("attachid", attachid);
                                VLCApplication.one_b.add(one_bMap);
                            } else if (filename.contains("one_c")) {
                                Map<String, String> one_cMap = new HashMap<>();
                                one_cMap.put("attachid", attachid);
                                VLCApplication.one_c.add(one_cMap);
                            } else if (filename.contains("video")) {
                                Map<String, String> videoMap = new HashMap<>();
                                videoMap.put("attachid", attachid);
                                VLCApplication.video.add(videoMap);
                            }
                        }

                        //给casepictures集合赋值
                        VLCApplication.casepictures.put("one_a", VLCApplication.one_a);
                        VLCApplication.casepictures.put("one_b", VLCApplication.one_b);
                        VLCApplication.casepictures.put("one_c", VLCApplication.one_c);
                        VLCApplication.casepictures.put("video", VLCApplication.video);
                    }

                    for (int i = 0; i < attachs.length(); i++) {
                        JSONObject value = (JSONObject) attachs.get(i);
                        String filename = value.getString("filename");
                        String attachid = value.getString("attachid");

                        if (filename.contains("license_b")) {
                            Map<String, String> license_bMap = new HashMap<>();
                            license_bMap.put("attachid", attachid);
                            VLCApplication.license_b.add(license_bMap);
                        } else if (filename.contains("frame")) {
                            Map<String, String> frameMap = new HashMap<>();
                            frameMap.put("attachid", attachid);
                            VLCApplication.frame.add(frameMap);
                        } else if (filename.contains("license_a")) {
                            Map<String, String> license_aMap = new HashMap<>();
                            license_aMap.put("attachid", attachid);
                            VLCApplication.license_a.add(license_aMap);
                        } else if (filename.contains("Permit")) {
                            Map<String, String> permitMap = new HashMap<>();
                            permitMap.put("attachid", attachid);
                            VLCApplication.permit.add(permitMap);
                        } else if (filename.contains("card")) {
                            Map<String, String> cardMap = new HashMap<>();
                            cardMap.put("attachid", attachid);
                            VLCApplication.card.add(cardMap);
                        } else if (filename.contains("drivelicense")) {
                            Map<String, String> drivelicenseMap = new HashMap<>();
                            drivelicenseMap.put("attachid", attachid);
                            VLCApplication.drivelicense.add(drivelicenseMap);
                        }
                    }

                    //给casepictures集合赋值
                    VLCApplication.compensatepictures.put("license_b", VLCApplication.license_b);
                    VLCApplication.compensatepictures.put("frame", VLCApplication.frame);
                    VLCApplication.compensatepictures.put("license_a", VLCApplication.license_a);
                    VLCApplication.compensatepictures.put("permit", VLCApplication.permit);
                    VLCApplication.compensatepictures.put("card", VLCApplication.card);
                    VLCApplication.compensatepictures.put("drivelicense", VLCApplication.drivelicense);

                    //上传步骤2
                    uploadTap2();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * 上传步骤2
     */
    private void uploadTap2() {
        Map<String, Object> map = new HashMap<>();

        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));
        //设置照片数组
        Gson gson = new Gson();
        //把Map转为json字符串
        String jsonStr = gson.toJson(VLCApplication.casepictures);
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            map.put("casepictures", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.CASE_STEP_TWO_URL, jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {
                        isOneKeyReport = true;

                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);

                        getQiniuToken();
                        //上传步骤3
//                      uploadTap3();

                    } else {
                        Message message = new Message();
                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                        handler.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_NET_FAIL;
                handler.sendMessage(message);
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
     * 上传步骤3
     */
    private void uploadTap3() {
        Map<String, Object> map = new HashMap<>();

        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));
        //设置照片数组
        Gson gson = new Gson();
        //把Map转为json字符串
        String jsonStr = gson.toJson(VLCApplication.compensatepictures);
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            map.put("compensatepictures", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.CASE_STEP_THREE_URL, jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {

                        if (!PreferenceUtil.getString("isdemo", "0").equals("1")) {//如果不是模拟报案保存信息到本地
                            //保存个人信息到本地
                            if (PreferenceUtil.getString("driverName", "").equals("")) {
                                PreferenceUtil.commitString("driverName", informantName);
                            }
                            if (PreferenceUtil.getString("driverPhone", "").equals("")) {
                                PreferenceUtil.commitString("driverPhone", informantPhone);
                            }
                            PreferenceUtil.commitString("plateNumber", plateNumber);
                        }

                        if (VLCApplication.configsDictionary.get("app-c-170") != null) {
                            Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-c-170"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "以上信息已经为您成功上传", Toast.LENGTH_SHORT).show();
                        }
                        PreferenceUtil.commitString("examineState", "事故照片正在审核中");

                        Message message = new Message();
                        message.what = HANDLER_MESSAGE_UPLOAD_SUCCESS;
                        handler.sendMessage(message);
                    } else {
                        Message message = new Message();
                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                        handler.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_NET_FAIL;
                handler.sendMessage(message);
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
     * 撤销
     */
    private void revoke() {
        Map<String, String> map = new HashMap<>();

        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));

        showProgress("拼命加载中...");
        //设置撤回按钮不能点击
        payment_middle_btn.setEnabled(false);

        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.CASE_CANCEL_URL, jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    Message message = new Message();
                    message.what = HANDLER_MESSAGE_REWOKE_SUCCESS;
                    handler.sendMessage(message);
                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {
                        if (VLCApplication.configsDictionary.get("app-c-205") != null) {
                            Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-c-205"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "已经为您成功销案", Toast.LENGTH_SHORT).show();
                        }
                        //退出当前视图
                        getActivity().finish();
                        //发送本地广播
                        Intent intent = new Intent("refreshReportList");
                        localBroadcastManager.sendBroadcast(intent);
                    } else {
                        Message message1 = new Message();
                        message1.what = HANDLER_MESSAGE_REWOKE_SUCCESS;
                        handler.sendMessage(message1);
                        if (VLCApplication.configsDictionary.get("app-c-206") != null) {
                            Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-c-206"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "未销案成功，请稍后再试", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    //设置撤回按钮能点击
                    Message message = new Message();
                    message.what = HANDLER_MESSAGE_REWOKE_SUCCESS;
                    handler.sendMessage(message);
                    if (VLCApplication.configsDictionary.get("app-c-206") != null) {
                        Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-c-206"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "未销案成功，请稍后再试", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                //设置撤回按钮能点击
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_NET_FAIL;
                handler.sendMessage(message);
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
     * 拿到报案页面的内容
     */
    private void getReportField() {
        //报案时间
        reportTime = PreferenceUtil.getString("text_casedate", "");
        //事故日期时间
        accidentTime = PreferenceUtil.getString("text_accidentdate", "");
        //出险地点
        geographicalPosition = PreferenceUtil.getString("text_accidentaddress", "");
        //是否事故现场
        isAccidentScene = PreferenceUtil.getString("text_accidentscene", "");
        //车牌号
        plateNumber = PreferenceUtil.getString("text_carnumber", "");
        //报案人联系方式
        informantPhone = PreferenceUtil.getString("text_mobile", "");
        //报案人
        informantName = PreferenceUtil.getString("text_username", "");
        //事故类型
        accidentType = PreferenceUtil.getString("text_accidentkind", "");
        //事故责任
        accidentResponsibility = PreferenceUtil.getString("text_accident", "");
        //是否有人伤
        isWounded = PreferenceUtil.getString("text_personinjure", "");
        //车辆能否正常行驶
        isNormalDriving = PreferenceUtil.getString("text_carcanmove", "");
    }

    /**
     * 判断一键上传时 字段是否为空
     *
     * @return false 有字段为空  true没字段为空
     */
    private boolean isReportFieldFull() {
        // 出险地点
        if (geographicalPosition == null || "".equals(geographicalPosition)) {
            ToastUtils.showNomalShortToast(getActivity(), getString(R.string.report_start_position_is_null));
            return false;
            //车牌号
        } else if (plateNumber == null || "".equals(plateNumber)) {
            ToastUtils.showNomalShortToast(getActivity(), getString(R.string.report_start_plateNumber_is_null));
            return false;
            //报案人
        } else if (informantName == null || "".equals(informantName)) {
            ToastUtils.showNomalShortToast(getActivity(), getString(R.string.report_start_informantName_is_null));
            return false;
            //报案人联系方式
        } else if (informantPhone == null || "".equals(informantPhone)) {
            ToastUtils.showNomalShortToast(getActivity(), getString(R.string.report_start_informantPhone_is_null));
            return false;
            //事故类型
        } else if (accidentType == null || "".equals(accidentType)) {
            ToastUtils.showNomalShortToast(getActivity(), getString(R.string.report_start_accidentType_is_null));
            return false;
            //事故责任
        } else if ("多车".equals(accidentType) && (accidentResponsibility == null || "".equals(accidentResponsibility))) {
            ToastUtils.showNomalShortToast(getActivity(), getString(R.string.report_start_accidentResponsibility_is_null));
            return false;
            //是否有人受伤
        } else if (isWounded == null || "".equals(isWounded)) {
            ToastUtils.showNomalShortToast(getActivity(), getString(R.string.report_start_isWounded_is_null));
            return false;
            //是否能正常行驶
        } else if (isNormalDriving == null || "".equals(isNormalDriving)) {
            ToastUtils.showNomalShortToast(getActivity(), getString(R.string.report_start_isNormalDriving_is_null));
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断所有照片是否全有照片
     *
     * @return true是 / false否
     */
    private boolean isFieldFull() {


        if (switchIDOpposite.getVisibility() != View.VISIBLE) {
            ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传身份证反面照片");
            return false;
        } else if (switchIDPositive.getVisibility() != View.VISIBLE) {
            ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传身份证正面照片");
            return false;
        } else if (switchBank.getVisibility() != View.VISIBLE) {
            ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传银行卡照片");
            return false;
        } else if (switchDrivingLicense.getVisibility() != View.VISIBLE) {
            ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传行驶证照片");
            return false;
        } else if (switchDriverLicense.getVisibility() != View.VISIBLE) {
            ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传驾驶证照片");
            return false;
        } else if (switchFrameNumber.getVisibility() != View.VISIBLE) {
            ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传车架号照片");
            return false;
        } else {
            return true;
        }
    }

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label, int maxProgress) {
        kProgressHUD = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.ANNULAR_DETERMINATE)
                .setLabel(label)
                .setMaxProgress(maxProgress)
                .show();
    }

    private void showProgress(String label) {
        kProgressHUD = KProgressHUD.create(getContext());
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
                .btnTextColor(new int[]{ContextCompat.getColor(getActivity(), R.color.primary), ContextCompat.getColor(getActivity(), R.color.alphablack)})
                .show();
        dialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void loadData() {

    }

    /**
     * 判断是否全拍照了   是 true/false 否
     *
     * @return
     */
    private boolean isPhotoFieldFull() {

        File takeWholeCarPhoto0File = null;
        try {
            takeWholeCarPhoto0File = new File(PreferenceUtil.getString("takeWholeCarPhoto0", "").substring(7));
        } catch (Exception e) {
            ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传自己车带牌照照片");
            return false;
        }

        if (PreferenceUtil.getString("accidentkind", "").equals("多车")) {
            if (wholeCarUrlArr.size() <= 0) {
                ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传自己车带牌照照片");
                return false;
            } else if (reportPhotoUrlArr1.size() - 1 <= 0) {
                ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传撞击部位45度照片");
                return false;
            } else if (reportPhotoUrlArr2.size() - 1 <= 0) {
                ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传对方车带牌照照片");
                return false;
            } else if (reportPhotoUrlArr4.size() - 1 <= 0) {
                ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传自己车受损部位照片");
                return false;
            } else if (reportPhotoUrlArr5.size() - 1 <= 0) {
                ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传对方车受损部位照片");
                return false;
            } else if (takeWholeCarPhoto0File != null && !takeWholeCarPhoto0File.exists()) {
                ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传自己车带牌照照片");
                return false;
            } else {
                return true;
            }
        } else {
            if (reportPhotoUrlArr1.size() - 1 <= 0) {
                ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传被撞物体照片");
                return false;
            } else if (wholeCarUrlArr.size() <= 0) {
                ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传全车45度带车牌照片");
                return false;
            } else if (reportPhotoUrlArr2.size() - 1 <= 0) {
                ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传受损部位照片");
                return false;
            } else if (takeWholeCarPhoto0File != null && !takeWholeCarPhoto0File.exists()) {
                ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传全车45度带车牌照片");
                return false;
            } else {
                return true;
            }
        }

    }

    /**
     * 单车的一键上传
     */
    private void getQiniuTokenForOneCar() {

        JSONObject jsonObject = new JSONObject();
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.QINIU_TOKEN + "?n=" + onCarCount, jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    if (200 == jsonObject.getInt("code")) {
                        baseUrl = jsonObject.getJSONObject("data").getString("baseUrl");
                        qiniuToken = jsonObject.getJSONObject("data").getString("token");
                        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("fileName");
                        arrReport1.clear();
                        arrReport2.clear();

                        for (int i = 0; i < reportPhotoUrlArr1.size() - 1; i++) {
                            arrReport1.add(jsonArray.get(i).toString());
                        }
                        for (int j = reportPhotoUrlArr1.size() - 1; j < reportPhotoUrlArr2.size() - 1 + reportPhotoUrlArr1.size() - 1; j++) {
                            arrReport2.add(jsonArray.get(j).toString());
                        }

                        if (realVideoUrlArr.size() == 0) {
                            strWholeCar = jsonArray.get(jsonArray.length() - 1).toString();
                            strVideo = "";
                        } else {
                            strWholeCar = jsonArray.get(jsonArray.length() - 2).toString();
                            strVideo = jsonArray.get(jsonArray.length() - 1).toString();
                        }

                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);

                        upLoadReportArr1ForOneCar(arrReport1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_NET_FAIL;
                handler.sendMessage(message);
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
     * 上传单车定损图1
     *
     * @param list
     */
    private void upLoadReportArr1ForOneCar(final ArrayList<String> list) {
        if (list == null) {
            return;
        }
        VLCApplication.casepictures.clear();
        VLCApplication.one_b.clear();
        for (int i = 0; i < list.size(); i++) {

            File reportPhotoArr1File = new File(reportPhotoUrlArr1.get(i).substring(7));
            if (reportPhotoArr1File.exists()) {
                VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                    try {
                                        String attachid = res.getString("key");
                                        // 获取图片的url
                                        String url = Config.QINIU_BASE_URL + attachid;
                                        Map<String, String> one_bMap = new HashMap<>();
                                        one_bMap.put("attachid", attachid);
                                        VLCApplication.one_b.add(one_bMap);

                                        currentProgress += 1;
                                        kProgressHUD.setProgress(currentProgress);

                                        if (VLCApplication.one_b.size() == list.size()) {//more_a上传完毕
                                            VLCApplication.casepictures.put("one_b", VLCApplication.one_b);
                                            uploadReportArr2ForOneCar(arrReport2);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            }
                        }, null);
            } else {
                Map<String, String> one_bMap = new HashMap<>();
                one_bMap.put("attachid", reportPhotoUrlArr1.get(i).substring(33));
                VLCApplication.one_b.add(one_bMap);

                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);

                if (VLCApplication.one_b.size() == list.size()) {//more_a上传完毕
                    VLCApplication.casepictures.put("one_b", VLCApplication.one_b);
                    uploadReportArr2ForOneCar(arrReport2);
                }
            }
        }
    }

    /**
     * 上传单车定损图2
     *
     * @param list
     */
    private void uploadReportArr2ForOneCar(final ArrayList<String> list) {
        if (list == null) {
            return;
        }
        VLCApplication.one_c.clear();
        for (int i = 0; i < list.size(); i++) {
            File reportPhotoArr1File = new File(reportPhotoUrlArr2.get(i).substring(7));
            if (reportPhotoArr1File.exists()) {
                VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                    try {
                                        String attachid = res.getString("key");
                                        // 获取图片的url
                                        String url = Config.QINIU_BASE_URL + attachid;
                                        Map<String, String> one_cMap = new HashMap<>();
                                        one_cMap.put("attachid", attachid);
                                        VLCApplication.one_c.add(one_cMap);

                                        currentProgress += 1;
                                        kProgressHUD.setProgress(currentProgress);

                                        if (VLCApplication.one_c.size() == list.size()) {//more_a上传完毕
                                            VLCApplication.casepictures.put("one_c", VLCApplication.one_c);
                                            uploadWholeForOneCar(strWholeCar);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            }
                        }, null);
            } else {
                Map<String, String> one_cMap = new HashMap<>();
                one_cMap.put("attachid", reportPhotoUrlArr2.get(i).substring(33));
                VLCApplication.one_c.add(one_cMap);

                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);

                if (VLCApplication.one_c.size() == list.size()) {//more_a上传完毕
                    VLCApplication.casepictures.put("one_c", VLCApplication.one_c);
                    uploadWholeForOneCar(strWholeCar);
                }
            }
        }
    }

    /**
     * 上传全车照
     *
     * @param fileName
     */
    private void uploadWholeForOneCar(String fileName) {
        VLCApplication.one_a.clear();
//        File takeWholeCarPhoto0File = new File(PreferenceUtil.getString("takeWholeCarPhoto0", "").substring(7));
        File takeWholeCarPhoto0File = new File(wholeCarUrlArr.get(0).substring(7));
        if (takeWholeCarPhoto0File.exists()) {
            VLCApplication.uploadManager.put(takeWholeCarPhoto0File, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    String url = Config.QINIU_BASE_URL + attachid;

                                    Map<String, String> one_aMap = new HashMap<>();
                                    one_aMap.put("attachid", attachid);

                                    VLCApplication.one_a.add(one_aMap);
                                    VLCApplication.casepictures.put("one_a", VLCApplication.one_a);

                                    currentProgress += 1;
                                    kProgressHUD.setProgress(currentProgress);

                                    uploadVieo(strVideo);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                Message message = new Message();
                                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                handler.sendMessage(message);
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
            Map<String, String> one_aMap = new HashMap<>();
            one_aMap.put("attachid", wholeCarUrlArr.get(0).substring(33));
            VLCApplication.one_a.add(one_aMap);
            VLCApplication.casepictures.put("one_a", VLCApplication.one_a);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            uploadVieo(strVideo);
        }
    }

    /**
     * 一键上传
     */
    private void getQiniuTokenForMoreCar() {

        JSONObject jsonObject = new JSONObject();
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.QINIU_TOKEN + "?n=" + moreCarCount, jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    if (200 == jsonObject.getInt("code")) {
                        baseUrl = jsonObject.getJSONObject("data").getString("baseUrl");
                        qiniuToken = jsonObject.getJSONObject("data").getString("token");
                        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("fileName");

                        arrReport1.clear();
                        arrReport2.clear();
                        arrReport4.clear();
                        arrReport5.clear();

                        for (int i = 0; i < reportPhotoUrlArr1.size() - 1; i++) {
                            arrReport1.add(jsonArray.get(i).toString());
                        }
                        for (int j = reportPhotoUrlArr1.size() - 1; j < reportPhotoUrlArr2.size() - 1 + reportPhotoUrlArr1.size() - 1; j++) {
                            arrReport2.add(jsonArray.get(j).toString());
                        }
                        for (int k = reportPhotoUrlArr2.size() + reportPhotoUrlArr1.size() - 2; k < reportPhotoUrlArr2.size() + reportPhotoUrlArr1.size() + reportPhotoUrlArr4.size() - 3; k++) {
                            arrReport4.add(jsonArray.get(k).toString());
                        }

                        for (int l = reportPhotoUrlArr2.size() + reportPhotoUrlArr1.size() + reportPhotoUrlArr4.size() - 3;
                             l < reportPhotoUrlArr2.size() + reportPhotoUrlArr1.size() + reportPhotoUrlArr4.size() + reportPhotoUrlArr5.size() - 4; l++) {
                            arrReport5.add(jsonArray.get(l).toString());
                        }
                        if (realVideoUrlArr.size() == 0) {
                            strWholeCar = jsonArray.get(jsonArray.length() - 1).toString();
                            strVideo = "";
                        } else {
                            strWholeCar = jsonArray.get(jsonArray.length() - 2).toString();
                            strVideo = jsonArray.get(jsonArray.length() - 1).toString();
                        }

                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);

                        upLoadReportArr1(arrReport1);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_NET_FAIL;
                handler.sendMessage(message);

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
     * 上传多车定损图片 Arr1
     */
    private void upLoadReportArr1(final ArrayList<String> list) {
        if (list == null) {
            return;
        }
        VLCApplication.casepictures.clear();
        VLCApplication.more_a.clear();
        for (int i = 0; i < list.size(); i++) {

            File reportPhotoArr1File = new File(reportPhotoUrlArr1.get(i).substring(7));
            if (reportPhotoArr1File.exists()) {
                VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                    try {
                                        String attachid = res.getString("key");
                                        // 获取图片的url
                                        String url = Config.QINIU_BASE_URL + attachid;
                                        Map<String, String> more_aMap = new HashMap<>();
                                        more_aMap.put("attachid", attachid);
                                        VLCApplication.more_a.add(more_aMap);

                                        currentProgress += 1;
                                        kProgressHUD.setProgress(currentProgress);

                                        if (VLCApplication.more_a.size() == list.size()) {//more_a上传完毕
                                            VLCApplication.casepictures.put("more_a", VLCApplication.more_a);
                                            uploadReportArr2(arrReport2);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            }
                        }, null);
            } else {
                Map<String, String> more_aMap = new HashMap<>();
                more_aMap.put("attachid", reportPhotoUrlArr1.get(i).substring(33));
                VLCApplication.more_a.add(more_aMap);

                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);

                if (VLCApplication.more_a.size() == list.size()) {//more_a上传完毕
                    VLCApplication.casepictures.put("more_a", VLCApplication.more_a);
                    uploadReportArr2(arrReport2);
                }
            }
        }

    }

    /**
     * 上传多车定损图片 Arr2
     */
    private void uploadReportArr2(final ArrayList<String> list) {
        if (list == null) {
            return;
        }
        VLCApplication.more_c.clear();
        for (int i = 0; i < list.size(); i++) {
            File reportPhotoArr1File = new File(reportPhotoUrlArr2.get(i).substring(7));
            if (reportPhotoArr1File.exists()) {
                VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                    try {
                                        String attachid = res.getString("key");
                                        // 获取图片的url
                                        String url = Config.QINIU_BASE_URL + attachid;
                                        Map<String, String> more_cMap = new HashMap<>();
                                        more_cMap.put("attachid", attachid);
                                        VLCApplication.more_c.add(more_cMap);

                                        currentProgress += 1;
                                        kProgressHUD.setProgress(currentProgress);

                                        if (VLCApplication.more_c.size() == list.size()) {//more_a上传完毕
                                            VLCApplication.casepictures.put("more_c", VLCApplication.more_c);
                                            uploadReportArr4(arrReport4);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            }
                        }, null);
            } else {
                Map<String, String> more_cMap = new HashMap<>();
                more_cMap.put("attachid", reportPhotoUrlArr2.get(i).substring(33));
                VLCApplication.more_c.add(more_cMap);

                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);

                if (VLCApplication.more_c.size() == list.size()) {//more_a上传完毕
                    VLCApplication.casepictures.put("more_c", VLCApplication.more_c);
                    uploadReportArr4(arrReport4);
                }
            }
        }
    }

    /**
     * 上传多车定损图片 Arr4
     */
    private void uploadReportArr4(final ArrayList<String> list) {
        if (list == null) {
            return;
        }
        VLCApplication.more_d.clear();
        for (int i = 0; i < list.size(); i++) {
            File reportPhotoArr1File = new File(reportPhotoUrlArr4.get(i).substring(7));
            if (reportPhotoArr1File.exists()) {
                VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                    try {
                                        String attachid = res.getString("key");
                                        // 获取图片的url
                                        String url = Config.QINIU_BASE_URL + attachid;
                                        Map<String, String> more_dMap = new HashMap<>();
                                        more_dMap.put("attachid", attachid);
                                        VLCApplication.more_d.add(more_dMap);

                                        currentProgress += 1;
                                        kProgressHUD.setProgress(currentProgress);

                                        if (VLCApplication.more_d.size() == list.size()) {//more_d上传完毕
                                            VLCApplication.casepictures.put("more_d", VLCApplication.more_d);
                                            uploadReportArr5(arrReport5);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            }
                        }, null);
            } else {
                Map<String, String> more_dMap = new HashMap<>();
                more_dMap.put("attachid", reportPhotoUrlArr4.get(i).substring(33));
                VLCApplication.more_d.add(more_dMap);

                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);

                if (VLCApplication.more_d.size() == list.size()) {//more_d上传完毕
                    VLCApplication.casepictures.put("more_d", VLCApplication.more_d);
                    uploadReportArr5(arrReport5);
                }
            }
        }

    }

    /**
     * 上传多车定损图片 Arr5
     */
    private void uploadReportArr5(final ArrayList<String> list) {
        if (list == null) {
            return;
        }
        VLCApplication.more_e.clear();
        for (int i = 0; i < list.size(); i++) {
            File reportPhotoArr1File = new File(reportPhotoUrlArr5.get(i).substring(7));
            if (reportPhotoArr1File.exists()) {
                VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                    try {
                                        String attachid = res.getString("key");
                                        // 获取图片的url
                                        String url = Config.QINIU_BASE_URL + attachid;
                                        Map<String, String> more_eMap = new HashMap<>();
                                        more_eMap.put("attachid", attachid);
                                        VLCApplication.more_e.add(more_eMap);

                                        currentProgress += 1;
                                        kProgressHUD.setProgress(currentProgress);

                                        if (VLCApplication.more_e.size() == list.size()) {//more_e上传完毕
                                            VLCApplication.casepictures.put("more_e", VLCApplication.more_e);
                                            uploadWholeCar(strWholeCar);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            }
                        }, null);

            } else {
                Map<String, String> more_eMap = new HashMap<>();
                more_eMap.put("attachid", reportPhotoUrlArr5.get(i).substring(33));
                VLCApplication.more_e.add(more_eMap);

                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);

                if (VLCApplication.more_e.size() == list.size()) {//more_e上传完毕
                    VLCApplication.casepictures.put("more_e", VLCApplication.more_e);
                    uploadWholeCar(strWholeCar);
                }
            }
        }

    }

    /**
     * 上传全车照
     *
     * @param fileName
     */
    private void uploadWholeCar(String fileName) {

//        File takeWholeCarPhoto0File = new File(PreferenceUtil.getString("takeWholeCarPhoto0", "").substring(7));
        File takeWholeCarPhoto0File = new File(wholeCarUrlArr.get(0).substring(7));
        VLCApplication.more_b.clear();
        if (takeWholeCarPhoto0File.exists()) {
            VLCApplication.uploadManager.put(takeWholeCarPhoto0File, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    String url = Config.QINIU_BASE_URL + attachid;

                                    Map<String, String> license_bMap = new HashMap<>();
                                    license_bMap.put("attachid", attachid);

                                    VLCApplication.more_b.add(license_bMap);
                                    VLCApplication.casepictures.put("more_b", VLCApplication.more_b);

                                    currentProgress += 1;
                                    kProgressHUD.setProgress(currentProgress);

                                    uploadVieo(strVideo);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                Message message = new Message();
                                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                handler.sendMessage(message);
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
            Map<String, String> more_bMap = new HashMap<>();
            more_bMap.put("attachid", wholeCarUrlArr.get(0).substring(33));
            VLCApplication.more_b.add(more_bMap);
            VLCApplication.casepictures.put("more_b", VLCApplication.more_b);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            uploadVieo(strVideo);
        }
    }

    /**
     * 上传小视频
     *
     * @param fileName
     */
    private void uploadVieo(String fileName) {
        VLCApplication.video.clear();

        if (realVideoUrlArr == null || realVideoUrlArr.size() == 0) {
//            Map<String, String> videoMap = new HashMap<>();
//                videoMap.put("attachid", "");

//            VLCApplication.video.add(videoMap);
            VLCApplication.casepictures.put("video", VLCApplication.video);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            uploadTap2();
        } else {
            File realVideoNameArrFile = new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/" + realVideoUrlArr.get(0));

            if (realVideoNameArrFile.exists()) {
                VLCApplication.uploadManager.put(realVideoNameArrFile, fileName + ".mp4", qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                    try {
                                        String attachid = res.getString("key");
                                        // 获取图片的url
                                        String url = Config.QINIU_BASE_URL + attachid;

                                        Map<String, String> videoMap = new HashMap<>();
                                        videoMap.put("attachid", attachid);

                                        VLCApplication.video.add(videoMap);
                                        VLCApplication.casepictures.put("video", VLCApplication.video);

                                        currentProgress += 1;
                                        kProgressHUD.setProgress(currentProgress);

                                        uploadTap2();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            }
                        }, null);
            } else {
                Map<String, String> videoMap = new HashMap<>();
                videoMap.put("attachid", realVideoUrlArr.get(0).substring(33));

                VLCApplication.video.add(videoMap);
                VLCApplication.casepictures.put("video", VLCApplication.video);

                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);

                uploadTap2();
            }
        }
    }

    /**
     * 获取七牛token
     */
    private void getQiniuToken() {
        JSONObject jsonObject = new JSONObject();
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.QINIU_TOKEN + "?n=6", jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    if (200 == jsonObject.getInt("code")) {
                        baseUrl = jsonObject.getJSONObject("data").getString("baseUrl");
                        qiniuToken = jsonObject.getJSONObject("data").getString("token");
                        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("fileName");

                        arrPayment.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            arrPayment.add(jsonArray.get(i).toString());
                        }

                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);

                        upLoadIdOpposite(arrPayment.get(0));
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
     * 上传身份证反面照片
     *
     * @param fileName
     */
    private void upLoadIdOpposite(String fileName) {

        VLCApplication.license_b.clear();
        VLCApplication.compensatepictures.clear();
        //上传身份证反面照片
        File IDOppositeImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDOpposite.png");
        if (IDOppositeImageFile.exists()) {
            VLCApplication.uploadManager.put(IDOppositeImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    String url = Config.QINIU_BASE_URL + attachid;

                                    Map<String, String> license_bMap = new HashMap<>();
                                    license_bMap.put("attachid", attachid);

                                    VLCApplication.license_b.add(license_bMap);
                                    VLCApplication.compensatepictures.put("license_b", VLCApplication.license_b);

                                    currentProgress += 1;
                                    kProgressHUD.setProgress(currentProgress);

                                    upLoadIdPositive(arrPayment.get(1));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                Message message = new Message();
                                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                handler.sendMessage(message);
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
//            Map<String, String> license_bMap = new HashMap<>();
//            license_bMap.put("attachid", "");

//            VLCApplication.license_b.add(license_bMap);
            VLCApplication.compensatepictures.put("license_b", VLCApplication.license_b);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            upLoadIdPositive(arrPayment.get(1));
        }
    }

    /**
     * 上传身份证正面照片
     *
     * @param fileName
     */
    private void upLoadIdPositive(String fileName) {

        VLCApplication.license_a.clear();
        //上传身份证正面照片
        File IDPositiveImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png");
        if (IDPositiveImageFile.exists()) {

            VLCApplication.uploadManager.put(IDPositiveImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    String url = Config.QINIU_BASE_URL + attachid;

                                    Map<String, String> license_bMap = new HashMap<>();
                                    license_bMap.put("attachid", attachid);

                                    VLCApplication.license_a.add(license_bMap);
                                    VLCApplication.compensatepictures.put("license_a", VLCApplication.license_a);

                                    currentProgress += 1;
                                    kProgressHUD.setProgress(currentProgress);

                                    upLoadBank(arrPayment.get(2));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                Message message = new Message();
                                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                handler.sendMessage(message);
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
//            Map<String, String> license_bMap = new HashMap<>();
//            license_bMap.put("attachid", "");

//            VLCApplication.license_a.add(license_bMap);
            VLCApplication.compensatepictures.put("license_a", VLCApplication.license_a);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            upLoadBank(arrPayment.get(2));
        }

    }

    /**
     * 上传银行卡照片
     *
     * @param fileName
     */
    private void upLoadBank(String fileName) {

        VLCApplication.card.clear();
        //上传银行卡照片
        File bankImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/bank.png");
        if (bankImageFile.exists()) {
            VLCApplication.uploadManager.put(bankImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    String url = Config.QINIU_BASE_URL + attachid;

                                    Map<String, String> license_bMap = new HashMap<>();
                                    license_bMap.put("attachid", attachid);

                                    VLCApplication.card.add(license_bMap);
                                    VLCApplication.compensatepictures.put("card", VLCApplication.card);

                                    currentProgress += 1;
                                    kProgressHUD.setProgress(currentProgress);

                                    upLoadDrivingLicense(arrPayment.get(3));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                Message message = new Message();
                                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                handler.sendMessage(message);
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
//            Map<String, String> license_bMap = new HashMap<>();
//            license_bMap.put("attachid", "");

//            VLCApplication.card.add(license_bMap);
            VLCApplication.compensatepictures.put("card", VLCApplication.card);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            upLoadDrivingLicense(arrPayment.get(3));
        }
    }

    /**
     * 上传行驶证照片
     *
     * @param fileName
     */
    private void upLoadDrivingLicense(String fileName) {

        VLCApplication.permit.clear();
        //上传行驶证照片
        File drivingLicenseImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png");
        if (drivingLicenseImageFile.exists()) {
            VLCApplication.uploadManager.put(drivingLicenseImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    String url = Config.QINIU_BASE_URL + attachid;

                                    Map<String, String> license_bMap = new HashMap<>();
                                    license_bMap.put("attachid", attachid);

                                    VLCApplication.permit.add(license_bMap);
                                    VLCApplication.compensatepictures.put("permit", VLCApplication.permit);

                                    currentProgress += 1;
                                    kProgressHUD.setProgress(currentProgress);

                                    upLoadDriverLicense(arrPayment.get(4));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                Message message = new Message();
                                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                handler.sendMessage(message);
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
//            Map<String, String> license_bMap = new HashMap<>();
//            license_bMap.put("attachid", "");

//            VLCApplication.permit.add(license_bMap);
            VLCApplication.compensatepictures.put("permit", VLCApplication.permit);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            upLoadDriverLicense(arrPayment.get(4));
        }
    }

    /**
     * 上传驾驶证照片
     *
     * @param fileName
     */
    private void upLoadDriverLicense(String fileName) {

        VLCApplication.drivelicense.clear();
        //上传驾驶证照片
        File driverLicenseImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png");
        if (driverLicenseImageFile.exists()) {
            VLCApplication.uploadManager.put(driverLicenseImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    String url = Config.QINIU_BASE_URL + attachid;

                                    Map<String, String> license_bMap = new HashMap<>();
                                    license_bMap.put("attachid", attachid);

                                    VLCApplication.drivelicense.add(license_bMap);
                                    VLCApplication.compensatepictures.put("drivelicense", VLCApplication.drivelicense);

                                    currentProgress += 1;
                                    kProgressHUD.setProgress(currentProgress);

                                    upLoadFrameNumber(arrPayment.get(5));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                Message message = new Message();
                                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                handler.sendMessage(message);
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
//            Map<String, String> license_bMap = new HashMap<>();
//            license_bMap.put("attachid", "");

//            VLCApplication.drivelicense.add(license_bMap);
            VLCApplication.compensatepictures.put("drivelicense", VLCApplication.drivelicense);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            upLoadFrameNumber(arrPayment.get(5));
        }
    }

    /**
     * 上传车架号照片
     *
     * @param fileName
     */
    private void upLoadFrameNumber(String fileName) {

        VLCApplication.frame.clear();
        //上传车架号照片
        File frameNumberImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png");
        if (frameNumberImageFile.exists()) {
            VLCApplication.uploadManager.put(frameNumberImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    String url = Config.QINIU_BASE_URL + attachid;

                                    Map<String, String> license_bMap = new HashMap<>();
                                    license_bMap.put("attachid", attachid);

                                    VLCApplication.frame.add(license_bMap);
                                    VLCApplication.compensatepictures.put("frame", VLCApplication.frame);

                                    currentProgress += 1;
                                    kProgressHUD.setProgress(currentProgress);

                                    oneKeyPayment2();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                Message message = new Message();
                                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                handler.sendMessage(message);
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
//            Map<String, String> license_bMap = new HashMap<>();
//            license_bMap.put("attachid", "");

//            VLCApplication.frame.add(license_bMap);
            VLCApplication.compensatepictures.put("frame", VLCApplication.frame);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            oneKeyPayment2();
        }
    }

    /**
     * 调用发送短信接口
     *
     * @param caseid
     */
    private void sendSMS(String caseid, int type) {
        Map<String, String> map = new HashMap<>();
        JSONObject jsonObject = new JSONObject(map);

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, Config.BASE_URL + "/ins/api/sendsmstip?caseId=" + caseid + "&type=" + type, jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {

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
}
