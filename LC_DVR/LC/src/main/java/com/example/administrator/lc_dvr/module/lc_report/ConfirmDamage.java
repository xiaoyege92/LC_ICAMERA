package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.android.volley.VolleyError;
import com.android.volley.listener.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.common.adapter.CommonAdapter;
import com.example.administrator.lc_dvr.common.adapter.ViewHolder;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.HorizontalListView;
import com.example.administrator.lc_dvr.common.utils.ListDataSave;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import io.vov.vitamio.utils.Log;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by yangboru on 2018/1/24.
 * <p>
 * 定损
 */

public class ConfirmDamage extends BaseFragment {

    private final int HANDLER_MESSAGE_UPLOAD_SUCCESS = 1;
    private final int HANDLER_MESSAGE_REWOKE_SUCCESS = 2;
    private final int HANDLER_MESSAGE_UPLOAD_FAIL = 3;
    private final int HANDLER_MESSAGE_UPLOAD_NET_FAIL = 4;

    private final int MAX_UPLOAD_PAYMENT = 8;
    private final int MAX_UPLOAD_REPORT = 11;
    private int currentProgress;

    private TextView finishrepairdate;
    private TextView prerepairdate;
    private TextView finishsum;
    private TextView finishdate;
    private TextView losssum;
    private TextView lossdate;
    //    private Button confirmDamageRightBtn;
    private Button confirmDamageLeftBtn;

    private String idOppositeUrl;
    private String idPositiveUrl;
    private String bankUrl;
    private String drivingLicenseUrl;
    private String frameNumberUrl;
    private String driverLicenseUrl;
    private KProgressHUD kProgressHUD;
    private int trueFinish;
    private List<String> repairpicturesArr;
    private HorizontalListView repairpicturesList;
    private CommonAdapter adapter;
    private ListDataSave dataSave;
    private String examineState;
    private NormalDialog dialog;
    private String[] mStringBts;
    private String baseUrl;
    private String qiniuToken;
    private ArrayList<String> arrPayment;
    private LocalBroadcastManager localBroadcastManager;

    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_MESSAGE_UPLOAD_SUCCESS:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    confirmDamageLeftBtn.setEnabled(true);
                    break;
                case HANDLER_MESSAGE_UPLOAD_FAIL:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    confirmDamageLeftBtn.setEnabled(true);
                    if (VLCApplication.configsDictionary.get("app-c-171") != null) {
                        Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-c-171"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "未上传成功，请稍后再试", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case HANDLER_MESSAGE_UPLOAD_NET_FAIL:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    confirmDamageLeftBtn.setEnabled(true);
                    if (VLCApplication.configsDictionary.get("app-z-010") != null) {
                        Toast.makeText(getActivity(), VLCApplication.configsDictionary.get("app-z-010"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "连接失败，请检查您的网络连接", Toast.LENGTH_SHORT).show();
                    }
                    android.util.Log.e("ConfirmDamage-134","连接失败，请检查您的网络连接");
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        //获得列表中某项的审核状态
        examineState = PreferenceUtil.getString("examineState", "");

        if (examineState.equals("事故照片已通过审核")) {// 审核成功
            confirmDamageLeftBtn.setText("理赔上传");
//            confirmDamageRightBtn.setText("微信分享");
        } else {
            confirmDamageLeftBtn.setVisibility(View.GONE);
//            confirmDamageRightBtn.setText("微信分享");
        }
    }

    @Override
    protected int setViewId() {
        return R.layout.confirmdamage_layout;
    }

    @Override
    protected void findView(View view) {
        //维修完毕日期
        finishrepairdate = (TextView) view.findViewById(R.id.finishrepairdate);
        //预计交车日期
        prerepairdate = (TextView) view.findViewById(R.id.prerepairdate);
        //结案金额
        finishsum = (TextView) view.findViewById(R.id.finishsum);
        //结案日期
        finishdate = (TextView) view.findViewById(R.id.finishdate);
        //定损金额
        losssum = (TextView) view.findViewById(R.id.losssum);
        //定损日期时间
        lossdate = (TextView) view.findViewById(R.id.lossdate);
        //最底下右边按钮
//        confirmDamageRightBtn = (Button) view.findViewById(R.id.confirmDamageRightBtn);
        //最底下左边按钮
        confirmDamageLeftBtn = (Button) view.findViewById(R.id.confirmDamageLeftBtn);
        //维修照片列表
        repairpicturesList = (HorizontalListView) view.findViewById(R.id.repairpicturesList);
    }

    @Override
    protected void init() {

        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        arrPayment = new ArrayList<>();

        dialog = new NormalDialog(getActivity());
        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);

        //用来保存list到本地
        dataSave = new ListDataSave(getContext(), "baiyu");

        //维修照片数组
        repairpicturesArr = new ArrayList<>();

        //用来解决上传时发生的错误，就是那个完成接口会调用两次
        trueFinish = 0;

        //列表的适配器
        adapter = new CommonAdapter(this.getActivity(), repairpicturesArr, R.layout.repairpictures_item) {
            @Override
            public void convert(ViewHolder helper, final int position, Object item) {

                //获得item中的控件
                ImageView repair_image = helper.getView(R.id.repair_report_image);

                helper.setImageByUrl(R.id.repair_report_image, repairpicturesArr.get(position), getContext());

                //点击repair_image的响应方法
                repair_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //判断是那个list
                        PreferenceUtil.commitInt("whichList", -1);
                        //判断是否要隐藏删除按钮
                        PreferenceUtil.commitBoolean("isEditReport", false);
                        //保存当前的reportPhotoArr1
                        dataSave.setDataList("LookImageList", repairpicturesArr);
                        //从当前的界面跳转到Identifier为"look_image_list"的界面
                        Intent intent = new Intent(getContext(), LookImageList.class);
                        startActivity(intent);
                        PreferenceUtil.commitInt("numOfPages", repairpicturesArr.size() - 1);
                    }
                });

            }
        };
        repairpicturesList.setAdapter(adapter);

        //获得事故ID的具体内容
        getCasedetail();

    }

    /**
     * 获得事故ID的具体内容
     */
    private void getCasedetail() {
        Map<String, String> map = new HashMap<>();

        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));

        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.CASE_DETAIL_URL, jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {

                        //维修完毕日期
                        if (jsonObject.getJSONObject("datas").getJSONObject("case").getString("finishrepairdate") != null) {
                            finishrepairdate.setText(jsonObject.getJSONObject("datas").getJSONObject("case").getString("finishrepairdate"));
                        } else {
                            finishrepairdate.setText("");
                        }

                        //预计交车日期
                        if (jsonObject.getJSONObject("datas").getJSONObject("case").getString("prerepairdate") != null) {
                            prerepairdate.setText(jsonObject.getJSONObject("datas").getJSONObject("case").getString("prerepairdate"));
                        } else {
                            prerepairdate.setText("");
                        }

                        //结案金额
                        if (jsonObject.getJSONObject("datas").getJSONObject("case").getString("finishsum") != null) {
                            finishsum.setText(jsonObject.getJSONObject("datas").getJSONObject("case").getString("finishsum"));
                        } else {
                            finishsum.setText("");
                        }

                        //结案日期
                        if (jsonObject.getJSONObject("datas").getJSONObject("case").getString("finishdate") != null) {
                            finishdate.setText(jsonObject.getJSONObject("datas").getJSONObject("case").getString("finishdate"));
                        } else {
                            finishdate.setText("");
                        }

                        //定损金额
                        if (jsonObject.getJSONObject("datas").getJSONObject("case").getString("losssum") != null) {
                            losssum.setText(jsonObject.getJSONObject("datas").getJSONObject("case").getString("losssum"));
                        } else {
                            losssum.setText("");
                        }

                        //定损日期时间
                        if (jsonObject.getJSONObject("datas").getJSONObject("case").getString("lossdate") != null) {
                            lossdate.setText(jsonObject.getJSONObject("datas").getJSONObject("case").getString("lossdate"));
                        } else {
                            lossdate.setText("");
                        }


                        JSONObject datas = jsonObject.getJSONObject("datas");
                        //维修照片
                        JSONArray repairpictures = datas.getJSONArray("repairpictures");
                        for (int i = 0; i < repairpictures.length(); i++) {
                            JSONObject value = (JSONObject) repairpictures.get(i);
                            String piclink = value.getString("attachid");
                            repairpicturesArr.add(Config.QINIU_BASE_URL + piclink);
                        }
                        //刷新数据
                        adapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(getContext(), "获取数据失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "获取数据失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                if (VLCApplication.configsDictionary.get("app-z-010") != null) {
                    Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-z-010"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "连接失败，请检查您的网络连接", Toast.LENGTH_SHORT).show();
                }
                android.util.Log.e("ConfirmDamage-330","连接失败，请检查您的网络连接");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");

                //上传授权码
                headers.put("tokenid", PreferenceUtil.getString("tokenid", null));

                return headers;
            }
        };
        VLCApplication.queue.add(jsonRequest);
    }

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label,int maxProgress) {
//        kProgressHUD = KProgressHUD.create(getContext());
//        kProgressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
//        kProgressHUD.setLabel(label);
//        kProgressHUD.setCancellable(true);
//        kProgressHUD.setAnimationSpeed(2);
//        kProgressHUD.setDimAmount(0.5f);
//        kProgressHUD.show();

        kProgressHUD = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.ANNULAR_DETERMINATE)
                .setLabel(label)
                .setMaxProgress(maxProgress)
                .show();
    }

    @Override
    protected void initEvents() {
        //点击最底下左边按钮时的响应
        confirmDamageLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetUtils.isNetworkConnected(getActivity())) {//是否联网
                    ToastUtils.showNomalShortToast(getActivity(), getString(R.string.network_off));
                    android.util.Log.e("ConfirmDamage-371","连接失败，请检查您的网络连接");
                    return;
                }
                //获得保存在本地的赔付图片url
                idOppositeUrl = PreferenceUtil.getString("IDOpposite", null);
                idPositiveUrl = PreferenceUtil.getString("IDPositive", null);
                bankUrl = PreferenceUtil.getString("bank", null);
                drivingLicenseUrl = PreferenceUtil.getString("drivingLicense", null);
                frameNumberUrl = PreferenceUtil.getString("frameNumber", null);
                driverLicenseUrl = PreferenceUtil.getString("driverLicense", null);

                showTipDialog("请再次确认您的理赔信息是否正确无误？");
                dialog.setOnBtnClickL(
                        new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
//                                oneKeyPayment();//上传里理赔资料
                                showProgress("拼命上传中",MAX_UPLOAD_PAYMENT);
                                getQiniuToken();
                            }
                        },
                        new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                            }
                        });

            }
        });
//        //点击最底下右边按钮时的响应
//        confirmDamageRightBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!NetUtils.isNetworkConnected(getActivity())) {//是否联网
//                    ToastUtils.showNomalShortToast(getActivity(), getString(R.string.network_off));
//                    return;
//                }
//                weChatSharing();
//            }
//        });

    }

    /**
     * 上传赔付资料
     */
    private void oneKeyPayment() {
        //获得保存在本地的赔付图片url
        idOppositeUrl = PreferenceUtil.getString("IDOpposite", null);
        idPositiveUrl = PreferenceUtil.getString("IDPositive", null);
        bankUrl = PreferenceUtil.getString("bank", null);
        drivingLicenseUrl = PreferenceUtil.getString("drivingLicense", null);
        frameNumberUrl = PreferenceUtil.getString("frameNumber", null);
        driverLicenseUrl = PreferenceUtil.getString("driverLicense", null);

//        if (!isFieldFull()) {
//            return;
//        }

        String url = Config.ATTACH_UPLOAD_URL;

        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.addHeader("tokenid", PreferenceUtil.getString("tokenid", null));
        builder.url(url);

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();

        //设置报案按钮不能点击
        confirmDamageLeftBtn.setEnabled(false);

        //弹出一个进度框
//        if (VLCApplication.configsDictionary.get("app-z-005") != null) {
//            showProgress(VLCApplication.configsDictionary.get("app-z-005"));
//        } else {
//            showProgress("正在拼命上传中");
//        }

        if (idOppositeUrl != null) {
            //上传身份证反面照片
            File IDOppositeImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDOpposite.png");
            bodyBuilder.addFormDataPart("license_b", "license_b.png", RequestBody.create(MediaType.parse("image/png"), IDOppositeImageFile));
        }

        if (idPositiveUrl != null) {
            //上传身份证正面照片
            File IDPositiveImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png");
            bodyBuilder.addFormDataPart("license_a", "license_a.png", RequestBody.create(MediaType.parse("image/png"), IDPositiveImageFile));
        }

        if (bankUrl != null) {
            //上传银行卡照片
            File bankImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/bank.png");
            bodyBuilder.addFormDataPart("card", "card.png", RequestBody.create(MediaType.parse("image/png"), bankImageFile));
        }

        if (drivingLicenseUrl != null) {
            //上传行驶证照片
            File drivingLicenseImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png");
            bodyBuilder.addFormDataPart("Permit", "Permit.png", RequestBody.create(MediaType.parse("image/png"), drivingLicenseImageFile));
        }

        if (driverLicenseUrl != null) {
            //上传驾驶证照片
            File driverLicenseImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png");
            bodyBuilder.addFormDataPart("drivelicense", "drivelicense.png", RequestBody.create(MediaType.parse("image/png"), driverLicenseImageFile));
        }

        if (frameNumberUrl != null) {
            //上传车架号照片
            File frameNumberImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png");
            bodyBuilder.addFormDataPart("frame", "frame.png", RequestBody.create(MediaType.parse("image/png"), frameNumberImageFile));
        }

        MultipartBody build = bodyBuilder.build();

        //注意onUIProgressFinish会执行两次，会造成一些错误，所以用trueFinish这个变量来解决
        RequestBody requestBody = ProgressHelper.withProgress(build, new ProgressUIListener() {

            @Override
            public void onUIProgressStart(long totalBytes) {
            }

            @Override
            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                if (trueFinish == 1) {
                    //设置进度
                    kProgressHUD.setProgress((int) (((double) (numBytes) / (double) (totalBytes)) * 100));
                }
            }

            @Override
            public void onUIProgressFinish() {
                //onUIProgressFinish会执行两次，第一次是错误的，第二次才是真正的完成
                if (trueFinish == 1) {
                    //设置为初始值
                    trueFinish = 0;
                }
                trueFinish += 1;
            }

        });
        builder.post(requestBody);

        Call call = okHttpClient.newCall(builder.build());

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //关闭进度框
                kProgressHUD.dismiss();
                //设置报案按钮能点击
                confirmDamageLeftBtn.setEnabled(true);
                if (VLCApplication.configsDictionary.get("app-c-196") != null) {
                    Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-c-196"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "未上传成功，请稍后再试", Toast.LENGTH_SHORT).show();
                }
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //注意，这里有一个巨坑，要获得返回的json数据，一定要写成response.body().string()，不然返回的不是json数据
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONObject datas = jsonObject.getJSONObject("datas");
                    JSONArray attachs = datas.getJSONArray("attachs");

                    for (int i = 0; i < attachs.length(); i++) {
                        JSONObject value = (JSONObject) attachs.get(i);
                        String filename = value.getString("filename");
                        String attachid = value.getString("attachid");

                        if (filename.contains("license_b")) {
                            Map<String, String> license_bMap = new HashMap<>();
                            license_bMap.put("attachid", attachid);
                            VLCApplication.license_b.add(license_bMap);
                        } else if (filename.contains("frame")) {
                            Map<String, String> frameMap = new HashMap<>();
                            frameMap.put("attachid", attachid);
                            VLCApplication.frame.add(frameMap);
                        } else if (filename.contains("license_a")) {
                            Map<String, String> license_aMap = new HashMap<>();
                            license_aMap.put("attachid", attachid);
                            VLCApplication.license_a.add(license_aMap);
                        } else if (filename.contains("Permit")) {
                            Map<String, String> permitMap = new HashMap<>();
                            permitMap.put("attachid", attachid);
                            VLCApplication.permit.add(permitMap);
                        } else if (filename.contains("card")) {
                            Map<String, String> cardMap = new HashMap<>();
                            cardMap.put("attachid", attachid);
                            VLCApplication.card.add(cardMap);
                        } else if (filename.contains("drivelicense")) {
                            Map<String, String> drivelicenseMap = new HashMap<>();
                            drivelicenseMap.put("attachid", attachid);
                            VLCApplication.drivelicense.add(drivelicenseMap);
                        }
                    }

                    //给casepictures集合赋值
                    VLCApplication.compensatepictures.put("license_b", VLCApplication.license_b);
                    VLCApplication.compensatepictures.put("frame", VLCApplication.frame);
                    VLCApplication.compensatepictures.put("license_a", VLCApplication.license_a);
                    VLCApplication.compensatepictures.put("permit", VLCApplication.permit);
                    VLCApplication.compensatepictures.put("card", VLCApplication.card);
                    VLCApplication.compensatepictures.put("drivelicense", VLCApplication.drivelicense);

                    //上传步骤2
                    oneKeyPayment2();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * 上传步骤2
     */
    private void oneKeyPayment2() {
        Map<String, Object> map = new HashMap<>();

        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));
        //设置照片数组
        Gson gson = new Gson();
        //把Map转为json字符串
        String jsonStr = gson.toJson(VLCApplication.compensatepictures);
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            map.put("compensatepictures", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.CASE_STEP_THREE_URL, jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {

                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);
                        currentProgress = 0;

                        Message message = new Message();
                        message.what = HANDLER_MESSAGE_UPLOAD_SUCCESS;
                        handler.sendMessage(message);
                        if (VLCApplication.configsDictionary.get("app-c-170") != null) {
                            Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-c-170"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "以上信息已经为您成功上传", Toast.LENGTH_SHORT).show();
                        }
                        // 理赔上传结束后，调用发送短信接口
                        sendSMS(jsonObject.getJSONObject("datas").getJSONObject("case").getString("caseid"),3);
                        //发送本地广播
                        Intent intent = new Intent("OnlineSurvey2");
                        localBroadcastManager.sendBroadcast(intent);
                    } else {
                        Message message = new Message();
                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                        handler.sendMessage(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_NET_FAIL;
                handler.sendMessage(message);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");

                //上传授权码
                headers.put("tokenid", PreferenceUtil.getString("tokenid", null));

                return headers;
            }
        };
        VLCApplication.queue.add(jsonRequest);
    }

    @Override
    protected void loadData() {

    }

    /**
     * 判断所有照片是否全有照片
     *
     * @return true是 / false否
     */
    private boolean isFieldFull() {

        if (idOppositeUrl == null || "".equals(idOppositeUrl)) {
            ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传身份证反面照片");
            return false;
        } else if (idPositiveUrl == null || "".equals(idPositiveUrl)) {
            ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传身份证正面照片");
            return false;
        } else if (bankUrl == null || "".equals(bankUrl)) {
            ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传银行卡照片");
            return false;
        } else if (driverLicenseUrl == null || "".equals(driverLicenseUrl)) {
            ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传行驶证照片");
            return false;
        } else if (frameNumberUrl == null || "".equals(frameNumberUrl)) {
            ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传驾驶证照片");
            return false;
        } else if (drivingLicenseUrl == null || "".equals(drivingLicenseUrl)) {
            ToastUtils.showNomalShortToast(getActivity(), "请按照系统提示上传车架号照片");
            return false;
        } else {
            return true;
        }
    }

    /**
     * 弹出提示框
     *
     * @param lable
     */
    private void showTipDialog(String lable) {
        dialog.title(getString(R.string.tip));
        dialog.isTitleShow(true)//
                .cornerRadius(5)//
                .content(lable)//
                .contentGravity(Gravity.CENTER)//
                .btnTextSize(15.5f, 15.5f)//
                .widthScale(0.85f)//
                .btnText(mStringBts)
                .btnTextColor(new int[]{ContextCompat.getColor(getActivity(),R.color.primary),ContextCompat.getColor(getActivity(),R.color.alphablack)})
                .show();
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 获取七牛token
     */
    private void getQiniuToken() {

        JSONObject jsonObject = new JSONObject();
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.QINIU_TOKEN + "?n=6", jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    if (200 == jsonObject.getInt("code")) {
                        baseUrl = jsonObject.getJSONObject("data").getString("baseUrl");
                        qiniuToken = jsonObject.getJSONObject("data").getString("token");
                        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("fileName");
                        arrPayment.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            arrPayment.add(jsonArray.get(i).toString());
                        }

                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);

                        upLoadIdOpposite(arrPayment.get(0));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_NET_FAIL;
                handler.sendMessage(message);

            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");

                //上传授权码
                headers.put("tokenid", PreferenceUtil.getString("tokenid", null));

                return headers;
            }
        };
        VLCApplication.queue.add(jsonRequest);

    }

    /**
     * 上传身份证反面照片
     *
     * @param fileName
     */
    private void upLoadIdOpposite(String fileName) {

        VLCApplication.license_b.clear();
        VLCApplication.compensatepictures.clear();
        //上传身份证反面照片
        File IDOppositeImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDOpposite.png");
        if (IDOppositeImageFile.exists()) {
            VLCApplication.uploadManager.put(IDOppositeImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    String url = Config.QINIU_BASE_URL + attachid;

                                    Map<String, String> license_bMap = new HashMap<>();
                                    license_bMap.put("attachid", attachid);

                                    VLCApplication.license_b.add(license_bMap);
                                    VLCApplication.compensatepictures.put("license_b", VLCApplication.license_b);

                                    currentProgress += 1;
                                    kProgressHUD.setProgress(currentProgress);

                                    upLoadIdPositive(arrPayment.get(1));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                Message message = new Message();
                                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                handler.sendMessage(message);
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
//            Map<String, String> license_bMap = new HashMap<>();
//            license_bMap.put("attachid", "");

//            VLCApplication.license_b.add(license_bMap);
            VLCApplication.compensatepictures.put("license_b", VLCApplication.license_b);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            upLoadIdPositive(arrPayment.get(1));
        }
    }

    /**
     * 上传身份证正面照片
     *
     * @param fileName
     */
    private void upLoadIdPositive(String fileName) {

        VLCApplication.license_a.clear();
        //上传身份证正面照片
        File IDPositiveImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/IDPositive.png");
        if (IDPositiveImageFile.exists()) {
            VLCApplication.uploadManager.put(IDPositiveImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    String url = Config.QINIU_BASE_URL + attachid;

                                    Map<String, String> license_bMap = new HashMap<>();
                                    license_bMap.put("attachid", attachid);

                                    VLCApplication.license_a.add(license_bMap);
                                    VLCApplication.compensatepictures.put("license_a", VLCApplication.license_a);

                                    currentProgress += 1;
                                    kProgressHUD.setProgress(currentProgress);

                                    upLoadBank(arrPayment.get(2));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                Message message = new Message();
                                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                handler.sendMessage(message);
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
//            Map<String, String> license_bMap = new HashMap<>();
//            license_bMap.put("attachid", "");

//            VLCApplication.license_a.add(license_bMap);
            VLCApplication.compensatepictures.put("license_a", VLCApplication.license_a);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            upLoadBank(arrPayment.get(2));
        }
    }

    /**
     * 上传银行卡照片
     *
     * @param fileName
     */
    private void upLoadBank(String fileName) {

        VLCApplication.card.clear();
        //上传银行卡照片
        File bankImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/bank.png");
        if (bankImageFile.exists()) {
            VLCApplication.uploadManager.put(bankImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    String url = Config.QINIU_BASE_URL + attachid;

                                    Map<String, String> license_bMap = new HashMap<>();
                                    license_bMap.put("attachid", attachid);

                                    VLCApplication.card.add(license_bMap);
                                    VLCApplication.compensatepictures.put("card", VLCApplication.card);

                                    currentProgress += 1;
                                    kProgressHUD.setProgress(currentProgress);

                                    upLoadDrivingLicense(arrPayment.get(3));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                Message message = new Message();
                                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                handler.sendMessage(message);
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
//            Map<String, String> license_bMap = new HashMap<>();
//            license_bMap.put("attachid", "");

//            VLCApplication.card.add(license_bMap);
            VLCApplication.compensatepictures.put("card", VLCApplication.card);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            upLoadDrivingLicense(arrPayment.get(3));
        }
    }

    /**
     * 上传行驶证照片
     *
     * @param fileName
     */
    private void upLoadDrivingLicense(String fileName) {

        VLCApplication.permit.clear();
        //上传行驶证照片
        File drivingLicenseImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/drivingLicense.png");
        if (drivingLicenseImageFile.exists()) {
            VLCApplication.uploadManager.put(drivingLicenseImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    String url = Config.QINIU_BASE_URL + attachid;

                                    Map<String, String> license_bMap = new HashMap<>();
                                    license_bMap.put("attachid", attachid);

                                    VLCApplication.permit.add(license_bMap);
                                    VLCApplication.compensatepictures.put("permit", VLCApplication.permit);

                                    currentProgress += 1;
                                    kProgressHUD.setProgress(currentProgress);

                                    upLoadDriverLicense(arrPayment.get(4));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                Message message = new Message();
                                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                handler.sendMessage(message);
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
//            Map<String, String> license_bMap = new HashMap<>();
//            license_bMap.put("attachid", "");

//            VLCApplication.permit.add(license_bMap);
            VLCApplication.compensatepictures.put("permit", VLCApplication.permit);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            upLoadDriverLicense(arrPayment.get(4));
        }
    }

    /**
     * 上传驾驶证照片
     *
     * @param fileName
     */
    private void upLoadDriverLicense(String fileName) {

        VLCApplication.drivelicense.clear();
        //上传驾驶证照片
        File driverLicenseImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/driverLicense.png");
        if (driverLicenseImageFile.exists()) {
            VLCApplication.uploadManager.put(driverLicenseImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    String url = Config.QINIU_BASE_URL + attachid;

                                    Map<String, String> license_bMap = new HashMap<>();
                                    license_bMap.put("attachid", attachid);

                                    VLCApplication.drivelicense.add(license_bMap);
                                    VLCApplication.compensatepictures.put("drivelicense", VLCApplication.drivelicense);

                                    currentProgress += 1;
                                    kProgressHUD.setProgress(currentProgress);

                                    upLoadFrameNumber(arrPayment.get(5));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                Message message = new Message();
                                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                handler.sendMessage(message);
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
//            Map<String, String> license_bMap = new HashMap<>();
//            license_bMap.put("attachid", "");

//            VLCApplication.drivelicense.add(license_bMap);
            VLCApplication.compensatepictures.put("drivelicense", VLCApplication.drivelicense);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            upLoadFrameNumber(arrPayment.get(5));
        }
    }

    /**
     * 上传车架号照片
     *
     * @param fileName
     */
    private void upLoadFrameNumber(String fileName) {

        VLCApplication.frame.clear();
        //上传车架号照片
        File frameNumberImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/frameNumber.png");
        if (frameNumberImageFile.exists()) {
            VLCApplication.uploadManager.put(frameNumberImageFile, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    String url = Config.QINIU_BASE_URL + attachid;

                                    Map<String, String> license_bMap = new HashMap<>();
                                    license_bMap.put("attachid", attachid);

                                    VLCApplication.frame.add(license_bMap);
                                    VLCApplication.compensatepictures.put("frame", VLCApplication.frame);

                                    currentProgress += 1;
                                    kProgressHUD.setProgress(currentProgress);

                                    oneKeyPayment2();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                Message message = new Message();
                                message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                handler.sendMessage(message);
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        } else {
//            Map<String, String> license_bMap = new HashMap<>();
//            license_bMap.put("attachid", "");

//            VLCApplication.frame.add(license_bMap);
            VLCApplication.compensatepictures.put("frame", VLCApplication.frame);

            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);

            oneKeyPayment2();
        }
    }

    /**
     * 调用发送短信接口
     * @param caseid
     */
    private void sendSMS(String caseid,int type) {
        Map<String, String> map = new HashMap<>();
        JSONObject jsonObject = new JSONObject(map);

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, Config.BASE_URL+"/ins/api/sendsmstip?caseId="+caseid+"&type="+type, jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {

            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");

                //上传授权码
                headers.put("tokenid", PreferenceUtil.getString("tokenid", null));

                return headers;
            }
        };
        VLCApplication.queue.add(jsonRequest);
    }
}
