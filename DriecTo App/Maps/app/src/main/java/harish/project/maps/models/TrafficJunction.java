package harish.project.maps.models;

public class TrafficJunction {
  private String junctionId;
  private double latitude;
  private double longitude;
  private int vehicleDensity;
  private int greenLightDuration;
  private long timestamp;
  private boolean emergencyVehiclePresent;

  public TrafficJunction() {
    // Required for Firebase
  }

  public TrafficJunction(String junctionId, double latitude, double longitude,
      int vehicleDensity, int greenLightDuration,
      boolean emergencyVehiclePresent) {
    this.junctionId = junctionId;
    this.latitude = latitude;
    this.longitude = longitude;
    this.vehicleDensity = vehicleDensity;
    this.greenLightDuration = greenLightDuration;
    this.emergencyVehiclePresent = emergencyVehiclePresent;
    this.timestamp = System.currentTimeMillis();
  }

  // Getters and Setters
  public String getJunctionId() {
    return junctionId;
  }

  public void setJunctionId(String junctionId) {
    this.junctionId = junctionId;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public int getVehicleDensity() {
    return vehicleDensity;
  }

  public void setVehicleDensity(int vehicleDensity) {
    this.vehicleDensity = vehicleDensity;
  }

  public int getGreenLightDuration() {
    return greenLightDuration;
  }

  public void setGreenLightDuration(int greenLightDuration) {
    this.greenLightDuration = greenLightDuration;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public boolean isEmergencyVehiclePresent() {
    return emergencyVehiclePresent;
  }

  public void setEmergencyVehiclePresent(boolean emergencyVehiclePresent) {
    this.emergencyVehiclePresent = emergencyVehiclePresent;
  }
}