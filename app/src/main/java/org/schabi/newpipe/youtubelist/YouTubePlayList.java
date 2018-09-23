package org.schabi.newpipe.youtubelist;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by liyanju on 2018/9/23.
 */
@Entity(tableName = "YouTubePlayList")
public class YouTubePlayList {

    final static String YOUTUBEPLAYLIST              = "YouTubePlayList";

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo
    public String iconUrl;

    @ColumnInfo
    public String name;

    @ColumnInfo
    public String playlistId;

    @ColumnInfo
    public String accountName;

    @ColumnInfo
    public long createTime;

    @ColumnInfo
    public String youtubeId;

    @ColumnInfo
    public long listCount;

    @Ignore
    private boolean isSelected = false;

    @Ignore
    public boolean isSelected() {
        return isSelected;
    }

    @Ignore
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Ignore
    public void setToggle() {
        this.isSelected = !this.isSelected;
    }

}
