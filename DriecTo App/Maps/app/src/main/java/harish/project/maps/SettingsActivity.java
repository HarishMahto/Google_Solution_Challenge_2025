package harish.project.maps;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {
  private static final String PREFS_NAME = "SettingsPrefs";
  private static final String THEME_KEY = "theme";
  private static final String NOTIFICATIONS_KEY = "notifications";
  private static final String SOUND_KEY = "sound";
  private static final String VIBRATION_KEY = "vibration";

  private SharedPreferences preferences;
  private RadioGroup themeRadioGroup;
  private SwitchCompat notificationSwitch;
  private SwitchCompat soundSwitch;
  private SwitchCompat vibrationSwitch;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    // Initialize SharedPreferences
    preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

    // Initialize views
    ImageButton backButton = findViewById(R.id.backButton);
    themeRadioGroup = findViewById(R.id.themeRadioGroup);
    notificationSwitch = findViewById(R.id.notificationSwitch);
    soundSwitch = findViewById(R.id.soundSwitch);
    vibrationSwitch = findViewById(R.id.vibrationSwitch);

    // Setup back button
    backButton.setOnClickListener(v -> finish());

    // Load saved settings
    loadSettings();

    // Setup theme change listener
    themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
      int theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
      if (checkedId == R.id.lightTheme) {
        theme = AppCompatDelegate.MODE_NIGHT_NO;
      } else if (checkedId == R.id.darkTheme) {
        theme = AppCompatDelegate.MODE_NIGHT_YES;
      }
      AppCompatDelegate.setDefaultNightMode(theme);
      preferences.edit().putInt(THEME_KEY, theme).apply();
    });

    // Setup switch listeners
    notificationSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> preferences.edit().putBoolean(NOTIFICATIONS_KEY, isChecked).apply());

    soundSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> preferences.edit().putBoolean(SOUND_KEY, isChecked).apply());

    vibrationSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> preferences.edit().putBoolean(VIBRATION_KEY, isChecked).apply());
  }

  private void loadSettings() {
    // Load theme
    int savedTheme = preferences.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    int radioButtonId = R.id.systemTheme;
    if (savedTheme == AppCompatDelegate.MODE_NIGHT_NO) {
      radioButtonId = R.id.lightTheme;
    } else if (savedTheme == AppCompatDelegate.MODE_NIGHT_YES) {
      radioButtonId = R.id.darkTheme;
    }
    themeRadioGroup.check(radioButtonId);

    // Load other settings
    notificationSwitch.setChecked(preferences.getBoolean(NOTIFICATIONS_KEY, true));
    soundSwitch.setChecked(preferences.getBoolean(SOUND_KEY, true));
    vibrationSwitch.setChecked(preferences.getBoolean(VIBRATION_KEY, true));
  }
}