package com.example.administrator.lc_dvr.module.lc_dvr;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;

public class Advertisement extends BaseActivity {

    private TextView advertisement_title;

    private WebView webView;
    private ProgressBar pg1;

    @Override
    protected int setViewId() {
        return R.layout.advertisement_layout;
    }

    @Override
    protected void findView() {
        advertisement_title = (TextView) findViewById(R.id.advertisement_title);
    }

    @Override
    protected void init() {
        //获得界面传来的值
        Intent intent = getIntent();
        String adsTitle = intent.getStringExtra("adsTitle");
        String adsLink = intent.getStringExtra("adsLink");
        //设置页面的标题
        advertisement_title.setText(adsTitle);

        init2();
        webView.loadUrl(adsLink);

    }

    @Override
    protected void initEvents() {

    }

    @Override
    protected void loadData() {

    }

    private void init2() {
        // TODO 自动生成的方法存根
        webView = (WebView) findViewById(R.id.webview1);
        pg1 = (ProgressBar) findViewById(R.id.progressBar1);

        webView.setWebViewClient(new WebViewClient() {
            //覆写shouldOverrideUrlLoading实现内部显示网页
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    if (!url.startsWith("http://") || !url.startsWith("https://")) {
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                        startActivity(intent);
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
                view.loadUrl(url);
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                if (newProgress == 100) {
                    pg1.setVisibility(View.GONE);//加载完网页进度条消失
                } else {
                    pg1.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    pg1.setProgress(newProgress);//设置进度值
                }
            }
        });
    }

    //设置返回键动作（防止按返回键直接退出程序)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO 自动生成的方法存根
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {//当webview不是处于第一页面时，返回上一个页面
                webView.goBack();
                return true;
            } else {//当webview处于第一页面时,直接退出
                finish();
            }


        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 关闭当前的页面
     *
     * @param view
     */
    public void closeAdvertisement(View view) {
        finish();
    }
}
