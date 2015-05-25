package com.ozm.rocks.ui.sharing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ozm.R;
import com.ozm.rocks.base.ActivityConnector;
import com.ozm.rocks.data.api.response.ImageResponse;
import com.ozm.rocks.util.PInfo;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SharingDialogBuilder extends ActivityConnector<Activity> {

    @InjectView(R.id.sharing_dialog_header_text)
    TextView headerText;
    @InjectView(R.id.sharing_dialog_header_image)
    ImageView headerImage;
    @InjectView(R.id.sharing_dialog_top)
    LinearLayout topContainer;
    @InjectView(R.id.sharing_dialog_list)
    ListView list;

    @OnClick(R.id.sharing_dialog_header_image)
    public void back() {
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
    }

    @Nullable
    private
    SharingDialogCallBack mCallBack;
    @Nullable
    private AlertDialog mAlertDialog;

    @Inject
    public SharingDialogBuilder() {

    }

    public void setCallback(SharingDialogCallBack callBack) {
        this.mCallBack = callBack;
    }

    public void openDialog(final ArrayList<PInfo> pInfos, final ImageResponse image) {
        final Activity activity = getAttachedObject();
        if (activity == null) return;
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        SharingDialogAdapter sharingDialogAdapter = new SharingDialogAdapter(activity);
        View mSharingPickDialog = layoutInflater.inflate(R.layout.main_sharing_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(layoutInflater.getContext());
        ButterKnife.inject(this, mSharingPickDialog);
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable = activity.getResources().getDrawable(
                    R.drawable.ic_action_back, null);
        } else {
            drawable = activity.getResources().getDrawable(
                    R.drawable.ic_action_back);
        }
        if (drawable != null) {
            drawable.setColorFilter(activity.getResources().getColor(R.color.icons), PorterDuff.Mode.SRC_ATOP);
        }
        headerImage.setImageDrawable(drawable);
        list.setAdapter(sharingDialogAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == list.getAdapter().getCount() - 3) {
                    if (mCallBack != null && mAlertDialog != null) {
                        mCallBack.hideImage(image);
                        mAlertDialog.dismiss();
                    }
                } else if (position == list.getAdapter().getCount() - 2) {
                    ClipboardManager clipboard = (ClipboardManager)
                            activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", image.url);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(activity.getApplicationContext(),
                            "Ссылка скопирована в буфер обмена",
                            Toast.LENGTH_SHORT).show();
                    if (mAlertDialog != null) {
                        mAlertDialog.dismiss();
                    }
                } else if (position == list.getAdapter().getCount() - 1) {
                    if (mCallBack != null && mAlertDialog != null) {
                        mCallBack.other(image);
                        mAlertDialog.dismiss();
                    }
                } else if (mCallBack != null && mAlertDialog != null) {
                    mCallBack.share(pInfos.get(position + 3), image);
                    mAlertDialog.dismiss();
                }
            }
        });
        PInfo pInfo = new PInfo("Hide", null);
        pInfos.add(pInfo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pInfo = new PInfo("Скопировать ссылку", activity.getResources().
                    getDrawable(R.drawable.ic_copy, null));
        } else {
            pInfo = new PInfo("Скопировать ссылку", activity.getResources().
                    getDrawable(R.drawable.ic_copy));
        }
        pInfos.add(pInfo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pInfo = new PInfo("Другое", activity.getResources().getDrawable(R.drawable.ic_other, null));
        } else {
            pInfo = new PInfo("Другое", activity.getResources().getDrawable(R.drawable.ic_other));
        }

        pInfos.add(pInfo);

        for (int i = 0; i < pInfos.size(); i++) {
            if (i < 3 && i < pInfos.size() - 3) {
                ImageView imageView = new ImageView(activity);
                imageView.setImageDrawable(pInfos.get(i).getIcon());
                topContainer.addView(imageView);
                int padding = topContainer.getResources().getDimensionPixelSize(
                        R.dimen.sharing_dialog_top_element_padding);
                imageView.setPadding(padding, 0, padding, 0);
                final int finalI = i;
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCallBack != null && mAlertDialog != null) {
                            mCallBack.share(pInfos.get(finalI), image);
                            mAlertDialog.dismiss();
                        }
                    }
                });
            } else {
                sharingDialogAdapter.add(pInfos.get(i));
            }

        }
        builder.setView(mSharingPickDialog);
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    public interface SharingDialogCallBack {
        void share(PInfo pInfo, ImageResponse imageResponse);

        void hideImage(ImageResponse imageResponse);

        void other(ImageResponse imageResponse);
    }
}
