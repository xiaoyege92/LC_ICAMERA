package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.Config;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.bean.Comment;
import com.example.administrator.lc_dvr.common.adapter.CommonRecyclerAdapter;
import com.example.administrator.lc_dvr.common.adapter.RecyclerViewHolder;
import com.example.administrator.lc_dvr.common.customview.RatingBar;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.ListDataSave;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.utils.Log;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 评论页面
 */
public class CommentActivity extends BaseActivity implements View.OnClickListener {

    private RadioButton rb_back;

    private TextView tv_comment_time; // 第几次评价
    private RecyclerView recyclerView; // 添加评价照片
    private RatingBar quality_rating_bar; // 服务质量
    private RatingBar attitude_rating_bar; // 服务态度
    private EditText et_comment; // 评论
    private Button btn_comment_commit; // 提交评价

    private CommonRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;
    private ArrayList<String> commentPhotoArr;

    private String qiniuToken;
    private ArrayList<String> qiniuCommentArr; // 七牛云存贮名称

    private ListDataSave dataSave;
    // 图片是否可编辑
    private boolean isEditReport = true;
    private List<LocalMedia> selectList;

    private String caseid;  // 案件id
    private static Comment mComment = new Comment();

    private KProgressHUD kProgressHUD;
    public String fileName;
    private int unitkind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int setViewId() {
        return R.layout.activity_comment;
    }

    @Override
    protected void findView() {
        rb_back = (RadioButton) findViewById(R.id.rb_back);
        tv_comment_time = (TextView) findViewById(R.id.tv_comment_time);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        quality_rating_bar = (RatingBar) findViewById(R.id.quality_rating_bar);
        attitude_rating_bar = (RatingBar) findViewById(R.id.attitude_rating_bar);
        et_comment = (EditText) findViewById(R.id.et_comment);
        btn_comment_commit = (Button) findViewById(R.id.btn_comment_commit);
    }

    @Override
    protected void init() {
        //用来保存list到本地
        dataSave = new ListDataSave(this, "baiyu");
        // 初始化照片数据
        commentPhotoArr = new ArrayList<>();
        qiniuCommentArr = new ArrayList<>();
        // 案件id
        caseid = getIntent().getStringExtra("caseid");
        unitkind = getIntent().getIntExtra("unitkind", 1);

        PreferenceUtil.commitBoolean("isEditReport", false);

        recyclerAdapter = new CommonRecyclerAdapter(CommentActivity.this, R.layout.reportphoto1_item, commentPhotoArr) {
            @Override
            public void convert(RecyclerViewHolder helper, Object item, final int position) {
                //获得item中的控件
                ImageView report_image1 = helper.getView(R.id.report_image1);
                ImageView delete_image1 = helper.getView(R.id.delete_image1);

                //如果是最后一个cell，就把deleteImage隐藏
                if (position == commentPhotoArr.size() - 1 && (commentPhotoArr.size() != 5 || (commentPhotoArr.size() == 5 && commentPhotoArr.get(4).equals("1")))) {
                    delete_image1.setVisibility(View.GONE);
                    if (isEditReport) {
                        helper.setImageResource(R.id.report_image1, R.mipmap.report_image);
                    } else {
                        if (commentPhotoArr.get(position).contains("http")) {
                            helper.setImageByUrl(R.id.report_image1, commentPhotoArr.get(position), CommentActivity.this);
                        } else {
                            ImageLoader.getInstance().displayImage("file://" + commentPhotoArr.get(position), (ImageView) helper.getView(R.id.report_image1));
                        }
                    }

                } else {
                    delete_image1.setVisibility(View.VISIBLE);
                    if (commentPhotoArr.get(position).contains("http")) {
                        helper.setImageByUrl(R.id.report_image1, commentPhotoArr.get(position), CommentActivity.this);
                    } else {
                        ImageLoader.getInstance().displayImage("file://" + commentPhotoArr.get(position), (ImageView) helper.getView(R.id.report_image1));
                    }

                }
                //点击列表1中reportImage时的响应方法
                report_image1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        PreferenceUtil.commitInt("whichList", -3);
                        if (position == commentPhotoArr.size() - 1 && (commentPhotoArr.size() != 5 || (commentPhotoArr.size() == 5 && commentPhotoArr.get(4).equals("1")))) {
                            //判断是否要禁止编辑
                            if (!isEditReport) {
                                //保存当前的commentPhotoArr
                                dataSave.setDataList("LookImageList", commentPhotoArr);
                                //从当前的界面跳转到Identifier为"look_image_list"的界面
                                Intent intent = new Intent(CommentActivity.this, LookImageList.class);
                                intent.putExtra("current", position);
                                startActivity(intent);
                                PreferenceUtil.commitInt("numOfPages", commentPhotoArr.size() - 1);
                            } else {

                                //弹出图片选择页面
                                PictureSelector.create(CommentActivity.this)
                                        .openGallery(PictureMimeType.ofImage())
                                        .selectionMode(PictureConfig.SINGLE)//设置为单选
                                        .compress(true)// 是否压缩 true or false
                                        .forResult(PictureConfig.CHOOSE_REQUEST);
                            }
                        } else {
                            //保存当前的commentPhotoArr
                            dataSave.setDataList("LookImageList", commentPhotoArr);
                            //从当前的界面跳转到Identifier为"look_image_list"的界面
                            Intent intent = new Intent(CommentActivity.this, LookImageList.class);
                            intent.putExtra("current", position);
                            startActivity(intent);
                            PreferenceUtil.commitInt("numOfPages", commentPhotoArr.size() - 1);

                        }
                    }
                });

                //点击列表1中deleteImage时的响应方法
                delete_image1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //删除缓存图片
                        File hitImageFile = new File(commentPhotoArr.get(position));
                        if (hitImageFile.exists()) {
                            hitImageFile.delete();
                        }

                        if ((commentPhotoArr.size() == 5 && !commentPhotoArr.get(4).equals("1"))) {
                            //删除对应项
                            commentPhotoArr.remove(position);
                            commentPhotoArr.add("1");
                        } else {
                            //删除对应项
                            commentPhotoArr.remove(position);
                        }
                        //更新数据
                        recyclerAdapter.notifyDataSetChanged();
                    }
                });

                //判断是否要禁止编辑
                if (!isEditReport) {
                    delete_image1.setVisibility(View.GONE);
                }
            }
        };
        layoutManager = new LinearLayoutManager(CommentActivity.this, LinearLayoutManager.HORIZONTAL, false);
        //设置RecyclerView管理器
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    protected void initEvents() {
        rb_back.setOnClickListener(this);
        btn_comment_commit.setOnClickListener(this);
    }

    @Override
    protected void loadData() {


        Comment comment = getIntent().getParcelableExtra("comment");
        if (null == comment) {
            comment = new Comment();
            getCaseCommentList(); // 查看评论列表
        }else {
            et_comment.setText(Utils.parseStr(comment.getMsg()));
            quality_rating_bar.setSelectedNumber(comment.getStar1());
            attitude_rating_bar.setSelectedNumber(comment.getStar2());

            if (null != comment.getPic() && !"".equals(comment.getPic())) {
                String[] pics = comment.getPic().split(",");
                for (int i = 0; i < pics.length; i++) {
                    commentPhotoArr.add(Config.QINIU_BASE_URL + pics[i]);
                }
                commentPhotoArr.add("1");
            } else {
                commentPhotoArr.add("1");
            }

            mComment.setId(comment.getId());
            // 显示第几次评价
            if (null == comment.getN() || "".equals(comment.getN())
                    || "null".equals(comment.getN()) || "1".equals(comment.getN())) {
                tv_comment_time.setText("第1次评价");
                mComment.setN("2");
                if (1 == unitkind) {
                    btn_comment_commit.setText("确认收车并评价");
                } else {
                    btn_comment_commit.setText("立即评价");
                }
            } else if ("2".equals(comment.getN())) {
                tv_comment_time.setText("第2次评价");
                mComment.setN("3");
            } else if ("3".equals(comment.getN())) {
                et_comment.setEnabled(false);
                quality_rating_bar.setClickable(false);
                attitude_rating_bar.setClickable(false);
                tv_comment_time.setText("已完成2次评价");
                btn_comment_commit.setVisibility(View.GONE);
                isEditReport = false;
                commentPhotoArr.remove(commentPhotoArr.size() - 1);
            }
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_back:
                finish();
                break;
            case R.id.btn_comment_commit:
                if (quality_rating_bar.getSlelectedNumber() <= 0) {
                    ToastUtils.showNomalShortToast(CommentActivity.this, "请您先对服务质量进行评分！");
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                    }
                    return;
                }
                if (attitude_rating_bar.getSlelectedNumber() <= 0) {
                    ToastUtils.showNomalShortToast(CommentActivity.this, "请您先对服务态度进行评分！");
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                    }
                    return;
                }
                if (!NetUtils.isNetworkConnected(CommentActivity.this)) {
                    ToastUtils.showNomalShortToast(CommentActivity.this, getString(R.string.network_off));
                    return;
                }
                showProgress("拼命上传中...");
                getQiniuToken(); // 提交评论
                break;
        }
    }

    private void getCaseCommentList(){
        Call<ResponseBody> call = RetrofitManager.getInstance().create().getCaseCommentList(NetUtils.getHeaders(),caseid);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    Gson gson =new Gson();
                    Comment comment = null;
                    if(jsonObject.getJSONObject("datas").getJSONArray("data").length() == 0) {
                        comment = new Comment();
                    }else {
                        String str = jsonObject.getJSONObject("datas").getJSONArray("data").getJSONObject(0).toString();
                        comment = (Comment)gson.fromJson(str,Comment.class);
                    }
                    if(comment == null) {
                        comment = new Comment();
                    }

                    et_comment.setText(Utils.parseStr(comment.getMsg()));
                    quality_rating_bar.setSelectedNumber(comment.getStar1());
                    attitude_rating_bar.setSelectedNumber(comment.getStar2());

                    if (null != comment.getPic() && !"".equals(comment.getPic())) {
                        String[] pics = comment.getPic().split(",");
                        for (int i = 0; i < pics.length; i++) {
                            commentPhotoArr.add(Config.QINIU_BASE_URL + pics[i]);
                        }
                        commentPhotoArr.add("1");
                    } else {
                        commentPhotoArr.add("1");
                    }

                    mComment.setId(comment.getId());
                    // 显示第几次评价
                    if (null == comment.getN() || "".equals(comment.getN())
                            || "null".equals(comment.getN()) || "1".equals(comment.getN())) {
                        tv_comment_time.setText("第1次评价");
                        mComment.setN("2");
                        if (1 == unitkind) {
                            btn_comment_commit.setText("确认收车并评价");
                        } else {
                            btn_comment_commit.setText("立即评价");
                        }
                    } else if ("2".equals(comment.getN())) {
                        tv_comment_time.setText("第2次评价");
                        mComment.setN("3");
                    } else if ("3".equals(comment.getN())) {
                        et_comment.setEnabled(false);
                        quality_rating_bar.setClickable(false);
                        attitude_rating_bar.setClickable(false);
                        tv_comment_time.setText("已完成2次评价");
                        btn_comment_commit.setVisibility(View.GONE);
                        isEditReport = false;
                        commentPhotoArr.remove(commentPhotoArr.size() - 1);
                    }
                    recyclerAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    /**
     * 获取七牛token
     */
    private void getQiniuToken() {

        int count = 0;
        if (commentPhotoArr.size() == 5 && !commentPhotoArr.get(4).equals("1")) {
            count = commentPhotoArr.size();
        } else {
            count = commentPhotoArr.size() - 1;
        }
        Call<ResponseBody> call = RetrofitManager.getInstance().create().getQiniuToken(NetUtils.getHeaders(), count);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (200 == jsonObject.getInt("code")) {
                        qiniuToken = jsonObject.getJSONObject("data").getString("token");
                        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("fileName");

                        List<String> list = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            list.add(jsonArray.get(i).toString());
                        }
                        upLoadQiniuComment(list);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    /**
     * 上传照片
     *
     * @param list
     */
    private void upLoadQiniuComment(final List<String> list) {
        qiniuCommentArr.clear();
        if (0 == list.size()) {
            commitComment();
        } else {
            for (int i = 0; i < list.size(); i++) {
                File reportPhotoArr1File = new File(commentPhotoArr.get(i));
                if (reportPhotoArr1File.exists()) {
                    VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key, ResponseInfo info, JSONObject res) {
                                    //res包含hash、key等信息，具体字段取决于上传策略的设置
                                    if (info.isOK()) {
                                        try {
                                            String attachid = res.getString("key");
                                            // 获取图片的url
                                            String url = Config.QINIU_BASE_URL + attachid;
                                            qiniuCommentArr.add(attachid);

                                            if (qiniuCommentArr.size() == list.size()) {
                                                commitComment();
                                            }
                                        } catch (JSONException e) {
                                        }
                                    } else {
                                    }
                                    Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                                }
                            }, null);
                } else {
                    qiniuCommentArr.add(commentPhotoArr.get(i).replace(Config.QINIU_BASE_URL, ""));

                    if (qiniuCommentArr.size() == list.size()) {
                        commitComment();
                    }
                }
            }
        }
    }

    /**
     * 提交评论
     */
    private void commitComment() {

        mComment.setCaseid(PreferenceUtil.getString("caseid", ""));
        mComment.setUsertype(2);
        mComment.setUserid(PreferenceUtil.getString("personcode", ""));
        mComment.setCtime(null);
        mComment.setMsg(et_comment.getText().toString());
        mComment.setStar1((int) quality_rating_bar.getSlelectedNumber());
        mComment.setStar2((int) attitude_rating_bar.getSlelectedNumber());
        mComment.setN(mComment.getN());

        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < qiniuCommentArr.size(); i++) {
            stringBuffer.append(qiniuCommentArr.get(i));
            if (i != qiniuCommentArr.size() - 1) {
                stringBuffer.append(",");
            }
        }
        mComment.setPic(String.valueOf(stringBuffer));

        Call<ResponseBody> call = RetrofitManager.getInstance().create().saveComment(NetUtils.getHeaders(), mComment);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.body().string());

                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        // 回调关闭快赔索引页
                        Utils.doCallBackMethod();
                        Intent intent = new Intent(CommentActivity.this, CommentSuccessActivity.class);
                        intent.putExtra("CommentTime", mComment.getN());
                        startActivity(intent);
                        finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(kProgressHUD != null) {
                    kProgressHUD.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if(kProgressHUD != null) {
                    kProgressHUD.dismiss();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:

                    // 图片选择结果回调
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                    for (int i = 0; i < selectList.size(); i++) {
                        //如何图片压缩成功了就取得压缩后的图片地址
                        if (selectList.get(i).isCompressed()) {
                            String picturePath = selectList.get(i).getCompressPath();
                            //缓存图片
                            try {
                                fileName = (int) (Math.random() * 900) + 100 + "";
                                saveMyBitmap2(BitmapFactory.decodeFile(picturePath), fileName);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    break;
            }
        }
        //包括裁剪和压缩后的缓存，要在上传成功后调用，注意：需要系统sd卡权限
        PictureFileUtils.deleteCacheDirFile(CommentActivity.this);
    }

    /**
     * 保存bitmap到SD卡
     *
     * @param bmp
     * @param bitName
     * @return
     * @throws IOException
     */
    public void saveMyBitmap2(final Bitmap bmp, final String bitName) throws IOException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (bmp != null) {
                    File dirFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/");
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }
                    File f = new File(BitmapUtils.getSDPath() + "/VOC/Cache/" + bitName + ".png");
                    if (f.exists()) {
                        f.delete();
                    }
                    try {
                        f.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    FileOutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(f);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        fOut.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Message msg = new Message();
                    msg.obj = bitName;
                    refreshHandler.sendMessage(msg);

                }
            }
        });
        thread.start();
    }

    //更新图片的handler
    Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            commentPhotoArr.remove(commentPhotoArr.size() - 1);
            commentPhotoArr.add(BitmapUtils.getSDPath() + "/VOC/Cache/" + fileName + ".png");
            if (commentPhotoArr.size() == 5) {

            } else {
                commentPhotoArr.add("1");
            }
            recyclerAdapter.notifyDataSetChanged();
        }
    };

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label) {
        kProgressHUD = KProgressHUD.create(this);
        kProgressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        kProgressHUD.setLabel(label);
        kProgressHUD.setCancellable(true);
        kProgressHUD.setAnimationSpeed(2);
        kProgressHUD.setDimAmount(0.5f);
        kProgressHUD.show();
    }
}
