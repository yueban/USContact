package cn.ncuhome.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import cn.ncuhome.model.Contact;
import cn.ncuhome.model.Department;

import com.alibaba.fastjson.JSON;

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

	// ��json����ת��Ϊ�����б�
	public static List<Contact> parseJsonByContact(String jsondata) {
		return JSON.parseArray(jsondata, Contact.class);
	}

	public static List<Department> parseJsonByDepartment(String jsondata) {
		return JSON.parseArray(jsondata, Department.class);
	}

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
			Log.i("info", "contact--->" + contact.toString());
			String Emp_Name = contact.getEmp_Name().trim().replaceAll(" ", "");
			String Emp_Cellphone = contact.getEmp_Cellphone().trim().replaceAll(" ", "");
			String sortString = "";
			sortString = getSortByEmpName(Emp_Name) + Emp_Cellphone;

			// ���sql���
			sql[i] = "insert into " + DBHelper.T_ContactData_name + " (Sort,Dep_ID,Dep_Name,Emp_ID,Emp_Name,Emp_Cellphone) values ('" + sortString.trim() + "','" + contact.getDep_ID().trim() + "','" + contact.getDep_Name().trim() + "','" + contact.getEmp_ID() + "','" + Emp_Name + "','" + Emp_Cellphone + "')";
			i++;
		}
		DBHelper.execSQLDatabase(context, DBHelper.database_name, sql);
	}
}
