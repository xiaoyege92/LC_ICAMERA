package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

/**
 * Created by yangboru on 2018/1/6.
 */

public class LookPhoto extends BaseActivity {

    private MatrixImageView personalInformationImage;
    private TextView tvWhichImage;
    private String whichImage;
    private boolean lotusStatus = false;
    private RadioButton deleteRB;
    private String lookImage;

    private boolean isLocalPrivacy;//是否

    @Override
    protected int setViewId() {
        return R.layout.look_photo_layout;
    }

    @Override
    protected void findView() {
        personalInformationImage = (MatrixImageView) findViewById(R.id.personalInformationImage);
        tvWhichImage = (TextView) findViewById(R.id.tvWhichImage);
    }

    @Override
    protected void init() {

        deleteRB = (RadioButton) findViewById(R.id.photo_delete_btn);

        Intent intent = getIntent();
        whichImage = intent.getStringExtra("whichImage");
        lookImage = intent.getStringExtra("lookImage");
        // 如果是本地进入查看照片界面
        isLocalPrivacy = intent.getBooleanExtra("isLocalPrivacy",false);
        setTitle();
        if(null != whichImage) {
            if (whichImage.contains("http")){
                //从网络中获得图片
                ImageLoader.getInstance().displayImage(whichImage, personalInformationImage);
            }else {
                personalInformationImage.setImageURI(Uri.fromFile(new File(whichImage)));
            }
        }
        lotusStatus = intent.getBooleanExtra("lotusStatus",false);
        if(!lotusStatus) {
            deleteRB.setVisibility(View.GONE);
        }

    }

    @Override
    protected void initEvents() {

    }

    @Override
    protected void loadData() {

    }

    /**
     * 设置相应标题内容
     */
    private void setTitle() {
        // 根据传递照片信息，更改title内容
        if("IDOpposite".equals(whichImage) || "IDOpposite".equals(lookImage)) { //身份证反面照片
            tvWhichImage.setText(R.string.personal_photo_IDOpposite);
        }else if("IDPositive".equals(whichImage) || "IDPositive".equals(lookImage)) {//身份证正面照片
            tvWhichImage.setText(R.string.personal_photo_IDPositive);
        }else if("bank".equals(whichImage) || "bank".equals(lookImage)) {//银行卡照片
            tvWhichImage.setText(R.string.personal_photo_bank);
        }else if("drivingLicense".equals(whichImage) || "drivingLicense".equals(lookImage)) {//行驶证照片
            tvWhichImage.setText(R.string.personal_photo_drivingLicense);
        }else if("driverLicense".equals(whichImage) || "driverLicense".equals(lookImage)) {// 驾驶证照片
            tvWhichImage.setText(R.string.personal_photo_driverLicense);
        }else if("frameNumber".equals(whichImage) || "frameNumber".equals(lookImage)) {//车架号照片
            tvWhichImage.setText(R.string.personal_photo_frameNumber);
        }
    }

    /**
     * 退出当前的页面
     * @param view
     */
    public void closeLookPhoto(View view) {
        finish();
    }

    /**
     * 点击删除按钮时的响应
     * @param view
     */
    public void deleteImage(View view) {
        //删除缓存的图片
        File imageFile = new File(whichImage);
        if (imageFile.exists()) {
            imageFile.delete();
        }
        if(isLocalPrivacy) {
            PreferenceUtil.commitBoolean("isEditingPhoto",true);
        }
        PreferenceUtil.commitBoolean("isDeleteImage",true);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
