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
package com.playhaven.android.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.playhaven.android.req.model.RewardParam;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * A representation of a Reward
 */
public class Reward
        implements Parcelable
{
    /**
     * Construct a list of Reward objects from the JSON databound model
     *
     * @param param from the json model
     * @return a list of Reward objects
     */
    public static ArrayList<Reward> fromParameters(RewardParam param)
    {
        if(param == null) return new ArrayList<Reward>(0);

        List<com.playhaven.android.req.model.Reward> fromJson = param.getRewards();
        if(fromJson == null || fromJson.size() == 0) return new ArrayList<Reward>(0);

        ArrayList<Reward> toReturn = new ArrayList<Reward>(fromJson.size());
        for(com.playhaven.android.req.model.Reward jsonReward : fromJson)
            toReturn.add(new Reward(jsonReward));

        return toReturn;
    }

    private static final String NULL = "null";
    private Double quantity;
    private Double receipt;
    private String tag;
    private String signature;

    /**
     * Construct a Reward from the JSON databound model
     *
     * @param reward from the json model
     */
    public Reward(com.playhaven.android.req.model.Reward reward)
    {
        this.quantity = reward.getQuantity();
        this.receipt = reward.getReceipt();
        this.tag = reward.getReward();
        this.signature = reward.getSignature();
    }

    /**
     * Construct a Reward from a serialized Parcel
     *
     * @param in serialized parcel
     */
    public Reward(Parcel in)
    {
        readFromParcel(in);
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written. May be 0 or Parcel#PARCELABLE_WRITE_RETURN_VALUE.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(quantity);
        dest.writeDouble(receipt);
        dest.writeString(tag);
        dest.writeString(signature);
    }

    /**
     * Deserialize this object from a Parcel
     *
     * @param in parcel to read from
     */
    protected void readFromParcel(Parcel in)
    {
        quantity = in.readDouble();
        receipt = in.readDouble();
        tag = in.readString();
        signature = in.readString();
    }

    /**
     * Required Android annoyance
     */
    public static final Parcelable.Creator<Reward> CREATOR = new Creator<Reward>()
    {
        public Reward createFromParcel(Parcel in){return new Reward(in);}
        public Reward[] newArray(int size){return new Reward[size];}
    };

    public Double getQuantity() {
        return quantity;
    }

    public Double getReceipt() {
        return receipt;
    }

    public String getTag() {
        return tag;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }
}
