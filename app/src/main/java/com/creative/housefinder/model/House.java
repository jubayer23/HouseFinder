
package com.creative.housefinder.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class House implements Parcelable
{

    @SerializedName("Unit #")
    @Expose
    private String unit;
    @SerializedName("House #")
    @Expose
    private Integer house;
    @SerializedName("Street Name")
    @Expose
    private String streetName;
    @SerializedName("City")
    @Expose
    private String city;
    @SerializedName("Prov")
    @Expose
    private String prov;
    @SerializedName("Postal Code")
    @Expose
    private String postalCode;
    @SerializedName("Longitude")
    @Expose
    private Double longitude;
    @SerializedName("Latitude")
    @Expose
    private Double latitude;
    @SerializedName("Address Text")
    @Expose
    private String addressText;

    private double distance;



    public House() {
    }

    protected House(Parcel in) {
        unit = in.readString();
        streetName = in.readString();
        city = in.readString();
        prov = in.readString();
        postalCode = in.readString();
        addressText = in.readString();
        distance = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(unit);
        dest.writeString(streetName);
        dest.writeString(city);
        dest.writeString(prov);
        dest.writeString(postalCode);
        dest.writeString(addressText);
        dest.writeDouble(distance);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<House> CREATOR = new Creator<House>() {
        @Override
        public House createFromParcel(Parcel in) {
            return new House(in);
        }

        @Override
        public House[] newArray(int size) {
            return new House[size];
        }
    };

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getHouse() {
        return house;
    }

    public void setHouse(Integer house) {
        this.house = house;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProv() {
        return prov;
    }

    public void setProv(String prov) {
        this.prov = prov;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getAddressText() {
        return addressText;
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
