package nldoko.game.game;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import nldoko.game.R;
import nldoko.game.XML.DokoXMLClass;
import nldoko.game.classes.GameClass;
import nldoko.game.classes.PlayerClass;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class SavedGameListActivity extends Activity {
	private Context mContext;
	
	private String TAG = "SavedGameList";
	
	private ActionBar mActionBar;
	private LinearLayout mEntriesLayout;
	private LayoutInflater inflater;
	
	private static String[] fileList;

	private static int SAVED_GAME_TAG  = 2611;
	private static int SAVED_GAME_TAG_DELETE  = 4211;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_games);
        mContext = this;
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mActionBar = getActionBar();
        mActionBar.show();
        mActionBar.setTitle(getResources().getString(R.string.str_saved_game));
        mActionBar.setDisplayHomeAsUpEnabled(true);       
        reload();
        
        overridePendingTransition(R.anim.right_out, R.anim.left_in);
    }

    private void reload() {
    	fileList = getFileList();
    	setUI();
    }

    private String[] getFileList (){
        // add external files
        ArrayList<String> fileList = new ArrayList<String>();

        // search all external dirs
        for (String sdcard : DokoXMLClass.getPossibleExternalStorageDirs()) {
            File dir = new File(sdcard +  File.separatorChar + DokoXMLClass.APP_DIR_GAMES);
            addSavedGamesFromDir(dir, fileList);
        }

        // add system mounted sdcard but need to check absolute path against present to avoid double entries
        if(DokoXMLClass.isExternalStorageReady()) {
            File dir = new File(Environment.getExternalStorageDirectory(), DokoXMLClass.APP_DIR_GAMES);
            addSavedGamesFromDir(dir, fileList);
        }

        // add internal files
        File dir = new File(DokoXMLClass.getAppDir(this));
        addSavedGamesFromDir(dir, fileList);

        dir = new File(DokoXMLClass.getAppDir(this) + File.separatorChar + "files");
        addSavedGamesFromDir(dir, fileList);

        dir = mContext.getFilesDir();
        addSavedGamesFromDir(dir, fileList);

        return fileList.toArray(new String[fileList.size()]);
    }

    private static void addSavedGamesFromDir(File dir, ArrayList<String> fileList) {
        if (dir.exists() && dir.isDirectory()) {
            File[] savedGameFiles = dir.listFiles();
            for (File f : savedGameFiles){
                if (f.getAbsolutePath().endsWith(DokoXMLClass.SAVED_GAME_FILE_SUFFIX) &&!fileList.contains(f.getAbsolutePath())) {
                    fileList.add(f.getAbsolutePath());
                }
            }
        }
    }

    private void setUI() {
    	int tagCnt = 0;
    	mEntriesLayout = (LinearLayout)findViewById(R.id.saved_games_entries);
    	mEntriesLayout.removeAllViewsInLayout();

    	FileClickListerner mFileClickListerner = new FileClickListerner();
    	FileDeleteClickListener mFileDeleteClickListerner = new FileDeleteClickListener();
    	
    	Arrays.sort(fileList,Collections.reverseOrder());
    	if (fileList != null){
    		View v;
    		TextView mTv;
    		ImageView mIv;
    	    for (String savedGameFile : fileList){
    	    	Log.d("e",savedGameFile);

    	    	if (!savedGameFile.endsWith(DokoXMLClass.SAVED_GAME_FILE_SUFFIX)) {
    	    		tagCnt++;
    	    		continue;
    	    	}
    			v = inflater.inflate(R.layout.saved_game_entry, null);
    			    			
    			LinearLayout l = (LinearLayout)v.findViewById(R.id.saved_game_entry_layout_start_game);
    			l.setOnClickListener(mFileClickListerner);
    			l.setTag(SAVED_GAME_TAG + tagCnt);

                String basename = savedGameFile.substring( savedGameFile.lastIndexOf('/')+1, savedGameFile.length() );
    			String[] arr = basename.split("_");
    			 
    			mTv = (TextView)v.findViewById(R.id.saved_game_entry_filename);
    			if (arr.length > 5) {
    				mTv.setText(arr[0] + "." + arr[1] + "." + arr[2] + " - " + arr[3] + ":" + arr[4] + ":" + arr[5]);
    			} else {
    				mTv.setText(savedGameFile);
    			}

                mTv = (TextView)v.findViewById(R.id.saved_game_entry_path_filepath);
                mTv.setText(savedGameFile);
    			
            	GameClass mGame =  DokoXMLClass.restoreGameStateFromXML(this,savedGameFile);
            	if (mGame != null) {
            		// if success delete old file

                    String createDate =  mGame.getCreateDate("dd.MM.yyyy - HH:mm");
                    Log.d(TAG,"cd:"+createDate);
                    if (createDate.length() > 0) {
                        l = (LinearLayout)v.findViewById(R.id.saved_game_entry_create_date_layout);
                        l.setVisibility(View.VISIBLE);
                        mTv = (TextView)v.findViewById(R.id.saved_game_entry_create_date_date);
                        mTv.setText(createDate);
                    } else {
                        l = (LinearLayout)v.findViewById(R.id.saved_game_entry_create_date_layout);
                        l.setVisibility(View.GONE);
                    }


            		String gameStats = "";
            		for(PlayerClass p : mGame.getPlayers()) {
            			if (p.getName().length() > 0) {
            				gameStats += p.getName()+" ("+p.getPoints()+"), ";
            			}
            		}

                    mTv = (TextView)v.findViewById(R.id.saved_game_entry_text);
        			mTv.setText(gameStats);
            	}
            	
            	mIv = (ImageView)v.findViewById(R.id.saved_game_entry_delete);
            	mIv.setTag(SAVED_GAME_TAG_DELETE + tagCnt);
            	mIv.setOnClickListener(mFileDeleteClickListerner);
                mIv.setColorFilter(mContext.getResources().getColor(R.color.red_dark), PorterDuff.Mode.SRC_ATOP);
            	
            	Log.d(TAG,"set tagcnt:"+tagCnt);
    			mEntriesLayout.addView(v);  
    			tagCnt++;
    	    }
    	}    	
	} 
    
    private class FileDeleteClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			int tagCnt = (Integer)v.getTag();
			tagCnt -= SAVED_GAME_TAG_DELETE;
			if (fileList.length > tagCnt) {
				showDeleteSavedGamesDialog(fileList[tagCnt]);
			}
		}
	};
            	
    private class FileClickListerner implements OnClickListener{
		@Override
		public void onClick(View v) {
			int tagCnt = (Integer)v.getTag();
			tagCnt -= SAVED_GAME_TAG;
			Log.d(TAG,"get tag cnt:"+tagCnt);
			if (fileList.length > tagCnt) {
				String filename = fileList[tagCnt];
				if (filename.length() == 0) return;
	    		Intent i = new Intent(mContext, GameActivity.class);
	    		i.putExtra("RestoreGameFromXML", true);
	    		i.putExtra("filename", filename);
	    		startActivity(i);
	    		finish();
			}		
		}
    }
    
	private void showDeleteAllSavedGamesDialog(){
		Builder back = new AlertDialog.Builder(this);
		back.setTitle(R.string.str_delete_files);
		back.setMessage(R.string.str_saved_game_delete_all_q);
		back.setPositiveButton(R.string.str_yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				for(String filepath : fileList) {
					File f = new File(filepath);
                    if (f.exists() && !f.isDirectory() && f.getAbsolutePath().endsWith(DokoXMLClass.SAVED_GAME_FILE_SUFFIX)) {
                        f.delete();
                    }
				}
				reload();
			}
		});

		back.setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		});
		back.show();
	}
	
	private void showDeleteSavedGamesDialog(String file){
		final String filepath = file;
		Builder back = new AlertDialog.Builder(this);
		back.setTitle(R.string.str_delete_file);
		back.setMessage(R.string.str_saved_game_delete_q);
		back.setPositiveButton(R.string.str_yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
                File f = new File(filepath);
                if (f.exists() && !f.isDirectory() && f.getAbsolutePath().endsWith(DokoXMLClass.SAVED_GAME_FILE_SUFFIX)) {
                    f.delete();
                }
				reload();
			}
		});

		back.setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		});
		back.show();
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
            
    	case R.id.action_delete_all_saved_games_menu:
    		showDeleteAllSavedGamesDialog();
    		return true;
    		
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.saved_game, menu);
        return true;
    }
    
    
}
