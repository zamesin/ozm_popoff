package com.ozm.rocks.ui.screen.gold.favorite;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ozm.R;
import com.ozm.rocks.data.api.response.ImageResponse;
import com.ozm.rocks.ui.misc.RecyclerBindableAdapter;
import com.ozm.rocks.util.Strings;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GoldFavoriteAdapter extends RecyclerBindableAdapter<ImageResponse, GoldFavoriteAdapter.ViewHolder> {

    private final Context context;
    private final Callback callback;
    private final Picasso picasso;

    private int maximumDecide;

    private OnDecideListener onDecideListener;

    public GoldFavoriteAdapter(Context context, Picasso picasso,
                               RecyclerView.LayoutManager manager,
                               Callback callback) {
        super(context, manager);

        this.context = context;
        this.picasso = picasso;
        this.callback = callback;
    }

    private void loadingImagesPreview() {
        for (int i = 0; i < getRealItemCount(); i++) {
            ImageResponse image = getItem(i);
            if (!image.isGIF) {
                fetchImage(image);
            }
        }
    }

    @Override
    public int getItemCount() {
        return getRealItemCount();
    }

    @Override
    public void add(int position, ImageResponse item) {
        super.add(position, item);
        fetchImage(item);
    }

    private void fetchImage(ImageResponse item) {
        if (Strings.isBlank(item.thumbnailUrl)) {
            picasso.load(item.url).fetch();
        } else {
            picasso.load(item.thumbnailUrl).fetch();
        }
    }

    public void addAll(List<? extends ImageResponse> items) {
//        Collections.sort(items, new Comparator<ImageResponse>() {
//            @Override
//            public int compare(ImageResponse lhs, ImageResponse rhs) {
//                if((lhs.isNew && rhs.isNew) || (!lhs.isNew && !rhs.isNew)) {
//                    return 0;
//                } else if (lhs.isNew) {
//                    return -1;
//                } else {
//                    return 1;
//                }
//            }
//        });
        super.addAll(items);
        loadingImagesPreview();
    }

    @Override
    protected int getItemType(int position) {
        return 0;
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.gold_favorite_item_view;
    }

    @Override
    protected ViewHolder viewHolder(View view, int type) {
        return new ViewHolder(view);
    }

    @Override
    protected void onBindItemViewHolder(ViewHolder viewHolder, int position, int type) {
        viewHolder.bindView(getItem(position), context, position, picasso, callback);
        int decide = position / 10;
        if (decide > maximumDecide) {
            maximumDecide = decide;
            if (onDecideListener != null) {
                onDecideListener.callDecide(maximumDecide * 10);
            }
        }
    }

    public void setOnDecideListener(OnDecideListener onDecideListener) {
        this.onDecideListener = onDecideListener;
    }

    public interface Callback {
        void click(ImageResponse image, int position);
        void doubleTap(ImageResponse image, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void bindView(ImageResponse item, final Context context, int position,
                             final Picasso picasso, final Callback callback) {
            ((GoldFavoriteItemView) itemView).bindView(item, context, position, picasso, callback);
        }
    }

    public static interface OnDecideListener {
        void callDecide(int count);
    }
}
