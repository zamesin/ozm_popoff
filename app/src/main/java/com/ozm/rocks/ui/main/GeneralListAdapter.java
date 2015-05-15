package com.ozm.rocks.ui.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ozm.R;
import com.ozm.rocks.data.api.request.DislikeRequest;
import com.ozm.rocks.data.api.request.LikeRequest;
import com.ozm.rocks.data.api.response.ImageResponse;
import com.ozm.rocks.ui.misc.BindableAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneralListAdapter extends BindableAdapter<ImageResponse> {
    private List<ImageResponse> list = Collections.emptyList();
    private ActionListener actionListener;

    public GeneralListAdapter(Context context, @NonNull ActionListener actionListener) {
        super(context);
        this.actionListener = actionListener;
    }

    public void updateAll(List<ImageResponse> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void addAll(List<ImageResponse> list) {
        List<ImageResponse> newList = new ArrayList<>(this.list);
        newList.addAll(list);
        this.list = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ImageResponse getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        return inflater.inflate(R.layout.main_general_list_item_view, container, false);
    }

    @Override
    public void bindView(ImageResponse item, int position, View view) {
        ((GeneralListItemView) view).bindTo(item, position, actionListener);
    }

    public void updateLikedItem(int positionInList, boolean b) {
        getItem(positionInList).liked = b;
        notifyDataSetChanged();
    }

    public interface ActionListener {
        void like(int itemPosition, LikeRequest likeRequest);

        void dislike(int itemPosition, DislikeRequest dislikeRequest);
    }
}
