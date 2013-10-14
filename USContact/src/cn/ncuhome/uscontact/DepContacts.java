package cn.ncuhome.uscontact;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class DepContacts extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_depcontacts);

		// ��ȡIntent����
		String Dep_ID = getIntent().getStringExtra("Dep_ID");
		String Dep_Name = getIntent().getStringExtra("Dep_Name");

		// ����ActionBar
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setTitle(Dep_Name);

		// ��̬���Fragment��ʾ��ϵ��
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		Contacts contacts = new Contacts();
		Bundle b = new Bundle();
		b.putString("Dep_ID", Dep_ID);
		b.putString("Dep_Name", Dep_Name);
		contacts.setArguments(b);
		fragmentTransaction.add(R.id.linearLayoutDepContacts, contacts);
		fragmentTransaction.commit();
	}

	// [[ ��Ӧ�¼�
	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	// ]]
}
