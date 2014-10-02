package cn.ncuhome.uscontact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class SplashActivity extends Activity {
	// 设置启动画面延迟时间
	private static final long SPLASH_DELAY_MILLIS = 650;
	private Animation splashOutAnimation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		// 初始化splashOutAnimation
		splashOutAnimation = new AlphaAnimation(1.0f, 0.0f);
		splashOutAnimation.setDuration(500);
		// 使用Handler的postDelayed方法，延迟一段时间后执行跳转到MainActivity
		new Handler().postDelayed(new Runnable() {
			public void run() {
				goHome();
			}
		}, SPLASH_DELAY_MILLIS);
	}

	private void goHome() {
		Intent intent = new Intent(SplashActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
