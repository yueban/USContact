package cn.ncuhome.uscontact;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import cn.ncuhome.helper.CodeHelper;
import cn.ncuhome.helper.DBHelper;
import cn.ncuhome.helper.DataOperation;
import cn.ncuhome.widget.IndexBar;

import com.actionbarsherlock.app.SherlockFragment;

public class Contacts extends SherlockFragment {

	// 声明需要的变量
	private String Dep_Name;

	// 声明需要的控件
	private ListView listViewContact;
	private EditText editTextSearch;
	private RelativeLayout indexDialog;
	private IndexBar indexBarContacts;

	WindowManager window;
	private ArrayList<HashMap<String, String>> contactlist = new ArrayList<HashMap<String, String>>();
	private ArrayList<HashMap<String, String>> searchlist = new ArrayList<HashMap<String, String>>();
	private MyListAdapter myListAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_contacts, null);

		// 绑定控件
		listViewContact = (ListView) view.findViewById(R.id.listViewContact);
		editTextSearch = (EditText) view.findViewById(R.id.editTextSearch);
		indexDialog = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.indexdialog, null);
		indexBarContacts = (IndexBar) view.findViewById(R.id.indexBarContacts);

		indexDialog.setVisibility(View.INVISIBLE);
		window = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
		window.addView(indexDialog, lp);

		// 获取传入的数据
		final String Dep_ID = getArguments().getString("Dep_ID");
		Dep_Name = getArguments().getString("Dep_Name");
		String sql = "";
		String[] selectionArgs = { Dep_ID };

		// 编辑sql语句
		if (selectionArgs[0] == "-1") {
			sql = "SELECT DISTINCT [Sort],[Emp_Name],[Emp_Cellphone] FROM " + DBHelper.T_ContactData_name + " ORDER BY [Sort]";
			selectionArgs = null;
		} else {
			sql = "SELECT [Sort],[Emp_Name],[Emp_Cellphone] FROM " + DBHelper.T_ContactData_name + " WHERE [Dep_ID]=? ORDER BY [Sort]";
		}
		// 读取联系人数据
		if (contactlist.size() != 0) {
			contactlist.clear();
		}
		contactlist.addAll(DataOperation.getEmpListFromDatabase(getActivity(), sql, selectionArgs));
		myListAdapter = new MyListAdapter(getActivity(), contactlist);
		// 设置适配器
		listViewContact.setAdapter(myListAdapter);

		// [[ 设置监听器
		editTextSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() != 0) {
					String sqlstr = "SELECT DISTINCT [Sort],[Emp_Name],[Emp_Cellphone] FROM " + DBHelper.T_ContactData_name + " WHERE [Sort] like ?";
					String[] selectionStr = new String[1];
					if (!Dep_ID.equals("-1")) {
						sqlstr += " and [Dep_ID]='" + Dep_ID + "'";
					}
					selectionStr[0] = "%";
					for (int i = 0; i < s.length(); i++) {
						selectionStr[0] += s.subSequence(i, i + 1) + "%";
					}
					// 读取联系人数据
					if (searchlist.size() != 0) {
						searchlist.clear();
					}
					searchlist.addAll(DataOperation.getEmpListFromDatabase(getActivity(), sqlstr, selectionStr));
					myListAdapter.setList(searchlist);
				} else {
					myListAdapter.setList(contactlist);
				}
				myListAdapter.notifyDataSetChanged();
				listViewContact.invalidate();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});

		// ]]
		return view;
	}

	// 群发短信，供其父Activity调用
	public void sendMessage() {
		// 跳转到群发短信选择联系人页面，并将当前联系人列表传送过去
		Intent intent = new Intent(getActivity(), SendMessage.class);
		intent.putExtra("contactlist", myListAdapter.getList());
		intent.putExtra("Dep_Name", Dep_Name);
		startActivity(intent);
	}

	// 关闭SlidingMenu
	private void closeMenu() {
		if (getActivity() == null)
			return;

		if (getActivity() instanceof MainActivity) {
			MainActivity mainActivity = (MainActivity) getActivity();
			mainActivity.sm.showContent();
		}
	}

	// [[ 生命周期方法
	// 在onActivityCreated中获取listview的滚动监听器，并设置其监听器（在onCreateView中因为ListView还未生成，会返回null）
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// 设置监听器
		listViewContact.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				CodeHelper.hideKeyboard(getActivity());
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
		indexBarContacts.setListView(listViewContact);
		indexBarContacts.setRelativeLayoutDialog(indexDialog);
	}

	@Override
	public void onResume() {
		super.onResume();
		new Handler().postDelayed(new Runnable() {
			public void run() {
				closeMenu();
			}
		}, 100);
	}

	@Override
	public void onDestroyView() {
		window.removeView(indexDialog);
		super.onDestroyView();
	}

	// ]]

	// 创建ListAdapter
	private class MyListAdapter extends BaseAdapter implements SectionIndexer {

		// 创建类，管理ListView中每个item的控件
		public final class ViewHolder {
			TextView textViewContactName;
			TextView textViewCellPhone;
			ImageView imageViewCall;
			ImageView imageViewMessage;
			ImageView imageViewAddContact;

		}

		private LayoutInflater layout;
		private ArrayList<HashMap<String, String>> adapterlist = null;
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
				indexChar[i] = adapterlist.get(i).get("Sort").charAt(0);
			}
		}

		public ArrayList<HashMap<String, String>> getList() {
			return this.adapterlist;
		}

		public void setList(ArrayList<HashMap<String, String>> list) {
			this.adapterlist = list;
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

		
		
		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		@Override
		public int getCount() {
			return adapterlist.size();
		}

		@Override
		public Object getItem(int position) {
			return adapterlist.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// 判断列表是否变化，如果变化，重新生成索引列表
			if (indexChar.length != adapterlist.size()) {
				initIndexChar();
			}
			// 当第一次使用时，需要设定view所表示的layout，这里用ViewHolder更简单，而且方便管理
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = layout.inflate(R.layout.item_contact, null);
				holder.textViewContactName = (TextView) convertView.findViewById(R.id.textViewContactName);
				holder.textViewCellPhone = (TextView) convertView.findViewById(R.id.textViewCellPhone);
				holder.imageViewCall = (ImageView) convertView.findViewById(R.id.imageViewCall);
				holder.imageViewMessage = (ImageView) convertView.findViewById(R.id.imageViewMessage);
				holder.imageViewAddContact = (ImageView) convertView.findViewById(R.id.imageViewAddContact);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// 判断list中是否有数据
			if (adapterlist.size() != 0) {
				HashMap<String, String> map = adapterlist.get(position);
				final String Emp_Name = map.get("Emp_Name");
				final String Emp_Cellphone = map.get("Emp_Cellphone");
				
				holder.textViewContactName.setText(Emp_Name);
				holder.textViewCellPhone.setText(Emp_Cellphone);
				
				// 声明一个内部类监听联系人列表中的三个click事件（拨号，发短信，添加联系人）
				class OnClickEvent implements OnClickListener {

					@Override
					public void onClick(View v) {
						Uri uri = null;
						Intent intent = null;
						switch (v.getId()) {
						case R.id.imageViewCall:
							uri = Uri.parse("tel:" + Emp_Cellphone);
							intent = new Intent(Intent.ACTION_DIAL, uri);
							startActivity(intent);
							break;
						case R.id.imageViewMessage:
							uri = Uri.parse("smsto:" + Emp_Cellphone);
							intent = new Intent(Intent.ACTION_SENDTO, uri);
							startActivity(intent);
							break;
						case R.id.imageViewAddContact:
							uri = android.provider.ContactsContract.Contacts.CONTENT_URI;
							intent = new Intent(Intent.ACTION_INSERT, uri);
							intent.putExtra(android.provider.ContactsContract.Intents.Insert.NAME, Emp_Name);
							intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, Emp_Cellphone);

							startActivity(intent);
							break;
						default:
							break;
						}
					}
				}

				// 设置监听器
				holder.imageViewCall.setOnClickListener(new OnClickEvent());
				holder.imageViewMessage.setOnClickListener(new OnClickEvent());
				holder.imageViewAddContact.setOnClickListener(new OnClickEvent());
			}
			return convertView;
		}
	}
}
