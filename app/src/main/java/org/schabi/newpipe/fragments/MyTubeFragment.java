package org.schabi.newpipe.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.common.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;


import org.schabi.newpipe.App;
import org.schabi.newpipe.NewPipeDatabase;
import org.schabi.newpipe.R;
import org.schabi.newpipe.database.AppDatabase;
import org.schabi.newpipe.database.history.model.StreamHistoryEntity;
import org.schabi.newpipe.database.stream.model.StreamEntity;
import org.schabi.newpipe.database.subscription.SubscriptionEntity;
import org.schabi.newpipe.extractor.subscription.SubscriptionItem;
import org.schabi.newpipe.util.ImageDisplayConstants;
import org.schabi.newpipe.util.NavigationHelper;
import org.schabi.newpipe.util.Utils;
import org.schabi.newpipe.youtubelist.WaitingDialog;
import org.schabi.newpipe.youtubelist.YouTubeApiMannager;
import org.schabi.newpipe.youtubelist.YouTubePlayList;
import org.schabi.newpipe.youtubelist.YouTubePlaylistDao;
import org.schabi.newpipe.youtubelist.YouTubeVideo;
import org.schabi.newpipe.youtubelist.YouTubeVideoActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by liyanju on 2018/9/13.
 */

public class MyTubeFragment  extends Fragment {

    private static final String TAG = "MyTubeFragment";

    private RecyclerView tubeRecyclerView;
    private ArrayList<Object> tubeItems = new ArrayList<>();
    private MultiItemTypeAdapter<Object> itemTypeAdapter;

    private Context mContext;

    private WaitingDialog mWaitingDlg;

    private AppDatabase mDatabase;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mytube, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tubeRecyclerView = view.findViewById(R.id.mytube_recyclerview);
        tubeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tubeItems.clear();
        tubeItems.add(new TubeItem(TubeItem.LOCAL_VIDEO_TYPE, getString(R.string.tube_my_video),
                R.drawable.ic_video_label_6060_24dp));
        tubeItems.add(new TubeItem(TubeItem.YOUTUBE_VIDEO_TYPE, getString(R.string.tube_add_youtube),
                R.drawable.ic_library_add_6060_24dp));
        if (App.isGodMode()) {
            tubeItems.add(new TubeItem(TubeItem.DOWNLOAD_VIDEO_TYPE, getString(R.string.download),
                    R.drawable.ic_file_download_black_24dp));
        }

        itemTypeAdapter = new MultiItemTypeAdapter(getContext(), tubeItems);
        itemTypeAdapter.addItemViewDelegate(new LocalVideo());
        itemTypeAdapter.addItemViewDelegate(new TubeListItem());
        itemTypeAdapter.addItemViewDelegate(new YouTubePlaylistItem());
        itemTypeAdapter.addItemViewDelegate(new PlaylistTitleItem());
        if (App.isGodMode()) {
            itemTypeAdapter.addItemViewDelegate(new TubeListItem());
        }
        tubeRecyclerView.setAdapter(itemTypeAdapter);

        mWaitingDlg = new WaitingDialog(getActivity());

        mDatabase = NewPipeDatabase.getInstance(mContext);

        Utils.sSingleExecutor.execute(()->{
            List<YouTubePlayList> list = mDatabase.playlistYouTubeDAO().getAllYouTubePlaylist();

            Utils.runUIThead(()->{
                if (list.size() > 0) {
                    tubeItems.add(2, new TubeItem(TubeItem.PLAY_LIST_TITLE_TYPE, "", 0));
                    tubeItems.addAll(list);
                }

                itemTypeAdapter.notifyDataSetChanged();
            });
        });

    }

    YouTubeApiMannager.YouTubeRequest<YouTubePlayList> requestPlaylist = new YouTubeApiMannager.YouTubeRequest<YouTubePlayList>() {
        @Override
        public void onError(int errorCode, String string) {
            if (getContext() == null || !isAdded()) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (((Activity) getContext()).isFinishing() || ((Activity) getContext()).isDestroyed()) {
                    return;
                }
            } else {
                if (((Activity) getContext()).isFinishing()) {
                    return;
                }
            }

            Utils.runUIThead(()->{
                if (mWaitingDlg.isShowing()) {
                    mWaitingDlg.dismiss();
                }
            });

            int textRes;
            switch (errorCode) {
                case YouTubeApiMannager.RESULT_LIST_EMPTY:
                    textRes = R.string.txt_youtube_return_empty;
                    break;
                case YouTubeApiMannager.RESULT_NET_UNAVAILABLE:
                    textRes = R.string.txt_youtube_return_net;
                    break;
                case YouTubeApiMannager.RESULT_REQUEST_FAIL:
                    textRes = R.string.txt_youtube_return_error;
                    break;
                case YouTubeApiMannager.RESULT_GOOGLEPLAY_UNAVAILABLE:
                    textRes = R.string.txt_youtube_return_google;
                    break;
                case YouTubeApiMannager.RESULT_UNSELECTED:
                    return;
                case YouTubeApiMannager.RESULT_LIST_ITEM_EMPTY:
                    return;
                default:
                    textRes = R.string.txt_youtube_return_error;
                    break;
            }

            Utils.runUIThead(()->{
                Toast.makeText(App.sContext, textRes, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onSuccess(List<YouTubePlayList> list) {
            LogUtil.v(TAG, "YouTubePlayList onSuccess ");

            if (getContext() == null || !isAdded()) {
                return;
            }

            if (mWaitingDlg.isShowing()) {
                mWaitingDlg.dismiss();
            }


            addPlaylist(list);

            if (tubeItems.size() > 2) {
                Object object = tubeItems.get(2);
                if (!(object instanceof TubeItem && ((TubeItem) object).type == TubeItem.PLAY_LIST_TITLE_TYPE)) {
                    tubeItems.add(2, new TubeItem(TubeItem.PLAY_LIST_TITLE_TYPE, "", 0));
                }
            }
            Utils.sSingleExecutor.execute(()->{
                mDatabase.playlistYouTubeDAO().insertAll(list);
            });

            if (itemTypeAdapter != null) {
                itemTypeAdapter.notifyDataSetChanged();
            }
        }

        private void addPlaylist(List<YouTubePlayList> list) {
            Iterator iterator = tubeItems.iterator();
            while (iterator.hasNext()) {
                Object object = iterator.next();
                if (object instanceof YouTubePlayList) {
                    iterator.remove();
                    Utils.sSingleExecutor.execute(()->{
                        mDatabase.playlistYouTubeDAO().delete((YouTubePlayList)object);
                    });
                }
            }

            tubeItems.addAll(list);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        YouTubeApiMannager.get().onActivityResult(requestCode, resultCode, data);
    }

    private class PlaylistTitleItem implements ItemViewDelegate<Object> {
        @Override
        public int getItemViewLayoutId() {
            return R.layout.play_list_title_item;
        }

        @Override
        public boolean isForViewType(Object item, int position) {
            return item instanceof TubeItem && ((TubeItem) item).type == TubeItem.PLAY_LIST_TITLE_TYPE;
        }

        @Override
        public void convert(ViewHolder holder, Object o, int position) {

        }
    }

    private class YouTubePlaylistItem implements ItemViewDelegate<Object> {
        @Override
        public int getItemViewLayoutId() {
            return R.layout.youtube_playlist_item;
        }

        @Override
        public boolean isForViewType(Object item, int position) {
            return item instanceof YouTubePlayList;
        }

        @Override
        public void convert(ViewHolder holder, Object o, int position) {
            YouTubePlayList youTubePlayList = (YouTubePlayList)o;
            ImageView thumbnailIV = holder.getView(R.id.itemThumbnailView);
            ImageLoader.getInstance().displayImage(youTubePlayList.iconUrl,
                    thumbnailIV,ImageDisplayConstants.DISPLAY_THUMBNAIL_OPTIONS);

            TextView titleTV = holder.getView(R.id.itemVideoTitleView);
            titleTV.setText(youTubePlayList.name);

            TextView playcountTV = holder.getView(R.id.playcount_tv);
            playcountTV.setText(String.format(App.sContext.getString(R.string.dialog_youtube_video_count,
                    youTubePlayList.listCount)));

            holder.setOnClickListener(R.id.itemRoot, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    YouTubeVideoActivity.launch(getActivity(), youTubePlayList.playlistId,
                            youTubePlayList.name);
                }
            });
        }
    }

    private class TubeListItem implements ItemViewDelegate<Object> {
        @Override
        public int getItemViewLayoutId() {
            return R.layout.tube_local_video_item;
        }

        @Override
        public boolean isForViewType(Object item, int position) {
            return item instanceof TubeItem && (((TubeItem)item).type == TubeItem.YOUTUBE_VIDEO_TYPE
                    || ((TubeItem)item).type == TubeItem.DOWNLOAD_VIDEO_TYPE);
        }

        @Override
        public void convert(ViewHolder holder, Object object, int position) {
            TubeItem tubeItem = (TubeItem)object;
            ImageView iconIV = holder.getView(R.id.img_1);
            iconIV.setImageResource(tubeItem.resIcon);

            TextView titleTV = holder.getView(R.id.title_tv);
            titleTV.setText(tubeItem.title);

            TextView descTV = holder.getView(R.id.txt_music);
            descTV.setVisibility(View.GONE);

            holder.setOnClickListener(R.id.rl_my_music, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tubeItem.type == TubeItem.DOWNLOAD_VIDEO_TYPE) {
                        NavigationHelper.openDownloads(getActivity());
                    } else if (tubeItem.type == TubeItem.YOUTUBE_VIDEO_TYPE){

                        if (mWaitingDlg.isShowing()) {
                            mWaitingDlg.dismiss();
                        }
                        mWaitingDlg.show();

                        YouTubeApiMannager.get().setCallBack(requestPlaylist)
                                .setTargetActivity(getActivity())
                                .createYouTubeApi(requestPlaylist)
                                .requestYoutubePlayLists();
                    }
                }
            });

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        YouTubeApiMannager.get().setCallBack(null);
    }

    private class LocalVideo implements ItemViewDelegate<Object> {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.tube_local_video_item;
        }

        @Override
        public boolean isForViewType(Object item, int position) {
            return item instanceof TubeItem && (((TubeItem)item).type == TubeItem.LOCAL_VIDEO_TYPE);
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void convert(ViewHolder holder, Object object, int position) {
            TubeItem tubeItem = (TubeItem)object;
            ImageView iconIV = holder.getView(R.id.img_1);
            iconIV.setImageResource(tubeItem.resIcon);

            TextView titleTV = holder.getView(R.id.title_tv);
            titleTV.setText(tubeItem.title);

            TextView descTV = holder.getView(R.id.txt_music);
            descTV.setVisibility(View.VISIBLE);
            descTV.setText(String.format(getString(R.string.video_count, LocalVideoFragment.getLocalVideoCount(mContext))));

            holder.setOnClickListener(R.id.rl_my_music, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LocalVideoActivity.launch(getActivity());
                }
            });
        }
    }

    private class TubeItem {

        public TubeItem(int type, String title, int resIcon) {
            this.type = type;
            this.title = title;
            this.resIcon = resIcon;
        }

        public static final int LOCAL_VIDEO_TYPE = 1;
        public static final int YOUTUBE_VIDEO_TYPE = 2;
        public static final int DOWNLOAD_VIDEO_TYPE = 3;
        public static final int PLAY_LIST_TITLE_TYPE = 4;

        public int type = LOCAL_VIDEO_TYPE;

        public String title;

        public int resIcon;

    }
}

