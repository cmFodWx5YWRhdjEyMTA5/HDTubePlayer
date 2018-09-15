package org.schabi.newpipe.util;

import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.schabi.newpipe.App;
import org.schabi.newpipe.R;

public class ImageDisplayConstants {
    private static final int BITMAP_FADE_IN_DURATION_MILLIS = 250;

    /**
     * Base display options
     */
    private static final DisplayImageOptions BASE_DISPLAY_IMAGE_OPTIONS =
            new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .resetViewBeforeLoading(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .displayer(new FadeInBitmapDisplayer(BITMAP_FADE_IN_DURATION_MILLIS))
                    .build();

    /*//////////////////////////////////////////////////////////////////////////
    // DisplayImageOptions default configurations
    //////////////////////////////////////////////////////////////////////////*/

    public static final DisplayImageOptions DISPLAY_AVATAR_OPTIONS =
            new DisplayImageOptions.Builder()
                    .cloneFrom(BASE_DISPLAY_IMAGE_OPTIONS)
                    .showImageOnLoading(R.drawable.buddy)
                    .showImageForEmptyUri(R.drawable.buddy)
                    .showImageOnFail(R.drawable.buddy)
                    .build();

    public static final DisplayImageOptions DISPLAY_THUMBNAIL_OPTIONS =
            new DisplayImageOptions.Builder()
                    .cloneFrom(BASE_DISPLAY_IMAGE_OPTIONS)
                    .showImageOnLoading(ContextCompat.getDrawable(App.sConetxt, R.drawable.default_dummy_thumbnail))
                    .showImageForEmptyUri(ContextCompat.getDrawable(App.sConetxt, R.drawable.default_dummy_thumbnail))
                    .showImageOnFail(ContextCompat.getDrawable(App.sConetxt, R.drawable.default_dummy_thumbnail))
                    .build();
    public static final DisplayImageOptions VIDEO_DISPLAY_THUMBNAIL_OPTIONS =
            new DisplayImageOptions.Builder()
                    .cloneFrom(BASE_DISPLAY_IMAGE_OPTIONS)
                    .build();

    public static final DisplayImageOptions DISPLAY_BANNER_OPTIONS =
            new DisplayImageOptions.Builder()
                    .cloneFrom(BASE_DISPLAY_IMAGE_OPTIONS)
                    .showImageForEmptyUri(R.drawable.channel_banner)
                    .showImageOnFail(R.drawable.channel_banner)
                    .build();

    public static final DisplayImageOptions DISPLAY_PLAYLIST_OPTIONS =
            new DisplayImageOptions.Builder()
                    .cloneFrom(BASE_DISPLAY_IMAGE_OPTIONS)
                    .showImageOnLoading(ContextCompat.getDrawable(App.sConetxt, R.drawable.default_dummy_thumbnail))
                    .showImageForEmptyUri(ContextCompat.getDrawable(App.sConetxt, R.drawable.default_dummy_thumbnail))
                    .showImageOnFail(ContextCompat.getDrawable(App.sConetxt, R.drawable.default_dummy_thumbnail))
                    .build();
}
