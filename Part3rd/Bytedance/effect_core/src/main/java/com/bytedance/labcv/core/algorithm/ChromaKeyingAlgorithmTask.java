package com.bytedance.labcv.core.algorithm;

import android.content.Context;

import com.bytedance.labcv.core.algorithm.base.AlgorithmResourceProvider;
import com.bytedance.labcv.core.algorithm.base.AlgorithmTask;
import com.bytedance.labcv.core.algorithm.base.AlgorithmTaskKey;
import com.bytedance.labcv.core.algorithm.factory.AlgorithmTaskKeyFactory;
import com.bytedance.labcv.core.license.EffectLicenseProvider;
import com.bytedance.labcv.core.util.timer_record.LogTimerRecord;
import com.bytedance.labcv.effectsdk.BefChromaKeyingInfo;
import com.bytedance.labcv.effectsdk.BefDynamicGestureInfo;
import com.bytedance.labcv.effectsdk.BytedEffectConstants;
import com.bytedance.labcv.effectsdk.ChromaKeying;
import com.bytedance.labcv.effectsdk.DynamicGestureDetect;

import java.nio.ByteBuffer;

public class ChromaKeyingAlgorithmTask extends AlgorithmTask<ChromaKeyingAlgorithmTask.ChromaKeyingResourceProvider, BefChromaKeyingInfo> {

    public static final AlgorithmTaskKey CHROMA_KEYING = AlgorithmTaskKeyFactory.create("chromaKeying", true);

    private ChromaKeying mDetector;

    public ChromaKeyingAlgorithmTask(Context context, ChromaKeyingResourceProvider resourceProvider, EffectLicenseProvider licenseProvider) {
        super(context, resourceProvider, licenseProvider);
        mDetector = new ChromaKeying();
    }

    @Override
    public int initTask() {
        boolean onlineLicenseFlag = mLicenseProvider.getLicenseMode() == EffectLicenseProvider.LICENSE_MODE_ENUM.ONLINE_LICENSE;
        int ret = mDetector.init(mContext, mResourceProvider.chromaKeyingModel(), mLicenseProvider.getLicensePath(), onlineLicenseFlag);
        if (!checkResult("initChromaKeying", ret)) return ret;
        return ret;
    }

    @Override
    public BefChromaKeyingInfo process(ByteBuffer buffer, int width, int height, int stride, BytedEffectConstants.PixlFormat pixlFormat, BytedEffectConstants.Rotation rotation) {
        LogTimerRecord.RECORD("chromaKeying");
        BefChromaKeyingInfo info = mDetector.detect(buffer, pixlFormat, width, height, stride, rotation);
        LogTimerRecord.STOP("chromaKeying");
        return info;
    }

    @Override
    public int destroyTask() {
        mDetector.release();
        return 0;
    }

    @Override
    public int[] preferBufferSize() {
        return new int[0];
    }

    @Override
    public AlgorithmTaskKey key() {
        return CHROMA_KEYING;
    }


    public interface ChromaKeyingResourceProvider extends AlgorithmResourceProvider {
        String chromaKeyingModel();
    }
}
