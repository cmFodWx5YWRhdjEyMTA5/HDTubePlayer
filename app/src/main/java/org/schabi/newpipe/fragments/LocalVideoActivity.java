package org.schabi.newpipe.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.schabi.newpipe.R;
import org.schabi.newpipe.util.Utils;

/**
 * Created by liyanju on 2018/9/15.
 */

public class LocalVideoActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_video_activity);

        Utils.compat(this, ContextCompat.getColor(this, R.color.color_cccccc));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.tube_my_video);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.local_video_frame,
                new LocalVideoFragment()).commitAllowingStateLoss();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void launch(Activity activity) {
        activity.startActivity(new Intent(activity, LocalVideoActivity.class));
        activity.overridePendingTransition(R.anim.slide_from_bottom, 0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_to_bottom);
    }
}
