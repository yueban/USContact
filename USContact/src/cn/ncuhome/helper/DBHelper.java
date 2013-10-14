package cn.ncuhome.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	private static final int VERSION = 1;
	public static final String database_name = "db_data";
	public static final String T_DepData_name = "t_DepData";
	public static final String T_ContactData_name = "t_ContactData";

	public DBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	public DBHelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	public DBHelper(Context context, String name) {
		this(context, name, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("create table " + T_DepData_name + "(Dep_ID nvarchar(10),Dep_Name nvarchar(20))");
		db.execSQL("create table " + T_ContactData_name + "(Sort nvarchar(30),Dep_ID nvarchar(10),Dep_Name nvarchar(20),Emp_ID nvarchar(10),Emp_Name nvarchar(10),Emp_Cellphone nvarchar(50))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}

	public static SQLiteDatabase getWritableDB(Context context, String db_name) {
		DBHelper dbHelper = new DBHelper(context, db_name);
		return dbHelper.getWritableDatabase();
	}

	public static SQLiteDatabase getReadableDB(Context context, String db_name) {
		DBHelper dbHelper = new DBHelper(context, db_name);
		return dbHelper.getReadableDatabase();
	}

	// [[ 基本数据库操作

	public static void execSQLDatabase(Context context, String db_name, String[] sql) {
		DBHelper dbHelper = new DBHelper(context, db_name);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		for (int i = 0; i < sql.length; i++) {
			db.execSQL(sql[i]);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public static long insertDatabase(Context context, String db_name, String t_name, String nullColumnHack, ContentValues values) {
		DBHelper dbHelper = new DBHelper(context, db_name);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long flag = db.insert(t_name, nullColumnHack, values);
		db.close();
		return flag;
	}

	public static int updateDatabase(Context context, String db_name, String t_name, ContentValues values, String whereClause, String[] whereArgs) {
		DBHelper dbHelper = new DBHelper(context, db_name);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int flag = db.update(t_name, values, whereClause, whereArgs);
		db.close();
		return flag;
	}

	public static Cursor queryCursor(SQLiteDatabase db, String t_name, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
		return db.query(t_name, columns, selection, selectionArgs, groupBy, having, orderBy);
	}

	public static Cursor rawQueryCursor(SQLiteDatabase db, String sql, String[] selectionArgs) {
		return db.rawQuery(sql, selectionArgs);
	}
	// ]]
}
