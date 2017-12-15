package com.mobstac.beaconstacdemo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.mobstac.beaconstac.utils.MSLogger;

/**
 * Created by kislaykishore on 19/08/15.
 */
public class YoutubePlayerDialog extends DialogFragment {

    Context mContext;

    private String VIDEO_ID, API_KEY = "AIzaSyCc56j2m22CNQY4qfmgWdhXzH_rVgCXA0I";

    private YouTubeThumbnailView youTubeThumbnailView;
    private TextView errorText, closeDialog, okDialog;
    private ProgressBar loading;
    private ImageView playButton;

    public YoutubePlayerDialog() {

    }

    /**
     * Returns a new Instance of youtubePlayerDialog
     *
     * @param youtubeId Youtube ID of the video
     */

    public static YoutubePlayerDialog newInstance(String youtubeId, String ok_label, String ok_action) {
        YoutubePlayerDialog youtubePlayerDialog = new YoutubePlayerDialog();
        Bundle args = new Bundle();
        args.putString("ytId", youtubeId);
        args.putString("ok_label", ok_label);
        args.putString("ok_action", ok_action);
        youtubePlayerDialog.setArguments(args);
        return youtubePlayerDialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final String ok_label = getArguments().getString("ok_label");
        final String ok_action = getArguments().getString("ok_action");
        View rootView;
        if (ok_label.equals("")) {
            rootView = inflater.inflate(R.layout.dialog_youtube_popup, container, false);
        } else {
            rootView = inflater.inflate(R.layout.dialog_youtube_popup_notification, container, false);
        }

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);

        mContext = getActivity();
        errorText = (TextView) rootView.findViewById(R.id.youtube_error);
        closeDialog = (TextView) rootView.findViewById(R.id.youtube_dialog_close);
        loading = (ProgressBar) rootView.findViewById(R.id.youtube_loading);
        playButton = (ImageView) rootView.findViewById(R.id.youtube_play_button);

        VIDEO_ID = getArguments().getString("ytId");

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

        youTubeThumbnailView = (YouTubeThumbnailView) rootView.findViewById(R.id.youtube_thumbnail);

        youTubeThumbnailView.initialize(API_KEY, new ThumbnailListener());

        return rootView;
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity
                    = (MainActivity) getActivity();
            mainActivity.setIsPopupVisible(false);
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity
                    = (MainActivity) getActivity();
            mainActivity.setIsPopupVisible(false);
        }
    }

    class ThumbnailListener implements
            YouTubeThumbnailView.OnInitializedListener,
            YouTubeThumbnailLoader.OnThumbnailLoadedListener {

        @Override
        public void onInitializationSuccess(
                YouTubeThumbnailView view, YouTubeThumbnailLoader loader) {
            loader.setOnThumbnailLoadedListener(this);
            loader.setVideo(VIDEO_ID);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = YouTubeStandalonePlayer.createVideoIntent(getActivity(),
                            API_KEY, VIDEO_ID);
                    startActivity(intent);
                    dismiss();
                }
            });

        }

        @Override
        public void onInitializationFailure(
                YouTubeThumbnailView view, YouTubeInitializationResult loader) {
            errorText.setText("Error while loading video");
        }

        @Override
        public void onThumbnailLoaded(YouTubeThumbnailView view, String videoId) {
            loading.setVisibility(View.GONE);
            playButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onThumbnailError(YouTubeThumbnailView view, YouTubeThumbnailLoader.ErrorReason errorReason) {
            errorText.setText("Error while loading video");
        }
    }

}