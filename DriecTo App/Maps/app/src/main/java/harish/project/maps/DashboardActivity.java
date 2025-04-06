package harish.project.maps;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
  private RecyclerView articlesRecyclerView;
  private ArticlesAdapter articlesAdapter;
  private BottomNavigationView bottomNav;
  private FloatingActionButton sosButton;
  private Vibrator vibrator;
  private VibratorManager vibratorManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dashboard);

    // Initialize views
    articlesRecyclerView = findViewById(R.id.articlesRecyclerView);
    bottomNav = findViewById(R.id.bottom_navigation);
    sosButton = findViewById(R.id.sosButton);
    ImageButton notificationButton = findViewById(R.id.notificationButton);

    // Initialize vibrator based on Android version
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      vibratorManager = (VibratorManager) getSystemService(VIBRATOR_MANAGER_SERVICE);
      vibrator = vibratorManager.getDefaultVibrator();
    } else {
      vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    // Setup articles RecyclerView
    articlesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    articlesAdapter = new ArticlesAdapter(getSampleArticles());
    articlesRecyclerView.setAdapter(articlesAdapter);

    // Setup notification button
    notificationButton.setOnClickListener(v -> {
      // TODO: Implement notification center
      Toast.makeText(this, "Notifications coming soon!", Toast.LENGTH_SHORT).show();
    });

    // Setup bottom navigation
    bottomNav.setOnItemSelectedListener(item -> {
      int itemId = item.getItemId();
      if (itemId == R.id.nav_home) {
        // Already on home
        return true;
      } else if (itemId == R.id.nav_account) {
        startActivity(new Intent(this, AccountActivity.class));
        return true;
      } else if (itemId == R.id.navigation_voice) {
        startActivity(new Intent(this, GeminiChatActivity.class));
        return true;
      } else if (itemId == R.id.nav_help) {
        startActivity(new Intent(this, HelpActivity.class));
        return true;
      }
      return false;
    });

    // Setup SOS button
    sosButton.setOnClickListener(v -> {
      // Vibrate
      if (vibrator != null && vibrator.hasVibrator()) {
        try {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
          } else {
            vibrator.vibrate(500);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      // Pulse animation
      Animation pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse);
      sosButton.startAnimation(pulseAnimation);

      // Handle emergency
      handleEmergency();
    });

    // Setup feature cards and click listeners
    setupFeatureCards();
  }

  private void setupFeatureCards() {
    // Live Traffic Card
    CardView liveTrafficCard = findViewById(R.id.liveTrafficCard);
    liveTrafficCard.setOnClickListener(v -> {
      startActivity(new Intent(this, MainActivity.class));
    });

    // Analytics Card
    CardView analyticsCard = findViewById(R.id.trafficAnalyticsCard);
    analyticsCard.setOnClickListener(v -> {
      startActivity(new Intent(this, AnalyticsActivity.class));
    });

    // Settings Card
    CardView settingsCard = findViewById(R.id.settingsCard);
    settingsCard.setOnClickListener(v -> {
      startActivity(new Intent(this, SettingsActivity.class));
    });

    // See More Text
    TextView seeMoreText = findViewById(R.id.seeMoreText);
    seeMoreText.setOnClickListener(v -> {
      startActivity(new Intent(this, ArticlesActivity.class));
    });

    /*
     * Traffic Update Card
     * CardView trafficUpdateCard = findViewById(R.id.trafficUpdateCard);
     * trafficUpdateCard.setOnClickListener(v -> {
     * startActivity(new Intent(this, ArticlesActivity.class));
     * });
     */
  }

  private void handleEmergency() {
    // TODO: Implement emergency handling
    Toast.makeText(this, "Emergency SOS triggered!", Toast.LENGTH_SHORT).show();
  }

  private List<Article> getSampleArticles() {
    List<Article> articles = new ArrayList<>();
    articles.add(new Article(
        "Traffic Alert: Major Road Closure",
        "Due to ongoing construction work, Main Street will be closed for the next 3 days.",
        "2 hours ago"));
    articles.add(new Article(
        "Weather Warning: Heavy Rain Expected",
        "Heavy rainfall is expected in the next 24 hours. Please plan your journey accordingly.",
        "5 hours ago"));
    return articles;
  }

  // Article class for RecyclerView items
  private static class Article {
    private String title;
    private String description;
    private String time;

    public Article(String title, String description, String time) {
      this.title = title;
      this.description = description;
      this.time = time;
    }

    public String getTitle() {
      return title;
    }

    public String getDescription() {
      return description;
    }

    public String getTime() {
      return time;
    }
  }

  // ArticlesAdapter class for RecyclerView
  private static class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder> {
    private List<Article> articles;

    public ArticlesAdapter(List<Article> articles) {
      this.articles = articles;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_article, parent, false);
      return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
      Article article = articles.get(position);
      holder.titleTextView.setText(article.getTitle());
      holder.descriptionTextView.setText(article.getDescription());
      holder.timeTextView.setText(article.getTime());
    }

    @Override
    public int getItemCount() {
      return articles.size();
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
      TextView titleTextView;
      TextView descriptionTextView;
      TextView timeTextView;

      public ArticleViewHolder(@NonNull View itemView) {
        super(itemView);
        titleTextView = itemView.findViewById(R.id.articleTitle);
        descriptionTextView = itemView.findViewById(R.id.articleDescription);
        timeTextView = itemView.findViewById(R.id.articleTime);
      }
    }
  }
}