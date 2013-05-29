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
package com.playhaven.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/**
 * Entrypoint into the PlayHaven SDK
 */
public class PlayHaven
{
    /**
     * Tag to use for Logging
     *
     * @see android.util.Log#d
     */
    public static final String TAG = PlayHaven.class.getSimpleName();
    
    /**
     * String to use for scheme in Playhaven URIs 
     */
    public static final String URI_SCHEME = "playhaven";
    
    /**
     * Uri query parameter to launch placements from push notifications. 
     */
    public static final String ACTION_PLACEMENT = "placement";
    
    /**
     * Uri query parameter to launch Activities from push notifications. 
     */
    public static final String ACTION_ACTIVITY = "activity";

    /**
     * Shared Preferences file
     *
     * @see Context#getSharedPreferences(String, int)
     */
    private static final String SHARED_PREF_NAME = PlayHaven.class.getName();

    /**
     * Shared Preferences operating mode
     *
     * @see Context#getSharedPreferences(String, int)
     */
    private static final int SHARED_PREF_MODE = Context.MODE_PRIVATE;

    /**
     * Configuration parameter keys
     *
     * @see android.content.SharedPreferences.Editor#putString(String, String)
     */
    public enum Config
    {
        /**
         * Token as defined in the PlayHaven Dashboard
         *
         * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
         */
        Token,

        /**
         * Secret as defined in the PlayHaven Dashboard
         *
         * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
         */
        Secret,

        /**
         * PlayHaven API Server
         */
        APIServer,

        /**
         * The version of the PlayHaven Android SDK
         */
        SDKVersion,

        /**
         * The SDK Platform, if any.
         *
         * @see PlayHaven#setSDKPlatform(android.content.Context, String)
         */
        SDKPlatform,

        /**
         * Package of the Application
         */
        AppPkg,

        /**
         * Version of the Application
         */
        AppVersion,

        /**
         * Operating System Name
         */
        OSName,

        /**
         * Operating System version
         */
        OSVersion,

        /**
         * Device Identifer
         */
        DeviceId,
        /** 
         * Project identifier for GCM 
         */ 
        PushProjectId,
        /**
         * Device Model
         */
        DeviceModel,

        /**
         * MAC Address
         */
        MAC
    }

    /**
     * Connection type values
     */
    public enum ConnectionType {
        /**
         * The device is not connected to any network
         */
        NO_NETWORK,

        /**
         * The device is connected to a cellular network
         */
        MOBILE,

        /**
         * The device is connected to a WiFi network
         */
        WIFI,

        /**
         * The application does not have permission to read the network connection state
         */
        NO_PERMISSION
    }
    
    /**
     * Configure the token and secret for PlayHaven using String resources
     *
     * @param context of the application
     * @param tokenResourceId of the string resource containing the token configured in the PlayHaven Dashboard
     * @param secretResourceId of the string resource containing the secret configured in the PlayHaven Dashboard
     * @throws PlayHavenException if a problem occurs
     * @see Resources#getString(int)
     */
    public static void configure(final Context context, final int tokenResourceId, final int secretResourceId)
            throws PlayHavenException
    {
        final Resources resources = context.getResources();
        configure(context, resources.getString(tokenResourceId), resources.getString(secretResourceId), null);
    }
    
    /**
     * Configure the token and secret for PlayHaven using String resources
     *
     * @param context of the application
     * @param tokenResourceId of the string resource containing the token configured in the PlayHaven Dashboard
     * @param secretResourceId of the string resource containing the secret configured in the PlayHaven Dashboard
     * @param projectId for push notification settings 
     * @throws PlayHavenException if a problem occurs
     * @see Resources#getString(int)
     */
    public static void configure(final Context context, final int tokenResourceId, final int secretResourceId, final int projectId)
            throws PlayHavenException
    {
        final Resources resources = context.getResources();
        configure(context, resources.getString(tokenResourceId), resources.getString(secretResourceId), resources.getString(projectId));
    }

    /**
     * Validate that the specified value is not null/empty and can be represented as a hex character.
     * @param name of the value being tested (for the exception messages)
     * @param value to be tested
     * @throws PlayHavenException if validation fails
     */
    private static void validateHex(final String name, final String value)
            throws PlayHavenException
    {
        if(value == null) throw new PlayHavenException("%s must be set.", name);
        if(value.length() == 0) throw new PlayHavenException("%s must not be empty.", name);
        try{
            new BigInteger(value, 16);
        }catch(Exception e){
            throw new PlayHavenException(e, "%s must be a hex value", name);
        }
    }

    /**
     * Validate that the token is in the correct format
     *
     * @param token to validate
     * @throws PlayHavenException if validation fails
     */
    private static void validateToken(String token)
            throws PlayHavenException
    {
        /**
         * Real values are hex... some internal ones are not. Do we have a pattern for those?
         */
//        validateHex("Token", token);
    }

    /**
     * Validate that the secret is in the correct format
     *
     * @param secret to validate
     * @throws PlayHavenException if validation fails
     */
    private static void validateSecret(String secret)
            throws PlayHavenException
    {
        /**
         * Real values are hex... some internal ones are not. Do we have a pattern for those?
         */
//        validateHex("Secret", secret);
    }
    
    public static void configure(Context context, String token, String secret) throws PlayHavenException {
    	configure(context, token, secret, null);
    }

    /**
     * Configure PlayHaven
     *
     * @param context of the application
     * @param token configured in the PlayHaven Dashboard
     * @param secret configured in the PlayHaven Dashboard
     * @param projectId for push notification, optionally configured in PlayHaven Dashboard  
     * @throws PlayHavenException if a problem occurs
     * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
     */
    public static void configure(Context context, String token, String secret, String projectId)
            throws PlayHavenException
    {
        // Minimal validation
        validateToken(token);
        validateSecret(secret);

        // Setup default configuration values
        SharedPreferences.Editor editor = defaultConfiguration(context);

        // Setup PlayHaven values
        editor.putString(Config.Token.toString(), token);
        editor.putString(Config.Secret.toString(), secret);
        editor.putString(Config.APIServer.toString(), context.getResources().getString(R.string.playhaven_public_api_server));
        editor.putString(Config.SDKVersion.toString(), Version.PROJECT_VERSION);
        editor.putString(Config.SDKPlatform.toString(), "android");
        editor.putString(Config.PushProjectId.toString(), projectId);

        // Setup Publisher values
        String pkgName = context.getPackageName();
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new PlayHavenException("Unable to obtain package inforamtion", e);
        }
        // AndroidManifest.xml | <manifest package />
        editor.putString(Config.AppPkg.toString(), packageInfo.packageName);
        // AndroidManifest.xml | <manifest android:versionName />
        editor.putString(Config.AppVersion.toString(), packageInfo.versionName);

        // Setup Device values
        editor.putString(Config.OSName.toString(), Build.VERSION.RELEASE);
        editor.putInt(Config.OSVersion.toString(), Build.VERSION.SDK_INT);
        editor.putString(Config.DeviceId.toString(), new DeviceId(context).toString());
        editor.putString(Config.DeviceModel.toString(), Build.MODEL);

        // And commit it
        editor.commit();

        /**
         * This will generate a log that looks like this:
         * <pre>D/PlayHaven(  754): PlayHaven initialized: 2.0.0-SNAPSHOT-fe5c52f** 2012-11-21 08:45</pre>
         *
         * Which can then be used to find the actual commit the version was against:
         * <code>git log -n 1 fe5c52f</code>
         */
        i("PlayHaven initialized: %s", Version.BANNER);
        debugConfig(context);
    }

    /**
     * Configure PlayHaven
     *
     * @param context of the application
     * @param fileName property file containing the configuration parameters
     * @throws PlayHavenException if a problem occurs
     * @see PlayHaven.Config for configuration key names
     * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
     */
    public static void configure(Context context, String fileName) throws PlayHavenException
    {
        // Setup default configuration values
        SharedPreferences.Editor editor = defaultConfiguration(context);

        try{
            // Load the file
            Properties p = new Properties();

            if(Build.VERSION.SDK_INT >= 9)
            {
                p.load(new FileReader(fileName));
                for(String key : p.stringPropertyNames())
                {
                    Config param = Config.valueOf(key);
                    editor.putString(param.toString(), p.getProperty(key));
                }
            }else{
                // @playhaven.apihack API8 does not support load(Reader)
                p.load(new BufferedInputStream(new FileInputStream(fileName)));
                // @playhaven.apihack API8 support does not have stringPropertyNames
                Enumeration keys = p.propertyNames();
                while(keys.hasMoreElements())
                {
                    String key = (String)keys.nextElement();
                    Config param = Config.valueOf(key);
                    editor.putString(param.toString(), p.getProperty(key));
                }
            }

            // And commit it
            editor.commit();

            /**
             * This will generate a log that looks like this:
             * <pre>D/PlayHaven(  754): PlayHaven initialized: 2.0.0-SNAPSHOT-fe5c52f** 2012-11-21 08:45</pre>
             *
             * Which can then be used to find the actual commit the version was against:
             * <code>git log -n 1 fe5c52f</code>
             */
            i("PlayHaven initialized: %s", Version.BANNER);
            debugConfig(context);
        } catch (Exception e) {
            throw new PlayHavenException("Failed to configure PlayHaven", e);
        }
    }

    /**
     * Provide default configuration for PlayHaven
     *
     * @param context of the application
     * @return a SharedPreferences Editor for further configuration
     * @throws PlayHavenException if a problem occurs
     */
    private static SharedPreferences.Editor defaultConfiguration(Context context) throws PlayHavenException {
        // Application-wide configuration
        Context appContext = context.getApplicationContext();

        // PlayHaven specific configuration for this Application
        SharedPreferences pref = appContext.getSharedPreferences(SHARED_PREF_NAME, SHARED_PREF_MODE);
        SharedPreferences.Editor editor = pref.edit();

        // Setup PlayHaven values
        editor.putString(Config.APIServer.toString(), context.getResources().getString(R.string.playhaven_public_api_server));
        editor.putString(Config.SDKVersion.toString(), Version.PROJECT_VERSION);
        editor.putString(Config.SDKPlatform.toString(), "android");

        // Setup Publisher values
        String pkgName = context.getPackageName();
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new PlayHavenException("Unable to obtain package inforamtion", e);
        }
        // AndroidManifest.xml | <manifest package />
        editor.putString(Config.AppPkg.toString(), packageInfo.packageName);
        // AndroidManifest.xml | <manifest android:versionName />
        editor.putString(Config.AppVersion.toString(), packageInfo.versionName);

        // Setup Device values
        editor.putString(Config.OSName.toString(), Build.VERSION.RELEASE);
        editor.putInt(Config.OSVersion.toString(), Build.VERSION.SDK_INT);
        editor.putString(Config.DeviceId.toString(), new DeviceId(context).toString());
        editor.putString(Config.DeviceModel.toString(), Build.MODEL);

        // And commit it
        editor.commit();

        return editor;
    }

    /**
     * Used by wrappers (such as Unity and Air) to specify their platform in the logs
     *
     * @param platform to set
     */
    public static void setSDKPlatform(Context context, String platform) throws PlayHavenException {
        try{
            if(platform == null || platform.length() == 0)
                throw new PlayHavenException("Platform can not be null");

            // Application-wide configuration
            Context appContext = context.getApplicationContext();

            // PlayHaven specific configuration for this Application
            SharedPreferences pref = appContext.getSharedPreferences(SHARED_PREF_NAME, SHARED_PREF_MODE);
            SharedPreferences.Editor editor = pref.edit();

            /**
             * Per http://tools.ietf.org/html/rfc3986#section-2.3
             * unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
             */
            String replacePattern = "[^A-Za-z0-9\\-\\.\\_\\~]*";

            /**
             * Replace all invalid characters
             * This works because we are saying to replace all characters that don't match
             */
            platform = platform.replaceAll(replacePattern, "");

            if(platform == null || platform.length() == 0)
                throw new PlayHavenException("Platform has no valid characters in it");

            // Trim to size
            platform = platform.substring(0, Math.min(platform.length(), 42));

            // Set the property
            editor.putString(Config.SDKPlatform.toString(), platform);

            // And commit it
            editor.commit();

            i("PlayHaven platform set: %s", platform);
            debugConfig(context);
        } catch (Exception e) {
            throw new PlayHavenException("Failed to set platform", e);
        }
    }

    /**
     * Get the SharedPreferences used by PlayHaven
     *
     * @param context of the application
     * @return SharedPreferences used for configuration
     */
    public static SharedPreferences getPreferences(Context context)
    {
        // Application-wide configuration
        Context appContext = context.getApplicationContext();

        // PlayHaven specific configuration for this Application
        return appContext.getSharedPreferences(SHARED_PREF_NAME, SHARED_PREF_MODE);
    }

    /**
     * Log the configured key/value pairs to logcat using the DEBUG log level
     *
     * @param context of the application
     */
    private static void debugConfig(Context context)
    {
        // Application-wide configuration
        Context appContext = context.getApplicationContext();
        // PlayHaven specific configuration for this Application
        SharedPreferences pref = appContext.getSharedPreferences(SHARED_PREF_NAME, SHARED_PREF_MODE);
        Map<String, ?> map = pref.getAll();
        d("Configuration Parameters");
        for(String key : map.keySet())
        {
            d("%s: %s", key, map.get(key));
        }
    }

    /**
     * Names of the various log levels
     *
     * @see android.util.Log
     */
    private enum LogName
    {
        /** Suppression */
        SUPPRESS(-1),
        /** Verbose */
        VERBOSE(Log.VERBOSE),
        /** Debug */
        DEBUG(Log.DEBUG),
        /** Informational */
        INFO(Log.INFO),
        /** Warning */
        WARN(Log.WARN),
        /** Error */
        ERROR(Log.ERROR),
        /** Assertions */
        ASSERT(Log.ASSERT);
        LogName(int level)
        {
            this.level = level;
        }
        private int level;
    }

    /**
     * Set the log level to control the output to logcat
     *
     * @param logLevel to enable
     * @see LogName
     */
    public static void setLogLevel(String logLevel)
    {
        setLogLevel(LogName.valueOf(logLevel).level);
    }

    /**
     * Set the log level to control the output to logcat
     *
     * @param logLevel to enable
     * @see android.util.Log
     */
    public static void setLogLevel(int logLevel)
    {
        System.setProperty(TAG, ""+logLevel);
    }

    /**
     * Checks whether the log level is enabled
     *
     * @param logLevel to check
     * @see android.util.Log
     */
    public static  boolean isLoggable(int logLevel)
    {
        if(Log.isLoggable(TAG, logLevel))
            return true;

        int stored = Integer.parseInt(System.getProperty(TAG, ""+Log.INFO));
        return (stored <= logLevel);
    }

    /**
     * Logs using the VERBOSE log level, if enabled.
     *
     * @param t cause
     * @param fmt format of the message
     * @param args to populate the format
     * @see android.util.Log#VERBOSE
     * @see PlayHaven#isLoggable
     * @see String#format(String, Object...)
     */
    public static void v(Throwable t, String fmt, Object ... args)
    {
        if(isLoggable(Log.VERBOSE))
            Log.v(TAG, String.format(fmt, args), t);
    }

    /**
     * Logs using the VERBOSE log level, if enabled.
     *
     * @param fmt format of the message
     * @param args to populate the format
     * @see android.util.Log#VERBOSE
     * @see PlayHaven#isLoggable
     * @see String#format(String, Object...)
     */
    public static void v(String fmt, Object ... args)
    {
        if(isLoggable(Log.VERBOSE))
            Log.v(TAG, String.format(fmt, args));
    }

    /**
     * Logs using the DEBUG log level, if enabled.
     *
     * @param t cause
     * @param fmt format of the message
     * @param args to populate the format
     * @see android.util.Log#DEBUG
     * @see PlayHaven#isLoggable
     * @see String#format(String, Object...)
     */
    public static void d(Throwable t, String fmt, Object ... args)
    {
        if(isLoggable(Log.DEBUG))
            Log.d(TAG, String.format(fmt, args), t);
    }

    /**
     * Logs using the DEBUG log level, if enabled.
     *
     * @param fmt format of the message
     * @param args to populate the format
     * @see android.util.Log#DEBUG
     * @see PlayHaven#isLoggable
     * @see String#format(String, Object...)
     */
    public static void d(String fmt, Object ... args)
    {
        if(isLoggable(Log.DEBUG))
            Log.d(TAG, String.format(fmt, args));
    }

    /**
     * Logs using the INFO log level, if enabled.
     *
     * @param t cause
     * @param fmt format of the message
     * @param args to populate the format
     * @see android.util.Log#INFO
     * @see PlayHaven#isLoggable
     * @see String#format(String, Object...)
     */
    public static void i(Throwable t, String fmt, Object ... args)
    {
        if(isLoggable(Log.INFO))
            Log.i(TAG, String.format(fmt, args), t);
    }

    /**
     * Logs using the INFO log level, if enabled.
     *
     * @param fmt format of the message
     * @param args to populate the format
     * @see android.util.Log#INFO
     * @see PlayHaven#isLoggable
     * @see String#format(String, Object...)
     */
    public static void i(String fmt, Object ... args)
    {
        if(isLoggable(Log.INFO))
            Log.i(TAG, String.format(fmt, args));
    }

    /**
     * Logs using the WARN log level, if enabled.
     *
     * @param t cause
     * @see android.util.Log#WARN
     * @see PlayHaven#isLoggable
     */
    public static void w(Throwable t)
    {
        if(isLoggable(Log.WARN))
            Log.w(TAG, t.getMessage(), t);
    }

    /**
     * Logs using the WARN log level, if enabled.
     *
     * @param t cause
     * @param fmt format of the message
     * @param args to populate the format
     * @see android.util.Log#WARN
     * @see PlayHaven#isLoggable
     * @see String#format(String, Object...)
     */
    public static void w(Throwable t, String fmt, Object ... args)
    {
        if(isLoggable(Log.WARN))
            Log.w(TAG, String.format(fmt, args), t);
    }

    /**
     * Logs using the WARN log level, if enabled.
     *
     * @param fmt format of the message
     * @param args to populate the format
     * @see android.util.Log#WARN
     * @see PlayHaven#isLoggable
     * @see String#format(String, Object...)
     */
    public static void w(String fmt, Object ... args)
    {
        if(isLoggable(Log.WARN))
            Log.w(TAG, String.format(fmt, args));
    }

    /**
     * Logs using the ERROR log level, if enabled.
     *
     * @param t cause
     * @see android.util.Log#ERROR
     * @see PlayHaven#isLoggable
     */
    public static void e(Throwable t)
    {
        if(isLoggable(Log.ERROR))
            Log.e(TAG, t.getMessage(), t);
    }

    /**
     * Logs using the ERROR log level, if enabled.
     *
     * @param fmt format of the message
     * @param args to populate the format
     * @see android.util.Log#ERROR
     * @see PlayHaven#isLoggable
     * @see String#format(String, Object...)
     */
    public static void e(String fmt, Object ... args)
    {
        if(isLoggable(Log.ERROR))
            Log.e(TAG, String.format(fmt, args));
    }

    /**
     * Logs using the ERROR log level, if enabled.
     *
     * @param t cause
     * @param fmt format of the message
     * @param args to populate the format
     * @see android.util.Log#ERROR
     * @see PlayHaven#isLoggable
     * @see String#format(String, Object...)
     */
    public static void e(Throwable t, String fmt, Object ... args)
    {
        if(isLoggable(Log.ERROR))
            Log.e(TAG, String.format(fmt, args), t);
    }

}
