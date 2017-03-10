package com.mobstac.beaconstacdemo;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

/**
 * Created by shashankraj on 01/02/17.
 *
 * Utility class for common repeatedly usable functions
 */

public class BeaconstacUtils {
    /**
     * Display a snackBar, threadsafe, can be called from a background thread
     *
     * @param message  Message to display in the toast
     * @param activity Calling activity's reference
     */
    public static void snackBar(final String message, final Activity activity,
                                final String textColorCode) {

        if (activity != null)
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Snackbar snackbar = Snackbar
                            .make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(Color.GRAY);

                    if (textColorCode != null) {
                        View snackBarView = snackbar.getView();
                        TextView textView = (TextView)
                                snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        textView.setTextColor(Color.parseColor(textColorCode));
                    }

                    snackbar.show();

                }
            });
    }

}
