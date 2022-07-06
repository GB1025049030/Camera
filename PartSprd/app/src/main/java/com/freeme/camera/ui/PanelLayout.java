package com.freeme.camera.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.camera.MultiToggleImageButton;
import com.android.camera.ui.RotateImageButton;
import com.android.camera2.R;
import com.freeme.camera.util.PanelUtil;

import java.util.ArrayList;

public class PanelLayout extends LinearLayout {

    public static final int NORMAL_MODE = 0;
    public static final int SIDE_MODE = 1;

    ArrayList<PanelUtil.IconAndDes> mList = new ArrayList<>();
    private int mDisplayMode;

    public PanelLayout(Context context) {
        this(context, null);
    }

    public PanelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PanelLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PanelLayout);
        int mode = typedArray.getInt(R.styleable.PanelLayout_displayMode, NORMAL_MODE);
        if ((mode & SIDE_MODE) != 0) {
            mDisplayMode = SIDE_MODE;
        } else {
            mDisplayMode = NORMAL_MODE;
        }
        typedArray.recycle();
    }

    public void addButtons(ArrayList<PanelUtil.IconAndDes> list) {
        if (list.size() <= 0) {
            return;
        }

        mList.addAll(list);
        int length = mList.size();
        for (int i = 0; i < length; i++) {
            PanelUtil.IconAndDes value = mList.get(i);
            addButton(value, i, length);
        }

    }

    public void addButtons(PanelUtil.IconAndDes value, int index) {
        mList.add(index, value);
        addButton(value, index, mList.size());
        if (index == 0 && mList.size() >= 2) {
            ((ViewGroup) this.getChildAt(1)).getChildAt(0).setVisibility(View.VISIBLE);
        }
    }

    public void removeButton(Integer id) {
        int index = findItemIndex(id);

        if (index == -1) {
            return;
        }

        PanelUtil.IconAndDes value = mList.remove(index);

        if (index == 0) {
            ((ViewGroup) this.getChildAt(1)).getChildAt(0).setVisibility(View.GONE);
        }
        this.removeViewAt(index);
    }

    public View getPanelButton(Integer id) {
        int index = findItemIndex(id);

        if (index == -1) {
            return null;
        }

        return findViewById(id);
    }

    public void showButton(Integer id) {
        int index = findItemIndex(id);

        if (index == -1) {
            return;
        }
        this.getChildAt(index).setVisibility(View.VISIBLE);
    }

    public void hideButton(Integer id) {
        int index = findItemIndex(id);
        if (index == -1) {
            return;
        }
        this.getChildAt(index).setVisibility(View.GONE);
    }


    public void updateButton(Integer id, PanelUtil.IconAndDes value) {
        int index = findItemIndex(id);

        if (index == -1) {
            return;
        }

        mList.set(index, value);
        overRideButtonValue(index, value);

    }

    public boolean isContain(Integer id) {
        int index = findItemIndex(id);

        return !(index == -1);
    }

    private void overRideButtonValue(int index, PanelUtil.IconAndDes value) {
        if (value.buttonType == R.integer.button_type_multitoggleimagebutton) {
            MultiToggleImageButton button = (MultiToggleImageButton) ((ViewGroup) this.getChildAt(index)).getChildAt(1);
            button.setId(value.id);
            button.overrideContentDescriptions(value.desID);
            button.overrideImageIds(value.iconID);
        } else if (value.buttonType == R.integer.button_type_rotateimagebutton) {
            RotateImageButton button = (RotateImageButton) ((ViewGroup) this.getChildAt(index)).getChildAt(1);
            button.setId(value.id);
            button.setImageResource(value.iconID);
        }
    }

    private void addButton(PanelUtil.IconAndDes value, int index, int length) {
        boolean isLeftEdge = index == 0 && length != 1;
        LinearLayout frame = generateButtonLayout(value, isLeftEdge);
        addButton(frame, index, isLeftEdge);

    }

    private void addButton(LinearLayout frame, int index, boolean leftEdge) {
        LayoutParams lp;
        if (leftEdge) {
            lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
        } else {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT, 1);
        }

        this.addView(frame, index, lp);
    }

    private LinearLayout generateButtonLayout(PanelUtil.IconAndDes value, boolean leftEdge) {
        LinearLayout frame = null;
        if (value.buttonType == R.integer.button_type_multitoggleimagebutton) {
            frame = addMultitoggleImageButton(value, leftEdge);
        } else if (value.buttonType == R.integer.button_type_rotateimagebutton) {
            frame = addRotateImageButton(value, leftEdge);
        }
        return frame;
    }

    private LinearLayout addMultitoggleImageButton(PanelUtil.IconAndDes value, boolean leftEdge) {
        int layoutId = R.layout.panel_item_multitoggle_image_button;
        LayoutInflater lf = LayoutInflater.from(getContext());
        LinearLayout frame = (LinearLayout) lf.inflate(layoutId, null);

        MultiToggleImageButton button = (MultiToggleImageButton) frame.getChildAt(1);
        button.setId(value.id);
        button.overrideContentDescriptions(value.desID);
        button.overrideImageIds(value.iconID);
        button.setState(0);

        if (leftEdge) {
            frame.getChildAt(0).setVisibility(View.GONE);
        }

        return frame;
    }

    private LinearLayout addRotateImageButton(PanelUtil.IconAndDes value, boolean leftEdge) {
        int layoutId = R.layout.panel_item_rotate_image_button;
        LayoutInflater lf = LayoutInflater.from(getContext());
        LinearLayout frame = (LinearLayout) lf.inflate(layoutId, null);

        RotateImageButton button = (RotateImageButton) frame.getChildAt(1);
        button.setId(value.id);
        button.setImageResource(value.iconID);

        if (leftEdge) {
            frame.getChildAt(0).setVisibility(View.GONE);
        }

        return frame;
    }

    private int findItemIndex(Integer id) {
        int i = 0;
        for (; i < mList.size(); i++) {
            if (mList.get(i).id == id) {
                break;
            }
        }

        if (i == mList.size()) {
            return -1;
        } else {
            return i;
        }
    }

    public int getDisplayMode() {
        return mDisplayMode;
    }

    @Override
    public int getVisibility() {
        return super.getVisibility();
    }

    public boolean isNeedHide() {
        return getDisplayMode() == SIDE_MODE && getFirstActiveChildId(this) != null;
    }

    public void setFirstChildState(int state) {
        MultiToggleImageButton button = getFirstActiveChildId(this);
        if (button != null) button.setState(state);
    }

    private MultiToggleImageButton getFirstActiveChildId(ViewGroup group) {
        MultiToggleImageButton childButton;
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i) instanceof MultiToggleImageButton) {
                if (((MultiToggleImageButton) group.getChildAt(i)).getState() != 0) {
                    return (MultiToggleImageButton) group.getChildAt(i);
                }
            } else if (group.getChildAt(i) instanceof ViewGroup) {
                childButton = getFirstActiveChildId((ViewGroup) group.getChildAt(i));
                if (childButton != null) {
                    return childButton;
                }
            }
        }
        return null;
    }
}
