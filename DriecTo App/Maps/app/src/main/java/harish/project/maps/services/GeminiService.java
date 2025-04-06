package harish.project.maps.services;

import harish.project.maps.BuildConfig;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import harish.project.maps.models.TrafficJunction;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.concurrent.CompletableFuture;



public class GeminiService {
  private final GenerativeModel model;
  private static final String API_KEY = BuildConfig.GEMINI_API_KEY;


  public interface GeminiCallback {
    void onSuccess(String response);

    void onError(String error);
  }

  public GeminiService() {
    model = new GenerativeModel("gemini-pro", API_KEY);
  }

  public void analyzeTrafficPattern(List<TrafficJunction> junctions, GeminiCallback callback) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Analyze the following traffic data and suggest the best route:\n");

    for (TrafficJunction junction : junctions) {
      prompt.append(String.format(
          "Junction %s: Density=%d, GreenLight=%ds, Emergency=%b\n",
          junction.getJunctionId(),
          junction.getVehicleDensity(),
          junction.getGreenLightDuration(),
          junction.isEmergencyVehiclePresent()));
    }

    generateContent(prompt.toString(), callback);
  }

  public void predictFutureTraffic(List<TrafficJunction> historicalData, GeminiCallback callback) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Based on the following historical traffic data, predict traffic conditions for the next hour:\n");

    for (TrafficJunction data : historicalData) {
      prompt.append(String.format(
          "Time: %d, Junction %s: Density=%d\n",
          data.getTimestamp(),
          data.getJunctionId(),
          data.getVehicleDensity()));
    }

    generateContent(prompt.toString(), callback);
  }

  public void generateVoiceAlert(TrafficJunction junction, GeminiCallback callback) {
    String prompt = String.format(
        "Generate a concise voice alert for the following traffic condition: " +
            "Junction %s has %d%% congestion. %s",
        junction.getJunctionId(),
        junction.getVehicleDensity(),
        junction.isEmergencyVehiclePresent() ? "Emergency vehicle detected!" : "");

    generateContent(prompt, callback);
  }

  private void generateContent(String prompt, GeminiCallback callback) {
    model.generateContent(prompt, new Continuation<GenerateContentResponse>() {
      @NotNull
      @Override
      public CoroutineContext getContext() {
        return EmptyCoroutineContext.INSTANCE;
      }

      @Override
      public void resumeWith(@NotNull Object result) {
        try {
          if (result instanceof GenerateContentResponse) {
            GenerateContentResponse response = (GenerateContentResponse) result;
            callback.onSuccess(response.getText());
          } else {
            callback.onError("Error generating content: Unexpected response type");
          }
        } catch (Exception e) {
          callback.onError("Error generating content: " + e.getMessage());
        }
      }
    });
  }
}