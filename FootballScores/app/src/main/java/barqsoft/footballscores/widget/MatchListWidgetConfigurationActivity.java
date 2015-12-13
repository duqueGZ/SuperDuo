package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Spinner;

import java.util.Arrays;

import barqsoft.footballscores.MainActivity;
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
public class MatchListWidgetConfigurationActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String MATCH_LIST_WIDGET_SHARED_PREFS = "MatchListWidgetSharedPrefs";
    public static final String WIDGET_PREF_LEAGUE = "LEAGUE";
    public static final String WIDGET_PREF_TIME_RANGE = "TIME_RANGE";

    private static final int MATCH_LIST_LEAGUE_LOADER_ID = 3;

    private static final String[] LEAGUE_COLUMNS = {
            DatabaseContract.LeaguesEntry._ID,
            DatabaseContract.LeaguesEntry.LEAGUE_ID_COL,
            DatabaseContract.LeaguesEntry.NAME_COL
    };
    // These indices are tied to LEAGUE_COLUMNS. If LEAGUE_COLUMNS changes, these must change too.
    private static final int COL_ID = 0;
    private static final int COL_LEAGUE_ID = 1;
    private static final int COL_NAME = 2;

    private static final String LOG_TAG =
            MatchListWidgetConfigurationActivity.class.getSimpleName();

    int mAppWidgetId;
    @Bind(R.id.match_list_league_spinner)
    Spinner mLeagueSpinner;
    @Bind(R.id.match_list_time)
    Spinner mTimeRangeSpinner;
    @Bind(R.id.match_list_ok_button)
    Button mOkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportLoaderManager().initLoader(MATCH_LIST_LEAGUE_LOADER_ID, null, this);

        setContentView(R.layout.activity_match_list_widget_configuration);
        setResult(RESULT_CANCELED);

        ButterKnife.bind(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.widget_list_time_options, R.layout.spinner_item);
        mTimeRangeSpinner.setAdapter(adapter);

        mOkButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showAppWidget();
            }
        });

        FootballScoresSyncAdapter.initializeSyncAdapter(this);
        if (savedInstanceState==null) {
            //Only call SyncAdapter the first time
            Utility.updateMatchesInfo(this, new int[]{MATCH_LIST_LEAGUE_LOADER_ID}, this,
                    Arrays.asList(FootballScoresSyncAdapter.LEAGUES));
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
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

            SharedPreferences prefs = getSharedPreferences(MATCH_LIST_WIDGET_SHARED_PREFS + "_"
                    + mAppWidgetId, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(WIDGET_PREF_LEAGUE, mLeagueSpinner.getSelectedItemId());
            String timeRange = Utility.timeRangeOptionToParameterValue(getApplicationContext(),
                    mTimeRangeSpinner.getSelectedItem().toString());
            if (timeRange!=null) {
                editor.putString(WIDGET_PREF_TIME_RANGE, timeRange);
            }
            editor.commit();

            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget_match_list);
            // Create an Intent to launch MainActivity
            Intent intentMain = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentMain, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);
            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(this, views, mAppWidgetId);
            } else {
                setRemoteAdapterV11(this, views, mAppWidgetId);
            }

            views.setEmptyView(R.id.widget_list, R.id.widget_empty);

            views.setTextViewText(R.id.widget_title,
                    mTimeRangeSpinner.getSelectedItem().toString());

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(mAppWidgetId, views);

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
            case MATCH_LIST_LEAGUE_LOADER_ID: {
                return new CursorLoader(this, DatabaseContract.LeaguesEntry.CONTENT_URI,
                        LEAGUE_COLUMNS, null, null, null);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        long loaderId = loader.getId();
        if (loaderId == MATCH_LIST_LEAGUE_LOADER_ID) {
            onLeaguesLoadFinished(data);
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
                items[i] = new SpinnerItem(data.getInt(COL_LEAGUE_ID), data.getString(COL_NAME));
                i++;
            } while (data.moveToNext());
            FootballScoresSpinnerAdapter adapter = new FootballScoresSpinnerAdapter(this,
                    android.R.layout.simple_spinner_item, items);
            mLeagueSpinner.setAdapter(adapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Do nothing
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views,
                                  int appWidgetId) {
        Intent intent = new Intent(context, MatchListWidgetRemoteViewsService.class);
        intent.setData(Uri.fromParts("content", String.valueOf(appWidgetId), null));
        views.setRemoteAdapter(R.id.widget_list, intent);
    }


    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views,
                                     int appWidgetId) {
        Intent intent = new Intent(context, MatchListWidgetRemoteViewsService.class);
        intent.setData(Uri.fromParts("content", String.valueOf(appWidgetId), null));
        views.setRemoteAdapter(0, R.id.widget_list, intent);
    }
}
