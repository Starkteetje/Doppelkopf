package com.doppelkopf.java.game;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.doppelkopf.java.DokoActivity;
import com.doppelkopf.java.R;
import com.doppelkopf.java.XML.DokoXMLClass;
import com.doppelkopf.java.data.GameClass;
import com.doppelkopf.java.data.PlayerClass;

public class SavedGameListActivity extends DokoActivity {

	private String TAG = "SavedGameList";

	private LinearLayout mEntriesLayout;
	
	private static String[] fileList;

    private boolean needToReloadUI;

	private static int SAVED_GAME_TAG  = 2611;
	private static int SAVED_GAME_TAG_DELETE  = 4211;

    private ProgressDialog progressDialog;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_saved_games);


        overridePendingTransition(R.anim.right_out, R.anim.left_in);


        needToReloadUI = true;

        setupDrawerAndToolbar(mContext.getResources().getString(R.string.str_saved_game));

        if (Build.VERSION.SDK_INT >= 23)
        {
            if (DokoXMLClass.checkPermissionWriteExternalStorage(mContext)) {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
            } else {
                DokoXMLClass.requestPermission(this); // Code for permission
            }
        }
    }

    public void onStart() {
        super.onStart();
        if (needToReloadUI) {
            needToReloadUI = false;
            reload();
        }
    }

    private void reload() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(SavedGameListActivity.this);
            progressDialog.setTitle(this.getResources().getString(R.string.str_plz_wait));
            progressDialog.setMessage(this.getResources().getString(R.string.str_saved_games_load));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
        }

        progressDialog.show();

        fileList = getFileList();
    	setUI();

        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) { }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
            }
        }.start();

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
            if (savedGameFiles != null) {
                for (File f : savedGameFiles) {
                    if (f.getAbsolutePath().endsWith(DokoXMLClass.SAVED_GAME_FILE_SUFFIX) && !fileList.contains(f.getAbsolutePath())) {
                        fileList.add(f.getAbsolutePath());
                    }
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
    			v = mInflater.inflate(R.layout.saved_game_entry, null);
    			    			
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
    			
            	GameClass mGame =  DokoXMLClass.restoreGameStateFromXML(this,savedGameFile, false);
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

                l = (LinearLayout)v.findViewById(R.id.saved_game_entry_icons);
                if (l != null) {
                    mIv = (ImageView)l.findViewById(R.id.saved_game_entry_icon_delete);
                    mIv.setTag(SAVED_GAME_TAG_DELETE + tagCnt);
                    mIv.setOnClickListener(mFileDeleteClickListerner);
                    mIv.setColorFilter(mContext.getResources().getColor(R.color.red_dark), PorterDuff.Mode.SRC_ATOP);

                    mIv = (ImageView)l.findViewById(R.id.saved_game_entry_icon_mail);
                    mIv.setOnClickListener(new FileMailClickListener(savedGameFile));
                    mIv.setColorFilter(mContext.getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
                }

            	
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

    private class FileMailClickListener implements OnClickListener {
        String savedGameFile;
        public FileMailClickListener(String file) {
            this.savedGameFile = file;
        }

        @Override
        public void onClick(View v) {
            GameClass mGame =  DokoXMLClass.restoreGameStateFromXML(v.getContext(), this.savedGameFile, true);
            DokoXMLClass.sendGameViaMail(v.getContext(), mGame);
        }
    };
    
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }
    
    
}
