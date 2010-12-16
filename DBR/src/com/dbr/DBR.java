package com.dbr;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DBR extends Activity {
	/** Called when the activity is first created. */
	
	private Player player;
	private Handler mHandler = new MainHandler();
	private final int UPDATE_UI = 1;

	ImageView mImageView;
	TextView mTextView;
	TextView tTextView;
	private Song[] sList;
	private Integer sCount;
	
	ProgressDialog m_Dialog;
	AlertDialog dlg;
	private EditText mUsername;
	private EditText mPassword;
	
	public String sessionId;
	public String dbcl2;
	public boolean isAnonymous=false; 

	String loginURI = "http://www.douban.com/accounts/login";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		mTextView = (TextView)this.findViewById(R.id.down_text);
		mTextView.setTextColor(Color.BLACK);
		tTextView = (TextView)this.findViewById(R.id.top_text);
		Button mButton = (Button)this.findViewById(R.id.widget67);
		Button pButton = (Button)this.findViewById(R.id.widget66);
		Button lButton = (Button)this.findViewById(R.id.widget65);
		mImageView = (ImageView)this.findViewById(R.id.albumImage);


		sCount = -1;
		sessionId="";
		dbcl2="";
		player = new Player();
		


		//mTextView.setText(str);

		mButton.setOnClickListener(new Button.OnClickListener()
		{	
			public void onClick(View v)
			{
				Params xx = new Params();
				String str = getPlayList(xx);
				sList = processJson(str);
				
				sCount = sCount +1;
				if(sCount > sList.length-1){sCount=0;};
				Song ss = sList[0];
				
				mTextView.setText("");
				mTextView.setText(ss.getUrl());
				
				//player.playSong(ss);
				mImageView.setImageBitmap(loadPicture(ss));
				//tTextView.setSingleLine();
				tTextView.setTextColor(Color.BLACK);
				tTextView.setText("歌曲:"+ss.getTitle()
						    +"\n"+"专辑:"+ss.getAlbumtitle()
						    +"\n"+"出品公司:"+ss.getCompany()
						    +"\n"+"年代:"+ss.getPublic_time()
						    +"   "+"评分:"+ss.getRating_avg()
						    );

			}
		});

		pButton.setOnClickListener(new Button.OnClickListener()
		{	
			public void onClick(View v)
			{
				sCount = -1;
				Params xx = new Params();
				String str = getPlayList(xx);
				sList = processJson(str);

				//mTextView.setFocusable(true);
				//mTextView.setMarqueeRepeatLimit(1);
				//mTextView.setSingleLine();
				//mTextView.setText(str);
			}
		});

		lButton.setOnClickListener(new Button.OnClickListener()
		{	
			public void onClick(View v)
			{
				showLoginDialog();
			}

		}); 
	}
	/*
	 * 获取播放列表
	 * */
	public String getPlayList(Params params){
		String json = "";
		String listUrl = "http://douban.fm/j/mine/playlist?sid=0&type=n&channel=1";

		URL playListUrl = null;
		try {
			playListUrl = new URL(listUrl);
			HttpURLConnection conn = (HttpURLConnection) playListUrl.openConnection();
			conn.setRequestProperty("Cookie", sessionId);
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader buffer = new BufferedReader(in);
			String inputLine = null;
			while (((inputLine = buffer.readLine()) != null)){
				json = json + inputLine;
			}
			in.close();
			conn.disconnect();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	/*
	 * 获取专辑封面
	 * */
	public Bitmap loadPicture(Song song){
		Bitmap bmImg =null;
		String pictureUrl = song.getPicture();

		URL pictureURL= null;
		try {
			pictureURL = new URL(pictureUrl);
			HttpURLConnection conn = (HttpURLConnection) pictureURL.openConnection();
			InputStream is = conn.getInputStream();
			bmImg = BitmapFactory.decodeStream(is);  
			is.close();
			conn.disconnect();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bmImg;
	}
	/*
	 * 
	 * 处理播放列表
	 * 返回Song[]
	 * 
	 */
	public Song[] processJson(String str){
		Song[] songList = null;

		JSONObject object;
		Song ss = new Song();
		mTextView.setText("");
		try {
			object = (JSONObject) new JSONTokener(str).nextValue();
			//String r = object.getString("r");
			JSONArray songs = object.getJSONArray("song");
			//String songs = object.getString("song");
			Integer j = songs.length();
			songList = new Song[j];
			for( Integer i = 0;i< j;i++){;
			JSONObject so = (JSONObject) new JSONTokener(songs.getString(i)).nextValue();

			ss.setAlbum(so.getString("album"));
			ss.setUrl(so.getString("url")); 
			ss.setPicture(so.getString("picture"));
			ss.setTitle(so.getString("title"));
			ss.setAlbumtitle(so.getString("albumtitle"));
			ss.setCompany(so.getString("company"));
			ss.setRating_avg(so.getString("rating_avg"));
			ss.setPublic_time(so.getString("public_time"));
			ss.setLike(so.getString("like"));
			ss.setArtist(so.getString("artist"));
			ss.setSubtype(so.getString("subtype"));
			ss.setSid(so.getString("sid"));
			ss.setAid(so.getString("aid"));
			mTextView.setText(mTextView.getText().toString()+i+"."+ss.getArtist()+"-"+ss.getTitle()+"\n"+ss.getUrl()+"\n");
			songList[i] = ss;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		player.playSong(songList[0]);
		return songList;
	}

	 
	public void showLoginDialog() {
		     //setContentView(R.layout.main);

		     LayoutInflater factory = LayoutInflater.from(DBR.this);
		                 final View DialogView = factory.inflate(R.layout.logindialog, null);
		                 mUsername =(EditText)DialogView.findViewById(R.id.username);
		                 mPassword =(EditText)DialogView.findViewById(R.id.password);
		                 dlg = new AlertDialog.Builder(DBR.this)
		                 .setTitle("登录框")
		                 .setView(DialogView)
		                 .setPositiveButton("确定", 
		                 new DialogInterface.OnClickListener() 
		                 {
		                     public void onClick(DialogInterface dialog, int whichButton)
		                     {
		                      
		                      m_Dialog = ProgressDialog.show
		                                    (
		                                      DBR.this,
		                                      mUsername.getText().toString(),
		                                      "正在验证您的信息...",
		                                      true
		                                    );
		                        
		                         new Thread()
		                         {
		                           public void run()
		                           {
		                             try
		                             {
		                               //sleep(3000);
		                            	 isAnonymous = login(mUsername.getText().toString(),mPassword.getText().toString()); 
		                             }
		                             catch (Exception e)
		                             {
		                               e.printStackTrace();
		                             }
		                             finally
		                             {
		                              mHandler.sendEmptyMessageDelayed(UPDATE_UI, 0);
		                              m_Dialog.dismiss();
		                             }
		                           }
		                         }.start();
		                     }
		                 })
		                 .setNegativeButton("取消", 
		                 new DialogInterface.OnClickListener()
		                 {
		                     public void onClick(DialogInterface dialog, int whichButton)
		                     {
		                    	 DBR.this.finish();
		                     }
		                 })
		                 .create();
		                 dlg.show();	
	}

		
	
	/*
	 * 验证登陆信息
	 * 
	 */
	
	public boolean login(String username,String password){
		 sessionId="";
		 dbcl2="";
		 isAnonymous = false;
		try {
			 URL loginUrl;	
			 loginUrl = new URL(loginURI);
			 HttpURLConnection connection = (HttpURLConnection) loginUrl.openConnection();
			 connection.setDoOutput(true);
			 connection.setDoInput(true);
			 connection.setRequestMethod("POST");
			 connection.setUseCaches(false);
			 connection.setInstanceFollowRedirects(false);
			 connection.setRequestProperty("user-agent","mozilla/4.0 (compatible; msie 6.0; windows 2000)");
			 connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			 connection.connect();		 
			 DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			 String content = URLEncoder.encode("form_email", "UTF-8") + "="
			 + URLEncoder.encode(username, "UTF-8");
			 content += "&" + URLEncoder.encode("form_password", "UTF-8") + "="
			 + URLEncoder.encode(password, "UTF-8");
			 out.writeBytes(content);
			 out.flush();
			 out.close(); // flush and close
			 
			 String key = null; 
			 String cookie = "";
			 for (int i = 1; (key = connection.getHeaderFieldKey(i)) != null; i++) {   

				 if (key.equalsIgnoreCase("set-cookie")) {   
					 cookie = connection.getHeaderField(i);   
					 StringTokenizer fenxi = new StringTokenizer(cookie,";");
					 while(fenxi.hasMoreTokens()){
						 String str=fenxi.nextToken(); 
						 String ss = str.substring(0, 4);
						 if(ss.equalsIgnoreCase("ue=\"")||ss.equalsIgnoreCase("bid=")||ss.equalsIgnoreCase("dbcl")){
							 sessionId=sessionId+str+";";
							 if(ss.equalsIgnoreCase("dbcl"))dbcl2=str;
						 }

					 }
				 }   
			 }

			 connection.disconnect();
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(dbcl2.equalsIgnoreCase("")){
		 return false;}else{return true;}
	}
	
	
	/*
	 * 显示菜单
	 */
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;

	}

	/*
	 * 处理菜单事件
	 */
	public boolean onOptionsItemSelected(MenuItem item){

		int item_id = item.getItemId();

		switch (item_id){
		case R.id.login:
			showLoginDialog();
			break;
		case R.id.exit:
			DBR.this.player.playPause();
			DBR.this.finish();
			break;
		}

		return true;
	}
	
	
	private class MainHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_UI: {
				Log.i("TTSDeamon", "UPDATE_UI");
				if(!isAnonymous){
					Toast toast = Toast.makeText(getApplicationContext(),
							"登陆失败了，检查一下。", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					LinearLayout toastView = (LinearLayout) toast.getView();
					ImageView imageCodeProject = new ImageView(getApplicationContext());
					imageCodeProject.setImageResource(R.drawable.error);
					toastView.addView(imageCodeProject, 0);
					toast.show();
					showLoginDialog();
				}else{
					Toast.makeText(getApplicationContext(), "登陆成功。",
							Toast.LENGTH_SHORT).show();
				}
				break;
			}
			default:
				break;
			}
		}
	}


	
}