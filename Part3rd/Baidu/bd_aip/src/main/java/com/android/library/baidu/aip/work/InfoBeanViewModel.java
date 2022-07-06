package com.android.library.baidu.aip.work;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.library.baidu.aip.BDImageClassifyManager;
import com.android.library.baidu.aip.R;
import com.android.library.baidu.aip.data.InfoBean;

public class InfoBeanViewModel extends ViewModel {
    private static final String IKO_IMAGE_SEARCH_URL = "com.feimi.browser";
    private static final String INFO_FLOW_ADDRESS_URL = "https://mini.eastday.com/channels/index.html?type=toutiao&qid=qid11113";

    public MutableLiveData<InfoBean> mInfoBeanLiveData = new MutableLiveData<>();
    public MutableLiveData<byte[]> mRgbaArrayLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> mIsSuccess = new MutableLiveData<>();
    public MutableLiveData<String> mWebViewUrl = new MutableLiveData<>();
    private VMListener mVMListener;
    private Application mApp;
    private String mBrowserPackage;

    public void setApplication(@NonNull Application application) {
        mApp = application;
    }

    public void setResponseListener(VMListener listener) {
        this.mVMListener = listener;
    }

    public void request(final byte[] rgba) {
        mRgbaArrayLiveData.postValue(rgba);
        BDImageClassifyManager.I.request(rgba, new BDImageClassifyManager.ResponseListener() {
            @Override
            public void onNext(InfoBean info) {
                mInfoBeanLiveData.postValue(info);
                mIsSuccess.postValue(info.getResult().get(0).getBaike_info().hasImageUrl()
                        || info.getResult().get(0).getBaike_info().hasDescription());
                mWebViewUrl.postValue(INFO_FLOW_ADDRESS_URL);
                if (mVMListener != null) mVMListener.onNext(info);
            }

            @Override
            public void onError(Throwable throwable) {
                mInfoBeanLiveData.postValue(null);
                mIsSuccess.postValue(false);
                mWebViewUrl.postValue(INFO_FLOW_ADDRESS_URL);
                if (mVMListener != null) mVMListener.onError();
            }
        });
    }

    public LiveData<String> getWebViewUrl() {
        return mWebViewUrl;
    }

    public void startSearch(String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage(TextUtils.isEmpty(mBrowserPackage) ? IKO_IMAGE_SEARCH_URL : mBrowserPackage);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mApp.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(mApp, R.string.freeme_check_browser, Toast.LENGTH_SHORT).show();
        }
    }

    public void finish() {
        if (mVMListener != null) mVMListener.onFinish();
    }

    public void setBrowserPackage(String mBrowserPackage) {
        this.mBrowserPackage = mBrowserPackage;
    }

    public interface VMListener {
        void onNext(InfoBean info);

        void onError();

        void onFinish();
    }
}
