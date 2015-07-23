package com.ozm.rocks.ui.screen.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.ozm.rocks.base.ComponentFinder;
import com.ozm.rocks.base.mvp.BaseView;
import com.ozm.rocks.data.TokenStorage;
import com.ozm.rocks.data.analytics.LocalyticsController;
import com.ozm.rocks.data.api.model.Config;
import com.ozm.rocks.ui.screen.main.MainComponent;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class SettingsView extends LinearLayout implements BaseView, SettingItemView.OnClickListener {

    @Inject
    TokenStorage tokenStorage;

    @Inject
    SettingsPresenter presenter;

    @Inject
    LocalyticsController localyticsController;

    private Map<SettingItems, SettingItemView> viewItems = new LinkedHashMap<>(SettingItems.values().length);

    public SettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        MainComponent component = ComponentFinder.findActivityComponent(context);
        component.inject(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        for (SettingItems item : SettingItems.values()) {
            SettingItemView view = (SettingItemView) findViewById(item.getViewId());
            view.bindView(item, this);
            viewItems.put(item, view);
        }
        getItemView(SettingItems.WIDGET).setChecked(tokenStorage.isShowWidget());
        getItemView(SettingItems.ALBUM).setChecked(tokenStorage.isCreateAlbum());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        presenter.takeView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        presenter.dropView(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void showContent() {

    }

    @Override
    public void showError(Throwable throwable) {

    }

    private SettingItemView getItemView(SettingItems item) {
        return viewItems.get(item);
    }

    @Override
    public void onClick(SettingItemView view) {
        if (view.getItem() == SettingItems.WIDGET) {
            final boolean checked = view.isChecked();
            tokenStorage.showWidget(checked);
            if (checked) {
                presenter.startService();
            } else {
                presenter.stopService();
            }
        } else if (view.getItem() == SettingItems.FEEDBACK) {
            presenter.openFeedback();
        } else if (view.getItem() == SettingItems.ESTIMATION) {
            presenter.openGooglePlay();
        } else if (view.getItem() == SettingItems.ALBUM) {
            final boolean checked = view.isChecked();
            localyticsController.setAlbumSettings(checked ? LocalyticsController.ON : LocalyticsController.OFF);
            tokenStorage.setCreateAlbum(checked);
            if (!getItemView(SettingItems.ALBUM).isChecked()){
                presenter.deleteAllFromGallery();
            }
        } else if (view.getItem() == SettingItems.CENSORSHIP) {
            final boolean checked = view.isChecked();
            localyticsController.setSwearSettings(checked ? LocalyticsController.ON : LocalyticsController.OFF);
            presenter.sendCensorShipSetting(checked);
        } else if (view.getItem() == SettingItems.TALK_FRIEND) {
            localyticsController.setShareOzm(LocalyticsController.SIDEBAR);
            presenter.talkFriend();
        }
    }

    public void bindConfigData(Config config) {
        getItemView(SettingItems.CENSORSHIP).setChecked(config.obsceneDisabled());
    }
}
