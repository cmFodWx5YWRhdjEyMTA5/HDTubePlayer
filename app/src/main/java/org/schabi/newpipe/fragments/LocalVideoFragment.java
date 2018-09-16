package org.schabi.newpipe.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.stetho.common.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.schabi.newpipe.App;
import org.schabi.newpipe.R;
import org.schabi.newpipe.player.LocalVideoPlayerActivity;
import org.schabi.newpipe.util.ImageDisplayConstants;
import org.schabi.newpipe.util.Localization;

import java.io.File;

/**
 * Created by liyanju on 2018/9/11.
 */

public class LocalVideoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private LocalVideoCursorAdapter localVideoCursorAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local_video, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = view.findViewById(R.id.local_listview);
        localVideoCursorAdapter = new LocalVideoCursorAdapter(getContext(), null, false);
        listView.setAdapter(localVideoCursorAdapter);
        getActivity().getSupportLoaderManager().initLoader(1, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Uri mImageUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String proj[] = { MediaStore.Video.Thumbnails._ID
                , MediaStore.Video.Thumbnails.DATA
                ,MediaStore.Video.Media.DURATION
                ,MediaStore.Video.Media.SIZE
                ,MediaStore.Video.Media.DISPLAY_NAME
                ,MediaStore.Video.Media.DATA
                ,MediaStore.Video.Media.DATE_MODIFIED};
        return new CursorLoader(App.sConetxt, mImageUri, proj, MediaStore.Video.Media.MIME_TYPE + "=?",
                new String[]{"video/mp4"}, MediaStore.Video.Media.DATE_MODIFIED+" desc");
    }

    public static int getLocalVideoCount(Context context) {
        Uri mImageUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String proj[] = { MediaStore.Video.Thumbnails._ID
                , MediaStore.Video.Thumbnails.DATA
                ,MediaStore.Video.Media.DURATION
                ,MediaStore.Video.Media.SIZE
                ,MediaStore.Video.Media.DISPLAY_NAME
                ,MediaStore.Video.Media.DATE_MODIFIED};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(mImageUri, proj, MediaStore.Video.Media.MIME_TYPE + "=?",
                    new String[]{"video/mp4"}, MediaStore.Video.Media.DATE_MODIFIED + " desc");
            if (cursor != null) {
                return cursor.getCount();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        localVideoCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    private class LocalVideoCursorAdapter extends CursorAdapter {

        public LocalVideoCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_stream_item, null);
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            ImageView thumbnailIV = view.findViewById(R.id.itemThumbnailView);
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
            Uri uri = Uri.fromFile(new File(path));
            ImageLoader.getInstance().displayImage(uri.toString(), thumbnailIV,
                    ImageDisplayConstants.DISPLAY_THUMBNAIL_OPTIONS);

            TextView durationTV = view.findViewById(R.id.itemDurationView);
            long duraton = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
            durationTV.setText(Localization.timeParse(duraton));

            TextView titleTV = view.findViewById(R.id.itemVideoTitleView);
            final String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
            titleTV.setText(name);

            TextView sizeTV = view.findViewById(R.id.itemUploaderView);
            long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
            sizeTV.setText(Localization.FormetFileSize(size));

            TextView dateTV = view.findViewById(R.id.itemAdditionalDetails);
            long time = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED));
            dateTV.setText(Localization.ms2DateOnlyDay(time));

            final String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtil.v("localvideo", "path:: " + filePath);
                    LocalVideoPlayerActivity.launch(App.sConetxt, path, name);
                }
            });
        }
    }
}
