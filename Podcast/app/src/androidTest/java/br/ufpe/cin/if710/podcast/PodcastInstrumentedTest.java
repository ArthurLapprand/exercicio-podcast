package br.ufpe.cin.if710.podcast;

/**
 * Created by danil on 08/12/2017.
 */

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
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
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PodcastInstrumentedTest {
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

    public void downloadEpisode() throws UiObjectNotFoundException {
        UiScrollable listPodcast = new UiScrollable(new UiSelector().className(ListView.class.getName()));
        UiObject downloadButtonEpisode = null;
        UiObject linearLayoutOuter = null;
        UiObject linearLayoutInner = null;
        int i = 0;
        do {
            for (i = 0; i < listPodcast.getChildCount(); i++) {
                linearLayoutOuter = listPodcast.getChild(new UiSelector().index(i));
                linearLayoutInner = linearLayoutOuter.getChild(new UiSelector().index(0));
                try {
                    downloadButtonEpisode = linearLayoutInner.getChild(new UiSelector().index(1));
                    if(downloadButtonEpisode.getContentDescription().equals("toDownloadState")){
                        break;
                    }
                } catch (UiObjectNotFoundException e) {
                    continue;
                }
            }
        }while(!downloadButtonEpisode.exists() && listPodcast.scrollForward());
        assertTrue("Did not found a download button", downloadButtonEpisode.exists());
        downloadButtonEpisode.click();
        assertFalse("The button still exists after clicking at it", downloadButtonEpisode.waitForExists(300));
        //Getting the altered button to check if it is downloading
        UiObject downloadingButtonEpisode = null;
        downloadingButtonEpisode = linearLayoutInner.getChild(new UiSelector().resourceId("item_action").description("downloadingState"));
        assertTrue("The episode is not being downloaded", downloadingButtonEpisode.exists());
        //Waiting for the download to happen
        UiObject playButtonEpisode = linearLayoutInner.getChild(new UiSelector().resourceId("item_action").description("playState"));
        assertTrue("The episode is not playable", playButtonEpisode.waitForExists(3*60*1000));
    }
}
