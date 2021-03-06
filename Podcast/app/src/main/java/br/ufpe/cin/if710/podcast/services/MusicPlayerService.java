package br.ufpe.cin.if710.podcast.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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
import br.ufpe.cin.if710.podcast.db.room.AppDatabase;
import br.ufpe.cin.if710.podcast.db.room.ItemFeedEntity;
import br.ufpe.cin.if710.podcast.ui.MusicPlayerActivity;

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
                    pauseMusic(newUriStr);
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
//                        ContentValues contentValues = new ContentValues();
//                        contentValues.put(EPISODE_FILE_URI, "");
//                        contentValues.put(EPISODE_DOWNLOAD_STATE, 0);
//                        getContentResolver().update(
//                                EPISODE_LIST_URI,
//                                contentValues,
//                                EPISODE_FILE_URI + " =? ",
//                                new String[]{newUriStr}
//                        );
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MyApplication app = (MyApplication) getApplicationContext();
                                AppDatabase db = app.getDb();
                                ItemFeedEntity item = db.itemFeedDAO().getEpisodeFromFileURI(newUriStr);
                                item.setEpisodeFileUri("");
                                item.setEpisodeDownloadState(0);
                                db.itemFeedDAO().updateIteemFeed(item);
                            }
                        }).start();
                        if (mediaPlayer != null) {
                            mediaPlayer.reset();
                        }
                    }
                }
            }
        });
        this.startId = startId;
        new Thread(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.seekTo(getPosition());
            }
        }).start();
//        mediaPlayer.seekTo(getPosition());
//        playMusic();
    }

    private int getPosition() {
        int pos = 0;
        MyApplication app = (MyApplication) getApplicationContext();
        AppDatabase db = app.getDb();
        ItemFeedEntity item = db.itemFeedDAO().getEpisodeFromFileURI(currentPodcastUriStr);
        pos =  item.getEpisodeTimestamp();
//        Cursor c = getContentResolver().query(
//                EPISODE_LIST_URI,
//                new String[] {EPISODE_TIMESTAMP},
//                EPISODE_FILE_URI + " =? ",
//                new String[] {currentPodcastUriStr}, null
//        );
//
//        if (c != null && c.moveToFirst()) {
//            pos = c.getInt(c.getColumnIndex(EPISODE_TIMESTAMP));
//            c.close();
//        }

        return pos;
    }

    public void playMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pauseMusic(String newUriStr) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            new Thread(new CustomRunnable(newUriStr)).start();
        }
    }

    public void pauseMusicOnly() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    class CustomRunnable implements Runnable {
        String newUriStr;
        public CustomRunnable(String uriStr) { newUriStr = uriStr; }
        @Override
        public void run() {
            savePosition(newUriStr);
        }
    }

    private void savePosition(String newUriStr) {
        int pos = mediaPlayer.getCurrentPosition();
        MyApplication app = (MyApplication) getApplicationContext();
        AppDatabase db = app.getDb();
        ItemFeedEntity item = db.itemFeedDAO().getEpisodeFromFileURI(currentPodcastUriStr);
        item.setEpisodeTimestamp(pos);
        db.itemFeedDAO().updateIteemFeed(item);
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(EPISODE_TIMESTAMP, pos);
//        getContentResolver().update(
//                EPISODE_LIST_URI,
//                contentValues,
//                EPISODE_FILE_URI + " =? ",
//                new String[]{currentPodcastUriStr}
//        );
        mediaPlayer.stop();
        mediaPlayer.release();
        createAndStartPlayer(startId, newUriStr);
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
