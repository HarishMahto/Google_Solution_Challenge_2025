package harish.project.maps;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {
  private ImageButton backButton;
  private View faq;
  private View contactSupport;
  private View reportIssue;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_help);

    // Initialize views
    backButton = findViewById(R.id.backButton);
    faq = findViewById(R.id.faq);
    contactSupport = findViewById(R.id.contactSupport);
    reportIssue = findViewById(R.id.reportIssue);

    // Set up back button
    backButton.setOnClickListener(v -> finish());

    // Set up click listeners
    faq.setOnClickListener(v -> {
      // TODO: Implement FAQ
      Toast.makeText(this, "FAQ Coming Soon", Toast.LENGTH_SHORT).show();
    });

    contactSupport.setOnClickListener(v -> {
      // TODO: Implement contact support
      Toast.makeText(this, "Contact Support Coming Soon", Toast.LENGTH_SHORT).show();
    });

    reportIssue.setOnClickListener(v -> {
      // TODO: Implement report issue
      Toast.makeText(this, "Report Issue Coming Soon", Toast.LENGTH_SHORT).show();
    });
  }
}