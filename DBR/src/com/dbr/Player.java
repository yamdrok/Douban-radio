/**
 * 
 */
package com.dbr;

import android.media.MediaPlayer;

/**
 * @author Administrator
 *
 */
public class Player {
	
	private Song song;
	private MediaPlayer mMediaPlayer ;
	
	Player(){
		mMediaPlayer = new MediaPlayer();
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
    
}
