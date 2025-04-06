package harish.project.maps;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import harish.project.maps.models.TrafficJunction;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsActivity extends AppCompatActivity {
  private LineChart trafficTrendChart;
  private BarChart trafficDensityChart;
  private PieChart peakHoursChart;
  private TextView avgSpeedText;
  private TextView congestionText;
  private TextView totalVehiclesText;
  private TextView accidentRateText;
  private ProgressBar progressBar;
  private FirebaseService firebaseService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_analytics);

    // Initialize FirebaseService
    firebaseService = FirebaseService.getInstance();

    // Initialize views
    trafficTrendChart = findViewById(R.id.trafficTrendChart);
    trafficDensityChart = findViewById(R.id.trafficDensityChart);
    peakHoursChart = findViewById(R.id.peakHoursChart);
    avgSpeedText = findViewById(R.id.avgSpeedText);
    congestionText = findViewById(R.id.congestionText);
    totalVehiclesText = findViewById(R.id.totalVehiclesText);
    accidentRateText = findViewById(R.id.accidentRateText);
    progressBar = findViewById(R.id.progressBar);

    // Setup back button
    ImageButton backButton = findViewById(R.id.backButton);
    backButton.setOnClickListener(v -> finish());

    // Setup refresh button
    ImageButton refreshButton = findViewById(R.id.refreshButton);
    refreshButton.setOnClickListener(v -> refreshData());

    // Initialize charts
    setupTrafficTrendChart();
    setupTrafficDensityChart();
    setupPeakHoursChart();
    updateStatistics();

    // Load traffic data
    loadTrafficData();
  }

  private void setupTrafficTrendChart() {
    // Sample data for traffic trend
    List<Entry> entries = new ArrayList<>();
    entries.add(new Entry(0, 45));
    entries.add(new Entry(1, 40));
    entries.add(new Entry(2, 35));
    entries.add(new Entry(3, 30));
    entries.add(new Entry(4, 25));
    entries.add(new Entry(5, 20));
    entries.add(new Entry(6, 15));

    LineDataSet dataSet = new LineDataSet(entries, "Traffic Speed (km/h)");
    dataSet.setColor(Color.parseColor("#FF5722"));
    dataSet.setValueTextColor(Color.BLACK);
    dataSet.setLineWidth(2f);
    dataSet.setDrawCircles(true);
    dataSet.setCircleColor(Color.parseColor("#FF5722"));
    dataSet.setCircleRadius(4f);
    dataSet.setDrawValues(false);

    LineData lineData = new LineData(dataSet);
    trafficTrendChart.setData(lineData);

    // Customize chart appearance
    trafficTrendChart.getDescription().setEnabled(false);
    trafficTrendChart.getLegend().setEnabled(false);
    trafficTrendChart.setTouchEnabled(true);
    trafficTrendChart.setDragEnabled(true);
    trafficTrendChart.setScaleEnabled(true);
    trafficTrendChart.setPinchZoom(true);

    // Customize X axis
    XAxis xAxis = trafficTrendChart.getXAxis();
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setDrawGridLines(false);
    xAxis.setValueFormatter(
        new IndexAxisValueFormatter(new String[] { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" }));

    // Customize Y axis
    YAxis leftAxis = trafficTrendChart.getAxisLeft();
    leftAxis.setDrawGridLines(true);
    leftAxis.setAxisMinimum(0f);
    leftAxis.setAxisMaximum(50f);

    trafficTrendChart.getAxisRight().setEnabled(false);
    trafficTrendChart.invalidate();
  }

  private void setupTrafficDensityChart() {
    // Sample data for traffic density
    List<BarEntry> entries = new ArrayList<>();
    entries.add(new BarEntry(0, 1200));
    entries.add(new BarEntry(1, 1500));
    entries.add(new BarEntry(2, 1800));
    entries.add(new BarEntry(3, 2000));
    entries.add(new BarEntry(4, 2200));

    BarDataSet dataSet = new BarDataSet(entries, "Vehicle Count");
    dataSet.setColor(Color.parseColor("#FF5722"));
    dataSet.setValueTextColor(Color.BLACK);
    dataSet.setValueFormatter(new ValueFormatter() {
      @Override
      public String getFormattedValue(float value) {
        return String.valueOf((int) value);
      }
    });

    BarData barData = new BarData(dataSet);
    trafficDensityChart.setData(barData);

    // Customize chart appearance
    trafficDensityChart.getDescription().setEnabled(false);
    trafficDensityChart.getLegend().setEnabled(false);
    trafficDensityChart.setTouchEnabled(true);
    trafficDensityChart.setDragEnabled(true);
    trafficDensityChart.setScaleEnabled(true);
    trafficDensityChart.setPinchZoom(true);

    // Customize X axis
    XAxis xAxis = trafficDensityChart.getXAxis();
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setDrawGridLines(false);
    xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[] { "6-9", "9-12", "12-15", "15-18", "18-21" }));

    // Customize Y axis
    YAxis leftAxis = trafficDensityChart.getAxisLeft();
    leftAxis.setDrawGridLines(true);
    leftAxis.setAxisMinimum(0f);

    trafficDensityChart.getAxisRight().setEnabled(false);
    trafficDensityChart.invalidate();
  }

  private void setupPeakHoursChart() {
    // Sample data for peak hours
    List<PieEntry> entries = new ArrayList<>();
    entries.add(new PieEntry(30f, "Morning\n(6-9)"));
    entries.add(new PieEntry(25f, "Afternoon\n(12-15)"));
    entries.add(new PieEntry(35f, "Evening\n(17-20)"));
    entries.add(new PieEntry(10f, "Night\n(20-6)"));

    PieDataSet dataSet = new PieDataSet(entries, "Peak Hours");
    dataSet.setColors(new int[] { Color.parseColor("#FF5722"), Color.parseColor("#FF9800"),
        Color.parseColor("#FFC107"), Color.parseColor("#FFEB3B") });
    dataSet.setValueTextColor(Color.BLACK);
    dataSet.setValueTextSize(12f);

    PieData pieData = new PieData(dataSet);
    peakHoursChart.setData(pieData);

    // Customize chart appearance
    peakHoursChart.getDescription().setEnabled(false);
    peakHoursChart.getLegend().setEnabled(false);
    peakHoursChart.setHoleRadius(40f);
    peakHoursChart.setTransparentCircleRadius(45f);
    peakHoursChart.setDrawHoleEnabled(true);
    peakHoursChart.setRotationEnabled(true);
    peakHoursChart.setHighlightPerTapEnabled(true);
    peakHoursChart.invalidate();
  }

  private void updateStatistics() {
    // Update statistics text
    avgSpeedText.setText("45 km/h");
    congestionText.setText("Moderate");
    totalVehiclesText.setText("1,234");
    accidentRateText.setText("0.5%");
  }

  private void refreshData() {
    // Simulate data refresh
    setupTrafficTrendChart();
    setupTrafficDensityChart();
    setupPeakHoursChart();
    updateStatistics();
  }

  private void loadTrafficData() {
    progressBar.setVisibility(View.VISIBLE);
    firebaseService.getTrafficData(new FirebaseService.TrafficDataListener() {
      @Override
      public void onDataLoaded(List<Entry> entries) {
        progressBar.setVisibility(View.GONE);
        if (entries.isEmpty()) {
          avgSpeedText.setText("No traffic data available");
          return;
        }

        // Update traffic trend chart
        LineDataSet dataSet = new LineDataSet(entries, "Traffic Speed (km/h)");
        dataSet.setColor(Color.parseColor("#FF5722"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.parseColor("#FF5722"));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        trafficTrendChart.setData(lineData);
        trafficTrendChart.invalidate();
        trafficTrendChart.animateX(1000);

        // Update traffic density chart with bar data
        List<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
          barEntries.add(new BarEntry(i, entries.get(i).getY()));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Vehicle Count");
        barDataSet.setColor(Color.parseColor("#FF5722"));
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueFormatter(new ValueFormatter() {
          @Override
          public String getFormattedValue(float value) {
            return String.valueOf((int) value);
          }
        });

        BarData barData = new BarData(barDataSet);
        trafficDensityChart.setData(barData);
        trafficDensityChart.invalidate();
        trafficDensityChart.animateY(1000);

        // Update statistics
        updateAnalyticsText(entries);
      }

      @Override
      public void onCancelled(DatabaseError error) {
        progressBar.setVisibility(View.GONE);
        avgSpeedText.setText("Error loading data: " + error.getMessage());
      }
    });
  }

  private void updateAnalyticsText(List<Entry> entries) {
    if (entries.isEmpty()) {
      avgSpeedText.setText("No traffic data available");
      return;
    }

    // Calculate average traffic density
    float sum = 0;
    for (Entry entry : entries) {
      sum += entry.getY();
    }
    float average = sum / entries.size();

    // Calculate peak traffic
    float peak = 0;
    for (Entry entry : entries) {
      if (entry.getY() > peak) {
        peak = entry.getY();
      }
    }

    String analytics = String.format(
        "Traffic Analytics:\n\n" +
            "Average Traffic Density: %.1f%%\n" +
            "Peak Traffic Density: %.1f%%\n" +
            "Total Data Points: %d",
        average, peak, entries.size());
    avgSpeedText.setText(analytics);
  }
}