package com.example.administrator.lc_dvr.common.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;

import java.util.List;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/10/21
 *   desc   :
 *  version :
 * </pre>
 */
public class RecyclerViewAdapter1 extends RecyclerView.Adapter<RecyclerViewAdapter1.ViewHolder>{

    private List<String> list;
    private Context mContext;
    private int layoutId;

    public RecyclerViewAdapter1(Context context, List<String> list,int layoutId) {
        this.mContext = context;
        this.list = list;
        this.layoutId = layoutId;
    }

    @Override
    public RecyclerViewAdapter1.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        RecyclerViewAdapter1.ViewHolder viewHolder = new RecyclerViewAdapter1.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter1.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView report_image1;
        ImageView delete_image1;
        ImageView switch_image1;
        ImageView dotted_line1;

        public ViewHolder(View itemView) {
            super(itemView);
            report_image1 = itemView.findViewById(R.id.report_image1);
            delete_image1 = itemView.findViewById(R.id.delete_image1);
            dotted_line1 =  itemView.findViewById(R.id.dotted_line1);
        }
    }
}
