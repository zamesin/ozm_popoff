package com.ozm.rocks;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.ozm.BuildConfig;
import com.ozm.R;
import com.ozm.rocks.ui.ActivityHierarchyServer;

import org.jraf.android.util.activitylifecyclecallbackscompat.ApplicationHelper;

import javax.inject.Inject;

import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class OzomeDependency extends Application {
    private OzomeComponent component;

    @Inject
    ActivityHierarchyServer activityHierarchyServer;

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);

        CalligraphyConfig.initDefault(
                new CalligraphyConfig.Builder().
                        setDefaultFontPath("fonts/roboto_regular.ttf").
                        setFontAttrId(R.attr.fontPath).
                        build()
        );
//
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
//        } else {
        // TODO Crashlytics.start(this);
        // TODO Timber.plant(new CrashlyticsTree());
//        }

//        JodaTimeAndroid.init(this);

        buildComponentAndInject();

        ApplicationHelper.registerActivityLifecycleCallbacks(this, activityHierarchyServer);
    }

    public void buildComponentAndInject() {
        component = OzomeComponent.Initializer.init(this);
        component.inject(this);
    }

    public OzomeComponent component() {
        return component;
    }

    public static OzomeDependency get(Context context) {
        return (OzomeDependency) context.getApplicationContext();
    }
}
