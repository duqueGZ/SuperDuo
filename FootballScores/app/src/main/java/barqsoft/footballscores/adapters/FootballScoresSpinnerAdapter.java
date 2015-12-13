package barqsoft.footballscores.adapters;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import barqsoft.footballscores.R;

public class FootballScoresSpinnerAdapter extends ArrayAdapter<SpinnerItem> {

    private Context context;
    private SpinnerItem[] values;

    public FootballScoresSpinnerAdapter(Context context, int textViewResourceId,
                                        SpinnerItem[] values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    public int getCount(){
        return values.length;
    }

    public SpinnerItem getItem(int position){
        return values[position];
    }

    public long getItemId(int position){
        return values[position].getId();
    }

    // This is for the "passive" state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(values[position].getValue());
        label.setTextColor(getContext().getResources().getColor(R.color.black));
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getContext().getResources().getDimension(R.dimen.widget_spinner_text));
        label.setPadding(8,8,8,8);
        return label;
    }

    // Here is when the "chooser" is popped up
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
