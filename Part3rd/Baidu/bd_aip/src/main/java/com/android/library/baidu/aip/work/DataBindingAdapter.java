package com.android.library.baidu.aip.work;

import android.content.Context;
import androidx.databinding.BindingAdapter;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import com.android.library.baidu.aip.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Optional;

public class DataBindingAdapter {
    @BindingAdapter("imageUrl")
    public static void setSrc(ImageView imageView, String url) {
        if (!TextUtils.isEmpty(url)) {
            imageView.setVisibility(View.VISIBLE);
            Context context = imageView.getContext();
            RequestOptions requestOptions = new RequestOptions().override(
                    (int) context.getResources().getDimension(R.dimen.iko_search_image_width),
                    (int) context.getResources().getDimension(R.dimen.iko_search_image_height));
            Glide.with(context).load(url)
                    .apply(requestOptions)
                    .into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    @BindingAdapter("imageUrl")
    public static void setSrc(ImageView imageView, byte[] data) {
        if (data == null) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length, null));
        }
    }

    @BindingAdapter("url")
    public static void setUrl(WebView webView, String url) {
        Optional.ofNullable(url).ifPresent(s -> webView.loadUrl(s));
    }
}
