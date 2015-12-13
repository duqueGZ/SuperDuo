package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.R;
import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.data.ScoresProvider;
import barqsoft.footballscores.util.Utility;

/**
 * Created by David Duque. 12/02/2015.
 * RemoteViewsService controlling the data being shown in the scrollable weather detail
 * barqsoft.footballscores.widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MatchListWidgetRemoteViewsService extends RemoteViewsService {

    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.ScoresEntry.TABLE_NAME + "."
                    + DatabaseContract.ScoresEntry._ID,
            DatabaseContract.ScoresEntry.TABLE_NAME + "."
                    + DatabaseContract.ScoresEntry.MATCH_ID_COL,
            DatabaseContract.ScoresEntry.TABLE_NAME + "."
                    + DatabaseContract.ScoresEntry.HOME_GOALS_COL,
            DatabaseContract.ScoresEntry.TABLE_NAME + "."
                    + DatabaseContract.ScoresEntry.AWAY_GOALS_COL,
            DatabaseContract.ScoresEntry.TABLE_NAME + "."
                    + DatabaseContract.ScoresEntry.DATE_COL,
            DatabaseContract.ScoresEntry.TABLE_NAME + "."
                    + DatabaseContract.ScoresEntry.TIME_COL,
            ScoresProvider.HOME_TEAM_TABLE_ALIAS + "." + DatabaseContract.TeamsEntry.NAME_COL,
            ScoresProvider.AWAY_TEAM_TABLE_ALIAS + "." + DatabaseContract.TeamsEntry.NAME_COL,
    };
    // These indices are tied to SCORE_COLUMNS. If SCORE_COLUMNS changes, these must change too.
    private static final int INDEX_ID = 0;
    private static final int INDEX_MATCH_ID = 1;
    private static final int INDEX_HOME_GOALS = 2;
    private static final int INDEX_AWAY_GOALS = 3;
    private static final int INDEX_DATE = 4;
    private static final int INDEX_TIME = 5;
    private static final int INDEX_HOME_NAME = 6;
    private static final int INDEX_AWAY_NAME = 7;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new MatchListWidgetFactory(intent);
    }

    private class MatchListWidgetFactory implements RemoteViewsFactory {

        private static final long INVALID_LEAGUE_ID = -1;

        private Cursor data = null;
        private int appWidgetId;

        public MatchListWidgetFactory(Intent intent) {
            this.appWidgetId = Integer.valueOf(intent.getData().getSchemeSpecificPart());
        }

        @Override
        public void onCreate() {
            // Nothing to do
        }

        @Override
        public void onDataSetChanged() {
            if (data != null) {
                data.close();
            }
            // This method is called by the app hosting the widget (e.g., the launcher)
            // However, our ContentProvider is not exported so it doesn't have access to the
            // data. Therefore we need to clear (and finally restore) the calling identity so
            // that calls use our process and permission
            final long identityToken = Binder.clearCallingIdentity();

            SharedPreferences prefs =
                    getSharedPreferences(
                            MatchListWidgetConfigurationActivity.MATCH_LIST_WIDGET_SHARED_PREFS
                            + "_" + appWidgetId, Context.MODE_PRIVATE);
            long leagueId = prefs.getLong(MatchListWidgetConfigurationActivity.WIDGET_PREF_LEAGUE,
                    INVALID_LEAGUE_ID);
            //By default, using "next week" time range option
            String timeRange = prefs
                    .getString(MatchListWidgetConfigurationActivity.WIDGET_PREF_TIME_RANGE,
                            Utility.NEXT_WEEK_CODE);
            data = null;
            if (leagueId!=INVALID_LEAGUE_ID) {
                Uri matchesForLeagueUri = DatabaseContract.ScoresEntry
                        .buildScoreWithLeagueAndTimeRange(leagueId, timeRange);
                data = getContentResolver().query(matchesForLeagueUri,
                        SCORE_COLUMNS,
                        null,
                        null,
                        DatabaseContract.ScoresEntry.DATE_COL + " ASC");
            }
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            if (data != null) {
                data.close();
                data = null;
            }
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION ||
                    data == null || !data.moveToPosition(position)) {
                return null;
            }

            RemoteViews views = new RemoteViews(getPackageName(),
                    R.layout.widget_match_list_item);

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
            String awayName = data.getString(INDEX_AWAY_NAME);

            views.setImageViewResource(R.id.widget_home_crest,
                    Utility.getTeamCrestByTeamName(homeName));
            views.setTextViewText(R.id.widget_home_name, homeName);

            views.setTextViewText(R.id.widget_scores, result);
            views.setTextViewText(R.id.widget_date, matchDate);

            views.setImageViewResource(R.id.widget_away_crest,
                    Utility.getTeamCrestByTeamName(awayName));
            views.setTextViewText(R.id.widget_away_name, awayName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, R.id.widget_list_item,
                        Utility.getMatchItemContentDescription(data.getString(INDEX_HOME_GOALS),
                                data.getString(INDEX_AWAY_GOALS),
                                homeName, awayName, matchDate, null, null));
            }

            return views;
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        private void setRemoteContentDescription(RemoteViews views, int viewId,
                                                 String description) {
            views.setContentDescription(viewId, description);
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.widget_match_list_item);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if (data.moveToPosition(position))
                return data.getLong(INDEX_MATCH_ID);
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}