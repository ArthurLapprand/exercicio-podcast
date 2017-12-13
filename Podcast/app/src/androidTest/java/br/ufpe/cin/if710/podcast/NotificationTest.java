package br.ufpe.cin.if710.podcast;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by danil on 12/12/2017.
 */

public class NotificationTest {

    private static final String APP_PACKAGE = "br.ufpe.cin.if710.podcast";
    private static final int LAUNCH_TIMEOUT = 5000;
    private UiDevice mDevice;

    @Before
    public void startMainActivityFromHomeScreen() throws UiObjectNotFoundException, InterruptedException {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.pressHome();
        String launcherPackage = mDevice.getLauncherPackageName();
        assertNotNull(launcherPackage);
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);
        Context context = InstrumentationRegistry.getContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(APP_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        mDevice.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), LAUNCH_TIMEOUT);
        if(!checkPlayableEpisode()) {
            downloadEpisode();
        }
        playEpisode();
    }

    public void downloadEpisode() throws UiObjectNotFoundException, InterruptedException {
        UiScrollable listPodcast= null;
        UiObject downloadButtonEpisode = null;
        UiObject linearLayoutOuter = null;
        UiObject linearLayoutInner = null;
        int i = 0;
        boolean downloadButtonFound = false;
        do {
            listPodcast = new UiScrollable(new UiSelector().className(ListView.class.getName()));
            for (i = 0; i < listPodcast.getChildCount(); i++) {
                linearLayoutOuter = listPodcast.getChild(new UiSelector().index(i));
                linearLayoutInner = linearLayoutOuter.getChild(new UiSelector().index(0));
                try {
                    downloadButtonEpisode = linearLayoutInner.getChild(new UiSelector().index(1));
                    System.out.println(downloadButtonEpisode.getContentDescription());
                    if(downloadButtonEpisode.getContentDescription().equals("toDownloadState")){
                        downloadButtonFound = true;
                        break;
                    }
                } catch (UiObjectNotFoundException e) {
                    continue;
                }
            }
        }while(!downloadButtonFound && listPodcast.scrollForward());
        assertTrue("Did not found a download button", downloadButtonEpisode.exists());
        //Getting the altered button to check if it is downloading
        UiObject downloadingButtonEpisode = null;
        System.out.println(downloadButtonEpisode.getContentDescription());
        downloadButtonEpisode.click();
        downloadingButtonEpisode = mDevice.findObject(new UiSelector().text("DOWNLOADING"));;
        assertTrue("The episode is not being downloaded", downloadingButtonEpisode.waitForExists(10000));
        //Waiting for the download to happen
        UiObject playButtonEpisode = linearLayoutInner.getChild(new UiSelector().description("playState"));
        assertTrue("The episode is not playable", playButtonEpisode.waitForExists(3*60*1000));
    }

    public boolean checkPlayableEpisode() throws UiObjectNotFoundException {
        UiScrollable listPodcast = new UiScrollable(new UiSelector().className(ListView.class.getName()));
        UiObject playButtonEpisode = null;
        UiObject linearLayoutOuter = null;
        UiObject linearLayoutInner = null;
        int i = 0;
        boolean playButtonFound = false;
        do {
            for (i = 0; i < listPodcast.getChildCount(); i++) {
                linearLayoutOuter = listPodcast.getChild(new UiSelector().index(i));
                linearLayoutInner = linearLayoutOuter.getChild(new UiSelector().index(0));
                try {
                    playButtonEpisode = linearLayoutInner.getChild(new UiSelector().index(1));
                    if(playButtonEpisode.getContentDescription().equals("playState")){
                        return true;
                    }
//                    playButtonEpisode = linearLayoutInner.getChild(new UiSelector().resourceId("item_action").description("playState"));
//                    if (playButtonEpisode.exists()) {
//                        System.out.println(playButtonEpisode.getContentDescription());
//                        return true;
//                    }
                } catch (UiObjectNotFoundException e) {
                    continue;
                }
            }
        }while(!playButtonFound && listPodcast.scrollForward());
        return false;
    }

    public void playEpisode() throws UiObjectNotFoundException {
        UiScrollable listPodcast = new UiScrollable(new UiSelector().className(ListView.class.getName()));
        UiObject playButtonEpisode = null;
        UiObject linearLayoutOuter = null;
        UiObject linearLayoutInner = null;
        int i = 0;
        boolean playEpisodeFound = false;
        do {
            for (i = 0; i < listPodcast.getChildCount(); i++) {
                linearLayoutOuter = listPodcast.getChild(new UiSelector().index(i));
                linearLayoutInner = linearLayoutOuter.getChild(new UiSelector().index(0));
                try {
                    playButtonEpisode = linearLayoutInner.getChild(new UiSelector().index(1));
                    System.out.println(playButtonEpisode.getContentDescription());
                    if(playButtonEpisode.getContentDescription().equals("playState")){
                        playEpisodeFound = true;
                        break;
                    }
                } catch (UiObjectNotFoundException e) {
                    continue;
                }
            }
        }while(!playEpisodeFound && listPodcast.scrollForward());
        playButtonEpisode.clickAndWaitForNewWindow(5000);
        UiObject playButton = mDevice.findObject(new UiSelector().text("Play"));
        System.out.println(playButton.getText());
        UiObject pauseButton = mDevice.findObject(new UiSelector().text("Pause"));
        System.out.println(pauseButton.getText());
        assertTrue("The play button is not displayed", playButton.exists());
        assertTrue("The pause button is not displayed", pauseButton.exists());
    }

    @Test
    public void episodePlayingNotificationTest() throws UiObjectNotFoundException, InterruptedException {
        //Check if podcast is playing notification
        UiObject playButton = mDevice.findObject(new UiSelector().text("Play"));
        System.out.println(playButton.getText());
        UiObject pauseButton = mDevice.findObject(new UiSelector().text("Pause"));
        System.out.println(pauseButton.getText());
        playButton.click();
        mDevice.pressBack();
        mDevice.openNotification();
        UiObject playingMessageNotification = mDevice.findObject(new UiSelector().text("Podcast is playing, click to access player!"));
        assertTrue("The notification is not displayed",playingMessageNotification.waitForExists(5000));
        playingMessageNotification.clickAndWaitForNewWindow(5000);
        pauseButton.click();
        mDevice.pressBack();
        mDevice.openNotification();
        UiObject pauseMessageNotification = mDevice.findObject(new UiSelector().text("Podcast is paused, click to access player!"));
        assertTrue("The notification is not displayed", pauseMessageNotification.waitForExists(5000));
        pauseMessageNotification.clickAndWaitForNewWindow(5000);
        assertTrue("The play button is not displayed", playButton.waitForExists(3000));
        assertTrue("The pause button is not displayed", pauseButton.waitForExists(3000));
    }
}
