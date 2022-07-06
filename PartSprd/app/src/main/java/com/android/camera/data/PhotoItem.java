/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.data;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.android.camera.Storage;
import com.android.camera.data.FilmstripItemAttributes.Attributes;
import com.android.camera.debug.Log;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.Size;
import com.android.camera2.R;
import com.google.common.base.Optional;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.annotation.Nonnull;

/**
 * Backing data for a single photo displayed in the filmstrip.
 */
/*
 * SPRD: Fix bug 535110, Photo voice record. @{
 * Original Android code :
public class PhotoItem extends FilmstripItemBase<FilmstripItemData> {
*/
public class PhotoItem extends FilmstripItemBase<PhotoItemData> {
/* @} */
    private static final Log.Tag TAG = new Log.Tag("PhotoItem");
    private static final int MAX_PEEK_BITMAP_PIXELS = 1600000; // 1.6 * 4 MBs.

    private static final FilmstripItemAttributes PHOTO_ITEM_ATTRIBUTES =
          new FilmstripItemAttributes.Builder()
              .with(Attributes.CAN_SHARE)
              .with(Attributes.CAN_EDIT)
              .with(Attributes.CAN_PUZZLE)//SPRD:fix bug 474674
              .with(Attributes.CAN_DELETE)
              .with(Attributes.CAN_SWIPE_AWAY)
              .with(Attributes.CAN_ZOOM_IN_PLACE)
              .with(Attributes.HAS_DETAILED_CAPTURE_INFO)
              .with(Attributes.IS_IMAGE)
              .build();

    private final PhotoItemFactory mPhotoItemFactory;

    private Optional<Bitmap> mSessionPlaceholderBitmap = Optional.absent();

    public PhotoItem(Context context, PhotoItemData data,
            PhotoItemFactory photoItemFactory) {
        super(context, data, PHOTO_ITEM_ATTRIBUTES);
        mPhotoItemFactory = photoItemFactory;
    }

    public PhotoItem(Context context, PhotoItemData data,
                     PhotoItemFactory photoItemFactory, FilmstripItemAttributes attributes) {
        super(context, data, attributes);
        mPhotoItemFactory = photoItemFactory;
    }

    /**
     * A bitmap that if present, is a high resolution bitmap from a temporary
     * session, that should be used as a placeholder in place of placeholder/
     * thumbnail loading.
     *
     * @param sessionPlaceholderBitmap a Bitmap to set as a placeholder
     */
    public void setSessionPlaceholderBitmap(Optional<Bitmap> sessionPlaceholderBitmap) {
        mSessionPlaceholderBitmap = sessionPlaceholderBitmap;
    }

    @Override
    public String toString() {
        return "PhotoItem: " + mData.toString();
    }

    @Override
    public boolean delete() {
        ContentResolver cr = mContext.getContentResolver();
        cr.delete(PhotoDataQuery.CONTENT_URI,
              MediaStore.Images.ImageColumns._ID + "=" + mData.getContentId(), null);
        return super.delete();
    }

    @Override
    public Optional<MediaDetails> getMediaDetails() {
        Optional<MediaDetails> optionalDetails = super.getMediaDetails();
        if (optionalDetails.isPresent()) {
            MediaDetails mediaDetails = optionalDetails.get();
            MediaDetails.extractExifInfo(mediaDetails, mData.getFilePath());
            mediaDetails.addDetail(MediaDetails.INDEX_ORIENTATION, mData.getOrientation());
        }
        return optionalDetails;
    }

    @Override
    public FilmstripItem refresh() {
        // TODO: Consider simply replacing the data inline
        return mPhotoItemFactory.get(mData.getUri());
    }

    @Override
    public View getView(Optional<View> optionalView, LocalFilmstripDataAdapter adapter,
          boolean isInProgress, VideoClickedCallback videoClickedCallback) {
        ImageView imageView;

        /*
         * SPRD:Fix bug 531747 java.lang.ClassCastException
         * Original Code @{
        if (optionalView.isPresent()) {
            imageView = (ImageView) optionalView.get();
         */
        if (optionalView.isPresent()) {
            if (optionalView.get() instanceof android.widget.ImageView) {
                imageView = (ImageView) optionalView.get();
            }
            else {
                Log.w(TAG, "getView called with a view that is not compatible with PhotoItem.");
                imageView = new ImageView(mContext);
                imageView.setTag(R.id.mediadata_tag_viewtype, getItemViewType().ordinal());
            }
            /* @} */
        } else {
            imageView = new ImageView(mContext);
            imageView.setTag(R.id.mediadata_tag_viewtype, getItemViewType().ordinal());
        }

        fillImageView(imageView);

        return imageView;
    }

    protected void fillImageView(final ImageView imageView) {

        // TODO consider having metadata have a "get description" string
        // or some other way of selecting rendering details based on metadata.
        int stringId = R.string.photo_date_content_description;
        if (getMetadata().isPanorama() ||
              getMetadata().isPanorama360()) {
            stringId = R.string.panorama_date_content_description;
        } else if (getMetadata().isUsePanoramaViewer()) {
            // assume it's a PhotoSphere
            stringId = R.string.photosphere_date_content_description;
        } else if (this.getMetadata().isHasRgbzData()) {
            stringId = R.string.refocus_date_content_description;
        }

        imageView.setContentDescription(mContext.getResources().getString(
              stringId,
              mDateFormatter.format(mData.getLastModifiedDate())));
    }

    @Override
    public void recycle(@Nonnull View view) {
        mSessionPlaceholderBitmap = Optional.absent();
    }

    @Override
    public FilmstripItemType getItemViewType() {
        return FilmstripItemType.PHOTO;
    }

    @Override
    public void renderTiny(@Nonnull View view) {
    }

    @Override
    public void renderThumbnail(@Nonnull View view) {
    }

    @Override
    public void renderFullRes(@Nonnull View view) {
    }

    @Override
    public void update(int burstNum) {
    }

    @Override
    public Optional<Bitmap> generateThumbnail(int boundingWidthPx, int boundingHeightPx) {
        FilmstripItemData data = getData();
        final Bitmap bitmap;

        if (getAttributes().isRendering()) {
            return Storage.getPlaceholderForSession(data.getUri());
        } else {

            FileInputStream stream;

            try {
                stream = new FileInputStream(data.getFilePath());
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found:" + data.getFilePath());
                return Optional.absent();
            }
            int width = data.getDimensions().getWidth();
            int height = data.getDimensions().getHeight();
            int orientation = data.getOrientation();

            Point dim = CameraUtil.resizeToFill(
                  width,
                  height,
                  orientation,
                  boundingWidthPx,
                  boundingHeightPx);

            // If the orientation is not vertical
            if (orientation % 180 != 0) {
                int dummy = dim.x;
                dim.x = dim.y;
                dim.y = dummy;
            }

            bitmap = FilmstripItemUtils
                  .loadImageThumbnailFromStream(
                        stream,
                        data.getDimensions().getWidth(),
                        data.getDimensions().getHeight(),
                        (int) (dim.x), (int) (dim.y),
                        data.getOrientation(), MAX_PEEK_BITMAP_PIXELS);

            try {//fix bug517384 remove sdcard, camera stop run
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {}
            return Optional.fromNullable(bitmap);
        }
    }

    /* SPRD: Fix bug 535110, Photo voice record. @{ */
    public String getVoice() {
        // TODO Auto-generated method stub
        return mData.getPhotoVoice();
    }
    /* @} */
}
