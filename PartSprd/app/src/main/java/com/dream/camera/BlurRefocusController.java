package com.dream.camera;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.os.Handler;
import android.util.Log;

import com.android.camera.settings.Keys;
import com.dream.camera.settings.DataConfig;
import com.dream.camera.settings.DataModuleManager;
import com.dream.camera.settings.DreamSettingUtil;
import com.android.camera2.R;

public class BlurRefocusController {

    private int parameterLevel;
    private String[] fNumList = new String[] {"F0.95", "F1.4", "F2.0", "F2.8", "F4.0", "F5.6", "F8.0", "F11.0", "F13.0", "F16.0"};

    public interface BlurRefocusFNumberListener {

        public void onFNumberValueChanged(int value);

    }

    class BlurRefocusSeekBarChangedListener implements OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar SeekBarId, int progress,
                boolean fromUser) {
            Log.d(TAG, "onProgressChanged " + progress);
            parameterLevel = progress;
            mCurFNumberLevel.setText(fNumList[progress]);
            setValue(parameterLevel + 1);
            mHandler.removeCallbacks(runnAble);
            mHandler.postAtTime(runnAble, 100);
        }

        @Override
        public void onStartTrackingTouch(SeekBar SeekBarId) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar SeekBarId) {
            // SPRD: fix bug 487525 save makeup level for makeup module
            mController.onFNumberValueChanged(getValue());
        }
    }

    private class UpdateRunnable implements Runnable{

        @Override
        public void run() {
            mController.onFNumberValueChanged(getValue());
        }

    }

    private int getValue(){
        return DreamSettingUtil.convertToInt(DataModuleManager
                .getInstance(mBlurRefocusSeekBar.getContext())
                .getCurrentDataModule()
                .getString(DataConfig.SettingStoragePosition.positionList[3],
                        mKey, "" + mDefaultValue));
    }

    private void setValue(int level){
        DataModuleManager
                .getInstance(mBlurRefocusSeekBar.getContext())
                .getCurrentDataModule()
                .set(DataConfig.SettingStoragePosition.positionList[3], mKey,
                        "" + level);
    }

    private static final String TAG = "BlurRefocusController";

    private SeekBar mBlurRefocusSeekBar;
    private LinearLayout mBlurRefocusControllerView;
    private TextView mCurFNumberLevel;
    private BlurRefocusFNumberListener mController;
    private String mKey;
    private int mDefaultValue;
    Handler mHandler;
    UpdateRunnable runnAble;

    public BlurRefocusController(View extendPanelParent, BlurRefocusFNumberListener listener,
            String key, int defaultValue) {
        if (extendPanelParent != null) {
            mBlurRefocusControllerView = (LinearLayout) extendPanelParent
                    .findViewById(R.id.dream_blur_refocus_panel);
            mBlurRefocusSeekBar = (SeekBar) mBlurRefocusControllerView
                    .findViewById(R.id.blur_f_number_seekbar);
            mBlurRefocusSeekBar
                    .setOnSeekBarChangeListener(new BlurRefocusSeekBarChangedListener());
            mCurFNumberLevel = (TextView) mBlurRefocusControllerView
                    .findViewById(R.id.current_blur_f_number_level);
            mController = listener;
            mKey = key;
            mDefaultValue = defaultValue;

            mHandler = new Handler(extendPanelParent.getContext().getMainLooper());
            runnAble = new UpdateRunnable();
        }
        resumeFNumberControllerView();
    }

    private void initFNumberLevel() {
        if (mBlurRefocusSeekBar != null) {
            int curentValue = getValue() - 1;
            mBlurRefocusSeekBar.setProgress(curentValue);
            mCurFNumberLevel.setText(fNumList[curentValue]);
            mBlurRefocusSeekBar.invalidate();
            mCurFNumberLevel.invalidate();
            mController.onFNumberValueChanged(getValue());
        }
    }

    public void resumeFNumberControllerView() {
        if (mBlurRefocusSeekBar == null) {
            return;
        }
        initFNumberLevel();

        mBlurRefocusControllerView.setVisibility(View.VISIBLE);
    }

    public void hideFNumberControllerView(){
        mBlurRefocusControllerView.setVisibility(View.INVISIBLE);
    }

    public void resetFNumberControllerView() {
        if (mBlurRefocusSeekBar == null) {
            return;
        }
        int resetValue = getValue() - 1;
        mBlurRefocusSeekBar.setProgress(resetValue);
        mCurFNumberLevel.setText(fNumList[resetValue]);
        mBlurRefocusSeekBar.invalidate();
        mCurFNumberLevel.invalidate();
        mBlurRefocusControllerView.setVisibility(View.VISIBLE);
    }
}
