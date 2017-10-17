package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.services.MusicPlayerService;

/**
 * Created by Lapp on 15/10/2017.
 */

public class MusicPlayerActivity extends Activity {

    MusicPlayerService musicPlayerService;
    boolean isBound;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        isBound = false;

        String uriKey = getString(R.string.podcast_uri);
        String titlekey = getString(R.string.podcast_title);

        // Check if not returning from notification
        if (getIntent().hasExtra(uriKey)) {

            String podcastTitle =
                    getIntent().getStringExtra(titlekey);

            ((TextView) findViewById(R.id.now_playing_label)).setText(getString(R.string.now_playing, podcastTitle));

            String podcastUriStr =
                    getIntent().getStringExtra(uriKey);

            final Intent mServiceIntent = new Intent(this, MusicPlayerService.class);
            mServiceIntent.putExtra(uriKey, podcastUriStr);
            startService(mServiceIntent);
        }


        final Button startBtn = findViewById(R.id.btn_play);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    musicPlayerService.playMusic();
                } else {
                    Toast.makeText(getApplicationContext(), "Music Service not bound!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final Button pauseBtn = findViewById(R.id.btn_pause);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    musicPlayerService.pauseMusic();
                } else {
                    Toast.makeText(getApplicationContext(), "Music Service not bound!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private ServiceConnection serviceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicPlayerService = ((MusicPlayerService.MusicBinder) service).getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicPlayerService = null;
            isBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (!isBound) {
            Intent intent = new Intent(this, MusicPlayerService.class);
            isBound = bindService(intent, serviceCon, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            musicPlayerService.sendNotification();
            unbindService(serviceCon);
            isBound = false;
        }
    }
}
