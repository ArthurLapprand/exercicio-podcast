package br.ufpe.cin.if710.podcast.domain;

/**
 * Created by Lapp on 08/10/2017.
 */

public class NewItemFeed extends ItemFeed {

    public NewItemFeed(String title, String link, String pubDate, String description, String downloadLink) {
        super(title, link, pubDate, description, downloadLink);
    }

    public NewItemFeed(String[] infos) {
        super(infos[0], infos[1], infos[2], infos[3], infos[4]);
    }

}
