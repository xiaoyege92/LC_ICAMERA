package com.example.administrator.lc_dvr.module.lc_report;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.Config;
import com.bumptech.glide.Glide;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.bean.Car;
import com.example.administrator.lc_dvr.bean.InsCompany;
import com.example.administrator.lc_dvr.bean.Unit;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.common.utils.observer.ObserverListener;
import com.example.administrator.lc_dvr.common.utils.observer.ObserverManager;
import com.example.administrator.lc_dvr.module.AppCaseData;
import com.example.administrator.lc_dvr.module.lc_help.PaymentDetail;
import com.example.administrator.lc_dvr.module.login_registration.InsuranceCompanyList;
import com.example.administrator.lc_dvr.module.login_registration.Register;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.PictureFileUtils;

import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 *
 */
public class InsInfoFragment extends BaseFragment implements ObserverListener{

    private TextView tv_ins_code; // 投保公司
    private TextView tv_ins_mobile; // 投保公司电话
    private ImageView iv_right; // 向右的箭头

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

    private String whichImage; // 是对那个照片进行操作
    private String lookImage; // 查看的是哪张照片

    private String license_bUrl;
    private String license_aUrl;
    private String cardUrl;
    private String drivelicenseUrl;
    private String permitUrl;
    private String frameUrl;

    @Override
    public void onResume() {
        super.onResume();

        //获得保险公司名字
        if(!"".equals(PreferenceUtil.getString("insuranceCompany", ""))) {
            tv_ins_code.setText(Utils.parseStr(PreferenceUtil.getString("insuranceCompany", "")));
            AppCaseData.carDetail.setInscode(VLCApplication.insuranceCompanyDictionary.get(PreferenceUtil.getString("insuranceCompany", "")));
            if("空".equals(PreferenceUtil.getString("insuranceCompany", ""))){
                tv_ins_code.setText("请选择投保公司");
                tv_ins_mobile.setText("");
            }
        }
        if(!"".equals(PreferenceUtil.getString("insuranceCompanyMobile", ""))) {
            //获得保险公司电话
            tv_ins_mobile.setText(Utils.parseStr(PreferenceUtil.getString("insuranceCompanyMobile", null)));
        }

        if(AppCaseData.unitDetail.getUnitkind() == 2 ) {
            iv_right.setVisibility(View.GONE);
            tv_ins_code.setClickable(false);
        }else {
            iv_right.setVisibility(View.VISIBLE);
            tv_ins_code.setClickable(true);
        }

        if (PreferenceUtil.getBoolean("isDeleteImage", false)) {
            switch (lookImage) {
                case "IDOpposite":
                    //赋值，图片视图显示图片
                    IDOpposite.setImageResource(R.mipmap.id_opposite);
                    //隐藏switchIDOppositeImage
                    switchIDOpposite.setVisibility(View.GONE);
                    license_bUrl="";
                    AppCaseData.carDetail.setLicenseB(null);
                    break;
                case "IDPositive":
                    //赋值，图片视图显示图片
                    IDPositive.setImageResource(R.mipmap.id_positive);
                    //隐藏switchIDPositiveImage
                    switchIDPositive.setVisibility(View.GONE);
                    license_aUrl = "";
                    AppCaseData.carDetail.setLicenseA(null);
                    break;
                case "bank":
                    //赋值，图片视图显示图片
                    bank.setImageResource(R.mipmap.bank);
                    //隐藏switchBankImage
                    switchBank.setVisibility(View.GONE);
                    cardUrl = "";
                    AppCaseData.carDetail.setCard(null);
                    break;
                case "drivingLicense":
                    //赋值，图片视图显示图片
                    drivingLicense.setImageResource(R.mipmap.driving_license);
                    //隐藏switchDrivingLicenseImage
                    switchDrivingLicense.setVisibility(View.GONE);
                    permitUrl = "";
                    AppCaseData.carDetail.setPermit(null);
                    break;
                case "frameNumber":
                    //赋值，图片视图显示图片
                    frameNumber.setImageResource(R.mipmap.frame_number);
                    //隐藏switchFrameNumberImage
                    switchFrameNumber.setVisibility(View.GONE);
                    frameUrl = "";
                    AppCaseData.carDetail.setCarframe(null);
                    break;
                case "driverLicense":
                    //赋值，图片视图显示图片
                    driverLicense.setImageResource(R.mipmap.driver_license);
                    //隐藏switchDriverLicenseImage
                    switchDriverLicense.setVisibility(View.GONE);
                    drivelicenseUrl = "";
                    AppCaseData.carDetail.setDrivelicense(null);
                    break;
                default:
                    break;
            }
            PreferenceUtil.commitBoolean("isDeleteImage", false);
        }

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden) {
            onResume();
        }
    }

    @Override
    protected int setViewId() {
        return R.layout.fragment_ins_info;
    }

    @Override
    protected void findView(View view) {
        tv_ins_code =  (TextView) view.findViewById(R.id.tv_ins_code);
        tv_ins_mobile =  (TextView) view.findViewById(R.id.tv_ins_mobile);
        iv_right =  (ImageView) view.findViewById(R.id.iv_right);

        IDOpposite = (ImageView) view.findViewById(R.id.IDOpposite);
        IDPositive = (ImageView) view.findViewById(R.id.IDPositive);
        bank = (ImageView) view.findViewById(R.id.bank);
        drivingLicense = (ImageView) view.findViewById(R.id.drivingLicense);
        driverLicense = (ImageView) view.findViewById(R.id.driverLicense);
        frameNumber = (ImageView) view.findViewById(R.id.frameNumber);

        switchIDOpposite = (ImageView) view.findViewById(R.id.switchIDOpposite);
        switchIDPositive = (ImageView) view.findViewById(R.id.switchIDPositive);
        switchBank = (ImageView) view.findViewById(R.id.switchBank);
        switchDrivingLicense = (ImageView) view.findViewById(R.id.switchDrivingLicense);
        switchDriverLicense = (ImageView) view.findViewById(R.id.switchDriverLicense);
        switchFrameNumber = (ImageView) view.findViewById(R.id.switchFrameNumber);
    }

    @Override
    protected void init() {

        if(null == AppCaseData.unitDetail) {
            AppCaseData.unitDetail = new Unit();
        }
        if(null == AppCaseData.carDetail) {
            AppCaseData.carDetail = new Car();
        }
        if(null == AppCaseData.insCompanyDetail) {
            AppCaseData.insCompanyDetail = new InsCompany();
        }

         tv_ins_code.setText(Utils.parseStr(AppCaseData.insCompanyDetail.getCompanyname()));
         tv_ins_mobile.setText(Utils.parseStr(AppCaseData.insCompanyDetail.getCompanymobile()));

        // 如果有照片，switch图片不显示，否则显示    :身份证正面
        if(null == AppCaseData.carDetail.getLicenseA() || "".equals(AppCaseData.carDetail.getLicenseA())) {
            switchIDPositive.setVisibility(View.GONE);
        }else {
            switchIDPositive.setVisibility(View.VISIBLE);
            // 身份证正面照片
            license_aUrl = Config.QINIU_BASE_URL + AppCaseData.carDetail.getLicenseA();
            Glide.with(getActivity()).load(license_aUrl).into(IDPositive);
        }
        // 身份证反面
        if(null == AppCaseData.carDetail.getLicenseB() || "".equals(AppCaseData.carDetail.getLicenseB())) {
            switchIDOpposite.setVisibility(View.GONE);
        }else {
            switchIDOpposite.setVisibility(View.VISIBLE);
            // 身份证反面照片
            license_bUrl = Config.QINIU_BASE_URL +AppCaseData.carDetail.getLicenseB();
            Glide.with(getActivity()).load(Config.QINIU_BASE_URL +AppCaseData.carDetail.getLicenseB()).into(IDOpposite);
        }
        // 银行卡
        if(null == AppCaseData.carDetail.getCard() || "".equals(AppCaseData.carDetail.getCard())) {
            switchBank.setVisibility(View.GONE);
        }else {
            switchBank.setVisibility(View.VISIBLE);
            // 银行卡照片
            cardUrl = Config.QINIU_BASE_URL +AppCaseData.carDetail.getCard();
            Glide.with(getActivity()).load(cardUrl).into(bank);
        }
        // 车架号
        if(null == AppCaseData.carDetail.getCarframe() || "".equals(AppCaseData.carDetail.getCarframe())) {
            switchFrameNumber.setVisibility(View.GONE);
        }else {
            switchFrameNumber.setVisibility(View.VISIBLE);
            // 车架号照片
            frameUrl = Config.QINIU_BASE_URL +AppCaseData.carDetail.getCarframe();
            Glide.with(getActivity()).load(frameUrl).into(frameNumber);
        }
        // 行驶证
        if(null == AppCaseData.carDetail.getDrivelicense() || "".equals(AppCaseData.carDetail.getDrivelicense())) {
            switchDrivingLicense.setVisibility(View.GONE);
        }else {
            switchDrivingLicense.setVisibility(View.VISIBLE);
            // 行驶证照片
            drivelicenseUrl = Config.QINIU_BASE_URL +AppCaseData.carDetail.getDrivelicense();
            Glide.with(getActivity()).load(drivelicenseUrl).into(drivingLicense);
        }
        // 驾驶证
        if(null == AppCaseData.carDetail.getPermit() || "".equals(AppCaseData.carDetail.getPermit())) {
            switchDriverLicense.setVisibility(View.GONE);
        }else {
            switchDriverLicense.setVisibility(View.VISIBLE);
            // 驾驶证
            permitUrl = Config.QINIU_BASE_URL + AppCaseData.carDetail.getPermit();
            Glide.with(getActivity()).load(permitUrl).into(driverLicense);
        }

        ObserverManager.getInstance().add(this);
    }

    @Override
    protected void initEvents() {
        tv_ins_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断是否联网
                if (!NetUtils.isNetworkConnected(getActivity())) {
                    ToastUtils.showNomalShortToast(getActivity(), getString(R.string.network_off));
                    return;
                }
                //跳转到保险公司列表界面
                Intent intent = new Intent(getActivity(), InsuranceCompanyList.class);
                startActivity(intent);
            }
        });
        tv_ins_mobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(null != tv_ins_mobile.getText() && !"".equals(tv_ins_mobile.getText().toString())) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tv_ins_mobile.getText().toString()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
        //点击身份证反面照片时的响应
        IDOpposite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichImage = "IDOpposite";
                lookImage = "IDOpposite";
                if (switchIDOpposite.getVisibility() == View.GONE) {
                    //弹出图片选择页面
                    openPictureSelector();
                } else {
                    openPictureDetail(license_bUrl);
                }
            }
        });
        //点击身份证正面照片时的响应
        IDPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lookImage = "IDPositive";
                whichImage = "IDPositive";
                if (switchIDPositive.getVisibility() == View.GONE) {
                    //弹出图片选择页面
                    openPictureSelector();
                } else {
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
                    //弹出图片选择页面
                    openPictureSelector();
                } else {
                    openPictureDetail(cardUrl);
                }
            }
        });
        //点击行驶证照片时的响应
        drivingLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lookImage = "drivingLicense";
                whichImage = "drivingLicense";
                if (switchDrivingLicense.getVisibility() == View.GONE) {
                    //弹出图片选择页面
                    openPictureSelector();
                } else {
                    openPictureDetail(drivelicenseUrl);
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
                        //弹出图片选择页面
                    openPictureSelector();
                } else {
                    //从当前的界面跳转到图片查看界面
                    openPictureDetail(permitUrl);
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
                    //弹出图片选择页面
                    openPictureSelector();
                } else {
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

    @Override
    protected void loadData() {

    }

    /**
     * 查看大图详情
     * @param url
     */
    private void openPictureDetail(String url) {
        Intent intent = new Intent(getActivity(), LookPhoto.class);
        intent.putExtra("whichImage", url);
        intent.putExtra("lookImage", lookImage);
        intent.putExtra("lotusStatus", true);
        startActivity(intent);
    }
    /**
     * 打开图片选择器
     */
    private void openPictureSelector() {
        //弹出图片选择页面
        PictureSelector.create(InsInfoFragment.this)
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
                                    IDOpposite.setImageDrawable(null);
                                    //缓存图片
                                    try {
                                        saveMyBitmap2(BitmapFactory.decodeFile(picturePath), "IDOpposite");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "IDPositive":
                                    IDPositive.setImageDrawable(null);
                                    //缓存图片
                                    try {
                                        saveMyBitmap2(BitmapFactory.decodeFile(picturePath), "IDPositive");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "bank":
                                    bank.setImageDrawable(null);
                                    //缓存图片
                                    try {
                                        saveMyBitmap2(BitmapFactory.decodeFile(picturePath), "bank");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "drivingLicense":
                                    drivingLicense.setImageDrawable(null);
                                    //缓存图片
                                    try {
                                        saveMyBitmap2(BitmapFactory.decodeFile(picturePath), "drivingLicense");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "frameNumber":
                                    frameNumber.setImageDrawable(null);
                                    //缓存图片
                                    try {
                                        saveMyBitmap2(BitmapFactory.decodeFile(picturePath), "frameNumber");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "driverLicense":
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
        PictureFileUtils.deleteCacheDirFile(getActivity());
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
                    AppCaseData.carDetail.setLicenseB(license_bUrl);
                    break;
                case "IDPositive":
                    //赋值，图片视图显示图片
                    IDPositive.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png"));
                    //显示switchIDPositiveImage
                    switchIDPositive.setVisibility(View.VISIBLE);
                    license_aUrl = BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png";
                    AppCaseData.carDetail.setLicenseA(license_aUrl);
                    break;
                case "bank":
                    //赋值，图片视图显示图片
                    bank.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/bank.png"));
                    //显示switchBankImage
                    switchBank.setVisibility(View.VISIBLE);
                    cardUrl = BitmapUtils.getSDPath() + "/VOC/Cache/bank.png";
                    AppCaseData.carDetail.setCard(cardUrl);
                    break;
                case "drivingLicense":
                    //赋值，图片视图显示图片
                    drivingLicense.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png"));
                    //显示switchDrivingLicenseImage
                    switchDrivingLicense.setVisibility(View.VISIBLE);
                    drivelicenseUrl = BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png";
                    AppCaseData.carDetail.setDrivelicense(drivelicenseUrl);
                    break;
                case "frameNumber":
                    //赋值，图片视图显示图片
                    frameNumber.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png"));
                    //显示switchFrameNumberImage
                    switchFrameNumber.setVisibility(View.VISIBLE);
                    frameUrl = BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png";
                    AppCaseData.carDetail.setCarframe(frameUrl);
                    break;
                case "driverLicense":
                    //赋值，图片视图显示图片
                    driverLicense.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png"));
                    //显示switchDriverLicenseImage
                    switchDriverLicense.setVisibility(View.VISIBLE);
                    permitUrl = BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png";
                    AppCaseData.carDetail.setPermit(permitUrl);
                    break;
                default:
                    break;
            }
        }
    };


    /**
     *  将内容赋予 APPCaseData.carDetail 用于 内容上传
     * @param content
     */
    @Override
    public void observerUpData(String content) {
        // 投保公司 code
        if(!"".equals(PreferenceUtil.getString("insuranceCompany", ""))) {
            AppCaseData.carDetail.setInscode(VLCApplication.insuranceCompanyDictionary.get(PreferenceUtil.getString("insuranceCompany", "")));
        }
    }

}
