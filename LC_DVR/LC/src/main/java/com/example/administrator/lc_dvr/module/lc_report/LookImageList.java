package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.administrator.lc_dvr.GuideViewAdapter;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.common.adapter.PhotoViewAdapter;
import com.example.administrator.lc_dvr.common.adapter.PhotoViewPager;
import com.example.administrator.lc_dvr.common.utils.ListDataSave;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.circlenavigator.CircleNavigator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangboru on 2018/1/31.
 * 查看照片
 */

public class LookImageList extends BaseActivity {

    private TextView tvTitle;
    private int numOfPages;
    private PhotoViewPager imagePageView;
    private List<String> imageArr;
    private LocalBroadcastManager localBroadcastManager;
    private int whichPage;
    private String imageUrl;
    private ListDataSave dataSave;
    private List<String> reportPhotoArr;
    private Boolean isEditReport;
    private RadioButton deleteBtn;
    private int whichList;

    private int currentPage;//图片的当前页
    private List<String> photoViewList; //图片URL
    private PhotoViewAdapter photoViewAdapter;//图片适配器
    @Override
    protected int setViewId() {
        return R.layout.lookimagelist_layout;
    }

    @Override
    protected void findView() {
        imagePageView = (PhotoViewPager) findViewById(R.id.imagePageView);
        deleteBtn = (RadioButton) findViewById(R.id.deleteBtn);
        tvTitle = (TextView) findViewById(R.id.online_report_title);
    }

    @Override
    protected void init() {
        // 获得传过来的当前图片页
        Intent intent = getIntent();
        currentPage = intent.getIntExtra("current",0);
        //得到本地广播管理器的实例
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        //获得页面传过来的值
        isEditReport = PreferenceUtil.getBoolean("isEditReport", true);
        numOfPages = PreferenceUtil.getInt("numOfPages", -1);
        whichList = PreferenceUtil.getInt("whichList", -1);

        setTitle();
        //判断是否要隐藏删除按钮
        if (!isEditReport) {
            deleteBtn.setVisibility(View.GONE);
            numOfPages += 1;
        }
        //用来保存list到本地
        dataSave = new ListDataSave(this, "baiyu");

        //图片List
        imageArr = new ArrayList<>();

        //获得界面传过来的List
        reportPhotoArr = dataSave.getDataList("LookImageList");
        //photoViewPage适配器
        photoViewAdapter = new PhotoViewAdapter(imageArr,this);
        //给imageArr赋值
        for (int i = 0; i < numOfPages; i++) {
            imageArr.add(reportPhotoArr.get(i));
            //如果是最后一个就刷新列表
            if (i == numOfPages - 1) {
                //更新数据
                photoViewAdapter.notifyDataSetChanged();
            }
        }
        imagePageView.setAdapter(photoViewAdapter);
        // 设置当前的图片为传过来的图片
        imagePageView.setCurrentItem(currentPage);
        imagePageView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 设置标题
     */
    private void setTitle() {
        switch (whichList) {
            case 1:
                if (PreferenceUtil.getString("accidentkind", "").equals("多车")){
                    //撞击部位45度照片
                    tvTitle.setText("撞击部位45度照片");
                }else {
                    //被撞物体照片
                    tvTitle.setText("被撞物体照片");
                }
                break;
            case 2:
                if (PreferenceUtil.getString("accidentkind", "").equals("多车")){
                    // 对方车带牌照照片
                    tvTitle.setText("对方车带牌照照片");
                }else {
                    tvTitle.setText("受损部位照片");
                }

                break;
            case 4:
                //设置顶部导航栏的标题和其他文本
                tvTitle.setText("自己车受损部位照片");
                break;
            case 5:
                //设置顶部导航栏的标题和其他文本
                tvTitle.setText("对方车受损部位照片");
                break;
            case 0:
                if (PreferenceUtil.getString("accidentkind", "").equals("多车")){
                    //自己车带牌照照片
                    tvTitle.setText("自己车带牌照照片");
                }else {
                    //全车45度照片
                    tvTitle.setText("全车45度带车牌照片");
                }
                break;
            case -1:
                tvTitle.setText("维修前照片");
                break;
            case -2:
                tvTitle.setText("维修后照片");
                break;
            case -3:
                tvTitle.setText("评价照片");
                break;
            default:
                break;
        }
    }


    @Override
    protected void initEvents() {
        // 2.监听当前显示的页面，将对应的小圆点设置为选中状态，其它设置为未选中状态
        imagePageView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //保存当前是那一页
                whichPage = position;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    protected void loadData() {

    }

    /**
     * 删除图片
     *
     * @param view
     */
    public void deletePhoto(View view) {
        if (numOfPages == 1) {
            //保存还有多少张图片没有删除
            PreferenceUtil.commitInt("numOfPages", 0);
            //发送本地广播
            Intent intent = new Intent("ReportPhoto");
            localBroadcastManager.sendBroadcast(intent);
            //隐藏当前的视图
            finish();
        } else {
            numOfPages -= 1;
            //设置UIPageControl的页数
            imageArr.remove(whichPage);
            reportPhotoArr.remove(whichPage);
            //刷新数据
            photoViewAdapter.notifyDataSetChanged();
            //刷新完数据后就跳转到第一项，不然会跟指示器对不上
            imagePageView.setCurrentItem(0);
            //重新设置指示器圆点的数量
        }
    }

    /**
     * 返回上一个页面
     *
     * @param view
     */
    public void closeLookImageList(View view) {
        if (whichList == -1 || whichList == -2 || whichList == -3){
            //隐藏当前的视图
            finish();
        }else {
            //保存还有多少张图片没有删除
            dataSave.setDataList("remain_image", reportPhotoArr);

            PreferenceUtil.commitInt("numOfPages", numOfPages);
            //发送本地广播
            Intent intent = new Intent("ReportPhoto");
            localBroadcastManager.sendBroadcast(intent);
            //隐藏当前的视图
            finish();
        }
    }

}
