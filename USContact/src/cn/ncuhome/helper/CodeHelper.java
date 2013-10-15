package cn.ncuhome.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.view.inputmethod.InputMethodManager;

public class CodeHelper {
	// [[ 声明常量
	public static final String url_getDepartmentInfo = "http://webservice.ncuhome.cn/NcuhomeUS.asmx/getDepartmentInfo";
	public static final String namespace_getDepartmentInfo = "http://webservice.ncuhome.cn/NcuhomeUS.asmx/";
	public static final String method_getDepartmentInfo = "getDepartmentInfo";

	public static final String url_getEmployeeInfoByDep_ID = "http://webservice.ncuhome.cn/NcuhomeUS.asmx/getEmployeeInfoByDep_ID";
	public static final String namespace_getEmployeeInfoByDep_ID = "http://webservice.ncuhome.cn/NcuhomeUS.asmx/";
	public static final String method_getEmployeeInfoByDep_ID = "getEmployeeInfoByDep_ID";

	public static final String url_getNewestVersion = "http://webservice.ncuhome.cn/NcuhomeUS.asmx/getNewestVersion";
	public static final String namespace_getNewestVersion = "http://webservice.ncuhome.cn/NcuhomeUS.asmx/";
	public static final String method_getNewestVersion = "getNewestVersion";

	// ]]

	// 收起键盘
	public static void hideKeyboard(Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	// 根据用户传入的时间表示格式，返回当前时间的格式 如:yyyy-MM-dd
	public static String getUserDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	// 得到二个日期间的间隔天数
	public static long getTwoDay(String sj1, String sj2) {
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		long day = 0;
		try {
			java.util.Date date = myFormatter.parse(sj1);
			java.util.Date mydate = myFormatter.parse(sj2);
			day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return day;
	}

	/**
	 * 返回app当前代码版本versionCode
	 * 
	 * @param context
	 *            上下文对象
	 * @return AndroidManifest.xml中的代码版本versionCode
	 */
	public static int getVersionCode(Context context) {
		int versionCode = -1;
		try {
			versionCode = context.getPackageManager().getPackageInfo("cn.ncuhome.uscontact", 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 * 自定义异步任务类，用来获取webservice返回的json数据
	 * 
	 * @author bigfat
	 */
	public static abstract class BigFatAsyncTaskwithProgressDialog extends AsyncTask<String, Integer, String> {
		private ProgressDialog pDialog;
		private Context mContext;
		private String url;
		private String namespace;
		private String method;
		private String pTitle;

		/**
		 * BigFatAsyncTask类的构造函数，用于传参，初始化参数
		 * 
		 * @param context
		 *            上下文对象
		 * @param url
		 *            webservice的url
		 * @param namespace
		 *            webservice的namespace
		 * @param method
		 *            webservice的method
		 * @param pTitle
		 *            显示在ProgressDialog上的Title（文本）
		 */
		public BigFatAsyncTaskwithProgressDialog(Context context, String url, String namespace, String method, String pTitle) {
			this.mContext = context;
			this.url = url;
			this.namespace = namespace;
			this.method = method;
			this.pTitle = pTitle;
		}

		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(mContext);
			pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDialog.setTitle(pTitle);
			pDialog.setCanceledOnTouchOutside(false);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			String jsondata = WebHelper.getJsonData(url, namespace, method, params[0]);
			return jsondata;
		}

		@Override
		protected void onPostExecute(String result) {
			doInMainThread(result);
			pDialog.dismiss();
		}

		public abstract void doInMainThread(String result);
	}

	public static abstract class BigFatAsyncTask extends AsyncTask<String, Integer, String> {
		private String url;
		private String namespace;
		private String method;

		public BigFatAsyncTask(String url, String namespace, String method) {
			this.url = url;
			this.namespace = namespace;
			this.method = method;
		}

		@Override
		protected String doInBackground(String... params) {
			String jsondata = WebHelper.getJsonData(url, namespace, method, params[0]);
			return jsondata;
		}

		@Override
		protected void onPostExecute(String result) {
			doInMainThread(result);
		}

		public abstract void doInMainThread(String result);
	}
}
