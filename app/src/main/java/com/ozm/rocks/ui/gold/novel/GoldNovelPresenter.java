package com.ozm.rocks.ui.gold.novel;

import android.support.annotation.Nullable;

import com.ozm.rocks.base.mvp.BasePresenter;
import com.ozm.rocks.data.DataService;
import com.ozm.rocks.data.analytics.LocalyticsController;
import com.ozm.rocks.data.api.response.Category;
import com.ozm.rocks.data.api.response.ImageResponse;
import com.ozm.rocks.ui.gold.GoldScope;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

@GoldScope
public class GoldNovelPresenter extends BasePresenter<GoldNovelView> {

    private final DataService dataService;
    private final LocalyticsController localyticsController;
    private final Category category;

    @Nullable
    private CompositeSubscription subscriptions;

    private List<ImageResponse> mImageResponses = new ArrayList<>();

    @Inject
    public GoldNovelPresenter(DataService dataService,
                              LocalyticsController localyticsController,
                              @Named("category") Category category) {
        this.dataService = dataService;
        this.localyticsController = localyticsController;
        this.category = category;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        subscriptions = new CompositeSubscription();
        if (mImageResponses.isEmpty()) {
            loadFeed(0);
        }
    }

    public void loadFeed(int page) {
        final GoldNovelView view = getView();
        if (view == null || subscriptions == null) {
            return;
        }
        subscriptions.add(dataService.getCategoryFeed(category.id, page)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Action1<List<ImageResponse>>() {
                                    @Override
                                    public void call(List<ImageResponse> imageResponses) {
                                        mImageResponses.addAll(imageResponses);
                                        if (view != null) {
                                            view.updateFeed(imageResponses);
                                        }
                                    }
                                },
                                new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        Timber.w(throwable, "Gold. Load novel feed error!");
                                    }
                                })
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscriptions != null) {
            subscriptions.unsubscribe();
            subscriptions = null;
        }
    }

}
