package br.ufpe.cin.if710.podcast.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.applications.MyApplication;
import br.ufpe.cin.if710.podcast.ui.MainActivity;

import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.COLUMNS;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_DATE;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_LIST_URI;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_TITLE;
import static br.ufpe.cin.if710.podcast.services.DownloadXMLIntentService.BROADCAST_TYPE;
import static br.ufpe.cin.if710.podcast.services.DownloadXMLIntentService.DOWNLOAD_PODCAST_BROADCAST;
import static br.ufpe.cin.if710.podcast.services.DownloadXMLIntentService.GET_DATA_BROADCAST;

public class MyReceiver extends BroadcastReceiver {

    // Notification IDs
    private static final int MY_NOTIFICATION_ID = 1;

    Context context;
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mNotificationBuilder;

    // Notification text elements
    private final CharSequence tickerText = "Your podcast list was updated";
    private final CharSequence contentTitle = "Podcasts updated";
    private final CharSequence contentText = "Podcast list updated, check back in for new podcasts!";

    // Notification intents
    private Intent mNotificationIntent;
    private PendingIntent mContentIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(context);

        String bcType = intent.getStringExtra(BROADCAST_TYPE);

        switch (bcType) {
            case GET_DATA_BROADCAST:
                sendNotification();
                break;
            case DOWNLOAD_PODCAST_BROADCAST:
                break;
            default:
                Toast.makeText(context, "Error: wrong broadcast type!", Toast.LENGTH_SHORT).show();
                Log.e("BROADCAST_RECEIVER", "Error: wrong broadcast type.");
                break;
        }
    }

    private void sendNotification() {
//        mNotificationIntent = new Intent(context, MainActivity.class);
//        mContentIntent = PendingIntent.getActivity(context, 0, mNotificationIntent, 0);

        mNotificationBuilder
                .setTicker(tickerText)
                .setSmallIcon(R.drawable.ic_podcast_notification)
                .setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
//                .setContentIntent(mContentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setChannelId(MyApplication.NOTIFICATION_CHANNEL_ID);

        // Creates an explicit intent for MainActivity
        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your app to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mNotificationBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager.notify(MY_NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void updatePodcastList() {
        CursorLoader cursorLoader = new CursorLoader(
                context,
                EPISODE_LIST_URI,
                COLUMNS,
                null, null, null
        );
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                context, R.layout.itemlista,
                cursorLoader.loadInBackground(),
                new String[]{EPISODE_TITLE, EPISODE_DATE},
                new int[]{R.id.item_title, R.id.item_date},
                0
        );
    }

}
