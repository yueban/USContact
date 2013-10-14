package cn.ncuhome.uscontact;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.ncuhome.helper.DataOperation;

import com.actionbarsherlock.app.SherlockListFragment;

public class Groups extends SherlockListFragment {

	private ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
	private MyListAdapter myListAdapter;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		// 读取部门数据
		if (list.size() != 0) {
			list.clear();
		}
		list.addAll(DataOperation.getDepListFromDatabase(getActivity()));
		myListAdapter = new MyListAdapter(getActivity(), list);
		setListAdapter(myListAdapter);
		return inflater.inflate(R.layout.fragment_groups, null);
	}

	// listview点击事件
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent();
		intent.setClass(getActivity(), DepContacts.class);
		intent.putExtra("Dep_ID", list.get(position).get("Dep_ID"));
		intent.putExtra("Dep_Name", list.get(position).get("Dep_Name"));
		startActivity(intent);
	}

	// 创建自定义ListView适配器
	private class MyListAdapter extends BaseAdapter {

		private LayoutInflater layout;
		private ArrayList<HashMap<String, String>> list;

		public MyListAdapter(Context context, ArrayList<HashMap<String, String>> list) {
			this.layout = LayoutInflater.from(context);
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = layout.inflate(R.layout.item_department, null);
			TextView textViewDepartmentName = (TextView) convertView.findViewById(R.id.textViewDepartmentName);
			if (list.size() != 0) {
				HashMap<String, String> map = list.get(position);
				textViewDepartmentName.setText(map.get("Dep_Name"));
			}
			return convertView;
		}
	}
}
