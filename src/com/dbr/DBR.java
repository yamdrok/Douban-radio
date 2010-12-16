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
import android.content.SharedPreferences;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DBR extends Activity {
	/** Called when the activity is first created. */
	
	private Player player;
	private Handler mHandler = new MainHandler();
	private final int UPDATE_UI = 1;

	private ImageView mImageView;
	private TextView mTextView;
	private TextView tTextView;
	private TextView lTextView;
	private Song[] sList;
	private Integer cCount;
	private Integer tCount;
	
	ProgressDialog m_Dialog;
	AlertDialog dlg;
	private EditText mUsername;
	private EditText mPassword;
	private String username;
	private String password;
	private  CheckBox chk;
	private boolean autologin;
	
	public String sessionId;
	public String dbcl2;
	public boolean isAnonymous=false; 

	String loginURI = "http://www.douban.com/accounts/login";
	private int offset;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		mTextView = (TextView)this.findViewById(R.id.down_text);
		mTextView.setTextColor(Color.BLACK);
		tTextView = (TextView)this.findViewById(R.id.top_text);
		tTextView.setTextColor(Color.BLACK);
		lTextView = (TextView)this.findViewById(R.id.lrc_text);
		lTextView.setTextColor(Color.BLACK);
		Button mButton = (Button)this.findViewById(R.id.btn_play);
		Button pButton = (Button)this.findViewById(R.id.btn_pause);
		Button lButton = (Button)this.findViewById(R.id.btn_rate);
		Button bButton = (Button)this.findViewById(R.id.btn_unlike);
		mImageView = (ImageView)this.findViewById(R.id.albumImage);
		
		cCount = -1;
		tCount = 0;
		sessionId="";
		dbcl2="";
		player = new Player(mHandler);

		/*
        new Thread()
        {
          public void run()
          {
            try
            {
              sleep(5000);
           	 
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
		*/

		mButton.setOnClickListener(new Button.OnClickListener()
		{	
			public void onClick(View v)
			{
				//播放下一首
				playNext();

			}
		});

		pButton.setOnClickListener(new Button.OnClickListener()
		{	
			public void onClick(View v)
			{
				//暂停
				if(player.isPlaying()){
                player.playPause();}else{
                	player.playContinue();
                }
			}
		});

		bButton.setOnClickListener(new Button.OnClickListener()
		{	
			public void onClick(View v)
			{
				//删除一首歌
				   try {
					rateSong(sList[cCount].getSid(),"b");
					System.out.println(sList[cCount].getTitle());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		lButton.setOnClickListener(new Button.OnClickListener()
		{	
			public void onClick(View v)
			{
			   //喜欢这首歌
			   try {
				rateSong(sList[cCount].getSid(),"r");
				System.out.println(sList[cCount].getTitle());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}

		});
		
		SharedPreferences settings  = getPreferences(DBR.MODE_PRIVATE);
		autologin =settings.getBoolean("autologin", false);
		username = settings.getString("username", "");
		password = settings.getString("password", "");
		if(autologin){
			login(username,password);
		}
		
		Params xx = new Params();
		String str = getPlayList(xx);
		sList = processJson(str);
		playNext();
		
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
		Song ss;
		mTextView.setText("");
		try {
			object = (JSONObject) new JSONTokener(str).nextValue();
			//String r = object.getString("r");
			JSONArray songs = object.getJSONArray("song");
			//String songs = object.getString("song");
			Integer j = songs.length();
			tCount = j;
			//tTextView.setText("数量:"+tCount);
			songList = new Song[j];
			for( Integer i = 0;i< j;i++){
				JSONObject so = (JSONObject) new JSONTokener(songs.getString(i)).nextValue();
				ss = new Song();
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
				//mTextView.setText(mTextView.getText().toString()+i+"."+ss.getArtist()+"-"+ss.getTitle()+"\n"+ss.getUrl()+"\n");
				mTextView.setText(mTextView.getText().toString()+i+"."+ss.getArtist()+"-"+ss.getTitle()+"\n");
				songList[i] = ss;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return songList;
	}

	/**
	 * 播放下一首歌
	 */
	
	public void playNext(){
		cCount = cCount +1;
		if(cCount == tCount-1){
			cCount=0;
			}
		Song sb = sList[cCount];
		if(sb==null){
			cCount = -1;
			tCount = 0;
			Params xx = new Params();
			String str = getPlayList(xx);
			sList = processJson(str);
			playNext();
			return;
			};
		player.playSong(sb);
		mImageView.setImageBitmap(loadPicture(sb));
		
		
		tTextView.setText("歌曲:"+sb.getTitle()
					+"\n"+"歌手:"+sb.getArtist()
				    +"\n"+"专辑:"+sb.getAlbumtitle()
				    +"\n"+"出品:"+sb.getCompany()
				    +"\n"+"年代:"+sb.getPublic_time()
				    +"\n"+"评分:"+sb.getRating_avg()
				    );
		//显示歌词
		lTextView.setText(getLrc(sb.getArtist(),sb.getTitle()));
	}
	
	/*
	 * 
	 * 获取歌词
	 */
	public String getLrc(String artist,String Title){
		//大约在冬季$$齐秦$$$$
		String lrc="";
		try {
			String lrcUrl = "http://box.zhangmen.baidu.com/x?op=12&count=1&title="+URLEncoder.encode(Title,"UTF-8")+"$$"+URLEncoder.encode(artist,"UTF-8")+"$$$$";
			
			URL getlrcUrl = new URL(lrcUrl);
			HttpURLConnection connLrc = (HttpURLConnection) getlrcUrl.openConnection();
			//connLrc.setRequestProperty("Cookie", sessionId);
			InputStreamReader in = new InputStreamReader(connLrc.getInputStream());
			BufferedReader buffer = new BufferedReader(in);
			String inputLine = null;
			while (((inputLine = buffer.readLine()) != null)){
				lrc = lrc + inputLine;
			}
			in.close();
			connLrc.disconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Integer i = lrc.indexOf("<lrcid>");
		Integer j = lrc.indexOf("</lrcid>");
		
		if(i==-1||j==-1){
			return "歌词未找到";
		}
		
		lrc = lrc.substring(i+7,j);
		
		

		try {
			String finalUrl = "http://box.zhangmen.baidu.com/bdlrc/"+lrc.substring(0,lrc.length()-2)+"/"+lrc+".lrc";
			
			URL finallrcUrl = new URL(finalUrl);
			HttpURLConnection connFinalLrc = (HttpURLConnection) finallrcUrl.openConnection();
			//connLrc.setRequestProperty("Cookie", sessionId);
			InputStreamReader in = new InputStreamReader(connFinalLrc.getInputStream(),"GBK");
			BufferedReader buffer = new BufferedReader(in);
			String inputLine = null;
			lrc="";
			String sl="";
			while (((inputLine = buffer.readLine()) != null)){
				sl = inputLine.substring(inputLine.indexOf("]")+1);
				if(sl.length()>0)
				lrc = lrc +sl+"\n";
			}
			in.close();
			connFinalLrc.disconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lrc;
	}
	
    /**
     * 把如00:00.00这样的字符串转化成
     * 毫秒数的时间，比如 
     * 01:10.34就是一分钟加上10秒再加上340毫秒
     * 也就是返回70340毫秒
     * @param time 字符串的时间
     * @return 此时间表示的毫秒
     */
    private long parseTime(String time) {
        String[] ss = time.split("\\:|\\.");
        //如果 是两位以后，就非法了
        if (ss.length < 2) {
            return -1;
        } else if (ss.length == 2) {//如果正好两位，就算分秒
            try {
                //先看有没有一个是记录了整体偏移量的
                if (offset == 0 && ss[0].equalsIgnoreCase("offset")) {
                    offset = Integer.parseInt(ss[1]);
                    //info.setOffset(offset);
                    System.err.println("整体的偏移量：" + offset);
                    return -1;
                }
                int min = Integer.parseInt(ss[0]);
                int sec = Integer.parseInt(ss[1]);
                if (min < 0 || sec < 0 || sec >= 60) {
                    throw new RuntimeException("数字不合法!");
                }
                return (min * 60 + sec) * 1000L;
            } catch (Exception exe) {
                return -1;
            }
        } else if (ss.length == 3) {//如果正好三位，就算分秒，十毫秒
            try {
                int min = Integer.parseInt(ss[0]);
                int sec = Integer.parseInt(ss[1]);
                int mm = Integer.parseInt(ss[2]);
                if (min < 0 || sec < 0 || sec >= 60 || mm < 0 || mm > 99) {
                    throw new RuntimeException("数字不合法!");
                }
                return (min * 60 + sec) * 1000L + mm * 10;
            } catch (Exception exe) {
                return -1;
            }
        } else {//否则也非法
            return -1;
        }
    }
	
	/*
	 * 显示登陆对话框
	 * 
	 */
	public void showLoginDialog() {
		     //setContentView(R.layout.main);

		     LayoutInflater factory = LayoutInflater.from(DBR.this);
		                 final View DialogView = factory.inflate(R.layout.logindialog, null);
		                 
		                 mUsername =(EditText)DialogView.findViewById(R.id.username);
		                 mPassword =(EditText)DialogView.findViewById(R.id.password);
		                 chk = (CheckBox)DialogView.findViewById(R.id.autologin);		               
		                 mUsername.setText(username);
		                 mPassword.setText(password);
		                 chk.setChecked(autologin);
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
		   		                      username = mUsername.getText().toString();
				                      password = mPassword.getText().toString();
				                      chk.setChecked(autologin);
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
		                    	 //DBR.this.finish();
		                    	 dlg.dismiss();
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
	
    public void rateSong(String sid,String typ) throws IOException{
    	
    	if(!isAnonymous){
    		if(!typ.equalsIgnoreCase("e")){
    		Toast.makeText(getApplicationContext(), "您还没有登陆到豆瓣。",
					Toast.LENGTH_SHORT).show();}
    		return;
    	}
		try {
			String rUrl = "http://douban.fm/j/mine/playlist?r=0&type="+typ+"&channel=0&sid=";
			URL rateUrl = new URL(rUrl+sid);
			HttpURLConnection rateConn = (HttpURLConnection) rateUrl.openConnection();
			rateConn.setRequestProperty("Cookie", sessionId);
			rateConn.setDoOutput(true);
			rateConn.setDoInput(true);
			rateConn.setRequestMethod("GET");
			rateConn.setUseCaches(false);
			rateConn.connect(); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(rateConn.getInputStream()));
			String inputLine = null;
			
			while((inputLine = reader.readLine())!= null ){
				inputLine +=inputLine + "\n";
			}
			
			reader.close();
			rateConn.disconnect();
			if(typ.equalsIgnoreCase("r")){
				Toast.makeText(getApplicationContext(), "喜欢了这首歌。",
						Toast.LENGTH_SHORT).show();
			}
			if(typ.equalsIgnoreCase("u")){
				Toast.makeText(getApplicationContext(), "又不喜欢这首歌了。",
						Toast.LENGTH_SHORT).show();
			}
			if(typ.equalsIgnoreCase("u")){
				Toast.makeText(getApplicationContext(), "屏蔽了这首歌。",
						Toast.LENGTH_SHORT).show();
			}			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		case R.id.refresh:
				cCount = -1;
				tCount = 0;
				Params xx = new Params();
				String str = getPlayList(xx);
				sList = processJson(str);
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
			//	Log.i("TTSDeamon", "UPDATE_UI");
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
					SharedPreferences uiState = getPreferences(0);
					SharedPreferences.Editor editor =uiState.edit();
					editor.putString("username", username);
					editor.putString("password", password);
					editor.putBoolean("autologin", autologin);
					editor.commit();
					Toast.makeText(getApplicationContext(), "登陆成功。",
							Toast.LENGTH_SHORT).show();
				}
				break;
			}
			case 2:{
				//这首歌唱完了
				   try {
						rateSong(sList[cCount].getSid(),"e");
						System.out.println(sList[cCount].getTitle());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				playNext();
				break;
			}
			default:
				break;
			}
		}
	}


	
}