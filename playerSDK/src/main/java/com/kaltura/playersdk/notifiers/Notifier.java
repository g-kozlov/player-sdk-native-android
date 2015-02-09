package com.kaltura.playersdk.notifiers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;

/**
 * Created by itayi on 2/6/15.
 */
public class Notifier {
    private final static String TAG = Notifier.class.getSimpleName();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void notifyKPlayer(final String action, final Object[] eventValues, final WebView webView) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                String values = "";

                if (eventValues != null) {
                    for (int i = 0; i < eventValues.length; i++) {
                        if (eventValues[i] instanceof String) {
                            values += "'" + eventValues[i] + "'";
                        } else {
                            values += eventValues[i].toString();
                        }
                        if (i < eventValues.length - 1) {
                            values += ", ";
                        }
                    }
                    // values = TextUtils.join("', '", eventValues);
                }
                if (webView != null) {
                    Log.d(TAG, "NotifyKplayer: " + values);
                    webView.loadUrl("javascript:NativeBridge.videoPlayer."
                            + action + "(" + values + ");");
                }

            }
        });
    }
}
