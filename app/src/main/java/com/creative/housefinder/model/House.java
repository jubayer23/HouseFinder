
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
    public final static Creator<House> CREATOR = new Creator<House>() {


        @SuppressWarnings({
            "unchecked"
        })
        public House createFromParcel(Parcel in) {
            return new House(in);
        }

        public House[] newArray(int size) {
            return (new House[size]);
        }

    }
    ;

    protected House(Parcel in) {
        this.unit = ((String) in.readValue((String.class.getClassLoader())));
        this.house = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.streetName = ((String) in.readValue((String.class.getClassLoader())));
        this.city = ((String) in.readValue((String.class.getClassLoader())));
        this.prov = ((String) in.readValue((String.class.getClassLoader())));
        this.postalCode = ((String) in.readValue((String.class.getClassLoader())));
        this.longitude = ((Double) in.readValue((Double.class.getClassLoader())));
        this.latitude = ((Double) in.readValue((Double.class.getClassLoader())));
        this.addressText = ((String) in.readValue((String.class.getClassLoader())));
    }

    public House() {
    }

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

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(unit);
        dest.writeValue(house);
        dest.writeValue(streetName);
        dest.writeValue(city);
        dest.writeValue(prov);
        dest.writeValue(postalCode);
        dest.writeValue(longitude);
        dest.writeValue(latitude);
        dest.writeValue(addressText);
    }

    public int describeContents() {
        return  0;
    }

}
