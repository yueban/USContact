package cn.ncuhome.uscontact;

import android.app.Application;

public class MyApplication extends Application {
	// 这个类是方便在工具类中获取Context
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
