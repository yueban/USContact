package cn.ncuhome.uscontact;

import android.app.Application;

public class MyApplication extends Application {
	// ������Ƿ����ڹ������л�ȡContext
	private static MyApplication context;

	public static MyApplication getContext() {
		return context;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		context = this;
	}
}
