package cn.ncuhome.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import cn.ncuhome.uscontact.MainActivity;
import cn.ncuhome.uscontact.R;

import com.actionbarsherlock.app.SherlockListFragment;

public class MenuApp extends SherlockListFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_menu_app, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String[] menu_app_items = getResources().getStringArray(R.array.menu_app);
		ArrayAdapter<String> menuAppAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, menu_app_items);
		setListAdapter(menuAppAdapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		switchMenu(position);
	}

	private void switchMenu(int position) {
		if (getActivity() == null)
			return;

		if (getActivity() instanceof MainActivity) {
			MainActivity mainActivity = (MainActivity) getActivity();
			mainActivity.switchMenuApp(position);
		}
	}
}
