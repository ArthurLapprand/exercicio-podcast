package br.ufpe.cin.if710.podcast;

import org.junit.Before;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import br.ufpe.cin.if710.podcast.domain.NewItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.services.DownloadIntentService;

import static org.junit.Assert.*;
/**
 * Created by danil on 08/12/2017.
 */

public class XmlFeedParserTest {

    private byte[] xmlByte;

    @Before
    public void getXmlByte() throws IOException {
            DownloadIntentService.downloadFileBytes("http://leopoldomt.com/if710/fronteirasdaciencia.xml");
    }

    @Test
    public void ListItemSizeTest() throws IOException, XmlPullParserException {
        ArrayList<NewItemFeed> items;
        items = (ArrayList<NewItemFeed>) XmlFeedParser.parse(new String(xmlByte,"UTF-8"));
        assertEquals("There was loss of RSS Item after the parsing process", 311,items.size());
    }

    @Test
    public void NoDuplicateItemTest() throws IOException, XmlPullParserException {
        ArrayList<NewItemFeed> items;
        items = (ArrayList<NewItemFeed>) XmlFeedParser.parse(new String(xmlByte, "UTF-8"));
        for(int i =0; i< items.size()-1; i++){
            for(int j=i+1;j<items.size();j++){
                assertNotEquals("It has duplicate ItemFeeds", items.get(i).getDownloadLink(), items.get(j).getDownloadLink());
            }
        }
    }

}
