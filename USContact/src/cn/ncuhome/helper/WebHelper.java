package cn.ncuhome.helper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class WebHelper {
	// 判断是否联网
	public static Boolean isConnectingtoInternet(Context context) {
		ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cManager != null) {
			NetworkInfo[] infos = cManager.getAllNetworkInfo();
			if (infos != null) {
				for (int i = 0; i < infos.length; i++) {
					if (infos[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// 向webservice发送请求，获取json数据
	public static String getJsonData(String url, String namespace, String method, String requestStr) {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-Type", "application/json; charest:utf-8");
		httpPost.setHeader("SOPAction", namespace + method);
		if (requestStr != null && requestStr != "") {
			try {
				HttpEntity entity = new StringEntity(requestStr, "utf-8");
				httpPost.setEntity(entity);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				String jsonData = EntityUtils.toString(response.getEntity());
				// System.out.println(jsonData);
				try {
					return new JSONObject(jsonData).get("d").toString();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
