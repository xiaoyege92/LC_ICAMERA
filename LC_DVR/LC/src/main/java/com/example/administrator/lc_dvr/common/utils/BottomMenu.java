package com.example.administrator.lc_dvr.common.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.common.customview.BadgeView;

/**
 * 一般自定义的控件，放在这个widget包里
 * Created by Administrator on 2016/4/6.
 */
public class BottomMenu extends LinearLayout {

    private TextView tvMenu;
    private ImageView ivMenu;
    private boolean mBSelete;//保存上一次的点击
    private int normalpicId;//没选中时的图片
    private int presspicId;//选中时的图片
    private BadgeView badgeView;
    private ImageView iv_red_circler;

    public BottomMenu(Context context) {
        super(context);
    }

    public BottomMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBSelete = false;
        View view = LayoutInflater.from(context).inflate(R.layout.layout_bottommenu, this);
        findViews(view);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BottomMenu);
        String strText = typedArray.getString(R.styleable.BottomMenu_text);
        normalpicId = typedArray.getResourceId(R.styleable.BottomMenu_normalpic, -1);
        presspicId = typedArray.getResourceId(R.styleable.BottomMenu_presspic, -1);
        tvMenu.setText(strText);
        ivMenu.setImageResource(normalpicId);
    }

    public void onSelete() {
        if (mBSelete) {
            return;
        }
        tvMenu.setTextColor(getResources().getColor(R.color.switch_color));
        ivMenu.setImageResource(presspicId);
    }

    public void onSuoFang() {
        tvMenu.setTextColor(getResources().getColor(R.color.alphablack));
        ivMenu.setImageResource(normalpicId);
    }

    /**
     * 显示消息数红点
     *
     * @param num
     */
    public void showMsg(int num) {
        badgeView.setVisibility(VISIBLE);
        badgeView.setText(num + "");
    }


    public void showRedCircle() {
        iv_red_circler.setVisibility(VISIBLE);
    }

    public void hideRedCircle() {
        iv_red_circler.setVisibility(GONE);
    }

    /**
     * 隐藏消息数红点
     */
    public void hideMsg() {
        badgeView.setVisibility(GONE);
    }

    public BottomMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void findViews(View view) {
        ivMenu = (ImageView) view.findViewById(R.id.ivmenu);
        tvMenu = (TextView) view.findViewById(R.id.tvmenu);
        badgeView = (BadgeView) view.findViewById(R.id.badgeView);
        iv_red_circler = (ImageView) findViewById(R.id.iv_red_circler);
        // 默认隐藏消息
        hideMsg();
    }

}
