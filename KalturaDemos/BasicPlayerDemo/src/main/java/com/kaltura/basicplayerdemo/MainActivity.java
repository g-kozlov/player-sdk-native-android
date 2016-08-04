package com.kaltura.basicplayerdemo;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.kaltura.playersdk.KPPlayerConfig;
import com.kaltura.playersdk.PlayerViewController;
import com.kaltura.playersdk.events.KPErrorEventListener;
import com.kaltura.playersdk.events.KPPlayheadUpdateEventListener;
import com.kaltura.playersdk.events.KPStateChangedEventListener;
import com.kaltura.playersdk.events.KPlayerState;
import com.kaltura.playersdk.tracks.KTrackActions;
import com.kaltura.playersdk.tracks.TrackFormat;
import com.kaltura.playersdk.tracks.TrackType;
import com.kaltura.playersdk.types.KPError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,KTrackActions.VideoTrackEventListener,KTrackActions.AudioTrackEventListener, KTrackActions.TextTrackEventListener,KTrackActions.EventListener, KPErrorEventListener, KPPlayheadUpdateEventListener, KPStateChangedEventListener /*--deprecated, KPEventListener*/ {
    private static final String TAG = "BasicPlayerDemo";

    private static final int MENU_GROUP_TRACKS = 1;
    private static final int TRACK_DISABLED = -1;
    private static final int ID_OFFSET = 2;
    private final String adUrl2 = "http://dpndczlul8yjf.cloudfront.net/creatives/assets/79dba610-b5ee-448b-8e6b-531b3d3ebd54/5fe7eb54-0296-4688-af06-9526007054a4.mp4";
    private final String adUrl = "http://dpndczlul8yjf.cloudfront.net/creatives/assets/c00cfcf0-985c-4d83-b32a-af8824025e9b/fa69a864-0e37-4597-b2f0-bdaceb16b56b.mp4";

    private Button mPlayPauseButton;
    private SeekBar mSeekBar;
    private PlayerViewController mPlayer;
    private boolean onCreate = false;
    private boolean enableBackgroundAudio;
    private Button videoButton;
    private Button audioButton;
    private Button textButton;

    private RelativeLayout.LayoutParams defaultVideoViewParams;
    private int defaultScreenOrientationMode;
    private ArrayList<String> mEntyIds;
    private ListIterator<String> mListIterator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setContentView(R.layout.activity_main);
        initEntryIds();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        videoButton = (Button) findViewById(R.id.video_controls);
        audioButton = (Button) findViewById(R.id.audio_controls);
        textButton = (Button) findViewById(R.id.text_controls);
        mPlayPauseButton = (Button)findViewById(R.id.button);
        mPlayPauseButton.setOnClickListener(this);
        mPlayPauseButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getPlayer().getMediaControl().replay();
                return true;
            }
        });
        findViewById(R.id.next_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Next selected");
                mPlayer.getMediaControl().pause();
                mPlayer.detachView();

                String entryId = getNextEntryId();
                try {
                    KPPlayerConfig config = KPPlayerConfig.fromJSONObject(new JSONObject(getJson(Long.parseLong(entryId))));
                    mPlayer.changeConfiguration(config);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //mPlayer.re();
            }
        });
        mSeekBar = (SeekBar)findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);
        onCreate = true;
        getPlayer();
    }

    private void initEntryIds() {
        mEntyIds = new ArrayList<>();
        mEntyIds.add("308649");
        mEntyIds.add("308650");
        //mEntyIds.add("435638");
        //mEntyIds.add("435616");
        mEntyIds.add("308651");
        mEntyIds.add("308652");
        mListIterator = mEntyIds.listIterator();
    }

    public String getNextEntryId() {
        if (mListIterator.hasNext()) {
            return mListIterator.next();
        }
        else {
            mListIterator = mEntyIds.listIterator();
            return mListIterator.next();
        }
    }

    private PlayerViewController getPlayer() {
        if (mPlayer == null) {
            mPlayer = (PlayerViewController)findViewById(R.id.player);
            mPlayer.loadPlayerIntoActivity(this);

            //KPPlayerConfig config = new KPPlayerConfig("http://kgit.html5video.org/tags/v2.46.rc6/mwEmbedFrame.php", "31638861", "1831271").setEntryId("1_ng282arr");
            //KPPlayerConfig config = new KPPlayerConfig("http://kgit.html5video.org/tags/v2.44/mwEmbedFrame.php", "12905712", "243342").setEntryId("0_uka1msg4");
            //mPlayPauseButton.setText("Pause");

            //config.addConfig("controlBarContainer.hover", "true");
            /*config.addConfig("closedCaptions.plugin", "true");
            config.addConfig("sourceSelector.plugin", "true");
            config.addConfig("sourceSelector.displayMode", "bitrate");
            config.addConfig("audioSelector.plugin", "true");
            config.addConfig("closedCaptions.showEmbeddedCaptions", "true");*/

            String json = getJson(Long.parseLong(getNextEntryId()));

            KPPlayerConfig config = null;
            try {
                config = KPPlayerConfig.fromJSONObject(new JSONObject(json));

                config.addConfig("topBarContainer.hover", "true");
                //config.addConfig("autoPlay", "true");
                config.addConfig("controlBarContainer.plugin", "true");
                config.addConfig("durationLabel.prefix", " ");
                config.addConfig("largePlayBtn.plugin", "true");
                //        config.addConfig("mediaProxy.mediaPlayFrom", String.valueOf("100"));
                config.addConfig("scrubber.sliderPreview", "false");
                //config.addConfig("largePlayBtn","false");
                //config.addConfig("debugKalturaPlayer", "true");
                config.addConfig("EmbedPlayer.HidePosterOnStart", "true");
                mPlayer.setKDPAttribute("nextBtnComponent", "visible", "false");
                mPlayer.setKDPAttribute("prevBtnComponent", "visible", "false");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            config.setAutoPlay(true);
            mPlayer.initWithConfiguration(config);
            mPlayer.setCustomSourceURLProvider(new PlayerViewController.SourceURLProvider() {
                @Override
                public String getURL(String entryId, String currentURL) {
                    return adUrl2;
                }
            });
            mPlayer.setOnKPErrorEventListener(this);
            mPlayer.setOnKPPlayheadUpdateEventListener(this);
            //mPlayer.setOnKPFullScreenToggeledEventListener(this);
            mPlayer.setOnKPStateChangedEventListener(this);

            /****FOR TRACKS****/
            //// Tracks on Web supported only from 2.44
            //// if TracksEventListener  is removed the tracks will be pushed to the web layer o/w app controled via
            ////onTracksUpdate and the mPlayer.getTrackManager() methodes
            //mPlayer.setTracksEventListener(this);
            //mPlayer.setVideoTrackEventListener(this);
            //mPlayer.setTextTrackEventListener(this);
            //mPlayer.setAudioTrackEventListener(this);
        }
        return mPlayer;
    }

    public String getJson(long mediaID) {
        String json = "{\n" +
                "  \"base\": {\n" +
                "    \"server\": \"http://52.17.68.92/DVV/v2.45/mwEmbed/mwEmbedFrame.php\n\",\n" +
                "    \"partnerId\": \"\",\n" +
                "    \"uiConfId\": \"35629551\",\n" +

                "    \"entryId\": \"" + mediaID + "\"\n" +
                "  },\n" +
                "  \"extra\": {\n" +
                "    \"controlBarContainer.hover\": true,\n" +
                "    \"controlBarContainer.plugin\": true,\n" +
                "    \"kidsPlayer.plugin\": true,\n" +
                "    \"nextBtnComponent.plugin\": true,\n" +
                "    \"prevBtnComponent.plugin\": true,\n" +
                "    \n" +
                "    \"liveCore.disableLiveCheck\": true,\n" +
                "    \"tvpapiGetLicensedLinks.plugin\": true,\n" +
                "    \"TVPAPIBaseUrl\": \"http://tvpapi-stg.as.tvinci.com/v3_9/gateways/jsonpostgw.aspx?m=\",\n" +
                "    \"proxyData\": {\n";

        json = json + "      \"MediaID\": \"" + mediaID + "\",\n" +
                "      \"iMediaID\": \"" + mediaID + "\",\n" +
                "      \"mediaType\": \"0\",\n" +
                "      \"picSize\": \"640x360\",\n" +
                "      \"withDynamic\": \"false\",\n" +
                "      \"initObj\": {\n" +
                "        \"ApiPass\": \"11111\",\n" +
                "        \"ApiUser\": \"tvpapi_394\",\n" +
                "        \"DomainID\": 0,\n" +
                "        \"Locale\": {\n" +
                "            \"LocaleCountry\": \"null\",\n" +
                "            \"LocaleDevice\": \"null\",\n" +
                "            \"LocaleLanguage\": \"null\",\n" +
                "            \"LocaleUserState\": \"Unknown\"\n" +
                "        },\n" +
                "        \"Platform\": \"Cellular\",\n" +
                "        \"SiteGuid\": \"USER_ID\",\n" +
                "        \"UDID\": \"aa5e1b6c96988d68\"\n" +
                "      }\n" +
                "    },\n" +
                " \"streamerType\": \"auto\",\n" +
                " \"EmbedPlayer.NotPlayableDownloadLink\" : \"false\",\n" +
                " \"autoPlay\": \"true\"\n" +
                "  }\n" +
                "}\n";
        return json;
    }

    private RelativeLayout getPlayerContainer() {
        return (RelativeLayout)findViewById(R.id.playerContainer);
    }

    @Override
    protected void onPause() {
        if (mPlayer != null) {
            mPlayer.releaseAndSavePosition(true);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        if (onCreate) {
            onCreate = false;
        }
        if (mPlayer != null) {
            mPlayer.resumePlayer();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mPlayer != null) {
            mPlayer.removePlayer();
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Timer swapTimer = new Timer();
        swapTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) getPlayerContainer().getLayoutParams();
                        lp.weight = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 8;
                        lp.height = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT ? 7 : 3;
                        getPlayerContainer().setLayoutParams(lp);
                    }
                });
            }
        }, 100);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.replay) {
            if (mPlayPauseButton.getText().equals("Play")) {
                mPlayPauseButton.setText("Pause");
                getPlayer().getMediaControl().start();
            } else {
                mPlayPauseButton.setText("Play");
                getPlayer().getMediaControl().pause();
            }
        } else {
            mPlayer.getMediaControl().replay();
            mPlayPauseButton.setText("Pause");
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            float progressInPercent = progress / 100f;
            float seekVal = (float) (progressInPercent * mPlayer.getDurationSec());
            getPlayer().getMediaControl().seek(seekVal);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onKPlayerStateChanged(PlayerViewController playerViewController, KPlayerState state) {
        if (state == KPlayerState.PAUSED && playerViewController.getCurrentPlaybackTime() > 0) {
//            findViewById(R.id.replay).setVisibility(View.VISIBLE);
            mPlayPauseButton.setText("Play");
        } else if (state == KPlayerState.PLAYING) {
//            findViewById(R.id.replay).setVisibility(View.INVISIBLE);
            mPlayPauseButton.setText("Pause");
        }
    }

    @Override
    public void onKPlayerError(PlayerViewController playerViewController, KPError error) {
        Log.d(TAG, "onKPlayerError Error Received:" + error.getErrorMsg());
    }


//    @Override
//    public void onKPlayerFullScreenToggeled(PlayerViewController playerViewController, boolean isFullscreen) {
//        Log.d(TAG, "onKPlayerFullScreenToggeled isFullscreen " + isFullscreen);
//    }


    @Override
    public void onKPlayerPlayheadUpdate(PlayerViewController playerViewController, long currentTime) {
        long currentSeconds = (int) (currentTime / 1000);
        long totalSeconds = (int) (playerViewController.getDurationSec());

        double percentage = 0;
        if (totalSeconds > 0) {
            percentage = (((double) currentSeconds) / totalSeconds) * 100;
        }
        Log.d(TAG, "onKPlayerPlayheadUpdate " +  currentSeconds + "/" + totalSeconds + " => " + (int)percentage + "%");
        mSeekBar.setProgress((int)percentage);
    }

    private void configurePopupWithTracks(PopupMenu popup,
                                          final PopupMenu.OnMenuItemClickListener customActionClickListener,
                                          final TrackType trackType) {
        int trackCount = 0;
        if (mPlayer == null || mPlayer.getTrackManager() == null) {
            return;
        }
        if (TrackType.AUDIO.equals(trackType)) {
            trackCount = mPlayer.getTrackManager().getAudioTrackList().size();
        }else if (TrackType.TEXT.equals(trackType)) {
            trackCount = mPlayer.getTrackManager().getTextTrackList().size();
        } else if (TrackType.VIDEO.equals(trackType)) {
            trackCount = mPlayer.getTrackManager().getVideoTrackList().size();
        }
        if (trackCount <= 0) {
            return;
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return (customActionClickListener != null
                        && customActionClickListener.onMenuItemClick(item))
                        || onTrackItemClick(item, trackType);
            }
        });
        Menu menu = popup.getMenu();
        // ID_OFFSET ensures we avoid clashing with Menu.NONE (which equals 0).
        menu.add(MENU_GROUP_TRACKS, TRACK_DISABLED + ID_OFFSET, Menu.NONE, R.string.off);

        for (int i = 0; i < trackCount; i++) {

            if (TrackType.AUDIO.equals(trackType)) {
                menu.add(MENU_GROUP_TRACKS, i + ID_OFFSET, Menu.NONE,
                        mPlayer.getTrackManager().getAudioTrackList().get(i).trackLabel);
            }else if (TrackType.TEXT.equals(trackType)) {
                menu.add(MENU_GROUP_TRACKS, i + ID_OFFSET, Menu.NONE,
                        mPlayer.getTrackManager().getTextTrackList().get(i).trackLabel);
            } else if (TrackType.VIDEO.equals(trackType)) {
                menu.add(MENU_GROUP_TRACKS, i + ID_OFFSET, Menu.NONE,
                        mPlayer.getTrackManager().getVideoTrackList().get(i).trackLabel);
            }

        }
        menu.setGroupCheckable(MENU_GROUP_TRACKS, true, true);
        menu.findItem(mPlayer.getTrackManager().getCurrentTrack(trackType).index + ID_OFFSET).setChecked(true);
    }

    private boolean onTrackItemClick(MenuItem item, TrackType type) {
        if (mPlayer == null || item.getGroupId() != MENU_GROUP_TRACKS) {
            return false;
        }

        int switchTrackIndex = item.getItemId() - ID_OFFSET;
        Log.d(TAG, "onTrackItemClick switchTrackIndex: " + switchTrackIndex);
        mPlayer.getTrackManager().switchTrack(type, switchTrackIndex);

        return true;
    }

    public void showVideoPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        configurePopupWithTracks(popup, null,TrackType.VIDEO);
        popup.show();
    }

    public void showAudioPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        Menu menu = popup.getMenu();
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.enable_background_audio);
        final MenuItem backgroundAudioItem = menu.findItem(0);
        backgroundAudioItem.setCheckable(true);
        backgroundAudioItem.setChecked(enableBackgroundAudio);
        PopupMenu.OnMenuItemClickListener clickListener = new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item == backgroundAudioItem) {
                    enableBackgroundAudio = !item.isChecked();
                    return true;
                }
                return false;
            }
        };
        configurePopupWithTracks(popup, clickListener, TrackType.AUDIO);
        popup.show();
    }

    public void showTextPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        configurePopupWithTracks(popup, null, TrackType.TEXT);
        popup.show();
    }



    @Override
    public void onTracksUpdate(KTrackActions tracksManager) {
        if (mPlayer != null) {
            updateButtonVisibilities();
            Log.e(TAG, "----------------");
            for (TrackFormat track : mPlayer.getTrackManager().getAudioTrackList()) {
                Log.d(TAG, track.toString());
            }
            Log.e(TAG, "----------------");
            for (TrackFormat track : mPlayer.getTrackManager().getVideoTrackList()) {
                Log.e(TAG, track.toString());
            }
            Log.e(TAG, "----------------");
            for (TrackFormat track : mPlayer.getTrackManager().getTextTrackList()) {
                Log.d(TAG, track.toString());
            }
            Log.e(TAG, "----------------");
        }
    }

    private void updateButtonVisibilities() {
        if (mPlayer != null) {
            if (mPlayer.getTrackManager() != null) {
                videoButton.setVisibility((mPlayer.getTrackManager().getVideoTrackList().size() > 0) ? View.VISIBLE : View.GONE);
                audioButton.setVisibility((mPlayer.getTrackManager().getAudioTrackList().size() > 0) ? View.VISIBLE : View.GONE);
                textButton.setVisibility((mPlayer.getTrackManager().getTextTrackList().size() > 0) ? View.VISIBLE : View.GONE);
            }
        }
    }

    @Override
    public void onVideoTrackChanged(int currentTrack) {
        Log.d(TAG, "** onVideoTrackChanged ** " + currentTrack);
    }

    @Override
    public void onTextTrackChanged(int currentTrack) {
        Log.d(TAG, "** onTextTrackChanged ** " + currentTrack);
    }

    @Override
    public void onAudioTrackChanged(int currentTrack) {
        Log.d(TAG, "** onAudioTrackChanged ** " + currentTrack);

    }
}
