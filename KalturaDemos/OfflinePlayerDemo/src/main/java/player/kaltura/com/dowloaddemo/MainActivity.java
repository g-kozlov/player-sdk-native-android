package player.kaltura.com.dowloaddemo;


import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.api.GoogleApiClient;
import com.kaltura.playersdk.KPPlayerConfig;
import com.kaltura.playersdk.LocalAssetsManager;
import com.kaltura.playersdk.PlayerViewController;
import com.kaltura.playersdk.casting.KCastDevice;
import com.kaltura.playersdk.casting.KCastProviderImpl;
import com.kaltura.playersdk.events.KPEventListener;
import com.kaltura.playersdk.events.KPlayerState;
import com.kaltura.playersdk.interfaces.KCastProvider;
import com.kaltura.playersdk.interfaces.ScanCastDeviceListener;
import com.kaltura.playersdk.types.KPError;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,
        KPEventListener, RadioGroup.OnCheckedChangeListener {

    private static final int REQUEST_WRITE_STORAGE = 200;

    public static final int DEMO_1 = 1;
    public static final int DEMO_2 = 2;
    public static final int DEMO_LOCAL = 3;
    public static final int DEMO_CHROMECAST = 4;

    private static final String TAG = "ChangeMediaDemo";
    private Button mPlayPauseButton;
    private SeekBar mSeekBar;
    private PlayerViewController mPlayer;
    private View mSwitchContainer;
    private View mPlayerContainer;
    private boolean onCreate = false;

    private String currentMediaId;
    private int mDemoType;
    private KCastProviderImpl mChromeCastProvider;

    private KPPlayerConfig config;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mPlayPauseButton = (Button)findViewById(R.id.button);
        mPlayPauseButton.setOnClickListener(this);
        mSeekBar = (SeekBar)findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mPlayPauseButton.setClickable(false);
        mSeekBar.setEnabled(false);
        mSwitchContainer = findViewById(R.id.switch_container);
        mPlayerContainer = findViewById(R.id.player_container);
        findViewById(R.id.start_btn).setOnClickListener(this);
        RadioGroup group = (RadioGroup)findViewById(R.id.group_check);
        group.setOnCheckedChangeListener(this);

        onCreate = true;
        mDemoType = DEMO_1;
        //loadMediaId("1_rxg6yzjw");
        loadMediaId("1_ypo9wae3");
        askPermission();
    }

    private void askPermission() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //reload my activity with permission granted or use the features what required the permission
                } else
                {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    private void loadMediaId(String mediaId) {
        mPlayPauseButton.setVisibility(View.VISIBLE);
        mSeekBar.setVisibility(View.VISIBLE);
        if (mPlayer != null)
            mPlayer.setVisibility(View.VISIBLE);
        mPlayPauseButton.setClickable(true);
        mSeekBar.setEnabled(true);

        currentMediaId = mediaId;
        Toast.makeText(getApplicationContext(), "Selected Media Id = " + mediaId, Toast.LENGTH_SHORT).show();
        if (mPlayer == null) {
            Log.d(TAG, "first time with entry id = " +  currentMediaId);
            getPlayer();
        }
        else {
            Log.d(TAG, "changeMedia with entry id = " + currentMediaId);
            mPlayer.changeMedia(currentMediaId);
        }
    }

    private PlayerViewController getPlayer() {
        if (mPlayer == null) {
            mPlayer = (PlayerViewController)findViewById(R.id.player);
            mPlayer.loadPlayerIntoActivity(this);
            if (currentMediaId == null || "".equals(currentMediaId)){
                return null;
            }

            final KPPlayerConfig config = getConfig();

            mPlayer.initWithConfiguration(config);

            mPlayer.addEventListener(this);

            mPlayer.addKPlayerEventListener("AdSupport_StartAdPlayback", "AdSupport_StartAdPlayback", new PlayerViewController.EventListener() {
                @Override
                public void handler(String eventName, String params) {
                    Log.e(TAG, "AdSupport_StartAdPlayback called");
                    if ("true".equals(config.getConfigValueString("controlBarContainer.hover"))) {
                        mPlayer.sendNotification("showPlayerControls", null);
                    }
                }
            });
            mPlayer.addKPlayerEventListener("AdSupport_EndAdPlayback", "AdSupport_EndAdPlayback", new PlayerViewController.EventListener() {
                @Override
                public void handler(String eventName, String params) {
                    Log.e(TAG, "AdSupport_EndAdPlayback called");
                    if ("true".equals(config.getConfigValueString("controlBarContainer.hover"))) {
                        mPlayer.sendNotification("hidePlayerControls", null);
                    }
                }
            });

            mPlayer.addKPlayerEventListener("adClick", "adClick", new PlayerViewController.EventListener() {
                @Override
                public void handler(String eventName, String params) {
                    Log.e(TAG, "adClick called");
                }
            });

            mPlayer.addKPlayerEventListener("onAdSkip", "onAdSkip", new PlayerViewController.EventListener() {
                @Override
                public void handler(String eventName, String params) {
                    Log.e(TAG, "onAdSkip called");
                }
            });

            mPlayer.addKPlayerEventListener("textTracksReceived", "textTracksReceived", new PlayerViewController.EventListener() {
                @Override
                public void handler(String eventName, String params) {
                    Log.e(TAG, "textTracksReceived textTracksReceived " + params);
                }
            });
        }
        return mPlayer;
    }

    private KPPlayerConfig getConfig() {
        String url = "http://kgit.html5video.org/tags/v2.44/mwEmbedFrame.php";
        String uiConf = "32855491";
        String parentID = "1424501";
        switch (mDemoType) {
            case DEMO_1:
                config = new KPPlayerConfig(url, uiConf, parentID).setEntryId(currentMediaId);
                config.addConfig("doubleClick.adTagUrl", "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=xml_vmap1&unviewed_position_start=1&cust_params=sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=[timestamp]");
                config.addConfig("doubleClick.plugin", "true");
                config.setAutoPlay(true);
                mPlayer.setCustomSourceURLProvider(null);
                break;
            case DEMO_2:
                config = new KPPlayerConfig(url, uiConf, parentID).setEntryId(currentMediaId);
                config.addConfig("doubleClick.adTagUrl", "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=xml_vmap1&unviewed_position_start=1&cust_params=sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=[timestamp]");
                config.addConfig("doubleClick.plugin", "true");
                config.addConfig("closedCaptions.plugin", "false");
                config.addConfig("controlBarContainer.plugin", "false");
                config.addConfig("watermark.plugin", "false");
                config.addConfig("loadingSpinner.plugin", "false");
                config.addConfig("topBarContainer.plugin", "false");
                config.addConfig("largePlayBtn.plugin", "false");
                mPlayer.setCustomSourceURLProvider(null);
                break;
            case DEMO_LOCAL:
                config = new KPPlayerConfig(url, uiConf, parentID).setEntryId(currentMediaId);
                config.setAutoPlay(true);
                PlayerViewController.SourceURLProvider mSourceURLProvider = new PlayerViewController.SourceURLProvider() {
                    @Override
                    public String getURL(String entryId, String currentURL) {
                       return "/sdcard/Download/cat.mp4";
                    }
                };
                mPlayer.setCustomSourceURLProvider(mSourceURLProvider);
                LocalAssetsManager.registerAsset(MainActivity.this, config, "1_6dadj61z", "/sdcard/Download/cat.mp4", new LocalAssetsManager.AssetRegistrationListener() {
                    @Override
                    public void onRegistered(String assetPath) {
                        Log.e("local Player", "Register successful");
                    }

                    @Override
                    public void onFailed(String assetPath, Exception error) {
                        Log.e("local Player", "Register failed - " + error.getMessage());
                    }
                });
                break;
            case DEMO_CHROMECAST:
                config = new KPPlayerConfig(url, uiConf, parentID).setEntryId(currentMediaId);
                config.setAutoPlay(true);
                config.addConfig("chromecast.plugin", "true");
                config.addConfig("chromecast.useKalturaPlayer", "true");
                //mPlayer.getKCastRouterManager().
                //mPlayer.get
                mChromeCastProvider.startScan(this, "48A28189");
                break;
        }
        //config.setAutoPlay(true);
        //config.addConfig("doubleClick.adTagUrl", "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/3274935/preroll&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]");
        //config.addConfig("controlBarContainer.hover", "false");
        //config.addConfig("autoPlay", "true");
        return config;
    }

    private RelativeLayout getPlayerContainer() {
        return (RelativeLayout)findViewById(R.id.playerContainer);
    }

    @Override
    protected void onPause() {
        if (mPlayer != null) {
            mPlayer.releaseAndSavePosition();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (onCreate) {
            onCreate = false;
        } else {
            if (mPlayer != null)
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
    public void onBackPressed() {
        if (mPlayerContainer.getVisibility() == View.VISIBLE) {
            mPlayerContainer.setVisibility(View.GONE);
            mSwitchContainer.setVisibility(View.VISIBLE);
            if (mPlayer != null) {
                mPlayer.resetPlayer();
            }
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mr_control_play_pause:
                if (mPlayPauseButton.getText().equals("Play")) {
                    mPlayPauseButton.setText("Pause");
                    getPlayer().sendNotification("doPlay", null);
                } else {
                    mPlayPauseButton.setText("Play");
                    getPlayer().sendNotification("doPause", null);
                }
                break;
            case R.id.start_btn:
                mPlayerContainer.setVisibility(View.VISIBLE);
                mSwitchContainer.setVisibility(View.GONE);
                if (mPlayer != null) {
                    mPlayer.changeConfiguration(getConfig());
                }
                break;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            float progressInPercent = progress / 100f;
            float seekVal = (float) (progressInPercent * mPlayer.getDurationSec());
            getPlayer().sendNotification("doSeek", Float.toString(seekVal));
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
        if (state == KPlayerState.ENDED) {
            Log.d(TAG,"Stream ENDED");
            mPlayPauseButton.setVisibility(View.INVISIBLE);
            mSeekBar.setVisibility(View.INVISIBLE);
            mPlayer.setVisibility(View.INVISIBLE);
        }
        if (state == KPlayerState.PAUSED) {
            Log.d(TAG, "Stream PAUSED");
            mPlayPauseButton.setText("Play");
        }
        if (state == KPlayerState.PLAYING) {
            Log.d(TAG, "Stream PAUSED");
            mPlayPauseButton.setText("Pause");
        }
    }

    @Override
    public void onKPlayerPlayheadUpdate(PlayerViewController playerViewController, float currentTime) {
        mSeekBar.setProgress((int) (currentTime / playerViewController.getDurationSec() * 100));
    }

    @Override
    public void onKPlayerFullScreenToggeled(PlayerViewController playerViewController, boolean isFullscrenn) {

    }

    @Override
    public void onKPlayerError(PlayerViewController playerViewController, KPError error) {
        Log.e(TAG, "Error Received:" + error.getErrorMsg());
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.radioCheck1:
                mDemoType = DEMO_1;
                break;
            case R.id.radioCheck2:
                mDemoType = DEMO_2;
                break;
            case R.id.radioCheckLocal:
                mDemoType = DEMO_LOCAL;
                break;
            case R.id.radioCheckChromecast:
                mDemoType = DEMO_CHROMECAST;
                break;
        }
    }
}
