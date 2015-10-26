package net.macdidi.androidtutorial;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by jiahunghsu on 2015/8/12.
 */
public class PlayActivity  extends Activity {

    private MediaPlayer mediaPlayer;
    private boolean isPause = false;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Intent intent = getIntent();
        String fileName = intent.getStringExtra("fileName");

        // 建立指定資源的MediaPlayer物件
        uri = Uri.parse(fileName);
        //mediaPlayer = MediaPlayer.create(this, uri);

    }

    @Override
    protected void onStop() {

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            // 停止播放
            mediaPlayer.stop();
        }

        // 清除MediaPlayer物件
        mediaPlayer.release();
        super.onStop();
    }

    public void onSubmit(View view) {
        // 結束Activity元件
        finish();
    }

    public void clickPlay(View view) {
        // 開始播放
        if(isPause){
            mediaPlayer.start();
        }else{
            if(mediaPlayer != null){
                mediaPlayer.reset();
            }
            //mediaPlayer = MediaPlayer.create(this, R.raw.flashlight);
            Log.v("uri ->>>",uri.toString());
            mediaPlayer = MediaPlayer.create(this, uri);
            mediaPlayer.start();
        }
        isPause = false;
    }

    public void clickPause(View view) {
        // 暫停播放
        mediaPlayer.pause();
        isPause = true;
    }

    public void clickStop(View view) {
        // 停止播放
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

}
