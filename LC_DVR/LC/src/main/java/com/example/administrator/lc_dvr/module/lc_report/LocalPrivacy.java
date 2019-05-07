package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.Config;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.listener.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.module.login_registration.Landing;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.PictureFileUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Created by yangboru on 2018/1/6.
 * <p>
 * 本地信息
 */

public class LocalPrivacy extends BaseFragment implements TextWatcher {

    private ImageView IDOpposite;
    private ImageView IDPositive;
    private ImageView bank;
    private ImageView drivingLicense;
    private ImageView driverLicense;
    private ImageView frameNumber;
    private EditText plateNumber;
    private EditText driverPhone;
    private EditText driverName;
    private ImageView switchIDOpposite;
    private ImageView switchIDPositive;
    private ImageView switchBank;
    private ImageView switchDrivingLicense;
    private ImageView switchDriverLicense;
    private ImageView switchFrameNumber;

    //判断是点击了那张图片进入的图片查看界面
    private String lookImage = "";
    private String strPlateNumber;
    private String strDriverPhone;
    private String strDriverName;

    //判断是点击了那张图片
    private String whichImage = "";
    private Button quitTap;
    private Button loadSaveTap;
    private HashMap<String, String> bitmapList;
    private boolean isEditingPhoto;
    // 退出提示框
    private NormalDialog dialog;
    private String[] mStringBts;

    private String IDOppositeURL;
    private String IDPositiveURL;
    private String bankURL;
    private String drivingLicenseURL;
    private String frameNumberURL;
    private String driverLicenseURL;

    @Override
    protected int setViewId() {
        return R.layout.local_privacy_layout;
    }

    @Override
    protected void findView(View view) {
        IDOpposite = (ImageView) view.findViewById(R.id.IDOpposite);
        IDPositive = (ImageView) view.findViewById(R.id.IDPositive);
        bank = (ImageView) view.findViewById(R.id.bank);
        drivingLicense = (ImageView) view.findViewById(R.id.drivingLicense);
        driverLicense = (ImageView) view.findViewById(R.id.driverLicense);
        frameNumber = (ImageView) view.findViewById(R.id.frameNumber);
        plateNumber = (EditText) view.findViewById(R.id.plateNumber);
        driverPhone = (EditText) view.findViewById(R.id.driverPhone);
        driverName = (EditText) view.findViewById(R.id.driverName);
        switchIDOpposite = (ImageView) view.findViewById(R.id.switchIDOpposite);
        switchIDPositive = (ImageView) view.findViewById(R.id.switchIDPositive);
        switchBank = (ImageView) view.findViewById(R.id.switchBank);
        switchDrivingLicense = (ImageView) view.findViewById(R.id.switchDrivingLicense);
        switchDriverLicense = (ImageView) view.findViewById(R.id.switchDriverLicense);
        switchFrameNumber = (ImageView) view.findViewById(R.id.switchFrameNumber);
        quitTap = (Button) view.findViewById(R.id.quitTap);
        loadSaveTap = (Button) view.findViewById(R.id.loadSaveTap);
    }

    @Override
    protected void init() {
        // 先设置保存本地不能点击
        loadSaveTap.setEnabled(false);
        loadSaveTap.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray_white));
        loadSaveTap.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.sky_blue));

        dialog = new NormalDialog(getActivity());
        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);
        //判断是否编辑了图片
        isEditingPhoto = false;

        plateNumber.addTextChangedListener(this);
        driverName.addTextChangedListener(this);
        driverPhone.addTextChangedListener(this);

        PreferenceUtil.commitBoolean("isDeleteImage", false);
        //临时保存bitmap地址
        bitmapList = new HashMap<>();
        //获得本地的个人信息
        strDriverName = PreferenceUtil.getString("driverName", "");
        strDriverPhone = PreferenceUtil.getString("driverPhone", "");
        strPlateNumber = PreferenceUtil.getString("plateNumber", "");

        IDOppositeURL = BitmapUtils.getSDPath() + "/VOC/Cache/IDOpposite.png";
        IDPositiveURL = BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png";
        bankURL = BitmapUtils.getSDPath() + "/VOC/Cache/bank.png";
        drivingLicenseURL = BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png";
        frameNumberURL = BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png";
        driverLicenseURL = BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png";
        //判断本地是否存在图片
        File IDOppositeFile = new File(IDOppositeURL);
        if (IDOppositeFile.exists()) {
            //加载本地图片
            IDOpposite.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(IDOppositeURL));
            bitmapList.put("IDOpposite", IDOppositeURL);
            //显示切换图标
            switchIDOpposite.setVisibility(View.VISIBLE);

        }
        //判断本地中是否存在图片
        File IDPositiveFile = new File(IDPositiveURL);
        if (IDPositiveFile.exists()) {
            //从本地中获得图片
            IDPositive.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(IDPositiveURL));
            bitmapList.put("IDPositive", IDPositiveURL);
            //显示切换图标
            switchIDPositive.setVisibility(View.VISIBLE);
        }
        //判断本地缓存中是否存在图片
        File bankFile = new File(bankURL);
        if (bankFile.exists()) {
            //从本地获得图片
            bank.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(bankURL));
            bitmapList.put("bank", bankURL);
            //显示切换图标
            switchBank.setVisibility(View.VISIBLE);
        }
        //判断本地缓存中是否存在图片
        File drivingLicenseFile = new File(drivingLicenseURL);
        if (drivingLicenseFile.exists()) {
            //从缓存中获得图片
            drivingLicense.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(drivingLicenseURL));
            bitmapList.put("drivingLicense", drivingLicenseURL);
            //显示切换图标
            switchDrivingLicense.setVisibility(View.VISIBLE);
        }
        //判断本地缓存中是否存在图片
        File frameNumberFile = new File(frameNumberURL);
        if (frameNumberFile.exists()) {
            //从缓存中获得图片
            frameNumber.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(frameNumberURL));
            bitmapList.put("frameNumber", frameNumberURL);
            //显示切换图标
            switchFrameNumber.setVisibility(View.VISIBLE);
        }
        //判断本地缓存中是否存在图片
        File driverLicenseFile = new File(driverLicenseURL);
        if (driverLicenseFile.exists()) {
            //从缓存中获得图片
            driverLicense.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(driverLicenseURL));
            bitmapList.put("driverLicense", driverLicenseURL);
            //显示切换图标
            switchDriverLicense.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //获得本地的个人信息和显示
        driverName.setText(strDriverName);
        driverPhone.setText(strDriverPhone);
        plateNumber.setText(strPlateNumber);

        if (!isEditingPhoto) {
            isEditingPhoto = PreferenceUtil.getBoolean("isEditingPhoto", false);
        }
        if (PreferenceUtil.getBoolean("isDeleteImage", false)) {
            switch (lookImage) {
                case "IDOppositeImage":
                    //赋值，图片视图显示图片
                    IDOpposite.setImageResource(R.mipmap.id_opposite);
                    //隐藏switchIDOppositeImage
                    switchIDOpposite.setVisibility(View.GONE);
                    break;
                case "IDPositiveImage":
                    //赋值，图片视图显示图片
                    IDPositive.setImageResource(R.mipmap.id_positive);
                    //隐藏switchIDPositiveImage
                    switchIDPositive.setVisibility(View.GONE);
                    break;
                case "bankImage":
                    //赋值，图片视图显示图片
                    bank.setImageResource(R.mipmap.bank);
                    //隐藏switchBankImage
                    switchBank.setVisibility(View.GONE);
                    break;
                case "drivingLicenseImage":
                    //赋值，图片视图显示图片
                    drivingLicense.setImageResource(R.mipmap.driving_license);
                    //隐藏switchDrivingLicenseImage
                    switchDrivingLicense.setVisibility(View.GONE);
                    break;
                case "frameNumberImage":
                    //赋值，图片视图显示图片
                    frameNumber.setImageResource(R.mipmap.frame_number);
                    //隐藏switchFrameNumberImage
                    switchFrameNumber.setVisibility(View.GONE);
                    break;
                case "driverLicenseImage":
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
    public void onPause() {
        super.onPause();
        strPlateNumber = plateNumber.getText().toString();
        strDriverName = driverName.getText().toString();
        strDriverPhone = driverPhone.getText().toString();
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
                            //判断是否编辑了图片
                            isEditingPhoto = true;
                            switch (whichImage) {
                                case "IDOpposite":
                                    //赋值，图片视图显示图片
                                    IDOpposite.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(picturePath));
                                    BitmapUtils.saveBitmap(picturePath, "IDOpposite");
                                    bitmapList.put("IDOpposite", picturePath);
                                    //显示switchIDOppositeImage
                                    switchIDOpposite.setVisibility(View.VISIBLE);
                                    break;
                                case "IDPositive":
                                    //赋值，图片视图显示图片
                                    IDPositive.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(picturePath));
                                    BitmapUtils.saveBitmap(picturePath, "IDPositive");
                                    bitmapList.put("IDPositive", picturePath);
                                    //显示switchIDPositiveImage
                                    switchIDPositive.setVisibility(View.VISIBLE);
                                    break;
                                case "bank":
                                    //赋值，图片视图显示图片
                                    bank.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(picturePath));
                                    BitmapUtils.saveBitmap(picturePath, "bank");
                                    bitmapList.put("bank", picturePath);
                                    //显示switchBankImage
                                    switchBank.setVisibility(View.VISIBLE);
                                    break;
                                case "drivingLicense":
                                    //赋值，图片视图显示图片
                                    drivingLicense.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(picturePath));
                                    BitmapUtils.saveBitmap(picturePath, "drivingLicense");
                                    bitmapList.put("drivingLicense", picturePath);
                                    //显示switchDrivingLicenseImage
                                    switchDrivingLicense.setVisibility(View.VISIBLE);
                                    break;
                                case "frameNumber":
                                    //赋值，图片视图显示图片
                                    frameNumber.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(picturePath));
                                    BitmapUtils.saveBitmap(picturePath, "frameNumber");
                                    bitmapList.put("frameNumber", picturePath);
                                    //显示switchFrameNumberImage
                                    switchFrameNumber.setVisibility(View.VISIBLE);
                                    break;
                                case "driverLicense":
                                    //赋值，图片视图显示图片
                                    driverLicense.setImageBitmap(BitmapUtils.decodeSampledBitmapFromPath(picturePath));
                                    BitmapUtils.saveBitmap(picturePath, "driverLicense");
                                    bitmapList.put("driverLicense", picturePath);
                                    //显示switchDriverLicenseImage
                                    switchDriverLicense.setVisibility(View.VISIBLE);
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
        PictureFileUtils.deleteCacheDirFile(LocalPrivacy.this.getContext());
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
    protected void initEvents() {
        //点击身份证反面照片时的响应
        IDOpposite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchIDOpposite.getVisibility() == View.GONE) {
                    whichImage = "IDOpposite";
                    //弹出图片选择页面
                    PictureSelector.create(LocalPrivacy.this)
                            .openGallery(PictureMimeType.ofImage())
                            .selectionMode(PictureConfig.SINGLE)//设置为单选
                            .compress(true)// 是否压缩 true or false
                            .forResult(PictureConfig.CHOOSE_REQUEST);
                } else {
                    whichImage = "IDOpposite";
                    lookImage = "IDOppositeImage";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(getContext(), LookPhoto.class);
                    intent.putExtra("isLocalPrivacy", true);
                    intent.putExtra("whichImage", whichImage);
                    startActivity(intent);
                }
            }
        });
        //点击身份证正面照片时的响应
        IDPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchIDPositive.getVisibility() == View.GONE) {
                    whichImage = "IDPositive";
                    //弹出图片选择页面
                    PictureSelector.create(LocalPrivacy.this)
                            .openGallery(PictureMimeType.ofImage())
                            .selectionMode(PictureConfig.SINGLE)//设置为单选
                            .compress(true)// 是否压缩 true or false
                            .forResult(PictureConfig.CHOOSE_REQUEST);
                } else {
                    whichImage = "IDPositive";
                    lookImage = "IDPositiveImage";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(getContext(), LookPhoto.class);
                    intent.putExtra("isLocalPrivacy", true);
                    intent.putExtra("whichImage", whichImage);
                    startActivity(intent);
                }
            }
        });
        //点击银行卡照片时的响应
        bank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchBank.getVisibility() == View.GONE) {
                    whichImage = "bank";
                    //弹出图片选择页面
                    PictureSelector.create(LocalPrivacy.this)
                            .openGallery(PictureMimeType.ofImage())
                            .selectionMode(PictureConfig.SINGLE)//设置为单选
                            .compress(true)// 是否压缩 true or false
                            .forResult(PictureConfig.CHOOSE_REQUEST);
                } else {
                    whichImage = "bank";
                    lookImage = "bankImage";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(getContext(), LookPhoto.class);
                    intent.putExtra("isLocalPrivacy", true);
                    intent.putExtra("whichImage", whichImage);
                    startActivity(intent);
                }
            }
        });
        //点击行驶证照片时的响应
        drivingLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchDrivingLicense.getVisibility() == View.GONE) {
                    whichImage = "drivingLicense";
                    //弹出图片选择页面
                    PictureSelector.create(LocalPrivacy.this)
                            .openGallery(PictureMimeType.ofImage())
                            .selectionMode(PictureConfig.SINGLE)//设置为单选
                            .compress(true)// 是否压缩 true or false
                            .forResult(PictureConfig.CHOOSE_REQUEST);
                } else {
                    whichImage = "drivingLicense";
                    lookImage = "drivingLicenseImage";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(getContext(), LookPhoto.class);
                    intent.putExtra("isLocalPrivacy", true);
                    intent.putExtra("whichImage", whichImage);
                    startActivity(intent);
                }
            }
        });
        //点击驾驶证照片时的响应
        driverLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchDriverLicense.getVisibility() == View.GONE) {
                    whichImage = "driverLicense";
                    //弹出图片选择页面
                    PictureSelector.create(LocalPrivacy.this)
                            .openGallery(PictureMimeType.ofImage())
                            .selectionMode(PictureConfig.SINGLE)//设置为单选
                            .compress(true)// 是否压缩 true or false
                            .forResult(PictureConfig.CHOOSE_REQUEST);
                } else {
                    whichImage = "driverLicense";
                    lookImage = "driverLicenseImage";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(getContext(), LookPhoto.class);
                    intent.putExtra("isLocalPrivacy", true);
                    intent.putExtra("whichImage", whichImage);
                    startActivity(intent);
                }
            }
        });
        //点击车架号照片时的响应
        frameNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchFrameNumber.getVisibility() == View.GONE) {
                    whichImage = "frameNumber";
                    //弹出图片选择页面
                    PictureSelector.create(LocalPrivacy.this)
                            .openGallery(PictureMimeType.ofImage())
                            .selectionMode(PictureConfig.SINGLE)//设置为单选
                            .compress(true)// 是否压缩 true or false
                            .forResult(PictureConfig.CHOOSE_REQUEST);
                } else {
                    whichImage = "frameNumber";
                    lookImage = "frameNumberImage";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(getContext(), LookPhoto.class);
                    intent.putExtra("isLocalPrivacy", true);
                    intent.putExtra("whichImage", whichImage);
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
                PictureSelector.create(LocalPrivacy.this)
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
                PictureSelector.create(LocalPrivacy.this)
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
                PictureSelector.create(LocalPrivacy.this)
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
                PictureSelector.create(LocalPrivacy.this)
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
                PictureSelector.create(LocalPrivacy.this)
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
                PictureSelector.create(LocalPrivacy.this)
                        .openGallery(PictureMimeType.ofImage())
                        .selectionMode(PictureConfig.SINGLE)//设置为单选
                        .compress(true)// 是否压缩 true or false
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
        });
        //点击退出按钮时的响应
        quitTap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示提示框
                showTipDialog(getString(R.string.personal_info_exit_sure));
                //提示框
                dialog.setOnBtnClickL(
                        new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                                //移除所有的activity
                                VLCApplication.removeALLActivity();
                                //清空授权码
                                PreferenceUtil.commitString("tokenid", null);
                                //清空用户的手机号码
                                PreferenceUtil.commitString("user_mobile", null);
                                //跳转到登录界面
                                Intent intent = new Intent(getContext(), Landing.class);
                                getActivity().startActivity(intent);
                                // quitAPP();
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

        //点击本地保存按钮时的响应
        loadSaveTap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //保存信息之前判断用户有没有更新过了
                if (driverName.getText().toString().equals(PreferenceUtil.getString("driverName", ""))
                        && driverPhone.getText().toString().equals(PreferenceUtil.getString("driverPhone", ""))
                        && plateNumber.getText().toString().equals(PreferenceUtil.getString("plateNumber", ""))
                        && !isEditingPhoto) {
                    Toast.makeText(getContext(), R.string.personal_info_revise_nothing, Toast.LENGTH_SHORT).show();
                    return;

                }

                //保存个人信息到本地
                PreferenceUtil.commitString("driverName", driverName.getText().toString());
                PreferenceUtil.commitString("driverPhone", driverPhone.getText().toString());
                PreferenceUtil.commitString("plateNumber", plateNumber.getText().toString());

                if (bitmapList.get("IDOpposite") == null) {
                    File imageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDOpposite.png");
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                } else {
                    bitmapList.put("IDOpposite", BitmapUtils.getSDPath() + "/VOC/Cache/IDOpposite.png");
                }
                if (bitmapList.get("IDPositive") == null) {
                    File imageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png");
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                } else {
                    bitmapList.put("IDPositive", BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png");
                }
                if (bitmapList.get("bank") == null) {
                    File imageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/bank.png");
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                } else {
                    bitmapList.put("bank", BitmapUtils.getSDPath() + "/VOC/Cache/bank.png");
                }

                if (bitmapList.get("drivingLicense") == null) {
                    File imageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png");
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                } else {
                    bitmapList.put("drivingLicense", BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png");
                }

                if (bitmapList.get("frameNumber") == null) {
                    File imageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png");
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                } else {
                    bitmapList.put("frameNumber", BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png");
                }
                if (bitmapList.get("driverLicense") == null) {
                    File imageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png");
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                } else {
                    bitmapList.put("driverLicense", BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png");
                }

                if (VLCApplication.configsDictionary.get("app-b-050") != null) {
                    Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-b-050"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), R.string.personal_info_save_success, Toast.LENGTH_SHORT).show();
                }

                //退出当前的视图
                getActivity().finish();
            }
        });
    }

    @Override
    protected void loadData() {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!plateNumber.getText().toString().equals(PreferenceUtil.getString("plateNumber", ""))
                || !driverPhone.getText().toString().equals(PreferenceUtil.getString("driverPhone", ""))
                || !driverName.getText().toString().equals(PreferenceUtil.getString("driverName", ""))
                || isEditingPhoto) {
            if (!loadSaveTap.isEnabled()) {
                loadSaveTap.setEnabled(true);
                loadSaveTap.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                loadSaveTap.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.primary));
            }
        } else {
            if (loadSaveTap.isEnabled()) {
                //设置登录按钮不能点击
                loadSaveTap.setEnabled(false);
                loadSaveTap.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray_white));
                loadSaveTap.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.sky_blue));
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // 如果bitmaplist中对应的值不包含对应的值，返回的时候就没有保存
        if (bitmapList.get("IDOpposite") != null && !bitmapList.get("IDOpposite").contains("IDOpposite")) {
            File imageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDOpposite.png");
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
        // 如果bitmaplist中对应的值不包含对应的值，返回的时候就没有保存
        if (bitmapList.get("IDPositive") != null && !bitmapList.get("IDPositive").contains("IDPositive")) {
            File imageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png");
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
        // 如果bitmaplist中对应的值不包含对应的值，返回的时候就没有保存
        if (bitmapList.get("bank") != null && !bitmapList.get("bank").contains("bank")) {
            File imageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/bank.png");
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
        // 如果bitmaplist中对应的值不包含对应的值，返回的时候就没有保存
        if (bitmapList.get("drivingLicense") != null && !bitmapList.get("drivingLicense").contains("drivingLicense")) {
            File imageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png");
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
        // 如果bitmaplist中对应的值不包含对应的值，返回的时候就没有保存
        if (bitmapList.get("frameNumber") != null && !bitmapList.get("frameNumber").contains("frameNumber")) {
            File imageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png");
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
        // 如果bitmaplist中对应的值不包含对应的值，返回的时候就没有保存
        if (bitmapList.get("driverLicense") != null && !bitmapList.get("driverLicense").contains("driverLicense")) {
            File imageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png");
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bitmapList.clear();

    }

}
