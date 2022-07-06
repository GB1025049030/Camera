package com.android.library.baidu.aip.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Point;
import android.net.http.SslError;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.android.library.baidu.aip.R;
import com.android.library.baidu.aip.data.InfoBean;
import com.android.library.baidu.aip.databinding.LayoutActivityBaikeInfoBinding;
import com.android.library.baidu.aip.work.InfoBeanViewModel;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.WebViewClient;
import com.wang.avi.AVLoadingIndicatorView;
import com.wang.avi.Indicator;

import java.util.ArrayList;

public class BaikeInfoActivity extends AppCompatActivity implements DragScrollDetailsLayout.OnSlideFinishListener {
    public static final String RGBA_DATA_KEY = "rgba_data";
    public static final String SPECIFIED_BROWSER_PKG_KEY = "specified_browser_pkg";

    private static final int LOADER_SIZE_SCALE = 8;
    private static final String INDICATOR_VIEW_STYLE = "BallClipRotatePulseIndicator";
    private static final ArrayList<Dialog> LOADERS = new ArrayList<>();

    private LayoutActivityBaikeInfoBinding mBinding;
    private ViewModelProvider mActivityProvider;
    private InfoBeanViewModel mViewModel;

    private AgentWeb mAgentWeb;
    private AgentWeb.PreAgentWeb mPreAgentWeb;

    private String mCurrentStatus;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSystemBar(true);
        mContext = this;

        mViewModel = getActivityViewModel(InfoBeanViewModel.class);
        mViewModel.setApplication(this.getApplication());
        mViewModel.setResponseListener(new InfoBeanViewModel.VMListener() {
            @Override
            public void onNext(InfoBean info) {
                InfoBean.ResultBean.BaikeInfoBean baikeInfoBean = info.getResult().get(0).getBaike_info();
                if (!baikeInfoBean.hasBaikeUrl()) {
                    mBinding.success.imageIko.setVisibility(View.GONE);
                }
                if (!baikeInfoBean.hasDescription()) {
                    mBinding.success.textIko.setVisibility(View.GONE);
                }
                runOnUiThread(() -> {
                    stopLoading();
                });
            }

            @Override
            public void onError() {
                runOnUiThread(() -> {
                    stopLoading();
                });
            }

            @Override
            public void onFinish() {
                complete();
            }
        });
        mBinding = DataBindingUtil.setContentView(this, R.layout.layout_activity_baike_info);
        mBinding.setVm(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.dragLayout.setOnSlideDetailsListener(this);
        runOnUiThread(() -> showLoading(BaikeInfoActivity.this));
        createWebView();
        startRequest();
    }

    private void createWebView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mPreAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(mBinding.webParent, params)
                .useDefaultIndicator()
                .setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        return super.shouldOverrideUrlLoading(view, request);
                    }

                    @Override
                    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                        handler.proceed();
                    }
                })
                .createAgentWeb()
                .ready();
    }

    private void startRequest() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mViewModel.setBrowserPackage(bundle.getString(SPECIFIED_BROWSER_PKG_KEY));
            mViewModel.request(bundle.getByteArray(RGBA_DATA_KEY));
            mViewModel.getWebViewUrl().observe(this, s -> {
                if (!TextUtils.isEmpty(s)) {
                    mAgentWeb = mPreAgentWeb.go(s);
                    mAgentWeb.getWebLifeCycle().onResume();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        complete();
    }

    protected void complete() {
        finish();
        overridePendingTransition(R.anim.camera_zoom, R.anim.zoom_out);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (DragScrollDetailsLayout.CurrentTargetIndex.DOWNSTAIRS.name().equals(mCurrentStatus)) {
            if (mAgentWeb.handleKeyEvent(keyCode, event)) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        if (mAgentWeb != null) mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAgentWeb != null) mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroy();
    }

    @SuppressLint("InlinedApi")
    public void initSystemBar(Boolean isLight) {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (isLight) {
            window.setStatusBarColor(getResources().getColor(R.color.iko_background_color));
        } else {
            window.setStatusBarColor(getResources().getColor(R.color.iko_background_color));
        }
        View decor = window.getDecorView();
        int ui = decor.getSystemUiVisibility();
        if (isLight) {
            ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            ui &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decor.setSystemUiVisibility(ui);
    }

    protected <T extends ViewModel> T getActivityViewModel(Class<T> modelClass) {
        if (mActivityProvider == null) {
            mActivityProvider = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()));
        }
        return mActivityProvider.get(modelClass);
    }

    @Override
    public void onStatueChanged(DragScrollDetailsLayout.CurrentTargetIndex status) {
        mCurrentStatus = status.name();
    }

    public void showLoading(Context context) {
        final Dialog dialog = new Dialog(context, R.style.dialog);
        final AVLoadingIndicatorView avLoadingIndicatorView = new AVLoadingIndicatorView(context);
        avLoadingIndicatorView.setIndicatorColor(Color.parseColor("#9000ff00"));
        avLoadingIndicatorView.setIndicator(getIndicator(INDICATOR_VIEW_STYLE));
        dialog.setContentView(avLoadingIndicatorView);
        dialog.setCancelable(false);

        final Point point = getDisplaySize(context);
        if (point != null) {
            final int mResolutionWidth = point.x;
            final int mResolutionHeight = point.y;
            final Window dialogWindow = dialog.getWindow();
            if (dialogWindow != null) {
                final WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                lp.width = mResolutionWidth / LOADER_SIZE_SCALE;
                lp.height = mResolutionHeight / LOADER_SIZE_SCALE;
                lp.gravity = Gravity.CENTER;
            }
            LOADERS.add(dialog);
            dialog.show();
        }
    }

    public void stopLoading() {
        for (Dialog dialog : LOADERS) {
            if (dialog != null && dialog.isShowing()) {
                dialog.cancel();
            }
        }
    }

    private static Indicator getIndicator(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        final StringBuilder drawableClassName = new StringBuilder();
        if (!name.contains(".")) {
            final String defaultPackageName = AVLoadingIndicatorView.class.getPackage().getName();
            drawableClassName.append(defaultPackageName)
                    .append(".indicators")
                    .append(".");
        }
        drawableClassName.append(name);
        try {
            final Class<?> drawableClass = Class.forName(drawableClassName.toString());
            return (Indicator) drawableClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        final Point point = new Point();
        point.x = dm.widthPixels;
        point.y = dm.heightPixels;
        return point;
    }
}
