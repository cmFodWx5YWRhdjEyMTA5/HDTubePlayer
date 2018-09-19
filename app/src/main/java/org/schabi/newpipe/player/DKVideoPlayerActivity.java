package org.schabi.newpipe.player;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.dueeeke.videocontroller.StandardVideoController;
import com.dueeeke.videoplayer.player.IjkVideoView;
import com.dueeeke.videoplayer.player.PlayerConfig;

import org.schabi.newpipe.R;
import org.schabi.newpipe.util.Utils;

/**
 * Created by liyanju on 2018/9/15.
 */

public class DKVideoPlayerActivity extends AppCompatActivity{

    private IjkVideoView ijkVideoView;

    public static void launch(Context context, String url, String title) {
        Intent intent = new Intent(context, DKVideoPlayerActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_player_layout);
        Utils.compat(this, ContextCompat.getColor(this, R.color.black));
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        String title = intent.getStringExtra("title");

        ijkVideoView = findViewById(R.id.player);
        StandardVideoController controller = new StandardVideoController(this);
        ijkVideoView.setPlayerConfig(new PlayerConfig.Builder()
                .autoRotate()
                .setCustomMediaPlayer(new ExoMediaPlayer(this))
                .build());
        ijkVideoView.setTitle(title);
        ijkVideoView.setUrl(url);
        ijkVideoView.setVideoController(controller);
        ijkVideoView.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        ijkVideoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ijkVideoView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ijkVideoView.release();
    }

    @Override
    public void onBackPressed() {
        if (!ijkVideoView.onBackPressed()){
            super.onBackPressed();
        }
    }
}
