package br.ufpe.cin.if710.podcast.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.applications.MyApplication;
import br.ufpe.cin.if710.podcast.ui.MainActivity;

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

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(context, MyApplication.NOTIFICATION_CHANNEL_ID);

        String bcType = intent.getStringExtra(BROADCAST_TYPE);

        switch (bcType) {
            case GET_DATA_BROADCAST:
                sendNotification();
                break;
            case DOWNLOAD_PODCAST_BROADCAST:
                downloadPodcast();
                break;
            default:
                Toast.makeText(context, "Error: wrong broadcast type!", Toast.LENGTH_SHORT).show();
                Log.e("BROADCAST_RECEIVER", "Error: wrong broadcast type.");
                break;
        }
    }

    private void sendNotification() {
        mNotificationBuilder
                .setTicker(tickerText)
                .setSmallIcon(R.drawable.ic_podcast_notification)
                .setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Creates an explicit intent for MainActivity
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setAction(MainActivity.UPDATE_LIST_ACTION);

        // This ensures that navigating backward from the Activity leads out of
        // your app to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        777,
                        PendingIntent.FLAG_ONE_SHOT
                );
        mNotificationBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager.notify(MY_NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void downloadPodcast() {

    }

}
