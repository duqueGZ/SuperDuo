package barqsoft.footballscores;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by yehya khaled on 2/26/2015.
 * Modified by David Duque. 12/02/2015.
 */
public class ScoresViewHolder
{
    public TextView homeName;
    public TextView awayName;
    public TextView score;
    public TextView date;
    public ImageView homeCrest;
    public ImageView awayCrest;
    public LinearLayout matchItem;
    public double matchId;

    public ScoresViewHolder(View view)
    {
        homeName = (TextView) view.findViewById(R.id.home_name);
        awayName = (TextView) view.findViewById(R.id.away_name);
        score     = (TextView) view.findViewById(R.id.scores);
        date      = (TextView) view.findViewById(R.id.date);
        homeCrest = (ImageView) view.findViewById(R.id.home_crest);
        awayCrest = (ImageView) view.findViewById(R.id.away_crest);
        matchItem = (LinearLayout) view.findViewById(R.id.match_item);
    }
}
