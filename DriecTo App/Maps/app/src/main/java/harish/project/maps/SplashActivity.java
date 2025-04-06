package harish.project.maps;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SplashActivity extends AppCompatActivity {
  private static final long SPLASH_DURATION = 3000; // 3 seconds
  private ImageView splashLogo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    // Initialize views
    splashLogo = findViewById(R.id.splashLogo);

    // Start animations
    startAnimations();

    // Navigate to Dashboard after delay
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      // Add exit animation before starting the new activity
      Animation exitAnimation = new AlphaAnimation(1.0f, 0.0f);
      exitAnimation.setDuration(800);
      exitAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
      exitAnimation.setAnimationListener(new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
          Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
          startActivity(intent);
          overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
          finish();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
      });
      splashLogo.startAnimation(exitAnimation);
    }, SPLASH_DURATION - 800); // Start exit animation 800ms before transition
  }

  private void startAnimations() {
    // Create a sequence of animations

    // 1. Initial scale and fade in
    ScaleAnimation scaleAnimation = new ScaleAnimation(
        0.0f, 1.0f, // Start and end X scale
        0.0f, 1.0f, // Start and end Y scale
        Animation.RELATIVE_TO_SELF, 0.5f, // Pivot X
        Animation.RELATIVE_TO_SELF, 0.5f // Pivot Y
    );
    scaleAnimation.setDuration(1200);
    scaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

    AlphaAnimation fadeAnimation = new AlphaAnimation(0.0f, 1.0f);
    fadeAnimation.setDuration(1200);
    fadeAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

    // 2. Bounce animation
    TranslateAnimation bounceAnimation = new TranslateAnimation(
        Animation.RELATIVE_TO_SELF, 0.0f,
        Animation.RELATIVE_TO_SELF, 0.0f,
        Animation.RELATIVE_TO_SELF, 0.0f,
        Animation.RELATIVE_TO_SELF, -0.15f);
    bounceAnimation.setStartOffset(1200); // Start after scale/fade
    bounceAnimation.setDuration(1500);
    bounceAnimation.setRepeatCount(1); // Bounce once
    bounceAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

    // Combine all animations
    AnimationSet animationSet = new AnimationSet(true);
    animationSet.addAnimation(scaleAnimation);
    animationSet.addAnimation(fadeAnimation);
    animationSet.addAnimation(bounceAnimation);

    // Start the animation
    splashLogo.startAnimation(animationSet);
  }
}