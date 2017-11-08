package io.github.ziginsider.revolutiondemo;


import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.Random;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements View.OnClickListener {

    private RevolutionAnimationView mAnimationView;

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen);

        mAnimationView = (RevolutionAnimationView) findViewById(R.id.animated_view);
        findViewById(R.id.btn_pause).setOnClickListener(this);
        findViewById(R.id.btn_resume).setOnClickListener(this);

        mediaPlayer = MediaPlayer.create(FullscreenActivity.this, R.raw.rodina_shostakovich);

        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(1.0f, 1.0f);
        mediaPlayer.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAnimationView.resume();
        mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAnimationView.pause();
        mediaPlayer.pause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pause:
                mediaPlayer.pause();
                mAnimationView.pause();
                break;
            case R.id.btn_resume:
                mediaPlayer.start();
                mAnimationView.resume();
                break;
        }
    }

}
