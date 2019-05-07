package com.example.administrator.lc_dvr.common.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baoyz.swipemenulistview.BaseSwipListAdapter;

import java.util.List;

public abstract class DvrCommonAdapter<T> extends BaseSwipListAdapter {

    protected LayoutInflater mInflater;

    protected Context mContext;

    protected List<T> mDatas;

    protected final int mItemLayoutId;

    protected int countSum = -1;

    public boolean isLeftSlip = false;

    public DvrCommonAdapter(Context context, List<T> mDatas, int itemLayoutId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mDatas = mDatas;
        this.mItemLayoutId = itemLayoutId;
    }

    /**
     * 在这个回调方法设置那一项不能左滑
     *
     * @param position
     * @return
     */
    @Override
    public boolean getSwipEnableByPosition(int position) {
        //判断是否要禁止dvr列表的左滑
        if (isLeftSlip) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 替换元素并刷新
     *
     * @param mDatas
     */
    public void refresh(List<T> mDatas) {
        this.mDatas = mDatas;
        this.notifyDataSetChanged();
    }

    /**
     * 删除元素并更新
     *
     * @param position
     */
    public void deleteList(int position) {
        this.mDatas.remove(position);
        this.notifyDataSetChanged();
    }

    /**
     * 定义itemCount
     */
    public DvrCommonAdapter setCount(int i) {
        countSum = i;
        this.notifyDataSetChanged();
        return this;
    }

    @Override
    public int getCount() {
        if (countSum == -1) {
            return mDatas.size();
        } else {
            return countSum;
        }
    }

    @Override
    public T getItem(int position) {
        if (countSum == -1) {
            return mDatas.get(position);
        } else {
            return mDatas.get(countSum % mDatas.size());
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = getViewHolder(position, convertView,
                parent);
        convert(viewHolder, position, getItem(position));
        return viewHolder.getConvertView();
    }

    public abstract void convert(ViewHolder helper, int position, T item);

    public ViewHolder getViewHolder(int position, View convertView,
                                    ViewGroup parent) {
        return ViewHolder.get(mContext, convertView, parent, mItemLayoutId,
                position);
    }

}
