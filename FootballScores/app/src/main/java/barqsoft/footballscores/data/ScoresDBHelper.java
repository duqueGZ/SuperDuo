package barqsoft.footballscores.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yehya khaled on 2/25/2015.
 * Modified by David Duque. 12/02/2015.
 */
public class ScoresDBHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "scores.db";
    private static final int DATABASE_VERSION = 8;
    public ScoresDBHelper(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        final String CreateScoresTable = "CREATE TABLE "
                + DatabaseContract.ScoresEntry.TABLE_NAME + " ("
                + DatabaseContract.ScoresEntry._ID + " INTEGER PRIMARY KEY,"
                + DatabaseContract.ScoresEntry.DATE_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresEntry.TIME_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresEntry.HOME_ID_COL + " INTEGER NOT NULL,"
                + DatabaseContract.ScoresEntry.AWAY_ID_COL + " INTEGER NOT NULL,"
                + DatabaseContract.ScoresEntry.LEAGUE_ID_COL + " INTEGER NOT NULL,"
                + DatabaseContract.ScoresEntry.HOME_GOALS_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresEntry.AWAY_GOALS_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresEntry.MATCH_ID_COL + " INTEGER NOT NULL,"
                + DatabaseContract.ScoresEntry.MATCHDAY_COL + " INTEGER NOT NULL,"
                + " UNIQUE (" + DatabaseContract.ScoresEntry.MATCH_ID_COL + ") ON CONFLICT REPLACE"
                + " FOREIGN KEY (" + DatabaseContract.ScoresEntry.LEAGUE_ID_COL
                + ") REFERENCES "  + DatabaseContract.LeaguesEntry.TABLE_NAME
                + " (" + DatabaseContract.LeaguesEntry.LEAGUE_ID_COL + ")"
                + " FOREIGN KEY (" + DatabaseContract.ScoresEntry.HOME_ID_COL
                + ") REFERENCES "  + DatabaseContract.TeamsEntry.TABLE_NAME
                + " (" + DatabaseContract.TeamsEntry.TEAM_ID_COL + ")"
                + " FOREIGN KEY (" + DatabaseContract.ScoresEntry.AWAY_ID_COL
                + ") REFERENCES "  + DatabaseContract.TeamsEntry.TABLE_NAME
                + " (" + DatabaseContract.TeamsEntry.TEAM_ID_COL + ")"
                + " );";

        final String CreateLeaguesTable = "CREATE TABLE "
                + DatabaseContract.LeaguesEntry.TABLE_NAME + " ("
                + DatabaseContract.LeaguesEntry._ID + " INTEGER PRIMARY KEY,"
                + DatabaseContract.LeaguesEntry.NAME_COL + " TEXT NOT NULL,"
                + DatabaseContract.LeaguesEntry.YEAR_COL + " INTEGER NOT NULL,"
                + DatabaseContract.LeaguesEntry.LEAGUE_ID_COL + " INTEGER NOT NULL,"
                + " UNIQUE (" + DatabaseContract.LeaguesEntry.LEAGUE_ID_COL
                + ") ON CONFLICT REPLACE"
                + " );";

        final String CreateTeamsTable = "CREATE TABLE "
                + DatabaseContract.TeamsEntry.TABLE_NAME + " ("
                + DatabaseContract.TeamsEntry._ID + " INTEGER PRIMARY KEY,"
                + DatabaseContract.TeamsEntry.NAME_COL + " TEXT NOT NULL,"
                + DatabaseContract.TeamsEntry.CODE_COL + " TEXT,"
                + DatabaseContract.TeamsEntry.CREST_COL + " TEXT,"
                + DatabaseContract.TeamsEntry.LEAGUE_ID_COL + " INTEGER NOT NULL,"
                + DatabaseContract.TeamsEntry.TEAM_ID_COL + " INTEGER NOT NULL,"
                + " UNIQUE (" + DatabaseContract.TeamsEntry.TEAM_ID_COL + ") ON CONFLICT REPLACE"
                + " FOREIGN KEY (" + DatabaseContract.TeamsEntry.LEAGUE_ID_COL
                + ") REFERENCES "  + DatabaseContract.LeaguesEntry.TABLE_NAME
                + " (" + DatabaseContract.LeaguesEntry.LEAGUE_ID_COL + ")"
                + ");";

        db.execSQL(CreateScoresTable);
        db.execSQL(CreateLeaguesTable);
        db.execSQL(CreateTeamsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.ScoresEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.LeaguesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.TeamsEntry.TABLE_NAME);
        onCreate(db);
    }
}