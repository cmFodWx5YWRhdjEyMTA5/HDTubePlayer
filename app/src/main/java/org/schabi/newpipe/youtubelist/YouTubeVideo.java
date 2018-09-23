package org.schabi.newpipe.youtubelist;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by liyanju on 2018/9/23.
 */
@Entity(tableName = "YouTubeVideo")
public class YouTubeVideo {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo
    public String vid;

    @ColumnInfo
    public String coverUrl;

    @ColumnInfo
    public String title;

    @ColumnInfo
    public String playlistid;
}
