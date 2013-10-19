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
import cn.ncuhome.helper.CodeHelper;
import cn.ncuhome.helper.CodeHelper.BigFatAsyncTask;
import cn.ncuhome.helper.DBHelper;
import cn.ncuhome.helper.DataOperation;
import cn.ncuhome.helper.IOHelper;
import cn.ncuhome.helper.MyListener.OnServiceListener;
import cn.ncuhome.helper.WebHelper;
import cn.ncuhome.menu.MenuApp;
import cn.ncuhome.menu.MenuDep;
import cn.ncuhome.service.UpdateService;
import cn.ncuhome.service.UpdateService.MyBinder;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends SherlockFragmentActivity {

	// 联网获取json数据的类型
	private enum WebRequest {
		Dep, Emp, Newversion
	}

	// 声明sp对象
	SharedPreferences data;
	SharedPreferences.Editor dataEditor;

	// 声明需要的对象
	private long exittime = 0;
	private UpdateService updateService;
	SlidingMenu sm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_frame);
		// 设置ActionBar
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

		// [[ 启动相关
		// 读取sp数据
		data = getSharedPreferences("data", 0);
		dataEditor = getSharedPreferences("data", 0).edit();

		// 读取启动次数的值，缺省值为0
		int startCount = data.getInt("startCount", 0);
		switch (startCount) {
		// 如果是0，表明是第一次启动程序
		case 0:
			dataEditor.putInt("startCount", 1);
			dataEditor.putInt("versionCode", CodeHelper.getVersionCode(MainActivity.this));
			dataEditor.putString("updateLog", "居然没有更新日志唉，说不定是个bug，赶快反映给大胖吧，第一个反映的有奶茶哦，电话：18607006059");
			dataEditor.commit();
			updateDatabase();
			Toast.makeText(MainActivity.this, "欢迎使用", Toast.LENGTH_SHORT).show();
			break;

		case 1:
			int versionCode = data.getInt("versionCode", -1);
			// 判断是否为更新后第一次启动，如果是，显示更新日志
			int currentVersionCode = CodeHelper.getVersionCode(MainActivity.this);
			if (currentVersionCode > versionCode) {
				dataEditor.putInt("versionCode", currentVersionCode);
				dataEditor.commit();
				showUpdateLog();
			}
			showSlidingMenu();
			changeContactFragment(null, null);
			// 启动后台检查更新服务
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

	// [[ 显示UI界面
	// 替换联系人页面
	private void changeContactFragment(String Dep_ID, String Dep_Name) {
		// 收起SlidingMenu
		Dep_ID = Dep_ID == null ? "-1" : Dep_ID;
		Dep_Name = Dep_Name == null ? "联系人" : Dep_Name;
		// 替换联系人页面
		Contacts contacts = new Contacts();
		Bundle b = new Bundle();
		b.putString("Dep_ID", Dep_ID);
		b.putString("Dep_Name", Dep_Name);
		contacts.setArguments(b);
		getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, contacts, "Contacts").commit();
		// 更改ActionBar标题
		getSupportActionBar().setTitle(Dep_Name);
	}

	// 替换SlidingMenu菜单
	private void showSlidingMenu() {
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, new MenuDep()).commit();
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame_two, new MenuApp()).commit();
	}

	// ]]

	// [[ 更新组件
	// 启动更新服务
	private void startUpdateService() {
		bindService(new Intent(MainActivity.this, UpdateService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}

	// 实例化一个ServiceConnection接口
	ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

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

	// 显示更新日志
	private void showUpdateLog() {
		String updateLog = data.getString("updateLog", null);
		new AlertDialog.Builder(MainActivity.this).setTitle("更新日志").setMessage(updateLog).setPositiveButton("确认", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		}).create().show();
	}

	// 更新app
	private void updateApplication() {
		new MyAsyncTask(MainActivity.this, CodeHelper.url_getNewestVersion, CodeHelper.namespace_getNewestVersion, CodeHelper.method_getNewestVersion, "检查新版本", WebRequest.Newversion).execute("");
	}

	// 更新联系人数据库数据
	private void updateDatabase() {
		if (!WebHelper.isConnectingtoInternet(this.getApplicationContext())) {
			Toast.makeText(MainActivity.this, "啊呀，没联网，检查一下吧", Toast.LENGTH_SHORT).show();
		} else {
			String[] sql = { "delete from " + DBHelper.T_DepData_name, "delete from " + DBHelper.T_ContactData_name };
			DBHelper.execSQLDatabase(MainActivity.this, DBHelper.database_name, sql);
			new MyAsyncTask(MainActivity.this, CodeHelper.url_getDepartmentInfo, CodeHelper.namespace_getDepartmentInfo, CodeHelper.method_getDepartmentInfo, "获取部门分组信息", WebRequest.Dep).execute("");
			new MyAsyncTask(MainActivity.this, CodeHelper.url_getEmployeeInfoByDep_ID, CodeHelper.namespace_getEmployeeInfoByDep_ID, CodeHelper.method_getEmployeeInfoByDep_ID, "获取联系人信息", WebRequest.Emp).execute("{\"Dep_ID\":\"-1\"}");
		}
	}

	// 显示更新apk对话框
	private void showUpdateDialog(final String downloadUrl, final String filename) {
		Dialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("软件更新").setMessage("发现新版本，是否更新？").setPositiveButton("更新", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// 下载并安装新版本
				new MyDownloadAsynvTask(MainActivity.this, downloadUrl, filename).execute("");
			}
		}).setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dataEditor.putString("updateDay", CodeHelper.getUserDate()).commit();
			}
		}).create();
		dialog.setCancelable(false);
		dialog.show();
	}

	// ]]

	// [[ 点选事件
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			switch (event.getAction()) {
			case KeyEvent.ACTION_DOWN:
				if (System.currentTimeMillis() - exittime > 2000) {
					Toast.makeText(getApplicationContext(), "亲~再按一次退出哦", Toast.LENGTH_SHORT).show();
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
		MenuItem bulkSMS = menu.add(0, 1, 0, "群发短信");
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
			Contacts contacts = (Contacts) getSupportFragmentManager().findFragmentByTag("Contacts");
			contacts.sendMessage();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}

	}

	// MenuDep菜单选择
	public void switchMenuDep(String Dep_ID, String Dep_Name) {
		changeContactFragment(Dep_ID, Dep_Name);
	}

	// MenuApp菜单选择
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

	// 创建调用webservice获取json数据并处理的异步任务类
	private class MyAsyncTask extends BigFatAsyncTask {
		private WebRequest flag;

		public MyAsyncTask(Context context, String url, String namespace, String method, String pTitle, WebRequest flag) {
			super(context, url, namespace, method, pTitle);
			this.flag = flag;
		}

		@Override
		public void doInMainThread(String result) {
			// TODO Auto-generated method stub
			switch (flag) {
			case Dep:
				DataOperation.insertDepDataByList(MainActivity.this, DataOperation.parseJsonByDepartment(result));
				break;

			case Emp:
				DataOperation.insertContactDataByList(MainActivity.this, DataOperation.parseJsonByContact(result));
				Toast.makeText(MainActivity.this, "联系人更新完成", Toast.LENGTH_LONG).show();
				showSlidingMenu();
				changeContactFragment(null, null);
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
						Toast.makeText(MainActivity.this, "已经是最新版本", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}

	// 创建下载apk并安装的异步任务类
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
			pDialog.setTitle("正在下载");
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
