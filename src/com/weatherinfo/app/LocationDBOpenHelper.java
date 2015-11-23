package com.weatherinfo.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * ����һ�����ݿ��� ������һ�����ݿ�  ���������ݿ�Ĳ���
 * @author wanjiali
 *
 */
public class LocationDBOpenHelper extends SQLiteOpenHelper {

	//��������� String��
	public static final String CREATE_PROVINCE = "create table Province ("
			+ "id integer primary key autoincrement,"
			+ "province_name text, "
			+ "province_label text)";
	
	public static final String CREATE_CITY = "create table City ("
			+ "id integer primary key autoincrement,"
			+ "attached_province_code text,"
			+ "city_name text, "
			+ "city_label text)";
	
	
			
	
	
	public LocationDBOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_PROVINCE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
