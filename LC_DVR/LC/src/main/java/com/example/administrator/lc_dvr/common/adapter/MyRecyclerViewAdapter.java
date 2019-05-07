package com.example.administrator.lc_dvr.common.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.bean.CaseIdea;
import java.util.ArrayList;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/08/06
 *   desc   :
 *  version :
 * </pre>
 */
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>{

    private ArrayList<CaseIdea> listCaseIdea;

    public  MyRecyclerViewAdapter(ArrayList<CaseIdea> list) {
        this.listCaseIdea = list;
    }

    @Override
    public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.caseidea_item, parent, false);
        MyRecyclerViewAdapter.ViewHolder viewHolder = new MyRecyclerViewAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.rejectDate.setText(listCaseIdea.get(position).getCreatetime());
        holder.rejectUser.setText(listCaseIdea.get(position).getUsername());
        holder.rejectOpinion.setText(listCaseIdea.get(position).getIdea());
    }

    @Override
    public int getItemCount() {
        return listCaseIdea.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView rejectOpinion;
        TextView rejectUser;
        TextView rejectDate;

        public ViewHolder(View itemView) {
            super(itemView);
            rejectOpinion = itemView.findViewById(R.id.rejectOpinion);
            rejectUser = itemView.findViewById(R.id.rejectUser);
            rejectDate = itemView.findViewById(R.id.rejectDate);
        }
    }

}
