package org.schabi.newpipe.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.schabi.newpipe.BaseFragment;
import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.fragments.list.kiosk.KioskFragment;
import org.schabi.newpipe.local.bookmark.BookmarkFragment;
import org.schabi.newpipe.local.subscription.SubscriptionFragment;
import org.schabi.newpipe.report.ErrorActivity;
import org.schabi.newpipe.report.UserAction;
import org.schabi.newpipe.router.Router;
import org.schabi.newpipe.settings.tabs.Tab;
import org.schabi.newpipe.settings.tabs.TabsManager;
import org.schabi.newpipe.util.NavigationHelper;
import org.schabi.newpipe.util.ServiceHelper;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends BaseFragment implements TabLayout.OnTabSelectedListener,IMainFragment {
    private ViewPager viewPager;
    private SelectedTabsPagerAdapter pagerAdapter;

    private List<Fragment> tabsList = new ArrayList<>();

    /*//////////////////////////////////////////////////////////////////////////
    // Fragment's LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void setCurrentItem(int item) {
        if (viewPager != null) {
            viewPager.setCurrentItem(item);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    protected void initViews(View rootView, Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);

        Router.getInstance().register(this);

        viewPager = rootView.findViewById(R.id.pager);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                try {
                    Router.getInstance().getReceiver(IMainActivity.class).setSelectedItemId(position);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        /*  Nested fragment, use child fragment here to maintain backstack in view pager. */
        pagerAdapter = new SelectedTabsPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        try {
            tabsList.add(KioskFragment.getInstance(0, "Trending"));
            tabsList.add(new SubscriptionFragment());
            tabsList.add(new BookmarkFragment());
            tabsList.add(new MyTubeFragment());
        } catch (ExtractionException e) {
            e.printStackTrace();
        }

        pagerAdapter.notifyDataSetChanged();
        viewPager.setOffscreenPageLimit(pagerAdapter.getCount());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewPager.clearOnPageChangeListeners();
        Router.getInstance().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Menu
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (DEBUG) Log.d(TAG, "onCreateOptionsMenu() called with: menu = [" + menu + "], inflater = [" + inflater + "]");
        inflater.inflate(R.menu.main_fragment_menu, menu);

        ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                try {
                    NavigationHelper.openSearchFragment(
                            getFragmentManager(),
                            ServiceHelper.getSelectedServiceId(activity),
                            "");
                } catch (Exception e) {
                    ErrorActivity.reportUiError((AppCompatActivity) getActivity(), e);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Tabs
    //////////////////////////////////////////////////////////////////////////*/


    @Override
    public void onTabSelected(TabLayout.Tab selectedTab) {
        if (DEBUG) Log.d(TAG, "onTabSelected() called with: selectedTab = [" + selectedTab + "]");
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        if (DEBUG) Log.d(TAG, "onTabReselected() called with: tab = [" + tab + "]");
    }

    private class SelectedTabsPagerAdapter extends FragmentPagerAdapter {
        private SelectedTabsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
           return tabsList.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            // Causes adapter to reload all Fragments when
            // notifyDataSetChanged is called
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return tabsList.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            getChildFragmentManager()
                    .beginTransaction()
                    .remove((Fragment) object)
                    .commitNowAllowingStateLoss();
        }
    }
}
