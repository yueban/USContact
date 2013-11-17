package cn.ncuhome.uscontact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import cn.ncuhome.helper.CodeHelper;
import cn.ncuhome.helper.HanZiToPinYin;
import cn.ncuhome.widget.IndexBar;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class SendMessage extends SherlockListActivity {

	// ������Ҫ�Ŀؼ�
	private RelativeLayout indexDialog;
	private IndexBar indexBarSendMessage;

	WindowManager window;
	private MyListAdapter myListAdapter;
	private ArrayList<HashMap<String, String>> contactlist = new ArrayList<HashMap<String, String>>();

	// �����洢ȫѡ��ȫ��ѡ״̬������
	private SparseBooleanArray allIsCheckedArray;
	private SparseBooleanArray allIsUnCheckedArray;

	// ��ŵ�ǰѡ��״̬
	private SparseBooleanArray isCheckedArray;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sendmessage);

		// �󶨿ؼ�
		indexDialog = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.indexdialog, null);
		indexBarSendMessage = (IndexBar) findViewById(R.id.indexBarSendMessage);

		indexDialog.setVisibility(View.INVISIBLE);
		window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
		window.addView(indexDialog, lp);

		// ��ȡIntent���Ρ���ϵ������
		Intent intent = getIntent();
		final String Dep_Name = intent.getStringExtra("Dep_Name");
		contactlist.addAll((ArrayList<HashMap<String, String>>) intent.getSerializableExtra("contactlist"));
		myListAdapter = new MyListAdapter(SendMessage.this, contactlist);
		setListAdapter(myListAdapter);

		// ����IndexBar
		indexBarSendMessage.setListView(getListView());
		indexBarSendMessage.setRelativeLayoutDialog(indexDialog);

		// ����ActionBar

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setTitle(Dep_Name != null ? Dep_Name : "��ϵ��");

		// ʵ����ѡ�����飬����CheckBox��Ϊδ��ѡ��״̬
		allIsCheckedArray = new SparseBooleanArray();
		allIsUnCheckedArray = new SparseBooleanArray();
		isCheckedArray = new SparseBooleanArray();
		for (int i = 0; i < contactlist.size(); i++) {
			allIsCheckedArray.put(i, true);
			allIsUnCheckedArray.put(i, false);
		}
		isCheckedArray = CodeHelper.cloneCheckStates(allIsCheckedArray);
	}

	@Override
	protected void onDestroy() {
		window.removeView(indexDialog);
		super.onDestroy();
	}

	// Ⱥ������
	private void sendMessage() {
		// ����Ƿ�ѡ������ϵ��
		boolean isNobodyChecked = false;
		for (int i = 0; i < isCheckedArray.size(); i++) {
			if (isCheckedArray.get(i) == false && i == isCheckedArray.size() - 1) {
				isNobodyChecked = true;
			}
		}
		if (isNobodyChecked) {
			Toast.makeText(SendMessage.this, "һ��С��鶼ûѡ��~~", Toast.LENGTH_SHORT).show();
		} else {
			// ƴ�Ӻ����ַ���
			String phoneString = "";
			for (int i = 0; i < contactlist.size(); i++) {
				if (isCheckedArray.get(i)) {
					phoneString += contactlist.get(i).get("Emp_Cellphone");
					phoneString += ";";
				}
			}
			phoneString.substring(0, phoneString.length() - 1);
			// ���÷��Ͷ���ҳ��
			Uri uri = Uri.parse("smsto:" + phoneString);
			Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
			startActivity(intent);

		}
	}

	// [[��Ӧ�¼�
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			isCheckedArray = CodeHelper.cloneCheckStates(allIsCheckedArray);
			myListAdapter.notifyDataSetChanged();
			return true;

		case 2:
			isCheckedArray = CodeHelper.cloneCheckStates(allIsUnCheckedArray);
			myListAdapter.notifyDataSetChanged();
			return true;

		case 3:
			sendMessage();
			return true;

		case android.R.id.home:
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem allSelect = menu.add(0, 1, 0, "ȫѡ");
		MenuItem allUnselect = menu.add(0, 2, 1, "ȫ��ѡ");
		MenuItem sendMessage = menu.add(0, 3, 2, "ȷ��");
		allSelect.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		allUnselect.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		sendMessage.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		isCheckedArray.put(position, isCheckedArray.get(position) == true ? false : true);
		myListAdapter.notifyDataSetChanged();
	}

	// ]]

	// �Զ���ListView������
	private class MyListAdapter extends BaseAdapter implements SectionIndexer {

		// ����ViewHolder�࣬�������Item�еĿؼ������Ч��
		private class ViewHolder {
			TextView textViewContactName;
			TextView textViewCellPhone;
			CheckBox checkBoxContact;
		}

		private LayoutInflater layout;
		private ArrayList<HashMap<String, String>> adapterlist;
		private char[] indexChar;
		private ViewHolder holder;

		public MyListAdapter(Context context, ArrayList<HashMap<String, String>> list) {
			this.layout = LayoutInflater.from(context);
			this.adapterlist = list;
			initIndexChar();
		}

		private void initIndexChar() {
			indexChar = new char[adapterlist.size()];
			for (int i = 0; i < adapterlist.size(); i++) {
				indexChar[i] = HanZiToPinYin.toPinYin(adapterlist.get(i).get("Emp_Name")).toUpperCase(Locale.getDefault()).charAt(0);
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return adapterlist.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return adapterlist.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// �ж��б��Ƿ�仯������仯���������������б�
			if (indexChar.length != adapterlist.size()) {
				initIndexChar();
			}
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = layout.inflate(R.layout.item_sendmessage, null);
				holder.textViewContactName = (TextView) convertView.findViewById(R.id.textViewContactName);
				holder.textViewCellPhone = (TextView) convertView.findViewById(R.id.textViewCellPhone);
				holder.checkBoxContact = (CheckBox) convertView.findViewById(R.id.checkBoxContact);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// ����CheckBox������
			holder.checkBoxContact.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					isCheckedArray.put(position, isChecked);
				}
			});

			if (adapterlist.size() != 0) {
				HashMap<String, String> map = adapterlist.get(position);
				final String Emp_Name = map.get("Emp_Name");
				final String Emp_Cellphone = map.get("Emp_Cellphone");
				holder.textViewContactName.setText(Emp_Name);
				holder.textViewCellPhone.setText(Emp_Cellphone);
				holder.checkBoxContact.setChecked(isCheckedArray.get(position));
			}

			return convertView;
		}

		@Override
		public int getPositionForSection(int section) {
			for (int i = 0; i < getCount(); i++) {
				char firstchar = indexChar[i];
				if (firstchar == section) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public int getSectionForPosition(int position) {
			return 0;
		}

		@Override
		public Object[] getSections() {
			return null;
		}
	}
}
