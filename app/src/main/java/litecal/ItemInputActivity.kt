package litecal.app
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


class ItemInputActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_input)

        val itemNameInput: EditText = findViewById(R.id.itemNameInput)
        val itemTotalInput: EditText = findViewById(R.id.itemTotalInput)
        val saveButton: Button = findViewById(R.id.saveItemButton)

        saveButton.setOnClickListener {
            val itemName = itemNameInput.text.toString()
            val itemTotal = itemTotalInput.text.toString().toIntOrNull() ?: 0

            // Save the item to SharedPreferences (you can also save a list if necessary)
            val prefs = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
            val total = prefs.getInt("total", 0) + itemTotal
            val itemCount = prefs.getInt("itemCount", 0) + 1
            prefs.edit().putInt("total", total).putInt("itemCount", itemCount).apply()

            // Update widget after adding item
            updateWidget()

            // Close the activity
            finish()
        }
    }

    private fun updateWidget() {
        val widgetManager = AppWidgetManager.getInstance(this)
        val widgetComponent = ComponentName(this, MyWidgetProvider::class.java)
        val widgetIds = widgetManager.getAppWidgetIds(widgetComponent)
        val intent = Intent(this, MyWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        sendBroadcast(intent)
    }
}
