package nldoko.game.game;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import nldoko.game.R;
import nldoko.game.XML.DokoXMLClass;
import nldoko.game.base.BaseActivity;
import nldoko.game.data.DokoData;
import nldoko.game.data.DokoData.GAME_CNT_VARIANT;
import nldoko.game.information.AboutActivity;
import nldoko.game.information.InfoSettingsDialog;

import java.util.ArrayList;


public class NewGameActivity extends BaseActivity {


	private LinearLayout mLayout;
	private LinearLayout mMarkSuspendedLayout;
	private Boolean isMarkSuspendedPlayerSelected = false;
	private ImageView mIv;
	private TextView mTv;
	private TextView mTvPlayerCnt;
	private int mPlayerCnt = DokoData.MIN_PLAYER;
	private AutoCompleteTextView myAutoComplete;
	private Spinner mSpActivePlayer;
	private Spinner mSpBockLimit;
	private Spinner mSpGameCntVariant;
	private CheckBox mCbSuspendMark;
    private CheckBox mCbAutoBockCalc;
    private LinearLayout mGameSettingsEntry;
    private LinearLayout mGameSettingsList;
	
	private Button mBtnStart;
	
	private ArrayList<Integer> mActivePlayerArrayList = new ArrayList<Integer>();
	private ArrayList<Integer> mBockLimitArrayList = new ArrayList<Integer>();
	private ArrayAdapter<Integer> mSPActivePlayerArrayAdapter;
	private ArrayAdapter<Integer> mSPBockLimitArrayAdapter;
	private ArrayAdapter<String> mSPGameCntVaraintArrayAdapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_newgame);
        getSupportActionBar().setTitle(R.string.str_new_game);
        setUI();
        
        overridePendingTransition(R.anim.right_out, R.anim.left_in);
    }


    private void setUI() {
    	mLayout = (LinearLayout)findViewById(R.id.new_gamge_layout_content);
    	mLayout.requestFocus();

    	mLayout = (LinearLayout)findViewById(R.id.player_entry_add_btn); 
    	if(mLayout != null)mLayout.setOnClickListener(new addPlayerClickListener());
    	
    	mSpActivePlayer	 	= (Spinner)findViewById(R.id.sp_act_player_cnt);
    	mSpBockLimit 		= (Spinner)findViewById(R.id.sp_bock_cnt);
    	mSpGameCntVariant	= (Spinner)findViewById(R.id.sp_game_cnt_variant);
    	mCbSuspendMark		= (CheckBox)findViewById(R.id.cb_suspend);
        mCbAutoBockCalc     = (CheckBox)findViewById(R.id.cb_bock_auto_calc);
        mCbSuspendMark     = (CheckBox)findViewById(R.id.cb_suspend);

        mGameSettingsEntry = (LinearLayout)findViewById(R.id.new_game_settings_entry);
        mGameSettingsEntry.setOnClickListener(new showGameSettingsClickListener());

        mGameSettingsList = (LinearLayout)findViewById(R.id.new_game_settings_entry_list);
        mGameSettingsList.setVisibility(View.GONE);
    	
    	mTvPlayerCnt = (TextView)findViewById(R.id.player_add_player_cnt);

        
        mMarkSuspendedLayout = (LinearLayout)findViewById(R.id.fragment_game_set_mark_suspend_entry);
        if (mCbSuspendMark != null) {
        	mCbSuspendMark.setChecked(true);
        }

        if (mCbAutoBockCalc != null) {
            mCbAutoBockCalc.setChecked(true); // default on
        }

    	mBtnStart = (Button)findViewById(R.id.btn_start_new_game);
    	mBtnStart.setOnClickListener(new startBtnClickListener());
    	
    	setSpinnerValues();
    	setAutoCompleteNames();

        setPlayerColors();
        updatePlayerCnt();
	}
    
    private void setSpinnerValues(){
    	int mSelction;
    	
    	mSelction = mSpActivePlayer.getSelectedItemPosition();
    	mActivePlayerArrayList.clear();
    	for(int k=DokoData.MIN_PLAYER; k <= mPlayerCnt && k <= DokoData.MAX_ACTIVE_PLAYER;k++) mActivePlayerArrayList.add(k);
    	mSPActivePlayerArrayAdapter = new ArrayAdapter<Integer>(getApplicationContext(), R.layout.spinner_item_text,mActivePlayerArrayList);
   	    mSpActivePlayer.setAdapter(mSPActivePlayerArrayAdapter);
   	    mSpActivePlayer.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int arg2, long arg3) {
				updateMarkPlayerOption();				
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
   	    });
   	    		
   	    if(mSpActivePlayer.getAdapter().getCount() > mSelction) mSpActivePlayer.setSelection(mSelction);

   	    int i = 0;
   		String[] mGameCntVariantArr  = new String[DokoData.GAME_CNT_VARAINT_ARRAY.length];
   		for (Integer[] entry : DokoData.GAME_CNT_VARAINT_ARRAY) {
   			if (entry != null && entry.length == 2) {
   				String cntName = getResources().getString(entry[0]);
   				mGameCntVariantArr[i] = cntName;
   			}
   			i++;
   		}

   		mSPGameCntVaraintArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item_text,mGameCntVariantArr);
   	    mSpGameCntVariant.setAdapter(mSPGameCntVaraintArrayAdapter);
   	    
   	 	mSelction = mSpBockLimit.getSelectedItemPosition();
    	mBockLimitArrayList.clear();
    	for(int k=0;k<=mPlayerCnt;k++) mBockLimitArrayList.add(k);
    	mSPBockLimitArrayAdapter = new ArrayAdapter<Integer>(getApplicationContext(), R.layout.spinner_item_text,mBockLimitArrayList);
   	    mSpBockLimit.setAdapter(mSPBockLimitArrayAdapter);
   	    
   	    if(mSpBockLimit.getAdapter().getCount() > mSelction) mSpBockLimit.setSelection(mSelction);
    }
    
    private void setAutoCompleteNames(){
    	View v;
    	mLayout = (LinearLayout)findViewById(R.id.player_view_holder);
    	loadPlayerNames();
    	for(int i=0;i<mLayout.getChildCount();i++){
    	    v = mLayout.getChildAt(i);
    	    if (v.getId() == R.id.player_entry){
    	    	v = (AutoCompleteTextView)v.findViewById(R.id.player_entry_auto_complete);
    	    	   ArrayAdapter<String> adapter =  new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item_text,DokoData.PLAYER_NAMES);
    	    	((AutoCompleteTextView) v).setAdapter(adapter);
    	    	((AutoCompleteTextView) v).setOnTouchListener(new View.OnTouchListener(){
    	    		   @Override
    	    		   public boolean onTouch(View v, MotionEvent event){
    	    			   ((AutoCompleteTextView) v).showDropDown();    return false;
    	    		   }
    	    		});

    	    }  
    	}
    }
    
    private void loadPlayerNames() {
		if(!DokoXMLClass.isAppDirOK(mContext)) return;
		DokoXMLClass.isXMLPresent(mContext,DokoData.PLAYER_NAMES_XML,true);
		DokoXMLClass.getPlayerNamesFromXML(mContext,DokoData.PLAYER_NAMES_XML,DokoData.PLAYER_NAMES);
		
	}

    private void setPlayerColors() {
        LinearLayout mLayout = (LinearLayout)findViewById(R.id.player_view_holder);

        int pCount = 0;
        for (int i = 0; i < mLayout.getChildCount(); ++i) {
            View v = mLayout.getChildAt(i);
            if (v.getId() == R.id.player_entry) {
                View mPlayercolorView = v.findViewById(R.id.player_color);
                if (mPlayercolorView != null) {
                    mPlayercolorView.setBackgroundColor(this.getResources().getColor(DokoData.PLAYERS_COLORS_KEY[pCount]));
                    pCount++;
                }

            }
        }
    }

	private void updatePlayerCnt(){
    	View v;
    	mLayout = (LinearLayout)findViewById(R.id.player_view_holder);
    	mPlayerCnt = 0;
    	for(int i=0;i<mLayout.getChildCount();i++){
    	    v = mLayout.getChildAt(i);
    	    if (v.getId() == R.id.player_entry) mPlayerCnt++;
    	}

        mTvPlayerCnt.setText(String.valueOf(mPlayerCnt));

    	updateMarkPlayerOption();
    	setSpinnerValues();
    }
	
	private void updateMarkPlayerOption() {
		if (mMarkSuspendedLayout == null) {
			return;
		}
		CheckBox cb = (CheckBox)mMarkSuspendedLayout.findViewById(R.id.cb_suspend);
        if (cb != null) {
        	if (mSpActivePlayer.getSelectedItemPosition()+4 < mPlayerCnt) {
        		mMarkSuspendedLayout.setVisibility(View.VISIBLE);
        	} else {
        		mMarkSuspendedLayout.setVisibility(View.GONE);
        		cb.setSelected(false);
        		isMarkSuspendedPlayerSelected = false;
        	}
        }
	}
	
	private class startBtnClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if(!isPlayerNameSet(true)){
				Toast.makeText(v.getContext(), R.string.str_error_player_name, Toast.LENGTH_SHORT).show();
				return;
			}
			ArrayList<String> mPlayerNames = getPlayerNames();
			Intent i = new Intent(mContext,GameActivity.class);
			for(int k=0;k<mPlayerCnt && k<mPlayerNames.size();k++){
				i.putExtra(DokoData.PLAYERS_KEY[k], mPlayerNames.get(k).toString());
			}
			i.putExtra(DokoData.PLAYER_CNT_KEY, mPlayerCnt);
			i.putExtra(DokoData.MARK_SUSPEND_OPTION_KEY, mCbSuspendMark.isChecked());
			i.putExtra(DokoData.BOCKLIMIT_KEY, mSpBockLimit.getSelectedItemPosition());
			i.putExtra(DokoData.ACTIVE_PLAYER_KEY, mSpActivePlayer.getSelectedItemPosition()+4);
			i.putExtra(DokoData.GAME_CNT_VARIANT_KEY, GAME_CNT_VARIANT.values()[mSpGameCntVariant.getSelectedItemPosition()]);
            i.putExtra(DokoData.AUTO_BOCK_CALC_KEY, mCbAutoBockCalc.isChecked());

			startActivity(i);
		}
	}
	

	private boolean isPlayerNameSet(boolean saveToXML) {
		ArrayList<String> mPlayerNames = getPlayerNames();
		//Log.d(TAG,mPlayerNames.size()+"+"+ mPlayerCnt);
		if(mPlayerNames.size() < mPlayerCnt) return false;
		
		for(int i=0;i<mPlayerNames.size();i++){
			if(mPlayerNames.get(i).isEmpty()) return false;
		}
		
    	for(int i=0;i<DokoData.PLAYER_NAMES.size();i++){ 
    		if(!mPlayerNames.contains(DokoData.PLAYER_NAMES.get(i)) )
    			mPlayerNames.add(DokoData.PLAYER_NAMES.get(i));
    	}
		DokoData.PLAYER_NAMES = mPlayerNames;
		DokoXMLClass.savePlayerNamesToXML(mContext, DokoData.PLAYER_NAMES_XML,mPlayerNames);
		return true;
	}
	
	private ArrayList<String> getPlayerNames(){
		ArrayList<String> mPlayerNames = new ArrayList<String>();
		View v;
		AutoCompleteTextView ac;
		mLayout = (LinearLayout)findViewById(R.id.player_view_holder);

    	for(int i=0;i<mLayout.getChildCount();i++){
    	    v = mLayout.getChildAt(i);
    	    if (v.getId() == R.id.player_entry){
    	    	ac = (AutoCompleteTextView)v.findViewById(R.id.player_entry_auto_complete);
    	    	if(!mPlayerNames.contains(ac.getText().toString().trim())){
    	    		//Log.d(TAG,ac.getText().toString());
    	    		mPlayerNames.add(ac.getText().toString().trim());
    	    	}
    	    }
    	}

    	return mPlayerNames;
	}
    
    private class addPlayerClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if(mPlayerCnt == DokoData.MAX_PLAYER) {

                return;
            }
			mLayout = (LinearLayout)findViewById(R.id.player_view_holder);
			LinearLayout mNewPlayerView = (LinearLayout)mInflater.inflate(R.layout.player_entry, null);
			
			mIv = (ImageView)mNewPlayerView.findViewById(R.id.player_entry_remove);
			mIv.setOnClickListener(new removePlayerClickListener());
			mIv.setVisibility(View.VISIBLE);
			mIv.setId(mPlayerCnt+1);
			mLayout.addView(mNewPlayerView);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mNewPlayerView.getLayoutParams();

            float marginTop = v.getResources().getDimension(R.dimen.item_space);
            layoutParams.setMargins(0, Math.round(marginTop),0,0);
            mNewPlayerView.setLayoutParams(layoutParams);
			
			updatePlayerCnt();

			setAutoCompleteNames();
            setPlayerColors();
		}
    	
    }

    private class showGameSettingsClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            if (mGameSettingsList.getVisibility() == View.VISIBLE) {
                mGameSettingsList.setVisibility(View.GONE);
            } else {
                mGameSettingsList.setVisibility(View.VISIBLE);
            }

        }

    }
    
    private class removePlayerClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			mLayout = (LinearLayout)findViewById(R.id.player_view_holder);	
			mLayout.removeView((View) v.getParent());
			updatePlayerCnt();
            setPlayerColors();
		}
    	
    }
    
    public class settingInfoClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			InfoSettingsDialog infoDialog = new InfoSettingsDialog(mContext);
			infoDialog.show();
		}
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }
    
    
    @Override
  	public boolean onOptionsItemSelected(MenuItem item){
    	// same as using a normal menu
    	Intent i;
    	switch(item.getItemId()) {
    	case R.id.action_about:
    		i = new Intent(this, AboutActivity.class);
    		startActivity(i);
    		break;
    	}
    	return true;
    }
    
    
}
