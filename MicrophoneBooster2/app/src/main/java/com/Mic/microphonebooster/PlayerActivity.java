package com.Mic.microphonebooster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {
    Bundle songExtraData;
    ImageView prev, play, next,share;
    int position;
    SeekBar mSeekBarTime;
    static MediaPlayer mMediaPlayer;
    TextView songName;
    static boolean player_active = false;
    boolean stopped = false;
    List<String> musicList = new ArrayList<>();
    @Override
    public void onStop() {
        super.onStop();
        stopped = true;
        player_active = false;
        if(mMediaPlayer!= null && mMediaPlayer.isPlaying())
        mMediaPlayer.pause();
    }

    @Override
    protected void onStart() {
        player_active = true;
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        prev = findViewById(R.id.previous);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        mSeekBarTime = findViewById(R.id.mSeekBarTime);
        songName = findViewById(R.id.songName);
        if (mMediaPlayer!=null) {
            mMediaPlayer.stop();
        }
        Intent intent = getIntent();
        songExtraData = intent.getExtras();
        share = findViewById(R.id.sharebtn);
        musicList = songExtraData.getStringArrayList("songs");
        position = songExtraData.getInt("position", 0);
        initializeMusicPlayer(position);
        play.setOnClickListener(v -> play());

        next.setOnClickListener(v -> {

            if (position < musicList.size() -1) {
                position++;
            } else {
                position = 0;
            }
            initializeMusicPlayer(position);
        });

        prev.setOnClickListener(v -> {
            if (position<=0 || position >= musicList.size()) {
                position = musicList.size() - 1;
            } else{
                position--;
            }

            initializeMusicPlayer(position);
        });
        share.setOnClickListener(v -> {
            String extension = FilenameUtils.getExtension(Environment.getExternalStorageDirectory().getPath()+ "/microphone_booster/" +musicList.get(position));
            String sharePath = Environment.getExternalStorageDirectory().getPath()+ "/microphone_booster/" +musicList.get(position);
            Uri uri = Uri.parse(sharePath);
            Intent share = new Intent(Intent.ACTION_SEND);
            Log.v("extension",extension);
            share.setType("audio/" + extension);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(share, "Share Sound File"));
        });
    }
    Uri uri;
    private void initializeMusicPlayer(int position) {
        if (mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.reset();
        }
        if(position >= musicList.size() || position < 0)
        {
            position = 0;
        }
        String name = musicList.get(position);
        songName.setText(name);
        String path;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/";
        }
        else {
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/microphone_booster/";
        }
        uri = Uri.parse(path+musicList.get(position));
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        mMediaPlayer = MediaPlayer.create(this, uri);
        if(mMediaPlayer != null) {
            mMediaPlayer.setOnPreparedListener(mp -> {
                mSeekBarTime.setMax(mMediaPlayer.getDuration());
                play.setImageResource(R.drawable.ic_pause_foreground);
                mMediaPlayer.start();
            });
            mMediaPlayer.setOnCompletionListener(mp -> play.setImageResource(R.drawable.ic_play_foreground));
            mSeekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mSeekBarTime.setProgress(progress);
                        mMediaPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        if(!stopped) {
            new Thread(() -> {
                while (mMediaPlayer != null) {
                    try {
                        if (mMediaPlayer.isPlaying()) {
                            Message message = new Message();
                            message.what = mMediaPlayer.getCurrentPosition();
                            handler.sendMessage(message);
                            handler.post(() -> {
                           if(uri != null)
                           {
                               File check = new File(uri.getPath());
                               if(!check.isFile() || !check.exists())
                               {
                                   Toast.makeText(PlayerActivity.this,"file not found",Toast.LENGTH_SHORT).show();
                                   startActivity(new Intent(PlayerActivity.this,FragmentsActivity.class));
                               }
                           }
                            });
                            Thread.sleep(1000);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mSeekBarTime.setProgress(msg.what);
        }
    };
    private void play() {
        if (mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            play.setImageResource(R.drawable.ic_play_foreground);
        }
        else if(mMediaPlayer == null)
        {
            Toast.makeText(PlayerActivity.this,"file not found",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PlayerActivity.this,FragmentsActivity.class));
        }
        else if(!mMediaPlayer.isPlaying()){
            mMediaPlayer.start();
            play.setImageResource(R.drawable.ic_pause_foreground);

        }
    }

}