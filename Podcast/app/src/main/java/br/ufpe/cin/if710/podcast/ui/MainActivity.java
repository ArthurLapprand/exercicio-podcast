package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.domain.NewItemFeed;
import br.ufpe.cin.if710.podcast.services.DownloadXMLIntentService;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.COLUMNS;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_LIST_URI;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.INFO_COLUMNS;
import static br.ufpe.cin.if710.podcast.services.DownloadXMLIntentService.BROADCAST_ACTION;
import static br.ufpe.cin.if710.podcast.services.DownloadXMLIntentService.BROADCAST_TYPE;
import static br.ufpe.cin.if710.podcast.services.DownloadXMLIntentService.DOWNLOAD_PODCAST_BROADCAST;
import static br.ufpe.cin.if710.podcast.services.DownloadXMLIntentService.GET_DATA_BROADCAST;

public class MainActivity extends Activity {

    public final static String TAG = "MAIN_ACTIVITY";

    //ao fazer envio da resolucao, use este link no seu codigo!
    private final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";

    private ListView items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        items = findViewById(R.id.items);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Register Dynamic Receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
                MyDynamicReceiver,
                new IntentFilter(BROADCAST_ACTION)
        );

        // Calls service to download podcasts info
        DownloadXMLIntentService.startActionGetData(this);

    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unregister Dynamic Receiver
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(MyDynamicReceiver);

        XmlFeedAdapter adapter = (XmlFeedAdapter) items.getAdapter();
        if (adapter != null) adapter.clear();
    }

    /* ==== Receives broadcasts from IntentService ==== */
    public BroadcastReceiver MyDynamicReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String bcType = intent.getStringExtra(BROADCAST_TYPE);

            switch (bcType) {
                case GET_DATA_BROADCAST:
                    updatePodcastList();
                    break;
                case DOWNLOAD_PODCAST_BROADCAST:
                    break;
                default:
                    Toast.makeText(context, "Error: wrong broadcast type!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error: wrong broadcast type.");
                    break;
            }
        }
    };

    private void updatePodcastList() {

//        CursorLoader cursorLoader = new CursorLoader(
//                this,
//                EPISODE_LIST_URI,
//                COLUMNS,
//                null, null, null
//        );

//        Cursor c = cursorLoader.loadInBackground();

        Cursor c = getContentResolver().query(
                EPISODE_LIST_URI,
                COLUMNS,
                null, null, null
        );

        List<NewItemFeed> feed = new ArrayList<>();
        assert c != null;
        String[] infos = new String[c.getColumnNames().length];

        int i, j;
        if (c.moveToFirst()) {
            do {
                j = 0;
                for (String column : INFO_COLUMNS) {
                    i = c.getColumnIndex(column);
                    infos[j++] = c.getString(i);
                }
                feed.add(new NewItemFeed(infos));
            } while (c.moveToNext());
        }

        c.close();

//        ContentResolver contentResolver = getContentResolver();
//        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
//                this, R.layout.itemlista,
//                contentResolver.query(
//                        EPISODE_LIST_URI,
//                        COLUMNS,
//                        null, null, null, null
//                ),
//                new String[]{EPISODE_TITLE, EPISODE_DATE},
//                new int[]{R.id.item_title, R.id.item_date},
//                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
//        );

        //Adapter Personalizado
        XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, feed);

        //atualizar o list view
        items.setAdapter(adapter);
        items.setTextFilterEnabled(true);
            /*
            items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    XmlFeedAdapter adapter = (XmlFeedAdapter) parent.getAdapter();
                    ItemFeed item = adapter.getItem(position);
                    String msg = item.getTitle() + " " + item.getLink();
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
            /**/
    }

}
