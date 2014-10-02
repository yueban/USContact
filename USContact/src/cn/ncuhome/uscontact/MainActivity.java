package cn.ncuhome.uscontact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.widget.Toast;
import cn.ncuhome.fragment.FragmentContacts;
import cn.ncuhome.fragment.FragmentMenuApp;
import cn.ncuhome.fragment.FragmentMenuDep;
import cn.ncuhome.helper.CodeHelper;
import cn.ncuhome.helper.CodeHelper.BigFatAsyncTask;
import cn.ncuhome.helper.DBHelper;
import cn.ncuhome.helper.DataOperation;
import cn.ncuhome.helper.IOHelper;
import cn.ncuhome.helper.MyListener.OnServiceListener;
import cn.ncuhome.helper.WebHelper;
import cn.ncuhome.service.UpdateService;
import cn.ncuhome.service.UpdateService.MyBinder;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends SherlockFragmentActivity {

	// ������ȡjson���ݵ�����
	private enum WebRequest {
		Dep, Emp, Newversion
	}

	// ����sp����
	SharedPreferences data;
	SharedPreferences.Editor dataEditor;

	// ������Ҫ�Ķ���
	private long exittime = 0;
	private UpdateService updateService;
	public SlidingMenu sm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_frame);
		// ����ActionBar
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		sm = new SlidingMenu(this);
		sm.setMode(SlidingMenu.LEFT_RIGHT);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setMenu(R.layout.menu_frame);
		sm.setSecondaryMenu(R.layout.menu_frame_two);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setSecondaryShadowDrawable(R.drawable.shadowright);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		// [[ �������
		// ��ȡsp����
		data = getSharedPreferences("data", 0);
		dataEditor = getSharedPreferences("data", 0).edit();

		// ��ȡ����������ֵ��ȱʡֵΪ0
		int startCount = data.getInt("startCount", 0);
		switch (startCount) {
		// �����0�������ǵ�һ����������
		case 0:
			dataEditor.putInt("startCount", 1);
			dataEditor.putInt("versionCode", CodeHelper.getVersionCode(MainActivity.this));
			dataEditor.putString("updateLog", "��Ȼû�и�����־����˵�����Ǹ�bug���Ͽ췴ӳ�����ְɣ���һ����ӳ�����̲�Ŷ���绰��18607006059");
			dataEditor.commit();
			updateDatabase();
			Toast.makeText(MainActivity.this, "��ӭʹ��", Toast.LENGTH_SHORT).show();
			break;

		case 1:
			int versionCode = data.getInt("versionCode", -1);
			// �ж��Ƿ�Ϊ���º��һ������������ǣ���ʾ������־
			int currentVersionCode = CodeHelper.getVersionCode(MainActivity.this);
			if (currentVersionCode > versionCode) {
				dataEditor.putInt("versionCode", currentVersionCode);
				dataEditor.commit();
				showUpdateLog();
			}
			showSlidingMenu();
			switchContactFragment(null, null);
			// ������̨�����·���
			if (WebHelper.isConnectingtoInternet(MainActivity.this)) {
				String updateDay = data.getString("updateDay", null);
				String currentDay = CodeHelper.getUserDate();
				if (updateDay == null || CodeHelper.getTwoDay(currentDay, updateDay) > 0) {
					startUpdateService();
				}
			}
		default:
			break;
		}
		// ]]
	}

	@Override
	protected void onDestroy() {
		unbindService(serviceConnection);
		super.onDestroy();
	}

	// [[ ��ʾUI����
	// �滻��ϵ��ҳ��
	private void switchContactFragment(String Dep_ID, String Dep_Name) {
		// ����SlidingMenu
		Dep_ID = Dep_ID == null ? "-1" : Dep_ID;
		Dep_Name = Dep_Name == null ? "��ϵ��" : Dep_Name;
		// �滻��ϵ��ҳ��
		FragmentContacts contacts = new FragmentContacts();
		Bundle b = new Bundle();
		b.putString("Dep_ID", Dep_ID);
		b.putString("Dep_Name", Dep_Name);
		contacts.setArguments(b);
		getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, contacts, "Contacts").commit();
		// ����ActionBar����
		getSupportActionBar().setTitle(Dep_Name);
	}

	// �滻SlidingMenu�˵�
	private void showSlidingMenu() {
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, new FragmentMenuDep()).commit();
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame_two, new FragmentMenuApp()).commit();
	}

	// ]]

	// [[ �������
	// �������·���
	private void startUpdateService() {
		bindService(new Intent(MainActivity.this, UpdateService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}

	// ʵ����һ��ServiceConnection�ӿ�
	ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			updateService = ((MyBinder) service).getService();
			updateService.setOnServiceListener(new OnServiceListener() {

				@Override
				public void ServiceListening(String downloadUrl, String filename) {
					showUpdateDialog(downloadUrl, filename);
				}
			});
			updateService.update();
		}
	};

	// ��ʾ������־
	private void showUpdateLog() {
		String updateLog = data.getString("updateLog", null);
		new AlertDialog.Builder(MainActivity.this).setTitle("������־").setMessage(updateLog).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).create().show();
	}

	// ����app
	private void updateApplication() {
		new MyAsyncTask(MainActivity.this, CodeHelper.url_getNewestVersion, CodeHelper.namespace_getNewestVersion, CodeHelper.method_getNewestVersion, "����°汾", WebRequest.Newversion).execute("");
	}

	// ������ϵ�����ݿ�����
	private void updateDatabase() {
		if (!WebHelper.isConnectingtoInternet(this.getApplicationContext())) {
			Toast.makeText(MainActivity.this, "��ѽ��û���������һ�°�", Toast.LENGTH_SHORT).show();
		} else {
			String[] sql = { "delete from " + DBHelper.T_DepData_name, "delete from " + DBHelper.T_ContactData_name };
			DBHelper.execSQLDatabase(MainActivity.this, DBHelper.database_name, sql);
			new MyAsyncTask(MainActivity.this, CodeHelper.url_getDepartmentInfo, CodeHelper.namespace_getDepartmentInfo, CodeHelper.method_getDepartmentInfo, "��ȡ���ŷ�����Ϣ", WebRequest.Dep).execute("");
			new MyAsyncTask(MainActivity.this, CodeHelper.url_getEmployeeInfoByDep_ID, CodeHelper.namespace_getEmployeeInfoByDep_ID, CodeHelper.method_getEmployeeInfoByDep_ID, "��ȡ��ϵ����Ϣ", WebRequest.Emp).execute("{\"Dep_ID\":\"-1\"}");
		}
	}

	// ��ʾ����apk�Ի���
	private void showUpdateDialog(final String downloadUrl, final String filename) {
		Dialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("�������").setMessage("�����°汾���Ƿ���£�").setPositiveButton("����", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ���ز���װ�°汾
				new MyDownloadAsynvTask(MainActivity.this, downloadUrl, filename).execute("");
			}
		}).setNegativeButton("�ݲ�����", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dataEditor.putString("updateDay", CodeHelper.getUserDate()).commit();
			}
		}).create();
		dialog.setCancelable(false);
		dialog.show();
	}

	// ]]

	// [[ ��ѡ�¼�
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			switch (event.getAction()) {
			case KeyEvent.ACTION_DOWN:
				if (System.currentTimeMillis() - exittime > 2000) {
					Toast.makeText(getApplicationContext(), "��~�ٰ�һ���˳�Ŷ", Toast.LENGTH_SHORT).show();
					exittime = System.currentTimeMillis();
				} else {
					finish();
					System.exit(0);
				}
				return true;

			default:
				break;
			}

		case KeyEvent.KEYCODE_MENU:
			switch (event.getAction()) {
			case KeyEvent.ACTION_DOWN:
				if (sm.isSecondaryMenuShowing()) {
					sm.showContent();
				} else {
					sm.showSecondaryMenu();
				}
				return true;

			default:
				break;
			}
			break;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem bulkSMS = menu.add(0, 1, 0, "Ⱥ������");
		bulkSMS.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (sm.isMenuShowing()) {
				sm.showContent();
			} else {
				sm.showMenu();
			}
			return true;
		case 1:
			FragmentContacts contacts = (FragmentContacts) getSupportFragmentManager().findFragmentByTag("Contacts");
			contacts.sendMessage();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}

	}

	// MenuDep�˵�ѡ��
	public void switchMenuDep(String Dep_ID, String Dep_Name) {
		switchContactFragment(Dep_ID, Dep_Name);
	}

	// MenuApp�˵�ѡ��
	public void switchMenuApp(int position) {
		switch (position) {
		case 0:
			updateDatabase();
			break;

		case 1:
			updateApplication();
			break;

		case 2:
			finish();
			System.exit(0);
			break;

		default:
			break;
		}
		sm.showContent();
	}

	// ]]

	// ��������webservice��ȡjson���ݲ�������첽������
	private class MyAsyncTask extends BigFatAsyncTask {
		private WebRequest flag;

		public MyAsyncTask(Context context, String url, String namespace, String method, String pTitle, WebRequest flag) {
			super(context, url, namespace, method, pTitle);
			this.flag = flag;
		}

		@Override
		public void doInBack(String result) {
			switch (flag) {
			case Dep:
				DataOperation.insertDepDataByList(MainActivity.this, DataOperation.parseJsonByDepartment(result));
				break;

			case Emp:
				DataOperation.insertContactDataByList(MainActivity.this, DataOperation.parseJsonByContact(result));
				break;

			case Newversion:
				break;
			}
		}

		@Override
		public void doInMainThread(String result) {
			switch (flag) {
			case Dep:
				break;

			case Emp:
				Toast.makeText(MainActivity.this, "��ϵ�˸������", Toast.LENGTH_LONG).show();
				showSlidingMenu();
				switchContactFragment(null, null);
				break;

			case Newversion:
				try {
					JSONObject versionData = new JSONArray(result).getJSONObject(0);
					int newestVersionCode = Integer.parseInt(versionData.getString("versionCode"));
					if (newestVersionCode > CodeHelper.getVersionCode(MainActivity.this)) {
						String downloadUrl = versionData.getString("downloadUrl");
						String filename = versionData.getString("filename");
						dataEditor.putString("updateLog", versionData.getString("updateLog"));
						dataEditor.commit();
						showUpdateDialog(downloadUrl, filename);
					} else {
						Toast.makeText(MainActivity.this, "�Ѿ������°汾", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}

	// ��������apk����װ���첽������
	private class MyDownloadAsynvTask extends AsyncTask<String, Integer, String> {
		private ProgressDialog pDialog;
		private Context mContext;
		private String downloadUrl;
		private String filename;
		private float filesize;

		public MyDownloadAsynvTask(Context mContext, String downloadUrl, String filename) {
			super();
			this.mContext = mContext;
			this.downloadUrl = downloadUrl;
			this.filename = filename;
		}

		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(mContext);
			pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pDialog.setMax(100);
			pDialog.setTitle("��������");
			pDialog.setCanceledOnTouchOutside(false);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(downloadUrl);
			HttpResponse response;
			try {
				response = client.execute(get);
				HttpEntity entity = response.getEntity();
				filesize = entity.getContentLength();
				InputStream is = entity.getContent();
				String dirname = "cn.ncuhome.us";
				if (is != null) {
					if (!IOHelper.isDirExist(dirname)) {
						IOHelper.creatSDDir(dirname);
					}
					File file = IOHelper.creatSDFile(dirname, filename);
					FileOutputStream fileOutputStream = new FileOutputStream(file);
					byte[] b = new byte[1024];
					int ch = -1;
					int i = 1;
					while (true) {
						ch = is.read(b);
						if (ch <= 0) {
							break;
						}
						fileOutputStream.write(b, 0, ch);
						publishProgress(i);
						i++;
					}
					is.close();
					fileOutputStream.close();
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			pDialog.setProgress((int) (1024 * values[0] / filesize * 100));
		}

		@Override
		protected void onPostExecute(String result) {
			pDialog.dismiss();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(IOHelper.getSDPATH() + "cn.ncuhome.us", filename)), "application/vnd.android.package-archive");
			startActivity(intent);
			System.exit(0);
			finish();
		}
	}
}
