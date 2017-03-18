package com.doppelkopf.java.game;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.doppelkopf.java.DokoActivity;
import com.doppelkopf.java.R;
import com.doppelkopf.java.XML.DokoXMLClass;
import com.doppelkopf.java.data.DokoData;
import com.doppelkopf.java.data.DokoData.GAME_CNT_VARIANT;
import com.doppelkopf.java.data.DokoData.GAME_VIEW_TYPE;
import com.doppelkopf.java.data.DokoData.PLAYER_ROUND_RESULT_STATE;
import com.doppelkopf.java.data.GameClass;
import com.doppelkopf.java.util.Functions;
import com.doppelkopf.java.util.TextDrawable;

public class GameActivity extends DokoActivity {

	private String TAG = "Game";
		
	private static ListView mLvRounds;
	private static GameMainListAdapter mLvRoundAdapter;

	private static LinearLayout mLayout;
	private static LinearLayout mGameRoundsInfoSwipe;
	private static LinearLayout mBottomInfos;
	private static TextView mBottomInfoBockRoundCount;
	private static TextView mBottomInfoBockRoundPreview;
	
	private static boolean mBockPreviewOnOff = true;

	private TextView mTvPlayerCnt;
	private static TextView mTvAddRoundBockPoints;
    private static LinearLayout mTvRoundBockPointsContainer;
    private static TextView mTvRoundBockPointsAutoCalcOnOff;

	private int mPlayerCnt;
	private Spinner mSpActivePlayer;
	private Spinner mSpBockLimit;
	private Boolean mMarkSuspendedPlayers;

	private static Button mBtnAddRound;
	private static TextView mEtNewRoundPoints;
	
	private ArrayList<Integer> mActivePlayerArrayList = new ArrayList<Integer>();
	private ArrayList<Integer> mBockLimitArrayList = new ArrayList<Integer>();
	private ArrayAdapter<Integer> mSPActivePlayerArrayAdapter;
	private ArrayAdapter<Integer> mSPBockLimitArrayAdapter;
	
	private static ArrayList<TextView> mGameAddRoundPlayerState = new ArrayList<TextView>();
	
	private SwipePagerAdapter mDemoCollectionPagerAdapter;
    private ViewPager mViewPager;
    private int mFocusedPage = 0;
	private static final int mIndexGameMain 		= 0;
	private static final int mIndexGameNewRound 	= 1;
	protected static GameClass mGame;
    private static int mWinnerList[] = new int[DokoData.MAX_PLAYER];
    private static int mSuspendList[] = new int[DokoData.MAX_PLAYER];
    

    private static CheckBox mCBNewBockRound;

    private static GameAddRoundPlayernameClickListener mAddRoundPlayernameClickListener;
    private static GameAddRoundPlayernameLongClickListener mAddRoundPlayernameLongClickListener;
    private static btnAddRoundClickListener mBtnAddRoundClickListener;

    private ProgressDialog progressDialog;

	private static Drawable winnerDraw;
	private static Drawable loserDraw;
	private static Drawable suspendDraw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_game);

        setupDrawerAndToolbar(mContext.getResources().getString(R.string.str_game));

        mGame = getGame(savedInstanceState);

        if(mGame == null){
        	Toast.makeText(this, getResources().getString(R.string.str_error_game_start), Toast.LENGTH_LONG).show();
        	finish();
        }

        mPlayerCnt = mGame.getPlayerCount();

        loadSwipeViews();

        mAddRoundPlayernameClickListener = new GameAddRoundPlayernameClickListener();
        mAddRoundPlayernameLongClickListener = new GameAddRoundPlayernameLongClickListener();
        mBtnAddRoundClickListener = new btnAddRoundClickListener();

        //findViewById(R.id.game_main_layout).requestFocus();

        overridePendingTransition(R.anim.right_out, R.anim.left_in);
    }

    private void loadSwipeViews(){
    	  mDemoCollectionPagerAdapter =  new SwipePagerAdapter(getSupportFragmentManager());
          mViewPager = (ViewPager) findViewById(R.id.game_pager);
          mViewPager.setAdapter(mDemoCollectionPagerAdapter);
          mViewPager.setOnPageChangeListener(new GamePageChangeListener());
    }

    
    private void reloadSwipeViews(){
    	loadSwipeViews();
    }
    
    
    private class GamePageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            mFocusedPage = position;
            String mStr = "";
            switch (mFocusedPage) {
			case mIndexGameMain:
				getSupportActionBar().setTitle(getResources().getString(R.string.str_game)); return;
			case mIndexGameNewRound:
                getSupportActionBar().setTitle(getResources().getString(R.string.str_game_new_round));
	  			if(mGame != null && mGame.getPreRoundList().size() > 0 && mGame.getPreRoundList().get(0).getBockCount() > 0){
	  				mStr = getResources().getString(R.string.str_bockround)+" ";
	  				mStr += Functions.getBockCountAsString(mGame.getPreRoundList().get(0).getBockCount());
	  				mTvAddRoundBockPoints.setText(mStr);

                    mTvRoundBockPointsContainer.setVisibility(View.VISIBLE);
                    if(mGame.isAutoBockCalculationOn()){
                        mTvRoundBockPointsAutoCalcOnOff.setText(mContext.getResources().getString(R.string.str_yes));
                    } else {
                        mTvRoundBockPointsAutoCalcOnOff.setText(mContext.getResources().getString(R.string.str_no));
                    }
	  			}
	  			else {
                    mTvAddRoundBockPoints.setText(mStr);
                    mTvRoundBockPointsContainer.setVisibility(View.INVISIBLE);
                }
	  			
	  			return;
			default:
				getSupportActionBar().setTitle(getResources().getString(R.string.str_game));
				break;
			}
        }
    }
    
    private GameClass getGame(Bundle savedInstanceState){
    	GameClass mGame;
    	Intent intent = getIntent();
    	Bundle extras = intent.getExtras();
    	int mActivePlayers,mBockLimit,mPlayerCnt;
    	GAME_CNT_VARIANT mGameCntVaraint;
    	String mTmp = "";
        boolean mAutoBockCalc = true;
    	
    	//Log.d(TAG,"getgame");
        //Check if a game exists else try to create one 
        if(savedInstanceState != null && !savedInstanceState.isEmpty()) {
        	mGame =  loadStateData(savedInstanceState);
        }
        else if(extras != null && extras.getBoolean("RestoreGameFromXML", false)){
            progressDialog = new ProgressDialog(GameActivity.this);
            progressDialog.setTitle(this.getResources().getString(R.string.str_plz_wait));
            progressDialog.setMessage(this.getResources().getString(R.string.str_game_load));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();

        	String file = extras.getString("filename");
        	Log.d(TAG,"Game from XML file:"+file);
        	mGame =  DokoXMLClass.restoreGameStateFromXML(this,file, true);
        	if (mGame != null) {
        		// if success delete old file
                DokoXMLClass.saveGameStateToXML(mContext, mGame);
        	}

            new CountDownTimer(1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) { }

                @Override
                public void onFinish() {
                    progressDialog.dismiss();
                }
            }.start();

        }
        else if(extras != null){
        	mPlayerCnt 		= extras.getInt(DokoData.PLAYER_CNT_KEY,0);
        	mActivePlayers 	= extras.getInt(DokoData.ACTIVE_PLAYER_KEY,0);
        	mBockLimit		= extras.getInt(DokoData.BOCKLIMIT_KEY,0);
        	mMarkSuspendedPlayers = extras.getBoolean(DokoData.MARK_SUSPEND_OPTION_KEY,false);
        	mGameCntVaraint = (GAME_CNT_VARIANT)intent.getSerializableExtra(DokoData.GAME_CNT_VARIANT_KEY);
            mAutoBockCalc = extras.getBoolean(DokoData.AUTO_BOCK_CALC_KEY,true);
        	
        	if(mPlayerCnt < DokoData.MIN_PLAYER || mPlayerCnt > DokoData.MAX_PLAYER 
        			|| mActivePlayers > mPlayerCnt || mActivePlayers < DokoData.MIN_PLAYER
        			|| (mPlayerCnt == 0 || mActivePlayers == 0))
        		return null;
        	Log.d(TAG,"ng:"+mMarkSuspendedPlayers);
        	mGame = new GameClass(mPlayerCnt, mActivePlayers, mBockLimit, mGameCntVaraint,mMarkSuspendedPlayers, mAutoBockCalc);
        	Log.d(TAG,"bl:"+mBockLimit+" - prercnt"+mGame.getPreRoundList().size());
        	for(int k=0;k<mPlayerCnt;k++){
        		//Log.d(TAG,mTmp+"k:"+k);
        		mTmp = extras.getString(DokoData.PLAYERS_KEY[k],"");
        		if(mTmp == null || mTmp.length() == 0) return null;
        		//Log.d(TAG,mTmp);
        		mGame.getPlayer(k).setName(mTmp);
        	}
        }
        else{
        	mGame = new GameClass(5, 4, 1, GAME_CNT_VARIANT.CNT_VARIANT_NORMAL,false,true);
	    	
        	mGame.getPlayer(0).setName("Johannes");
        	mGame.getPlayer(1).setName("Christoph");
        	mGame.getPlayer(2).setName("P3");
	    	mGame.getPlayer(3).setName("P4");
	    	mGame.getPlayer(4).setName("P5");
	    	mGame.getPlayer(5).setName("P6");
	    	mGame.getPlayer(6).setName("P7");
	    	mGame.getPlayer(7).setName("P8");
        }
        return mGame;
    }
    

    
    
	 // Since this is an object collection, use a FragmentStatePagerAdapter,
	 // and NOT a FragmentPagerAdapter.
	 public class SwipePagerAdapter extends FragmentPagerAdapter {
	    private static final int mIndexCnt	= 2;  
	    

	    
	    public SwipePagerAdapter(FragmentManager fm) {
	        super(fm);
	    }
	
	    @Override
	    public Fragment getItem(int i) {
	        Fragment fragment = new DemoObjectFragment();
	        Bundle args = new Bundle();
	        // Our object is just an integer :-P
	        args.putInt(DemoObjectFragment.mPageIndex, i);
	        fragment.setArguments(args);
	        return fragment;
	    }
	
	    @Override
	    public int getCount() {
	        return mIndexCnt;
	    }
	
	    @Override
	    public CharSequence getPageTitle(int position) {
	    	Log.d(TAG,"frag getPageTitle");
	        return "OBJECT " + (position);
	    }
	}

	// Instances of this class are fragments representing a single
	//object in our collection.
	public static class DemoObjectFragment extends Fragment {
	  public static final String mPageIndex = "pageIndex";

	
	  @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	      // The last two arguments ensure LayoutParams are inflated
	      // properly.
		  Bundle args = getArguments();
		  View rootView;
		  switch(args.getInt(mPageIndex)){
		  	case mIndexGameMain:
		  		rootView  = inflater.inflate(R.layout.fragment_game_main, container, false);
		  		setUIMain(rootView, inflater, this.getContext());
		  		break;
		  	case mIndexGameNewRound:
		  		rootView  = inflater.inflate(R.layout.fragment_game_add_round, container, false);
		  		setUINewRound(rootView, inflater, this.getContext());
		  		break;
		  	default:
		  		rootView  = inflater.inflate(R.layout.spacer_gray, container, false);
		  }
		  return rootView;
	  }
	}
	
	private static void setUIMain(View rootView, LayoutInflater inflater, Context context) {
		mLvRounds = (ListView)rootView.findViewById(R.id.fragment_game_round_list);

		mLvRoundAdapter = new GameMainListAdapter(context, mGame.getRoundList(),mGame);
		mLvRounds.setAdapter(mLvRoundAdapter);
		mLvRoundAdapter.changeRoundListViewMode(GAME_VIEW_TYPE.ROUND_VIEW_TABLE);
		mGameRoundsInfoSwipe = (LinearLayout)rootView.findViewById(R.id.fragment_game_rounds_infos);
		if(mGame != null && mGame.getRoundCount() > 0 && mLvRoundAdapter.getRoundListViewMode() == GAME_VIEW_TYPE.ROUND_VIEW_DETAIL)
			mGameRoundsInfoSwipe.removeAllViews();
		
		if(mGame != null && mGame.getRoundCount() > 0 && mLvRoundAdapter.getRoundListViewMode() == GAME_VIEW_TYPE.ROUND_VIEW_TABLE){
			mGameRoundsInfoSwipe.removeAllViews();
			createTableHeader(inflater, context);
		}
		
		mBottomInfos = (LinearLayout)rootView.findViewById(R.id.fragment_game_bottom_infos);
		mBottomInfoBockRoundCount = (TextView)rootView.findViewById(R.id.fragment_game_bottom_infos_content_bock_count);
		mBottomInfoBockRoundPreview = (TextView)rootView.findViewById(R.id.fragment_game_bottom_infos_content_bock_count_preview);
		
		if(mGame != null){
			if(mGame.getBockRoundLimit() == 0 )
				mBottomInfos.setVisibility(View.GONE);
			else {
                setBottomInfo(context);
            }
		}
		
	}

	
	private static void setBottomInfo(Context context) {
		int mBockRoundCnt = 0, mTmp = 0;
		String mBockPreviewStr = "";
		if(mGame != null && mGame.getBockRoundLimit() > 0){
			for(int i=0;i<mGame.getPreRoundList().size();i++){
				mTmp = mGame.getPreRoundList().get(i).getBockCount();
				if(mTmp > 0){
					mBockRoundCnt++;
					mBockPreviewStr += Functions.getBockCountAsString(mTmp)+"  ";
				}
			}
		}
		if(mBockPreviewStr.length() > 0) {
            mBockPreviewStr.substring(0, mBockPreviewStr.length()-1);
        }

        if (mBockRoundCnt == 0) {
            mBottomInfoBockRoundCount.setText(context.getResources().getString(R.string.str_no_bock));

        } else {
            mBottomInfoBockRoundCount.setText(String.valueOf(mBockRoundCnt));
        }

		mBottomInfoBockRoundPreview.setText(mBockPreviewStr);
	}


    private static void setUINewRound(View rootView, LayoutInflater inflater, Context context) {
		winnerDraw = rootView.getResources().getDrawable(R.drawable.layer_game_add_player_win);
		loserDraw = rootView.getResources().getDrawable(R.drawable.layer_game_add_player_lose);
		suspendDraw = rootView.getResources().getDrawable(R.drawable.layer_game_add_player_suspended);


		mEtNewRoundPoints = (TextView)rootView.findViewById(R.id.game_add_round_points_entry);
        mEtNewRoundPoints.clearFocus();

		GameActivity.loadUINewRoundPlayerSection(rootView, inflater);

		mBtnAddRound = (Button)rootView.findViewById(R.id.btn_game_add_new_round);
		mBtnAddRound.setOnClickListener(mBtnAddRoundClickListener);
		
		mTvAddRoundBockPoints = (TextView)rootView.findViewById(R.id.game_add_round_bock_points);


        mTvRoundBockPointsContainer = (LinearLayout)rootView.findViewById(R.id.game_add_round_bock_info_container);
        mTvRoundBockPointsAutoCalcOnOff = (TextView)rootView.findViewById(R.id.game_add_round_bock_auto_calc_onoff);


        mCBNewBockRound = (CheckBox)rootView.findViewById(R.id.game_add_round_bock_cb);


        mLayout = (LinearLayout)rootView.findViewById(R.id.game_add_round_bock_container);
		if(mGame.getBockRoundLimit() == 0) {
            mLayout.setVisibility(View.GONE);
        }
		else {
            mLayout.setVisibility(View.VISIBLE);
        }

		resetNewRoundFields(context);
		
		rootView.findViewById(R.id.game_add_round_main_layout).requestFocus();
	}
	
	
    private static void loadUINewRoundPlayerSection(View rootView, LayoutInflater inflater) {
    	LinearLayout mLl;
    	TextView mTv;
    	int mTmp;

        mGameAddRoundPlayerState.clear();
    	
		mLayout = (LinearLayout)rootView.findViewById(R.id.game_add_round_playersection);

        LinearLayout mPointGrid = (LinearLayout)rootView.findViewById(R.id.point_grid);
        setupGridPointButtonsToEditValues(mPointGrid, mEtNewRoundPoints);


		mTmp = (int) ((double)mGame.getPlayerCount()/2 + 0.5d);
		for(int i=0;i<(DokoData.MAX_PLAYER/2) && i<mTmp ;i++){
			mLl = (LinearLayout)inflater.inflate(R.layout.fragment_game_add_round_2_player_row, null);
			mLayout.addView(mLl);

            // left
            LinearLayout mLeftLayout = (LinearLayout)mLl.findViewById(R.id.game_add_round_player_left);

            View mPlayerColor = mLl.findViewById(R.id.game_add_round_playercolor_left);
            mPlayerColor.setBackgroundColor(rootView.getContext().getResources().getColor(DokoData.PLAYERS_COLORS_KEY[i * 2]));

			mTv = (TextView)mLl.findViewById(R.id.game_add_round_playername_left);

			mTv.setText(mGame.getPlayer(i*2).getName());


            mLeftLayout.setOnClickListener(mAddRoundPlayernameClickListener);
			if(mGame.getPlayerCount()-mGame.getActivePlayerCount() > 0) {
                mLeftLayout.setOnLongClickListener(mAddRoundPlayernameLongClickListener);
			}

			TextView mLeftStateView = (TextView)mLeftLayout.findViewById(R.id.game_add_round_player_left_state);
            mGameAddRoundPlayerState.add(mLeftStateView);

            // right
            LinearLayout mRightLayout = (LinearLayout)mLl.findViewById(R.id.game_add_round_player_right);

            mPlayerColor = mLl.findViewById(R.id.game_add_round_playercolor_right);
            mPlayerColor.setBackgroundColor(rootView.getContext().getResources().getColor(DokoData.PLAYERS_COLORS_KEY[i * 2+ 1]));

			mTv = (TextView)mLl.findViewById(R.id.game_add_round_playername_right);
			
			if(mGame.getPlayerCount() == 5 && i == 2){
                mRightLayout.setVisibility(View.GONE);
			} else if(mGame.getPlayerCount() == 7 && i == 3){
                mRightLayout.setVisibility(View.GONE);
			} else{
				mTv.setText(mGame.getPlayer(i*2+1).getName());

                mRightLayout.setOnClickListener(mAddRoundPlayernameClickListener);
                if(mGame.getPlayerCount()-mGame.getActivePlayerCount() > 0) {
                    mRightLayout.setOnLongClickListener(mAddRoundPlayernameLongClickListener);
                }
                TextView mRightStateView = (TextView)mRightLayout.findViewById(R.id.game_add_round_player_right_state);
                mGameAddRoundPlayerState.add(mRightStateView);

			}
		}
	}


    public static void setupGridPointButtonsToEditValues(LinearLayout grid, final TextView valueField) {
        OnClickListener clickPoints = new OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView)v;
                valueField.setText(valueField.getText() + tv.getText().toString());
            }
        };

        TextView v = (TextView)grid.findViewById(R.id.point_grid_top_left);
        v.setOnClickListener(clickPoints);

        v = (TextView)grid.findViewById(R.id.point_grid_top_center);
        v.setOnClickListener(clickPoints);

        v = (TextView)grid.findViewById(R.id.point_grid_top_right);
        v.setOnClickListener(clickPoints);

        v = (TextView)grid.findViewById(R.id.point_grid_center_left);
        v.setOnClickListener(clickPoints);

        v = (TextView)grid.findViewById(R.id.point_grid_center_center);
        v.setOnClickListener(clickPoints);

        v = (TextView)grid.findViewById(R.id.point_grid_center_right);
        v.setOnClickListener(clickPoints);

        v = (TextView)grid.findViewById(R.id.point_grid_bottom_left);
        v.setOnClickListener(clickPoints);

        v = (TextView)grid.findViewById(R.id.point_grid_bottom_center);
        v.setOnClickListener(clickPoints);

        v = (TextView)grid.findViewById(R.id.point_grid_bottom_right);
        v.setOnClickListener(clickPoints);

        v = (TextView)grid.findViewById(R.id.point_grid_footer_center);
        v.setOnClickListener(clickPoints);

        v = (TextView)grid.findViewById(R.id.point_grid_footer_right);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                valueField.setText("");
            }
        });
    }

    
    private void setSpinnerValues(){
    	int mSelction;
    	
    	mSelction = mSpActivePlayer.getSelectedItemPosition();
    	mActivePlayerArrayList.clear();
    	for(int k=DokoData.MIN_PLAYER;k<=mPlayerCnt;k++) mActivePlayerArrayList.add(k);
    	mSPActivePlayerArrayAdapter = new ArrayAdapter<Integer>(this, R.layout.spinner_item,R.id.spinner_text,mActivePlayerArrayList);
        mSPActivePlayerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        mSpActivePlayer.setAdapter(mSPActivePlayerArrayAdapter);

   	    
   	    if(mSpActivePlayer.getAdapter().getCount() > mSelction) mSpActivePlayer.setSelection(mSelction);
   	    
   	 	mSelction = mSpBockLimit.getSelectedItemPosition();
    	mBockLimitArrayList.clear();
    	for(int k=0;k<=mPlayerCnt;k++) mBockLimitArrayList.add(k);
    	mSPBockLimitArrayAdapter = new ArrayAdapter<Integer>(this, R.layout.spinner_item,R.id.spinner_text,mBockLimitArrayList);
        mSPBockLimitArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
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
    	    	   ArrayAdapter<String> adapter =  new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,DokoData.PLAYER_NAMES);
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


	private void updatePlayerCnt(){
    	View v;
    	mLayout = (LinearLayout)findViewById(R.id.player_view_holder);
    	mPlayerCnt = 0;
    	for(int i=0;i<mLayout.getChildCount();i++){
    	    v = mLayout.getChildAt(i);
    	    if (v.getId() == R.id.player_entry) mPlayerCnt++;
    	}
    	mTvPlayerCnt.setText(String.valueOf(mPlayerCnt));
    	
    	setSpinnerValues();
    }
	
	private class btnStartClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if(!isPlayerNameSet(true)){
				Toast.makeText(v.getContext(), R.string.str_error_player_name, Toast.LENGTH_SHORT).show();
				return;
			}
		}
	}
	
	public class btnAddRoundClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if(mGame.getRoundCount() == 0){
				if(mLvRoundAdapter.getRoundListViewMode() == GAME_VIEW_TYPE.ROUND_VIEW_TABLE){
					mGameRoundsInfoSwipe.removeAllViews();
					createTableHeader(mInflater, mContext);
				}
				else if(mLvRoundAdapter.getRoundListViewMode() == GAME_VIEW_TYPE.ROUND_VIEW_DETAIL){ 
					mGameRoundsInfoSwipe.removeAllViews();
				}
			}
			

			if(!isNewRoundDataOK()){
				Toast.makeText(v.getContext(), R.string.str_error_game_new_round_data, Toast.LENGTH_SHORT).show();
				return;
			}
			
			for(int i=0;i<mWinnerList.length;i++){
				Log.d(TAG,"i:"+i+" win: "+mWinnerList[i]+"~ suspend: "+mSuspendList[i]);
			}
			
			Log.d(TAG,getNewRoundPoints()+"-"+mGame.toString());
			mGame.addNewRound(getNewRoundPoints(),isNewBockRoundSet(),mWinnerList,mSuspendList);
			Log.d(TAG,mGame.toString());
			notifyDataSetChanged();
			 
			
			mViewPager.setCurrentItem(0,true);
			mLvRounds.requestFocus();
			
			if(mLvRounds.getCount() >= 1)
				mLvRounds.setSelection(mLvRounds.getCount()-1);
			
			resetNewRoundFields(mContext);
			
			DokoXMLClass.saveGameStateToXML(mContext, mGame);
			
			setBottomInfo(mContext);
		}
	}
	
	private void  notifyDataSetChanged() {
		if(mLvRounds.getAdapter() instanceof ArrayAdapter<?>)((ArrayAdapter<?>) mLvRoundAdapter).notifyDataSetChanged();
	}
	
	
	private static void createTableHeader(LayoutInflater inflater, Context context) {
		LinearLayout mLl = null;
		TextView mTv = null;
		switch(mGame.getPlayerCount()){
			case 4: mLl = (LinearLayout) inflater.inflate(R.layout.fragment_game_round_view_table_4_player_header, null); break;
			case 5: mLl = (LinearLayout) inflater.inflate(R.layout.fragment_game_round_view_table_5_player_header, null); break;
			case 6: mLl = (LinearLayout) inflater.inflate(R.layout.fragment_game_round_view_table_6_player_header, null); break;
			case 7: mLl = (LinearLayout) inflater.inflate(R.layout.fragment_game_round_view_table_7_player_header, null); break;
			case 8: mLl = (LinearLayout) inflater.inflate(R.layout.fragment_game_round_view_table_8_player_header, null); break;
		}
		if(mLl ==  null || DokoData.mTvTablePlayerName.length < mGame.getPlayerCount()) {
            return;
        }
		
		for(int i=0;i<mGame.getPlayerCount();i++){
			mTv = (TextView)mLl.findViewById(DokoData.mTvTablePlayerName[i]);
			mTv.setText(mGame.getPlayer(i).getName());

            View color = mLl.findViewById(DokoData.mTvTablePlayerNameColor[i]);
            color.setBackgroundColor(color.getResources().getColor(DokoData.PLAYERS_COLORS_KEY[i]));
		}
		mGameRoundsInfoSwipe.removeAllViews();
		mGameRoundsInfoSwipe.addView(mLl);
		mGameRoundsInfoSwipe.setGravity(Gravity.LEFT);
	}
	
	public class GameAddRoundPlayernameClickListener implements OnClickListener{
		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View v) {
			if(mGameAddRoundPlayerState.size() > mSuspendList.length) {
                Log.e(TAG, "error Array" + mGameAddRoundPlayerState.size() + "#" + mSuspendList.length);
            }
			for(int i=0;i<mGameAddRoundPlayerState.size();i++){
                TextView mTvState = mGameAddRoundPlayerState.get(i);
                // maybe right or left
                TextView mTvStateOfView = (TextView)v.findViewById(R.id.game_add_round_player_left_state);
                if (mTvStateOfView == null) {
                    mTvStateOfView = (TextView)v.findViewById(R.id.game_add_round_player_right_state);
                }

				if(mTvState != null && mTvStateOfView == mTvState && mSuspendList[i]==0){
					if(mWinnerList[i] == 0 && getWinnerCnt() < mGame.getActivePlayerCount()-1){

                        changePlayerViewState(mTvState, winnerDraw, R.string.str_game_points_winner_select_text, YES);
						mWinnerList[i] = 1;
					}
					else{
						mWinnerList[i] = 0;
                        changePlayerViewState(mTvState, loserDraw, R.string.str_game_points_lose_select_text, YES);
					}
				}
			}
		}
    }
	
	public class GameAddRoundPlayernameLongClickListener implements OnLongClickListener{
		@SuppressWarnings("deprecation")
		@Override
		public boolean onLongClick(View v) {
			for(int i=0;i<mGameAddRoundPlayerState.size();i++){
                TextView mTvState = mGameAddRoundPlayerState.get(i);

                // maybe right or left
                TextView mTvStateOfView = (TextView)v.findViewById(R.id.game_add_round_player_left_state);
                if (mTvStateOfView == null) {
                    mTvStateOfView = (TextView)v.findViewById(R.id.game_add_round_player_right_state);
                }

                if(mTvState != null && mTvStateOfView == mTvState && mWinnerList[i]==0){
					if(mSuspendList[i] == 0 && getSuspendCnt() < mGame.getPlayerCount()-mGame.getActivePlayerCount()){
                        changePlayerViewState(mTvState, suspendDraw, R.string.str_game_points_suspend_select_text, YES);
						mSuspendList[i] = 1;
					}
					else{
						mSuspendList[i] = 0;
                        changePlayerViewState(mTvState, loserDraw, R.string.str_game_points_lose_select_text, YES);
					}
				}
			}
			return true;	
		}
    }

    private void changePlayerViewState(TextView mTvStateView, Drawable newDrawable, int stringID, boolean animate) {
        changePlayerViewState(mTvStateView, newDrawable, stringID, animate, mContext);
    }

    private static void changePlayerViewState(TextView mTvStateView, Drawable newDrawable, int stringID, boolean animate, Context context) {
        if (animate) {
            Drawable backgrounds[] = new Drawable[2];
            Resources res = context.getResources();
            backgrounds[0] = mTvStateView.getBackground();
            backgrounds[1] = newDrawable;

            TransitionDrawable crossfader = new TransitionDrawable(backgrounds);
            crossfader.setCrossFadeEnabled(true);

            mTvStateView.setBackgroundDrawable(crossfader);

            crossfader.startTransition(600);
        } else {
            mTvStateView.setBackgroundDrawable(newDrawable);
        }

        mTvStateView.setText(context.getResources().getString(stringID));
    }
	
	private boolean isNewRoundDataOK() {
		if(getNewRoundPoints() == -1) return false;
		if(getNewRoundPoints() != 0 && (!isWinnerCntOK() || !isSuspendCntOK())) return false;
		return true;
	}
	
	private static  void resetNewRoundFields(Context context) {
		TextView mTv = null;
		mEtNewRoundPoints.setText("");
		for(int i=0;i<mGame.getMAXPlayerCount();i++){
			mSuspendList[i] = 0;
			mWinnerList[i] = 0;
		}
		
		for(int i=0;i<mGameAddRoundPlayerState.size();i++){
			mTv = mGameAddRoundPlayerState.get(i);
            changePlayerViewState(mTv, loserDraw, R.string.str_game_points_lose_select_text, NO, context);
		}

        mCBNewBockRound.setChecked(false);

		if (mBtnAddRound != null && mBtnAddRound.getParent() != null && mBtnAddRound.getParent().getParent() != null && (mBtnAddRound.getParent().getParent() instanceof ScrollView)) {
			ScrollView sv = (ScrollView)mBtnAddRound.getParent().getParent();
			sv.fullScroll(ScrollView.FOCUS_UP);
		}
	}


	private boolean isNewBockRoundSet(){
        return mCBNewBockRound.isChecked();
	}

    private int getNewRoundPoints(){
        int mPoints;
        try{
            mPoints = Integer.valueOf(mEtNewRoundPoints.getText().toString());
            if (!mGame.isAutoBockCalculationOn() && mGame.getPreRoundList().size() > 0) {
                int mBockCountRound = mGame.getPreRoundList().get(0).getBockCount();
                if (mBockCountRound > 0) {
                    mPoints /= (mGame.getPreRoundList().get(0).getBockCount() * 2);
                }
            }
            return mPoints;
        }
        catch(Exception e){
            return -1;
        }
    }
	

	
	private boolean isSuspendCntOK(){
		if(mGame.getPlayerCount()-mGame.getActivePlayerCount() == 0) return true;
		if(getSuspendCnt() == (mGame.getPlayerCount()-mGame.getActivePlayerCount())) return true;
		return false;
	}
	
	private boolean isWinnerCntOK(){
		int mWinnerCnt = getWinnerCnt();
		if(mWinnerCnt >= mGame.getActivePlayerCount() || mWinnerCnt == 0) return false;
		return true;
	}
	
	private int getSuspendCnt(){
		int m = 0;
		for(int i=0;i<mSuspendList.length;i++){
			if(mSuspendList[i] == 1) m++;
		}
		return m;
	}
	
	private int getWinnerCnt(){
		int m = 0;
		for(int i=0;i<mWinnerList.length;i++){
			if(mWinnerList[i] == 1) m++;
		}
		return m;
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
    

    
    private void saveStateData(Bundle outState){
    	if(mGame != null){
	    	ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
	    	try {
	    		ObjectOutput out1 = new ObjectOutputStream(bos1);
				out1.writeObject(mGame);
		    	out1.flush();
		    	out1.close();
		    	outState.putByteArray("GAME_KEY", bos1.toByteArray());
			} 
	    	catch (IOException e) {    	
				e.printStackTrace();
			}	
    	}
    }
    

	private GameClass loadStateData(Bundle savedState){
		GameClass mGame = null;
    	if(savedState != null){
			if(savedState.containsKey("GAME_KEY")){
				ObjectInputStream objectIn;
				try {
					objectIn = new ObjectInputStream(new ByteArrayInputStream(savedState.getByteArray("GAME_KEY")));
					Object obj = objectIn.readObject();
					mGame = (GameClass) obj;

					objectIn.close();
				} 
				catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}
    	return mGame;
    }

	private void showExitDialog(){
		Builder back = new AlertDialog.Builder(this);
		back.setTitle(R.string.str_exit_game);
		back.setMessage(R.string.str_exit_game_q);
		back.setPositiveButton(R.string.str_yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				finish();
			}
		});

		back.setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		});
		back.show();
	}
	

	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game, menu);
        return true;
    }
    
    
    @Override
  	public boolean onOptionsItemSelected(MenuItem item){
        Intent i;
    	switch(item.getItemId()) {
//    		case R.id.menu_switch_game_list_view:
//    			GAME_VIEW_TYPE mRoundListViewMode = mLvRoundAdapter.getRoundListViewMode();
//	    		if(mRoundListViewMode == GAME_VIEW_TYPE.ROUND_VIEW_DETAIL){
//	    			createTableHeader(mInflater, mContext);
//	    			mLvRoundAdapter.changeRoundListViewMode(GAME_VIEW_TYPE.ROUND_VIEW_TABLE);
//	    		}
//	    		else{
//	    			mGameRoundsInfoSwipe.removeAllViews();
//	    			mLvRoundAdapter.changeRoundListViewMode(GAME_VIEW_TYPE.ROUND_VIEW_DETAIL);
//	    		}
//
//	    		notifyDataSetChanged();
//				mLvRounds.requestFocus();
//
//				if(mLvRounds.getCount() >= 1)
//					mLvRounds.setSelection(mLvRounds.getCount()-1);
//    		break;
    		
    		case R.id.menu_change_game_settings:
    			i = new Intent(this, ChangeGameSettingActivity.class);
    			for(int k=0;k<mGame.getPlayerCount();k++){
    				i.putExtra(DokoData.PLAYERS_KEY[k], mGame.getPlayer(k).getName());
    			}
    			i.putExtra(DokoData.PLAYER_CNT_KEY, mGame.getPlayerCount());
    			i.putExtra(DokoData.BOCKLIMIT_KEY, mGame.getBockRoundLimit());
    			i.putExtra(DokoData.ACTIVE_PLAYER_KEY, mGame.getActivePlayerCount());
                i.putExtra(DokoData.AUTO_BOCK_CALC_KEY, mGame.isAutoBockCalculationOn());
                i.putExtra(DokoData.MARK_SUSPEND_OPTION_KEY, mGame.isMarkSuspendedPlayersEnable());
    			startActivityForResult(i,DokoData.CHANGE_GAME_SETTINGS_ACTIVITY_CODE);
    		break;
    		
//    		case R.id.menu_bock_preview_on_off:
//    			if(mBockPreviewOnOff){
//    				mBockPreviewOnOff = false;
//					mBottomInfos.animate()
//							.translationY(mBottomInfos.getHeight())
//							.alpha(0.0f)
//							.setDuration(300)
//							.setListener(new AnimatorListenerAdapter() {
//								@Override
//								public void onAnimationEnd(Animator animation) {
//									super.onAnimationEnd(animation);
//									mBottomInfos.setVisibility(View.GONE);
//								}
//							});
//    			}
//    			else{
//    				if(mGame != null && mGame.getBockRoundLimit() == 0){
//    					Toast.makeText(mContext, getResources().getString(R.string.str_bock_preview_not_posible), Toast.LENGTH_LONG).show();
//    					return true;
//    				}
//    				mBockPreviewOnOff = true;
//					mBottomInfos.animate()
//							.translationY(0)
//							.alpha(1.0f)
//							.setDuration(300)
//							.setListener(new AnimatorListenerAdapter() {
//								@Override
//								public void onAnimationEnd(Animator animation) {
//									super.onAnimationEnd(animation);
//									mBottomInfos.setVisibility(View.VISIBLE);
//								}
//							});
//    			}
//    		break;
    		
    		case R.id.menu_edit_round:
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                builder.setTitle(R.string.str_hint);
                builder.setMessage(getResources().getString(R.string.str_edit_round_info));
                builder.setPositiveButton(R.string.str_accept, null);
                android.support.v7.app.AlertDialog dialog = builder.show();

                // Must call show() prior to fetching text view
                TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
                messageView.setGravity(Gravity.LEFT);

    		break;

            case R.id.menu_game_result:
                i = new Intent(this, GameResultActivity.class);
                for(int k=0;k<mGame.getPlayerCount();k++){
                    i.putExtra(DokoData.PLAYERS_KEY[k], mGame.getPlayer(k).getName());
                    i.putExtra(DokoData.PLAYERS_POINTS_KEY[k], mGame.getPlayer(k).getPoints());
                }
                i.putExtra(DokoData.PLAYER_CNT_KEY, mGame.getPlayerCount());

                startActivityForResult(i,DokoData.GAME_RESULT_ACTIVITY);
                break;
    		
    		case R.id.menu_exit_game:
    			showExitDialog();
    		break;

            case R.id.menu_game_help:
                showAlertDialog(R.string.str_help, R.string.str_info_game);
    	}
    	return true;
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);

    	switch (requestCode) {
			case DokoData.CHANGE_GAME_SETTINGS_ACTIVITY_CODE:
				handleChangeGameSettingsFinish(requestCode, resultCode, data);
				break;

			case DokoData.EDIT_ROUND_ACTIVITY_CODE:
				handleEditRoundFinish(requestCode, resultCode, data);

            case DokoData.GAME_RESULT_ACTIVITY:
				break;

            default:
                break;
		}
    }
    
    private void handleEditRoundFinish(int requestCode, int resultCode, Intent data) {
    	Bundle extras = null;
    	int mNewRoundPoints;
        int mTmpWinnerList[] = new int[DokoData.MAX_PLAYER];
        int mTmpSuspendList[] = new int[DokoData.MAX_PLAYER];
		PLAYER_ROUND_RESULT_STATE mPlayerRoundState = PLAYER_ROUND_RESULT_STATE.WIN_STATE;
		
    	if(data != null) extras = data.getExtras();
    	if(extras != null && extras.getBoolean(DokoData.CHANGE_ROUND_KEY,false)){
    		mNewRoundPoints = extras.getInt(DokoData.ROUND_POINTS_KEY,0);
    		//Log.d("GA before",mGame.toString() + " new points:"+mNewRoundPoints);
    		int mTmpState;
        	for(int k=0; k<mPlayerCnt; k++){
        		mTmpState = extras.getInt(DokoData.PLAYERS_KEY[k]+"_STATE",-1);
        		if (mTmpState == -1 || PLAYER_ROUND_RESULT_STATE.valueOf(mTmpState) == null) {
        			Toast.makeText(mContext, getResources().getString(R.string.str_edit_round_error), Toast.LENGTH_LONG).show();
        			return;
        		} else {
        			mTmpWinnerList[k] = 0; // lose default
        			mTmpSuspendList[k] = 0; // not suspending  default
        			mPlayerRoundState = PLAYER_ROUND_RESULT_STATE.valueOf(mTmpState);
        			//Log.d("GA EDIT:","player:"+mGame.getPlayer(k).getName()+" with state: "+mPlayerRoundState+", plcnt:"+mPlayerCnt);
        			switch (mPlayerRoundState) {
						case WIN_STATE: mTmpWinnerList[k] = 1;	break;
						case SUSPEND_STATE: mTmpSuspendList[k] = 1;	break;
						default:
							break;
					}
        		}
        	}

        	mGame.editLastRound(mNewRoundPoints, false, mTmpWinnerList, mTmpSuspendList);
        	reloadSwipeViews(); 
        	//Log.d("GA after",mGame.toString());
        	DokoXMLClass.saveGameStateToXML(mContext, mGame);
        	
        	Toast.makeText(mContext, getResources().getString(R.string.str_edit_round_finish), Toast.LENGTH_LONG).show();
        }
    }
    
    private void handleChangeGameSettingsFinish(int requestCode, int resultCode, Intent data) {
    	Bundle extras = null;
    	int mActivePlayers,mBockLimit,mPlayerCnt,mOldPlayerCnt;
    	String mName = "";
        boolean mBockAutoCalc;
        boolean mMarkSuspendedPlayers;
    	    	   	
    	if(data != null) extras = data.getExtras();
    	if(extras != null && extras.getBoolean(DokoData.CHANGE_GAME_SETTINGS_KEY,false)){
    		mPlayerCnt = extras.getInt(DokoData.PLAYER_CNT_KEY,0);
        	mActivePlayers =  extras.getInt(DokoData.ACTIVE_PLAYER_KEY,0);
        	mBockLimit = extras.getInt(DokoData.BOCKLIMIT_KEY,0);
            mBockAutoCalc = extras.getBoolean(DokoData.AUTO_BOCK_CALC_KEY, true);
            mMarkSuspendedPlayers = extras.getBoolean(DokoData.MARK_SUSPEND_OPTION_KEY, true);
        	
        	if(mPlayerCnt < DokoData.MIN_PLAYER || mPlayerCnt > DokoData.MAX_PLAYER 
        			|| mActivePlayers > mPlayerCnt || mActivePlayers < DokoData.MIN_PLAYER
        			|| (mPlayerCnt == 0 || mActivePlayers == 0))
        		return;
        	
        	//set new game settings
        	mOldPlayerCnt = mGame.getPlayerCount(); 
        	mGame.setPlayerCount(mPlayerCnt);
        	mGame.setActivePlayerCount(mActivePlayers);
        	mGame.setBockRoundLimit(mBockLimit);
            mGame.setAutoBockCalculation(mBockAutoCalc);
            mGame.setMarkSuspendedPlayers(mMarkSuspendedPlayers);


        	
        	for(int k=mOldPlayerCnt;k<mPlayerCnt;k++){
        		mName = extras.getString(DokoData.PLAYERS_KEY[k],"");
        		if(mName == null || mName.length() == 0) return;
        		mGame.getPlayer(k).setName(mName);
        	}

        	reloadSwipeViews(); 
        	DokoXMLClass.saveGameStateToXML(mContext, mGame);
        	
        	Toast.makeText(mContext, getResources().getString(R.string.str_change_game_settings_finish), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onBackPressed(){
    	showExitDialog();
    }
    
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
		saveStateData(outState);
	    super.onSaveInstanceState(outState);
    }
    
    
    
    
}
