package br.ufpe.cin.if710.podcast;

import android.support.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Suite;

/**
 * Created by danil on 12/12/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({DownloadEpisodeTest.class,PlayEpisodeTest.class,NotificationTest.class})
public class PodcastTestSuite {
}
