package br.ufpe.cin.if710.podcast.domain;

import java.util.ArrayList;

/**
 * Created by Lapp on 08/10/2017.
 */

public class NewItemFeed extends ItemFeed {

    private int downloading;

    public NewItemFeed(String title, String link, String pubDate,
                       String description, String downloadLink,
                       String downloadUri, int isDownloading) {
        super(title, link, pubDate, description, downloadLink, downloadUri);
        this.downloading = isDownloading;
    }

    public NewItemFeed(String[] info, int downloading) {
        super(info[0], info[1], info[2], info[3], info[4], info[5]);
        this.downloading = downloading;
    }

    public ArrayList<String> getDetails() {
        ArrayList<String> details = new ArrayList<>();
        details.add(getTitle());
        details.add(getDescription());
        details.add(getDownloadUri());
        details.add(getPubDate());
        return details;
    }

    public int isDownloading() {
        return downloading;
    }

    public void setDownloading(int downloading) {
        this.downloading = downloading;
    }
}
