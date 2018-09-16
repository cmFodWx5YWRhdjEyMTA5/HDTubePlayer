package org.schabi.newpipe.info_list;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dueeeke.videocontroller.StandardVideoController;
import com.dueeeke.videoplayer.player.BaseIjkVideoView;
import com.dueeeke.videoplayer.player.IjkVideoView;
import com.dueeeke.videoplayer.player.PlayerConfig;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.schabi.newpipe.App;
import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.fetchurl.ParseStreamMetaData;
import org.schabi.newpipe.fetchurl.StreamMetaData;
import org.schabi.newpipe.player.ExoMediaPlayer;
import org.schabi.newpipe.player.HomeListItemVideoView;
import org.schabi.newpipe.player.PlayLogicEventCallBack;
import org.schabi.newpipe.util.ACache;
import org.schabi.newpipe.util.FallbackViewHolder;
import org.schabi.newpipe.util.ImageDisplayConstants;
import org.schabi.newpipe.util.Localization;


/**
 * Created by liyanju on 2018/9/16.
 */

public class HomeInfoListAdapter extends InfoListAdapter {

    private Context mContext;

    public HomeInfoListAdapter(Activity a) {
        super(a);
        mContext = a;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        switch (type) {
            case HEADER_TYPE:
                return new HFHolder(header);
            case FOOTER_TYPE:
                return new HFHolder(footer);
            case STREAM_HOLDER_TYPE:
                return new HomeStreamHolder(LayoutInflater.from(mContext).inflate(R.layout.home_info_list_item,
                        parent, false));
            default:
                return new FallbackViewHolder(new View(parent.getContext()));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HFHolder && position == 0 && header != null) {
            ((HFHolder) holder).view = header;
        } else if (holder instanceof HFHolder && position == sizeConsideringHeaderOffset() && footer != null && showFooter) {
            ((HFHolder) holder).view = footer;
        } else if (holder instanceof HomeStreamHolder) {

            final InfoItem infoItem = infoItemList.get(position);
            if (!(infoItem instanceof StreamInfoItem)) return;

            final StreamInfoItem item = (StreamInfoItem) infoItem;

            ImageView thumbIV = ((HomeStreamHolder) holder)
                    .controller.getThumb();
            thumbIV.setImageResource(R.drawable.default_dummy_thumbnail);
            ImageLoader.getInstance().displayImage(item.getThumbnailUrl(), thumbIV,
                    ImageDisplayConstants.DISPLAY_THUMBNAIL_OPTIONS);

            final HomeStreamHolder homeStreamHolder = (HomeStreamHolder) holder;
            homeStreamHolder.ijkVideoView.setPlayerConfig(homeStreamHolder.mPlayerConfig);
            homeStreamHolder.ijkVideoView.setTitle(item.getName());

            if (item.getDuration() > 0) {
                homeStreamHolder.controller.setDuration(Localization.getDurationString(item.getDuration()));
            } else if (item.getStreamType() == StreamType.LIVE_STREAM) {
                homeStreamHolder.controller.setDuration(mContext.getString(R.string.duration_live));
                homeStreamHolder.controller.setLive();
            } else {
                homeStreamHolder.controller.setDuration("");
            }

            homeStreamHolder.ijkVideoView.setPlayLogicEventCallBack(new PlayLogicEventCallBack() {
                @Override
                public String handleGetPlayUrl(boolean forceload) {
                    if (!forceload) {
                        String playurl = ACache.get(App.sConetxt).getAsString(item.getUrl());
                        Log.v("home", " handleGetPlayUrl playurl " + playurl);
                        if (!TextUtils.isEmpty(playurl)) {
                            return playurl;
                        }
                    }

                    StreamMetaData streamMetaData = new ParseStreamMetaData(item.getUrl()).getStreamMetaDataList()
                            .getDesiredStream();
                    if (!TextUtils.isEmpty(streamMetaData.getUri().toString())) {
                        ACache.get(App.sConetxt).put(item.getUrl(), streamMetaData.getUri().toString(),
                                60 * 5);
                    }
                    Log.v("home", " handleGetPlayUrl playurl22 " + streamMetaData.getUri().toString());
                    return streamMetaData.getUri().toString();
                }
            });
            homeStreamHolder.ijkVideoView.setVideoController(homeStreamHolder.controller);
            homeStreamHolder.title.setText(item.getName());
        }
    }

    private ExoMediaPlayer player = new ExoMediaPlayer(App.sConetxt);

    public class HomeStreamHolder extends RecyclerView.ViewHolder {

        private HomeListItemVideoView ijkVideoView;
        private StandardVideoController controller;
        private TextView title;
        private PlayerConfig mPlayerConfig;

        public HomeStreamHolder(View itemView) {
            super(itemView);
            ijkVideoView = itemView.findViewById(R.id.video_player);
            int widthPixels = mContext.getResources().getDisplayMetrics().widthPixels;
            ijkVideoView.setLayoutParams(new LinearLayout.LayoutParams(widthPixels, widthPixels * 9 / 16 + 1));
            controller = new StandardVideoController(mContext);
            title = itemView.findViewById(R.id.tv_title);
            mPlayerConfig = new PlayerConfig.Builder()
                    .enableCache()
                    .setCustomMediaPlayer(player)
                    .autoRotate()
                    .addToPlayerManager()//required
                    .build();
        }
    }
}
