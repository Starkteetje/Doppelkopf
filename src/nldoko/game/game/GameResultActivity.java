package nldoko.game.game;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import nldoko.game.R;
import nldoko.game.data.DokoData;

public class GameResultActivity extends Activity {
    private Context mContext;

    private String TAG = "GameResult";
    private ActionBar mActionBar;
    private ArrayList<View> mPlayerViewList = new ArrayList<View>();
    private LayoutInflater inflater;

    ArrayList<String> playerNames;
    ArrayList<Float> playerPoints;

    Button mBtnCalc;
    EditText mFactorField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_game_result);
        mContext = this;
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mBtnCalc = (Button)findViewById(R.id.btn_game_result_calc);
        mBtnCalc.setOnClickListener(new calcBtnClickListener());

        mFactorField = (EditText)findViewById(R.id.field_game_result_calc_factor);

        mActionBar = getActionBar();
        mActionBar.show();
        mActionBar.setTitle(getResources().getString(R.string.str_game_result_calc));
        mActionBar.setDisplayHomeAsUpEnabled(true);

        playerNames = new ArrayList<String>();
        playerPoints = new ArrayList<Float>();

        loadPlayerStates();

        setUI(1.0f);

        overridePendingTransition(R.anim.right_out, R.anim.left_in);
    }


    private void loadPlayerStates() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int mPlayerCnt;
        String mTmp = "";
        Float mPTmp= 0.0f;
        boolean error = false;

        if(extras != null){
            mPlayerCnt = extras.getInt(DokoData.PLAYER_CNT_KEY,0);

            if(mPlayerCnt < DokoData.MIN_PLAYER || mPlayerCnt > DokoData.MAX_PLAYER || mPlayerCnt == 0) {
                return;
            }

            for(int k=0;k<mPlayerCnt;k++){
                Log.d(TAG,mTmp+"k:"+k);
                mTmp = extras.getString(DokoData.PLAYERS_KEY[k],"");
                mPTmp = extras.getFloat(DokoData.PLAYERS_POINTS_KEY[k], 0.0f);
                if(mTmp == null || mTmp.length() == 0) {
                    error = true;
                    break;
                }

                playerNames.add(mTmp);
                playerPoints.add(mPTmp);
            }
        }

        if (error) {
            playerPoints.clear();
            playerNames.clear();
        }
    }


    private void setUI(Float factor) {
        LinearLayout container = (LinearLayout)findViewById(R.id.game_result_view_container);

        if (container == null) {
            return;
        }

        container.removeAllViewsInLayout();

        for (int i = 0; i < playerNames.size() && i < playerPoints.size(); i++) {
            String name = playerNames.get(i);
            Float points = playerPoints.get(i);

            LinearLayout playerEntry = (LinearLayout)getLayoutInflater().inflate(R.layout.player_entry_game_result, container, false);
            if (playerEntry != null) {
                TextView mTv = (TextView)playerEntry.findViewById(R.id.player_entry_game_result_name);
                mTv.setText(name);

                mTv = (TextView)playerEntry.findViewById(R.id.player_entry_game_result_name_points);
                mTv.setText(Float.toString(points));
                if (points < 0) {
                    mTv.setTextColor(this.getResources().getColor(R.color.red_dark));
                } else {
                    mTv.setTextColor(this.getResources().getColor(R.color.green_dark));
                }

                mTv = (TextView)playerEntry.findViewById(R.id.player_entry_game_result_factor);
                mTv.setText(Float.toString(factor));

                mTv = (TextView)playerEntry.findViewById(R.id.player_entry_game_result_final_result);
                mTv.setText(String.format("%.2f",factor * points));

                if (points < 0) {
                    mTv.setTextColor(this.getResources().getColor(R.color.red_dark));
                } else {
                    mTv.setTextColor(this.getResources().getColor(R.color.green_dark));
                }

                playerEntry.setPadding(0,0,0,40);
                container.addView(playerEntry);
            }
        }


    }


    private class calcBtnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String newFactor = mFactorField.getText().toString();
            newFactor = newFactor.replace(",", ".");
            setUI(Float.valueOf(newFactor));
        }
    }


    @Override
    public void onBackPressed(){
        finish();
        return;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // same as using a normal menu
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return true;
    }

}
