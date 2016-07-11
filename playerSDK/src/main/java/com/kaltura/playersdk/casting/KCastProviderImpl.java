package com.kaltura.playersdk.casting;

import android.content.Context;
import android.media.RemoteControlClient;
import android.os.Bundle;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.LaunchOptions;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.kaltura.playersdk.cast.KRouterCallback;
import com.kaltura.playersdk.interfaces.KCastMediaRemoteControl;
import com.kaltura.playersdk.interfaces.ScanCastDeviceListener;
import com.kaltura.playersdk.players.KChromeCastPlayer;
import com.kaltura.playersdk.players.KPlayerListener;

import java.io.IOException;


/**
 * Created by nissimpardo on 29/05/16.
 */
public class KCastProviderImpl implements com.kaltura.playersdk.interfaces.KCastProvider, KRouterCallback.KRouterCallbackListener {
    private static final String TAG = "KCastProviderImpl";
    private String nameSpace = "urn:x-cast:com.kaltura.cast.player";
    private String mCastAppID;
    private ScanCastDeviceListener mScanCastDeviceListener;
    private KCastProviderListener mProviderListener;
    private Context mContext;

    private KCastKalturaChannel mChannel;
    private GoogleApiClient mApiClient;
    private CastDevice mSelectedDevice;
    private MediaRouter mRouter;
    private KRouterCallback mCallback;
    private MediaRouteSelector mSelector;

    private Cast.Listener mCastClientListener;
    private ConnectionCallbacks mConnectionCallbacks;
    private ConnectionFailedListener mConnectionFailedListener;

    private boolean mWaitingForReconnect = false;
    private boolean mApplicationStarted = false;
    private boolean mCastButtonEnabled = false;

    private KCastMediaRemoteControl mCastMediaRemoteControl;

    private String mSessionId;

    private InternalListener mInternalListener;


    public void setScanCastDeviceListener(ScanCastDeviceListener listener) {
        mScanCastDeviceListener = listener;
    }

    public GoogleApiClient getApiClient() {
        return mApiClient;
    }

    public interface InternalListener extends KCastMediaRemoteControl.KCastMediaRemoteControlListener {
        void onStartCasting(KChromeCastPlayer remoteMediaPlayer);
        void onCastStateChanged(String state);
        void onStopCasting();
    }

    public void setInternalListener(InternalListener internalListener) {
        mInternalListener = internalListener;
    }


    public KCastKalturaChannel getChannel() {
        return mChannel;
    }

    @Override
    public void setKCastButton(boolean enable) {
        mCastButtonEnabled = enable;
    }

    @Override
    public void startScan(Context context, String appID) {
        mContext = context;
        mCastAppID = appID;
        mRouter = MediaRouter.getInstance(mContext.getApplicationContext());
        mCallback = new KRouterCallback();
        mCallback.setListener(this);
        mSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent.categoryForCast(mCastAppID)).build();
        mRouter.addCallback(mSelector, mCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    @Override
    public void stopScan() {

    }

    @Override
    public void setPassiveScan(boolean passiveScan) {

    }

    @Override
    public void connectToDevice(KCastDevice device) {
        mCallback.setRouter(mRouter);
        MediaRouter.RouteInfo selectedRoute = mCallback.routeById(device.getRouterId());
        mRouter.selectRoute(selectedRoute);
//        if (mScanCastDeviceListener != null) {
//            mScanCastDeviceListener.onConnecting();
//        }
    }

    @Override
    public void disconnectFromDevcie() {
        if (mScanCastDeviceListener != null) {
            mScanCastDeviceListener.onDisconnectCastDevice();
        }
        mRouter.unselect(MediaRouter.UNSELECT_REASON_STOPPED);
        mSelectedDevice = null;
    }

    @Override
    public void setKCastProviderListener(KCastProviderListener listener) {
        mProviderListener = listener;
    }

    @Override
    public KCastMediaRemoteControl getCastMediaRemoteControl() {
        return mCastMediaRemoteControl;
    }

    private Cast.Listener getCastClientListener() {
        if (mCastClientListener == null) {
            mCastClientListener = new Cast.Listener() {
                @Override
                public void onApplicationStatusChanged() {
                    if (mApiClient != null) {
                        Log.d(TAG, "onApplicationStatusChanged: "
                                + Cast.CastApi.getApplicationStatus(mApiClient));
                        if (Cast.CastApi.getApplicationStatus(mApiClient) == "Ready to play" && mProviderListener != null) {
                            mProviderListener.onDeviceConnected();
                        }
                    }
                }

                @Override
                public void onApplicationMetadataChanged(ApplicationMetadata applicationMetadata) {
                }

                @Override
                public void onApplicationDisconnected(int statusCode) {
                    if (mProviderListener != null) {
                        mProviderListener.onDeviceDisconnected();
                    }
                    teardown();
                }

                @Override
                public void onActiveInputStateChanged(int activeInputState) {
                }

                @Override
                public void onStandbyStateChanged(int standbyState) {
                }

                @Override
                public void onVolumeChanged() {
                }
            };
        }
        return mCastClientListener;
    }

    private ConnectionCallbacks getConnectionCallbacks() {
        if (mConnectionCallbacks == null) {
            mConnectionCallbacks = new ConnectionCallbacks();
        }
        return mConnectionCallbacks;
    }

    private ConnectionFailedListener getConnectionFailedListener() {
        if (mConnectionFailedListener == null) {
            mConnectionFailedListener = new ConnectionFailedListener();
        }
        return mConnectionFailedListener;
    }

    private void teardown() {
        Log.d(TAG, "teardown");
        if (mApiClient != null) {
            if (mApplicationStarted) {
                if (mApiClient.isConnected() || mApiClient.isConnecting()) {
                    try {
                        Cast.CastApi.stopApplication(mApiClient, mSessionId);
                        if (mChannel != null) {
                            Cast.CastApi.removeMessageReceivedCallbacks(
                                    mApiClient,
                                    mChannel.getNamespace());
                            mChannel = null;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception while removing channel", e);
                    }
                    mApiClient.disconnect();
                }
                mApplicationStarted = false;
            }
            mApiClient = null;
        }
        mSelectedDevice = null;
        mWaitingForReconnect = false;
        mSessionId = null;
    }


    public void sendMessage(final String message) {
        if (mApiClient != null && mChannel != null) {
            try {
                Log.d("chromecast.sendMessage", "namespace: " + nameSpace + " message: " + message);
                Cast.CastApi.sendMessage(mApiClient, nameSpace, message)
                        .setResultCallback(
                                new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status result) {
                                        if (result.isSuccess()) {
                                            Log.d(TAG, "namespace:" + nameSpace + " message:" + message);
                                        } else {
                                            Log.e(TAG, "Sending message failed");
                                        }
                                    }
                                });
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message", e);
            }
        }
    }


    @Override
    public void onDeviceSelected(CastDevice castDeviceSelected) {
        if (castDeviceSelected != null) {
            mSelectedDevice = castDeviceSelected;
            Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                    .builder(castDeviceSelected, getCastClientListener());
            mApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(Cast.API, apiOptionsBuilder.build())
                    .addConnectionCallbacks(getConnectionCallbacks())
                    .addOnConnectionFailedListener(getConnectionFailedListener())
                    .build();
            mApiClient.connect();
        } else if (mProviderListener != null){
            teardown();
            mProviderListener.onDeviceDisconnected();
        }
    }

    @Override
    public void onRouteAdded(boolean isAdded, KCastDevice route) {
        if (isAdded) {
            mProviderListener.onDeviceCameOnline(route);
        } else {
            mProviderListener.onDeviceWentOffline(route);
        }
    }

    @Override
    public void onFoundDevices(boolean didFound) {
//        if (mCastButtonEnabled) {
//            mScanCastDeviceListener.onDevicesInRange(didFound);
//        }
    }


    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle bundle) {
            if (mWaitingForReconnect) {
                mWaitingForReconnect = false;

                // In case of kaltura receiver is loaded, open channel for sneding messages
            } else {

                try {
                    Cast.CastApi.launchApplication(mApiClient, mCastAppID, new LaunchOptions())
                            .setResultCallback(
                                    new ResultCallback<Cast.ApplicationConnectionResult>() {
                                        @Override
                                        public void onResult(Cast.ApplicationConnectionResult result) {
                                            Status status = result.getStatus();
                                            if (status.isSuccess()) {
                                                mSessionId = result.getSessionId();
//                                                mRemoteMediaPlayer = new RemoteMediaPlayer();

                                                // Prepare the custom channel (listens to Kaltura's receiver messages)
                                                mChannel = new KCastKalturaChannel(nameSpace, new KCastKalturaChannel.KCastKalturaChannelListener() {

                                                    @Override
                                                    public void readyForMedia(final String[] params) {
                                                        sendMessage("{\"type\":\"hide\",\"target\":\"logo\"}");
                                                        // Receiver send the new content
                                                        if (params != null) {
                                                            mCastMediaRemoteControl = new KChromeCastPlayer(mApiClient);
                                                            ((KChromeCastPlayer)mCastMediaRemoteControl).setMediaInfoParams(params);
                                                            mInternalListener.onStartCasting((KChromeCastPlayer)mCastMediaRemoteControl);
                                                        }
                                                    }
                                                });
                                                sendMessage("{\"type\":\"show\",\"target\":\"logo\"}");
                                                mApplicationStarted = true;
                                                try {
                                                    Cast.CastApi.setMessageReceivedCallbacks(mApiClient,
                                                            mChannel.getNamespace(),
                                                            mChannel);
                                                } catch (IOException e) {
                                                    Log.e(TAG, "Exception while creating channel", e);
                                                }
                                                mProviderListener.onDeviceConnected();
                                            } else {
                                                teardown();
                                            }
                                        }
                                    });

                } catch (Exception e) {
                    Log.d(TAG, "Failed to launch application", e);
                }
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            mWaitingForReconnect = true;
        }
    }

    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            teardown();
        }
    }
}
