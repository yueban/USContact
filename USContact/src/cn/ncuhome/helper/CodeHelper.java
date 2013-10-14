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
	// [[ ��������
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

	// �������
	public static void hideKeyboard(Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	// �����û������ʱ���ʾ��ʽ�����ص�ǰʱ��ĸ�ʽ ��:yyyy-MM-dd
	public static String getUserDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	// �õ��������ڼ�ļ������
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
	 * ����app��ǰ����汾versionCode
	 * 
	 * @param context
	 *            �����Ķ���
	 * @return AndroidManifest.xml�еĴ���汾versionCode
	 */
	public static int getVersionCode(Context context) {
		int versionCode = -1;
		try {
			versionCode = context.getPackageManager().getPackageInfo("cn.ncuhome.us", 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 * �Զ����첽�����࣬������ȡwebservice���ص�json����
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
		 * BigFatAsyncTask��Ĺ��캯�������ڴ��Σ���ʼ������
		 * 
		 * @param context
		 *            �����Ķ���
		 * @param url
		 *            webservice��url
		 * @param namespace
		 *            webservice��namespace
		 * @param method
		 *            webservice��method
		 * @param pTitle
		 *            ��ʾ��ProgressDialog�ϵ�Title���ı���
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