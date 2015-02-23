package com.kaltura.playersdk.events;

import android.util.Log;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.kaltura.playersdk.notifiers.Notifier;

/**
 * Created by itayi on 1/28/15.
 */
public abstract class Listener {

    private static String TAG;
    protected EventType mEventType;
    private boolean mShouldNotifyKPlayer = false;
    private WebView mWebView;

    final public void executeCallback(InputObject inputObject)
    {
        if(checkValidInputObjectType(inputObject)){
            if(mShouldNotifyKPlayer){
                Notifier.notifyKPlayer("trigger", new Object[]{mEventType.getNotifierLabel(), inputObject}, mWebView);
            }
            executeInternalCallback(inputObject);
        }else{
            Log.e(TAG, "Received wrong inputObject type");
        }
    }

    abstract protected void executeInternalCallback(InputObject inputObject);
    abstract protected boolean checkValidInputObjectType(InputObject inputObject);

    public EventType getEventType(){
        return mEventType;
    }

    abstract protected void setEventType();

    public Listener(){
        TAG = this.getClass().getSimpleName();
        setEventType();
    }

    public Listener setShouldAutoNotifyKPlayer(boolean shouldNotify, WebView webView){
        mShouldNotifyKPlayer = shouldNotify;
        mWebView = webView;
        return this;
    }

    public abstract static class InputObject{
        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }

    public static enum EventType{
        JS_CALLBACK_READY_LISTENER_TYPE(""),
        AUDIO_TRACKS_LIST_LISTENER_TYPE(""),
        AUDIO_TRACK_SWITCH_LISTENER_TYPE(""),
        CAST_DEVICE_CHANGE_LISTENER_TYPE(""),
        CAST_ROUTE_DETECTED_LISTENER_TYPE(""),
        ERROR_LISTENER_TYPE("error"),
        PLAYER_STATE_CHANGE_LISTENER_TYPE(""),
        PLAYHEAD_UPDATE_LISTENER_TYPE("timeupdate"),
        PROGRESS_UPDATE_LISTENER_TYPE("progress"),
        QUALITY_SWITCHING_LISTENER_TYPE(""),
        QUALITY_TRACKS_LIST_LISTENER_TYPE(""),
        TEXT_TRACK_CHANGE_LISTENER_TYPE(""),
        TEXT_TRACK_LIST_LISTENER_TYPE("textTracksReceived"),
        TEXT_TRACK_TEXT_LISTENER_TYPE("loadEmbeddedCaptions"),
        TOGGLE_FULLSCREEN_LISTENER_TYPE(""),
        WEB_VIEW_MINIMIZE_LISTENER_TYPE(""),
        KPLAYER_EVENT_LISTENER_TYPE(""),
        DURATION_CHANGED_LISTENER_TYPE("");

        private final String notifyLabel;

        EventType(String notifyLabel){
            this.notifyLabel = notifyLabel;
        }

        public String getNotifierLabel(){
            return notifyLabel;
        }

    }
}
