package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import barqsoft.footballscores.sync.FootballScoresSyncAdapter;

public class MainActivity extends ActionBarActivity
{
    public static int selectedMatchId;
    public static int currentFragment = 2;

    private static final String CURRENT_FRAGMENT = "CURRENT_FRAGMENT";
    private static final String SELECTED_MATCH = "SELECTED_MATCH";
    private static final String PAGER_FRAGMENT = "PAGER_FRAGMENT";

    private PagerFragment mainPagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            mainPagerFragment = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mainPagerFragment)
                    .commit();
        }

        FootballScoresSyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            Intent startAbout = new Intent(this,AboutActivity.class);
            startActivity(startAbout);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(CURRENT_FRAGMENT, mainPagerFragment.mPagerHandler.getCurrentItem());
        outState.putInt(SELECTED_MATCH, selectedMatchId);
        getSupportFragmentManager().putFragment(outState, PAGER_FRAGMENT, mainPagerFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        currentFragment = savedInstanceState.getInt(CURRENT_FRAGMENT);
        selectedMatchId = savedInstanceState.getInt(SELECTED_MATCH);
        mainPagerFragment = (PagerFragment) getSupportFragmentManager()
                .getFragment(savedInstanceState, PAGER_FRAGMENT);
        super.onRestoreInstanceState(savedInstanceState);
    }
}
