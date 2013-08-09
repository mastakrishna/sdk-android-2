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
package com.playhaven.android.req;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.Version;
import com.playhaven.android.compat.VendorCompat;
import com.playhaven.android.data.DataboundMapper;
import com.playhaven.android.req.model.ClientApiResponseModel;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.UUID;

import static com.playhaven.android.PlayHaven.Config.*;

/**
 * Base class for making requests to the server
 */
public abstract class PlayHavenRequest
{
    protected static final String UTF8 = "UTF-8";
    protected static final String HMAC = "HmacSHA1";

    /**
     * REST handler
     */
    protected RestTemplate rest;

    /**
     * Handler for server response
     */
    private RequestListener handler;

    /**
     * Signature verification
     */
    private Mac sigMac;

    protected PlayHavenRequest()
    {
        // Add the gzip Accept-Encoding header
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAcceptEncoding(ContentCodingType.GZIP);
        requestHeaders.setAccept(Collections.singletonList(new MediaType("application","json")));
        HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

        // Create our REST handler
        rest = new RestTemplate();
        rest.setErrorHandler(new ServerErrorHandler());

        // Capture the JSON for signature verification
        rest.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName(UTF8)));
    }

    /**
     * Set the REST method to use
     *
     * @return method to use
     */
    protected HttpMethod getMethod()
    {
        return HttpMethod.GET;
    }

    protected HttpHeaders getHeaders()
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(new MediaType("application", "json")));
        headers.setUserAgent(UserAgent.USER_AGENT);
        return headers;
    }

    protected HttpEntity<?> getEntity()
    {
        return new HttpEntity<Object>(getHeaders());
    }

    protected int getApiPath(Context context)
    {
        return -1;
    }

    protected String getString(SharedPreferences pref, PlayHaven.Config param)
    {
        return getString(pref, param, "unknown");
    }

    protected String getString(SharedPreferences pref, PlayHaven.Config param, String defaultValue)
    {
        return pref.getString(param.toString(), defaultValue);
    }

    protected Integer getInt(SharedPreferences pref, PlayHaven.Config param, int defaultValue)
    {
        return pref.getInt(param.toString(), defaultValue);
    }

    @SuppressWarnings("deprecation")
    protected UriComponentsBuilder createUrl(Context context) throws PlayHavenException {
        try{
            SharedPreferences pref = PlayHaven.getPreferences(context);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getString(pref, APIServer));
            builder.path(context.getResources().getString(getApiPath(context)));
            builder.queryParam("app", getString(pref, AppPkg));
            builder.queryParam("app_version", getString(pref, AppVersion));
            builder.queryParam("os", getInt(pref, OSVersion, 0));
            WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            builder.queryParam("orientation", display.getRotation());
            builder.queryParam("hardware", getString(pref, DeviceModel));
            PlayHaven.ConnectionType connectionType = getConnectionType(context);
            builder.queryParam("connection", connectionType.ordinal());
            String mac = getString(pref, MAC, null);
            if(mac == null)
                mac = getMacAddress(context, connectionType);

            if(mac != null)
            {
                mac = mac.toLowerCase().replaceAll("[^a-z0-9]", "");
                builder.queryParam("mac", mac);
            }
            builder.queryParam("idiom", context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);

            /**
             * For height/width we will use getSize(Point) not getRealSize(Point) as this will allow us to automatically
             * account for rotation and screen decorations like the status bar. We only want to know available space.
             *
             * @playhaven.apihack for SDK_INT < 13, have to use getHeight and getWidth!
             */
            Point size = new Point();
            if(Build.VERSION.SDK_INT >= 13)
            {
                display.getSize(size);
            }else{
                size.x = display.getWidth();
                size.y = display.getHeight();
            }
            builder.queryParam("width", size.x);
            builder.queryParam("height", size.y);


            /**
             * SDK Version needs to be reported as a dotted numeric value
             * So, if it is a -SNAPSHOT build, we will replace -SNAPSHOT with the date of the build
             * IE: 2.0.0.20130201
             * as opposed to an actual released build, which would be like 2.0.0
             */
            String sdkVersion = getString(pref, SDKVersion);
            String[] date = Version.PLUGIN_BUILD_TIME.split("[\\s]");
            sdkVersion = sdkVersion.replace("-SNAPSHOT", "." + date[0].replaceAll("-",""));
            builder.queryParam("sdk_version", sdkVersion);


            builder.queryParam("plugin", getString(pref, PluginIdentifer));
            builder.queryParam("languages", context.getResources().getConfiguration().locale.getLanguage());
            builder.queryParam("token", getString(pref, Token));

            builder.queryParam("device", getString(pref, DeviceId));
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            builder.queryParam("dpi", metrics.densityDpi);

            String uuid = UUID.randomUUID().toString();
            String nonce = base64Digest(uuid);
            builder.queryParam("nonce", nonce);

            addSignature(builder, pref, nonce, mac);

            // Setup for signature verification
            String secret = getString(pref, Secret);
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(UTF8), HMAC);
            sigMac = Mac.getInstance(HMAC);
            sigMac.init(key);
            sigMac.update(nonce.getBytes(UTF8));

            return builder;
        }catch(Exception e){
            throw new PlayHavenException(e);
        }
    }

    protected void addSignature(UriComponentsBuilder builder, SharedPreferences pref, String nonce, String mac) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        addV3Signature(builder, pref, nonce, mac);
    }

    protected void addV3Signature(UriComponentsBuilder builder, SharedPreferences pref, String nonce, String mac) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        builder.queryParam("signature", hexDigest(
            concat(":",
                getString(pref, Token),
                getString(pref, DeviceId),
                nonce,
                getString(pref, Secret)
            )
        ));
    }

    protected void addV4Signature(UriComponentsBuilder builder, SharedPreferences pref, String nonce, String mac) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        // Malachi - I don't think this is right since the secret is not included...?
        builder.queryParam("sig4", base64Digest(
            concat(":",
                nonce,
                getString(pref, Token),
                getString(pref, DeviceId),
                // ifa,
                mac
                // odin
            )
        ));
    }

    protected String concat(String delim, String ... data)
    {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for(String d : data)
        {
            if(d == null) continue;

            if(!first) sb.append(":");
            else first = false;

            sb.append(d);
        }
        return sb.toString();
    }

    protected static PlayHaven.ConnectionType getConnectionType(Context context) {
        try {
            ConnectivityManager manager	= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (manager == null)
                return PlayHaven.ConnectionType.NO_NETWORK; // happens during tests

            NetworkInfo wifiInfo   = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if(wifiInfo != null)
            {
                NetworkInfo.State wifi = wifiInfo.getState();
                if(wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING)
                    return PlayHaven.ConnectionType.WIFI;
            }

            NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if(mobileInfo != null)
            {
                NetworkInfo.State mobile = mobileInfo.getState();
                if(mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING)
                    return PlayHaven.ConnectionType.MOBILE;
            }
        } catch (SecurityException e) {
            // ACCESS_NETWORK_STATE permission not granted in the manifest
            return PlayHaven.ConnectionType.NO_PERMISSION;
        }

        return PlayHaven.ConnectionType.NO_NETWORK;
    }

    protected static String getMacAddress(Context context) {
        return getMacAddress(context, getConnectionType(context));
    }

    protected static String getMacAddress(Context context, PlayHaven.ConnectionType connectionType) {
        switch(connectionType)
        {
            case WIFI:
                try{
                    WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo info = manager.getConnectionInfo();
                    return info.getMacAddress();
                }catch(SecurityException e){
                    PlayHaven.d(e, "Error obtaining mac address");
                    return null;
                }
            default:
                return null;
        }
    }

    protected static String convertToHex(byte[] in) {
        StringBuilder builder = new StringBuilder(in.length*2);

        Formatter formatter = new Formatter(builder);
        for (byte inByte : in)
            formatter.format("%02x", inByte);


        return builder.toString();
    }

    /** First encrypts with SHA1 and then spits the result out as a hex string*/
    protected static String hexDigest(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return convertToHex(dataDigest(input));
    }

    /** First encrypt with SHA1 then convert to Base64*/
    protected static String base64Digest(String input) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        String b64digest = convertToBase64(dataDigest(input));

        // Trim off last character for v1.x compatibility
        return b64digest.substring(0, b64digest.length() - 1);
    }

    protected static String convertToBase64(byte[] in) throws UnsupportedEncodingException {
        if (in == null) return null;

        return new String(Base64.encode(in, Base64.URL_SAFE | Base64.NO_PADDING), "UTF8");
    }

    protected static byte[] dataDigest(String in) throws NoSuchAlgorithmException, UnsupportedEncodingException{
        if (in == null) return null;

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return 		  md.digest(in.getBytes("UTF8"));
    }

    protected void validateSignature(String xPhDigest, String json) throws PlayHavenException {
        if(sigMac == null) return;

        // If we did not get a signature from the server, don't validate it
        if(xPhDigest == null) return;

        try{
            sigMac.update(json.getBytes(UTF8));
            byte[] bytes = sigMac.doFinal();
            String derived = new String(Base64.encode(bytes, Base64.URL_SAFE), UTF8).trim();
            if(!xPhDigest.equals(derived))
                throw new PlayHavenException("Invalid signature.");
        } catch (UnsupportedEncodingException e) {
            throw new PlayHavenException("Error decoding signature", e);
        }
    }

    public void send(final Context context)
    {
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    DataboundMapper mapper = new DataboundMapper();

                    /**
                     * First, check if we are mocking the URL
                     */
                    String mockJsonResponse = getMockJsonResponse();
                    if(mockJsonResponse != null)
                    {
                        /**
                         * Mock the response
                         */
                        PlayHaven.v("Mock Response: %s", mockJsonResponse);
                        ClientApiResponseModel model = mapper.readValue(mockJsonResponse, ClientApiResponseModel.class);
                        handleResponse(model);
                        return;
                    }

                    /**
                     * Not mocking the response. Do an actual server call.
                     */
                    String url = getUrl(context);
                    PlayHaven.v("Request(%s): %s", getClass().getSimpleName(), url);

                    ResponseEntity<String> entity = rest.getForEntity(url, String.class);
                    String json = entity.getBody();

                    List<String> digests = entity.getHeaders().get("X-PH-DIGEST");
                    String digest = (digests == null || digests.size() == 0) ? null : digests.get(0);

                    validateSignature(digest, json);

                    HttpStatus statusCode = entity.getStatusCode();
                    PlayHaven.v("Response (%s): %s",
                        statusCode,
                        json
                    );

                    ClientApiResponseModel responseModel = mapper.readValue(json, ClientApiResponseModel.class);

                    serverSuccess(context);
                    handleResponse(responseModel);
                }catch(PlayHavenException e){
                    handleResponse(e);
                }catch(IOException e2){
                    handleResponse(new PlayHavenException(e2));
                }catch(Exception e3){
                    handleResponse(new PlayHavenException(e3.getMessage()));
                }
            }
        }).start();
    }

    protected void serverSuccess(Context context)
    {

    }

    public void setResponseHandler(RequestListener handler)
    {
        this.handler = handler;
    }

    public RequestListener getResponseHandler()
    {
        return handler;
    }

    protected void handleResponse(ClientApiResponseModel model)
    {
        if(handler != null)
            handler.handleResponse(model);
    }

    protected void handleResponse(PlayHavenException e)
    {
        PlayHaven.e("Error calling server: %s", e.getMessage());
        if(handler != null)
            handler.handleResponse(e);
    }

    /**
     * To actually call the server, but with a mock URL - return it here
     *
     * @param context of the request
     * @return the url to call
     * @throws PlayHavenException if there is a problem
     */
    protected String getUrl(Context context) throws PlayHavenException {
        return createUrl(context).build().encode().toUriString();
    }

    /**
     * To pretend to call the server, and return a mock result - return the JSON result here
     * Note: This is used for testing
     *
     * @return json result
     */
    protected String getMockJsonResponse()
    {
        return null;
    }

    protected VendorCompat getCompat(Context context){return PlayHaven.getVendorCompat(context);}
}
