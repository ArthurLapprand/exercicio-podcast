package br.ufpe.cin.if710.podcast.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

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
        holder.item_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Disable button
                v.setEnabled(false);

                // Calls service to download the podcast
                DownloadXMLIntentService
                        .startActionDownloadPodcast(
                                getContext(), item.getDownloadLink()
                        );
            }
        });
        return convertView;
    }

}