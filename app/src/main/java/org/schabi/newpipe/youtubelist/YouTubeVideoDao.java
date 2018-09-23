package org.schabi.newpipe.youtubelist;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyanju on 2018/9/23.
 */
@Dao
public abstract class YouTubeVideoDao {

    @Insert
    public abstract void insertAll(List<YouTubeVideo> lists);

    @Insert
    public abstract void insert(YouTubeVideo youTubeVideo);

    @Query("select * from YouTubeVideo where playlistid = :playlistid" )
    public abstract List<YouTubeVideo> getYoutubeVideoByid(String playlistid);

    @Query("select * from YouTubeVideo where vid = :vid" )
    public abstract List<YouTubeVideo> getYoutubeVideoByVid(String vid);

    @Delete
    public abstract void delete(YouTubeVideo youTubeVideo);
}
