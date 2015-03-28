package nldoko.game.information;

import nldoko.game.R;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class AboutActivity extends ActionBarActivity {
    private static Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);


        mToolbar = (Toolbar)findViewById(R.id.toolbar_id);
        setSupportActionBar(mToolbar);
        mToolbar.bringToFront();
        mToolbar.setTitle(getResources().getString(R.string.action_about));

    	overridePendingTransition(R.anim.right_out, R.anim.left_in);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    
}
