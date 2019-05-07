package com.example.administrator.lc_dvr.common.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import com.example.administrator.lc_dvr.common.adapter.RecyclerViewHolder;
/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/10/21
 *   desc   :
 *  version :
 * </pre>
 */
public abstract class CommonRecyclerAdapter extends RecyclerView.Adapter {
    protected Context mContext;
    protected int mLayoutId;
    protected List mDatas;
    protected LayoutInflater mInflater;

    public CommonRecyclerAdapter(Context context, int layoutId, List datas)
    {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLayoutId = layoutId;
        mDatas = datas;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(final ViewGroup parent, int viewType)
    {
        RecyclerViewHolder viewHolder = RecyclerViewHolder.get(mContext, parent, mLayoutId);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        convert((RecyclerViewHolder) holder, mDatas.get(position),position);
    }

    public abstract void convert(RecyclerViewHolder holder, Object t, final int position);

    @Override
    public int getItemCount()
    {
        return mDatas.size();
    }


}
