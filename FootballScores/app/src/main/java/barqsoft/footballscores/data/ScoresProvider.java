package barqsoft.footballscores.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import barqsoft.footballscores.util.Utility;

/**
 * Created by yehya khaled on 2/25/2015.
 * Modified by David Duque. 12/02/2015.
 */
public class ScoresProvider extends ContentProvider
{
    //"score.matchId = ?" selection String
    public static final String sScoresByMatchIdSelection = DatabaseContract.ScoresEntry.TABLE_NAME +
            "." + DatabaseContract.ScoresEntry.MATCH_ID_COL + " = ? ";
    //"score.date = ?" selection String
    public static final String sScoresByDateSelection = DatabaseContract.ScoresEntry.TABLE_NAME +
            "." + DatabaseContract.ScoresEntry.DATE_COL + " = ? ";
    //"score.league_id = ?" selection String
    public static final String sScoresByLeagueSelection = DatabaseContract.ScoresEntry.TABLE_NAME +
            "." + DatabaseContract.ScoresEntry.LEAGUE_ID_COL + " = ? ";
    //"score.league_id = ? and (score.date between ? and ?)" selection String
    public static final String sScoresByLeagueAndTimeRangeSelection =
            DatabaseContract.ScoresEntry.TABLE_NAME + "." +
                    DatabaseContract.ScoresEntry.LEAGUE_ID_COL + " = ? AND (" +
                    DatabaseContract.ScoresEntry.DATE_COL + " BETWEEN ? AND ?)";
    //"score.home_id = ? or score.away_id = ?" selection String
    public static final String sScoresByTeamSelection = DatabaseContract.ScoresEntry.TABLE_NAME +
            "." + DatabaseContract.ScoresEntry.HOME_ID_COL + " = ? OR "
            + DatabaseContract.ScoresEntry.TABLE_NAME + "."
            + DatabaseContract.ScoresEntry.AWAY_ID_COL + " = ?";
    //"(score.home_id = ? or score.away_id = ?) and (score.date between ? and ?)" selection String
    public static final String sScoresByTeamAndTimeRangeSelection =
            "(" + DatabaseContract.ScoresEntry.TABLE_NAME + "." +
                    DatabaseContract.ScoresEntry.HOME_ID_COL + " = ? OR " +
                    DatabaseContract.ScoresEntry.TABLE_NAME + "." +
                    DatabaseContract.ScoresEntry.AWAY_ID_COL + " = ?) AND (" +
                    DatabaseContract.ScoresEntry.DATE_COL + " BETWEEN ? AND ?)";
    //"team.league_id = ?" selection String
    public static final String sTeamsByLeagueSelection = DatabaseContract.TeamsEntry.TABLE_NAME +
            "." + DatabaseContract.TeamsEntry.LEAGUE_ID_COL + " = ? ";

    public static final String HOME_TEAM_TABLE_ALIAS = "home";
    public static final String AWAY_TEAM_TABLE_ALIAS = "away";

    private static ScoresDBHelper mOpenHelper;
    private static final int MATCHES = 100;
    private static final int MATCHES_WITH_LEAGUE = 101;
    private static final int MATCHES_WITH_ID = 102;
    private static final int MATCHES_WITH_DATE = 103;
    private static final int MATCHES_WITH_TEAM = 104;
    private static final int LEAGUES = 200;
    private static final int TEAMS = 300;
    private static final int TEAMS_WITH_LEAGUE = 301;
    private UriMatcher sUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sScoreQueryBuilder;
    private static final SQLiteQueryBuilder sLeagueQueryBuilder;
    private static final SQLiteQueryBuilder sTeamQueryBuilder;

    static{
        sScoreQueryBuilder = new SQLiteQueryBuilder();
        sScoreQueryBuilder.setTables(DatabaseContract.ScoresEntry.TABLE_NAME + " INNER JOIN " +
                DatabaseContract.TeamsEntry.TABLE_NAME + " " + HOME_TEAM_TABLE_ALIAS + " ON " +
                DatabaseContract.ScoresEntry.TABLE_NAME + "." +
                DatabaseContract.ScoresEntry.HOME_ID_COL + " = " + HOME_TEAM_TABLE_ALIAS + "." +
                DatabaseContract.TeamsEntry.TEAM_ID_COL + " INNER JOIN " +
                DatabaseContract.TeamsEntry.TABLE_NAME + " " + AWAY_TEAM_TABLE_ALIAS + " ON " +
                DatabaseContract.ScoresEntry.TABLE_NAME + "." +
                DatabaseContract.ScoresEntry.AWAY_ID_COL + " = " + AWAY_TEAM_TABLE_ALIAS + "." +
                DatabaseContract.TeamsEntry.TEAM_ID_COL);
        sLeagueQueryBuilder = new SQLiteQueryBuilder();
        sLeagueQueryBuilder.setTables(DatabaseContract.LeaguesEntry.TABLE_NAME);
        sTeamQueryBuilder = new SQLiteQueryBuilder();
        sTeamQueryBuilder.setTables(DatabaseContract.TeamsEntry.TABLE_NAME);
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DatabaseContract.PATH_SCORES , MATCHES);
        matcher.addURI(authority,
                DatabaseContract.PATH_SCORES + "/"
                        + DatabaseContract.ScoresEntry.PATH_SCORES_BY_LEAGUE + "/*",
                MATCHES_WITH_LEAGUE);
        matcher.addURI(authority,
                DatabaseContract.PATH_SCORES + "/"
                        + DatabaseContract.ScoresEntry.PATH_SCORES_BY_TEAM + "/*",
                MATCHES_WITH_TEAM);
        matcher.addURI(authority,
                DatabaseContract.PATH_SCORES + "/"
                        + DatabaseContract.ScoresEntry.PATH_SCORES_BY_DATE + "/*",
                MATCHES_WITH_DATE);
        matcher.addURI(authority, DatabaseContract.PATH_SCORES + "/*" , MATCHES_WITH_ID);
        matcher.addURI(authority, DatabaseContract.PATH_LEAGUES , LEAGUES);
        matcher.addURI(authority, DatabaseContract.PATH_TEAMS , TEAMS);
        matcher.addURI(authority,
                DatabaseContract.PATH_TEAMS + "/"
                        + DatabaseContract.TeamsEntry.PATH_TEAMS_BY_LEAGUE + "/*",
                TEAMS_WITH_LEAGUE);

        return matcher;
    }

    @Override
    public boolean onCreate()
    {
        mOpenHelper = new ScoresDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri)
    {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MATCHES:
                return DatabaseContract.ScoresEntry.CONTENT_TYPE;
            case MATCHES_WITH_LEAGUE:
                return DatabaseContract.ScoresEntry.CONTENT_TYPE;
            case MATCHES_WITH_TEAM:
                return DatabaseContract.ScoresEntry.CONTENT_TYPE;
            case MATCHES_WITH_DATE:
                return DatabaseContract.ScoresEntry.CONTENT_TYPE;
            case MATCHES_WITH_ID:
                return DatabaseContract.ScoresEntry.CONTENT_ITEM_TYPE;
            case LEAGUES:
                return DatabaseContract.LeaguesEntry.CONTENT_TYPE;
            case TEAMS:
                return DatabaseContract.TeamsEntry.CONTENT_TYPE;
            case TEAMS_WITH_LEAGUE:
                return DatabaseContract.TeamsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri :" + uri );
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case MATCHES:
                rowsUpdated = db.update(DatabaseContract.ScoresEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case LEAGUES:
                rowsUpdated = db.update(DatabaseContract.LeaguesEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TEAMS:
                rowsUpdated = db.update(DatabaseContract.TeamsEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown update uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder)
    {
        Cursor retCursor;
        switch (sUriMatcher.match(uri))
        {
            case MATCHES: {
                retCursor = sScoreQueryBuilder.query(mOpenHelper.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case MATCHES_WITH_DATE: {
                String date = DatabaseContract.ScoresEntry.getDateFromUri(uri);
                retCursor = sScoreQueryBuilder.query(mOpenHelper.getReadableDatabase(), projection,
                        sScoresByDateSelection, new String[]{date}, null, null, sortOrder);
                break;
            }
            case MATCHES_WITH_ID: {
                String matchId = DatabaseContract.ScoresEntry.getMatchIdFromUri(uri);
                retCursor = sScoreQueryBuilder.query(mOpenHelper.getReadableDatabase(), projection,
                        sScoresByMatchIdSelection, new String[]{matchId}, null, null, sortOrder);
                break;
            }
            case MATCHES_WITH_LEAGUE: {
                String league = DatabaseContract.ScoresEntry.getLeagueIdFromUri(uri);
                String timeRange = DatabaseContract.ScoresEntry.getTimeRangeParameterFromUri(uri);
                String leagueSelection;
                String leagueSelectionArgs[];
                if (timeRange==null) {
                    leagueSelection = sScoresByLeagueSelection;
                    leagueSelectionArgs = new String[]{league};
                } else {
                    leagueSelection = sScoresByLeagueAndTimeRangeSelection;
                    leagueSelectionArgs = new String[]{league,
                            Utility.getTimeRangeFromDate(timeRange),
                            Utility.getTimeRangeToDate(timeRange)};
                }
                retCursor = sScoreQueryBuilder.query(mOpenHelper.getReadableDatabase(), projection,
                        leagueSelection, leagueSelectionArgs, null, null, sortOrder);
                break;
            }
            case MATCHES_WITH_TEAM: {
                String team = DatabaseContract.ScoresEntry.getTeamIdFromUri(uri);
                String timeRange = DatabaseContract.ScoresEntry.getTimeRangeParameterFromUri(uri);
                String teamSelection;
                String teamSelectionArgs[];
                if (timeRange==null) {
                    teamSelection = sScoresByTeamSelection;
                    teamSelectionArgs = new String[]{team, team};
                } else {
                    teamSelection = sScoresByTeamAndTimeRangeSelection;
                    teamSelectionArgs = new String[]{team, team,
                            Utility.getTimeRangeFromDate(timeRange),
                            Utility.getTimeRangeToDate(timeRange)};
                }
                retCursor = sScoreQueryBuilder.query(mOpenHelper.getReadableDatabase(), projection,
                        teamSelection, teamSelectionArgs, null, null, sortOrder,
                        DatabaseContract.ScoresEntry.getLimitParameterFromUri(uri));
                break;
            }
            case LEAGUES: {
                retCursor = sLeagueQueryBuilder.query(mOpenHelper.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case TEAMS: {
                retCursor = sTeamQueryBuilder.query(mOpenHelper.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case TEAMS_WITH_LEAGUE: {
                String league = DatabaseContract.TeamsEntry.getLeagueIdFromUri(uri);
                retCursor = sTeamQueryBuilder.query(mOpenHelper.getReadableDatabase(), projection,
                        sTeamsByLeagueSelection, new String[]{league}, null, null, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown query uri" + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);

        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case MATCHES: {
                long _id = db.insert(DatabaseContract.ScoresEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DatabaseContract.ScoresEntry.buildScoreWithId(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LEAGUES: {
                long _id = db.insert(DatabaseContract.LeaguesEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DatabaseContract.LeaguesEntry.buildLeagueUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TEAMS: {
                long _id = db.insert(DatabaseContract.TeamsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DatabaseContract.TeamsEntry.buildTeamUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri))
        {
            case MATCHES: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db
                                .insertWithOnConflict(DatabaseContract.ScoresEntry.TABLE_NAME, null,
                                        value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case LEAGUES: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db
                                .insertWithOnConflict(DatabaseContract.LeaguesEntry.TABLE_NAME, null,
                                        value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case TEAMS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db
                                .insertWithOnConflict(DatabaseContract.TeamsEntry.TABLE_NAME, null,
                                        value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            default:
                throw new UnsupportedOperationException("Unknown bulkInsert uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;
        // This makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";

        switch (sUriMatcher.match(uri)) {
            case MATCHES:
                rowsDeleted = db.delete(DatabaseContract.ScoresEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case LEAGUES:
                rowsDeleted = db.delete(DatabaseContract.LeaguesEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case TEAMS:
                rowsDeleted = db.delete(DatabaseContract.TeamsEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown delete uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
