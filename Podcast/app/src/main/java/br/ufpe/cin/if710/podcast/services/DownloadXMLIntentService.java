package br.ufpe.cin.if710.podcast.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.applications.MyApplication;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.receivers.MyReceiver;
import br.ufpe.cin.if710.podcast.ui.SettingsActivity;

import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_DATE;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_DESC;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_DOWNLOAD_LINK;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_LINK;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_LIST_URI;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_TITLE;

public class DownloadXMLIntentService extends IntentService {

    public static final String BROADCAST_ACTION = "br.ufpe.cin.if710.broadcasts";

    private static final String ACTION_GET_DATA = "br.ufpe.cin.if710.podcast.services.action.ACTION_GET_DATA";
    private static final String ACTION_DOWNLOAD_PODCAST = "br.ufpe.cin.if710.podcast.services.action.ACTION_DOWNLOAD_PODCAST";

    private static final String GET_DATA_PARAM1 = "br.ufpe.cin.if710.podcast.services.extra.GET_DATA_PARAM1";

    public static final String BROADCAST_TYPE = "BROADCAST_TYPE";

    public static final String GET_DATA_BROADCAST = "GET_DATA";
    public static final String DOWNLOAD_PODCAST_BROADCAST = "DOWNLOAD_PODCAST";

    public DownloadXMLIntentService() {
        super("DownloadXMLIntentService");
    }

    public static void startActionGetData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SettingsActivity.FEED_LINK, MODE_PRIVATE);
        String feedLink = sharedPreferences.getString(SettingsActivity.FEED_LINK, context.getResources().getString(R.string.feed_link));

        Intent intent = new Intent(context, DownloadXMLIntentService.class);
        intent.setAction(ACTION_GET_DATA);
        intent.putExtra(GET_DATA_PARAM1, feedLink);
        context.startService(intent);
    }

    public static void startActionDownloadPodcast(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DownloadXMLIntentService.class);
        intent.setAction(ACTION_DOWNLOAD_PODCAST);
        intent.putExtra(GET_DATA_PARAM1, param1);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_DATA.equals(action)) {
                final String param1 = intent.getStringExtra(GET_DATA_PARAM1);
                try {
                    handleActionGetData(param1);
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                }
            } else if (ACTION_DOWNLOAD_PODCAST.equals(action)) {
                final String param1 = intent.getStringExtra(GET_DATA_PARAM1);
                handleActionDownloadPodcast(param1);
            }
        }
    }

    private void handleActionGetData(String feed) throws IOException, XmlPullParserException {

        ContentResolver contentResolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        List<ItemFeed> itemList = XmlFeedParser.parse(getRssFeed(feed));

        for (ItemFeed item: itemList) {
            contentValues.clear();
            contentValues.put(EPISODE_TITLE, item.getTitle());
            contentValues.put(EPISODE_LINK, item.getLink());
            contentValues.put(EPISODE_DATE, item.getPubDate());
            contentValues.put(EPISODE_DESC, item.getDescription());
            contentValues.put(EPISODE_DOWNLOAD_LINK, item.getDownloadLink());
            contentResolver.insert(EPISODE_LIST_URI, contentValues);
        }

        MyApplication app = (MyApplication) (getApplicationContext());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

    private void handleActionDownloadPodcast(String param1) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
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
}
