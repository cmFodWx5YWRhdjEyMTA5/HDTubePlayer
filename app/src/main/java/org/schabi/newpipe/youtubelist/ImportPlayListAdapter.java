package org.schabi.newpipe.youtubelist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.schabi.newpipe.R;

import java.util.List;

/**
 * Created by liyanju on 2018/9/23.
 */

public class ImportPlayListAdapter extends BaseAdapter {
    private Context mContext;
    private List<YouTubePlayList> lists;

    public ImportPlayListAdapter(Context mContext, List<YouTubePlayList> lists) {
        this.mContext = mContext;
        this.lists = lists;
        lists.add(0, new YouTubePlayList());
    }

    @Override
    public int getCount() {
        if (lists != null) {
            return lists.size();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public YouTubePlayList getItem(int position) {
        if (lists != null) {
            return lists.get(position);
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IPViewHolder holder = null;
        View view = convertView;
        if (view == null) {
            holder = new IPViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.listitem_import, parent, false);
            holder.mTitleTv = (TextView) view.findViewById(R.id.dialog_import_playlist_name);
            holder.mCheckBtn = (CheckBox) view.findViewById(R.id.checkbox_import);
            view.setTag(holder);
        } else {
            holder = (IPViewHolder) view.getTag();
        }
        YouTubePlayList list = getItem(position);
        if (list != null) {
            int selectedCount = getSelectedCount();
            if (position == 0) {
                holder.mTitleTv.setText(mContext.getResources().getString(R.string.select_all));
                if (selectedCount >= lists.size() - 1) {
                    list.setSelected(true);
                } else {
                    list.setSelected(false);
                }
            } else {
                holder.mTitleTv.setText(list.name);
            }
            holder.mCheckBtn.setChecked(list.isSelected());
        }
        return view;
    }

    public int getSelectedCount() {
        int count = 0;
        int index = 0;
        while (index < lists.size()) {
            if (index != 0 && lists.get(index).isSelected()) {
                count++;
            }
            index++;
        }
        return count;
    }

    private class IPViewHolder {
        public TextView mTitleTv;
        public CheckBox mCheckBtn;
    }
}
