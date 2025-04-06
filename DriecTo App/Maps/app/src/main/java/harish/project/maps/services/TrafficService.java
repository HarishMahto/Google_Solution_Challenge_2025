package harish.project.maps.services;

import com.google.android.gms.maps.model.LatLng;
import harish.project.maps.models.TrafficJunction;
import java.util.ArrayList;
import java.util.List;

public class TrafficService {
  private static final double TRAFFIC_RADIUS = 100.0; // meters
  private static final int HEAVY_TRAFFIC_THRESHOLD = 60;
  private static final int MODERATE_TRAFFIC_THRESHOLD = 30;

  public interface TrafficCallback {
    void onTrafficUpdate(List<TrafficJunction> junctions);

    void onError(String error);
  }

  public void analyzeTrafficDensity(List<TrafficJunction> junctions, LatLng location, TrafficCallback callback) {
    try {
      List<TrafficJunction> nearbyJunctions = new ArrayList<>();

      for (TrafficJunction junction : junctions) {
        double distance = calculateDistance(
            location.latitude, location.longitude,
            junction.getLatitude(), junction.getLongitude());

        if (distance <= TRAFFIC_RADIUS) {
          nearbyJunctions.add(junction);
        }
      }

      callback.onTrafficUpdate(nearbyJunctions);
    } catch (Exception e) {
      callback.onError("Error analyzing traffic: " + e.getMessage());
    }
  }

  public String getTrafficStatus(int density) {
    if (density >= HEAVY_TRAFFIC_THRESHOLD) {
      return "Heavy Traffic";
    } else if (density >= MODERATE_TRAFFIC_THRESHOLD) {
      return "Moderate Traffic";
    } else {
      return "Light Traffic";
    }
  }

  public int getTrafficColor(int density) {
    if (density >= HEAVY_TRAFFIC_THRESHOLD) {
      return 0x80FF0000; // Red with 50% opacity
    } else if (density >= MODERATE_TRAFFIC_THRESHOLD) {
      return 0x80FFA500; // Orange with 50% opacity
    } else {
      return 0x8000FF00; // Green with 50% opacity
    }
  }

  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    double R = 6371e3; // Earth's radius in meters
    double φ1 = Math.toRadians(lat1);
    double φ2 = Math.toRadians(lat2);
    double Δφ = Math.toRadians(lat2 - lat1);
    double Δλ = Math.toRadians(lon2 - lon1);

    double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
        Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c; // Distance in meters
  }
}