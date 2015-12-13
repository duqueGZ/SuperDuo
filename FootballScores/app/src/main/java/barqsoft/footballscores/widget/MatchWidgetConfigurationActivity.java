package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.Arrays;

import barqsoft.footballscores.R;
import barqsoft.footballscores.adapters.FootballScoresSpinnerAdapter;
import barqsoft.footballscores.adapters.SpinnerItem;
import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.sync.FootballScoresSyncAdapter;
import barqsoft.footballscores.util.Utility;
import butterknife.Bind;
import butterknife.ButterKnife;

/*
 * Created by David Duque. 12/02/2015.
 */
public class MatchWidgetConfigurationActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String MATCH_WIDGET_SHARED_PREFS = "MatchWidgetSharedPrefs";
    public static final String WIDGET_PREF_FAVOURITE_TEAM = "FAVOURITE_TEAM";
    public static final String WIDGET_PREF_TIME_RANGE = "TIME_RANGE";

    private static final int MATCH_LEAGUE_LOADER_ID = 1;
    private static final int MATCH_TEAM_LOADER_ID = 2;
    private static final int INVALID_LEAGUE_SELECTED = -1;

    private static final String[] LEAGUE_COLUMNS = {
            DatabaseContract.LeaguesEntry._ID,
            DatabaseContract.LeaguesEntry.LEAGUE_ID_COL,
            DatabaseContract.LeaguesEntry.NAME_COL
    };
    // These indices are tied to LEAGUE_COLUMNS. If LEAGUE_COLUMNS changes, these must change too.
    private static final int COL_LEAGUE_PK_ID = 0;
    private static final int COL_LEAGUE_ID = 1;
    private static final int COL_LEAGUE_NAME = 2;

    private static final String[] TEAM_COLUMNS = {
            DatabaseContract.TeamsEntry._ID,
            DatabaseContract.TeamsEntry.TEAM_ID_COL,
            DatabaseContract.TeamsEntry.NAME_COL
    };
    // These indices are tied to TEAM_COLUMNS. If TEAM_COLUMNS changes, these must change too.
    private static final int COL_TEAM_PK_ID = 0;
    private static final int COL_TEAM_ID = 1;
    private static final int COL_TEAM_NAME = 2;

    private static final String LOG_TAG = MatchWidgetConfigurationActivity.class.getSimpleName();

    int mAppWidgetId;
    int mSelectedLeagueId;
    @Bind(R.id.league_spinner)
    Spinner mLeagueSpinner;
    @Bind(R.id.favourite_team_spinner)
    Spinner mFavouriteTeamSpinner;
    @Bind(R.id.match_time)
    Spinner mTimeRangeSpinner;
    @Bind(R.id.ok_button)
    Button mOkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSelectedLeagueId = INVALID_LEAGUE_SELECTED;

        getSupportLoaderManager().initLoader(MATCH_LEAGUE_LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(MATCH_TEAM_LOADER_ID, null, this);

        setContentView(R.layout.activity_match_widget_configuration);
        setResult(RESULT_CANCELED);

        ButterKnife.bind(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.widget_match_time_options, R.layout.spinner_item);
        mTimeRangeSpinner.setAdapter(adapter);

        mLeagueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                                       long id) {
                mSelectedLeagueId = ((SpinnerItem) adapterView.getItemAtPosition(position)).getId();
                getSupportLoaderManager().restartLoader(MATCH_TEAM_LOADER_ID, null,
                        MatchWidgetConfigurationActivity.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSelectedLeagueId = INVALID_LEAGUE_SELECTED;
                getSupportLoaderManager().restartLoader(MATCH_TEAM_LOADER_ID, null,
                        MatchWidgetConfigurationActivity.this);
            }
        });

        mOkButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showAppWidget();
            }
        });

        FootballScoresSyncAdapter.initializeSyncAdapter(this);
        if (savedInstanceState==null) {
            //Only call SyncAdapter the first time
            Utility.updateMatchesInfo(this,
                    new int[]{MATCH_LEAGUE_LOADER_ID, MATCH_TEAM_LOADER_ID}, this,
                    Arrays.asList(FootballScoresSyncAdapter.LEAGUES,
                            FootballScoresSyncAdapter.TEAMS));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ButterKnife.unbind(this);
    }

    private void showAppWidget() {
        mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            SharedPreferences prefs = getSharedPreferences(MATCH_WIDGET_SHARED_PREFS + "_"
                    + mAppWidgetId, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(WIDGET_PREF_FAVOURITE_TEAM, mFavouriteTeamSpinner.getSelectedItemId());
            String timeRange = Utility.timeRangeOptionToParameterValue(getApplicationContext(),
                    mTimeRangeSpinner.getSelectedItem().toString());
            if (timeRange!=null) {
                editor.putString(WIDGET_PREF_TIME_RANGE, timeRange);
            }
            editor.commit();

            getApplicationContext().startService(new Intent(getApplicationContext(),
                    MatchWidgetIntentService.class));

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.d(LOG_TAG, "showAppWidget. Invalid widget id");
            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MATCH_LEAGUE_LOADER_ID: {
                return new CursorLoader(this,
                        DatabaseContract.LeaguesEntry.CONTENT_URI,
                        LEAGUE_COLUMNS, null, null, null);
            }
            case MATCH_TEAM_LOADER_ID: {
                return new CursorLoader(this,
                        DatabaseContract.TeamsEntry
                                .buildTeamsUriByLeagueId(mSelectedLeagueId), TEAM_COLUMNS,
                        null, null, null);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        long loaderId = loader.getId();
        if (loaderId == MATCH_LEAGUE_LOADER_ID) {
            onLeaguesLoadFinished(data);
        } else if (loaderId == MATCH_TEAM_LOADER_ID) {
            onTeamsLoadFinished(data);
        }
    }

    private void onLeaguesLoadFinished(Cursor data) {

        if (data.getCount()==0) {
            mLeagueSpinner.setAdapter(null);
        } else {
            data.moveToFirst();
            SpinnerItem[] items = new SpinnerItem[data.getCount()];
            int i = 0;
            do  {
                items[i] = new SpinnerItem(data.getInt(COL_LEAGUE_ID),
                        data.getString(COL_LEAGUE_NAME));
                i++;
            } while (data.moveToNext());
            FootballScoresSpinnerAdapter adapter = new FootballScoresSpinnerAdapter(this,
                    android.R.layout.simple_spinner_item, items);
            mLeagueSpinner.setAdapter(adapter);
        }
    }

    private void onTeamsLoadFinished(Cursor data) {

        if (data.getCount()==0) {
            mFavouriteTeamSpinner.setAdapter(null);
        } else {
            data.moveToFirst();
            SpinnerItem[] items = new SpinnerItem[data.getCount()];
            int i = 0;
            do  {
                items[i] = new SpinnerItem(data.getInt(COL_TEAM_ID), data.getString(COL_TEAM_NAME));
                i++;
            } while (data.moveToNext());
            FootballScoresSpinnerAdapter adapter = new FootballScoresSpinnerAdapter(this,
                    android.R.layout.simple_spinner_item, items);
            mFavouriteTeamSpinner.setAdapter(adapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Do nothing
    }
}
