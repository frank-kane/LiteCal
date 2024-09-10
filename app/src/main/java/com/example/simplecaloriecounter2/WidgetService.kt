package com.example.simplecaloriecounter2

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ItemRemoteViewsFactory(applicationContext)
    }
}

class ItemRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var items: List<Item> = listOf()

    override fun onCreate() {
        items = loadItemsFromPreferences(context)
    }

    override fun onDataSetChanged() {
        items = loadItemsFromPreferences(context)
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_item)

        if (items.isEmpty()) {
            // Handle the empty case
            views.setTextViewText(R.id.widgetItemName, "No items")
            views.setTextViewText(R.id.widgetItemTotal, "")
        } else {
            val item = items[position]
            views.setTextViewText(R.id.widgetItemName, item.name)
            views.setTextViewText(R.id.widgetItemTotal, item.total.toString())

            // Set up the click action for this item
            val fillInIntent = Intent()
            fillInIntent.putExtra("item_name", item.name)  // Pass item data if needed
            views.setOnClickFillInIntent(R.id.widgetItemName, fillInIntent)
        }

        return views
    }


    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun onDestroy() {
        items = emptyList()
    }

    private fun loadItemsFromPreferences(context: Context): List<Item> {
        val prefs = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val itemCount = prefs.getInt("itemCount", 0)

        val items = mutableListOf<Item>()
        for (i in 0 until itemCount) {
            val itemName = prefs.getString("item_${i}_name", "Item")
            val itemTotal = prefs.getInt("item_${i}_total", 0)
            if (itemName != null) {
                items.add(Item(itemName, itemTotal))
            }
        }
        return items
    }
}
