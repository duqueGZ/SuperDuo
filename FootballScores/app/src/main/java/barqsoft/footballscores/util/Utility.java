package barqsoft.footballscores.util;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import barqsoft.footballscores.R;
import barqsoft.footballscores.sync.FootballScoresSyncAdapter;

/**
 * Created by yehya khaled on 3/3/2015.
 * Modified by David Duque. 12/02/2015.
 */
public class Utility
{
    // Currently Football Scores 2015/2016 season supported leagues.
    // In fall of 2016, they will need to be updated
    public static final int SERIE_A = 401;
    public static final int PREMIER_LEAGUE = 398;
    public static final int PRIMERA_DIVISION = 399;
    public static final int BUNDESLIGA_1 = 394;

    public static final String LAST_WEEK_CODE = "last";
    public static final String NEXT_WEEK_CODE = "next";
    public static final String BOTH_WEEKS_CODE = "both";

    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static String getLeague(Context context, int leagueId)
    {
        switch (leagueId)
        {
            case SERIE_A : return context.getString(R.string.serie_a);
            case PREMIER_LEAGUE: return context.getString(R.string.premier_league);
            case PRIMERA_DIVISION : return context.getString(R.string.primera_divison);
            case BUNDESLIGA_1: return context.getString(R.string.bundesliga);
            default: return context.getString(R.string.unknown_league);
        }
    }
    public static String getMatchday(Context context, int matchday)
    {
            return context.getString(R.string.matchday_text) + ": " + String.valueOf(matchday);
    }

    public static String getScores(int homeGoals, int awayGoals)
    {
        if(homeGoals < 0 || awayGoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(homeGoals) + " - " + String.valueOf(awayGoals);
        }
    }

    public static int getTeamCrestByTeamName (String teamName)
    {
        if (teamName==null){return R.drawable.no_icon;}
        switch (teamName)
        {   // This is the set of icons that are currently locally in the app.
            case "Manchester United FC" : return R.drawable.manchester_united;
            case "Everton FC" : return R.drawable.everton_fc_logo1;
            case "West Ham United FC" : return R.drawable.west_ham;
            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC" : return R.drawable.sunderland;
            case "Stoke City FC" : return R.drawable.stoke_city;
            case "Southampton FC": return R.drawable.southampton_fc;
            case "Aston Villa FC": return R.drawable.aston_villa;
            case "Arsenal FC": return R.drawable.arsenal;
            case "Manchester City FC": return R.drawable.manchester_city;
            case "Swansea City FC": return R.drawable.swansea_city_afc;
            case "Leicester City FC": return R.drawable.leicester_city_fc_hd_logo;
            case "West Bromwich Albion FC": return R.drawable.west_bromwich_albion_hd_logo;
            case "Chelsea FC": return R.drawable.chelsea;
            case "Newcastle United FC": return R.drawable.newcastle_united;
            case "Liverpool FC": return R.drawable.liverpool;
            case "Crystal Palace FC": return R.drawable.crystal_palace_fc;
            default: return R.drawable.no_icon;
        }
    }

    public static void updateMatchesInfo(FragmentActivity activity, int[] loaderIds,
                                         LoaderManager.LoaderCallbacks callbacks,
                                         List itemsToSync) {
        for (int loaderId : loaderIds) {
            activity.getSupportLoaderManager().restartLoader(loaderId, null, callbacks);
        }
        FootballScoresSyncAdapter.syncImmediately(activity, itemsToSync);
    }

    public static String timeRangeOptionToParameterValue(Context context, String timeRange) {
        if (timeRange.equals(context.getString(R.string.last_week_time_option))) {
            return LAST_WEEK_CODE;
        }
        if (timeRange.equals(context.getString(R.string.next_week_time_option))) {
            return NEXT_WEEK_CODE;
        }
        if (timeRange.equals(context.getString(R.string.both_weeks_time_option))) {
            return BOTH_WEEKS_CODE;
        }

        return null;
    }

    public static String timeRangeParameterValueToOption(Context context, String timeRange) {
        if (timeRange==null) {
            return null;
        }

        if (timeRange.equals(LAST_WEEK_CODE)) {
            return context.getString(R.string.last_week_time_option);
        }
        if (timeRange.equals(NEXT_WEEK_CODE)) {
            return context.getString(R.string.next_week_time_option);
        }
        if (timeRange.equals(BOTH_WEEKS_CODE)) {
            return context.getString(R.string.both_weeks_time_option);
        }

        return null;
    }

    public static String getTimeRangeFromDate(String timeRange) {
        String fromDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        if ((timeRange.equals(LAST_WEEK_CODE))||(timeRange.equals(BOTH_WEEKS_CODE))) {
            calendar.add(Calendar.WEEK_OF_YEAR, -1);
            fromDate = sdf.format(calendar.getTime());
        } else if (timeRange.equals(NEXT_WEEK_CODE)) {
            fromDate = sdf.format(calendar.getTime());
        }

        return fromDate;
    }

    public static String getTimeRangeToDate(String timeRange) {
        String toDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        if ((timeRange.equals(NEXT_WEEK_CODE))||(timeRange.equals(BOTH_WEEKS_CODE))) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
            toDate = sdf.format(calendar.getTime());
        } else if (timeRange.equals(LAST_WEEK_CODE)) {
            toDate = sdf.format(calendar.getTime());
        }

        return toDate;
    }


    public static String getDateI18nFormatted(Context context, String fsDate) {

        SimpleDateFormat sourceSdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat destinationSdf = new SimpleDateFormat(context
                .getString(R.string.sdf_format));

        String formattedDate;
        try {
            formattedDate = destinationSdf.format(sourceSdf.parse(fsDate));
        } catch (ParseException e) {
            Log.d(LOG_TAG, "Error while parsing date: " + fsDate);
            formattedDate = fsDate;
        }
        return formattedDate;
    }

    public static String getMatchItemContentDescription(String homeGoals, String awayGoals,
                                                        String home, String away, String dateString,
                                                        String league, String matchday) {
        StringBuffer description = new StringBuffer();
        boolean hasResult = (homeGoals!=null) && (!homeGoals.isEmpty()) &&
                (!homeGoals.equals("null")) && (awayGoals!=null) && (!awayGoals.isEmpty()) &&
                (!awayGoals.equals("null"));

        description.append(home);
        if (hasResult) {
            description.append(" ").append(homeGoals);
        }
        description.append(" ")
                .append(away);
        if (hasResult) {
            description.append(" ").append(awayGoals);
        }
        description.append(", ")
                .append(dateString);
        if (league!=null) {
            description.append(", ").append(league);
        }
        if (matchday!=null) {
            description.append(" ").append(matchday);
        }
        return description.toString();
    }

    public static String getShareButtonContentDescription(Context context, String homeGoals,
                                                          String awayGoals, String home,
                                                          String away) {
        StringBuffer description = new StringBuffer();
        boolean hasResult = (homeGoals!=null) && (!homeGoals.isEmpty()) &&
                (!homeGoals.equals("null")) && (awayGoals!=null) && (!awayGoals.isEmpty())
                && (!awayGoals.equals("null"));

        description.append(context.getString(R.string.share_match))
                .append(": ")
                .append(home);
        if (hasResult) {
            description.append(" ").append(homeGoals);
        }
        description.append(" ")
                .append(away);
        if (hasResult) {
            description.append(" ").append(awayGoals);
        }

        return description.toString();
    }
}
