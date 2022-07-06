package com.freeme.camera.widget.selector;

import android.content.Context;

import com.android.camera2.R;
import com.freeme.camera.widget.selector.interfaces.InterfaceSelectorContainer;
import com.freeme.camera.widget.selector.interfaces.InterfaceSelectorScroller;
import com.freeme.camera.widget.selector.interfaces.InterfaceSelectorSwitcher;

public class MakeUpControllerImpl extends SelectorControllerWithSwitcher {

    public MakeUpControllerImpl(Context context, InterfaceSelectorScroller scroller,
                                InterfaceSelectorContainer container,
                                InterfaceSelectorSwitcher switcher,
                                CallBack callBack,
                                InitializeCallBack initializeCallBack){
        super(context,scroller,container, switcher, callBack, initializeCallBack);
    }

    @Override
    protected int getSwitcherID() {
        return R.array.switcher_makeup;
    }

}