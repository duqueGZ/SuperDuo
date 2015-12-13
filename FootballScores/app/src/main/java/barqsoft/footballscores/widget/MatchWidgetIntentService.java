/* 
 * Copyright (C) 2015 The Android Open Source Project 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.data.ScoresProvider;
import barqsoft.footballscores.util.Utility;

/**
 * Created by David Duque. 12/02/2015.
 * IntentService which handles updating all Today widgets with the latest data 
 */
public class MatchWidgetIntentService extends IntentService {

    private final String LOG_TAG = MatchWidgetIntentService.class.getSimpleName();

    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.ScoresEntry.TABLE_NAME + "."
                    + DatabaseContract.ScoresEntry._ID,
            DatabaseContract.ScoresEntry.TABLE_NAME + "."
                    + DatabaseContract.ScoresEntry.HOME_GOALS_COL,
            DatabaseContract.ScoresEntry.TABLE_NAME + "."
                    + DatabaseContract.ScoresEntry.AWAY_GOALS_COL,
            DatabaseContract.ScoresEntry.TABLE_NAME + "."
                    + DatabaseContract.ScoresEntry.DATE_COL,
            DatabaseContract.ScoresEntry.TABLE_NAME + "."
                    + DatabaseContract.ScoresEntry.TIME_COL,
            ScoresProvider.HOME_TEAM_TABLE_ALIAS + "." + DatabaseContract.TeamsEntry.NAME_COL,
            ScoresProvider.HOME_TEAM_TABLE_ALIAS + "." + DatabaseContract.TeamsEntry.CODE_COL,
            ScoresProvider.AWAY_TEAM_TABLE_ALIAS + "." + DatabaseContract.TeamsEntry.NAME_COL,
            ScoresProvider.AWAY_TEAM_TABLE_ALIAS + "." + DatabaseContract.TeamsEntry.CODE_COL,
    };
    // These indices are tied to SCORE_COLUMNS. If SCORE_COLUMNS changes, these must change too.
    private static final int INDEX_ID = 0;
    private static final int INDEX_HOME_GOALS = 1;
    private static final int INDEX_AWAY_GOALS = 2;
    private static final int INDEX_DATE = 3;
    private static final int INDEX_TIME = 4;
    private static final int INDEX_HOME_NAME = 5;
    private static final int INDEX_HOME_CODE = 6;
    private static final int INDEX_AWAY_NAME = 7;
    private static final int INDEX_AWAY_CODE = 8;

    private static final long INVALID_TEAM_ID = -1;
    private static final int MATCHES_TO_RETRIEVE = 1;

    public MatchWidgetIntentService() {
        super("MatchWidgetIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Match barqsoft.footballscores.widget ids: these are the widgets we
        // need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                MatchWidgetProvider.class));

        // Perform this loop procedure for each Today barqsoft.footballscores.widget
        for (int appWidgetId : appWidgetIds) {
            // Find the correct layout based on the barqsoft.footballscores.widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int defaultWidth = getResources()
                    .getDimensionPixelSize(R.dimen.widget_match_default_width);
            int layoutId;
            if (widgetWidth >= defaultWidth) {
                layoutId = R.layout.widget_match;
            } else {
                layoutId = R.layout.widget_match_small;
            }
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            SharedPreferences prefs =
                    getSharedPreferences(MatchWidgetConfigurationActivity.MATCH_WIDGET_SHARED_PREFS
                            + "_" + appWidgetId, Context.MODE_PRIVATE);
            long teamId = prefs.getLong(MatchWidgetConfigurationActivity.WIDGET_PREF_FAVOURITE_TEAM,
                    INVALID_TEAM_ID);
            //By default, using "next week" time range option
            String timeRange = prefs
                    .getString(MatchListWidgetConfigurationActivity.WIDGET_PREF_TIME_RANGE,
                            Utility.NEXT_WEEK_CODE);
            String sortOrder;
            if (timeRange.equals(Utility.LAST_WEEK_CODE)) {
                sortOrder = "DESC"; // In order to get most recent past game
            } else {
                sortOrder = "ASC"; // In order to get most recent future game
            }
            // Get team match data from the ContentProvider
            Uri matchForTeamUri = DatabaseContract.ScoresEntry
                    .buildScoreWithTeamAndTimeRange(teamId, timeRange, MATCHES_TO_RETRIEVE);
            Cursor data = getContentResolver().query(matchForTeamUri, SCORE_COLUMNS, null,
                    null, DatabaseContract.ScoresEntry.DATE_COL + " " + sortOrder);
            if (data == null) {
                return;
            }

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);

            if (!data.moveToFirst()) {
                data.close();
                views.setViewVisibility(R.id.widget, View.INVISIBLE);
                views.setViewVisibility(R.id.widget_empty, View.VISIBLE);
                views.setOnClickPendingIntent(R.id.widget_empty, pendingIntent);

                // Tell the AppWidgetManager to perform an update on the current app
                // barqsoft.footballscores.widget
                appWidgetManager.updateAppWidget(appWidgetId, views);

                return;
            }

            views.setViewVisibility(R.id.widget, View.VISIBLE);
            views.setViewVisibility(R.id.widget_empty, View.INVISIBLE);

            int homeGoals = -1;
            String goals = data.getString(INDEX_HOME_GOALS);
            if ((goals!=null)&&(!goals.isEmpty())&&(!goals.equals("null"))) {
                homeGoals = Integer.valueOf(goals);
            }
            int awayGoals = -1;
            goals = data.getString(INDEX_AWAY_GOALS);
            if ((goals!=null)&&(!goals.isEmpty())&&(!goals.equals("null"))) {
                awayGoals = Integer.valueOf(goals);
            }
            String result = Utility.getScores(homeGoals, awayGoals);
            String date = data.getString(INDEX_DATE);
            String time = data.getString(INDEX_TIME);
            String matchDate = Utility.getDateI18nFormatted(getApplicationContext(), date)
                    + " " + time;
            String homeName = data.getString(INDEX_HOME_NAME);
            String homeCode = data.getString(INDEX_HOME_CODE);
            String awayName = data.getString(INDEX_AWAY_NAME);
            String awayCode = data.getString(INDEX_AWAY_CODE);

            views.setImageViewResource(R.id.widget_home_crest,
                    Utility.getTeamCrestByTeamName(homeName));
            views.setTextViewText(R.id.widget_home_name, homeName);
            views.setTextViewText(R.id.widget_home_code, homeCode);

            views.setTextViewText(R.id.widget_scores, result);
            views.setTextViewText(R.id.widget_date, matchDate);

            views.setImageViewResource(R.id.widget_away_crest,
                    Utility.getTeamCrestByTeamName(awayName));
            views.setTextViewText(R.id.widget_away_name, awayName);
            views.setTextViewText(R.id.widget_away_code, awayCode);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, R.id.widget,
                        Utility.getMatchItemContentDescription(data.getString(INDEX_HOME_GOALS),
                                data.getString(INDEX_AWAY_GOALS),
                                homeName, awayName, matchDate, null, null));
            }

            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app
            // barqsoft.footballscores.widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_match_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_match_default_width);
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, int viewId, String description) {
        views.setContentDescription(viewId, description);
    }
}