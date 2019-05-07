package com.example.administrator.lc_dvr.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * base包都是拿来存放一些基类的
 * Created by Administrator on 2016/4/5.
 */
public abstract class BaseFragment extends android.support.v4.app.Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(setViewId(), container, false);
        findView(view);
        init();
        initEvents();
        loadData();
        return view;
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
    protected abstract void findView(View view);

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
