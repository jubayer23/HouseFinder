
package com.creative.housefinder.model;

import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Houses implements Parcelable
{

    @SerializedName("houses")
    @Expose
    private List<House> houses = null;
    public final static Creator<Houses> CREATOR = new Creator<Houses>() {


        @SuppressWarnings({
            "unchecked"
        })
        public Houses createFromParcel(Parcel in) {
            return new Houses(in);
        }

        public Houses[] newArray(int size) {
            return (new Houses[size]);
        }

    }
    ;

    protected Houses(Parcel in) {
        in.readList(this.houses, (com.creative.housefinder.model.House.class.getClassLoader()));
    }

    public Houses() {
    }

    public List<House> getHouses() {
        return houses;
    }

    public void setHouses(List<House> houses) {
        this.houses = houses;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(houses);
    }

    public int describeContents() {
        return  0;
    }

}
