package com.playhaven.android.compat;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.R;

/**
 * Created by malachi on 6/27/13.
 */
public class VendorCompat
{
    public enum Resource
    {
        com_playhaven_android_view_PlayHavenView,
        com_playhaven_android_view_PlayHavenView_placementTag,
        com_playhaven_android_view_PlayHavenView_displayOptions,
        com_playhaven_android_view_Badge,
        com_playhaven_android_view_Badge_placementTag,
        com_playhaven_android_view_Badge_textColor,
        playhaven_dialog,
        playhaven_dialog_view,
        playhaven_badge,
        playhaven_overlay,
        com_playhaven_android_view_Overlay,
        playhaven_loadinganim,
        com_playhaven_android_view_LoadingAnimation,
        playhaven_exit,
        com_playhaven_android_view_Exit,
        com_playhaven_android_view_Exit_button,
        playhaven_activity,
        playhaven_activity_view
    }

    public enum ResourceType {
        string,
        layout,
        id,
        styleable,
        drawable,
        attr
    }

    private String vendorId;

    public VendorCompat(String vendorId)
    {
        if(vendorId != null || vendorId.length() > 0)
        {
            /**
             * Per http://tools.ietf.org/html/rfc3986#section-2.3
             * unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
             */
            String replacePattern = "[^A-Za-z0-9\\-\\.\\_\\~]*";

            /**
             * Replace all invalid characters
             * This works because we are saying to replace all characters that don't match
             */
            this.vendorId = vendorId.replaceAll(replacePattern, "");

        }

        if(this.vendorId == null || this.vendorId.length() == 0)
        {
            PlayHaven.v("vendorId has no valid characters in it. Using default.");
            this.vendorId = getClass().getSimpleName();
        }

        // Trim to size
        this.vendorId = this.vendorId.substring(0, Math.min(this.vendorId.length(), 42));
    }

    public String getVendorId(){return vendorId;}

    /**
     * @param context
     * @param type the ResourceType wanted
     * @param name the name of the wanted resource
     * @return the resource id for a given resource
     */
    public int getResourceId(Context context, ResourceType type, String name)
    {
        return context.getResources().getIdentifier(name, type.name(), context.getPackageName());
    }

    public int getResourceId(Context context, ResourceType type, Resource resource)
    {
        /**
         * Unity needs to look up by string name
         * If not Unity, this will give us better performance
         */
        switch(resource)
        {
            case com_playhaven_android_view_Exit:
                return R.id.com_playhaven_android_view_Exit;
            case com_playhaven_android_view_Exit_button:
                return R.id.com_playhaven_android_view_Exit_button;
            case com_playhaven_android_view_Overlay:
                return R.id.com_playhaven_android_view_Overlay;
            case com_playhaven_android_view_LoadingAnimation:
                return R.id.com_playhaven_android_view_LoadingAnimation;
            case com_playhaven_android_view_PlayHavenView_displayOptions:
                return R.styleable.com_playhaven_android_view_PlayHavenView_displayOptions;
            case com_playhaven_android_view_PlayHavenView_placementTag:
                return R.styleable.com_playhaven_android_view_PlayHavenView_placementTag;
            case com_playhaven_android_view_Badge_placementTag:
                return R.styleable.com_playhaven_android_view_Badge_placementTag;
            case com_playhaven_android_view_Badge_textColor:
                return R.styleable.com_playhaven_android_view_Badge_textColor;
            default:
                return getResourceId(context, type, resource.name());
        }
    }

    public TypedArray obtainStyledAttributes(Context context, AttributeSet attrs, Resource resource)
    {
        switch(resource)
        {
            case com_playhaven_android_view_Badge:
                return context.obtainStyledAttributes(attrs, R.styleable.com_playhaven_android_view_Badge, 0, 0);
            case com_playhaven_android_view_PlayHavenView:
                return context.obtainStyledAttributes(attrs, R.styleable.com_playhaven_android_view_PlayHavenView, 0, 0);
            default:
                return null;
        }
    }

}
