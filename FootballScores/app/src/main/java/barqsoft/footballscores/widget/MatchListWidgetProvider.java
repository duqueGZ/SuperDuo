package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.sync.FootballScoresSyncAdapter;
import barqsoft.footballscores.util.Utility;

/**
 * Created by David Duque. 12/02/2015.
 * Provider for a scrollable match list widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MatchListWidgetProvider extends AppWidgetProvider {
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider 
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_match_list);

            // Create an Intent to launch MainActivity 
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Set up the collection 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views, appWidgetId);
            } else {
                setRemoteAdapterV11(context, views, appWidgetId);
            }

            views.setEmptyView(R.id.widget_list, R.id.widget_empty);

            SharedPreferences prefs =
                    context.getSharedPreferences(
                            MatchListWidgetConfigurationActivity.MATCH_LIST_WIDGET_SHARED_PREFS
                            + "_" + appWidgetId, Context.MODE_PRIVATE);

            String timeRange = prefs
                    .getString(MatchListWidgetConfigurationActivity.WIDGET_PREF_TIME_RANGE,
                            null);
            views.setTextViewText(R.id.widget_title,
                    Utility.timeRangeParameterValueToOption(context, timeRange));

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);

        if (FootballScoresSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        }
    }


    /**
     * Sets the remote adapter used to fill in the list items 
     *
     * @param views RemoteViews to set the RemoteAdapter 
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views,
                                  int appWidgetId) {
        Intent intent = new Intent(context, MatchListWidgetRemoteViewsService.class);
        intent.setData(Uri.fromParts("content", String.valueOf(appWidgetId), null));
        views.setRemoteAdapter(R.id.widget_list, intent);
    }


    /**
     * Sets the remote adapter used to fill in the list items 
     *
     * @param views RemoteViews to set the RemoteAdapter 
     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views,
                                     int appWidgetId) {
        Intent intent = new Intent(context, MatchListWidgetRemoteViewsService.class);
        intent.setData(Uri.fromParts("content", String.valueOf(appWidgetId), null));
        views.setRemoteAdapter(0, R.id.widget_list, intent);
    }
}