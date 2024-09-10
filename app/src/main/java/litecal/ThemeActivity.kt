package litecal.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class ThemeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)

        val themeLightButton: Button = findViewById(R.id.themeLightButton)
        val themeDarkButton: Button = findViewById(R.id.themeDarkButton)

        themeLightButton.setOnClickListener {
            setTheme(R.style.Theme_SimpleCalorieCounter2_Light) // Apply light theme
            saveTheme("light")
        }

        themeDarkButton.setOnClickListener {
            setTheme(R.style.Theme_SimpleCalorieCounter2_Dark) // Apply dark theme
            saveTheme("dark")
        }
    }

    private fun saveTheme(theme: String) {
        val prefs = getSharedPreferences("myAppPrefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("theme", theme)
        editor.apply()

        // Restart the MainActivity to apply the new theme
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
