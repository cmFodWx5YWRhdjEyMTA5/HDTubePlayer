package org.schabi.newpipe.youtubelist;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;



import java.util.List;


/**
 * Created by liyanju on 2018/9/23.
 */

@Dao
public abstract class YouTubePlaylistDao {

    @Query("SELECT * FROM " + YouTubePlayList.YOUTUBEPLAYLIST)
    public abstract List<YouTubePlayList> getAllYouTubePlaylist();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(YouTubePlayList... youTubePlayLists);

    @Insert
    public abstract void insertAll(List<YouTubePlayList> lists);

    @Delete
    public abstract void delete(YouTubePlayList youTubePlayList);
}
