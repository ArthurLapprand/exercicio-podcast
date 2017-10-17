package br.ufpe.cin.if710.podcast.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import java.io.File;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.applications.MyApplication;
import br.ufpe.cin.if710.podcast.ui.MusicPlayerActivity;

import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_DOWNLOAD_STATE;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_FILE_URI;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_LIST_URI;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_TIMESTAMP;

public class MusicPlayerService extends Service {

    MediaPlayer mediaPlayer;
    int startId;
    String currentPodcastUriStr;

    public MusicPlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {

        // Check if not returning from notification
        if (intent.hasExtra(getString(R.string.podcast_uri))) {
            String newUriStr = intent.getStringExtra(getString(R.string.podcast_uri));
            // If new podcast to play
            if (!newUriStr.equals(currentPodcastUriStr)) {
                // Stop current if playing and play new podcast
                if (mediaPlayer != null) {
                    pauseMusic();
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    createAndStartPlayer(startId, newUriStr);
                }
                // Instantiate new Player and start playing
                else {
                    createAndStartPlayer(startId, newUriStr);
                }
            }
        }

        return START_STICKY;
    }

    private void createAndStartPlayer(final int startId, final String newUriStr) {
        mediaPlayer = MediaPlayer.create(this, Uri.parse(newUriStr));
        currentPodcastUriStr = newUriStr;
        mediaPlayer.setLooping(false);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopSelf(startId);
                File root =
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS
                        );
                File podcast = new File(root, Uri.parse(newUriStr).getLastPathSegment());
                if (podcast.exists()) {
                    if (!podcast.delete()) {
                        Toast.makeText(getApplicationContext(), "Error while deleting file!", Toast.LENGTH_SHORT).show();
                    } else {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(EPISODE_FILE_URI, "");
                        contentValues.put(EPISODE_DOWNLOAD_STATE, 0);
                        getContentResolver().update(
                                EPISODE_LIST_URI,
                                contentValues,
                                EPISODE_FILE_URI + " =? ",
                                new String[]{newUriStr}
                        );

                        if (mediaPlayer != null) {
                            mediaPlayer.reset();
                        }
                    }
                }
            }
        });
        this.startId = startId;
        mediaPlayer.seekTo(getPosition());
        playMusic();
    }

    private int getPosition() {
        int pos = 0;

        Cursor c = getContentResolver().query(
                EPISODE_LIST_URI,
                new String[] {EPISODE_TIMESTAMP},
                EPISODE_FILE_URI + " =? ",
                new String[] {currentPodcastUriStr}, null
        );

        if (c != null && c.moveToFirst()) {
            pos = c.getInt(c.getColumnIndex(EPISODE_TIMESTAMP));
            c.close();
        }

        return pos;
    }

    public void playMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            savePosition();
        }
    }

    private void savePosition() {
        int pos = mediaPlayer.getCurrentPosition();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EPISODE_TIMESTAMP, pos);
        getContentResolver().update(
                EPISODE_LIST_URI,
                contentValues,
                EPISODE_FILE_URI + " =? ",
                new String[]{currentPodcastUriStr}
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private final IBinder mBinder = new MusicBinder();

    public class MusicBinder extends Binder {
        public MusicPlayerService getService() {
            /* Enables public method calls by returning
             * the Service instance */
            return MusicPlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Enables return to music player activity
     */
    public void sendNotification() {
        if (mediaPlayer != null) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    getApplicationContext(), MyApplication.NOTIFICATION_CHANNEL_ID
            );

            boolean p = mediaPlayer.isPlaying();

            mBuilder
                    .setSmallIcon(p
                            ? android.R.drawable.ic_media_play
                            : android.R.drawable.ic_media_pause)
                    .setOngoing(true)
                    .setContentTitle("Music Player running")
                    .setContentText(p
                            ? "Podcast is playing, click to access player!"
                            : "Podcast is paused, click to access player!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            // Creates an explicit intent for MusicPlayerActivity
            Intent resultIntent = new Intent(getApplicationContext(), MusicPlayerActivity.class);

            // This ensures that navigating backward from the Activity leads out of
            // your app to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());

            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MusicPlayerActivity.class);

            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            555,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            startForeground(2, mBuilder.build());
        }
    }
}
