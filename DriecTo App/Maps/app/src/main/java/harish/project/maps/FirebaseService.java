package harish.project.maps;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.github.mikephil.charting.data.Entry;
import harish.project.maps.models.TrafficJunction;
import java.util.ArrayList;
import java.util.List;

public class FirebaseService {
  private static FirebaseService instance;
  private final FirebaseDatabase database;

  private FirebaseService() {
    database = FirebaseDatabase.getInstance();
  }

  public static FirebaseService getInstance() {
    if (instance == null) {
      instance = new FirebaseService();
    }
    return instance;
  }

  public interface TrafficDataListener {
    void onDataLoaded(List<Entry> entries);

    void onCancelled(DatabaseError error);
  }

  public void getTrafficData(TrafficDataListener listener) {
    database.getReference("traffic_history")
        .addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot snapshot) {
            List<Entry> entries = new ArrayList<>();
            int count = 0;

            for (DataSnapshot junctionSnapshot : snapshot.getChildren()) {
              for (DataSnapshot timeSnapshot : junctionSnapshot.getChildren()) {
                TrafficJunction junction = timeSnapshot.getValue(TrafficJunction.class);
                if (junction != null) {
                  entries.add(new Entry(count++, junction.getVehicleDensity()));
                }
              }
            }

            listener.onDataLoaded(entries);
          }

          @Override
          public void onCancelled(DatabaseError error) {
            listener.onCancelled(error);
          }
        });
  }
}