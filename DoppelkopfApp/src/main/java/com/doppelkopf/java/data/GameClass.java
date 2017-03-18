package com.doppelkopf.java.data;

import android.util.Log;

import com.doppelkopf.java.data.DokoData.GAME_CNT_VARIANT;
import com.doppelkopf.java.XML.DokoXMLClass;


import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class GameClass  implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private GAME_CNT_VARIANT cntVariant;

    private boolean mAutoBockCalculation;
	
	private ArrayList<PlayerClass> mPlayers;
	private ArrayList<RoundClass> mRoundList;
	private ArrayList<RoundClass> mPreRoundList;
	
	private int mPlayerCount;
	private int mActivePlayerCount;
	private int mBockRoundLimit;
	private boolean mMarkSuspendedPlayers;
	private String mCurrentFilename;

    private java.sql.Timestamp createDate;

	public GameClass(String fromFile){
		setDefaults();
		this.mCurrentFilename = fromFile;
	}
	
	public GameClass(int playerCount, int activePlayer, int bockLimit, GAME_CNT_VARIANT cntVariant, boolean markSuspendedPlayers, boolean autoBockCalculation){
		this.mPlayers = new ArrayList<PlayerClass>();
		this.mRoundList = new ArrayList<RoundClass>();
		this.mPreRoundList = new ArrayList<RoundClass>();
    	this.mPlayerCount = playerCount;
    	this.mActivePlayerCount = activePlayer;    
    	this.mBockRoundLimit = bockLimit;
    	this.setMarkSuspendedPlayers(markSuspendedPlayers);

    	this.cntVariant = cntVariant;
    	
    	for(int i=0;i<getMAXPlayerCount();i++){
    		this.mPlayers.add(new PlayerClass(i));
    	}

        this.createDate = new Timestamp(System.currentTimeMillis());

        this.mAutoBockCalculation = autoBockCalculation;

	}
	
	private void setDefaults(){
		this.mPlayers = new ArrayList<PlayerClass>();
		this.mRoundList = new ArrayList<RoundClass>();
		this.mPreRoundList = new ArrayList<RoundClass>();
    	this.mPlayerCount = 0;
    	this.mActivePlayerCount = 0;    
    	this.mBockRoundLimit = 0;
    	this.cntVariant = GAME_CNT_VARIANT.CNT_VARIANT_NORMAL;
    	this.setMarkSuspendedPlayers(false);
	}
	
	public void setPlayers(ArrayList<PlayerClass> playerList){
		this.mPlayers = playerList;
	}


    public void setRoundList(ArrayList<RoundClass> roundList){
        this.mRoundList = roundList;
    }

    public void setPreRoundList(ArrayList<RoundClass> preRoundList){
        this.mPreRoundList = preRoundList;
    }

    public void setAutoBockCalculation(boolean autoBockCalculation) {
        this.mAutoBockCalculation = autoBockCalculation;
    }

    public boolean isAutoBockCalculationOn() {
        return this.mAutoBockCalculation;
    }

	public void setGameCntVariant(GAME_CNT_VARIANT variant){
		this.cntVariant = variant;
	}
		
	public PlayerClass getPlayer(int pos){
		return this.mPlayers.get(pos);
	}
	
	public ArrayList<PlayerClass> getPlayers(){
		return this.mPlayers;
	}
	
	public boolean isMarkSuspendedPlayersEnable() {
		return mMarkSuspendedPlayers;
	}

	public void setMarkSuspendedPlayers(boolean mMarkSuspendedPlayers) {
		this.mMarkSuspendedPlayers = mMarkSuspendedPlayers;
	}

	public void addRound(RoundClass round){
		this.mRoundList.add(round);
	}

	public int getRoundCount(){
		return this.mRoundList.size();
	}
		
	public ArrayList<RoundClass> getRoundList(){
		return mRoundList;
	}
	
	public ArrayList<RoundClass> getPreRoundList(){
		return mPreRoundList;
	}
	
	public ArrayList<RoundClass> mTmp(){
		return mPreRoundList;
	}
		
	public RoundClass getNewRound(){
		if((mBockRoundLimit == 0 || mPreRoundList.size() == 0) && mRoundList.size() != 0 ){
			return new RoundClass(mRoundList.get(mRoundList.size()-1).getID()+1, 0, 0);
		}
		else if(mPreRoundList.size() > 0){
			return getPreRound();
		}
		return new RoundClass(0, 0, 0);
	}
	
	private RoundClass getPreRound(){
		RoundClass r = this.mPreRoundList.get(0);
		mPreRoundList.remove(0);
		return r;
	}
	
	
	public void setActivePlayerCount(int activePlayerCount){
		this.mActivePlayerCount = activePlayerCount;
	}
	
	public int getActivePlayerCount(){
		return this.mActivePlayerCount;
	}
	
	public int getPlayerCount(){
		return this.mPlayerCount;
	}
	
	public void setPlayerCount(int playerCount){
		this.mPlayerCount = playerCount;
	}

	public int getBockRoundLimit(){
		return this.mBockRoundLimit;
	}
	
	public void setBockRoundLimit(int bockRoundLimit){
		this.mBockRoundLimit = bockRoundLimit;
	}
	
	public int getMAXPlayerCount(){
		return DokoData.MAX_PLAYER;
	}
	
	public GAME_CNT_VARIANT getGameCntVariant(){
		return this.cntVariant;
	}
	
	
	
	public boolean isReady(){
		if(mPlayers.size()== getMAXPlayerCount() && mPreRoundList.size() >= getMAXPlayerCount() ){
			return true;
		}
		return false;
	}

	public void updateBockCountPreRounds() {
		if(mBockRoundLimit == 0) return;
		else if(mBockRoundLimit <= getPlayerCount()){
			int mBockHeap = mPlayerCount;
			int i = 0;
			int mID = 0;
			RoundClass mRound;
			while(mBockHeap > 0){
				//Log.d(TAG,"i: "+i+" preSize:"+mPreRoundList.size()+ " heap"+mBockHeap);
				if(i >= mPreRoundList.size()){
					if(mPreRoundList.size() > 0){
						//Log.d(TAG,"1");
						mID = mPreRoundList.get(mPreRoundList.size()-1).getID()+1;
					}
					else if (mRoundList.size() > 0){
						//Log.d(TAG,"2");
						mID = mRoundList.get(mRoundList.size()-1).getID()+1;
					}
					else{
						//Log.d(TAG,"3");
						mID = 0;
					}
					//Log.d(TAG,"add");
					
					this.mPreRoundList.add(new RoundClass(mID,0,0));
				}
				mRound = mPreRoundList.get(i);
				if(mRound.getBockCount() < mBockRoundLimit){
					mRound.setBockCount(mRound.getBockCount()+1);
					mBockHeap--;
				}
				i++;
			}
		}		
	}
	
	public void editLastRound(int newRoundPoints, boolean isNewBockRoundSet, int[] mWinnerList, int[] mSuspendList) {
		RoundClass mRound = mRoundList.get(mRoundList.size()-1);
		
		if (mRound != null) {
			mRound.setPoints(newRoundPoints);
			mRound.setRoundType(getWinnerCnt(mWinnerList),mActivePlayerCount);
			updatePlayerPoints(mRound,mWinnerList,mSuspendList);
		}
	}

	public void addNewRound(int newRoundPoints, boolean isNewBockRoundSet, int[] mWinnerList, int[] mSuspendList) {
		RoundClass mRound = getNewRound();

		
		mRound.setPoints(newRoundPoints);
		mRound.setRoundType(getWinnerCnt(mWinnerList),mActivePlayerCount);
		//Log.d("GAMECLASS",mRound.getResultText());		
		updatePlayerPoints(mRound,mWinnerList,mSuspendList);
		addRound(mRound);

		if(isNewBockRoundSet)
			updateBockCountPreRounds();
	}
	
	private int getWinnerCnt(int[] mWinnerList){
		int m = 0;
		for(int i=0;i<mWinnerList.length;i++){
			if(mWinnerList[i] == 1) m++;
		}
		return m;
	}
	
	private void updatePlayerPoints(RoundClass mRound, int[] mWinnerList,int[] mSuspendList) {
		int mWinnerCnt = 0;
		int mSoloWinPos = 0;
		int mSoloLosePos = 0;
		
		
		for(int i=0;i<getPlayerCount();i++){
			if(mSuspendList[i] == 1)
				getPlayer(i).updatePoints(mRound.getID(),(float) 0);
			else if(mWinnerList[i] == 1){
				mWinnerCnt++;
				mSoloWinPos = i;
			}
			else mSoloLosePos = i;
		}
		
		
		if(mWinnerCnt == 1){
			//Win solo
			soloPointUpdate(mRound,true,mSoloWinPos,mSuspendList);
		}
		else if(mWinnerCnt == 3 && getActivePlayerCount() == 4){
			//Lose solo
			soloPointUpdate(mRound,false,mSoloLosePos,mSuspendList);
		}
		else if(mWinnerCnt == 4 && getActivePlayerCount() == 5){
			//Lose solo
			soloPointUpdate(mRound,false,mSoloLosePos,mSuspendList);
		}
		else if(mWinnerCnt == 3 && getActivePlayerCount() == 5){
			//3 win vs. 2 lose
			for(int i=0;i<getPlayerCount();i++){
				if(mWinnerList[i] == 1){
					//Win
					if(cntVariant == GAME_CNT_VARIANT.CNT_VARIANT_NORMAL || cntVariant == GAME_CNT_VARIANT.CNT_VARIANT_WIN)
						getPlayer(i).updatePoints(mRound.getID(),(float)mRound.getPoints());
					else getPlayer(i).updatePoints(mRound.getID(),(float)0);
				}
				else if(mSuspendList[i] != 1){
					if(cntVariant == GAME_CNT_VARIANT.CNT_VARIANT_NORMAL || cntVariant == GAME_CNT_VARIANT.CNT_VARIANT_LOSE)
						getPlayer(i).updatePoints(mRound.getID(),(float) ((float)mRound.getPoints()* 1.5 *-1));
					else getPlayer(i).updatePoints(mRound.getID(),(float)0);
				}
			}
		}
		else {
			//2 win vs. 3 lose || 2vs2
			float mFactor = 1;
			if(mWinnerCnt == 2 && getActivePlayerCount() == 5) mFactor = (float) 1.5;
			//Log.d("GAMECLASS",mWinnerCnt+"#"+getActivePlayerCount()+"#"+mFactor);
			
			for(int i=0;i<getPlayerCount();i++){
				if(mWinnerList[i] == 1){
					//Win
					if(cntVariant == GAME_CNT_VARIANT.CNT_VARIANT_NORMAL || cntVariant == GAME_CNT_VARIANT.CNT_VARIANT_WIN)
						getPlayer(i).updatePoints(mRound.getID(),(float)(mRound.getPoints()*mFactor));
					else getPlayer(i).updatePoints(mRound.getID(),(float)0);
				}
				else if(mSuspendList[i] != 1){
					if(cntVariant == GAME_CNT_VARIANT.CNT_VARIANT_NORMAL || cntVariant == GAME_CNT_VARIANT.CNT_VARIANT_LOSE)
						getPlayer(i).updatePoints(mRound.getID(),(float)(mRound.getPoints()*-1));
					else getPlayer(i).updatePoints(mRound.getID(),(float)0);
				}
			}
		}

		
		//Inactive player 
		for(int i=getPlayerCount();i<getMAXPlayerCount();i++){
			getPlayer(i).updatePoints(mRound.getID(),(float)0);
		}
	}

	private void soloPointUpdate(RoundClass mRound, boolean isSoloWinner, int mSoloPos, int[] mSuspendList) {
		int mPoints = 0;
		
		if(isSoloWinner)
			mPoints = mRound.getPoints()*-1;
		else
			mPoints = mRound.getPoints();

		
		for(int i=0; i<getPlayerCount();i++){
			if(i!=mSoloPos &&  mSuspendList[i] != 1){
				if(cntVariant == GAME_CNT_VARIANT.CNT_VARIANT_NORMAL || (isSoloWinner &&  cntVariant == GAME_CNT_VARIANT.CNT_VARIANT_LOSE)
						|| (!isSoloWinner &&  cntVariant == GAME_CNT_VARIANT.CNT_VARIANT_WIN))
					getPlayer(i).updatePoints(mRound.getID(),(float)mPoints);
				else getPlayer(i).updatePoints(mRound.getID(),(float)0);
			}	
		}
		if(cntVariant == GAME_CNT_VARIANT.CNT_VARIANT_NORMAL || (isSoloWinner &&  cntVariant == GAME_CNT_VARIANT.CNT_VARIANT_WIN)
				 || (!isSoloWinner &&  cntVariant == GAME_CNT_VARIANT.CNT_VARIANT_LOSE))
			getPlayer(mSoloPos).updatePoints(mRound.getID(),(float)(getActivePlayerCount()-1)*mPoints*-1);
		else getPlayer(mSoloPos).updatePoints(mRound.getID(),(float)0);
	}


	public String toString(){
		String mStr = "";
		mStr += "Active Player: "+mActivePlayerCount+" Bock Limit: "+mBockRoundLimit+" Player Count: "+mPlayerCount;
		for(int i=0;i<mPlayerCount;i++){
			mStr += " ("+mPlayers.get(i).getName()+" = "+mPlayers.get(i).getPoints()+")";
		}
		return mStr;
	}


    public void setCurrentFilename(String filepath) {
        mCurrentFilename = filepath;
    }
	public String currentFilename() {
		return mCurrentFilename;
	}
		
	public String generateNewFilename(){
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate + DokoXMLClass.SAVED_GAME_FILE_SUFFIX;
	}

    public String getCreateDate(String format) {
        if (this.createDate == null) {
            return "";
        }

        String f = "MM/dd/yyyy"; // default
        if (format.length() > 0 ) {
            f = format;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(f);
            return sdf.format(this.createDate);
        } catch (Exception e) {
            Log.d("GameClass", e.toString());
            return "";
        }

    }

    public java.sql.Timestamp getCreateDateTimestamp() {
        return this.createDate;
    }

    public void setCreateDate(java.sql.Timestamp timestamp) {
        this.createDate = timestamp;
    }


}