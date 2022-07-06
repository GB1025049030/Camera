package com.freeme.camera.modules.openglfilter.effectphoto;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.camera2.R;
import com.bytedance.labcv.effectsdk.BytedEffectConstants;
import com.bytedance.labcv.effectwork.effect.fragment.EffectFragment;
import com.bytedance.labcv.effectwork.effect.model.ComposerNode;

public class EffectAdjustManager implements EffectSettingAdjustView.OnPickerListener, OnClickListener {
    private static final String TAG = "[TYD_DEBUG]BeautyFaceAdjustManager";
    private static final int MAX_ITEMS = 4;
    private EffectSettingAdjustView mAdjustCtrl;
    private boolean mShowingCtrl = false;
    private int mCurrentItem = -2;
    Activity mContext;
    ViewGroup mListLayout;
    ViewGroup mDegreelayout;
    LinearLayout modeLayout;
    LinearLayout mDegreeView;
    LinearLayout mFacebeautyLayout;
    ViewGroup mLayout;
    private ViewGroup mSettingList;
    private TextView mDegree;
    private ImageView mDegreeIv;
    private TextView mModeBtnDown;
    private boolean isShownDegree = false;
    private String[] mFacetitles;
    //public FeatureBoardFragment.ISettingsCallBack mCallback;
    public EffectFragment.IEffectCallback mCallback;
    private boolean isCollpased = false;
    public EffectAdjustManager(ViewGroup Layout, Activity context) {
        super();
        mLayout = Layout;
        mContext = context;
        LayoutInflater mInflater = LayoutInflater.from(context);
        mSettingList = (ViewGroup) mInflater.inflate(
                context.getResources().getLayout(R.layout.effectbeauty_setting_panel), null);
        mFacebeautyLayout = (LinearLayout) mSettingList.findViewById(R.id.effectbeauty_layout);
        mListLayout = (ViewGroup) mSettingList.findViewById(R.id.effect_item_list);
        mDegreelayout = (ViewGroup) mSettingList.findViewById(R.id.effect_Degree_layout);
        modeLayout = (LinearLayout) mSettingList.findViewById(R.id.effect_mode_layout);
        mDegreelayout.setOnClickListener(this);
        modeLayout.setOnClickListener(this);
        mDegreeView = (LinearLayout) mSettingList.findViewById(R.id.effect_degree_view);
        mDegree = (TextView) mSettingList.findViewById(R.id.effect_Degree);
        mDegreeIv = (ImageView) mSettingList.findViewById(R.id.effect_degreeiv);
        mModeBtnDown = (TextView) mSettingList.findViewById(R.id.effect_mode_button_down);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.bottomMargin = /*mContext.getAppUi().getShutterRootView().getHeight()*/0;
        mSettingList.setOnClickListener(this);
        Resources res = context.getResources();
        mDegreeView.setOnClickListener(this);
        mFacetitles = new String[]{
                mContext.getResources().getString(R.string.setting_face_lift),
                mContext.getResources().getString(R.string.setting_big_eye),
                mContext.getResources().getString(R.string.setting_skin_grinding),
                mContext.getResources().getString(R.string.setting_skin_whitening),
                mContext.getResources().getString(R.string.setting_skin_monochrome)
        };
        Drawable[] faceviews = {
                mContext.getResources().getDrawable(R.drawable.effect_beauty_face_lift),
                mContext.getResources().getDrawable(R.drawable.effect_beauty_big_eye),
                mContext.getResources().getDrawable(R.drawable.effect_beauty_skin_grinding),
                mContext.getResources().getDrawable(R.drawable.effect_beauty_skin_whitening),
                mContext.getResources().getDrawable(R.drawable.effect_beauty_fade),
        };
        for (int i = 0; i < mFacetitles.length; i++) {
            View item = mInflater.inflate(R.layout.effectbeauty_setting_item, mListLayout, false);
            TextView facetitles1 = (TextView) item.findViewById(R.id.effect_beautymode_title);
            facetitles1.setText(mFacetitles[i]);
            ImageView faceviews1 = (ImageView) item.findViewById(R.id.effect_beautymode_imageview);
            facetitles1.setText(mFacetitles[i]);
            faceviews1.setImageDrawable(faceviews[i]);
	
	
            /*if(i == 1){
	
	
                facetitles1.setSelected(true);
	
	
                faceviews1.setSelected(true);
	
	
            }*/
            mListLayout.addView(item);
            item.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = mListLayout.indexOfChild(v);
                    if (index < 0 || index == mCurrentItem) {
                        return;
                    }
                    TextView faceti = (TextView) v.findViewById(R.id.effect_beautymode_title);
                    ImageView faceiv = (ImageView) v.findViewById(R.id.effect_beautymode_imageview);
                    faceti.setSelected(true);
                    faceiv.setSelected(true);
                    switcher(index, false);
                }
            });
        }
        mModeBtnDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                expand();
            }
        });
        Layout.addView(mSettingList, lp);
    }
    public void collpase() {
        setCollpased(true);
        hide(true, mCurrentItem);
        if (modeLayout.getVisibility() == View.VISIBLE) {
            modeLayout.setVisibility(View.GONE);
            if (mDegreelayout.getVisibility() == View.VISIBLE) {
                mDegreelayout.setVisibility(View.GONE);
                isShownDegree = true;
            }
            mModeBtnDown.setVisibility(View.VISIBLE);
        }
    }
    public void allCollpase() {
        setCollpased(true);
        hide(true, mCurrentItem);
        if (modeLayout.getVisibility() == View.VISIBLE) {
            modeLayout.setVisibility(View.GONE);
            if (mDegreelayout.getVisibility() == View.VISIBLE) {
                mDegreelayout.setVisibility(View.GONE);
                isShownDegree = true;
            }
        }
    }
    public void expand() {
        if (modeLayout.getVisibility() != View.VISIBLE) {
            if (isShownDegree) {
                isShownDegree = false;
                mDegreelayout.setVisibility(View.VISIBLE);
            }
            modeLayout.setVisibility(View.VISIBLE);
            mModeBtnDown.setVisibility(View.GONE);
            switcher(mCurrentItem, true);
            //restore effect filter
            //mCallback.updateComposeNodeIntensity(new ComposerNode(0, "reshape", EffectUtil.bytedEffectKeys[0], EffectUtil.getLevelIndex(0) * EffectUtil.DEFAULT_SLIM_RATIO / 100.0f));
            //mCallback.updateComposeNodeIntensity(new ComposerNode(1, "reshape", EffectUtil.bytedEffectKeys[1], EffectUtil.getLevelIndex(1) * EffectUtil.DEFAULT_ENLARGE_RATIO / 100.0f));
            //mCallback.updateComposeNodeIntensity(new ComposerNode(2, "beauty", EffectUtil.bytedEffectKeys[2], EffectUtil.getLevelIndex(2) / 100.0f));
            //mCallback.updateComposeNodeIntensity(new ComposerNode(3, "beauty", EffectUtil.bytedEffectKeys[3], EffectUtil.getLevelIndex(3) * EffectUtil.DEFAULT_WHITE_RATIO / 100.0f));
            //mCallback.updateComposeNodeIntensity(new ComposerNode(5, "beauty", "epm/frag/sharpen", EffectBeautyUtil.getLevelIndex(5) / 100.0f));
            //mCallback.updateFilterIntensity(BytedEffectConstants.IntensityType.Filter, EffectUtil.getLevelIndex(4) / 100.0f);
        }
        setCollpased(false);
    }
    public void switcher(int index, boolean hasCollpased) {
        if (!hasCollpased) {
            if (index != mCurrentItem && mCurrentItem != -2) {
                TextView faceti = (TextView) mListLayout.getChildAt(mCurrentItem).findViewById(
                        R.id.effect_beautymode_title);
                ImageView faceiv = (ImageView) mListLayout.getChildAt(mCurrentItem).findViewById(R.id.effect_beautymode_imageview);
                faceti.setSelected(false);
                faceiv.setSelected(false);
                mCurrentItem = index;
                show(true);
            } else {
                if (mDegreelayout.isShown()) {
                    mCurrentItem = -2;
                    hide(true, index);
                } else {
                    mCurrentItem = index;
                    TextView faceti = (TextView) mListLayout.getChildAt(mCurrentItem).findViewById(
                            R.id.effect_beautymode_title);
                    ImageView faceiv = (ImageView) mListLayout.getChildAt(mCurrentItem).findViewById(R.id.effect_beautymode_imageview);
                    faceti.setSelected(true);
                    faceiv.setSelected(true);
                    show(true);
                }
            }
        } else {
            if (mCurrentItem != -2) {
                TextView faceti = (TextView) mListLayout.getChildAt(mCurrentItem).findViewById(
                        R.id.effect_beautymode_title);
                ImageView faceiv = (ImageView) mListLayout.getChildAt(mCurrentItem).findViewById(R.id.effect_beautymode_imageview);
                faceti.setSelected(true);
                faceiv.setSelected(true);
                show(true);
            }
        }
    }
    private void show(boolean isAnimate) {
        int paddingleft = (int) mListLayout.getChildAt(0).findViewById(R.id.effect_beautymode_title).getLeft();
        int paddingright = (int) (mListLayout.getChildAt(mFacetitles.length - 1).getWidth() -
                mListLayout.getChildAt(mFacetitles.length - 1).findViewById(R.id.effect_beautymode_title).getRight());
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        float sPixelDensity = metrics.density;
//        if (mAdjustCtrl == null) {
//            mAdjustCtrl = new EffectSettingAdjustView(mContext, mCurrentItem, this, paddingleft, paddingright, EffectUtil.getLevelIndex(mCurrentItem) / 100f);
//            mAdjustCtrl.setCallback(mCallback);
//            LinearLayout.LayoutParams params = null;
//            params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
//                    LayoutParams.WRAP_CONTENT);
//            mDegreelayout.addView(mAdjustCtrl, params);
//        } else {
//            mAdjustCtrl.setCurrentSlicePosAndProgress(mCurrentItem, EffectUtil.getLevelIndex(mCurrentItem) / 100f);
//        }
        mShowingCtrl = true;
        mDegreelayout.setVisibility(View.VISIBLE);
        mAdjustCtrl.show(isAnimate);
    }
    public void hide(boolean isAnimate, int index) {
        if (mListLayout == null || !mShowingCtrl || mAdjustCtrl == null) {
            return;
        }
        TextView faceti = (TextView) mListLayout.getChildAt(index).findViewById(
                R.id.effect_beautymode_title);
        ImageView faceiv = (ImageView) mListLayout.getChildAt(index).findViewById(R.id.effect_beautymode_imageview);
        faceti.setSelected(false);
        faceiv.setSelected(false);
        mDegreelayout.setVisibility(View.GONE);
        mAdjustCtrl.setVisibility(View.GONE);
        mShowingCtrl = false;
    }
    @Override
    public void func(int mode, int progress) {
        //EffectUtil.saveLevelIndex(mode, progress);
    }
    public void removeView(ViewGroup rootView) {
        rootView.removeView(mSettingList);
        rootView.removeView(mAdjustCtrl);
    }
    public void addView(ViewGroup rootView) {
        if (mSettingList != null && mSettingList.getParent() != null) {
            ViewGroup parentViewGrop = (ViewGroup) mSettingList.getParent();
            parentViewGrop.removeView(mSettingList);
        }
        rootView.addView(mSettingList);
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        if (id == R.id.effect_degree_view) {
            //switcher(-1);
        }
    }
    public void setPluginVisibility(boolean visible) {
        if (visible) {
            mFacebeautyLayout.setVisibility(View.VISIBLE);
            if (mListLayout.getChildAt(mCurrentItem) == null) {
                return;
            }
            if (mAdjustCtrl.isShown()) {
                TextView faceti = (TextView) mListLayout.getChildAt(mCurrentItem).findViewById(
                        R.id.effect_beautymode_title);
                ImageView faceiv = (ImageView) mListLayout.getChildAt(mCurrentItem).findViewById(R.id.effect_beautymode_imageview);
                faceti.setSelected(true);
                faceiv.setSelected(true);
            } else {
                TextView faceti = (TextView) mListLayout.getChildAt(mCurrentItem).findViewById(
                        R.id.effect_beautymode_title);
                ImageView faceiv = (ImageView) mListLayout.getChildAt(mCurrentItem).findViewById(R.id.effect_beautymode_imageview);
                faceti.setSelected(false);
                faceiv.setSelected(false);
            }
        } else {
            mFacebeautyLayout.setVisibility(View.INVISIBLE);
            if (mListLayout.getChildAt(mCurrentItem) != null) {
                TextView faceti = (TextView) mListLayout.getChildAt(mCurrentItem).findViewById(
                        R.id.effect_beautymode_title);
                ImageView faceiv = (ImageView) mListLayout.getChildAt(mCurrentItem).findViewById(R.id.effect_beautymode_imageview);
                faceti.setSelected(false);
                faceiv.setSelected(false);
            }
        }
    }
    public void setCallback(EffectFragment.IEffectCallback callback) {
        this.mCallback = callback;
        //mCallback.setEffectOn(true);
    }
    public int getCurrentItem() {
        return mCurrentItem;
    }
    public void setmCurrentItem(int currentItem) {
        mCurrentItem = currentItem;
    }
    public boolean getCollpased() {
        return isCollpased;
    }
    public void setCollpased(boolean collpase) {
        isCollpased = collpase;
    }
}