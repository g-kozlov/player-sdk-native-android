package com.kaltura.kalturaplayertoolkit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.kaltura.playersdk.KPPlayerConfig;


public class MainActivity extends Activity implements LoginFragment.OnFragmentInteractionListener, PlayerFragment.OnFragmentInteractionListener, FullscreenFragment.OnFragmentInteractionListener{
	public static String TAG = MainActivity.class.getSimpleName();

    @SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        loadFragment();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        loadFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Intent intent = getIntent();
        Fragment fragment = new FullscreenFragment();
        Bundle extras = intent.getExtras();
        if (extras == null) {
            extras = new Bundle();
        }

        //KPPlayerConfig config = new KPPlayerConfig("http://kgit.html5video.org/branches/master/mwEmbedFrame.php", "20540612", "243342").setEntryId("1_sf5ovm7u");        extras.putSerializable("config", config);
        //KPPlayerConfig config = new KPPlayerConfig("http://192.168.160.195/html5.kaltura/mwEmbed/mwEmbedFrame.php", "15128121", "1091").setEntryId("0_vpdvoc48");
        //KPPlayerConfig config = new KPPlayerConfig("http://kgit.html5video.org/branches/master/mwEmbedFrame.php", "33460501", "2068231").setEntryId("1_y62mh1hl");
        //KPPlayerConfig config = new KPPlayerConfig("http://kgit.html5video.org/branches/master/mwEmbedFrame.php", "33189171", "2068231").setEntryId("1_az42om7s");

        //KPPlayerConfig config = new KPPlayerConfig("http://kgit.html5video.org/branches/master/mwEmbedFrame.php", "12905712", "243342").setEntryId("0_uka1msg4"); // COFFEE
        //JW = KPPlayerConfig config = new KPPlayerConfig("http://kgit.html5video.org/branches/master/mwEmbedFrame.php", "33502041", "2068231").setEntryId("1_jnlsx5nv");
        //KPPlayerConfig config = new KPPlayerConfig("http://kgit.html5video.org/branches/master/mwEmbedFrame.php", "33502041", "2068231").setEntryId("1_6q4u0wxw");
        //KPPlayerConfig config = new KPPlayerConfig("http://kgit.html5video.org/branches/master/mwEmbedFrame.php", "28013271", "1878761").setEntryId("1_qnsmpr3i"); //LIVE
        //KPPlayerConfig config = new KPPlayerConfig("http://192.168.160.202/html5.kaltura/mwEmbed/mwEmbedFrame.php", "28013271", "1878761").setEntryId("1_k64id08u"); //LIVE DVR
        KPPlayerConfig config = new KPPlayerConfig("http://kgit.html5video.org/branches/master/mwEmbedFrame.php", "28013271", "1878761").setEntryId("1_k64id08u"); //LIVE DVR

        config.addConfig("EmbedPlayer.ShowPosterOnStop", "false");
        config.addConfig("closedCaptions.displayCaptions", "true");
        //config.addConfig("Kaltura.LeadHLSOnAndroid", "true");
//        config.addConfig("doubleClick.plugin", "false");
//        config.addConfig("doubleClick.path", "http://cdnbakmi.kaltura.com/content/uiconf/ps/veria/kdp3.9.1/plugins/doubleclickPlugin.swf");
//        config.addConfig("doubleClick.adTagUrl", "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=xml_vmap1&unviewed_position_start=1&cust_params=sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=[timestamp]");
        config.setCacheSize(0.8f);
        extras.putSerializable("config", config);

        FragmentUtilities.loadFragment(false, fragment, extras, getFragmentManager());
    }

    private void loadFragment(){
        Intent intent = getIntent();
        Fragment fragment = new LoginFragment();
        Bundle extras = intent.getExtras();

        if (Intent.ACTION_VIEW.equals( intent.getAction())) {
            Uri uri = intent.getData();

            if (uri == null) {
                Log.e(TAG, "Can't load player; no data uri");
                return;
            }
            
            String embedFrameURL = uri.getQueryParameter("embedFrameURL");
            if (embedFrameURL == null) {
                Log.e(TAG, "Can't load player; uri does not contain embedFrameURL parameter");
                return;
            }
            
            extras.putSerializable("config", KPPlayerConfig.fromEmbedFrameURL(embedFrameURL));
            fragment = new FullscreenFragment();
        }

        FragmentUtilities.loadFragment(false, fragment, extras, getFragmentManager());
    }
}
