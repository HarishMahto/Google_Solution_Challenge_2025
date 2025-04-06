package harish.project.maps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends AppCompatActivity {
  private FirebaseAuth mAuth;
  private TextView emailText;
  private TextView nameText;
  private Button logoutButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_account);

    // Initialize Firebase Auth
    mAuth = FirebaseAuth.getInstance();

    // Initialize views
    emailText = findViewById(R.id.emailText);
    nameText = findViewById(R.id.nameText);
    logoutButton = findViewById(R.id.logoutButton);
    ImageButton backButton = findViewById(R.id.backButton);

    // Setup back button
    backButton.setOnClickListener(v -> finish());

    // Setup logout button
    logoutButton.setOnClickListener(v -> {
      // Sign out from Firebase
      mAuth.signOut();

      // Clear any saved preferences
      getSharedPreferences("SettingsPrefs", MODE_PRIVATE).edit().clear().apply();

      // Exit the app
      finishAffinity();
      System.exit(0);
    });

    // Load user data
    loadUserData();
  }

  private void loadUserData() {
    FirebaseUser user = mAuth.getCurrentUser();
    if (user != null) {
      emailText.setText(user.getEmail());
      nameText.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
    }
  }
}