package org.schabi.newpipe.youtubelist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.stetho.common.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import org.schabi.newpipe.App;
import org.schabi.newpipe.NewPipeDatabase;
import org.schabi.newpipe.R;
import org.schabi.newpipe.database.AppDatabase;
import org.schabi.newpipe.util.ImageDisplayConstants;
import org.schabi.newpipe.util.NavigationHelper;
import org.schabi.newpipe.util.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by liyanju on 2018/9/23.
 */

public class YouTubeVideoActivity extends AppCompatActivity{

    private RecyclerView recyclerView;

    private ArrayList<YouTubeVideo> mData = new ArrayList<>();

    public static void launch(Activity activity, String playlistid, String title) {
        Intent intent = new Intent(activity, YouTubeVideoActivity.class);
        intent.putExtra("playlistid", playlistid);
        intent.putExtra("title", title);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_from_bottom, 0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_to_bottom);
    }

    private CommonAdapter<YouTubeVideo> commonAdapter;

    YouTubeApiMannager.YouTubeRequest<YouTubeVideo> requestVideo = new YouTubeApiMannager.YouTubeRequest<YouTubeVideo>() {
        @Override
        public void onError(int errorCode, String string) {
            Utils.runUIThead(()->{
                Toast.makeText(App.sContext, string, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);

                if (mData.size() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onSuccess(List<YouTubeVideo> list) {
            LogUtil.v("video", "YouTubeVideo onSuccess ");
            if (list != null && list.size() > 0) {
                if (mData.size() == 0 && list.size() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                }

                mData.clear();
                mData.addAll(list);

                commonAdapter.notifyDataSetChanged();
            }

            progressBar.setVisibility(View.GONE);

            Utils.sSingleExecutor.execute(()->{
                for (YouTubeVideo youTubeVideo : list) {
                    if (mDatabase.youTubeVideoDAO()
                            .getYoutubeVideoByVid(youTubeVideo.vid) == null) {
                        youTubeVideo.playlistid = playlistID;
                        mDatabase.youTubeVideoDAO().insert(youTubeVideo);
                    }
                }
            });

        }
    };

    private String playlistID;
    private String title;

    private AppDatabase mDatabase;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("playlistid", playlistID);
        outState.putString("title", title);
    }

    private CommonAdapter<YouTubeVideo> getCommonAdapter(){
        commonAdapter = new CommonAdapter<YouTubeVideo>(this, R.layout.youtube_playlist_item, mData) {
            @Override
            protected void convert(ViewHolder holder, YouTubeVideo youTubeVideo, int position) {
                holder.getView(R.id.playcount_tv).setVisibility(View.GONE);

                TextView titleTV = holder.getView(R.id.itemVideoTitleView);
                titleTV.setText(youTubeVideo.title);

                ImageView thumbnailIV = holder.getView(R.id.itemThumbnailView);
                ImageLoader.getInstance().displayImage(youTubeVideo.coverUrl,
                        thumbnailIV, ImageDisplayConstants.DISPLAY_THUMBNAIL_OPTIONS);

                holder.setOnClickListener(R.id.itemRoot, new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        NavigationHelper.openVideoDetailFragment(getSupportFragmentManager(),
                                "https://www.youtube.com/watch?v="+youTubeVideo.vid, youTubeVideo.title);
                    }
                });
            }
        };
        return commonAdapter;
    }

    private ProgressBar progressBar;

    private View emptyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_video_layout);

        Utils.compat(this, ContextCompat.getColor(this, R.color.color_cccccc));

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplication()));

        recyclerView.setAdapter(getCommonAdapter());

        if (savedInstanceState == null) {
            if (getIntent() != null) {
                playlistID = getIntent().getStringExtra("playlistid");
                title = getIntent().getStringExtra("title");
            }
        } else {
            playlistID = savedInstanceState.getString("playlistid");
            title = savedInstanceState.getString("title");
        }

        Log.v("MAIN", "playlistID :: "+ playlistID);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(v->{
            finish();
        });

        progressBar = findViewById(R.id.loading_progress_bar);

        emptyView = findViewById(R.id.empty_state_view);

        mDatabase = NewPipeDatabase.getInstance(getApplication());

        Utils.sSingleExecutor.execute(()->{
            List<YouTubeVideo> list = mDatabase.youTubeVideoDAO().getYoutubeVideoByid(playlistID);
            Utils.runUIThead(()->{
                if (list != null && list.size() > 0) {
                    mData.clear();
                    mData.addAll(list);

                    commonAdapter.notifyDataSetChanged();

                    progressBar.setVisibility(View.GONE);
                }
            });
        });

        if (Utils.isNetWorkConnected()) {
            progressBar.setVisibility(View.VISIBLE);
            YouTubeApiMannager.get().setCallBack(requestVideo).setTargetActivity(this)
                    .createYouTubeApi(requestVideo).requestYoutubePlayListItems(playlistID,
                    YouTubeApiMannager.get().getSelectAccountName());
        }
    }
}
