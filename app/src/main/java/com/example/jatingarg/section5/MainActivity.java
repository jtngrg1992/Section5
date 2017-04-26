package com.example.jatingarg.section5;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button cameraBtn;
    private Button playBtn;

    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBtn = (Button) findViewById(R.id.cameraBtn);
        playBtn = (Button) findViewById(R.id.playBtn);

        cameraBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.cameraBtn:
                //launch camera activity
                Intent intent = new Intent(MainActivity.this,SecondActivity.class);
                startActivity(intent);
                break;
            case R.id.playBtn:
                //play some media
                Toast.makeText(getApplicationContext(),"Playing sound",Toast.LENGTH_LONG);
                mPlayer= MediaPlayer.create(getApplicationContext(),R.raw.sample);
                mPlayer.start();
                break;
            default:break;
        }
    }

    public MediaPlayer getPlayerInstance(){
        return mPlayer;
    }
}
