package harish.project.maps.services;

import androidx.annotation.NonNull;
import com.google.firebase.database.*;
import harish.project.maps.models.TrafficJunction;
import java.util.ArrayList;
import java.util.List;

public class FirebaseService {
  private final DatabaseReference database;
  private final List<TrafficDataListener> listeners;

  public interface TrafficDataListener {
    void onTrafficDataUpdated(List<TrafficJunction> junctions);

    void onEmergencyVehicleDetected(TrafficJunction junction);
  }

  public FirebaseService() {
    database = FirebaseDatabase.getInstance().getReference("traffic_junctions");
    listeners = new ArrayList<>();
  }

  public void addTrafficDataListener(TrafficDataListener listener) {
    listeners.add(listener);
    if (listeners.size() == 1) {
      startListening();
    }
  }

  public void removeTrafficDataListener(TrafficDataListener listener) {
    listeners.remove(listener);
    if (listeners.isEmpty()) {
      stopListening();
    }
  }

  private void startListening() {
    database.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        List<TrafficJunction> junctions = new ArrayList<>();
        for (DataSnapshot junctionSnapshot : snapshot.getChildren()) {
          TrafficJunction junction = junctionSnapshot.getValue(TrafficJunction.class);
          if (junction != null) {
            junctions.add(junction);
            if (junction.isEmergencyVehiclePresent()) {
              notifyEmergencyVehicle(junction);
            }
          }
        }
        notifyDataUpdate(junctions);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        // Handle error
      }
    });
  }

  private void stopListening() {
    database.removeEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
      }
    });
  }

  private void notifyDataUpdate(List<TrafficJunction> junctions) {
    for (TrafficDataListener listener : listeners) {
      listener.onTrafficDataUpdated(junctions);
    }
  }

  private void notifyEmergencyVehicle(TrafficJunction junction) {
    for (TrafficDataListener listener : listeners) {
      listener.onEmergencyVehicleDetected(junction);
    }
  }

  public void logTrafficHistory(TrafficJunction junction) {
    DatabaseReference historyRef = FirebaseDatabase.getInstance()
        .getReference("traffic_history")
        .child(junction.getJunctionId())
        .child(String.valueOf(junction.getTimestamp()));
    historyRef.setValue(junction);
  }
}