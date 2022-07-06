package com.freeme.camera.widget;

import android.util.Log;
import android.view.View;

import com.freeme.camera.ui.PanelLayout;

import java.util.ArrayList;
import java.util.List;

public class PanelManager {
    private static final String TOP = "Top";
    private static final String SIDE = "Side";

    protected List<PanelLayout> mPanels;
    private PanelLayout mTopPanel;
    private PanelLayout mSidePanel;

    public PanelManager() {
        this.mPanels = new ArrayList<>(4);
    }

    public void addPanel(PanelLayout panel) {
        if (panel != null) mPanels.add(panel);
    }

    public List<PanelLayout> getPanels() {
        return mPanels;
    }

    public boolean isContain(Integer id){
        boolean isContain = false;
        for (PanelLayout panel : mPanels) {
            if (isContain = panel.isContain(id)) break;
        }
        return isContain;
    }

    public View getPanelButton(int id) {
        View button;
        for (PanelLayout panel : mPanels) {
            button = panel.getPanelButton(id);
            if (button != null) return button;
        }
        return null;
    }

    public void showButton(Integer id) {
        for (PanelLayout panel : mPanels) {
            if (panel.isContain(id)) {
                panel.showButton(id);
                break;
            }
        }
    }

    public void hideButton(Integer id) {
        for (PanelLayout panel : mPanels) {
            if (panel.isContain(id)) {
                panel.hideButton(id);
                break;
            }
        }
    }

    private PanelLayout getSidePanel() {
        for (PanelLayout panel : mPanels) {
            if (SIDE.equals(String.valueOf(panel.getTag()))) {
                return panel;
            }
        }
        return null;
    }

    public void updateSidePanelVisibility() {
        if (mSidePanel == null) {
            mSidePanel = getSidePanel();
        }
        if (mSidePanel != null) {
            mSidePanel.setVisibility(mSidePanel.isNeedHide() ? View.GONE : View.VISIBLE);
        }
    }

    public void setFirstSideButtonState(int state) {
        if (mSidePanel == null) {
            mSidePanel = getSidePanel();
        }
        if (mSidePanel != null) {
            mSidePanel.setFirstChildState(state);
        }
    }

    public boolean isSidePanelShow() {
        if (mSidePanel == null) {
            mSidePanel = getSidePanel();
        }
        if (mSidePanel != null) {
            return mSidePanel.getVisibility() == View.VISIBLE;
        }
        return false;
    }
}

