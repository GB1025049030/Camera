package com.freeme.camera.mode.photo.effect.debug;

import static com.freeme.camera.mode.photo.effect.manager.EffectDataManager.TYPE_BEAUTY_FACE;
import static com.freeme.camera.mode.photo.effect.manager.EffectDataManager.TYPE_MAKEUP;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.freeme.camera.mode.photo.effect.R;
import com.freeme.camera.mode.photo.effect.manager.EffectDataManager;
import com.freeme.camera.mode.photo.effect.model.EffectType;
import com.freeme.camera.mode.photo.effect.mvi.ui.view.EffectFragment;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EffectDataManager mEffectDataManager;
    private EffectFragment mEffectFragment = null;
    private FrameLayout mParent;
    private Button mBeautyBtn;
    private Button mMakeupBtn;
    private Button mBackOptionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEffectDataManager = new EffectDataManager(EffectType.LITE_ASIA);
        mEffectFragment = generateEffectFragment();

        mParent = findViewById(R.id.parent);
        
        mBeautyBtn = findViewById(R.id.beauty);
        mBeautyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEffectFragment.select(0);
            }
        });
        mMakeupBtn = findViewById(R.id.makeup);
        mMakeupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEffectFragment.select(1);
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.parent, mEffectFragment);
        transaction.commit();
    }

    private EffectFragment generateEffectFragment() {
        if (mEffectFragment != null) {
            return mEffectFragment;
        }
        EffectFragment effectFragment = new EffectFragment();
//        effectFragment.setData(this, mEffectDataManager, getTabItems(), EffectType.LITE_ASIA);
        return effectFragment;
    }

//    public List<EffectFragment.TabItem> getTabItems() {
//        return Arrays.asList(
//                new EffectFragment.TabItem(TYPE_BEAUTY_FACE, R.string.tab_face_beautification),
//                /*  new EffectFragment.TabItem(TYPE_BEAUTY_RESHAPE, R.string.tab_face_beauty_reshape),*/
//                //new EffectFragment.TabItem(TYPE_BEAUTY_BODY, R.string.tab_face_beauty_body),
//                new EffectFragment.TabItem(TYPE_MAKEUP, R.string.tab_face_makeup)
//                /*new EffectFragment.TabItem(TYPE_FILTER, R.string.tab_filter)*/
//        );
//    }

//    private EffectFragment generateEffectFragment() {
//        if (mEffectFragment != null) return mEffectFragment;
//
//        String feature = "";
//        String sEffectConfig = getIntent().getStringExtra(EffectConfig.EffectConfigKey);
//        if (sEffectConfig != null) {
//            EffectConfig effectConfig = new Gson().fromJson(sEffectConfig, EffectConfig.class);
//            if (effectConfig != null) {
//                feature = effectConfig.getFeature();
//            }
//        }
//        mFeature = feature;
//        EffectFragment effectFragment = new EffectFragment();
//        if (feature != null && feature.equals(FEATURE_AR_LIPSTICK)) {
//            effectFragment.useProgressBar(false);
//            effectFragment.setData(mContext,mEffectDataManager, getLipstickTabItems(),mEffectConfig.getEffectType());
//        } else if (feature != null && feature.equals(FEATURE_AR_HAIR_DYE)) {
//            mEffectManager.setSyncLoadResource(true);
//            effectFragment.setColorListPosition(EffectFragment.BOARD_FRAGMENT_HEAD_ABOVE).useProgressBar(false);
//            effectFragment.setData(mContext,mEffectDataManager, getHairDyeTabItems(),mEffectConfig.getEffectType());
//        } else {
//            effectFragment.setData(mContext,mEffectDataManager, getTabItems(),mEffectConfig.getEffectType());
//        }
//        effectFragment.setCallback(this);
//        return effectFragment;
//    }
}