package cn.ncuhome.uscontact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class SplashActivity extends Activity {
	// �������������ӳ�ʱ��
	private static final long SPLASH_DELAY_MILLIS = 650;
	private Animation splashOutAnimation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		// ��ʼ��splashOutAnimation
		splashOutAnimation = new AlphaAnimation(1.0f, 0.0f);
		splashOutAnimation.setDuration(500);
		// ʹ��Handler��postDelayed�������ӳ�һ��ʱ���ִ����ת��MainActivity
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
