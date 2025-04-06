package harish.project.maps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder> {
  private List<ArticlesActivity.Article> articles;

  public ArticlesAdapter(List<ArticlesActivity.Article> articles) {
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
    ArticlesActivity.Article article = articles.get(position);
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

    ArticleViewHolder(View itemView) {
      super(itemView);
      titleTextView = itemView.findViewById(R.id.articleTitle);
      descriptionTextView = itemView.findViewById(R.id.articleDescription);
      timeTextView = itemView.findViewById(R.id.articleTime);
    }
  }
}