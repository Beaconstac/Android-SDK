package com.mobstac.beaconstacdemo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.mobstac.beaconstac.utils.MSLogger;

import java.util.ArrayList;

/**
 * Created by kislaykishore on 19/08/15.
 */
public class ImageCarouselDialog extends DialogFragment {

    Context mContext;

    TextView titleView, textView, closeDialog, okDialog;
    String title, text;
    ArrayList<String> imageUrls;

    CustomViewPager imagePager;

    public ImageCarouselDialog() {

    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }

    /**
     * Returns a new Instance of ImageCarouselDialog
     *
     * @param title Title of dialog (pass null to hide title)
     * @param text  Summary of dialog (pass null to hide summary)
     * @param url   ArrayList containing URLs of images (pass null to hide images)
     */

    public static ImageCarouselDialog newInstance(String title, String text, ArrayList<String> url, String ok_label, String ok_action) {
        ImageCarouselDialog imageCarouselDialog = new ImageCarouselDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("text", text);
        args.putString("ok_label", ok_label);
        args.putString("ok_action", ok_action);
        args.putStringArrayList("url", url);
        imageCarouselDialog.setArguments(args);
        return imageCarouselDialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final String ok_label = getArguments().getString("ok_label");
        final String ok_action = getArguments().getString("ok_action");
        View rootView;
        if (ok_label.equals("")) {
            rootView = inflater.inflate(R.layout.dialog_fragment_carousel, container, false);
        } else {
            rootView = inflater.inflate(R.layout.dialog_fragment_carousel_notification, container, false);
        }
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);

        mContext = getActivity();

        title = getArguments().getString("title");
        text = getArguments().getString("text");
        imageUrls = getArguments().getStringArrayList("url");

        closeDialog = (TextView) rootView.findViewById(R.id.dialog_close);
        titleView = (TextView) rootView.findViewById(R.id.carousel_title);
        textView = (TextView) rootView.findViewById(R.id.carousel_text);
        imagePager = (CustomViewPager) rootView.findViewById(R.id.image_pager);


        if (title == null)
            titleView.setVisibility(View.GONE);
        else
            titleView.setText(title);

        if (text == null)
            textView.setVisibility(View.GONE);
        else
            textView.setText(text);

        if (imageUrls == null)
            imagePager.setVisibility(View.GONE);
        else {
            PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager());
            imagePager.setAdapter(pagerAdapter);
        }

        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        if (!ok_label.equals("")) {
            okDialog = (TextView) rootView.findViewById(R.id.dialog_ok);
            okDialog.setText(ok_label);
            okDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    try {
                        Uri uri = Uri.parse(ok_action); // missing 'http://' will cause crashed
                        Intent openUrl = new Intent(Intent.ACTION_VIEW, uri);
                        openUrl.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().getApplicationContext().startActivity(openUrl);
                    } catch (Exception e) {
                        MSLogger.error("Cannot open this url");
                    }
                }


            });
        }
        return rootView;
    }


    public class PagerAdapter extends FragmentPagerAdapter {


        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return imageUrls.size();
        }

        @Override
        public Fragment getItem(int position) {
            return CarouselImageFragment.newInstance(imageUrls.get(position));
        }

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof MainActivity) {
            MainActivity experienceDemoActivity
                    = (MainActivity) getActivity();
            experienceDemoActivity.setIsPopupVisible(false);
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
        if (getActivity() instanceof MainActivity) {
            MainActivity experienceDemoActivity
                    = (MainActivity) getActivity();
            experienceDemoActivity.setIsPopupVisible(false);
        }
    }

}