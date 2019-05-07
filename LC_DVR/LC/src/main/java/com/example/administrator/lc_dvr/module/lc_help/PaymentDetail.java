package com.example.administrator.lc_dvr.module.lc_help;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.bean.CasePayment;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.lc_report.LookPhoto;
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

import io.vov.vitamio.utils.Log;
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
public class PaymentDetail extends BaseActivity implements View.OnClickListener {

    private final int HANDLER_MESSAGE_UPLOAD_SUCCESS = 1;
    private final int HANDLER_MESSAGE_REWOKE_SUCCESS = 2;
    private final int HANDLER_MESSAGE_UPLOAD_FAIL = 3;
    private final int HANDLER_MESSAGE_UPLOAD_NET_FAIL = 4;

    private final int MAX_UPLOAD_PAYMENT = 8;// 理赔上传时的数

    private RadioButton rb_back; // 左上角返回键
    private Button btn_payment_upload; // 一键上传

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

    private RelativeLayout rl_bottom;
    //判断是点击了那张图片进入的图片查看界面
    private String lookImage = "";


    private String license_bUrl;
    private String license_aUrl;
    private String cardUrl;
    private String drivelicenseUrl;
    private String permitUrl;
    private String frameUrl;

    //判断是点击了那张图片
    private String whichImage = "";
    private NormalDialog dialog;
    private String[] mStringBts;

    private KProgressHUD kProgressHUD;
    private String baseUrl;
    private String qiniuToken;
    private ArrayList<String> arrPayment;
    private int currentProgress;

    private TextView tv_assessment_date; // 定损日期时间
    private TextView tv_assessment_money; // 定损金额
    private TextView tv_close_date; // 结案日期
    private TextView tv_close_money; // 结案金额

    private boolean isEdit; // （（|报案状态|非空 同时<>已销案） 或者 （|定损状态|非空 同时<>已销案）） 同时  |理赔状态|<>已结案 时 ，才可编辑

    private LinearLayout ll_success; // 定损成功
    private LinearLayout ll_failure; // 定损失败
    private LinearLayout ll_dropped; // 销案

    private TextView tv_assessment_date_failure; // 失败定损日期时间
    private TextView tv_assessment_money_failure; // 定损失败原因类型
    private TextView tv_close_date_failure; // 定损失败原因

    private TextView tv_assessment_date_dropped; // 销案时间
    private TextView tv_assessment_money_dropped; // 销案原因类型
    private TextView tv_close_date_dropped; // 销案原因

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
                    break;
                case HANDLER_MESSAGE_REWOKE_SUCCESS:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    break;
                case HANDLER_MESSAGE_UPLOAD_FAIL:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    if (VLCApplication.configsDictionary.get("app-c-171") != null) {
                        Toast.makeText(PaymentDetail.this, VLCApplication.configsDictionary.get("app-c-171"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PaymentDetail.this, "未上传成功，请稍后再试", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case HANDLER_MESSAGE_UPLOAD_NET_FAIL:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    if (VLCApplication.configsDictionary.get("app-z-010") != null) {
                        Toast.makeText(PaymentDetail.this, VLCApplication.configsDictionary.get("app-z-010"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PaymentDetail.this, "连接失败，请检查您的网络连接", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (PreferenceUtil.getBoolean("isDeleteImage", false)) {
            switch (lookImage) {
                case "IDOpposite":
                    //赋值，图片视图显示图片
                    IDOpposite.setImageResource(R.mipmap.id_opposite);
                    //隐藏switchIDOppositeImage
                    switchIDOpposite.setVisibility(View.GONE);
                    license_bUrl = "";
                    VLCApplication.license_b.clear();
                    break;
                case "IDPositive":
                    //赋值，图片视图显示图片
                    IDPositive.setImageResource(R.mipmap.id_positive);
                    //隐藏switchIDPositiveImage
                    switchIDPositive.setVisibility(View.GONE);
                    license_aUrl = "";
                    VLCApplication.license_a.clear();
                    break;
                case "bank":
                    //赋值，图片视图显示图片
                    bank.setImageResource(R.mipmap.bank);
                    //隐藏switchBankImage
                    switchBank.setVisibility(View.GONE);
                    cardUrl = "";
                    VLCApplication.card.clear();
                    break;
                case "drivingLicense":
                    //赋值，图片视图显示图片
                    drivingLicense.setImageResource(R.mipmap.driving_license);
                    //隐藏switchDrivingLicenseImage
                    switchDrivingLicense.setVisibility(View.GONE);
                    permitUrl = "";
                    VLCApplication.permit.clear();
                    break;
                case "frameNumber":
                    //赋值，图片视图显示图片
                    frameNumber.setImageResource(R.mipmap.frame_number);
                    //隐藏switchFrameNumberImage
                    switchFrameNumber.setVisibility(View.GONE);
                    frameUrl = "";
                    VLCApplication.frame.clear();
                    break;
                case "driverLicense":
                    //赋值，图片视图显示图片
                    driverLicense.setImageResource(R.mipmap.driver_license);
                    //隐藏switchDriverLicenseImage
                    switchDriverLicense.setVisibility(View.GONE);
                    drivelicenseUrl = "";
                    VLCApplication.drivelicense.clear();
                    break;
                default:
                    break;
            }
            PreferenceUtil.commitBoolean("isDeleteImage", false);
        }
    }

    @Override
    protected int setViewId() {
        return R.layout.payment_detail_activity;
    }

    @Override
    protected void findView() {
        IDOpposite = (ImageView) findViewById(R.id.IDOppositePayment);
        IDPositive = (ImageView) findViewById(R.id.IDPositivePayment);
        bank = (ImageView) findViewById(R.id.bankPayment);
        drivingLicense = (ImageView) findViewById(R.id.drivingLicensePayment);
        driverLicense = (ImageView) findViewById(R.id.driverLicensePayment);
        frameNumber = (ImageView) findViewById(R.id.frameNumberPayment);
        switchIDOpposite = (ImageView) findViewById(R.id.switchIDOppositePayment);
        switchIDPositive = (ImageView) findViewById(R.id.switchIDPositivePayment);
        switchBank = (ImageView) findViewById(R.id.switchBankPayment);
        switchDrivingLicense = (ImageView) findViewById(R.id.switchDrivingLicensePayment);
        switchDriverLicense = (ImageView) findViewById(R.id.switchDriverLicensePayment);
        switchFrameNumber = (ImageView) findViewById(R.id.switchFrameNumberPayment);

        rl_bottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        rb_back = (RadioButton) findViewById(R.id.rb_back);
        btn_payment_upload = (Button) findViewById(R.id.btn_payment_upload);
        tv_assessment_date = (TextView) findViewById(R.id.tv_assessment_date);
        tv_assessment_money = (TextView) findViewById(R.id.tv_assessment_money);
        tv_close_date = (TextView) findViewById(R.id.tv_close_date);
        tv_close_money = (TextView) findViewById(R.id.tv_close_money);

        ll_success = (LinearLayout) findViewById(R.id.ll_success); // 定损成功
        ll_failure = (LinearLayout) findViewById(R.id.ll_failure); // 定损失败
        ll_dropped = (LinearLayout) findViewById(R.id.ll_dropped); // 销案

        tv_assessment_date_failure = (TextView) findViewById(R.id.tv_assessment_date_failure); // 失败定损日期时间
        tv_assessment_money_failure = (TextView) findViewById(R.id.tv_assessment_money_failure); // 定损失败原因类型
        tv_close_date_failure = (TextView) findViewById(R.id.tv_close_date_failure); // 定损失败原因

        tv_assessment_date_dropped = (TextView) findViewById(R.id.tv_assessment_date_dropped); // 销案时间
        tv_assessment_money_dropped = (TextView) findViewById(R.id.tv_assessment_money_dropped); // 销案原因类型
        tv_close_date_dropped = (TextView) findViewById(R.id.tv_close_date_dropped); // 销案原因
    }

    @Override
    protected void init() {

        dialog = new NormalDialog(PaymentDetail.this);
        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);

        arrPayment = new ArrayList<>();
    }

    @Override
    protected void initEvents() {
        rb_back.setOnClickListener(this);
        btn_payment_upload.setOnClickListener(this);

        //点击身份证反面照片时的响应
        IDOpposite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "IDOpposite";
                lookImage = "IDOpposite";
                if (switchIDOpposite.getVisibility() == View.GONE) {
                    if (!isEdit) {
                        //从当前的界面跳转到图片查看界面
                        if (null != license_bUrl && !"".equals(license_bUrl)) {
                            openPictureDetail(license_bUrl);
                        }
                    } else {
                        //弹出图片选择页面
                        openPictureSelector();
                    }
                } else {
                    //从当前的界面跳转到图片查看界面
                    openPictureDetail(license_bUrl);
                }
            }
        });
        //点击身份证正面照片时的响应
        IDPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "IDPositive";
                lookImage = "IDPositive";
                if (switchIDPositive.getVisibility() == View.GONE) {
                    if (!isEdit) {
                        //从当前的界面跳转到图片查看界面
                        if (null != license_aUrl && !"".equals(license_aUrl)) {
                            openPictureDetail(license_aUrl);
                        }
                    } else {
                        //弹出图片选择页面
                        openPictureSelector();
                    }
                } else {
                    //从当前的界面跳转到图片查看界面
                    openPictureDetail(license_aUrl);
                }
            }
        });
        //点击银行卡照片时的响应
        bank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "bank";
                lookImage = "bank";
                if (switchBank.getVisibility() == View.GONE) {
                    if (!isEdit) {
                        //从当前的界面跳转到图片查看界面
                        if (null != cardUrl && !"".equals(cardUrl)) {
                            openPictureDetail(cardUrl);
                        }
                    } else {
                        //弹出图片选择页面
                        openPictureSelector();
                    }
                } else {
                    //从当前的界面跳转到图片查看界面
                    openPictureDetail(cardUrl);
                }
            }
        });
        //点击行驶证照片时的响应
        drivingLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "drivingLicense";
                lookImage = "drivingLicense";
                if (switchDrivingLicense.getVisibility() == View.GONE) {
                    if (!isEdit) {
                        //从当前的界面跳转到图片查看界面
                        if (null != permitUrl && !"".equals(permitUrl)) {
                            openPictureDetail(permitUrl);
                        }
                    } else {
                        //弹出图片选择页面
                        openPictureSelector();
                    }
                } else {
                    //从当前的界面跳转到图片查看界面
                    openPictureDetail(permitUrl);
                }
            }
        });
        //点击驾驶证照片时的响应
        driverLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "driverLicense";
                lookImage = "driverLicense";
                if (switchDriverLicense.getVisibility() == View.GONE) {
                    if (!isEdit) {
                        //从当前的界面跳转到图片查看界面
                        if (null != drivelicenseUrl && !"".equals(drivelicenseUrl)) {
                            openPictureDetail(drivelicenseUrl);
                        }
                    } else {
                        //弹出图片选择页面
                        openPictureSelector();
                    }
                } else {
                    //从当前的界面跳转到图片查看界面
                    openPictureDetail(drivelicenseUrl);
                }
            }
        });
        //点击车架号照片时的响应
        frameNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "frameNumber";
                lookImage = "frameNumber";
                if (switchFrameNumber.getVisibility() == View.GONE) {
                    if (!isEdit) {
                        //从当前的界面跳转到图片查看界面
                        if (null != frameUrl && !"".equals(frameUrl)) {
                            openPictureDetail(frameUrl);
                        }
                    } else {
                        //弹出图片选择页面
                        openPictureSelector();
                    }
                } else {
                    //从当前的界面跳转到图片查看界面
                    openPictureDetail(frameUrl);
                }
            }
        });
        //点击切换身份证反面照片时的响应
        switchIDOpposite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "IDOpposite";
                //弹出图片选择页面
                openPictureSelector();
            }
        });
        //点击切换身份证正面照片时的响应
        switchIDPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "IDPositive";
                //弹出图片选择页面
                openPictureSelector();
            }
        });
        //点击切换银行卡照片时的响应
        switchBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "bank";
                //弹出图片选择页面
                openPictureSelector();
            }
        });
        //点击切换行驶证照片时的响应
        switchDrivingLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "drivingLicense";
                //弹出图片选择页面
                openPictureSelector();
            }
        });
        //点击切换驾驶证照片时的响应
        switchDriverLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "driverLicense";
                //弹出图片选择页面
                openPictureSelector();
            }
        });
        //点击切换车架号照片时的响应
        switchFrameNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "frameNumber";
                openPictureSelector();
            }
        });
    }

    /**
     * 查看大图详情
     *
     * @param url
     */
    private void openPictureDetail(String url) {
        Intent intent = new Intent(PaymentDetail.this, LookPhoto.class);
        intent.putExtra("whichImage", url);
        intent.putExtra("lookImage", lookImage);
        intent.putExtra("lotusStatus", isEdit);
        startActivity(intent);
    }

    /**
     * 打开图片选择器
     */
    private void openPictureSelector() {
        //弹出图片选择页面
        PictureSelector.create(PaymentDetail.this)
                .openGallery(PictureMimeType.ofImage())
                .selectionMode(PictureConfig.SINGLE)//设置为单选
                .compress(true)// 是否压缩 true or false
                .forResult(PictureConfig.CHOOSE_REQUEST);
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
        PictureFileUtils.deleteCacheDirFile(PaymentDetail.this);

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

                    license_bUrl = BitmapUtils.getSDPath() + "/VOC/Cache/IDOpposite.png";
                    break;
                case "IDPositive":
                    //赋值，图片视图显示图片
                    IDPositive.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png"));
                    //显示switchIDPositiveImage
                    switchIDPositive.setVisibility(View.VISIBLE);

                    license_aUrl = BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png";
                    break;
                case "bank":
                    //赋值，图片视图显示图片
                    bank.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/bank.png"));
                    //显示switchBankImage
                    switchBank.setVisibility(View.VISIBLE);

                    cardUrl = BitmapUtils.getSDPath() + "/VOC/Cache/bank.png";
                    break;
                case "drivingLicense":
                    //赋值，图片视图显示图片
                    drivingLicense.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png"));
                    //显示switchDrivingLicenseImage
                    switchDrivingLicense.setVisibility(View.VISIBLE);

                    permitUrl = BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png";
                    break;
                case "frameNumber":
                    //赋值，图片视图显示图片
                    frameNumber.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png"));
                    //显示switchFrameNumberImage
                    switchFrameNumber.setVisibility(View.VISIBLE);

                    frameUrl = BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png";
                    break;
                case "driverLicense":
                    //赋值，图片视图显示图片
                    driverLicense.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png"));
                    //显示switchDriverLicenseImage
                    switchDriverLicense.setVisibility(View.VISIBLE);

                    drivelicenseUrl = BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png";
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void loadData() {
//        showProgress("拼命加载中...");
        getCasedetail(); // 加载数据
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_back:
                finish();
                break;
            case R.id.btn_payment_upload:
                if (!NetUtils.isNetworkConnected(PaymentDetail.this)) {//判断是否联网
                    ToastUtils.showNomalShortToast(PaymentDetail.this, getString(R.string.network_off));
                    return;
                }
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
                break;
        }
    }

    /**
     * 获得事故ID的具体内容
     */
    private void getCasedetail() {
        Map<String, String> map = new HashMap<>();
        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getCaseDetail(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        JSONObject datas = jsonObject.getJSONObject("datas");
                        // 定损和报案 状态
                        String lossStatus = datas.getJSONObject("case").getString("losestatus");
                        String reportStatus = datas.getJSONObject("case").getString("reportstatus");
                        // 理赔状态
                        String paylossStatus = datas.getJSONObject("case").getString("paylossstatus");
                        // 已销案
                        if ("9".equals(lossStatus) || "9".equals(reportStatus)) {
                            ll_dropped.setVisibility(View.VISIBLE);
                            ll_failure.setVisibility(View.GONE);
                            ll_success.setVisibility(View.GONE);
                            tv_assessment_date_dropped.setText(Utils.parseStr(datas.getJSONObject("case").getString("canceltime"))); // 销案时间
                            tv_close_date_dropped.setText(Utils.parseStr(datas.getJSONObject("case").getString("cancelreasondetial"))); // 销案原因
                            String[] reasonTypes;
                            if (VLCApplication.configsDictionary.get("app-c-227") != null) {
                                reasonTypes = VLCApplication.configsDictionary.get("app-c-227").split("，");
                            } else {
                                reasonTypes = "1-案件信息填写错了，2-已送至其他店维修，0-其他".split("，");
                            }
                            for (int i = 0; i < reasonTypes.length; i++) {
                                String number = reasonTypes[i].split("-")[0];
                                if (number.equals(datas.getJSONObject("case").getString("cancelreason"))) {
                                    tv_assessment_money_dropped.setText(reasonTypes[i].split("-")[1]);
                                }
                            }
                        } else if ("5".equals(lossStatus) || "5".equals(reportStatus)) {// 以失败
                            ll_dropped.setVisibility(View.GONE);
                            ll_failure.setVisibility(View.VISIBLE);
                            ll_success.setVisibility(View.GONE);

                            tv_assessment_date_failure.setText(Utils.parseStr(datas.getJSONObject("case").getString("lossdate"))); // 失败定损日期时间
                            tv_close_date_failure.setText(Utils.parseStr(datas.getJSONObject("case").getString("failreasondetial"))); // 定损失败原因

                            String[] reasonTypes;
                            if (VLCApplication.configsDictionary.get("app-c-229") != null) {
                                reasonTypes = VLCApplication.configsDictionary.get("app-c-229").split("，");
                            } else {
                                reasonTypes = "1-未上传定损照片，2-定损照片不合规，3-超出定损限额，4-车辆需拆解，0-其他".split("，");
                            }
                            for (int i = 0; i < reasonTypes.length; i++) {
                                String number = reasonTypes[i].split("-")[0];
                                if (number.equals(datas.getJSONObject("case").getString("failreason"))) {
                                    tv_assessment_money_failure.setText(reasonTypes[i].split("-")[1]);
                                }
                            }
                        } else {
                            // 定损日期
                            if (null != datas.getJSONObject("case").getString("lossdate")) {
                                tv_assessment_date.setText(Utils.parseStr(datas.getJSONObject("case").getString("lossdate")));
                            }
                            // 定损金额
                            if (null != datas.getJSONObject("case").getString("losssum")) {
                                tv_assessment_money.setText(Utils.parseStr(datas.getJSONObject("case").getString("losssum")));
                            }
                            // 结案日期
                            if (null != datas.getJSONObject("case").getString("finishdate")) {
                                tv_close_date.setText(Utils.parseStr(datas.getJSONObject("case").getString("finishdate")));
                            }
                            // 结案金额
                            if (null != datas.getJSONObject("case").getString("finishsum")) {
                                tv_close_money.setText(Utils.parseStr(datas.getJSONObject("case").getString("finishsum")));
                            }
                        }
                        // ((|报案状态|非空 && <>已销案) || (|定损状态|非空 && <>已销案)) &&  |理赔状态|<>已结案 ，才显示并可操作
                        if (((!"0".equals(lossStatus) && !"9".equals(lossStatus)) || (!"0".equals(reportStatus) && !"9".equals(reportStatus)))
                                && !"2".equals(paylossStatus)) {
                            // 是否可编辑
                            isEdit = true;
                            // 判断底部是否隐藏或显示
                            rl_bottom.setVisibility(View.VISIBLE);
                        } else {
                            isEdit = false;
                            switchIDOpposite.setVisibility(View.GONE);
                            switchIDPositive.setVisibility(View.GONE);
                            switchBank.setVisibility(View.GONE);
                            switchDrivingLicense.setVisibility(View.GONE);
                            switchFrameNumber.setVisibility(View.GONE);
                            switchDriverLicense.setVisibility(View.GONE);
                            rl_bottom.setVisibility(View.GONE);
                        }

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
                            license_bUrl = Config.QINIU_BASE_URL + piclink;
                            //从网络中获得图片
                            if (piclink != null && !piclink.equals("") && !piclink.equals("null")) {
                                ImageLoader.getInstance().displayImage(license_bUrl, IDOpposite);
                            }
                            if (piclink != null && !piclink.equals("")
                                    && isEdit) {
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
                            license_aUrl = Config.QINIU_BASE_URL + piclink;
                            //从网络中获得图片
                            if (piclink != null && !piclink.equals("") && !piclink.equals("null")) {
                                ImageLoader.getInstance().displayImage(license_aUrl, IDPositive);
                            }
                            if (piclink != null && !piclink.equals("")
                                    && isEdit) {
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
                            cardUrl = Config.QINIU_BASE_URL + piclink;
                            //从网络中获得图片
                            if (piclink != null && !piclink.equals("") && !piclink.equals("null")) {
                                ImageLoader.getInstance().displayImage(cardUrl, bank);
                            }
                            if (piclink != null && !piclink.equals("")
                                    && isEdit) {
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
                            drivelicenseUrl = Config.QINIU_BASE_URL + piclink;
                            //从网络中获得图片

                            if (piclink != null && !piclink.equals("") && !piclink.equals("null")) {
                                ImageLoader.getInstance().displayImage(drivelicenseUrl, driverLicense);
                            }
                            if (piclink != null && !piclink.equals("")
                                    && isEdit) {
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
                            permitUrl = Config.QINIU_BASE_URL + piclink;
                            //从网络中获得图片
                            if (piclink != null && !piclink.equals("") && !piclink.equals("null")) {
                                ImageLoader.getInstance().displayImage(permitUrl, drivingLicense);
                            }
                            if (piclink != null && !piclink.equals("")
                                    && isEdit) {
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
                            frameUrl = Config.QINIU_BASE_URL + piclink;
                            //从网络中获得图片
                            if (piclink != null && !piclink.equals("") && !piclink.equals("null")) {
                                ImageLoader.getInstance().displayImage(frameUrl, frameNumber);
                            }
                            if (piclink != null && !piclink.equals("")
                                    && isEdit) {
                                //显示switchFrameNumber
                                switchFrameNumber.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        Toast.makeText(PaymentDetail.this, "数据加载异常，请稍后再试", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ToastUtils.showNomalShortToast(PaymentDetail.this, "数据加载异常，请稍后再试");
            }
        });
    }

    /**
     * 获取七牛token
     */
    private void getQiniuToken() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getQiniuToken(NetUtils.getHeaders(), 6);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                arrPayment.clear();
            }


        });
    }

    /**
     * 上传身份证反面照片
     *
     * @param fileName
     */
    private void upLoadIdOpposite(String fileName) {

        //上传身份证反面照片
        if (null == license_bUrl || "".equals(license_bUrl)) {
            // 先清空之前的内容
            VLCApplication.license_b.clear();
            VLCApplication.compensatepictures.put("license_b", VLCApplication.license_b);
            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);
            upLoadIdPositive(arrPayment.get(1));
        } else {
            File IDOppositeImageFile = new File(license_bUrl);
            if (IDOppositeImageFile.exists()) {
                // 先清空之前的内容
                VLCApplication.license_b.clear();
                VLCApplication.uploadManager.put(IDOppositeImageFile, fileName, qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
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
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            }
                        }, null);
            } else {

                VLCApplication.compensatepictures.put("license_b", VLCApplication.license_b);
                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);
                upLoadIdPositive(arrPayment.get(1));
            }
        }
    }

    /**
     * 上传身份证正面照片
     *
     * @param fileName
     */
    private void upLoadIdPositive(String fileName) {
        //上传身份证正面照片
        if (null == license_aUrl || "".equals(license_aUrl)) {
            VLCApplication.license_a.clear();
            VLCApplication.compensatepictures.put("license_a", VLCApplication.license_a);
            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);
            upLoadBank(arrPayment.get(2));
        } else {
            File IDPositiveImageFile = new File(license_aUrl);
            if (IDPositiveImageFile.exists()) {
                // 存在就清空之前的MAP
                VLCApplication.license_a.clear();
                VLCApplication.uploadManager.put(IDPositiveImageFile, fileName, qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
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
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            }
                        }, null);
            } else {
                VLCApplication.compensatepictures.put("license_a", VLCApplication.license_a);
                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);
                upLoadBank(arrPayment.get(2));
            }
        }
    }

    /**
     * 上传银行卡照片
     *
     * @param fileName
     */
    private void upLoadBank(String fileName) {
        //上传银行卡照片
        if (null == cardUrl || "".equals(cardUrl)) {
            VLCApplication.card.clear();
            VLCApplication.compensatepictures.put("card", VLCApplication.card);
            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);
            upLoadDrivingLicense(arrPayment.get(3));
        } else {
            File bankImageFile = new File(cardUrl);
            if (bankImageFile.exists()) {
                VLCApplication.card.clear();
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
                VLCApplication.compensatepictures.put("card", VLCApplication.card);
                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);
                upLoadDrivingLicense(arrPayment.get(3));
            }
        }
    }

    /**
     * 上传行驶证照片
     *
     * @param fileName
     */
    private void upLoadDrivingLicense(String fileName) {
        if (null == permitUrl || "".equals(permitUrl)) {
            VLCApplication.permit.clear();
            VLCApplication.compensatepictures.put("permit", VLCApplication.permit);
            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);
            upLoadDriverLicense(arrPayment.get(4));
        } else {
            //上传行驶证照片
            File drivingLicenseImageFile = new File(permitUrl);
            if (drivingLicenseImageFile.exists()) {
                VLCApplication.permit.clear();
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

                VLCApplication.compensatepictures.put("permit", VLCApplication.permit);
                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);
                upLoadDriverLicense(arrPayment.get(4));
            }
        }
    }

    /**
     * 上传驾驶证照片
     *
     * @param fileName
     */
    private void upLoadDriverLicense(String fileName) {

        if (null == drivelicenseUrl || "".equals(drivelicenseUrl)) {
            VLCApplication.drivelicense.clear();
            VLCApplication.compensatepictures.put("drivelicense", VLCApplication.drivelicense);
            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);
            upLoadFrameNumber(arrPayment.get(5));
        } else {
            //上传驾驶证照片
            File driverLicenseImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png");
            if (driverLicenseImageFile.exists()) {
                VLCApplication.drivelicense.clear();
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
                VLCApplication.compensatepictures.put("drivelicense", VLCApplication.drivelicense);
                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);
                upLoadFrameNumber(arrPayment.get(5));
            }
        }
    }

    /**
     * 上传车架号照片
     *
     * @param fileName
     */
    private void upLoadFrameNumber(String fileName) {

        if (null == frameUrl || "".equals(frameUrl)) {
            VLCApplication.frame.clear();
            VLCApplication.compensatepictures.put("frame", VLCApplication.frame);
            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);
            oneKeyPayment2();
        } else {
            //上传车架号照片
            File frameNumberImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png");
            if (frameNumberImageFile.exists()) {
                VLCApplication.frame.clear();
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
                VLCApplication.compensatepictures.put("frame", VLCApplication.frame);
                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);
                oneKeyPayment2();
            }
        }
    }

    /**
     * 调用发送短信接口
     *
     * @param caseid
     */
    private void sendSMS(String caseid, int type) {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().sendSMS(NetUtils.getHeaders(), caseid, type);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    /**
     * 理赔步骤2
     */
    private void oneKeyPayment2(){

        CasePayment casePayment = new CasePayment(PreferenceUtil.getString("caseid", null), VLCApplication.compensatepictures);
        Call<ResponseBody> call = RetrofitManager.getInstance().create().stepThree(NetUtils.getHeaders(), casePayment);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {

                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);
                        currentProgress = 0;

                        if ("1".equals(PreferenceUtil.getString("isdemo", "0"))) {
                        } else {
                            // 理赔上传结束后，调用发送短信接口
                            sendSMS(jsonObject.getJSONObject("datas").getJSONObject("case").getString("caseid"), 3);
                        }
                        // 回调关闭快赔索引页
                        Utils.doCallBackMethod();
                        Intent intent = new Intent(PaymentDetail.this, ReportSuccess.class);
                        intent.putExtra("isPayment", true);
                        startActivity(intent);

                        finish();
                    } else {
                        Message message = new Message();
                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                        handler.sendMessage(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_NET_FAIL;
                handler.sendMessage(message);
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
                .btnTextColor(new int[]{ContextCompat.getColor(PaymentDetail.this, R.color.primary), ContextCompat.getColor(PaymentDetail.this, R.color.alphablack)})
                .show();
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label, int maxProgress) {
        kProgressHUD = KProgressHUD.create(PaymentDetail.this)
                .setStyle(KProgressHUD.Style.ANNULAR_DETERMINATE)
                .setLabel(label)
                .setMaxProgress(maxProgress)
                .show();
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
