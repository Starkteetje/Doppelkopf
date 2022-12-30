package nldoko.game.java.game;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
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
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nldoko.game.R;
import nldoko.game.java.DokoActivity;
import nldoko.game.java.XML.DokoXMLClass;
import nldoko.game.java.data.GameClass;
import nldoko.game.java.data.PlayerClass;
import nldoko.game.java.util.Uploader;

public class SavedGameListActivity extends DokoActivity {

    private final String TAG = "SavedGameList";

    private LinearLayout mEntriesLayout;

    private ArrayList<File> fileList;

    private boolean needToReloadUI;

    private static final int SAVED_GAME_TAG  = 2611;
    private static final int SAVED_GAME_TAG_DELETE  = 4211;

    private ProgressDialog progressDialog;

    private static final int PERMISSION_REQUEST_CODE = 1;

    private ArrayList<Uri> mailAttachments = null;

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

    private ArrayList<File> getFileList (){
        // add external files
        ArrayList<File> newFiles = new ArrayList<>();

        // search all external dirs
        for (String sdcard : DokoXMLClass.getPossibleExternalStorageDirs()) {
            File dir = new File(sdcard +  File.separatorChar + DokoXMLClass.APP_DIR_GAMES);
            addSavedGamesFromDir(dir, newFiles);
        }

        // add system mounted sdcard but need to check absolute path against present to avoid double entries
        if(DokoXMLClass.isExternalStorageReady()) {
            File dir = new File(Environment.getExternalStorageDirectory(), DokoXMLClass.APP_DIR_GAMES);
            addSavedGamesFromDir(dir, newFiles);
        }

        // add internal files
        File dir = new File(DokoXMLClass.getAppDir(this));
        addSavedGamesFromDir(dir, newFiles);

        dir = new File(DokoXMLClass.getAppDir(this) + File.separatorChar + "files");
        addSavedGamesFromDir(dir, newFiles);

        dir = mContext.getFilesDir();
        addSavedGamesFromDir(dir, newFiles);

        // sort by last modified date
        Collections.sort(newFiles, new Comparator<File>() {
            public int compare(File f1, File f2)
            {
                int r = Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
                return  r;
            }
        });

        return newFiles;
    }

    private static void addSavedGamesFromDir(File dir, ArrayList<File> fList) {
        if (dir.exists() && dir.isDirectory()) {
            File[] savedGameFiles = dir.listFiles();
            if (savedGameFiles != null) {
                for (File f : savedGameFiles) {
                    if (f.getAbsolutePath().endsWith(DokoXMLClass.SAVED_GAME_FILE_SUFFIX) && !fList.contains(f.getAbsolutePath())) {

                        String filename = f.getName();
                        boolean isAlreadyInList = NO;

                        // check if filename is already in list
                        // maybe detected twice - different mounting points
                        // filename can only exits once because it's name contains the date
                        // e.g. filename = 22_03_2017_19_01_30_dokoSavedGame.xml
                        for (File fileFromList : fList) {
                            String fileFromListName = fileFromList.getName();
                            if (fileFromListName.equalsIgnoreCase(filename)) {
                                isAlreadyInList = YES;
                                break;
                            }
                        }


                        if (!isAlreadyInList) {
                            fList.add(f);
                        }
                    }
                }
            }
        }
    }

    private void setUI() {
        int tagCnt = 0;
        mEntriesLayout = (LinearLayout)findViewById(R.id.saved_games_entries);
        mEntriesLayout.removeAllViewsInLayout();

        FileClickListener mFileClickListener = new FileClickListener(fileList);
        FileDeleteClickListener mFileDeleteClickListener = new FileDeleteClickListener(fileList);

        if (fileList != null){
            View v;
            TextView mTv;
            ImageView mIv;
            for (File savedGameFile : fileList){
                String absolutePath = savedGameFile.getAbsolutePath();


                if (!absolutePath.endsWith(DokoXMLClass.SAVED_GAME_FILE_SUFFIX)) {
                    tagCnt++;
                    continue;
                }
                v = mInflater.inflate(R.layout.saved_game_entry, null);

                LinearLayout l = (LinearLayout)v.findViewById(R.id.saved_game_entry_layout_start_game);
                l.setOnClickListener(mFileClickListener);
                l.setTag(SAVED_GAME_TAG + tagCnt);

                String filename = savedGameFile != null ? savedGameFile.getName() : absolutePath;

                String[] arr = filename.split("_");

                mTv = (TextView)v.findViewById(R.id.saved_game_entry_filename);
                if (arr.length > 5) {
                    mTv.setText(arr[0] + "." + arr[1] + "." + arr[2] + " - " + arr[3] + ":" + arr[4] + ":" + arr[5]);
                } else {
                    mTv.setText(filename);
                }

                mTv = (TextView)v.findViewById(R.id.saved_game_entry_path_filepath);
                mTv.setText(absolutePath);

                GameClass mGame =  DokoXMLClass.restoreGameStateFromXML(this,absolutePath, false);
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
                    mIv.setOnClickListener(mFileDeleteClickListener);
                    mIv.setColorFilter(mContext.getResources().getColor(R.color.red_dark), PorterDuff.Mode.SRC_ATOP);

                    mIv = (ImageView)l.findViewById(R.id.saved_game_entry_icon_mail);
                    mIv.setOnClickListener(new FileMailClickListener(absolutePath));
                    mIv.setColorFilter(mContext.getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);

                    mIv = (ImageView)l.findViewById(R.id.saved_game_entry_icon_upload);
                    mIv.setOnClickListener(new GameUploadClickListener(absolutePath));
                    mIv.setColorFilter(mContext.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                }


                Log.d(TAG,"set tagcnt:"+tagCnt);
                mEntriesLayout.addView(v);
                tagCnt++;
            }
        }
    }

    private class FileDeleteClickListener implements OnClickListener {

        final ArrayList<File> fList;
        private FileDeleteClickListener(ArrayList<File> list) {
            this.fList = list;
        }

        @Override
        public void onClick(View v) {
            int tagCnt = (Integer)v.getTag();
            tagCnt -= SAVED_GAME_TAG_DELETE;
            if (fList.size() > tagCnt) {
                showDeleteSavedGamesDialog(fList.get(tagCnt).getAbsolutePath());
            }
        }
    }

    private class GameUploadClickListener implements OnClickListener {
        final String savedGameFile;

        private GameUploadClickListener(String fileName) {
            savedGameFile = fileName;
        }

        @Override
        public void onClick(View v) {
            ProgressDialog dialog = new ProgressDialog(v.getContext());
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage(getResources().getString(R.string.str_upload_in_progress));
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);

            Response.Listener<String> listener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    dialog.cancel();
                    Toast toast = Toast.makeText(v.getContext(), R.string.str_upload_successful, Toast.LENGTH_SHORT);
                    View toastView = toast.getView();
                    toastView.getBackground().setColorFilter(getResources().getColor(R.color.black, v.getContext().getTheme()), PorterDuff.Mode.SRC_IN);
                    TextView toastText = (TextView)toastView.findViewById(android.R.id.message);
                    toastText.setTextColor(getResources().getColor(R.color.white, v.getContext().getTheme()));
                    toast.show();
                    v.setVisibility(View.GONE);
                }
            };
            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v("test", "Upload unsuccessful");
                    dialog.cancel();
                    String errorMessage = getResources().getString(R.string.str_upload_error);
                    NetworkResponse response = error.networkResponse;
                    if (response != null) {
                        int statusCode = response.statusCode;
                        if (statusCode == 401) {
                            errorMessage += getResources().getString(R.string.str_upload_error_not_authenticated);
                        } else if (statusCode == 409) {
                            errorMessage += getResources().getString(R.string.str_upload_error_already_uploaded);
                            v.setVisibility(View.GONE);
                        } else {
                            errorMessage += getResources().getString(R.string.str_upload_error_code) + " " + statusCode;
                        }
                    }

                    Toast toast = Toast.makeText(v.getContext(), errorMessage, Toast.LENGTH_SHORT);
                    View toastView = toast.getView();
                    toastView.getBackground().setColorFilter(getResources().getColor(R.color.black, v.getContext().getTheme()), PorterDuff.Mode.SRC_IN);
                    TextView toastText = (TextView)toastView.findViewById(android.R.id.message);
                    toastText.setTextColor(getResources().getColor(R.color.white, v.getContext().getTheme()));
                    toast.show();
                }
            };

            dialog.show();
            Uploader.uploadGame(v.getContext(), getGame(v), listener, errorListener);
        }

        private GameClass getGame(View v) {
            return DokoXMLClass.restoreGameStateFromXML(v.getContext(), this.savedGameFile, true);
        }
    }

    private class FileClickListener implements OnClickListener{

        final ArrayList<File> fList;
        private FileClickListener(ArrayList<File> list) {
            this.fList = list;
        }

        @Override
        public void onClick(View v) {
            int tagCnt = (Integer)v.getTag();
            tagCnt -= SAVED_GAME_TAG;
            Log.d(TAG,"get tag cnt:"+tagCnt);
            if (fList.size() > tagCnt) {

                String filename = fList.get(tagCnt).getAbsolutePath();
                if (filename.length() == 0) {
                    return;
                }

                Intent i = new Intent(mContext, GameActivity.class);
                i.putExtra("RestoreGameFromXML", true);
                i.putExtra("filename", filename);
                startActivity(i);
                finish();
            }
        }
    }

    private class FileMailClickListener implements OnClickListener {
        final String savedGameFile;
        private FileMailClickListener(String file) {
            this.savedGameFile = file;
        }

        @Override
        public void onClick(View v) {
            GameClass mGame =  DokoXMLClass.restoreGameStateFromXML(v.getContext(), this.savedGameFile, true);
            mailAttachments = DokoXMLClass.sendGameViaMail(v.getContext(), mGame);
        }
    }


    private void showDeleteAllSavedGamesDialog(){
        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                for(File f : fileList) {
                    if (f.exists() && !f.isDirectory() && f.getAbsolutePath().endsWith(DokoXMLClass.SAVED_GAME_FILE_SUFFIX)) {
                        f.delete();
                    }
                }
                reload();
            }
        };

        DialogInterface.OnClickListener abortListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        };

        showAlertDialog(R.string.str_delete_files, R.string.str_saved_game_delete_all_q,
                R.string.str_yes, okListener,
                R.string.str_no, abortListener);
    }

    private void showDeleteSavedGamesDialog(String file){
        final String filepath = file;


        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                File f = new File(filepath);
                if (f.exists() && !f.isDirectory() && f.getAbsolutePath().endsWith(DokoXMLClass.SAVED_GAME_FILE_SUFFIX)) {
                    f.delete();
                }
                reload();
            }
        };

        DialogInterface.OnClickListener abortListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        };

        showAlertDialog(R.string.str_delete_file, R.string.str_saved_game_delete_q,
                R.string.str_yes, okListener,
                R.string.str_no, abortListener);
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
