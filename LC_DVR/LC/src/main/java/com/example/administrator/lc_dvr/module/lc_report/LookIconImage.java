package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RadioButton;

import com.Config;
import com.bumptech.glide.Glide;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.module.AppCaseData;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.PictureFileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/11/26
 *   desc   :
 *  version :
 * </pre>
 */
public class LookIconImage extends BaseActivity implements View.OnClickListener {

    private RadioButton rb_back; // 返回
    private RadioButton photo_switch_btn; //拍照或者相册看图
    private MatrixImageView personalInformationImage; // 照片

    private String headUrl; // 头像路径

    @Override
    protected int setViewId() {
        return R.layout.look_icon_image;
    }

    @Override
    protected void findView() {
        rb_back = (RadioButton) findViewById(R.id.rb_back);
        photo_switch_btn = (RadioButton) findViewById(R.id.photo_switch_btn);
        personalInformationImage = (MatrixImageView) findViewById(R.id.personalInformationImage);
    }

    @Override
    protected void init() {

    }

    @Override
    protected void initEvents() {
        rb_back.setOnClickListener(this);
        photo_switch_btn.setOnClickListener(this);
    }

    @Override
    protected void loadData() {
        Intent intent = getIntent();
        headUrl = intent.getStringExtra("headUrl");
        if (null != headUrl && !"".equals(headUrl)
                && !"null".equals(headUrl) && !(Config.QINIU_BASE_URL + "null").equals(headUrl)) {

            Glide.with(LookIconImage.this).load(headUrl).into(personalInformationImage);
        } else {
            personalInformationImage.setImageResource(R.mipmap.person_icon);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_back:
                finish();
                break;
            case R.id.photo_switch_btn:
                openPictureSelector();
                break;
        }
    }

    /**
     * 打开图片选择器
     */
    private void openPictureSelector() {
        //弹出图片选择页面
        PictureSelector.create(LookIconImage.this)
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

                    for (int i = 0; i < selectList.size(); i++) {
                        //如何图片压缩成功了就取得压缩后的图片地址
                        if (selectList.get(i).isCompressed()) {
                            String picturePath = selectList.get(i).getCompressPath();
                            //缓存图片
                            try {
                                saveMyBitmap2(BitmapFactory.decodeFile(picturePath), "head");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
            }
        }
        //包括裁剪和压缩后的缓存，要在上传成功后调用，注意：需要系统sd卡权限
        PictureFileUtils.deleteCacheDirFile(LookIconImage.this);

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
                case "head":
                    AppCaseData.headURL = BitmapUtils.getSDPath() + "/VOC/Cache/head.png";
                    personalInformationImage.setImageBitmap(BitmapFactory.decodeFile(BitmapUtils.getSDPath() + "/VOC/Cache/head.png"));
                    break;
            }
        }
    };

}
