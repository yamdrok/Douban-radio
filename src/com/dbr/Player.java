/**
 * 
 */
package com.dbr;

import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.Toast;

/**
 * @author Administrator
 *
 */
public class Player {
	
	private Song song;
	private MediaPlayer mMediaPlayer ;
	
	Player(final Handler mHandler){
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnCompletionListener(
				new MediaPlayer.OnCompletionListener(){
				             @Override
				             public void onCompletion(MediaPlayer arg0) {
//播放结束
				            	 mHandler.sendEmptyMessageDelayed(2, 0);	
				             }
				         });	
	} 

    public void playSong(Song ss){
    	//Uri uri = Uri.parse(song.getUrl());
        //mMediaPlayer = MediaPlayer.create(this,uri);
        song  = ss;
        try {
        	mMediaPlayer.reset();
			mMediaPlayer.setDataSource(song.getUrl());
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
    
    public void playPause(){
    	mMediaPlayer.pause();
    }
    
    public void playContinue(){
    	mMediaPlayer.start();
    }
    
    public boolean isPlaying(){
    		return mMediaPlayer.isPlaying();
    }
    
}
