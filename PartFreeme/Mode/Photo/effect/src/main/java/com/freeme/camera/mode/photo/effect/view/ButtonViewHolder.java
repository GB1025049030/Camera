package com.freeme.camera.mode.photo.effect.view;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.freeme.camera.mode.photo.effect.R;

public class ButtonViewHolder extends RecyclerView.ViewHolder {

    public static final float WH_RATIO = 1F;

    private int colorOn;
    private int colorOff;

    private LinearLayout llContent;
    private ImageView iv;
    private TextView tvTitle;
    private TextView tvDesc;
    private View vPoint;

    private boolean isOn = false;
    private boolean isPointOn = false;

    public ButtonViewHolder(View itemView) {
        super(itemView);
        llContent = itemView.findViewById(R.id.ll_content);
        iv = itemView.findViewById(R.id.iv_face_options);
        tvTitle = itemView.findViewById(R.id.tv_title_face_options);
        tvDesc = itemView.findViewById(R.id.tv_desc_face_options);
        vPoint = itemView.findViewById(R.id.v_face_options);

        colorOn = ActivityCompat.getColor(itemView.getContext(), R.color.colorWhite);
        colorOff = ActivityCompat.getColor(itemView.getContext(), R.color.colorGrey);
    }

    public void setIcon(int iconResource) {
        iv.setImageResource(iconResource);
    }

    public void setTitle(String title) {
        if (title.isEmpty()) {
            tvTitle.setVisibility(GONE);
        } else {
            tvTitle.setVisibility(VISIBLE);
            tvTitle.setText(title);
        }
    }

    public void setDesc(String desc) {
        if (desc == null || desc.isEmpty()) {
            tvDesc.setVisibility(GONE);
        } else {
            tvDesc.setVisibility(VISIBLE);
            tvDesc.setText(desc);
        }
    }

    public void change(boolean on) {
        if (on) {
            on();
        } else {
            off();
        }
    }

    public void on() {
        isOn = true;
        setColor(colorOn);
    }

    public void off() {
        isOn = false;
        setColor(colorOff);
    }

    public void pointChange(boolean on) {
        isPointOn = on;
        if (on) {
            //   {zh} 风格妆中会修改drawable缓存的填充色，此处使用单独的固定颜色的drawable       {en} Style makeup will modify the fill color of the drawable cache, here use a separate fixed color drawable
            vPoint.setBackgroundResource(R.drawable.dot_point_blue);
        } else {
            vPoint.setBackgroundResource(0);
        }
    }

    public boolean isOn() {
        return isOn;
    }

    public boolean isPointOn() {
        return isPointOn;
    }

    private void setColor(int color) {
        Drawable drawable = iv.getDrawable();
        if (drawable == null) return;
        DrawableCompat.setTint(drawable, color);
        iv.setImageDrawable(drawable);

        tvTitle.setTextColor(color);
        tvDesc.setTextColor(color);
    }

    public void setMarqueue(boolean flag) {
        if (tvTitle instanceof MarqueeTextView) {
            ((MarqueeTextView) tvTitle).setMarqueue(flag);
        }
    }

}
