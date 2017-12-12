package br.ufpe.cin.if710.podcast;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.widget.Button;
import android.widget.ListView;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by danil on 12/12/2017.
 */
@RunWith(AndroidJUnit4.class)
public class DownloadEpisodeTest {

    private static final String APP_PACKAGE = "br.ufpe.cin.if710.podcast";
    private static final int LAUNCH_TIMEOUT = 5000;
    private UiDevice mDevice;

    @BeforeClass
    public void startMainActivityFromHomeScreen() {
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
    }

    @Test
    public void downloadEpisodeTest() throws UiObjectNotFoundException, InterruptedException {
        UiScrollable listPodcast = (UiScrollable) mDevice.findObject(new UiSelector().className(ListView.class.getName()));
        UiObject downloadButtonEpisode = null;
        UiObject linearLayoutOuter = null;
        UiObject linearLayoutInner = null;
        int i = 0;
        do {
            for (i = 0; i < listPodcast.getChildCount(); i++) {
                linearLayoutOuter = listPodcast.getChild(new UiSelector().index(i));
                linearLayoutInner = linearLayoutOuter.getChild(new UiSelector().index(0));
                try {
                    downloadButtonEpisode = linearLayoutInner.getChild(new UiSelector().className(Button.class.getName()).resourceId("item_action").descriptionContains("toDownloadState"));
                    if (downloadButtonEpisode.exists()) {
                        System.out.println("Found download button at position: " + i);
                        break;
                    }
                } catch (UiObjectNotFoundException e) {
                    continue;
                }
            }
        }while(downloadButtonEpisode == null && listPodcast.scrollForward());
        assertNotNull("Did not found a download button", downloadButtonEpisode);
        downloadButtonEpisode.click();
        wait(500);
        assertFalse("The button still exists after clicking at it", downloadButtonEpisode.exists());
        //Getting the altered button to check if it is downloading
        UiObject downloadingButtonEpisode = null;
        downloadingButtonEpisode = linearLayoutInner.getChild(new UiSelector().className(Button.class.getName()).resourceId("item_action").descriptionContains("downloadingState"));
        assertTrue("The episode is not being downloaded", downloadingButtonEpisode.exists());
        //Waiting for the download to happen
        wait(6 * 60 * 1000);
        UiObject playButtonEpisode = linearLayoutInner.getChild(new UiSelector().className(Button.class.getName()).resourceId("item_action").description("playState"));
        assertTrue("The episode is not playable", playButtonEpisode.exists());
    }
}
