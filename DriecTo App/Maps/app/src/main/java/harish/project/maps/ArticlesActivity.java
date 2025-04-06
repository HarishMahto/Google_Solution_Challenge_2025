package harish.project.maps;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ArticlesActivity extends AppCompatActivity {
  private RecyclerView articlesRecyclerView;
  private ArticlesAdapter articlesAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_articles);

    // Initialize views
    articlesRecyclerView = findViewById(R.id.articlesRecyclerView);
    ImageButton backButton = findViewById(R.id.backButton);

    // Set up back button
    backButton.setOnClickListener(v -> finish());

    // Set up RecyclerView
    articlesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    articlesAdapter = new ArticlesAdapter(getSampleArticles());
    articlesRecyclerView.setAdapter(articlesAdapter);
  }

  private List<Article> getSampleArticles() {
    List<Article> articles = new ArrayList<>();
    articles.add(new Article(
        "Major Traffic Jam on Main Street",
        "Due to ongoing construction work, expect delays of up to 30 minutes on Main Street.",
        "2 hours ago"));
    articles.add(new Article(
        "New Traffic Light Installation",
        "Traffic lights being installed at the intersection of 5th Avenue and Park Street.",
        "4 hours ago"));
    articles.add(new Article(
        "Road Closure Alert",
        "Bridge Street will be closed for maintenance from 10 PM to 6 AM.",
        "6 hours ago"));
    articles.add(new Article(
        "Traffic Pattern Changes",
        "New one-way traffic pattern implemented in downtown area.",
        "Yesterday"));
    return articles;
  }

  public static class Article {
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
}