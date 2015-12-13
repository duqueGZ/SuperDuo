package barqsoft.footballscores.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import barqsoft.footballscores.MainScreenFragment;
import barqsoft.footballscores.R;
import barqsoft.footballscores.ScoresViewHolder;
import barqsoft.footballscores.util.Utility;

/**
 * Created by yehya khaled on 2/26/2015.
 * Modified by David Duque. 12/02/2015.
 */
public class ScoresAdapter extends CursorAdapter
{
    private double detailMatchId = 0;
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";
    public ScoresAdapter(Context context, Cursor cursor, int flags)
    {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ScoresViewHolder mHolder = new ScoresViewHolder(mItem);
        mItem.setTag(mHolder);
        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor)
    {
        final ScoresViewHolder mHolder = (ScoresViewHolder) view.getTag();
        mHolder.homeName.setText(cursor.getString(MainScreenFragment.COL_SCORE_HOME));
        mHolder.awayName.setText(cursor.getString(MainScreenFragment.COL_SCORE_AWAY));
        String dateString = Utility.getDateI18nFormatted(context, cursor
                .getString(MainScreenFragment.COL_SCORE_DATE))
                + " " + cursor.getString(MainScreenFragment.COL_SCORE_TIME);
        mHolder.date.setText(dateString);
        mHolder.score.setText(Utility
                .getScores(cursor.getInt(MainScreenFragment.COL_SCORE_HOME_GOALS),
                        cursor.getInt(MainScreenFragment.COL_SCORE_AWAY_GOALS)));
        mHolder.matchId = cursor.getDouble(MainScreenFragment.COL_SCORE_MATCH_ID);
        mHolder.homeCrest.setImageResource(Utility
                .getTeamCrestByTeamName(cursor.getString(MainScreenFragment.COL_SCORE_HOME)));
        mHolder.awayCrest.setImageResource(Utility
                .getTeamCrestByTeamName(cursor.getString(MainScreenFragment.COL_SCORE_AWAY)));
        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);
        if(mHolder.matchId == detailMatchId)
        {
            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            TextView matchDay = (TextView) v.findViewById(R.id.matchday_textview);
            String matchDayString = Utility.getMatchday(context, cursor
                    .getInt(MainScreenFragment.COL_SCORE_MATCHDAY));
            matchDay.setText(matchDayString);
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            String leagueString = Utility
                    .getLeague(context, cursor.getInt(MainScreenFragment.COL_SCORE_LEAGUE_ID));
            league.setText(leagueString);
            Button shareButton = (Button) v.findViewById(R.id.share_button);
            shareButton.setContentDescription(Utility.getShareButtonContentDescription(context,
                    cursor.getString(MainScreenFragment.COL_SCORE_HOME_GOALS),
                    cursor.getString(MainScreenFragment.COL_SCORE_AWAY_GOALS),
                    cursor.getString(MainScreenFragment.COL_SCORE_HOME),
                    cursor.getString(MainScreenFragment.COL_SCORE_AWAY)));
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(mHolder.homeName.getText() + " "
                            + mHolder.score.getText() + " " + mHolder.awayName.getText() + " "));
                }
            });
            mHolder.matchItem.setContentDescription(Utility.getMatchItemContentDescription(
                    cursor.getString(MainScreenFragment.COL_SCORE_HOME_GOALS),
                    cursor.getString(MainScreenFragment.COL_SCORE_AWAY_GOALS),
                    cursor.getString(MainScreenFragment.COL_SCORE_HOME),
                    cursor.getString(MainScreenFragment.COL_SCORE_AWAY),
                    dateString, leagueString, matchDayString));
        }
        else
        {
            mHolder.matchItem.setContentDescription(Utility.getMatchItemContentDescription(
                    cursor.getString(MainScreenFragment.COL_SCORE_HOME_GOALS),
                    cursor.getString(MainScreenFragment.COL_SCORE_AWAY_GOALS),
                    cursor.getString(MainScreenFragment.COL_SCORE_HOME),
                    cursor.getString(MainScreenFragment.COL_SCORE_AWAY),
                    dateString, null, null));
            container.removeAllViews();
        }

    }

    public Intent createShareForecastIntent(String shareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

    public void setDetailMatchId(double matchId) {
        detailMatchId = matchId;
    }

    public double getDetailMatchId() {
        return detailMatchId;
    }

}
