package com.ozm.rocks.ui.screen.sharing;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.ozm.R;
import com.ozm.rocks.OzomeComponent;
import com.ozm.rocks.base.HasComponent;
import com.ozm.rocks.base.mvp.BasePresenter;
import com.ozm.rocks.base.mvp.BaseView;
import com.ozm.rocks.base.navigation.activity.ActivityScreen;
import com.ozm.rocks.base.navigation.activity.ActivityScreenSwitcher;
import com.ozm.rocks.data.DataService;
import com.ozm.rocks.data.RequestResultCodes;
import com.ozm.rocks.data.analytics.LocalyticsController;
import com.ozm.rocks.data.api.model.Config;
import com.ozm.rocks.data.api.response.GifMessengerOrder;
import com.ozm.rocks.data.api.response.ImageResponse;
import com.ozm.rocks.data.api.response.MessengerConfigs;
import com.ozm.rocks.data.api.response.PackageRequest;
import com.ozm.rocks.data.rx.RequestFunction;
import com.ozm.rocks.data.social.SocialActivity;
import com.ozm.rocks.util.PInfo;
import com.ozm.rocks.util.PackageManagerTools;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SharingActivity extends SocialActivity implements HasComponent<SharingComponent> {
    @Inject
    Presenter presenter;

    @Inject
    SharingDialogBuilder sharingDialogBuilder;

    @Inject
    ChooseDialogBuilder chooseDialogBuilder;

    private ImageResponse imageResponse;
    private int from;
    private SharingComponent component;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_App);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    protected void onExtractParams(@NonNull Bundle params) {
        super.onExtractParams(params);
        imageResponse = params.getParcelable(Screen.BF_IMAGE);
        from = params.getInt(Screen.BF_FROM);
    }

    @Override
    protected void onCreateComponent(OzomeComponent ozomeComponent) {
        component = DaggerSharingComponent.builder().
                ozomeComponent(ozomeComponent).
                sharingModule(new SharingModule(imageResponse, from)).build();
        component.inject(this);
    }

    @Override
    protected void onStart() {
        sharingDialogBuilder.attach(this);
        chooseDialogBuilder.attach(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sharingDialogBuilder.detach();
        chooseDialogBuilder.detach();
    }

    @Override
    protected void onDestroy() {
        component = null;
        super.onDestroy();
    }

    @Override
    protected int layoutId() {
        return R.layout.sharing_view;
    }

    @Override
    protected BasePresenter<? extends BaseView> presenter() {
        return presenter;
    }

    @Override
    protected int viewId() {
        return R.id.sharing_view;
    }

    @Override
    public SharingComponent getComponent() {
        return component;
    }

    @SharingScope
    public static final class Presenter extends BasePresenter<SharingView> {
        private static final String SR_IMAGE_KEY = "SharingActivity.image";
        private static final String SR_FROM_KEY = "SharingActivity.from";
        private static final String SR_PACKAGES_KEY = "SharingActivity.packages";
        private static final String SR_VIEW_PACKAGES_KEY = "SharingActivity.viewPackages";

        private final DataService dataService;
        private final ActivityScreenSwitcher screenSwitcher;
        private final Application application;
        private ImageResponse imageResponse;
        private final SharingService sharingService;
        private final LocalyticsController localyticsController;
        private final ChooseDialogBuilder chooseDialogBuilder;
        private int from;
        private ArrayList<PInfo> packages;
        private ArrayList<PInfo> viewPackages;

        @Nullable
        private CompositeSubscription subscriptions;

        @Inject
        public Presenter(DataService dataService, ActivityScreenSwitcher screenSwitcher,
                         SharingService sharingService, @Named("sharingImage") ImageResponse imageResponse,
                         LocalyticsController localyticsController, @Named("sharingFrom") int from,
                         ChooseDialogBuilder chooseDialogBuilder, Application application) {
            this.dataService = dataService;
            this.screenSwitcher = screenSwitcher;
            this.sharingService = sharingService;
            this.imageResponse = imageResponse;
            this.localyticsController = localyticsController;
            this.from = from;
            this.chooseDialogBuilder = chooseDialogBuilder;
            this.application = application;
        }

        @Override
        protected void onLoad() {
            super.onLoad();
            subscriptions = new CompositeSubscription();
            getViewPackages();
        }

        private void getViewPackages() {
            if (!checkView()) {
                return;
            }
            if (subscriptions == null){
                subscriptions = new CompositeSubscription();
            }
            if (viewPackages != null && imageResponse != null) {
                getView().setData((ArrayList<PInfo>) viewPackages.clone());
                return;
            }
            subscriptions.add(dataService.getPackages()
                    .flatMap(new Func1<ArrayList<PInfo>, Observable<Config>>() {
                        @Override
                        public Observable<Config> call(ArrayList<PInfo> pInfos) {
                            packages = pInfos;
                            return dataService.getConfigFromPreferences();
                        }
                    }).flatMap(new Func1<Config, Observable<ArrayList<PInfo>>>() {
                        @Override
                        public Observable<ArrayList<PInfo>> call(final Config config) {
                            return Observable.create(new RequestFunction<ArrayList<PInfo>>() {
                                @Override
                                protected ArrayList<PInfo> request() {
                                    ArrayList<PInfo> pInfos = new ArrayList<>();
                                    if (imageResponse.isGIF) {
                                        for (GifMessengerOrder mc : config.gifMessengerOrders()) {
                                            for (PInfo p : packages) {
                                                if (mc.applicationId.equals(p.getPackageName())
                                                        && !mc.applicationId.equals(
                                                        PackageManagerTools.Messanger.FACEBOOK_MESSANGER.getPackagename())
                                                        && !mc.applicationId.equals(
                                                        PackageManagerTools.Messanger.VKONTAKTE.getPackagename())) {
                                                    pInfos.add(p);
                                                }
                                                if (pInfos.size() >= 3) {
                                                    break;
                                                }
                                            }
                                            if (pInfos.size() >= 3) {
                                                break;
                                            }
                                        }
                                    } else {
                                        for (MessengerConfigs mc : config.messengerConfigs()) {
                                            for (PInfo p : packages) {
                                                if (mc.applicationId.equals(p.getPackageName())
                                                        && !mc.applicationId.equals(
                                                        PackageManagerTools.Messanger.FACEBOOK_MESSANGER.getPackagename())
                                                        && !mc.applicationId.equals(
                                                        PackageManagerTools.Messanger.VKONTAKTE.getPackagename())) {
                                                    pInfos.add(p);
                                                }
                                                if (pInfos.size() >= 3) {
                                                    break;
                                                }
                                            }
                                            if (pInfos.size() >= 3) {
                                                break;
                                            }
                                        }
                                    }
                                    return pInfos;
                                }
                            });
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<ArrayList<PInfo>>() {
                        @Override
                        public void call(ArrayList<PInfo> pInfos) {
                            viewPackages = pInfos;
                            getView().setData((ArrayList<PInfo>) pInfos.clone());
                        }
                    }));
        }

        public void sendPackages(PackageRequest.VkData vkData) {
            sharingService.sendPackages(vkData, null);
        }

        public void share(PInfo pInfo) {
            sharingService.saveImageFromCacheAndShare(pInfo, imageResponse, from)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
            setShareResult();
        }

        public void shareVK(final VKApiUser user) {
            VKRequest.VKRequestListener vkRequestListener = new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    Intent startBrowser = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://vk.com/im"));
                    startBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    application.startActivity(startBrowser);
                }

                @Override
                public void onError(VKError error) {
                    super.onError(error);
                    Toast.makeText(application, R.string.error_information_repeate_please, Toast.LENGTH_SHORT).show();
                }
            };
            sharingService.shareToVk(imageResponse, user, vkRequestListener, from)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {

                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Toast.makeText(application, R.string.error_information_repeate_please, Toast.LENGTH_SHORT).show();
                        }
                    });
            setShareResult();
        }

        public void shareFB() {
            sharingService.shareToFb(imageResponse, from)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
            setShareResult();
        }

        public void shareVKAll() {
            for (PInfo pInfo : packages) {
                if (pInfo.getPackageName().equals(PackageManagerTools.Messanger.VKONTAKTE.getPackagename())) {
                    sharingService.saveImageFromCacheAndShare(pInfo, imageResponse, from)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe();
                    break;
                }
            }
            setShareResult();
        }

        public void shareOther() {
            sharingService.shareWithChooser(imageResponse, from);
            setShareResult();
        }

        private void setShareResult() {
            Intent data = new Intent();
            data.putExtra(RequestResultCodes.IMAGE_RESPONSE_KEY, imageResponse);
            screenSwitcher.setResult(RequestResultCodes.RESULT_CODE_SHARE_IMAGE, data);
        }

        public void like() {
            if (subscriptions == null) {
                return;
            }
            sharingService.sendActionLikeDislike(from, imageResponse);
            Intent data = new Intent();
//            Bundle bundle = new Bundle();
//            bundle.putParcelable(RequestResultCodes.IMAGE_RESPONSE_KEY, imageResponse);
//            ImageResponse dataImage = bundle.getParcelable(RequestResultCodes.IMAGE_RESPONSE_KEY);
//            dataImage.liked = !dataImage.liked;
            data.putExtra(RequestResultCodes.IMAGE_RESPONSE_KEY, imageResponse);
            screenSwitcher.setResult(RequestResultCodes.RESULT_CODE_LIKE_IMAGE, data);
        }

        public void hide() {
            sharingService.sendActionHide(from, imageResponse);
            Intent data = new Intent();
            data.putExtra(RequestResultCodes.IMAGE_RESPONSE_KEY, imageResponse);
            screenSwitcher.goBackResult(RequestResultCodes.RESULT_CODE_HIDE_IMAGE, data);
        }

        public ArrayList<PInfo> getPackages() {
            return packages;
        }

        public ImageResponse getImageResponse() {
            return imageResponse;
        }

        public void setImageResponse(ImageResponse imageResponse) {
            this.imageResponse = imageResponse;
        }

        @Override
        protected void onRestore(@NonNull Bundle savedInstanceState) {
            super.onRestore(savedInstanceState);
            imageResponse = savedInstanceState.getParcelable(SR_IMAGE_KEY);
            from = savedInstanceState.getInt(SR_FROM_KEY);
            packages = savedInstanceState.getParcelableArrayList(SR_PACKAGES_KEY);
            viewPackages = savedInstanceState.getParcelable(SR_VIEW_PACKAGES_KEY);
        }

        @Override
        protected void onSave(@NonNull Bundle outState) {
            outState.putParcelable(SR_IMAGE_KEY, imageResponse);
            outState.putInt(SR_FROM_KEY, from);
            outState.putParcelableArrayList(SR_PACKAGES_KEY, packages);
            outState.putParcelableArrayList(SR_VIEW_PACKAGES_KEY, viewPackages);
            super.onSave(outState);
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

    public static final class Screen extends ActivityScreen {
        public static final String BF_IMAGE = "SharingActivity.image";
        public static final String BF_FROM = "SharingActivity.from";
        private final ImageResponse imageResponse;
        private final int from;

        public Screen(ImageResponse imageResponse, @SharingService.From int from) {
            this.imageResponse = imageResponse;
            this.from = from;
        }

        @Override
        protected void configureIntent(@NonNull Intent intent) {
            intent.putExtra(BF_IMAGE, imageResponse);
            intent.putExtra(BF_FROM, from);
        }

        @Override
        protected Bundle activityOptions(Activity activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                return ActivityOptions.makeCustomAnimation(activity.getApplicationContext(),
                        R.anim.sharing_view_in, R.anim.sharing_view_out).toBundle();
            } else {
                return super.activityOptions(activity);
            }

        }

        @Override
        protected Class<? extends Activity> activityClass() {
            return SharingActivity.class;
        }
    }
}
