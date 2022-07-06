/*
 * Copyright (C) 2010,2012 Thundersoft Corporation
 * All rights Reserved
 */

package com.android.camera;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.android.camera2.R;

public class RefocusProgressDialog extends Dialog {

    public RefocusProgressDialog(Context context) {
        this(context, null);
    }

    public RefocusProgressDialog(Context context, String strMessage) {
        this(context, R.style.RefocusProgressDialog, strMessage);
    }

    public RefocusProgressDialog(Context context, int theme, String strMessage) {
        super(context, theme);
        this.setContentView(R.layout.refocus_progress_dialog);
        setCancelable(false);
    }

}
