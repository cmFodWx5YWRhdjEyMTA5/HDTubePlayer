package org.schabi.newpipe.youtubelist;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dueeeke.videoplayer.util.NetworkUtil;
import com.facebook.stetho.common.LogUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.util.MockBackOff;
import com.google.api.client.util.BackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequest;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemContentDetails;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.ThumbnailDetails;

import org.schabi.newpipe.App;
import org.schabi.newpipe.R;
import org.schabi.newpipe.util.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

import static android.app.Activity.RESULT_OK;

/**
 * Created by liyanju on 2018/9/23.
 */

public class YouTubeApiMannager {

    private static volatile YouTubeApiMannager sYouTubeApiManager;

    private WeakReference<Activity> mActivity;

    /**
     * YouTube 歌单
     */
    private static final int REQUEST_ACCOUNT_PICKER_LIST = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    /**
     * YouTube 歌单内歌曲
     */
    private static final int REQUEST_ACCOUNT_PICKER_ITEM = 1003;

    public static final int RESULT_LIST_EMPTY = 0;
    public static final int RESULT_UNSELECTED = 1;
    public static final int RESULT_GOOGLEPLAY_UNAVAILABLE = 2;
    public static final int RESULT_NET_UNAVAILABLE = 3;
    public static final int RESULT_REQUEST_FAIL = 4;

    public static final int RESULT_LIST_ITEM_EMPTY = 5;

    public static final Long YOUTUBE_PLAYLIST_MAX_SIZE = 50L;

    private static final String[] SCOPES = {YouTubeScopes.YOUTUBE_READONLY};

    private GoogleAccountCredential mCredential;

    private static final String PREF_ACCOUNT_NAME = "google_accountName";

    private YouTubeApiMannager(){
        BackOff mockBackOff = new MockBackOff().setBackOffMillis(500).setMaxTries(3);
        // BackOff mockBackOff = new ExponentialBackOff()
        mCredential = GoogleAccountCredential.usingOAuth2(
                App.sContext, Arrays.asList(SCOPES))
                .setBackOff(mockBackOff);
    }

    public static YouTubeApiMannager get() {
        if (sYouTubeApiManager == null) {
            synchronized (YouTubeApiMannager.class) {
                if (sYouTubeApiManager == null) {
                    sYouTubeApiManager = new YouTubeApiMannager();
                }
            }
        }
        return sYouTubeApiManager;
    }

    private boolean isGooglePlayServicesAvailable() {
        Context context = App.sContext;
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    public YouTubeApiMannager setTargetActivity(Activity activity) {
        mActivity = new WeakReference<>(activity);
        return this;
    }

    private YouTubeRequest mYouTubeRequest;

    public YouTubeApiMannager setCallBack(YouTubeRequest youTubeRequest) {
        mYouTubeRequest = youTubeRequest;
        return this;
    }

    private YouTubeApi mYouTubeApi;

    public YouTubeApiMannager createYouTubeApi(YouTubeRequest youTubeRequest) {
        mYouTubeApi = new YouTubeApi(youTubeRequest, mCredential);
        return this;
    }

    private void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        if (mActivity == null || mActivity.get() == null) {
            return;
        }

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                mActivity.get(),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private void chooseAccount(int requestCode) {
        if (mActivity == null || mActivity.get() == null) {
            return;
        }
        mActivity.get().startActivityForResult(
                mCredential.newChooseAccountIntent(),
                requestCode);
    }

    private void requestList(int requestCode, Intent data) {
        if (requestCode == REQUEST_ACCOUNT_PICKER_LIST || requestCode == REQUEST_AUTHORIZATION) {
            requestYoutubePlayLists();
        }
    }

    public String getSelectAccountName() {
        return App.sPreferences.getString(PREF_ACCOUNT_NAME, "");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("youtube", "onActivityResult requestCode " + requestCode
                + " resultCode "+ resultCode);
        if (mActivity == null || mActivity.get() == null) {
            return;
        }
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    if (mYouTubeRequest != null) {
                        mYouTubeRequest.onError(RESULT_GOOGLEPLAY_UNAVAILABLE,
                                "This app requires Google Play Services. Please install "
                                        + "Google Play Services on your device and relaunch this app.");
                    }
                } else {
                    requestList(requestCode, data);
                }
                break;
            case REQUEST_ACCOUNT_PICKER_LIST:
            case REQUEST_ACCOUNT_PICKER_ITEM:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = App.sPreferences;
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
//                        mCredential.setSelectedAccountName(accountName);
                        mCredential.setSelectedAccount(new Account(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME),
                                "com.google"));
                        requestList(requestCode, data);
                    } else {
                        if (mYouTubeRequest != null) {
                            mYouTubeRequest.onError(RESULT_UNSELECTED, "no selected");
                        }
                    }
                } else {
                    if (mYouTubeRequest != null) {
                        mYouTubeRequest.onError(RESULT_UNSELECTED, "no selected");
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    requestList(requestCode, data);
                } else {
                    if (mYouTubeRequest != null) {
                        mYouTubeRequest.onError(RESULT_GOOGLEPLAY_UNAVAILABLE,
                                "This app requires Google Play Services. Please install "
                                        + "Google Play Services on your device and relaunch this app.");

                    }
                }
        }
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(App.sContext);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }
    private void showSelectListDialog(final List<YouTubePlayList> lists, YouTubeRequest youTubeRequest) {
        if (mActivity == null || mActivity.get() == null) {
            return;
        }

        final Dialog create = new AlertDialog.Builder(mActivity.get()).create();
        create.show();
        create.setCancelable(true);
        create.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (youTubeRequest != null) {
                    youTubeRequest.onError(RESULT_UNSELECTED, "no selected");
                }
            }
        });
        Window window = create.getWindow();
        window.setContentView(R.layout.dialog_import_playlist);
        Window window2 = create.getWindow();
        WindowManager.LayoutParams attributes = window2.getAttributes();
        attributes.width = (int) (Utils.getScreenWidth() * 0.9f);
        attributes.height = -2;
        window2.setAttributes(attributes);
        final ImportPlayListAdapter mAdapter = new ImportPlayListAdapter(mActivity.get(), lists);
        ListView listView = (ListView) window.findViewById(R.id.dialog_listView);
        final Button button = (Button) window.findViewById(R.id.btn_confirm);
        Button button2 = (Button) window.findViewById(R.id.btn_cancel);
        int itemHeighUnit = lists.size();
        if (itemHeighUnit > 4) {
            itemHeighUnit = 4;
        }
        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
        layoutParams.height = Utils.dip2px(40) * itemHeighUnit;
        listView.setLayoutParams(layoutParams);

        button.setEnabled(false);
        listView.setAdapter(mAdapter);
        final TextView textView = (TextView) window.findViewById(R.id.dialog_tv_select);
        int selecedCount = 0;
        for (YouTubePlayList list : lists) {
            if (list.isSelected()) {
                selecedCount++;
            }
        }
        try {
            textView.setText(
                    String.format(mActivity.get().getResources().getString(R.string.dialog_youtube_sub_title), selecedCount));
        } catch (Exception e) {
            e.printStackTrace();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<YouTubePlayList> selectedLists = new ArrayList<>();
                lists.remove(0);
                for (YouTubePlayList list : lists) {
                    if (list.isSelected()) {
                        selectedLists.add(list);
                    }
                }
                if (youTubeRequest != null) {
                    if (selectedLists.size() > 0) {
                        youTubeRequest.onSuccess(selectedLists);
                    } else {
                        youTubeRequest.onError(RESULT_UNSELECTED, "no selected");
                    }
                }
                create.dismiss();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create.dismiss();
                if (youTubeRequest != null) {
                    youTubeRequest.onError(RESULT_UNSELECTED, "no selected");
                }
            }
        });
        final List<YouTubePlayList> tmpList = lists;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    boolean isSelected = tmpList.get(0).isSelected();
                    for (YouTubePlayList item : tmpList) {
                        item.setSelected(!isSelected);
                    }
                } else {
                    tmpList.get(position).setToggle();
                }
                int sCount = mAdapter.getSelectedCount();
                if (sCount > 0) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }
                try {
                    textView.setText(
                            String.format(mActivity.get().getResources().getString(R.string.dialog_youtube_sub_title), sCount));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAdapter.notifyDataSetChanged();
            }
        });

    }

    public void requestYoutubePlayLists() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount(REQUEST_ACCOUNT_PICKER_LIST);
        } else if (!Utils.isNetWorkConnected()) {
            if (mYouTubeRequest != null) {
                mYouTubeRequest.onError(RESULT_NET_UNAVAILABLE, "No network connection available");
            }
        } else {
            mYouTubeApi.requestPlaylist();
        }
    }

    public void requestYoutubePlayListItems(String playListId, String googleAccount) {
        if (!TextUtils.isEmpty(googleAccount)) {
            mCredential.setSelectedAccount(new Account(googleAccount, "com.google"));
        }
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount(REQUEST_ACCOUNT_PICKER_ITEM);
        } else if (!Utils.isNetWorkConnected()) {
            if (mYouTubeRequest != null) {
                mYouTubeRequest.onError(RESULT_NET_UNAVAILABLE, "No network connection available.");
            }
        } else {
            mYouTubeApi.requestListItem(playListId);
        }
    }

    private class YouTubeApi {

        private YouTubeRequest mYouTubeRequest;

        private YouTube mService = null;

        public YouTubeApi(YouTubeRequest youTubeRequest, GoogleAccountCredential credential) {
            mYouTubeRequest = youTubeRequest;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Melodyrun Music")
                    .build();
        }

        public void requestListItem(String playListId) {
            new ListItemRequestTask().executeOnExecutor(Utils.sSingleExecutor, playListId);
        }

        public void requestPlaylist() {
            new PlaylistRequestTask().executeOnExecutor(Utils.sSingleExecutor);
        }

        private class ListItemRequestTask extends AsyncTask<String, Void, List<YouTubeVideo>> {

            private Throwable mLastError = null;

            @Override
            protected List<YouTubeVideo> doInBackground(String... strings) {
                String playlistid = strings[0];
                try {
                    return getDataFromApi(playlistid);
                } catch (Throwable e) {
                    e.printStackTrace();
                    mLastError = e;
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                if (mLastError != null) {
                    if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                        showGooglePlayServicesAvailabilityErrorDialog(
                                ((GooglePlayServicesAvailabilityIOException) mLastError)
                                        .getConnectionStatusCode());
                    } else if (mLastError instanceof UserRecoverableAuthIOException) {
                        mActivity.get().startActivityForResult(
                                ((UserRecoverableAuthIOException) mLastError).getIntent(),
                                REQUEST_AUTHORIZATION);
                    } else {
                        if (mYouTubeRequest != null) {
                            mYouTubeRequest.onError(RESULT_REQUEST_FAIL, "The following error occurred:\n"
                                    + mLastError.getMessage());

                        }
                    }
                } else {
                    if (mYouTubeRequest != null) {
                        mYouTubeRequest.onError(RESULT_REQUEST_FAIL, "Request cancelled.");
                    }
                }
            }

            @Override
            protected void onPostExecute(List<YouTubeVideo> youTubeVideos) {
                super.onPostExecute(youTubeVideos);
                if (youTubeVideos == null || youTubeVideos.size() == 0) {
                    if (mYouTubeRequest != null) {
                        mYouTubeRequest.onError(RESULT_LIST_ITEM_EMPTY, "No results returned.");
                    }
                } else {
                    if (mYouTubeRequest != null) {
                        mYouTubeRequest.onSuccess(youTubeVideos);
                    }
                }
            }

            private List<YouTubeVideo> getDataFromApi(String playlistid) throws Exception{
                List<YouTubeVideo> youtubeLists = new ArrayList<>();

                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("part", "snippet,contentDetails");
                parameters.put("maxResults", "50");
                parameters.put("playlistId", playlistid);


                YouTube.PlaylistItems.List playlistItemsListByPlaylistIdRequest = mService.playlistItems()
                        .list("snippet,contentDetails");
                playlistItemsListByPlaylistIdRequest
                        .setMaxResults(YOUTUBE_PLAYLIST_MAX_SIZE);
                if (parameters.containsKey("playlistId") && parameters.get("playlistId") != "") {
                    playlistItemsListByPlaylistIdRequest.setPlaylistId(parameters.get("playlistId").toString());
                }

                PlaylistItemListResponse itemListResponse = playlistItemsListByPlaylistIdRequest.execute();
                List<PlaylistItem> items = itemListResponse.getItems();

                if (items != null) {
                    for (int i = 0, size = items.size(); i < size; i++) {
                        PlaylistItem playlistItem = items.get(i);
                        ThumbnailDetails thumbaails = playlistItem.getSnippet().getThumbnails();
                        String imageRefPath = "";
                        if (thumbaails != null) {
                            imageRefPath = thumbaails.getDefault().getUrl();
                        }
                        String title = playlistItem.getSnippet().getTitle();

                        YouTubeVideo tubeVideo = new YouTubeVideo();
                        PlaylistItemContentDetails itemDetails = playlistItem.getContentDetails();
                        String vid = itemDetails.getVideoId();
                        tubeVideo.vid = vid;
                        tubeVideo.coverUrl = imageRefPath;
                        tubeVideo.title = title;

                        youtubeLists.add(tubeVideo);
                    }
                }

                return youtubeLists;
            }
        }

        private class PlaylistRequestTask extends AsyncTask<Void, Void, java.util.List<YouTubePlayList>> {

            private java.util.List<YouTubePlayList> getDataFromApi() throws Exception{
                PlaylistListResponse response = mService.playlists().list("snippet,contentDetails").setMine(Boolean.TRUE)
                        .setMaxResults(Long.valueOf(50))
                        .execute();
                List<Playlist> lists = response.getItems();
                List<YouTubePlayList> youtubeLists = new ArrayList<>();
                if (lists != null) {
                    for (int i = 0, size = lists.size(); i < size; i++) {
                        Playlist playlist = lists.get(i);
                        ThumbnailDetails thumbaails = playlist.getSnippet().getThumbnails();
                        String imageRefPath = "";
                        if (thumbaails != null) {
                            imageRefPath = thumbaails.getDefault().getUrl();
                        }
                        String youtubeId = playlist.getId();
                        Log.d("Youtube", " playlist id:: " + youtubeId);
                        String playListName = playlist.getSnippet().getTitle();
                        String playListId = String.valueOf(playlist.getId());

                        YouTubePlayList youTubePlayList = new YouTubePlayList();
                        youTubePlayList.name = playListName;
                        youTubePlayList.iconUrl = imageRefPath;
                        youTubePlayList.playlistId = playListId;
                        youTubePlayList.createTime = System.currentTimeMillis();
                        youTubePlayList.accountName = mCredential.getSelectedAccountName();
                        youTubePlayList.youtubeId = playlist.getId();
                        youTubePlayList.listCount = playlist.getContentDetails().getItemCount();

                        youtubeLists.add(youTubePlayList);
                    }
                }
                return youtubeLists;
            }

            private Throwable mLastError = null;

            @Override
            protected List<YouTubePlayList> doInBackground(Void... voids) {
                try {
                    return getDataFromApi();
                } catch (Throwable e) {
                    e.printStackTrace();
                    mLastError = e;
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                if (mLastError == null) {
                    return;
                }

                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    if (mActivity.get() != null) {
                        mActivity.get().startActivityForResult(
                                ((UserRecoverableAuthIOException) mLastError).getIntent(),
                                REQUEST_AUTHORIZATION);
                    }
                } else {
                    if (mYouTubeRequest != null) {
                        mYouTubeRequest.onError(RESULT_REQUEST_FAIL, "The following error occurred:\n"
                                + mLastError.getMessage());
                    }
                }

            }

            @Override
            protected void onPostExecute(List<YouTubePlayList> youTubePlayLists) {
                super.onPostExecute(youTubePlayLists);
                if (youTubePlayLists == null || youTubePlayLists.size() == 0) {
                    if (mYouTubeRequest != null) {
                        mYouTubeRequest.onError(RESULT_LIST_EMPTY, "No results returned");
                    }
                } else {
                    showSelectListDialog(youTubePlayLists, mYouTubeRequest);
                }
            }
        }
    }

    public interface YouTubeRequest<T> {

        void onError(int errorCode, String string);

        void onSuccess(List<T> list);
    }


}
