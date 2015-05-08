package nldoko.game.base;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
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

    public LayoutInflater mInflater;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base_layout);
        initToolbarDrawer();
        mContext = this;
        mBaseLayout = (LinearLayout)this.findViewById(R.id.base_layout_content);
        mBaseLayout.setBackgroundColor(this.getResources().getColor(R.color.activity_background_color));

        mInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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


    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value thiso represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }
}
