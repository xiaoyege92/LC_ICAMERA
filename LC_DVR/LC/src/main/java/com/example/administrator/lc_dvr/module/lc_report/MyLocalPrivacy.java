package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
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

import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/11/19
 *   desc   :
 *  version :
 * </pre>
 */
public class MyLocalPrivacy extends BaseActivity {

    private RadioButton rb_back; // 返回键

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
    private HashMap<String, String> bitmapList;
    private boolean isEditingPhoto;

    private String IDOppositeURL;
    private String IDPositiveURL;
    private String bankURL;
    private String drivingLicenseURL;
    private String frameNumberURL;
    private String driverLicenseURL;

    @Override
    protected void onResume() {
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
    public void onStop() {
        super.onStop();
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        bitmapList.clear();
    }

    @Override
    protected int setViewId() {
        return R.layout.my_local_privacy_activity;
    }

    @Override
    protected void findView() {
        IDOpposite = (ImageView) findViewById(R.id.IDOpposite);
        IDPositive = (ImageView) findViewById(R.id.IDPositive);
        bank = (ImageView) findViewById(R.id.bank);
        drivingLicense = (ImageView) findViewById(R.id.drivingLicense);
        driverLicense = (ImageView) findViewById(R.id.driverLicense);
        frameNumber = (ImageView) findViewById(R.id.frameNumber);
        plateNumber = (EditText) findViewById(R.id.plateNumber);
        driverPhone = (EditText) findViewById(R.id.driverPhone);
        driverName = (EditText) findViewById(R.id.driverName);
        switchIDOpposite = (ImageView) findViewById(R.id.switchIDOpposite);
        switchIDPositive = (ImageView) findViewById(R.id.switchIDPositive);
        switchBank = (ImageView) findViewById(R.id.switchBank);
        switchDrivingLicense = (ImageView) findViewById(R.id.switchDrivingLicense);
        switchDriverLicense = (ImageView) findViewById(R.id.switchDriverLicense);
        switchFrameNumber = (ImageView) findViewById(R.id.switchFrameNumber);
        rb_back = (RadioButton) findViewById(R.id.rb_back);
    }

    @Override
    protected void init() {
        //判断是否编辑了图片
        isEditingPhoto = false;

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
    protected void initEvents() {
        // 左上角返回键
        rb_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //点击身份证反面照片时的响应
        IDOpposite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchIDOpposite.getVisibility() == View.GONE) {
                    whichImage = "IDOpposite";
                    //弹出图片选择页面
                    PictureSelector.create(MyLocalPrivacy.this)
                            .openGallery(PictureMimeType.ofImage())
                            .selectionMode(PictureConfig.SINGLE)//设置为单选
                            .compress(true)// 是否压缩 true or false
                            .forResult(PictureConfig.CHOOSE_REQUEST);
                } else {
                    whichImage = "IDOpposite";
                    lookImage = "IDOppositeImage";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(MyLocalPrivacy.this, LookPhoto.class);
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
                    PictureSelector.create(MyLocalPrivacy.this)
                            .openGallery(PictureMimeType.ofImage())
                            .selectionMode(PictureConfig.SINGLE)//设置为单选
                            .compress(true)// 是否压缩 true or false
                            .forResult(PictureConfig.CHOOSE_REQUEST);
                } else {
                    whichImage = "IDPositive";
                    lookImage = "IDPositiveImage";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(MyLocalPrivacy.this, LookPhoto.class);
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
                    PictureSelector.create(MyLocalPrivacy.this)
                            .openGallery(PictureMimeType.ofImage())
                            .selectionMode(PictureConfig.SINGLE)//设置为单选
                            .compress(true)// 是否压缩 true or false
                            .forResult(PictureConfig.CHOOSE_REQUEST);
                } else {
                    whichImage = "bank";
                    lookImage = "bankImage";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(MyLocalPrivacy.this, LookPhoto.class);
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
                    PictureSelector.create(MyLocalPrivacy.this)
                            .openGallery(PictureMimeType.ofImage())
                            .selectionMode(PictureConfig.SINGLE)//设置为单选
                            .compress(true)// 是否压缩 true or false
                            .forResult(PictureConfig.CHOOSE_REQUEST);
                } else {
                    whichImage = "drivingLicense";
                    lookImage = "drivingLicenseImage";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(MyLocalPrivacy.this, LookPhoto.class);
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
                    PictureSelector.create(MyLocalPrivacy.this)
                            .openGallery(PictureMimeType.ofImage())
                            .selectionMode(PictureConfig.SINGLE)//设置为单选
                            .compress(true)// 是否压缩 true or false
                            .forResult(PictureConfig.CHOOSE_REQUEST);
                } else {
                    whichImage = "driverLicense";
                    lookImage = "driverLicenseImage";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(MyLocalPrivacy.this, LookPhoto.class);
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
                    PictureSelector.create(MyLocalPrivacy.this)
                            .openGallery(PictureMimeType.ofImage())
                            .selectionMode(PictureConfig.SINGLE)//设置为单选
                            .compress(true)// 是否压缩 true or false
                            .forResult(PictureConfig.CHOOSE_REQUEST);
                } else {
                    whichImage = "frameNumber";
                    lookImage = "frameNumberImage";
                    //从当前的界面跳转到图片查看界面
                    Intent intent = new Intent(MyLocalPrivacy.this, LookPhoto.class);
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
                PictureSelector.create(MyLocalPrivacy.this)
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
                PictureSelector.create(MyLocalPrivacy.this)
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
                PictureSelector.create(MyLocalPrivacy.this)
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
                PictureSelector.create(MyLocalPrivacy.this)
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
                PictureSelector.create(MyLocalPrivacy.this)
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
                PictureSelector.create(MyLocalPrivacy.this)
                        .openGallery(PictureMimeType.ofImage())
                        .selectionMode(PictureConfig.SINGLE)//设置为单选
                        .compress(true)// 是否压缩 true or false
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
        });
    }

    @Override
    protected void loadData() {

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
        PictureFileUtils.deleteCacheDirFile(MyLocalPrivacy.this);
    }

}
