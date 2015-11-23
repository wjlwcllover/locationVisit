package com.weatherinfo.app;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.jar.Attributes.Name;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 访问服务器 返回省级 地址信息 传递到LocationDBOpenHelper 类中 存储到 数据库中
 * 
 * 
 * @author wanjiali '
 */
public class MainActivity extends Activity {

	private String path = "http://www.weather.com.cn/data/list3/city.xml";
	private final String PATH_HEAD = "http://www.weather.com.cn/data/list3/city";
	private final String PATH_TAIL = ".xml";
	private String responseString = null;

	private TextView textView;

	private final int SHOW_RESPONSE = 0;

	private final int SHOW_FAIL = 1;

	private final int SHOW_EXCEPTION = 2;

	private ArrayList<String> provinceLabeList = new ArrayList<String>();

	private ArrayList<String> provinceNameList = new ArrayList<String>();

	private LocationDBOpenHelper dbOpenHelper;
	
	private SQLiteDatabase database ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dbOpenHelper = new LocationDBOpenHelper(MainActivity.this,
				"LOCATION.db", null, 1);
	    database = dbOpenHelper.getWritableDatabase();
	    
		textView = (TextView) findViewById(R.id.test);

		sendRequestWithHttpConnection(database);

	}

	/**
	 * 处理Message的信息
	 */
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			if (msg.what == SHOW_RESPONSE) {
				
				responseString = (String) msg.obj;

				handlePronvinceInfo();

				int index = 0;
				String pstring = "";

				if (provinceLabeList.size() == 0) {
					Toast.makeText(MainActivity.this,
							"provinceLabeList.size() == 0", Toast.LENGTH_LONG)
							.show();
				}
				for (String s : provinceNameList) {
					pstring += provinceNameList.get(index);
					index++;

				}
				textView.setText(pstring);

				for (int i = 0; i < provinceLabeList.size(); i++) {

					ContentValues values = new ContentValues();
					values.put("province_name", provinceNameList.get(i));
					values.put("province_label", provinceLabeList.get(i));
					database.insert("Province", null, values);
				}
			} else if (msg.what == SHOW_FAIL) {
				Toast.makeText(MainActivity.this, msg.obj.toString(),
						Toast.LENGTH_LONG).show();

			} else if (msg.what == SHOW_EXCEPTION) {
				Toast.makeText(MainActivity.this, msg.obj.toString(),
						Toast.LENGTH_LONG).show();

			}

		};
	};

	/**
	 * 通过HttpURLConnetion 类 来访问 网络
	 */
	public synchronized void sendRequestWithHttpConnection(SQLiteDatabase database) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				
				
				try {
					URL url = new URL(path);

					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);

					// if connection.getResponse () == 200 说明 connection 成功可返回数据
					if (connection.getResponseCode() == 200) {
						InputStream inputStream = connection.getInputStream();
						BufferedReader bufferedReader = new BufferedReader(
								new InputStreamReader(inputStream));

						String line = null;
						String response = "";

						// the location information returned is stored in
						// variable response.
						while ((line = bufferedReader.readLine()) != null) {
							response += line;
						}

						Message message = new Message();

						message.what = SHOW_RESPONSE;

						message.obj = response;

						handler.sendMessage(message);

					} else {
						Message message = new Message();
						message.what = SHOW_FAIL;

						message.obj = "访问网络失败，响应码 =="
								+ connection.getResponseCode();

						handler.sendMessage(message);
					}

				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();

					Message message = new Message();
					message.what = SHOW_EXCEPTION;

					message.obj = "访问时，抛出异常";

					handler.sendMessage(message);
				}

			}
		}).start();

	}

	/**
	 * 处理返回的 省级地名信息 并存入一个 数组中 存入到 数据库中
	 */
	public synchronized void handlePronvinceInfo() {

		if (!TextUtils.isEmpty(responseString)) {
			// 将 逗号分隔的 部分 存入数组
			String[] provinceStrings = responseString.split(",");

			// provinceStrings 长度大于 0 时， 并且不用空时
			// 将 每个 code 和 province 名的组合分隔 到 一个 2位数组中， 分别存储 code 和 provinceName
			if ((provinceStrings.length > 0) && (provinceStrings != null)) {
				// 将每一个 code|province 的组合 分隔到 arrayString 中
				for (String p : provinceStrings) {
					String[] arrayStrings = p.split("\\|");

					provinceLabeList.add(arrayStrings[0]);
					provinceNameList.add(arrayStrings[1]);

				}

			}

		}

	}

}
