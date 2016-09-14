package com.kaltura.playersdk.interfaces;

import android.content.Context;

import com.google.android.gms.cast.framework.CastSession;
import com.kaltura.playersdk.casting.KCastDevice;

/**
 * Created by nissimpardo on 29/05/16.
 */
public interface KCastProvider {
    void startReceiver(Context context, boolean guestModeEnabled);
    void startReceiver(Context context);
    void disconnectFromCastDevice();
    KCastDevice getSelectedCastDevice();
    void setKCastProviderListener(KCastProviderListener listener);
    KCastMediaRemoteControl getCastMediaRemoteControl();
    boolean isConnected();

    interface KCastProviderListener {
        void onCastMediaRemoteControlReady(KCastMediaRemoteControl castMediaRemoteControl);
        //void onDeviceCameOnline(KCastDevice device);
        //void onDeviceWentOffline(KCastDevice device);
        //void onDeviceConnected();
        //void onDeviceDisconnected();

    }
}
