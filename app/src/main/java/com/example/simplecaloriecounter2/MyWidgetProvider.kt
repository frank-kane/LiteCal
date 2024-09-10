package com.example.simplecaloriecounter2


import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class MyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            // Update the widget UI
            updateWidgetUI(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidgetUI(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Get the total from SharedPreferences
        val prefs = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val total = prefs.getInt("total", 0)

        // Update the total display
        views.setTextViewText(R.id.widgetTotal, "Total: $total")

        // Set up the RemoteViewsService for the ListView
        val intent = Intent(context, WidgetService::class.java)
        views.setRemoteAdapter(R.id.widgetListView, intent)

        // Create a PendingIntent template to handle clicks on individual items in the ListView
        val clickIntent = Intent(context, MyWidgetProvider::class.java)
        clickIntent.action = "com.example.simplecaloriecounter2.ACTION_ITEM_CLICK"
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setPendingIntentTemplate(R.id.widgetListView, pendingIntent)

        // Notify the widget manager to refresh the data in the ListView
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widgetListView)

        // Update the widget
        appWidgetManager.updateAppWidget(widgetId, views)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "com.example.simplecaloriecounter2.ACTION_ITEM_CLICK") {
            val itemName = intent.getStringExtra("item_name")
            val activityIntent = Intent(context, MainActivity::class.java)
            activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(activityIntent)
        }
    }

}
