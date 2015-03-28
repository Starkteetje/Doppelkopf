package nldoko.game.start;

import nldoko.game.R;
import nldoko.game.base.BaseActivity;
import nldoko.game.game.NewGameActivity;
import nldoko.game.game.SavedGameListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class StartActivity extends BaseActivity {
	private Button mBtnNewGame;
	private LinearLayout mSavedGameBtn;


	private Handler mHandler;
	
	private long mDelayChar;
	
	private static TextView mSavedGameText;
	private static CharSequence mSavedGameTextCharSequence;
	private int mIndex;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_start);


        mHandler = new Handler();
        mDelayChar = 50; // ms
        mSavedGameTextCharSequence =  this.getResources().getString(R.string.str_saved_game);
        
        mBtnNewGame = (Button)findViewById(R.id.btn_new_game);
        mBtnNewGame.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(arg0.getContext(),NewGameActivity.class);
				startActivity(i);
			}
		});
        
        mSavedGameBtn = (LinearLayout)findViewById(R.id.saved_game_btn);
        mSavedGameText = (TextView)findViewById(R.id.saved_game_btn_text);
   
        
        mSavedGameBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(arg0.getContext(),SavedGameListActivity.class);
				startActivity(i);
			}
		});
               
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
    
    private Runnable characterAdder = new Runnable() {
    	@Override
    	public void run() {
        	
    		mSavedGameText.setText(mSavedGameTextCharSequence.subSequence(0, mIndex++));
    	    if(mIndex <= mSavedGameTextCharSequence.length()) {
    	    	Log.d(TAG,"run "+mIndex+mSavedGameTextCharSequence);
    	        mHandler.postDelayed(characterAdder, mDelayChar);
    	    }
    	}
    };

    protected void drawerItemPressed(int pos) {
        int i = 0;
    }

}
