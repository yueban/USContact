package cn.ncuhome.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import cn.ncuhome.helper.DataOperation;
import cn.ncuhome.uscontact.MainActivity;
import cn.ncuhome.uscontact.R;

import com.actionbarsherlock.app.SherlockListFragment;

public class FragmentMenuDep extends SherlockListFragment {

	private ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_menu_dep, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		list.addAll(DataOperation.getDepListFromDatabase(getActivity()));
		String[] menu_dep_items = new String[list.size() + 1];
		menu_dep_items[0] = "È«²¿";
		for (int i = 0; i < list.size(); i++) {
			menu_dep_items[i + 1] = list.get(i).get("Dep_Name");
		}
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_menudep, R.id.textViewItemMenuDep, menu_dep_items);
		setListAdapter(arrayAdapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (position == 0) {
			switchMenu(null, null);
		} else {
			switchMenu(list.get(position - 1).get("Dep_ID"), list.get(position - 1).get("Dep_Name"));
		}
	}

	private void switchMenu(String Dep_ID, String Dep_Name) {
		if (getActivity() == null)
			return;

		if (getActivity() instanceof MainActivity) {
			MainActivity mainActivity = (MainActivity) getActivity();
			mainActivity.switchMenuDep(Dep_ID, Dep_Name);
		}
	}
}
