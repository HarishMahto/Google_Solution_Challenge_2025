package harish.project.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import harish.project.maps.models.TrafficJunction;
import harish.project.maps.services.FirebaseService;
import harish.project.maps.services.GeminiService;
import harish.project.maps.services.TrafficService;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        FirebaseService.TrafficDataListener, TextToSpeech.OnInitListener {

    private GoogleMap mMap;
    private FirebaseService firebaseService;
    private GeminiService geminiService;
    private TextToSpeech textToSpeech;
    private Map<String, Circle> trafficMarkers;
    private Marker sourceMarker;
    private Marker destinationMarker;
    private EditText sourceInput;
    private EditText destinationInput;
    private Button findRouteButton;
    private GeoApiContext geoApiContext;
    private ExecutorService executorService;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private TrafficService trafficService;
    private Polyline routePolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize services
        firebaseService = new FirebaseService();
        geminiService = new GeminiService();
        trafficMarkers = new HashMap<>();
        textToSpeech = new TextToSpeech(this, this);
        executorService = Executors.newSingleThreadExecutor();
        trafficService = new TrafficService();

        // Initialize GeoApiContext for Directions API
        geoApiContext = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build();

        // Initialize views
        sourceInput = findViewById(R.id.sourceInput);
        destinationInput = findViewById(R.id.destinationInput);
        findRouteButton = findViewById(R.id.findRouteButton);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up route finding button
        findRouteButton.setOnClickListener(v -> findRoute());

        checkLocationPermission();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Set default location (e.g., city center)
        LatLng defaultLocation = new LatLng(12.9716, 77.5946); // Bangalore coordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));

        // Enable location if permission is granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Set up map click listeners for source and destination
        mMap.setOnMapClickListener(latLng -> {
            if (sourceMarker == null) {
                addSourceMarker(latLng);
            } else if (destinationMarker == null) {
                addDestinationMarker(latLng);
            }
        });

        // Start listening to traffic updates
        startTrafficUpdates();
    }

    @Override
    public void onTrafficDataUpdated(List<TrafficJunction> junctions) {
        runOnUiThread(() -> {
            updateTrafficMarkers(junctions);
            analyzeAndPredictTraffic(junctions);
        });
    }

    @Override
    public void onEmergencyVehicleDetected(TrafficJunction junction) {
        runOnUiThread(() -> {
            // Update map marker for emergency vehicle
            Circle circle = trafficMarkers.get(junction.getJunctionId());
            if (circle != null) {
                circle.setFillColor(Color.RED);
            }

            // Generate and speak emergency alert
            geminiService.generateVoiceAlert(junction, new GeminiService.GeminiCallback() {
                @Override
                public void onSuccess(String alert) {
                    runOnUiThread(() -> textToSpeech.speak(alert, TextToSpeech.QUEUE_FLUSH, null, null));
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show());
                }
            });
        });
    }

    private void updateTrafficMarkers(List<TrafficJunction> junctions) {
        for (TrafficJunction junction : junctions) {
            Circle circle = trafficMarkers.get(junction.getJunctionId());
            LatLng position = new LatLng(junction.getLatitude(), junction.getLongitude());

            if (circle == null) {
                // Create new marker
                CircleOptions circleOptions = new CircleOptions()
                        .center(position)
                        .radius(100) // meters
                        .strokeWidth(2)
                        .strokeColor(Color.BLACK);
                circle = mMap.addCircle(circleOptions);
                trafficMarkers.put(junction.getJunctionId(), circle);
            }

            // Update circle color based on traffic density using TrafficService
            int color = trafficService.getTrafficColor(junction.getVehicleDensity());
            circle.setFillColor(color);
        }
    }

    private void analyzeAndPredictTraffic(List<TrafficJunction> junctions) {
        // Analyze traffic pattern
        geminiService.analyzeTrafficPattern(junctions, new GeminiService.GeminiCallback() {
            @Override
            public void onSuccess(String analysis) {
                runOnUiThread(() -> {
                    TextView statusText = findViewById(R.id.trafficStatusText);
                    statusText.setText(analysis);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Analysis Error: " + error, Toast.LENGTH_SHORT).show());
            }
        });

        // Predict future traffic
        geminiService.predictFutureTraffic(junctions, new GeminiService.GeminiCallback() {
            @Override
            public void onSuccess(String prediction) {
                runOnUiThread(() -> {
                    TextView predictionText = findViewById(R.id.predictionText);
                    predictionText.setText(prediction);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Prediction Error: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                }
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.US);
        }
    }

    private void addSourceMarker(LatLng position) {
        if (sourceMarker != null) {
            sourceMarker.remove();
        }
        sourceMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Source")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        sourceInput.setText(String.format("%f, %f", position.latitude, position.longitude));
    }

    private void addDestinationMarker(LatLng position) {
        if (destinationMarker != null) {
            destinationMarker.remove();
        }
        destinationMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        destinationInput.setText(String.format("%f, %f", position.latitude, position.longitude));
    }

    private void findRoute() {
        if (sourceMarker == null || destinationMarker == null) {
            Toast.makeText(this, "Please select both source and destination", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear existing route if any
        if (routePolyline != null) {
            routePolyline.remove();
        }

        // Get source and destination coordinates
        LatLng source = sourceMarker.getPosition();
        LatLng destination = destinationMarker.getPosition();

        // Create route options
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(source)
                .add(destination)
                .width(10)
                .color(Color.BLUE)
                .geodesic(true);

        // Add the route to the map
        routePolyline = mMap.addPolyline(polylineOptions);

        // Move camera to show the entire route
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(source);
        builder.include(destination);
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

        // Update input fields with coordinates
        sourceInput.setText(String.format("%f, %f", source.latitude, source.longitude));
        destinationInput.setText(String.format("%f, %f", destination.latitude, destination.longitude));
    }

    private void startTrafficUpdates() {
        try {
            FirebaseDatabase.getInstance().getReference("traffic_data")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                List<TrafficJunction> junctions = new ArrayList<>();
                                for (DataSnapshot junctionSnapshot : snapshot.getChildren()) {
                                    TrafficJunction junction = junctionSnapshot.getValue(TrafficJunction.class);
                                    if (junction != null) {
                                        junctions.add(junction);
                                    }
                                }
                                if (!junctions.isEmpty()) {
                                    updateTrafficMarkers(junctions);
                                    analyzeAndPredictTraffic(junctions);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this,
                                    "Error loading traffic data: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show());
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopTrafficUpdates() {
        try {
            // Clear all traffic markers
            for (Circle circle : trafficMarkers.values()) {
                if (circle != null) {
                    circle.remove();
                }
            }
            trafficMarkers.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            startTrafficUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTrafficUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        firebaseService.removeTrafficDataListener(this);
        executorService.shutdown();
        if (geoApiContext != null) {
            geoApiContext.shutdown();
        }
        stopTrafficUpdates();
    }
}