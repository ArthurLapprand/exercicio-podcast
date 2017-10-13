package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import br.ufpe.cin.if710.podcast.R;

public class EpisodeDetailActivity extends Activity {

    ArrayList<TextView> detailViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_detail);

        fillPodcastDetails();
    }

    private void fillPodcastDetails() {

        // Get detail views
        // Note: don't change addition order
        detailViews = new ArrayList<>();
        detailViews.add((TextView) findViewById(R.id.detail_title));
        detailViews.add((TextView) findViewById(R.id.detail_description));
        detailViews.add((TextView) findViewById(R.id.detail_file_uri));
        detailViews.add((TextView) findViewById(R.id.detail_pubdate));

        // Get podcast details from intent and set view text
        ArrayList<String> details = getIntent().getStringArrayListExtra(getString(R.string.details));
        for (int i = 0; i < details.size(); i++) {
            if (details.get(i) == null) continue;
            detailViews.get(i).setText(details.get(i));
            detailViews.get(i).setVisibility(View.VISIBLE);
        }

    }
}
