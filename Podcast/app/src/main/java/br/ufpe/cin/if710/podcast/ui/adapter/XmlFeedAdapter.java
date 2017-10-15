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
import br.ufpe.cin.if710.podcast.domain.NewItemFeed;
import br.ufpe.cin.if710.podcast.services.DownloadXMLIntentService;
import br.ufpe.cin.if710.podcast.ui.EpisodeDetailActivity;

public class XmlFeedAdapter extends ArrayAdapter<NewItemFeed> {

    int linkResource;

    public XmlFeedAdapter(Context context, int resource, List<NewItemFeed> objects) {
        super(context, resource, objects);
        linkResource = resource;
    }

    //http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class ViewHolder {
        TextView item_title;
        TextView item_date;
        Button item_action;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        final NewItemFeed item = getItem(position);
        if (convertView == null) {
            convertView = View.inflate(getContext(), linkResource, null);
            holder = new ViewHolder();
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
            holder = (ViewHolder) convertView.getTag();
        }
        holder.item_title.setText(item.getTitle());
        holder.item_date.setText(item.getPubDate());
        setButtonState(holder, item);

        return convertView;
    }

    private void setButtonState(ViewHolder holder, final NewItemFeed item) {
        int dlIcon = R.drawable.ic_file_download_32dp;
        int playIcon = R.drawable.ic_file_play_32dp;

        // If DOWNLOADING, disable button and set text
        if (item.isDownloading() == 1) {
            holder.item_action.setEnabled(false);
            holder.item_action.setText(R.string.action_downloading);
        }

        // If DOWNLOADED, enable button and set play icon
        else if (item.isDownloading() == 2) {
            if (item.getDownloadUri() == null) {
                Toast.makeText(getContext(), "Error while getting file location", Toast.LENGTH_SHORT).show();
            } else {
                setIcon(holder.item_action, playIcon);
                holder.item_action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
            holder.item_action.setEnabled(false);
            setIcon(holder.item_action, dlIcon);
        }

        // If NOT DOWNLOADED/DOWNLOADING, enable button and set download icon
        else {
            setIcon(holder.item_action, dlIcon);
            holder.item_action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Disable button
                    if (v.isEnabled()) {
                        Toast.makeText(getContext(), "DOWNLOADING", Toast.LENGTH_SHORT).show();
                        v.setEnabled(false);
                        // Calls service to download the podcast
                        DownloadXMLIntentService
                                .startActionDownloadPodcast(
                                        getContext(), item.getDownloadLink()
                                );
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