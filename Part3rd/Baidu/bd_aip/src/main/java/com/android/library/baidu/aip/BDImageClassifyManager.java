package com.android.library.baidu.aip;

import com.android.library.baidu.aip.data.InfoBean;
import com.baidu.aip.imageclassify.AipImageClassify;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Optional;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.schedulers.Schedulers;

public enum BDImageClassifyManager {
    I;

    private static final String APP_ID = "22862077";
    private static final String API_KEY = "NtuiBF2DnOEpbsl6MG1Ylkb2";
    private static final String SECRET_KEY = "KGaLKmInzDWO7dX73g2mVDYzX7hulTXc";

    private static final String FREEME_APP_ID = "14252068";
    private static final String FREEME_API_KEY = "SYUrq5t9EVzCBDOc3L6nTgHb";
    private static final String FREEME_SECRET_KEY = "28sCxTC3D5xRurhfMuBS8fxCwi0BgaR2";

    private Gson mGson;
    private AipImageClassify mClient;
    private ResponseListener mListener;

    private AipImageClassify getAipImageClassify() {
        return Optional.ofNullable(mClient).orElseGet(() -> {
            mClient = BuildConfig.DEBUG ? new AipImageClassify(APP_ID, API_KEY, SECRET_KEY)
                    : new AipImageClassify(FREEME_APP_ID, FREEME_API_KEY, FREEME_SECRET_KEY);
            mClient.setConnectionTimeoutInMillis(2000);
            mClient.setSocketTimeoutInMillis(60000);
            return mClient;
        });
    }

    private Gson getGson() {
        return Optional.ofNullable(mGson).orElseGet(() -> {
            mGson = new Gson();
            return mGson;
        });
    }

    /**
     * @param data NV21 data
     */
    public void request(byte[] data, ResponseListener listener) {
        Observable<InfoBean> observable = Observable.defer((Supplier<ObservableSource<JSONObject>>) () -> Observable.just(getData(data)))
                .map(jsonObject -> getGson().fromJson(jsonObject.toString(), InfoBean.class))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        mListener = listener;
        if (mListener != null) {
            observable.subscribe(infoBean -> mListener.onNext(infoBean), throwable -> mListener.onError(throwable));
        }
    }

    private JSONObject getData(byte[] data) {
        HashMap<String, String> options = new HashMap<>();
        options.put("baike_num", "5");
        return getAipImageClassify().advancedGeneral(data, options);
    }

    public interface ResponseListener {
        void onNext(InfoBean info);

        void onError(Throwable throwable);
    }
}
