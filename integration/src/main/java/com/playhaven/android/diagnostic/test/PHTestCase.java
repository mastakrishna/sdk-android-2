/**
 * Copyright 2013 Medium Entertainment, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.playhaven.android.diagnostic.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.github.rtyley.android.screenshot.celebrity.Screenshots.poseForScreenshotNamed;
import static java.lang.Thread.sleep;

/**
 * Base class for instrumentation testing
 */
public class PHTestCase<ACTIVITY extends Activity>
extends ActivityInstrumentationTestCase2<ACTIVITY>
{
    protected String TAG = getClass().getSimpleName();

    /**
     * ActivityInstrumentationTestCase2/JUnit doesn't work with assertions run in a background thread.
     */
    protected ConcurrentHashMap<String,CountDownLatch> latches;

    public String getTag(){return TAG;}

    private Class<ACTIVITY> activityClass;

    public PHTestCase(Class<ACTIVITY> activityClass)
    {
        super(activityClass);
        this.activityClass = activityClass;
        latches = new ConcurrentHashMap<String, CountDownLatch>();
    }

    protected ACTIVITY doActivityTestSetup() throws InterruptedException
    {
        ACTIVITY activity = getSyncdActivity();
        sleep(250);
        return activity;
    }

    protected ACTIVITY getSyncdActivity(){
        return startActivitySync(activityClass);
    }

    protected Context getTargetContext()
    {
        return getInstrumentation().getTargetContext();
    }

    protected Resources getTargetResources()
    {
        return getTargetContext().getResources();
    }

    protected Context getInstrumentationContext()
    {
        return getInstrumentation().getContext();
    }

    protected Resources getInstrumentationResources()
    {
        return getInstrumentationContext().getResources();
    }

    protected void enableThreadedTesting(Object toTest)
    {
        latches.put(toTest.getClass().getSimpleName(), new CountDownLatch(1));
    }

    protected void waitForReady(Object toTest)
    {
        CountDownLatch latch = latches.get(toTest.getClass().getSimpleName());
        if(latch != null)
        {
            try {
                latch.await();
            } catch (InterruptedException e) {
                /* no-op */
            }
        }
    }

    protected SharedPreferences.Editor configurePlayHaven() throws PlayHavenException {
        return configurePlayHaven(R.string.instrumentation_token, R.string.instrumentation_secret);
    }

    protected SharedPreferences.Editor configurePlayHaven(int tokenResId, int secretResId) throws PlayHavenException {
        // Get the resources from our own strings.xml
        Context iCtx = getInstrumentationContext();
        Resources res = iCtx.getResources();
        String token = res.getString(tokenResId);
        String secret = res.getString(secretResId);

        // But do all the work in the target context
        Context tCtx = getTargetContext();

        // First clear any values from old tests
        SharedPreferences pref = PlayHaven.getPreferences(tCtx);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();

        // Now configure....
        PlayHaven.configure(tCtx, token, secret);
        return PlayHaven.getPreferences(tCtx).edit();
    }

    public void markReadyForTesting(Object toTest)
    {
        String name = toTest.getClass().getSimpleName();
        CountDownLatch latch = latches.get(name);
        if(latch != null)
        {
            latch.countDown();
            latches.put(name, latch);
        }
    }

    protected void screenshot(String name)
    {
        poseForScreenshotNamed(TAG + ": " + name);
    }

    @SuppressWarnings("unchecked")
    protected ACTIVITY startActivitySync(Class<ACTIVITY> clazz) {
        Intent intent = new Intent(getInstrumentation().getTargetContext(), clazz);
        intent.setFlags(intent.getFlags() | FLAG_ACTIVITY_NEW_TASK);
        return (ACTIVITY) getInstrumentation().startActivitySync(intent);
    }

    protected String getJSON(int rawResource) throws IOException {
        return getJSON(rawResource, false);
    }

    protected String getJSON(int rawResource, boolean useTargetContext) throws IOException {
        Context ctx = (useTargetContext ? getInstrumentation().getTargetContext() : getInstrumentation().getContext());
        return IOUtils.toString(ctx.getResources().openRawResource(rawResource));
    }

    protected byte[] readFile(File file) throws IOException {
        Log.d(TAG, "reading file: " + file.getAbsolutePath());
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        byte[] buf = new byte[1024];
        int len;
        while( (len = in.read(buf)) != -1)
            out.write(buf, 0, len);

        out.flush();
        buf = out.toByteArray();
        in.close();
        out.close();
        return buf;
    }
}
