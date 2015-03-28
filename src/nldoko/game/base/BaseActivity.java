package nldoko.game.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Toast;

import nldoko.game.R;


public class BaseActivity extends ActionBarActivity {

    public Context mContext;
	public static String TAG = "BaseActivity";
    public static Toolbar mToolbar;
    public ActionBarDrawerToggle mDrawToggle;
    public ListView mLeftDrawerList;
    public DrawerLayout mDrawerLayout;
    public ArrayAdapter<String> navigationDrawerAdapter;
    public String[] leftSliderData = {"Home", "Android", "Sitemap", "About", "Contact Me"};
    LinearLayout mBaseLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base_layout);
        initToolbarDrawer();
        mContext = this;
        mBaseLayout = (LinearLayout)this.findViewById(R.id.base_layout_content);
    }

    private void initToolbarDrawer() {
        mToolbar = (Toolbar)findViewById(R.id.toolbar_id);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        initDrawer();
        mToolbar.bringToFront();
    }

    @Override
    public void setContentView(int id) {
        LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(id, this.mBaseLayout);
    }

    private void initDrawer() {
        mLeftDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.base_layout_drawer);
        navigationDrawerAdapter  = new ArrayAdapter<>(BaseActivity.this, android.R.layout.simple_list_item_1, leftSliderData);
        mLeftDrawerList.setAdapter(navigationDrawerAdapter);
        mDrawToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
        };
        mDrawerLayout.setDrawerListener(mDrawToggle);
        mLeftDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawToggle.syncState();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawToggle.syncState();
    }

    private  class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position,long id) {
            // TODO Auto-generated method stub
            Toast.makeText(mContext, position+"pos", Toast.LENGTH_LONG).show();
            drawerItemPressed(position);
            if (shouldCloseDrawerAfterItemSelected(position)) {
                closeDrawer();
            }
        }
    }


    protected void drawerItemPressed(int pos) {
        // subclass
    }

    protected boolean shouldCloseDrawerAfterItemSelected(int position) {
       return true;
    }

    protected void closeDrawer() {
        mDrawerLayout.closeDrawer(mLeftDrawerList);
    }
}
