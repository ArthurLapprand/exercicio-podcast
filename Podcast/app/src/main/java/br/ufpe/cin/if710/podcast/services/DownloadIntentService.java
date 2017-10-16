package br.ufpe.cin.if710.podcast.services;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.applications.MyApplication;
import br.ufpe.cin.if710.podcast.domain.NewItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.receivers.MyReceiver;
import br.ufpe.cin.if710.podcast.ui.SettingsActivity;

import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_DATE;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_DESC;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_DOWNLOAD_LINK;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_DOWNLOAD_STATE;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_FILE_URI;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_LINK;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_LIST_URI;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_TITLE;

public class DownloadIntentService extends IntentService implements PermissionListener {

    public static final String BROADCAST_ACTION = "br.ufpe.cin.if710.broadcasts";

    private static final String ACTION_GET_DATA = "br.ufpe.cin.if710.podcast.services.action.ACTION_GET_DATA";
    private static final String ACTION_DOWNLOAD_PODCAST = "br.ufpe.cin.if710.podcast.services.action.ACTION_DOWNLOAD_PODCAST";

    private static final String GET_DATA_PARAM1 = "br.ufpe.cin.if710.podcast.services.extra.GET_DATA_PARAM1";
    private static final String DOWNLOAD_PODCAST_PARAM1 = "br.ufpe.cin.if710.podcast.services.extra.DOWNLOAD_PODCAST_PARAM1";

    public static final String BROADCAST_TYPE = "BROADCAST_TYPE";

    public static final String GET_DATA_BROADCAST = "GET_DATA";
    public static final String PODCAST_DOWNLOADED_BROADCAST = "DOWNLOAD_PODCAST";

    // Service download variables
    private String downloadLink;

    public DownloadIntentService() {
        super("DownloadIntentService");
    }

    public static void startActionGetData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SettingsActivity.FEED_LINK, MODE_PRIVATE);
        String feedLink = sharedPreferences.getString(SettingsActivity.FEED_LINK, context.getResources().getString(R.string.feed_link));

        Intent intent = new Intent(context, DownloadIntentService.class);
        intent.setAction(ACTION_GET_DATA);
        intent.putExtra(GET_DATA_PARAM1, feedLink);
        context.startService(intent);
    }

    public static void startActionDownloadPodcast(Context context, String downloadLink) {
        Intent intent = new Intent(context, DownloadIntentService.class);
        intent.setAction(ACTION_DOWNLOAD_PODCAST);
        intent.putExtra(DOWNLOAD_PODCAST_PARAM1, downloadLink);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_DATA.equals(action)) {
                final String feedLink = intent.getStringExtra(GET_DATA_PARAM1);
                try {
                    handleActionGetData(feedLink);
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                }
            } else if (ACTION_DOWNLOAD_PODCAST.equals(action)) {
                final String downloadLink = intent.getStringExtra(DOWNLOAD_PODCAST_PARAM1);
                handleActionDownloadPodcast(downloadLink);
            }
        }
    }

    private void handleActionGetData(String feedLink) throws IOException, XmlPullParserException {

        ContentResolver contentResolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        List<NewItemFeed> itemList = XmlFeedParser.parse(getRssFeed(feedLink));

        for (NewItemFeed item : itemList) {
            contentValues.clear();
            contentValues.put(EPISODE_TITLE, item.getTitle());
            contentValues.put(EPISODE_LINK, item.getLink());
            contentValues.put(EPISODE_DATE, item.getPubDate());
            contentValues.put(EPISODE_DESC, item.getDescription());
            contentValues.put(EPISODE_DOWNLOAD_LINK, item.getDownloadLink());

            // If item doesn't exist, insert it
            if (contentResolver.update(
                    EPISODE_LIST_URI,
                    contentValues,
                    EPISODE_DOWNLOAD_LINK + " =? ",
                    new String[]{item.getDownloadLink()}
            ) == 0) contentResolver.insert(EPISODE_LIST_URI, contentValues);

        }

        MyApplication app = (MyApplication) (getApplicationContext());

        if (app.isInBackground() || !app.areActivitiesCreated()) {
            Intent broadcastIntent = new Intent(getApplicationContext(), MyReceiver.class);
            broadcastIntent.putExtra(BROADCAST_TYPE, GET_DATA_BROADCAST);
            getApplicationContext().sendBroadcast(broadcastIntent);
        } else {
            Intent broadcastIntent = new Intent(BROADCAST_ACTION);
            broadcastIntent.putExtra(BROADCAST_TYPE, GET_DATA_BROADCAST);
            LocalBroadcastManager
                    .getInstance(getApplicationContext())
                    .sendBroadcast(broadcastIntent);
        }
    }

    private void handleActionDownloadPodcast(String downloadLink) {
        this.downloadLink = downloadLink;

        // Check if permission is granted
        TedPermission.with(this)
                .setPermissionListener(this)
                .setDeniedMessage(R.string.on_permission_denied_message)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    private byte[] downloadFileBytes(String urlStr) throws IOException {
        InputStream in = null;
        byte[] response = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            response = out.toByteArray();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return response;
    }

    /* Returns RSS data in string from feed */
    private String getRssFeed(String feed) throws IOException {
        byte[] response = downloadFileBytes(feed);
        return new String(response, "UTF-8");
    }

    @Override
    public void onPermissionGranted() {
        /* Start a thread, set item as downloading
           and start download */
        new Thread (new Runnable() {
            @Override
            public void run() {
                ContentValues contentValues = new ContentValues();
                contentValues.put(EPISODE_DOWNLOAD_STATE, 1);
                getContentResolver().update(
                        EPISODE_LIST_URI,
                        contentValues,
                        EPISODE_DOWNLOAD_LINK + " =? ",
                        new String[]{downloadLink}
                );

                startDownload();
            }
        }).start();
    }

    @Override
    public void onPermissionDenied(ArrayList<String> deniedPermissions) {

    }

    /**
     * Downloads podcast and saves it to the
     * {@link Environment#DIRECTORY_DOWNLOADS downloads directory}.
     * Then, saves file URI in database and notifies
     * that the download finished.
     */
    public void startDownload() {
        File root =
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                );
        root.mkdirs();
        Uri data = Uri.parse(downloadLink);
        File out = new File(root, data.getLastPathSegment());
        if (out.exists()) {
            if (!out.delete()) {
                Toast.makeText(this, "Error while deleting file!", Toast.LENGTH_SHORT).show();
            }
        }
        try {
            URL url = new URL(data.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            FileOutputStream fos = new FileOutputStream(out.getPath());
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            try {
                InputStream in = connection.getInputStream();
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) >= 0) {
                    bos.write(buffer, 0, len);
                }
                bos.flush();
            } finally {
                fos.getFD().sync();
                bos.close();
                connection.disconnect();

                String path = "file://" + out.getAbsolutePath();
                ContentValues contentValues = new ContentValues();
                contentValues.put(EPISODE_FILE_URI, path);
                contentValues.put(EPISODE_DOWNLOAD_STATE, 2);
                getContentResolver().update(
                        EPISODE_LIST_URI,
                        contentValues,
                        EPISODE_DOWNLOAD_LINK + " =? ",
                        new String[]{downloadLink}
                );

                Intent broadcastIntent = new Intent(BROADCAST_ACTION);
                broadcastIntent.putExtra(BROADCAST_TYPE, PODCAST_DOWNLOADED_BROADCAST);
                LocalBroadcastManager
                        .getInstance(getApplicationContext())
                        .sendBroadcast(broadcastIntent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
