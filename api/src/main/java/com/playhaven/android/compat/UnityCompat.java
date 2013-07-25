package com.playhaven.android.compat;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;

import java.lang.reflect.Field;

/**
 * Created by malachi on 6/27/13.
 */
public class UnityCompat
    extends VendorCompat
{
    public UnityCompat(String vendorId)
    {
        super(vendorId);
    }

//    private static final String r_styleable = "R.styleable";
    private static final String ph_r_styleable = "com.playhaven.android.R$styleable";

    public TypedArray obtainStyledAttributes(Context context, AttributeSet attrs, Resource resource)
    {
        switch(resource)
        {
            case com_playhaven_android_view_Badge:
            case com_playhaven_android_view_PlayHavenView:
                return context.obtainStyledAttributes(attrs, getResourceStyleableArray(resource.name()), 0, 0);
            default:
                return null;
        }
    }


    public int getResourceId(Context context, ResourceType type, Resource resource)
    {
        return context.getResources().getIdentifier(resource.name(), type.name(), context.getPackageName());
    }

    /**                                                                                                                               ul
     * Needed to allow wrapping with Unity 4.1.5 and below.
     * @param name the name of the styleable to parse
     * @return the attrs identifiers of a declare-styleable element, or an empty array
     */
    private int[] getResourceStyleableArray(String name)
    {

        try {
            Field field = Class.forName(ph_r_styleable).getField(name);
            return (int[])field.get(null);
        } catch (Exception e) {
            PlayHaven.e(e);
        }
        return new int[0];
    }

}
