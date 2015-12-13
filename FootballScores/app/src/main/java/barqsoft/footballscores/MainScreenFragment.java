package barqsoft.footballscores;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Arrays;

import barqsoft.footballscores.adapters.ScoresAdapter;
import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.data.ScoresProvider;
import barqsoft.footballscores.sync.FootballScoresSyncAdapter;
import barqsoft.footballscores.util.Utility;

public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public ScoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;

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
            DatabaseContract.ScoresEntry.TABLE_NAME + "."
                    + DatabaseContract.ScoresEntry.LEAGUE_ID_COL,
            DatabaseContract.ScoresEntry.TABLE_NAME + "."
                    + DatabaseContract.ScoresEntry.MATCHDAY_COL,
            ScoresProvider.HOME_TEAM_TABLE_ALIAS + "." + DatabaseContract.TeamsEntry.NAME_COL,
            ScoresProvider.AWAY_TEAM_TABLE_ALIAS + "." + DatabaseContract.TeamsEntry.NAME_COL,
    };
    // These indices are tied to SCORE_COLUMNS. If SCORE_COLUMNS changes, these must change too.
    public static final int COL_ID = 0;
    public static final int COL_SCORE_MATCH_ID = 1;
    public static final int COL_SCORE_HOME_GOALS = 2;
    public static final int COL_SCORE_AWAY_GOALS = 3;
    public static final int COL_SCORE_DATE = 4;
    public static final int COL_SCORE_TIME = 5;
    public static final int COL_SCORE_LEAGUE_ID = 6;
    public static final int COL_SCORE_MATCHDAY = 7;
    public static final int COL_SCORE_HOME = 8;
    public static final int COL_SCORE_AWAY = 9;

    private String fragmentDate = null;

    public MainScreenFragment()
    {
    }

    public void setFragmentDate(String date)
    {
        fragmentDate = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView scoreList = (ListView) rootView.findViewById(R.id.scores_list);
        mAdapter = new ScoresAdapter(getActivity(), null, 0);
        scoreList.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);
        mAdapter.setDetailMatchId(MainActivity.selectedMatchId);
        scoreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScoresViewHolder selected = (ScoresViewHolder) view.getTag();
                mAdapter.setDetailMatchId(selected.matchId);
                MainActivity.selectedMatchId = (int) selected.matchId;
                mAdapter.notifyDataSetChanged();
            }
        });

        if (savedInstanceState==null) {
            //Only call SyncAdapter the first time
            Utility.updateMatchesInfo(getActivity(), new int[]{SCORES_LOADER}, this,
                    Arrays.asList(FootballScoresSyncAdapter.MATCHES));
        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new CursorLoader(getActivity(),
                DatabaseContract.ScoresEntry.buildScoreWithDate(fragmentDate),
                SCORE_COLUMNS,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        int i = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            i++;
            cursor.moveToNext();
        }
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mAdapter.swapCursor(null);
    }
}
