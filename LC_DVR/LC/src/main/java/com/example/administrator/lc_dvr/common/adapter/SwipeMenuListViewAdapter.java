package com.example.administrator.lc_dvr.common.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2019/03/21
 *   desc   :
 *  version :
 * </pre>
 */
public abstract class SwipeMenuListViewAdapter<T> extends BaseAdapter {
    protected LayoutInflater mInflater;

    protected Context mContext;

    protected List<T> mDatas;

    protected final int mItemLayoutId;

    protected int countSum = -1;

    public static final int viewtype_normaldata = 0, viewtype_erpdata = 1;

    public SwipeMenuListViewAdapter(Context context, List<T> mDatas, int itemLayoutId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mDatas = mDatas;
        this.mItemLayoutId = itemLayoutId;
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
    public int getViewTypeCount() {
        // menu type count
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        // current menu type
        if (position == 0) return viewtype_normaldata;
        else return viewtype_erpdata;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder = getViewHolder(position, convertView,
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
