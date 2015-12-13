package barqsoft.footballscores.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.util.Utility;

/*
 * Created by David Duque. 12/02/2015.
 */
public class FootballScoresSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String SYNC_MATCHES = "SYNC_MATCHES";
    public static final String SYNC_LEAGUES = "SYNC_LEAGUES";
    public static final String SYNC_TEAMS = "SYNC_TEAMS";
    public static final int MATCHES = 0;
    public static final int LEAGUES = 1;
    public static final int TEAMS = 2;

    public static final String ACTION_DATA_UPDATED =
            "barqsoft.footballscores.ACTION_DATA_UPDATED";

    // Interval at which to sync with the FD info, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    public static final String SYNC_FINISHED = "SYNC_FINISHED";
    public static final String SYNC_STARTED = "SYNC_STARTED";

    private static final String ID_PLACEHOLDER = "{id}";
    private static final String FD_LAST_TIME_FRAME = "p7";
    private static final String FD_NEXT_TIME_FRAME = "n7";
    private static final String FD_TIME_FRAME_PARAM = "timeFrame";
    private static final String FD_SEASON_PARAM = "season";
    private static final String FD_FIXTURES = "fixtures";
    private static final String FD_TEAMS = "teams";
    private static final String FD_LINKS = "_links";
    private static final String FD_SOCCERSEASON = "soccerseason";
    private static final String FD_HOMETEAM_LINK = "homeTeam";
    private static final String FD_AWAYTEAM_LINK = "awayTeam";
    private static final String FD_SELF_LINK = "self";
    private static final String FD_MATCH_DATE = "date";
    private static final String FD_RESULT = "result";
    private static final String FD_HOME_GOALS = "goalsHomeTeam";
    private static final String FD_AWAY_GOALS = "goalsAwayTeam";
    private static final String FD_MATCH_DAY = "matchday";
    private static final String FD_LINK_HREF = "href";
    private static final String FD_LEAGUE_CAPTION = "caption";
    private static final String FD_LEAGUE_YEAR = "year";
    private static final String FD_TEAM_NAME = "name";
    private static final String FD_TEAM_CODE = "code";
    private static final String FD_TEAM_CREST_URL = "crestUrl";
    private static final String FD_SEASON_LINK = "http://api.football-data.org/v1/soccerseasons/";
    private static final String FD_MATCH_LINK = "http://api.football-data.org/v1/fixtures/";
    private static final String FD_TEAM_LINK = "http://api.football-data.org/v1/teams/";
    private static final String FD_MATCHES_BASE_URL = "http://api.football-data.org/v1/fixtures?";
    private static final String FD_LEAGUES_BASE_URL =
            "http://api.football-data.org/v1/soccerseasons?";
    private static final String FD_TEAMS_BASE_URL =
            "http://api.football-data.org/v1/soccerseasons/" + ID_PLACEHOLDER + "/teams?";
    private static final String FOOTBALL_SCORES_CURRENT_YEAR = "2015";

    private final String LOG_TAG = FootballScoresSyncAdapter.class.getSimpleName();

    private boolean syncMatches;
    private boolean syncLeagues;
    private boolean syncTeams;


    public FootballScoresSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        syncMatches = extras.getBoolean(SYNC_MATCHES, Boolean.FALSE);
        syncLeagues = extras.getBoolean(SYNC_LEAGUES, Boolean.FALSE);
        syncTeams = extras.getBoolean(SYNC_TEAMS, Boolean.FALSE);

        Log.d(LOG_TAG, "Going to Sync... Matches: " + syncMatches + " - Leagues: " + syncLeagues +
                " - Teams: " + syncTeams);

        Intent startIntent = new Intent(SYNC_STARTED);
        getContext().sendBroadcast(startIntent);

        queryFootballScoresData(FD_LAST_TIME_FRAME);
        syncLeagues = Boolean.FALSE;
        syncTeams = Boolean.FALSE;
        queryFootballScoresData(FD_NEXT_TIME_FRAME);
    }



    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context, List items) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(SYNC_MATCHES, items.contains(MATCHES));
        bundle.putBoolean(SYNC_LEAGUES, items.contains(LEAGUES));
        bundle.putBoolean(SYNC_TEAMS, items.contains(TEAMS));
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        FootballScoresSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount,
                context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context, Arrays.asList(MATCHES, LEAGUES, TEAMS));
    }

    private void queryFootballScoresData(String timeFrame) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            if (syncMatches) {
                // Creating fetch URL
                Uri matchesUri = Uri.parse(FD_MATCHES_BASE_URL).buildUpon().
                        appendQueryParameter(FD_TIME_FRAME_PARAM, timeFrame).build();

                // Opening Connection
                URL matchesUrl = new URL(matchesUri.toString());
                urlConnection = (HttpURLConnection) matchesUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.addRequestProperty("X-Auth-Token", getContext()
                        .getString(R.string.api_key));
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier
                    buffer.append(line).append("\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return;
                }
                String jsonData = buffer.toString();
                // Check if the data contains any matches.
                JSONArray matches = new JSONObject(jsonData).getJSONArray("fixtures");
                if (matches.length() == 0) {
                    // If there is no data, call the function on dummy data
                    // This is expected behavior during the off season.
                    getMatchesDataFromJson(getContext().getString(R.string.dummy_data), false);
                    Log.d(LOG_TAG, "Football Scores matches sync correctly ended " +
                            "with DUMMY data");
                    return;
                }
                getMatchesDataFromJson(jsonData, true);
                Log.d(LOG_TAG, "Football Scores matches sync correctly ended");
            }

            // Get additional data (leagues and teams)
            makeLeaguesDataQuery();

            updateWidgets();

            Log.d(LOG_TAG, "Football Scores Sync correctly ended");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error: " + e.getMessage(), e);
            // If the code didn't successfully get the matches data, there's no point in
            // attempting to parse it.
        } finally {
            Log.d(LOG_TAG, "Finishing Football Scores Sync");
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException ioe) {
                    Log.e(LOG_TAG, "Error closing stream: " + ioe.getMessage(), ioe);
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            Intent finishIntent = new Intent(SYNC_FINISHED);
            getContext().sendBroadcast(finishIntent);
        }
    }

    /**
     * Take the String representing the complete obtained matches data in JSON Format and
     * pull out the needed data
     */
    private void getMatchesDataFromJson(String jsonData, boolean isReal)
            throws JSONException, InterruptedException {
        // Match data
        String league;
        String date;
        String time;
        String homeTeam;
        String awayTeam;
        String homeGoals;
        String awayGoals;
        String matchId;
        String matchday;

        try {
            JSONArray matches = new JSONObject(jsonData).getJSONArray(FD_FIXTURES);

            // ContentValues to be inserted
            Vector<ContentValues> values = new Vector <ContentValues> (matches.length());
            for(int i = 0;i < matches.length();i++)
            {

                JSONObject matchData = matches.getJSONObject(i);
                league = matchData.getJSONObject(FD_LINKS).getJSONObject(FD_SOCCERSEASON).
                        getString(FD_LINK_HREF).replace(FD_SEASON_LINK, "");
                // This controls which leagues we're interested in the data from.
                if(league.equals(Integer.valueOf(Utility.PREMIER_LEAGUE).toString())      ||
                   league.equals(Integer.valueOf(Utility.SERIE_A).toString())             ||
                   league.equals(Integer.valueOf(Utility.BUNDESLIGA_1).toString())        ||
                   league.equals(Integer.valueOf(Utility.PRIMERA_DIVISION).toString()))
                {
                    matchId = matchData.getJSONObject(FD_LINKS).getJSONObject(FD_SELF_LINK).
                            getString(FD_LINK_HREF).replace(FD_MATCH_LINK, "");
                    if(!isReal){
                        // This if statement changes the match ID of the dummy data so that it all
                        // goes into the database
                        matchId= matchId + Integer.toString(i);
                    }

                    // Get date and time match data
                    date = matchData.getString(FD_MATCH_DATE);
                    time = date.substring(date.indexOf("T") + 1, date.indexOf("Z"));
                    date = date.substring(0, date.indexOf("T"));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parsedDate = sdf.parse(date+time);
                        SimpleDateFormat newSdf = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        newSdf.setTimeZone(TimeZone.getDefault());
                        date = newSdf.format(parsedDate);
                        time = date.substring(date.indexOf(":") + 1);
                        date = date.substring(0,date.indexOf(":"));

                        if(!isReal){
                            // This if statement changes the dummy data's date to match our
                            // current date range.
                            Date fragmentDate =
                                    new Date(System.currentTimeMillis()+((i-2)*86400000));
                            SimpleDateFormat dummySdf = new SimpleDateFormat("yyyy-MM-dd");
                            date=dummySdf.format(fragmentDate);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d(LOG_TAG, "an ERROR occurred parsing match " + matchId + " date data");
                        Log.e(LOG_TAG,e.getMessage());
                    }

                    // Get the rest of match data
                    homeTeam = matchData.getJSONObject(FD_LINKS).getJSONObject(FD_HOMETEAM_LINK).
                            getString(FD_LINK_HREF).replace(FD_TEAM_LINK, "");
                    awayTeam = matchData.getJSONObject(FD_LINKS).getJSONObject(FD_AWAYTEAM_LINK).
                            getString(FD_LINK_HREF).replace(FD_TEAM_LINK, "");
                    homeGoals = matchData.getJSONObject(FD_RESULT).getString(FD_HOME_GOALS);
                    awayGoals = matchData.getJSONObject(FD_RESULT).getString(FD_AWAY_GOALS);
                    matchday = matchData.getString(FD_MATCH_DAY);

                    // Build match content values
                    ContentValues matchValues = new ContentValues();
                    matchValues.put(DatabaseContract.ScoresEntry.MATCH_ID_COL, matchId);
                    matchValues.put(DatabaseContract.ScoresEntry.DATE_COL, date);
                    matchValues.put(DatabaseContract.ScoresEntry.TIME_COL, time);
                    matchValues.put(DatabaseContract.ScoresEntry.HOME_ID_COL, homeTeam);
                    matchValues.put(DatabaseContract.ScoresEntry.AWAY_ID_COL, awayTeam);
                    matchValues.put(DatabaseContract.ScoresEntry.HOME_GOALS_COL, homeGoals);
                    matchValues.put(DatabaseContract.ScoresEntry.AWAY_GOALS_COL, awayGoals);
                    matchValues.put(DatabaseContract.ScoresEntry.LEAGUE_ID_COL, league);
                    matchValues.put(DatabaseContract.ScoresEntry.MATCHDAY_COL, matchday);

                    values.add(matchValues);
                }
            }

            if (values.size() > 0 ) {
                int insertedData = 0;
                ContentValues[] insertData = new ContentValues[values.size()];
                values.toArray(insertData);
                insertedData = getContext().getContentResolver().bulkInsert(
                        DatabaseContract.ScoresEntry.CONTENT_URI, insertData);

                // Delete old data (more than two weeks old) so we don't build up an endless history
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.WEEK_OF_YEAR, -2);
                getContext().getContentResolver().delete(DatabaseContract.ScoresEntry.CONTENT_URI,
                        DatabaseContract.ScoresEntry.DATE_COL + " <= ?",
                        new String[] {sdf.format(calendar.getTime())});

                Log.d(LOG_TAG, "Successfully Inserted (Matches): " + String.valueOf(insertedData));
            }
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }
    }

    private void makeLeaguesDataQuery() {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            if (syncLeagues) {
                // Creating fetch URL
                Uri leaguesUri = Uri.parse(FD_LEAGUES_BASE_URL).buildUpon().
                        appendQueryParameter(FD_SEASON_PARAM, FOOTBALL_SCORES_CURRENT_YEAR).build();

                // Opening Connection
                URL leaguesUrl = new URL(leaguesUri.toString());
                urlConnection = (HttpURLConnection) leaguesUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.addRequestProperty("X-Auth-Token", getContext()
                        .getString(R.string.api_key));
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier
                    buffer.append(line).append("\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return;
                }
                getLeaguesDataFromJson(buffer.toString());
                Log.d(LOG_TAG, "Football Scores leagues sync correctly ended");
            }
        }catch(Exception e){
            Log.e(LOG_TAG, "Error: " + e.getMessage(), e);
            // If the code didn't successfully get the movie data, there's no point in
            // attempting to parse it.
        }finally{
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException ioe) {
                    Log.e(LOG_TAG, "Error closing stream: " + ioe.getMessage(), ioe);
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * Take the String representing the complete obtained leagues data in JSON Format and
     * pull out the needed data
     */
    private void getLeaguesDataFromJson(String jsonData) throws JSONException {
        JSONArray leagues = new JSONArray(jsonData);

        // ContentValues to be inserted
        Vector<ContentValues> values = new Vector <ContentValues> (leagues.length());
        Vector<String> resultLeagueIds = new Vector<String>();

        for(int i = 0;i < leagues.length();i++) {
            JSONObject leagueData = leagues.getJSONObject(i);

            String leagueId = leagueData.getJSONObject(FD_LINKS).getJSONObject(FD_SELF_LINK).
                    getString(FD_LINK_HREF).replace(FD_SEASON_LINK, "");
            // This controls which leagues we're interested in the data from.
            if(leagueId.equals(Integer.valueOf(Utility.PREMIER_LEAGUE).toString())         ||
                    leagueId.equals(Integer.valueOf(Utility.SERIE_A).toString())           ||
                    leagueId.equals(Integer.valueOf(Utility.BUNDESLIGA_1).toString())      ||
                    leagueId.equals(Integer.valueOf(Utility.PRIMERA_DIVISION).toString())) {
                String leagueName = leagueData.getString(FD_LEAGUE_CAPTION);
                String leagueYear = leagueData.getString(FD_LEAGUE_YEAR);

                ContentValues league = new ContentValues();
                league.put(DatabaseContract.LeaguesEntry.LEAGUE_ID_COL, leagueId);
                league.put(DatabaseContract.LeaguesEntry.NAME_COL, leagueName);
                league.put(DatabaseContract.LeaguesEntry.YEAR_COL, leagueYear);

                resultLeagueIds.add(leagueId);
                values.add(league);
            }
        }

        // Add to DB
        if (values.size() > 0 ) {
            int insertedData = 0;
            ContentValues[] insertData = new ContentValues[values.size()];
            values.toArray(insertData);
            insertedData = getContext().getContentResolver().bulkInsert(
                    DatabaseContract.LeaguesEntry.CONTENT_URI, insertData);

            Log.d(LOG_TAG, "Successfully Inserted (Leagues): " + String.valueOf(insertedData));
        }

        // Get additional data (teams)
        Vector<ContentValues> resultTeams = new Vector<ContentValues>();
        if (syncTeams) {
            for (String leagueId : resultLeagueIds) {
                resultTeams.addAll(makeTeamsDataQuery(leagueId));
            }

            if (resultTeams.size() > 0) {
                int insertedData = 0;
                ContentValues[] teamValues = new ContentValues[resultTeams.size()];
                resultTeams.toArray(teamValues);
                insertedData = getContext().getContentResolver()
                        .bulkInsert(DatabaseContract.TeamsEntry.CONTENT_URI, teamValues);

                Log.d(LOG_TAG, "Successfully Inserted (Teams): " + String.valueOf(insertedData));
            }
        }
    }

    private Vector<ContentValues> makeTeamsDataQuery(String leagueId) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            // Creating fetch URL
            Uri teamsUri = Uri.parse(FD_TEAMS_BASE_URL
                    .replace(ID_PLACEHOLDER, leagueId)).buildUpon()
                    .build();

            // Opening Connection
            URL teamsUrl = new URL(teamsUri.toString());
            urlConnection = (HttpURLConnection) teamsUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.addRequestProperty("X-Auth-Token", getContext()
                    .getString(R.string.api_key));
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return new Vector<ContentValues>();
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier
                buffer.append(line).append("\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return new Vector<ContentValues>();
            }

            return getTeamsDataFromJson(buffer.toString(), leagueId);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error: " + e.getMessage(), e);
            // If the code didn't successfully get the movie data, there's no point in
            // attempting to parse it.
            return new Vector<ContentValues>();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException ioe) {
                    Log.e(LOG_TAG, "Error closing stream: " + ioe.getMessage(), ioe);
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * Take the String representing the complete obtained teams data in JSON Format and
     * pull out the needed data
     */
    private Vector<ContentValues> getTeamsDataFromJson(String jsonData, String leagueId)
            throws JSONException {
        JSONArray teams = new JSONObject(jsonData).getJSONArray(FD_TEAMS);
        Vector<ContentValues> resultTeams = new Vector<ContentValues>(teams.length());

        for(int i = 0;i < teams.length();i++) {
            JSONObject teamData = teams.getJSONObject(i);

            String teamId = teamData.getJSONObject(FD_LINKS).getJSONObject(FD_SELF_LINK).
                    getString(FD_LINK_HREF).replace(FD_TEAM_LINK, "");
            String teamName = teamData.getString(FD_TEAM_NAME);
            String teamCode = teamData.getString(FD_TEAM_CODE);
            String teamCrestUrl = teamData.getString(FD_TEAM_CREST_URL);

            ContentValues team = new ContentValues();
            team.put(DatabaseContract.TeamsEntry.TEAM_ID_COL, teamId);
            team.put(DatabaseContract.TeamsEntry.NAME_COL, teamName);
            team.put(DatabaseContract.TeamsEntry.CODE_COL, teamCode);
            team.put(DatabaseContract.TeamsEntry.CREST_COL, teamCrestUrl);
            team.put(DatabaseContract.TeamsEntry.LEAGUE_ID_COL, leagueId);

            resultTeams.add(team);
        }

        return resultTeams;
    }

    private void updateWidgets() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}