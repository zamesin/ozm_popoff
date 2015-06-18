package com.ozm.rocks.ui.main;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.ozm.R;
import com.ozm.rocks.base.ComponentFinder;
import com.ozm.rocks.base.mvp.BaseView;
import com.ozm.rocks.data.analytics.LocalyticsController;
import com.ozm.rocks.util.view.SlidingTabLayout;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainView extends LinearLayout implements BaseView {

    @Inject
    MainActivity.Presenter presenter;
    @Inject
    LocalyticsController localyticsController;

    @InjectView(R.id.pager)
    protected ViewPager mViewPager;

    @InjectView(R.id.tabs)
    protected SlidingTabLayout mSlidingTabLayout;

    private MainPagerAdapter2 mMainPagerAdapter;

    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            MainComponent component = ComponentFinder.findActivityComponent(context);
            component.inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);

        mMainPagerAdapter = new MainPagerAdapter2(getContext());
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mMainPagerAdapter);
        mViewPager.setOnPageChangeListener(new MainPagerAdapter.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset == .0f && positionOffsetPixels == 0) {
                    final MainScreens screen = MainScreens.getList().get(position);
                    if (screen == MainScreens.EMOTIONS_SCREEN) {
                        localyticsController.openCategories();
                    } else if (screen == MainScreens.FAVORITE_SCREEN) {
                        localyticsController.openFavorites();
                    } else if (screen == MainScreens.GENERAL_SCREEN) {
                        localyticsController.openFeed(LocalyticsController.TAB);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (mMainPagerAdapter.getItem(position).getResId() == MainScreens.FAVORITE_SCREEN.getResId()) {
                    presenter.updateMyFeed();
                }
                presenter.pageChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mMainPagerAdapter.addAll(MainScreens.getList());

        mSlidingTabLayout.setDistributeEvenly(true);
        // Setting Custom Color for the Scroll bar indicator of the Tab View
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.WHITE;
            }
        });
        // Setting the ViewPager For the SlidingTabsLayout
        mSlidingTabLayout.setViewPager(mViewPager);

    }

//    private void updateCurrentButton(int position) {
//        mScreenButtonsGroup.check(mScreenPagerAdapter.getItem(position).getButtonId());
//    }
//
//    private void initScreenButtons(List<MainScreens> screens) {
//        for (MainScreens screen : screens) {
//            RadioButtonCenter view = (RadioButtonCenter) layoutInflater.inflate(
//                    R.layout.radio_button_view, null);
//            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
//                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
////            view.setButtonDrawable(screen.getIconSelectorResId());
//            final Drawable drawable = ResourcesCompat.getDrawable(getResources(),
//                    screen.getIconSelectorResId(), getContext().getTheme());
//            view.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
//            view.setPadding(0, getResources().getDimensionPixelSize(R.dimen.tab_button_top_padding), 0, 0);
//            view.setLayoutParams(params);
//            view.setText(screen.getNameResId());
//            view.setId(screen.getButtonId());
//            mScreenButtonsGroup.addView(view);
//        }
//    }

    @Override
    public void showLoading() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showContent() {
    }

    @Override
    public void showError(Throwable throwable) {
    }

    public void showMainContent() {
    }

    public void openFirstScreen() {
        showMainContent();
        mViewPager.setCurrentItem(0, true);
    }
}
