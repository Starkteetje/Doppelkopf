package nldoko.game.java;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.appcompat.BuildConfig;
import nldoko.game.R;

import nldoko.game.java.game.SavedGameListActivity;


public class SplashscreenActivity extends Activity {

    private static final int SPLASH_TIME = 1500; // ms
    private static final int SPLASH_TIME_DEBUG = 100; // ms

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.splashscreeen_layout);

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        int time = SPLASH_TIME;
        if (isDebug()) {
            time = SPLASH_TIME;
        }

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashscreenActivity.this,SavedGameListActivity.class);
                SplashscreenActivity.this.startActivity(mainIntent);
                SplashscreenActivity.this.finish();
            }
       }, time);

    }

    private Boolean isDebug () {
        if (BuildConfig.DEBUG) {
            return true;
        }

        return false;
    }


}
