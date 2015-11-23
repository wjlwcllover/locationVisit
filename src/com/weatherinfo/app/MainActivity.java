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
 * ���ʷ����� ����ʡ�� ��ַ��Ϣ ���ݵ�LocationDBOpenHelper ���� �洢�� ���ݿ���
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
	 * ����Message����Ϣ
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
	 * ͨ��HttpURLConnetion �� ������ ����
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

					// if connection.getResponse () == 200 ˵�� connection �ɹ��ɷ�������
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

						message.obj = "��������ʧ�ܣ���Ӧ�� =="
								+ connection.getResponseCode();

						handler.sendMessage(message);
					}

				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();

					Message message = new Message();
					message.what = SHOW_EXCEPTION;

					message.obj = "����ʱ���׳��쳣";

					handler.sendMessage(message);
				}

			}
		}).start();

	}

	/**
	 * �����ص� ʡ��������Ϣ ������һ�� ������ ���뵽 ���ݿ���
	 */
	public synchronized void handlePronvinceInfo() {

		if (!TextUtils.isEmpty(responseString)) {
			// �� ���ŷָ��� ���� ��������
			String[] provinceStrings = responseString.split(",");

			// provinceStrings ���ȴ��� 0 ʱ�� ���Ҳ��ÿ�ʱ
			// �� ÿ�� code �� province ������Ϸָ� �� һ�� 2λ�����У� �ֱ�洢 code �� provinceName
			if ((provinceStrings.length > 0) && (provinceStrings != null)) {
				// ��ÿһ�� code|province ����� �ָ��� arrayString ��
				for (String p : provinceStrings) {
					String[] arrayStrings = p.split("\\|");

					provinceLabeList.add(arrayStrings[0]);
					provinceNameList.add(arrayStrings[1]);

				}

			}

		}

	}

}
