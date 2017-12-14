package br.ufpe.cin.if710.podcast.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.room.ItemFeedEntity;
import br.ufpe.cin.if710.podcast.domain.NewItemFeed;
import br.ufpe.cin.if710.podcast.services.DownloadIntentService;
import br.ufpe.cin.if710.podcast.ui.EpisodeDetailActivity;
import br.ufpe.cin.if710.podcast.ui.MusicPlayerActivity;

/**
 * Created by danil on 13/12/2017.
 */

public class RoomXmlFeedAdapter extends ArrayAdapter<ItemFeedEntity> {

    private int linkResource;

    public RoomXmlFeedAdapter(Context context, int resource, List<ItemFeedEntity> objects) {
        super(context, resource, objects);
        linkResource = resource;
    }

    //http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    private static class ViewHolder {
        TextView item_title;
        TextView item_date;
        Button item_action;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        RoomXmlFeedAdapter.ViewHolder holder;
        final ItemFeedEntity item = getItem(position);
        if (convertView == null) {
            convertView = View.inflate(getContext(), linkResource, null);
            holder = new RoomXmlFeedAdapter.ViewHolder();
            holder.item_title = convertView.findViewById(R.id.item_title);
            holder.item_date = convertView.findViewById(R.id.item_date);
            holder.item_action = convertView.findViewById(R.id.item_action);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent detailsIntent = new Intent(getContext(), EpisodeDetailActivity.class);
                    detailsIntent.putStringArrayListExtra(getContext().getString(R.string.details), item.getDetails());
                    getContext().startActivity(detailsIntent);
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (RoomXmlFeedAdapter.ViewHolder) convertView.getTag();
        }
        holder.item_title.setText(item.getEpisodeTitle());
        holder.item_date.setText(item.getEpisodeDate());
        setButtonState(holder, item);

        return convertView;
    }

    private void setButtonState(RoomXmlFeedAdapter.ViewHolder holder, final ItemFeedEntity item) {
        int dlIcon = R.drawable.ic_file_download_32dp;
        int playIcon = R.drawable.ic_file_play_32dp;

        // If DOWNLOADING, disable button and set text and icon
        if (item.getEpisodeDownloadState() == 1) {
            holder.item_action.setEnabled(false);
            setIcon(holder.item_action, dlIcon);
            holder.item_action.setText(R.string.action_downloading);
            holder.item_action.setContentDescription("downloadingState");
        }

        // If DOWNLOADED, enable button and set play icon
        else if (item.getEpisodeDownloadState() == 2) {
            if (item.getEpisodeFileUri() == null) {
                Toast.makeText(getContext(), "Error while getting file location", Toast.LENGTH_SHORT).show();
            } else {
                holder.item_action.setEnabled(true);
                setIcon(holder.item_action, playIcon);
                holder.item_action.setContentDescription("playState");
                holder.item_action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), MusicPlayerActivity.class);
                        intent.putExtra(getContext().getString(R.string.podcast_uri), item.getEpisodeFileUri());
                        intent.putExtra(getContext().getString(R.string.podcast_title), item.getEpisodeTitle());
                        getContext().startActivity(intent);
                    }
                });
            }
        }

        // If NOT DOWNLOADED/DOWNLOADING, enable button and set download icon
        else {
            holder.item_action.setEnabled(true);
            holder.item_action.setText("");
            setIcon(holder.item_action, dlIcon);
            holder.item_action.setContentDescription("toDownloadState");
            holder.item_action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button btn = (Button) v;
                    // Disable button
                    if (btn.isEnabled()) {
                        btn.setEnabled(false);
                        // Calls service to download the podcast
                        DownloadIntentService
                                .startActionDownloadPodcast(
                                        getContext(), item.getEpisodeDownloadLink()
                                );
                        // Update button to downloading
                        btn.setText(R.string.action_downloading);
                    } else {
                        Toast.makeText(getContext(), "DISABLED", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void setIcon(Button button, int drawable) {
        button.setCompoundDrawablesWithIntrinsicBounds(
                0, drawable, 0, 0
        );
    }
}
