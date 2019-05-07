package com.example.administrator.lc_dvr.module.lc_help;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.Config;
import com.bumptech.glide.Glide;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.bean.UnitMessage;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.module.lc_dvr.Advertisement;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/11/08
 *   desc   :
 *  version :
 * </pre>
 */
public class UnitMessageDetail extends BaseActivity implements View.OnClickListener {

    private RadioButton rb_back; // 返回
    private TextView tv_message_title; // 消息标题
    private TextView tv_message_date; // 消息日期
    private ImageView iv_message_picture; // 消息图片
    private TextView tv_message_content; // 消息内容

    private UnitMessage unitMessage;// 传递过来的消息对象

    @Override
    protected void onResume() {
        super.onResume();
        // 如果消息状态为0 未读，则改成已读
        if (0 == unitMessage.getStatus()) {
            Call<ResponseBody> call = RetrofitManager.getInstance().create().readUserMSG(NetUtils.getHeaders(), unitMessage.getId());

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }
    }

    @Override
    protected int setViewId() {
        return R.layout.unit_message_detail_activity;
    }

    @Override
    protected void findView() {
        rb_back = (RadioButton) findViewById(R.id.rb_back);
        tv_message_title = (TextView) findViewById(R.id.tv_message_title);
        tv_message_date = (TextView) findViewById(R.id.tv_message_date);
        iv_message_picture = (ImageView) findViewById(R.id.iv_message_picture);
        tv_message_content = (TextView) findViewById(R.id.tv_message_content);
    }

    @Override
    protected void init() {
        Intent intent = getIntent();
        unitMessage = intent.getParcelableExtra("MessageDetail");

    }

    @Override
    protected void initEvents() {
        rb_back.setOnClickListener(this);
        iv_message_picture.setOnClickListener(this);
    }

    @Override
    protected void loadData() {
        tv_message_title.setText(unitMessage.getTitle());
        tv_message_date.setText(unitMessage.getMsgtime());
        if (null != unitMessage.getMsgpic() && !"".equals(unitMessage.getMsgpic())) {
            Glide.with(this).load(Config.QINIU_BASE_URL + unitMessage.getMsgpic()).into(iv_message_picture);
        } else {
            iv_message_picture.setVisibility(View.GONE);
        }
        tv_message_content.setText(unitMessage.getContent());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_back:
                finish();
                break;
            case R.id.iv_message_picture: // 点击图片打开外链
                if (null != unitMessage.getMsgurl() && !"".equals(unitMessage.getMsgurl())) {
                    Intent intent = new Intent(UnitMessageDetail.this, Advertisement.class);
                    intent.putExtra("adsTitle", unitMessage.getTitle());
                    intent.putExtra("adsLink", unitMessage.getMsgurl());
                    startActivity(intent);
                }
                break;
        }
    }

}
