package barqsoft.footballscores.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by yehya khaled on 2/25/2015.
 * Modified by David Duque. 12/02/2015.
 */
public class DatabaseContract
{
    //URI data
    public static final String CONTENT_AUTHORITY = "barqsoft.footballscores";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);
    public static final String PATH_SCORES = "scores";
    public static final String PATH_LEAGUES = "leagues";
    public static final String PATH_TEAMS = "teams";

    public static final class ScoresEntry implements BaseColumns
    {
        public static final String PATH_SCORES_BY_LEAGUE = "league";
        public static final String PATH_SCORES_BY_DATE = "date";
        public static final String PATH_SCORES_BY_TEAM = "team";

        private static final String LIMIT_PARAMETER = "limit";
        private static final String TIME_RANGE_PARAMETER = "time_range";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SCORES).build();
        public static final Uri SCORES_BY_LEAGUE_CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SCORES)
                        .appendPath(PATH_SCORES_BY_LEAGUE).build();
        public static final Uri SCORES_BY_TEAM_CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SCORES)
                        .appendPath(PATH_SCORES_BY_TEAM).build();
        public static final Uri SCORES_BY_DATE_CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SCORES)
                        .appendPath(PATH_SCORES_BY_DATE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_SCORES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_SCORES;

        public static final String TABLE_NAME = "score";
        //Table data
        public static final String LEAGUE_ID_COL = "league_id";
        public static final String DATE_COL = "date";
        public static final String TIME_COL = "time";
        public static final String HOME_ID_COL = "home_id";
        public static final String AWAY_ID_COL = "away_id";
        public static final String HOME_GOALS_COL = "home_goals";
        public static final String AWAY_GOALS_COL = "away_goals";
        public static final String MATCH_ID_COL = "match_id";
        public static final String MATCHDAY_COL = "matchday";

        public static Uri buildScoreWithLeagueAndTimeRange(long id, String timeRange)
        {
            return ContentUris.withAppendedId(SCORES_BY_LEAGUE_CONTENT_URI, id).buildUpon()
                    .appendQueryParameter(TIME_RANGE_PARAMETER, timeRange).build();
        }
        public static Uri buildScoreWithTeamAndTimeRange(long id, String timeRange, int count)
        {
            return ContentUris.withAppendedId(SCORES_BY_TEAM_CONTENT_URI, id).buildUpon()
                    .appendQueryParameter(TIME_RANGE_PARAMETER, timeRange)
                    .appendQueryParameter(LIMIT_PARAMETER, Integer.valueOf(count).toString())
                    .build();
        }
        public static Uri buildScoreWithId(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildScoreWithDate(String date)
        {
            return Uri.withAppendedPath(SCORES_BY_DATE_CONTENT_URI, date);
        }

        public static String getMatchIdFromUri(Uri uri) {

            return uri.getLastPathSegment();
        }

        public static String getDateFromUri(Uri uri) {

            return uri.getLastPathSegment();
        }

        public static String getLeagueIdFromUri(Uri uri) {

            return uri.getLastPathSegment();
        }

        public static String getTeamIdFromUri(Uri uri) {

            return uri.getLastPathSegment();
        }

        public static String getLimitParameterFromUri(Uri uri) {

            return uri.getQueryParameter(LIMIT_PARAMETER);
        }

        public static String getTimeRangeParameterFromUri(Uri uri) {
            return uri.getQueryParameter(TIME_RANGE_PARAMETER);
        }
    }

    public static final class LeaguesEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LEAGUES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_LEAGUES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_LEAGUES;

        public static final String TABLE_NAME = "league";
        //Table data
        public static final String NAME_COL = "name";
        public static final String YEAR_COL = "year";
        public static final String LEAGUE_ID_COL = "league_id";

        public static Uri buildLeagueUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class TeamsEntry implements BaseColumns
    {
        public static final String PATH_TEAMS_BY_LEAGUE = "league";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEAMS).build();
        public static final Uri TEAMS_BY_LEAGUE_CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEAMS)
                        .appendPath(PATH_TEAMS_BY_LEAGUE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_TEAMS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_TEAMS;

        public static final String TABLE_NAME = "team";
        //Table data
        public static final String NAME_COL = "name";
        public static final String CODE_COL = "code";
        public static final String CREST_COL = "crest_url";
        public static final String LEAGUE_ID_COL = "league_id";
        public static final String TEAM_ID_COL = "team_id";

        public static Uri buildTeamUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTeamsUriByLeagueId(long id) {
            return ContentUris.withAppendedId(TEAMS_BY_LEAGUE_CONTENT_URI, id);
        }

        public static String getLeagueIdFromUri(Uri uri) {

            return uri.getLastPathSegment();
        }
    }
}