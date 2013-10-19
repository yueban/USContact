package cn.ncuhome.uscontact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class Splash extends Activity {
	// 设置启动画面延迟时间
	private static final long SPLASH_DELAY_MILLIS = 1800;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		Animation animNcuhome = AnimationUtils.loadAnimation(this, R.anim.splash_ncuhome);
		findViewById(R.id.textViewSplashNcuhome).startAnimation(animNcuhome);
		Animation animContact = AnimationUtils.loadAnimation(this, R.anim.splash_contact);
		findViewById(R.id.textViewSplashContact).startAnimation(animContact);
		// 使用Handler的postDelayed方法，3秒后执行跳转到MainActivity
		new Handler().postDelayed(new Runnable() {
			public void run() {
				goHome();
			}
		}, SPLASH_DELAY_MILLIS);
	}

	private void goHome() {
		Intent intent = new Intent(Splash.this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
