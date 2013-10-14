package cn.ncuhome.helper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DataOperation {

	/**
	 * ��ȡ������ϵ�������ֶΣ��磺����-->���أ�zhang��san��
	 * 
	 * @param Emp_Name
	 *            ��ϵ������
	 * @return ����ϵ�˵������ֶ�
	 */
	private static String getSortByEmpName(String Emp_Name) {
		String sortString = "";
		if (Emp_Name.substring(0, 2).equals("09")) {
			Emp_Name = Emp_Name.substring(2, Emp_Name.length());
			sortString += "09";
		}
		// ��ȡ��������
		int length = Emp_Name.length();
		// ���ɺ����ַ�������
		String[] hanzi = new String[length];
		for (int i = 0; i < length; i++) {
			hanzi[i] = Emp_Name.substring(i, i + 1);
		}
		// ����ƴ���ַ�������
		String[] pinyin = new String[length];
		for (int i = 0; i < length; i++) {
			pinyin[i] = HanZiToPinYin.toPinYin(hanzi[i]);
		}
		// ƴ�������ֶ�
		for (int i = 0; i < length; i++) {
			sortString += pinyin[i] + hanzi[i];
		}
		return sortString;
	}

	// [[ ��json����ת��Ϊ�����б�
	public static List<Contact> parseJsonByContact(String jsondata) {
		List<Contact> list = null;
		// ʵ��Type�ӿڣ�������Gson�ཫjson����ת��Ϊ�����б�
		Type listType = new TypeToken<List<Contact>>() {
		}.getType();
		list = new Gson().fromJson(jsondata, listType);
		return list;
	}

	public static List<Department> parseJsonByDepartment(String jsondata) {
		List<Department> list = null;
		// ʵ��Type�ӿڣ�������Gson�ཫjson����ת��Ϊ�����б�
		Type listType = new TypeToken<List<Department>>() {
		}.getType();
		list = new Gson().fromJson(jsondata, listType);
		return list;
	}

	// ]]

	// [[ �����ݿ��ж�ȡ����
	public static ArrayList<HashMap<String, String>> getEmpListFromDatabase(Context context, String sql, String[] selectionArgs) {
		ArrayList<HashMap<String, String>> list_map = new ArrayList<HashMap<String, String>>();
		SQLiteDatabase db = DBHelper.getWritableDB(context, DBHelper.database_name);
		Cursor cursor = DBHelper.rawQueryCursor(db, sql, selectionArgs);
		while (cursor.moveToNext()) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("Sort", cursor.getString(cursor.getColumnIndex("Sort")));
			map.put("Emp_Name", cursor.getString(cursor.getColumnIndex("Emp_Name")));
			map.put("Emp_Cellphone", cursor.getString(cursor.getColumnIndex("Emp_Cellphone")));
			list_map.add(map);
		}
		cursor.close();
		db.close();
		return list_map;
	}

	public static ArrayList<HashMap<String, String>> getDepListFromDatabase(Context context) {
		ArrayList<HashMap<String, String>> list_map = new ArrayList<HashMap<String, String>>();
		SQLiteDatabase db = DBHelper.getWritableDB(context, DBHelper.database_name);
		Cursor cursor = DBHelper.queryCursor(db, DBHelper.T_DepData_name, new String[] { "Dep_ID", "Dep_Name" }, null, null, null, null, null);
		while (cursor.moveToNext()) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("Dep_ID", cursor.getString(cursor.getColumnIndex("Dep_ID")));
			map.put("Dep_Name", cursor.getString(cursor.getColumnIndex("Dep_Name")));
			list_map.add(map);
		}
		cursor.close();
		db.close();
		return list_map;
	}

	// ]]

	// [[ �������б��е����ݴ洢�����ݿ⵱��
	public static void insertDepDataByList(Context context, List<Department> listClass) {
		String[] sql = new String[listClass.size()];
		int i = 0;
		for (Iterator<Department> iterator = listClass.iterator(); iterator.hasNext();) {
			Department department = iterator.next();
			// ���sql���
			sql[i] = "insert into " + DBHelper.T_DepData_name + " (Dep_ID,Dep_Name) values ('" + department.getDep_ID() + "','" + department.getDep_Name() + "')";
			i++;
		}
		DBHelper.execSQLDatabase(context, DBHelper.database_name, sql);
	}

	public static void insertContactDataByList(Context context, List<Contact> listClass) {
		String[] sql = new String[listClass.size()];
		int i = 0;
		for (Iterator<Contact> iterator = listClass.iterator(); iterator.hasNext();) {
			Contact contact = iterator.next();
			String Emp_Name = contact.getEmp_Name().trim();
			String Emp_Cellphone = contact.getEmp_Cellphone().trim();
			String sortString = getSortByEmpName(Emp_Name) + Emp_Cellphone;
			// ���sql���
			sql[i] = "insert into " + DBHelper.T_ContactData_name + " (Sort,Dep_ID,Dep_Name,Emp_ID,Emp_Name,Emp_Cellphone) values ('" + sortString.trim() + "','" + contact.getDep_ID().trim() + "','" + contact.getDep_Name().trim() + "','" + contact.getEmp_ID() + "','" + Emp_Name + "','" + Emp_Cellphone + "')";
			i++;
		}
		DBHelper.execSQLDatabase(context, DBHelper.database_name, sql);
	}

	// ]]
	
	// [[ Model��
	public class Contact {
		public String Dep_ID;
		public String Dep_Name;
		public String Emp_ID;
		public String Emp_Name;
		public String Emp_Cellphone;

		public String getDep_ID() {
			return Dep_ID;
		}

		public void setDep_ID(String dep_ID) {
			Dep_ID = dep_ID;
		}

		public String getDep_Name() {
			return Dep_Name;
		}

		public void setDep_Name(String dep_Name) {
			Dep_Name = dep_Name;
		}

		public String getEmp_ID() {
			return Emp_ID;
		}

		public void setEmp_ID(String emp_ID) {
			Emp_ID = emp_ID;
		}

		public String getEmp_Name() {
			return Emp_Name;
		}

		public void setEmp_Name(String emp_Name) {
			Emp_Name = emp_Name;
		}

		public String getEmp_Cellphone() {
			return Emp_Cellphone;
		}

		public void setEmp_Cellphone(String emp_Cellphone) {
			Emp_Cellphone = emp_Cellphone;
		}
	}

	public class Department {
		public String Dep_ID;
		public String Dep_Name;

		public String getDep_ID() {
			return Dep_ID;
		}

		public void setDep_ID(String dep_ID) {
			Dep_ID = dep_ID;
		}

		public String getDep_Name() {
			return Dep_Name;
		}

		public void setDep_Name(String dep_Name) {
			Dep_Name = dep_Name;
		}
	}

	// ]]
}
