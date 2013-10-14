package cn.ncuhome.uscontact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import cn.ncuhome.helper.CodeHelper;
import cn.ncuhome.helper.CodeHelper.BigFatAsyncTaskwithProgressDialog;
import cn.ncuhome.helper.DBHelper;
import cn.ncuhome.helper.DataOperation;
import cn.ncuhome.helper.IOHelper;
import cn.ncuhome.helper.MyListener.OnServiceListener;
import cn.ncuhome.helper.WebHelper;
import cn.ncuhome.service.UpdateService;
import cn.ncuhome.service.UpdateService.MyBinder;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class MainActivity extends SherlockFragmentActivity {

	// ����sp����
	SharedPreferences data;
	SharedPreferences.Editor dataEditor;

	// ������Ҫ�Ķ���
	private long exittime = 0;
	private String[] mDrawerMenuTitles;
	private ArrayList<Fragment> listFragment;
	private MyFragmentPagerAdapter mMyFragmentPagerAdapter;
	private UpdateService updateService;

	// �����ؼ�����
	private ImageView imageViewLeftLine;
	private ImageView imageViewRightLine;
	private RelativeLayout relativeLayoutContact;
	private RelativeLayout relativeLayoutGroup;
	private ViewPager mViewPager;
	private DrawerLayout mDrawerLayout;
	private LinearLayout linearLayoutLeftDrawer;
	private ListView mDrawerList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		// �󶨿ؼ�
		imageViewLeftLine = (ImageView) findViewById(R.id.imageViewLeftLine);
		imageViewRightLine = (ImageView) findViewById(R.id.imageViewRightLine);
		relativeLayoutContact = (RelativeLayout) findViewById(R.id.relativeLayoutContact);
		relativeLayoutGroup = (RelativeLayout) findViewById(R.id.relativeLayoutGroup);
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		linearLayoutLeftDrawer = (LinearLayout) findViewById(R.id.linearLayoutLeftDrawer);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// ������߳�����Ӱ
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// ������߳���˵�ListView
		mDrawerMenuTitles = getResources().getStringArray(R.array.drawer_menu_array);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.item_drawer_list, mDrawerMenuTitles));

		// ���ü�����
		relativeLayoutContact.setOnClickListener(new OnClickEvent());
		relativeLayoutGroup.setOnClickListener(new OnClickEvent());
		mDrawerList.setOnItemClickListener(new OnDrawerItemClickListener());

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
			// ��ʾUI����
			showUI();
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
	}

	@Override
	protected void onDestroy() {
		unbindService(serviceConnection);
		super.onDestroy();
	}

	// ��ʾUI����
	private void showUI() {
		// ����ImageView
		imageViewLeftLine.setVisibility(View.VISIBLE);
		imageViewRightLine.setVisibility(View.INVISIBLE);

		// ����Fragmentҳ������
		listFragment = new ArrayList<Fragment>();
		listFragment.addAll(getListFragment());

		// ����ViewPager
		mMyFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), listFragment);
		mViewPager.setAdapter(mMyFragmentPagerAdapter);
		mViewPager.setOnPageChangeListener(onPageChangeListener);
	}

	// [[ �������
	// �������·���
	private void startUpdateService() {
		bindService(new Intent(MainActivity.this, UpdateService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}

	// ʵ����һ��ServiceConnection�ӿ�
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

	// ��ʾ������־
	private void showUpdateLog() {
		String updateLog = data.getString("updateLog", null);
		new AlertDialog.Builder(MainActivity.this).setTitle("������־").setMessage(updateLog).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		}).create().show();
	}

	// ����app
	private void updateApplication() {
		new MyAsyncTask(MainActivity.this, CodeHelper.url_getNewestVersion, CodeHelper.namespace_getNewestVersion, CodeHelper.method_getNewestVersion, "����°汾", 2).execute("");
	}

	// �������ݿ�����
	private void updateDatabase() {
		if (!WebHelper.isConnectingtoInternet(this.getApplicationContext())) {
			Toast.makeText(MainActivity.this, "��ѽ��û���������һ�°�", Toast.LENGTH_SHORT).show();
		} else {
			String[] sql = { "delete from " + DBHelper.T_DepData_name, "delete from " + DBHelper.T_ContactData_name };
			DBHelper.execSQLDatabase(MainActivity.this, DBHelper.database_name, sql);
			new MyAsyncTask(MainActivity.this, CodeHelper.url_getDepartmentInfo, CodeHelper.namespace_getDepartmentInfo, CodeHelper.method_getDepartmentInfo, "��ȡ���ŷ�����Ϣ", 0).execute("");
			new MyAsyncTask(MainActivity.this, CodeHelper.url_getEmployeeInfoByDep_ID, CodeHelper.namespace_getEmployeeInfoByDep_ID, CodeHelper.method_getEmployeeInfoByDep_ID, "��ȡ��ϵ����Ϣ", 1).execute("{\"Dep_ID\":\"-1\"}");
		}
	}

	// ��ʾ����apk�Ի���
	private void showUpdateDialog(final String downloadUrl, final String filename) {
		Dialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("�������").setMessage("�����°汾���Ƿ���£�").setPositiveButton("����", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// ���ز���װ�°汾
				new MyDownloadAsynvTask(MainActivity.this, downloadUrl, filename).execute("");
			}
		}).setNegativeButton("�ݲ�����", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		}).create();
		dialog.show();
	}

	// ]]

	// [[ ������
	private class OnClickEvent implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.relativeLayoutContact:
				mViewPager.setCurrentItem(0);
				break;
			case R.id.relativeLayoutGroup:
				mViewPager.setCurrentItem(1);
				break;
			default:
				break;
			}
		}
	}

	private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int arg0) {
			CodeHelper.hideKeyboard(MainActivity.this);
			switch (arg0) {
			case 0:
				imageViewLeftLine.setVisibility(View.VISIBLE);
				imageViewRightLine.setVisibility(View.INVISIBLE);
				break;

			case 1:
				imageViewLeftLine.setVisibility(View.INVISIBLE);
				imageViewRightLine.setVisibility(View.VISIBLE);
				break;
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};

	private class OnDrawerItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			switch (position) {
			case 0:
				mDrawerLayout.closeDrawer(linearLayoutLeftDrawer);
				updateDatabase();
				break;

			case 1:
				mDrawerLayout.closeDrawer(linearLayoutLeftDrawer);
				updateApplication();
				break;

			case 2:
				finish();
				System.exit(0);
				break;

			default:
				break;
			}
		}
	}

	// ]]

	// [[ ��Ӧ�¼�
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if (System.currentTimeMillis() - exittime > 2000) {
				Toast.makeText(getApplicationContext(), "��~�ٰ�һ���˳�Ŷ", Toast.LENGTH_SHORT).show();
				exittime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0);
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_DOWN) {
			if (mDrawerLayout.isDrawerOpen(linearLayoutLeftDrawer)) {
				mDrawerLayout.closeDrawer(linearLayoutLeftDrawer);
			} else {
				mDrawerLayout.openDrawer(linearLayoutLeftDrawer);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	// ]]

	// ��������webservice��ȡjson���ݲ�������첽������
	private class MyAsyncTask extends BigFatAsyncTaskwithProgressDialog {
		private int flag;

		public MyAsyncTask(Context context, String url, String namespace, String method, String pTitle, int flag) {
			super(context, url, namespace, method, pTitle);
			this.flag = flag;
		}

		@Override
		public void doInMainThread(String result) {
			// TODO Auto-generated method stub
			switch (flag) {
			case 0:
				DataOperation.insertDepDataByList(MainActivity.this, DataOperation.parseJsonByDepartment(result));
				break;

			case 1:
				DataOperation.insertContactDataByList(MainActivity.this, DataOperation.parseJsonByContact(result));
				showUI();
				break;

			case 2:
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

	// ����FragmentPagerAdapter������
	private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
		private ArrayList<Fragment> mListFragment = new ArrayList<Fragment>();

		public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> listFragment) {
			super(fm);
			this.mListFragment = listFragment;
		}

		@Override
		public Fragment getItem(int index) {
			return mListFragment.get(index);
		}

		@Override
		public int getCount() {
			return mListFragment.size();
		}

	}

	// ��ȡFragmentҳ������
	private ArrayList<Fragment> getListFragment() {
		ArrayList<Fragment> list = new ArrayList<Fragment>();
		Contacts contacts = new Contacts();
		Bundle b = new Bundle();
		b.putString("Dep_ID", "-1");
		contacts.setArguments(b);
		list.add(contacts);
		list.add(new Groups());
		return list;
	}
}
