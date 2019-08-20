package candybar.lib.services;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import candybar.lib.R;

public class CandyBarWidgetService extends AppWidgetProvider {

    public void onReceive(Context context, Intent intent) {
        String act = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(act)) {
            RemoteViews clockView = new RemoteViews(context.getPackageName(), R.layout.analog_clock);

            AppWidgetManager.getInstance(context).updateAppWidget(intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS), clockView);
        }
    }
}
