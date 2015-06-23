package com.ozm.rocks.base.mvp;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.facebook.appevents.AppEventsLogger;
import com.ozm.R;
import com.ozm.rocks.OzomeApplication;
import com.ozm.rocks.OzomeComponent;
import com.ozm.rocks.data.analytics.LocalyticsActivity;
import com.ozm.rocks.ui.AppContainer;
import com.ozm.rocks.ui.OnGoBackPresenter;
import com.ozm.rocks.ui.message.MessageInterface;
import com.ozm.rocks.ui.message.NoInternetPresenter;
import com.ozm.rocks.ui.message.NoInternetView;
import com.ozm.rocks.ui.sharing.SharingService;
import com.ozm.rocks.util.NetworkState;

import javax.inject.Inject;

import cat.ppicas.customtypeface.CustomTypeface;
import cat.ppicas.customtypeface.CustomTypefaceFactory;

public abstract class BaseActivity extends LocalyticsActivity implements MessageInterface {

    @Inject
    AppContainer appContainer;

    @Inject
    NetworkState networkState;

    @Inject
    NoInternetPresenter noInternetPresenter;

    @Inject
    SharingService sharingService;
    @Inject
    OnGoBackPresenter onGoBackPresenter;

    private NoInternetView noInternetView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle params = getIntent().getExtras();
        if (params != null) {
            onExtractParams(params);
        }
        getLayoutInflater().setFactory(new CustomTypefaceFactory(this, CustomTypeface.getInstance()));
        super.onCreate(savedInstanceState);

        OzomeApplication app = OzomeApplication.get(this);
        onCreateComponent(app.component());
        if (appContainer == null) {
            throw new IllegalStateException("No injection happened. Add component.inject(this)"
                    + " in onCreateComponent() implementation.");
        }
        Registry.add(this, viewId(), presenter());
        final LayoutInflater layoutInflater = getLayoutInflater();
        ViewGroup container = appContainer.get(this);
        ViewGroup base = (ViewGroup) layoutInflater.inflate(R.layout.base_layout, container);
        noInternetView = (NoInternetView) base.findViewById(R.id.no_internet_view);
        ViewGroup my = (ViewGroup) layoutInflater.inflate(layoutId(), null);
        base.addView(my, 0);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        networkState.bind();
        noInternetPresenter.attach(this);
        sharingService.attach(this);
    }

    @Override
    protected void onStop() {
        networkState.unbind();
        noInternetPresenter.detach();
        sharingService.detach();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (onGoBackPresenter.getOnBackInterface() != null) {
            onGoBackPresenter.getOnBackInterface().onBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    protected void onExtractParams(@NonNull Bundle params) {
    }

    @Override
    public NoInternetView getNoNoInternetView() {
        return noInternetView;
    }

    protected abstract void onCreateComponent(OzomeComponent component);

    @LayoutRes
    protected abstract int layoutId();

    protected abstract BasePresenter<? extends BaseView> presenter();

    @IdRes
    protected abstract int viewId();

}
