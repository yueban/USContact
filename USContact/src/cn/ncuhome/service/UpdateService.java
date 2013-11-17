package cn.ncuhome.service;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import cn.ncuhome.helper.CodeHelper;
import cn.ncuhome.helper.CodeHelper.BigFatAsyncTask;
import cn.ncuhome.helper.MyListener.OnServiceListener;

public class UpdateService extends Service {

	public class MyBinder extends Binder {

		public UpdateService getService() {
			return UpdateService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return new MyBinder();
	}

	private OnServiceListener onServiceListener;

	public void setOnServiceListener(OnServiceListener onServiceListener) {
		this.onServiceListener = onServiceListener;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public void update() {
		if (onServiceListener != null) {
			new MyAsyncTask(CodeHelper.url_getNewestVersion, CodeHelper.namespace_getNewestVersion, CodeHelper.method_getNewestVersion).execute("");
		}
	}

	// 创建调用webservice获取json数据并处理的异步任务类
	private class MyAsyncTask extends BigFatAsyncTask {

		public MyAsyncTask(String url, String namespace, String method) {
			super(url, namespace, method);
		}

		@Override
		public void doInMainThread(String result) {
			try {
				JSONObject versionData = new JSONArray(result).getJSONObject(0);
				int newestVersionCode = Integer.parseInt(versionData.getString("versionCode"));
				// 判断是否有新版本
				if (newestVersionCode > CodeHelper.getVersionCode(UpdateService.this)) {
					String downloadUrl = versionData.getString("downloadUrl");
					String filename = versionData.getString("filename");
					getSharedPreferences("data", 0).edit().putString("updateLog", versionData.getString("updateLog")).commit();
					onServiceListener.ServiceListening(downloadUrl, filename);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
