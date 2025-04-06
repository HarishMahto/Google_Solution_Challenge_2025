package harish.project.maps;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class TrafficMapApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    FirebaseApp.initializeApp(this);
  }
}