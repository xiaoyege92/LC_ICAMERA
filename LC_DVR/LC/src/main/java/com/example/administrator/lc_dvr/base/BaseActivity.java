package com.example.administrator.lc_dvr.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

/**
 * base包都是拿来存放一些基类的
 * Created by Administrator on 2016/4/5.
 */
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setViewId());
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        findView();
        init();
        initEvents();
        loadData();
    }

    /**
     * 设置layout的资源id
     *
     * @return
     */
    protected abstract int setViewId();

    /**
     * 查找子控件
     */
    protected abstract void findView();

    /**
     * 初始化
     */
    protected abstract void init();

    /**
     * 对控件设置事件监听
     */
    protected abstract void initEvents();

    /**
     * 加载数据
     */
    protected abstract void loadData();
}
