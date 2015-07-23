package com.ozm.rocks;

import android.app.Application;

import com.ozm.rocks.base.navigation.activity.ActivityScreenSwitcher;
import com.ozm.rocks.base.tools.KeyboardPresenter;
import com.ozm.rocks.base.tools.ToastPresenter;
import com.ozm.rocks.data.Clock;
import com.ozm.rocks.data.DataService;
import com.ozm.rocks.data.FileService;
import com.ozm.rocks.data.TokenStorage;
import com.ozm.rocks.data.analytics.LocalyticsController;
import com.ozm.rocks.data.prefs.rating.RatingStorage;
import com.ozm.rocks.data.social.SocialPresenter;
import com.ozm.rocks.ui.ActivityHierarchyServer;
import com.ozm.rocks.ui.AppContainer;
import com.ozm.rocks.ui.ApplicationSwitcher;
import com.ozm.rocks.ui.OnGoBackPresenter;
import com.ozm.rocks.ui.screen.categories.LikeHideResult;
import com.ozm.rocks.ui.screen.main.SendFriendDialogBuilder;
import com.ozm.rocks.ui.screen.main.personal.OnBoardingDialogBuilder;
import com.ozm.rocks.ui.screen.message.NoInternetPresenter;
import com.ozm.rocks.ui.screen.sharing.ChooseDialogBuilder;
import com.ozm.rocks.ui.screen.sharing.SharingDialogBuilder;
import com.ozm.rocks.ui.screen.sharing.SharingService;
import com.ozm.rocks.ui.screen.widget.WidgetController;
import com.ozm.rocks.util.NetworkState;
import com.ozm.rocks.util.PackageManagerTools;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;


/**
 * A common interface implemented by both the Release and Debug flavored components.
 */
public interface OzomeDependencies {
    void inject(OzomeApplication app);

    Application application();

    AppContainer appContainer();

//    RefWatcher refWatcher();

    Picasso picasso();

    OkHttpClient okHttpClient();

    ActivityScreenSwitcher activityScreenSwitcher();

    ActivityHierarchyServer activityHierarchyServer();

    DataService dataService();

    TokenStorage tokenStorage();

    RatingStorage ratingStorage();

    Clock clock();

    KeyboardPresenter keyboardPresenter();

    ToastPresenter toastPresenter();

    PackageManagerTools packageManagerTools();

    SharingDialogBuilder sharingDialogBuilder();

    ChooseDialogBuilder chooseDialogBuilder();

    SendFriendDialogBuilder sendFriendDialogBuilder();

    FileService fileService();

    NetworkState networkState();

    SharingService sharingService();

    LikeHideResult likeHideResult();

    NoInternetPresenter noInternetPresenter();

    WidgetController widgetController();

    OnGoBackPresenter onBackPresenter();

    LocalyticsController localyticsController();

    SocialPresenter vkpresenter();

    ApplicationSwitcher applicationSwitcher();

    OnBoardingDialogBuilder onBoardingDialogBuilder();
}
