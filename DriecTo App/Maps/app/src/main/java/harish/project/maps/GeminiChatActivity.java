package harish.project.maps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import java.util.ArrayList;
import java.util.List;

public class GeminiChatActivity extends AppCompatActivity {
  private RecyclerView chatRecyclerView;
  private ChatAdapter chatAdapter;
  private EditText messageInput;
  private ImageButton sendButton;
  private ImageButton micButton;
  private ImageButton backButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_gemini_chat);

    // Initialize views
    chatRecyclerView = findViewById(R.id.chatRecyclerView);
    messageInput = findViewById(R.id.messageInput);
    sendButton = findViewById(R.id.sendButton);
    micButton = findViewById(R.id.micButton);
    backButton = findViewById(R.id.backButton);

    // Setup RecyclerView
    chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    chatAdapter = new ChatAdapter(new ArrayList<>());
    chatRecyclerView.setAdapter(chatAdapter);

    // Setup click listeners
    backButton.setOnClickListener(v -> finish());

    sendButton.setOnClickListener(v -> {
      String message = messageInput.getText().toString().trim();
      if (!message.isEmpty()) {
        // Add user message
        chatAdapter.addMessage(new ChatMessage(message, true));
        messageInput.setText("");

        // Simulate AI response (replace with actual Gemini API call)
        simulateAIResponse(message);
      }
    });

    micButton.setOnClickListener(v -> {
      // TODO: Implement voice input
      Toast.makeText(this, "Voice input coming soon!", Toast.LENGTH_SHORT).show();
    });
  }

  private void simulateAIResponse(String userMessage) {
    // Simulate AI thinking
    chatAdapter.addMessage(new ChatMessage("Thinking...", false));

    // Simulate response after a delay
    new android.os.Handler().postDelayed(() -> {
      chatAdapter.removeLastMessage(); // Remove "Thinking..." message
      String response = generateResponse(userMessage);
      chatAdapter.addMessage(new ChatMessage(response, false));
    }, 1000);
  }

  private String generateResponse(String userMessage) {
    // Simple response generation (replace with actual Gemini API)
    if (userMessage.toLowerCase().contains("traffic")) {
      return "I can help you with traffic information. Would you like to know about current traffic conditions or get directions?";
    } else if (userMessage.toLowerCase().contains("help")) {
      return "I'm here to help! I can assist you with traffic information, directions, and general navigation queries.";
    } else {
      return "I'm your AI assistant. How can I help you with traffic and navigation today?";
    }
  }

  // ChatMessage class
  private static class ChatMessage {
    private String message;
    private boolean isUser;

    public ChatMessage(String message, boolean isUser) {
      this.message = message;
      this.isUser = isUser;
    }

    public String getMessage() {
      return message;
    }

    public boolean isUser() {
      return isUser;
    }
  }

  // ChatAdapter class
  private static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
      this.messages = messages;
    }

    public void addMessage(ChatMessage message) {
      messages.add(message);
      notifyItemInserted(messages.size() - 1);
    }

    public void removeLastMessage() {
      if (!messages.isEmpty()) {
        messages.remove(messages.size() - 1);
        notifyItemRemoved(messages.size());
      }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_chat_message, parent, false);
      return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
      ChatMessage message = messages.get(position);
      holder.messageText.setText(message.getMessage());

      // Set layout based on whether it's a user or AI message
      if (message.isUser()) {
        holder.messageText.setBackgroundResource(R.drawable.user_message_background);
        holder.messageText.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.messageText.getLayoutParams();
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        params.startToStart = ConstraintLayout.LayoutParams.UNSET;
        holder.messageText.setLayoutParams(params);
      } else {
        holder.messageText.setBackgroundResource(R.drawable.ai_message_background);
        holder.messageText.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.messageText.getLayoutParams();
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.UNSET;
        holder.messageText.setLayoutParams(params);
      }
    }

    @Override
    public int getItemCount() {
      return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
      TextView messageText;

      public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        messageText = itemView.findViewById(R.id.messageText);
      }
    }
  }
}