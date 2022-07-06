package com.freeme.camera.modules.openglfilter.effectphoto;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.android.camera2.R;
import com.bytedance.labcv.effectsdk.BytedEffectConstants;
import com.bytedance.labcv.effectwork.effect.fragment.EffectFragment;
import com.bytedance.labcv.effectwork.effect.model.ComposerNode;
public class EffectSettingAdjustView extends LinearLayout {
    private Context mContext;
    private EffectProgressBar mEffectProgressBar;
    private int mCurrentSlicePos;
    private OnPickerListener mListener;
    private final static int MSG_DELAY_HIDE = 0;
    private final static int DELAY_HIDE_MS = 1000;
    //public FeatureBoardFragment.ISettingsCallBack mCallback;
    public EffectFragment.IEffectCallback mCallback;
    private float mCurrentProgress;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DELAY_HIDE:
                    hide();
                    break;
                default:
                    break;
            }
        }
    };
    public void hide() {
        setVisibility(View.GONE);
    }
    public EffectSettingAdjustView(Context context, int mCurrentItem, OnPickerListener listener, int paddingleft, int paddingright, float progress) {
        super(context);
        mContext = context;
        mListener = listener;
        mCurrentSlicePos = mCurrentItem;
        mCurrentProgress = progress;
        inflateView(paddingleft, paddingright);
    }
    private void inflateView(int paddingleft, int paddingright) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.effect_setting_adjust_view_container, this);
        mEffectProgressBar = (EffectProgressBar) view.findViewById(R.id.pb_effect);
        mEffectProgressBar.setOnProgressChangedListener(new EffectProgressBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(EffectProgressBar progressBar, float progress, boolean isNeedStorage) {
                //1.Set effect filter 2.storage filter
                updateBeautyOrReshapeIntensity(mCurrentSlicePos, (int) (progress * 100));
                if (mListener != null && isNeedStorage) {
                    mListener.func(mCurrentSlicePos, (int) (progress * 100));
                }
            }
        });
    }
    public void show(boolean isAnimate) {
        setVisibility(View.VISIBLE);
        mEffectProgressBar.setProgress(mCurrentProgress);
    }
    public void hide(boolean isAnimate) {
        mHandler.removeMessages(MSG_DELAY_HIDE);
        mHandler.sendEmptyMessageDelayed(MSG_DELAY_HIDE, DELAY_HIDE_MS);
    }
    public interface OnPickerListener {
        public void func(int mode, int progress);
    }
    /**
     * update Beauty Or Reshape Intensity
     *
     * @param intensitytype intensity type.
     * @param progress      intensity level.
     */
    private void updateBeautyOrReshapeIntensity(int intensitytype, int progress) {
        switch (intensitytype) {
            case 0://Slim
                //mCallback.updateComposerNodeIntensity(new ComposerNode(0, "reshape", EffectUtil.bytedEffectKeys[0], progress * EffectUtil.DEFAULT_SLIM_RATIO / 100.0f));
                break;
            case 1://Enlarge
                //mCallback.updateComposerNodeIntensity(new ComposerNode(1, "reshape", EffectUtil.bytedEffectKeys[1], progress * EffectUtil.DEFAULT_ENLARGE_RATIO / 100.0f));
                break;
            case 2://BeautySmooth
                //mCallback.updateComposerNodeIntensity(new ComposerNode(2, "beauty", EffectUtil.bytedEffectKeys[2], progress / 100.0f));
                break;
            case 3://BeautyWhite
                //mCallback.updateComposerNodeIntensity(new ComposerNode(3, "beauty", EffectUtil.bytedEffectKeys[3], progress * EffectUtil.DEFAULT_WHITE_RATIO / 100.0f));
                break;
            case 4://fade filter
                //mCallback.updateComposeNodeIntensity(new ComposerNode(5, "beauty", "epm/frag/sharpen", progress / 100.0f));
                //mCallback.updateComposerNodeIntensity(BytedEffectConstants.IntensityType.Filter, progress / 100.0f);
                break;
            default:
                break;
        }
    }
    public void setCallback(EffectFragment.IEffectCallback callback) {
        this.mCallback = callback;
    }
    public void setCurrentSlicePosAndProgress(int currentSlicePos, float progress) {
        mCurrentSlicePos = currentSlicePos;
        mCurrentProgress = progress;
        if (null != mEffectProgressBar) {
            mEffectProgressBar.setProgress(mCurrentProgress);
        }
    }
}
