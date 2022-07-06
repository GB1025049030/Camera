package com.freeme.camera.ui;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.android.camera.debug.Log;
import com.android.camera2.R;
import java.util.ArrayList;


public class ModeScrollAdapter implements AutoCenterHorizontalScrollView.HAdapter {

    ArrayList<String> names = new ArrayList<>();

    private Context context;

    private final static Log.Tag TAG = new Log.Tag("ModeScrollAdapter");

    public ModeScrollAdapter(Context context, ArrayList<String> names) {
        this.names = names;
        this.context = context;
    }

    @Override
    public int getCount() {
        return names.size();
    }


    @Override
    public RecyclerView.ViewHolder getItemView(int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_mode_scroll, null, false);
        HViewHolder hViewHolder = new HViewHolder(v);
        hViewHolder.textView.setText(names.get(i));
        return hViewHolder;
    }

    @Override
    public void onSelectStateChanged(RecyclerView.ViewHolder viewHolder, int position, boolean isSelected) {
        // you can do something for item
        if (isSelected) {
            ((HViewHolder) viewHolder).textView.setTextColor(context.getResources().getColor(R.color.dream_yellow));
        } else {
            ((HViewHolder) viewHolder).textView.setTextColor(context.getResources().getColor(R.color.freeme_text_color_primary));
        }
    }
    public void inflateAdaptLayout() {
        LayoutInflater.from(context).inflate(R.layout.item_exposure_time, null, false);
    }
    public static class HViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView;
        public HViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.mode_scroll_text);
        }
    }
}