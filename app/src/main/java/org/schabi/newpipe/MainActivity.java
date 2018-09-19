/*
 * Created by Christian Schabesberger on 02.08.16.
 * <p>
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * DownloadActivity.java is part of NewPipe.
 * <p>
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.schabi.newpipe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.dueeeke.videoplayer.player.VideoViewManager;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.fragments.BackPressable;
import org.schabi.newpipe.fragments.IMainActivity;
import org.schabi.newpipe.fragments.IMainFragment;
import org.schabi.newpipe.fragments.detail.VideoDetailFragment;
import org.schabi.newpipe.fragments.list.search.SearchFragment;
import org.schabi.newpipe.report.ErrorActivity;
import org.schabi.newpipe.router.Router;
import org.schabi.newpipe.util.BottomNavigationViewHelper;
import org.schabi.newpipe.util.Constants;
import org.schabi.newpipe.util.NavigationHelper;
import org.schabi.newpipe.util.PermissionHelper;
import org.schabi.newpipe.util.ServiceHelper;
import org.schabi.newpipe.util.StateSaver;
import org.schabi.newpipe.util.ThemeHelper;
import org.schabi.newpipe.util.Utils;

public class MainActivity extends AppCompatActivity implements IMainActivity {
    private static final String TAG = "MainActivity";
    public static final boolean DEBUG = !BuildConfig.BUILD_TYPE.equals("release");

    private BottomNavigationView mBNavigation;
    /*//////////////////////////////////////////////////////////////////////////
    // Activity's LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");

        ThemeHelper.setTheme(this, ServiceHelper.getSelectedServiceId(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.compat(this, ContextCompat.getColor(this, R.color.color_cccccc));

        if (getSupportFragmentManager() != null && getSupportFragmentManager().getBackStackEntryCount() == 0) {
            initFragments();
        }

        setSupportActionBar(findViewById(R.id.toolbar));

        Router.getInstance().register(this);

        mBNavigation = findViewById(R.id.main_navigation);
        BottomNavigationViewHelper.disableShiftMode(mBNavigation);

        mBNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                try {
                    Router.getInstance().getReceiver(IMainFragment.class).setCurrentItem(item.getOrder());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                item.setChecked(true);
                return false;
            }
        });

        appBarLayout = findViewById(R.id.toolbar_container);

        if (App.sPreferences.getBoolean("isTrances", true)) {
            Utils.runUIThreadDelay(new Runnable() {
                @Override
                public void run() {
                    App.sPreferences.edit().putBoolean("isTrances", false).apply();
                }
            }, 500);
        }
    }

    @Override
    public void setToolbarExpanded() {
        if (appBarLayout != null) {
            appBarLayout.setExpanded(true, true);
        }
    }

    private AppBarLayout appBarLayout;

    @Override
    public void setSelectedItemId(int position) {
        Log.v(TAG, "setSelectedItemId mBNavigation " + mBNavigation);
        if (mBNavigation != null) {
            int menuId = mBNavigation.getMenu().getItem(position).getItemId();
            Log.v(TAG, "setSelectedItemId menuId " + menuId);
            mBNavigation.setSelectedItemId(menuId);
            mBNavigation.getMenu().getItem(position).setChecked(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            StateSaver.clearStateFiles();
        }
        Router.getInstance().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(Constants.KEY_THEME_CHANGE, false)) {
            if (DEBUG) Log.d(TAG, "Theme has changed, recreating activity...");
            sharedPreferences.edit().putBoolean(Constants.KEY_THEME_CHANGE, false).apply();
            // https://stackoverflow.com/questions/10844112/runtimeexception-performing-pause-of-activity-that-is-not-resumed
            // Briefly, let the activity resume properly posting the recreate call to end of the message queue
            new Handler(Looper.getMainLooper()).post(MainActivity.this::recreate);
        }

        if (sharedPreferences.getBoolean(Constants.KEY_MAIN_PAGE_CHANGE, false)) {
            if (DEBUG) Log.d(TAG, "main page has changed, recreating main fragment...");
            sharedPreferences.edit().putBoolean(Constants.KEY_MAIN_PAGE_CHANGE, false).apply();
            NavigationHelper.openMainActivity(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "onNewIntent() called with: intent = [" + intent + "]");
        if (intent != null) {
            // Return if launched from a launcher (e.g. Nova Launcher, Pixel Launcher ...)
            // to not destroy the already created backstack
            String action = intent.getAction();
            if ((action != null && action.equals(Intent.ACTION_MAIN)) && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) return;
        }

        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (DEBUG) Log.d(TAG, "onBackPressed() called");

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
        // If current fragment implements BackPressable (i.e. can/wanna handle back press) delegate the back press to it
        if (fragment instanceof BackPressable) {
            if (((BackPressable) fragment).onBackPressed()) return;
        }


        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            if (!VideoViewManager.instance().onBackPressed()) {
                finish();
            }
        } else if (!VideoViewManager.instance().onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i: grantResults){
            if (i == PackageManager.PERMISSION_DENIED){
                return;
            }
        }
        switch (requestCode) {
            case PermissionHelper.DOWNLOADS_REQUEST_CODE:
                NavigationHelper.openDownloads(this);
                break;
            case PermissionHelper.DOWNLOAD_DIALOG_REQUEST_CODE:
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
                if (fragment instanceof VideoDetailFragment) {
                    ((VideoDetailFragment) fragment).openDownloadDialog();
                }
                break;
        }
    }

    /**
     * Implement the following diagram behavior for the up button:
     * <pre>
     *              +---------------+
     *              |  Main Screen  +----+
     *              +-------+-------+    |
     *                      |            |
     *                      ▲ Up         | Search Button
     *                      |            |
     *                 +----+-----+      |
     *    +------------+  Search  |◄-----+
     *    |            +----+-----+
     *    |   Open          |
     *    |  something      ▲ Up
     *    |                 |
     *    |    +------------+-------------+
     *    |    |                          |
     *    |    |  Video    <->  Channel   |
     *    +---►|  Channel  <->  Playlist  |
     *         |  Video    <->  ....      |
     *         |                          |
     *         +--------------------------+
     * </pre>
     */
     @Override
     public void onHomeButtonPressed() {
        // If search fragment wasn't found in the backstack...
        if (!NavigationHelper.tryGotoSearchFragment(getSupportFragmentManager())) {
            // ...go to the main fragment
            NavigationHelper.gotoMainFragment(getSupportFragmentManager());
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Menu
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (DEBUG) Log.d(TAG, "onCreateOptionsMenu() called with: menu = [" + menu + "]");
        super.onCreateOptionsMenu(menu);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
        if (!(fragment instanceof VideoDetailFragment)) {
            findViewById(R.id.toolbar).findViewById(R.id.toolbar_spinner).setVisibility(View.GONE);
        }

        if (!(fragment instanceof SearchFragment)) {
            findViewById(R.id.toolbar).findViewById(R.id.toolbar_search_container).setVisibility(View.GONE);

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu, menu);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

//        updateDrawerNavigation();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (DEBUG) Log.d(TAG, "onOptionsItemSelected() called with: item = [" + item + "]");
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onHomeButtonPressed();
                return true;
            case R.id.action_show_downloads:
                    return NavigationHelper.openDownloads(this);
            case R.id.action_history:
                    NavigationHelper.openStatisticFragment(getSupportFragmentManager());
                    return true;
            case R.id.action_settings:
                    NavigationHelper.openSettings(this);
                    return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////////////////////////////*/

    private void initFragments() {
        if (DEBUG) Log.d(TAG, "initFragments() called");
        StateSaver.clearStateFiles();
        if (getIntent() != null && getIntent().hasExtra(Constants.KEY_LINK_TYPE)) {
            handleIntent(getIntent());
        } else NavigationHelper.gotoMainFragment(getSupportFragmentManager());
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

//    private void updateDrawerNavigation() {
//        if (getSupportActionBar() == null) return;
//
//        final Toolbar toolbar = findViewById(R.id.toolbar);
//        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
//
//        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
//        if (fragment instanceof MainFragment) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//            if (toggle != null) {
//                toggle.syncState();
//                toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
//                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED);
//            }
//        } else {
//            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            toolbar.setNavigationOnClickListener(v -> onHomeButtonPressed());
//        }
//    }

    private void handleIntent(Intent intent) {
        try {
            if (DEBUG) Log.d(TAG, "handleIntent() called with: intent = [" + intent + "]");

            if (intent.hasExtra(Constants.KEY_LINK_TYPE)) {
                String url = intent.getStringExtra(Constants.KEY_URL);
                int serviceId = intent.getIntExtra(Constants.KEY_SERVICE_ID, 0);
                String title = intent.getStringExtra(Constants.KEY_TITLE);
                switch (((StreamingService.LinkType) intent.getSerializableExtra(Constants.KEY_LINK_TYPE))) {
                    case STREAM:
                        boolean autoPlay = intent.getBooleanExtra(VideoDetailFragment.AUTO_PLAY, false);
                        NavigationHelper.openVideoDetailFragment(getSupportFragmentManager(), serviceId, url, title, autoPlay);
                        break;
                    case CHANNEL:
                        NavigationHelper.openChannelFragment(getSupportFragmentManager(),
                                serviceId,
                                url,
                                title);
                        break;
                    case PLAYLIST:
                        NavigationHelper.openPlaylistFragment(getSupportFragmentManager(),
                                serviceId,
                                url,
                                title);
                        break;
                }
            } else if (intent.hasExtra(Constants.KEY_OPEN_SEARCH)) {
                String searchString = intent.getStringExtra(Constants.KEY_SEARCH_STRING);
                if (searchString == null) searchString = "";
                int serviceId = intent.getIntExtra(Constants.KEY_SERVICE_ID, 0);
                NavigationHelper.openSearchFragment(
                        getSupportFragmentManager(),
                        serviceId,
                        searchString);

            } else {
                NavigationHelper.gotoMainFragment(getSupportFragmentManager());
            }
        } catch (Exception e) {
            ErrorActivity.reportUiError(this, e);
        }
    }
}
