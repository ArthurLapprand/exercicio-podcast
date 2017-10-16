package br.ufpe.cin.if710.podcast.domain;

import java.util.ArrayList;

/**
 * Created by Lapp on 08/10/2017.
 */

public class NewItemFeed extends ItemFeed {

    private int downloadState;

    public NewItemFeed(String title, String link, String pubDate,
                       String description, String downloadLink,
                       String downloadUri, int downloading) {
        super(title, link, pubDate, description, downloadLink, downloadUri);
        this.downloadState = downloading;
    }

    public NewItemFeed(String[] info, int downloading) {
        super(info[0], info[1], info[2], info[3], info[4], info[5]);
        this.downloadState = downloading;
    }

    public ArrayList<String> getDetails() {
        ArrayList<String> details = new ArrayList<>();
        details.add(getTitle());
        details.add(getDescription());
        details.add(getDownloadUri());
        details.add(getPubDate());
        return details;
    }

    public int getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(int downloadState) {
        this.downloadState = downloadState;
    }
}
