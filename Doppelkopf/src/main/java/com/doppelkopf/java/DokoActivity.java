package com.doppelkopf.java;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.appcompat.BuildConfig;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;

import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import com.doppelkopf.java.game.NewGameActivity;
import com.doppelkopf.java.game.SavedGameListActivity;


public class DokoActivity extends AppCompatActivity {

    public final static boolean YES = true;
    public final static boolean NO = false;

    protected Drawer drawer;
    protected Context mContext;
    protected LayoutInflater mInflater;
    protected Toolbar mToolbar;

    protected SecondaryDrawerItem drawerAbout;
    protected SecondaryDrawerItem drawerSavedGames;
    protected SecondaryDrawerItem drawerStart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (isDebug()) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override

                public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                    Log.e("EX", paramThrowable.getStackTrace().toString());
                    paramThrowable.printStackTrace();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
        }

        mContext = this;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Boolean isDebug() {
        if (BuildConfig.DEBUG) {
            return YES;
        }

        return NO;
    }

    public void setupDrawerAndToolbar(String toolbarTitle) {
        try {
            mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle(toolbarTitle);

            drawerStart = new SecondaryDrawerItem().withIdentifier(0).withName(R.string.str_start);
            drawerSavedGames = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.str_saved_game);
            drawerAbout = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.start_action_about);

            drawerStart.withTextColor(getColor(R.color.black));
            drawerSavedGames.withTextColor(getColor(R.color.black));
            drawerAbout.withTextColor(getColor(R.color.gray_dark));

            AccountHeader header = new AccountHeaderBuilder()
                    .withActivity(this)
                    .withHeaderBackground(R.drawable.doppelkopf_splash)
                    .build();
            header.removeProfile(0);
            header.setSelectionFirstLineShown(NO);
            header.getHeaderBackgroundView().setScaleType(ImageView.ScaleType.CENTER_CROP);

            drawer = new DrawerBuilder()
                    .withActivity(this)
                    .withTranslucentStatusBar(true)
                    .withActionBarDrawerToggle(YES)
                    .withAccountHeader(header)
                    .withToolbar(mToolbar)
                    .withCloseOnClick(YES)
                    .addDrawerItems(
                            drawerStart,
                            drawerSavedGames,
                             new DividerDrawerItem(),
                            drawerAbout
                    )
                    .withStickyFooterDivider(YES)
                    .withStickyFooter(R.layout.drawer_footer)
                    .withStickyFooterShadow(NO)
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            if (drawerItem == drawerAbout) {
                                showAbout();
                                return YES;
                            }

                            if (drawerItem == drawerSavedGames ){
                                showSavedGames();
                                return YES;
                            }

                            if (drawerItem == drawerStart) {
                                showStart();
                                return YES;
                            }

                            return NO;
                        }
                    })
                    .build();

            drawer.setSelectionAtPosition(71);

            setHamburgerIconActionBar();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBackArrowInToolbar() {
        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }


    protected void  showBackArrorActionBar () {
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected void setHamburgerIconActionBar () {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
    }

    protected void showAlertDialog(int titleID, int msgID) {
        showAlertDialog(getResources().getString(titleID), getResources().getString(msgID));
    }

    protected void showAlertDialog(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.str_accept, null);
        AlertDialog dialog = builder.show();

        // Must call show() prior to fetching text view
        TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.LEFT);
    }


    private void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.start_action_about);
        builder.setMessage(R.string.str_disclaimer);
        builder.setPositiveButton(R.string.str_accept, null);
        AlertDialog dialog = builder.show();

        // Must call show() prior to fetching text view
        TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.LEFT);
    }

    private void showSavedGames () {
        Intent i = new Intent(mContext,SavedGameListActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.right_out, R.anim.left_in);
    }

    private void showStart () {
        Intent i = new Intent(mContext,NewGameActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.right_out, R.anim.left_in);
    }
}
