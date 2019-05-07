package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.common.utils.ListDataSave;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.module.AppCaseData;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.PictureFileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangboru on 2018/1/31.
 * <p>
 * 开始拍照导航页面
 */

public class TakeReportPhoto extends BaseActivity {

    private int whichList;
    private LocalBroadcastManager localBroadcastManager;
    private List<LocalMedia> selectList;
    private TextView takePhotoTip1;
    private TextView takePhotoTip2;
    private TextView takeReportPhotoTitle;
    private ImageView takePhotoCar;
    private List<String> saveImageUrl;
    private ListDataSave dataSave;

    private RadioButton rb_back;

    @Override
    protected int setViewId() {
        return R.layout.takereportphoto_layout;
    }

    @Override
    protected void findView() {
        takePhotoTip1 = (TextView) findViewById(R.id.takePhotoTip1);
        takePhotoTip2 = (TextView) findViewById(R.id.takePhotoTip2);
        takeReportPhotoTitle = (TextView) findViewById(R.id.takeReportPhotoTitle);
        takePhotoCar = (ImageView) findViewById(R.id.takePhotoCar);
        rb_back = (RadioButton) findViewById(R.id.rb_back);
    }

    @Override
    protected void init() {

        //用来保存list到本地
        dataSave = new ListDataSave(this, "baiyu");

        //保存图片的地址
        saveImageUrl = new ArrayList<>();

        //获得页面传过来的值，判断是那个列表的图片
        whichList = PreferenceUtil.getInt("whichList", -1);

        //得到本地广播管理器的实例
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        switch (whichList) {
            case 1:
                if (2 == AppCaseData.carCount) {
                    //撞击部位45度照片
                    takeReportPhotoTitle.setText("撞击部位45度照片");
                    takePhotoTip1.setText("请按照以下示意图拍摄撞击部位45度照片");
                    takePhotoCar.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.car_more_accident));
                    takePhotoTip2.setText("请距车3-5米，与车头约45度角拍出事故现场全貌，保证车牌清晰，全车入境");
                } else {
                    //被撞物体照片
                    takeReportPhotoTitle.setText("被撞物体照片");
                    takePhotoTip1.setText("请按照以下示意图拍摄您的被撞物体照片");
                    takePhotoCar.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.car_one_body));
                    takePhotoTip2.setText("如果撞比较大的物体，要拍到被撞击物的外观及整体照片，而不能只拍被撞部位的局部");
                }
                break;
            case 2:
                if (2 == AppCaseData.carCount) {
                    // 对方车带牌照照片
                    takeReportPhotoTitle.setText("对方车带牌照照片");
                    takePhotoTip1.setText("请按照以下示意图拍摄对方车带牌照照片");
                    takePhotoCar.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.car_more_other));
                    takePhotoTip2.setText("请距车3-5米，与车头约45度角，拍摄对方车辆的全貌并保证车牌清晰、全车入镜");
                } else {
                    takeReportPhotoTitle.setText("受损部位照片");
                    takePhotoTip1.setText("请按照以下示意图拍摄您的受损部位照片");
                    takePhotoCar.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.car_one_damage));
                    takePhotoTip2.setText("请拍摄车损部位整体照片，例：车门剐蹭请拍摄整个车门，可多角度拍摄。较小车损部位可在距离10-30cm处拍摄，且多角度拍摄");
                }

                break;
            case 4:

                //设置顶部导航栏的标题和其他文本
                takeReportPhotoTitle.setText("自己车受损部位照片");
                takePhotoTip1.setText("请按照以下示意图拍摄自己车受损部位照片");
                takePhotoCar.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.car_more_damage_my));
                takePhotoTip2.setText("请拍摄车损部位整体照片，例：车门剐蹭请拍摄整个车门，可多角度拍摄。较小车损部位可在距离10-30cm处拍摄，且多角度拍摄");
                break;
            case 5:

                //设置顶部导航栏的标题和其他文本
                takeReportPhotoTitle.setText("对方车受损部位照片");
                takePhotoTip1.setText("请按照以下示意图拍摄对方车受损部位照片");
                takePhotoCar.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.car_more_damage_other));
                takePhotoTip2.setText("请拍摄车损部位整体照片，例：车门剐蹭请拍摄整个车门，可多角度拍摄。较小车损部位可在距离10-30cm处拍摄，且多角度拍摄");
                break;
            case 0:
                if (2 == AppCaseData.carCount) {
                    //自己车带牌照照片
                    takeReportPhotoTitle.setText("自己车带牌照照片");
                    takePhotoTip1.setText("请按照以下示意图拍摄自己车带牌照照片");
                    takePhotoCar.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.car_more_my));
                    takePhotoTip2.setText("请距车3-5米，与车头约45度角，拍摄自己车辆的全貌并保证车牌清晰、全车入镜");
                } else {
                    //全车45度照片
                    takeReportPhotoTitle.setText("全车45度带车牌照片");
                    takePhotoTip1.setText("请按照以下示意图拍摄您的全车45度带车牌照片");
                    takePhotoCar.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.take_report_image));
                    takePhotoTip2.setText("请距车3-5米，与车头约45度角拍出事故现场全貌，保证车牌清晰，全车入境");
                }
                break;
            default:
                break;
        }

    }

    @Override
    protected void initEvents() {
        rb_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                // 图片选择结果回调
                selectList = PictureSelector.obtainMultipleResult(data);
                // 例如 LocalMedia 里面返回三种path
                // 1.media.getPath(); 为原图path
                // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的

                switch (whichList) {
                    case 1:

                        for (int i = 0; i < selectList.size(); i++) {
                            //如何图片压缩成功了就取得压缩后的图片地址
                            if (selectList.get(i).isCompressed()) {
                                String picturePath = selectList.get(i).getCompressPath();
                                //保存图片地址
                                saveImageUrl.add("file://" + picturePath);
                                //如果是最后一张就执行下面的操作
                                if (i == selectList.size() - 1) {
                                    //保存图片地址list
                                    dataSave.setDataList("takeHitPhoto", saveImageUrl);
                                    //保存用户选择了多少张图片
                                    PreferenceUtil.commitInt("selectCount", selectList.size());
                                    //发送本地广播
                                    Intent intent = new Intent("ReportPhoto");
                                    localBroadcastManager.sendBroadcast(intent);
                                    //隐藏当前的视图
                                    finish();
                                }
                            }
                        }

                        break;
                    case 2:

                        for (int i = 0; i < selectList.size(); i++) {
                            //如何图片压缩成功了就取得压缩后的图片地址
                            if (selectList.get(i).isCompressed()) {
                                String picturePath = selectList.get(i).getCompressPath();
                                //保存图片地址
                                saveImageUrl.add("file://" + picturePath);
                                //如果是最后一张就执行下面的操作
                                if (i == selectList.size() - 1) {
                                    //保存图片地址list
                                    dataSave.setDataList("takeDamagedPhoto", saveImageUrl);
                                    //保存用户选择了多少张图片
                                    PreferenceUtil.commitInt("selectCount", selectList.size());
                                    //发送本地广播
                                    Intent intent = new Intent("ReportPhoto");
                                    localBroadcastManager.sendBroadcast(intent);
                                    //隐藏当前的视图
                                    finish();
                                }
                            }
                        }

                        break;
                    case 4:

                        for (int i = 0; i < selectList.size(); i++) {
                            //如何图片压缩成功了就取得压缩后的图片地址
                            if (selectList.get(i).isCompressed()) {
                                String picturePath = selectList.get(i).getCompressPath();
                                //保存图片地址
                                saveImageUrl.add("file://" + picturePath);
                                //如果是最后一张就执行下面的操作
                                if (i == selectList.size() - 1) {
                                    //保存图片地址list
                                    dataSave.setDataList("takeDamaged4Photo", saveImageUrl);
                                    //保存用户选择了多少张图片
                                    PreferenceUtil.commitInt("selectCount", selectList.size());
                                    //发送本地广播
                                    Intent intent = new Intent("ReportPhoto");
                                    localBroadcastManager.sendBroadcast(intent);
                                    //隐藏当前的视图
                                    finish();
                                }
                            }
                        }

                        break;
                    case 5:

                        for (int i = 0; i < selectList.size(); i++) {
                            //如何图片压缩成功了就取得压缩后的图片地址
                            if (selectList.get(i).isCompressed()) {
                                String picturePath = selectList.get(i).getCompressPath();
                                //保存图片地址
                                saveImageUrl.add("file://" + picturePath);
                                //如果是最后一张就执行下面的操作
                                if (i == selectList.size() - 1) {
                                    //保存图片地址list
                                    dataSave.setDataList("takeDamaged5Photo", saveImageUrl);
                                    //保存用户选择了多少张图片
                                    PreferenceUtil.commitInt("selectCount", selectList.size());
                                    //发送本地广播
                                    Intent intent = new Intent("ReportPhoto");
                                    localBroadcastManager.sendBroadcast(intent);
                                    //隐藏当前的视图
                                    finish();
                                }
                            }
                        }

                        break;
                    case 0:

                        for (int i = 0; i < selectList.size(); i++) {
                            //如何图片压缩成功了就取得压缩后的图片地址
                            if (selectList.get(i).isCompressed()) {
                                String picturePath = selectList.get(i).getCompressPath();
                                //保存图片地址
                                PreferenceUtil.commitString("takeWholeCarPhoto" + i, "file://" + picturePath);
                                //如果是最后一张就执行下面的操作
                                if (i == selectList.size() - 1) {
                                    //发送本地广播
                                    Intent intent = new Intent("ReportPhoto");
                                    localBroadcastManager.sendBroadcast(intent);
                                    //隐藏当前的视图
                                    finish();
                                }
                            }
                        }

                        break;
                    default:
                        break;
                }
            }
        }
        //包括裁剪和压缩后的缓存，要在上传成功后调用，注意：需要系统sd卡权限
        PictureFileUtils.deleteCacheDirFile(this);

    }

    /**
     * 返回上一个页面
     *
     * @param view
     */
    public void closeTakeReportPhoto(View view) {
        finish();
    }

    /**
     * 点击开始拍照按钮时的响应
     *
     * @param view
     */
    public void takePhotoTap(View view) {
        //弹出图片选择页面
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .selectionMode(PictureConfig.SINGLE)//设置为单选
                .compress(true)// 是否压缩 true or false
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

}
