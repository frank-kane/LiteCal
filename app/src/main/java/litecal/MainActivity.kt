package litecal.app

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RemoteViews
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


data class Item(val name: String, val total: Int)

class MainActivity : AppCompatActivity() {

    private lateinit var myTotal: TextView
    private lateinit var itemNameInput: EditText
    private lateinit var itemTotalInput: EditText
    private lateinit var addItemButton: Button
    private lateinit var clearButton: Button
    private lateinit var itemList: RecyclerView
    private lateinit var adapter: MyAdapter
    private lateinit var quantityButton: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuButton: ImageButton
    private lateinit var themesPageButton: Button
    private var currentQuantity = 1
    private var myDataset = mutableListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        myTotal = findViewById(R.id.totalValue)
        itemNameInput = findViewById(R.id.itemNameInput)
        itemTotalInput = findViewById(R.id.itemTotalInput)
        addItemButton = findViewById(R.id.addItemButton)
        clearButton = findViewById(R.id.clearButton)
        itemList = findViewById(R.id.itemList)
        itemList.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        quantityButton = findViewById(R.id.quantityButton)
//        drawerLayout = findViewById(R.id.drawer_layout)  // Initialize drawer layout
//        menuButton = findViewById(R.id.menuButton)  // Initialize the menu button
//        themesPageButton = findViewById(R.id.ThemesPageButton)
//        // Set up the menu button to open and close the drawer
//        menuButton.setOnClickListener {
//            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//                drawerLayout.closeDrawer(GravityCompat.START) // Close drawer if it's open
//            } else {
//                drawerLayout.openDrawer(GravityCompat.START) // Open the drawer
//            }
//        }

        // Load saved items from SharedPreferences
        myDataset = loadItemsFromPreferences(this)
        Log.d("MainActivity", "Loaded items: $myDataset")
        adapter = MyAdapter(myDataset) {
            updateTotalValue()
        }

        itemList.layoutManager = LinearLayoutManager(this)
        itemList.adapter = adapter

        // Add item button logic
        addItemButton.setOnClickListener {
            val itemName = itemNameInput.text.toString()
            val itemTotal = itemTotalInput.text.toString().toIntOrNull() ?: 0

            // Set up the item animator for add animations
            val animator = object : androidx.recyclerview.widget.DefaultItemAnimator() {
                init {
                    addDuration = 500 // Set the duration for adding animation (500ms)
                }
            }
            itemList.itemAnimator = animator

            val totalForItem = itemTotal * currentQuantity
            val itemNameWithQuantity = if (currentQuantity > 1) {
                "$itemName x$currentQuantity"
            } else {
                itemName // No need to add "x1" if the quantity is 1
            }

            // Create new item and add it to the dataset
            val newItem = Item(itemNameWithQuantity, totalForItem)
            myDataset.add(newItem)

            // Update the last added position to animate the new item
            adapter.lastAddedPosition = myDataset.size - 1

            // Notify the adapter that the new item has been added
            adapter.notifyItemInserted(myDataset.size - 1)

            // Save the items to SharedPreferences
            saveItemsToPreferences(this, myDataset)

            // Notify the widget that the data has changed
            notifyWidgetDataChanged(this)

            // **Make sure this is called!**
            updateTotalValue()

            // Clear input fields and reset the quantity
            itemNameInput.text.clear()
            itemTotalInput.text.clear()
            currentQuantity = 1  // Reset quantity to 1 after adding the item
            quantityButton.text = "Qty: $currentQuantity" // Reset the button text
        }

//        themesPageButton.setOnClickListener {
//            val intent = Intent(this, ThemeActivity::class.java)
//            startActivity(intent)
//        }


        // Clear button logic
        clearButton.setOnClickListener {
            // Clear the dataset and reset total to 0
            myDataset.clear()
            adapter.notifyDataSetChanged()
            saveItemsToPreferences(this, myDataset)  // Save the cleared list

            // Notify the widget that the data has changed
            notifyWidgetDataChanged(this)

            updateTotalValue()  // Reset the total
        }

        quantityButton.setOnClickListener {
            currentQuantity++
            quantityButton.text = "Qty: $currentQuantity" // Update the button text to reflect the new quantity
        }

        quantityButton.setOnLongClickListener {
            currentQuantity = 1
            quantityButton.text = "Qty: $currentQuantity" // Update the button text to show the reset quantity
            true // Return true to indicate the long click was handled
        }




        itemTotalInput.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                // Check if the action is the 'Done' or 'Enter' action from the keyboard
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                    // Check if both fields are filled before calling the add item function
                    val itemName = itemNameInput.text.toString().trim()
                    val itemTotal = itemTotalInput.text.toString().trim()

                    if (itemName.isNotEmpty() && itemTotal.isNotEmpty()) {
                        addItemButton.performClick() // Simulate the button click
                    }

                    return true // Return true to indicate the action has been handled
                }
                return false
            }
        })

        updateTotalValue()
    }

    // Function to calculate and update the total value
    private fun updateTotalValue() {
        val totalSum = myDataset.sumOf { it.total }
        myTotal.text = "Total: $totalSum"

        // Save the updated total to SharedPreferences
        val prefs = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt("total", totalSum)
        editor.apply()

        // Force the widget to update with the new total
        val widgetManager = AppWidgetManager.getInstance(this)
        val widgetComponent = ComponentName(this, MyWidgetProvider::class.java)
        val widgetIds = widgetManager.getAppWidgetIds(widgetComponent)

        // Explicitly update each widget UI with the new total
        widgetIds.forEach { widgetId ->
            val remoteViews = RemoteViews(packageName, R.layout.widget_layout)

            // Update the total in the widget
            remoteViews.setTextViewText(R.id.widgetTotal, "Total: $totalSum")

            // Notify the widget to update
            widgetManager.updateAppWidget(widgetId, remoteViews)
        }
    }

    // Function to save items to SharedPreferences
    private fun saveItemsToPreferences(context: Context, items: List<Item>) {
        val prefs = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Save the number of items
        editor.putInt("itemCount", items.size)

        // Save each item's name and total
        for (i in items.indices) {
            editor.putString("item_${i}_name", items[i].name)
            editor.putInt("item_${i}_total", items[i].total)
        }

        editor.apply()
    }

    // Function to load items from SharedPreferences
    private fun loadItemsFromPreferences(context: Context): MutableList<Item> {
        val prefs = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val itemCount = prefs.getInt("itemCount", 0)
        val items = mutableListOf<Item>()

        // Retrieve each item from SharedPreferences
        for (i in 0 until itemCount) {
            val itemName = prefs.getString("item_${i}_name", "Item")
            val itemTotal = prefs.getInt("item_${i}_total", 0)
            if (itemName != null) {
                items.add(Item(itemName, itemTotal))
            }
        }

        return items
    }

    private fun notifyWidgetDataChanged(context: Context) {
        val widgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, MyWidgetProvider::class.java)
        val widgetIds = widgetManager.getAppWidgetIds(widgetComponent)
        widgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widgetListView)
    }
}
