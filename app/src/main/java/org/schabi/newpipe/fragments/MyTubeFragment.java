package org.schabi.newpipe.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;


import org.schabi.newpipe.R;

import java.util.ArrayList;

/**
 * Created by liyanju on 2018/9/13.
 */

public class MyTubeFragment  extends Fragment {

    private RecyclerView tubeRecyclerView;
    private ArrayList<TubeItem> tubeItems = new ArrayList<>();
    private MultiItemTypeAdapter<TubeItem> itemTypeAdapter;

    private Context mContext;

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

        itemTypeAdapter = new MultiItemTypeAdapter(getContext(), tubeItems);
        itemTypeAdapter.addItemViewDelegate(new LocalVideo());
        itemTypeAdapter.addItemViewDelegate(new YouTubeVideo());
        tubeRecyclerView.setAdapter(itemTypeAdapter);

    }

    private class YouTubeVideo implements ItemViewDelegate<TubeItem> {
        @Override
        public int getItemViewLayoutId() {
            return R.layout.tube_local_video_item;
        }

        @Override
        public boolean isForViewType(TubeItem item, int position) {
            return item.type == TubeItem.YOUTUBE_VIDEO_TYPE;
        }

        @Override
        public void convert(ViewHolder holder, TubeItem tubeItem, int position) {
            ImageView iconIV = holder.getView(R.id.img_1);
            iconIV.setImageResource(tubeItem.resIcon);

            TextView titleTV = holder.getView(R.id.title_tv);
            titleTV.setText(tubeItem.title);

            TextView descTV = holder.getView(R.id.txt_music);
            descTV.setVisibility(View.GONE);

            holder.setOnClickListener(R.id.rl_my_music, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }
    }

    private class LocalVideo implements ItemViewDelegate<TubeItem> {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.tube_local_video_item;
        }

        @Override
        public boolean isForViewType(TubeItem item, int position) {
            return item.type == TubeItem.LOCAL_VIDEO_TYPE;
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void convert(ViewHolder holder, TubeItem tubeItem, int position) {
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

        public int type = LOCAL_VIDEO_TYPE;

        public String title;

        public int resIcon;

    }
}

