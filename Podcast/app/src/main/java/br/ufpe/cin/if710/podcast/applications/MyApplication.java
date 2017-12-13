package br.ufpe.cin.if710.podcast.applications;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.room.AppDatabase;

/**
 * Created by Lapp on 08/10/2017.
 */

public class MyApplication extends Application {

    public static final String NOTIFICATION_CHANNEL_ID = "channel_01";

    private boolean areActivitiesCreated;
    private boolean isInBackground;
    private AppDatabase db;
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        areActivitiesCreated = false;
        this.db = Room.databaseBuilder(this,AppDatabase.class,"podcast-database").build();
        checkActivityState();
    }

    public AppDatabase getDb() {
        return db;
    }

    private void createNotificationChannel() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // The user-visible name of the channel.
        CharSequence name = getString(R.string.channel_name);

        // The user-visible description of the channel.
        String description = getString(R.string.channel_description);

        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);

        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        mNotificationManager.createNotificationChannel(mChannel);
    }

    public void checkActivityState() {
        registerActivityLifecycleCallbacks(
                new ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle bundle) {
                        areActivitiesCreated = true;
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {

                    }

                    @Override
                    public void onActivityResumed(Activity activity) {

                    }

                    @Override
                    public void onActivityPaused(Activity activity) {

                    }

                    @Override
                    public void onActivityStopped(Activity activity) {

                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {

                    }
                });
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case TRIM_MEMORY_RUNNING_CRITICAL:
            case TRIM_MEMORY_RUNNING_LOW:
            case TRIM_MEMORY_RUNNING_MODERATE:
                isInBackground = false;
                break;
            default:
                isInBackground = true;
                break;
        }
    }

    public boolean areActivitiesCreated() {
        return areActivitiesCreated;
    }

    public void setAreActivitiesCreated(boolean activitiesCreated) {
        this.areActivitiesCreated = activitiesCreated;
    }

    public boolean isInBackground() {
        return isInBackground;
    }

    public void setInBackground(boolean inBackground) {
        isInBackground = inBackground;
    }
}
