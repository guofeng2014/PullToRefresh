package com.pulltorefresh;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：guofeng
 * ＊ 日期:16/7/5
 */
public class ListAdapter extends BaseAdapter {

    List<String> list;
    Context context;

    public ListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        if (list != null) return list.size();
        return 0;
    }

    @Override
    public String getItem(int position) {
        if (list != null && position < list.size()) return list.get(position);
        return "";
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void addData(List<String> sList) {
        if (list == null) list = new ArrayList<>();
        list.addAll(sList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.simple_item, null);
            holder = new ViewHolder();
            holder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvContent.setText(getItem(position));
        return convertView;
    }

    static class ViewHolder {
        TextView tvContent;
    }
}
