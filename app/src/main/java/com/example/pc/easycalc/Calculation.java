package com.example.pc.easycalc;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by pc on 01/01/2018.
 */

class Calculation implements Parcelable{
    public String expression;
    public String result;

    Calculation(String e, String r){
        expression = e;
        result = r;
    }

    public String toString(){
        return String.format("%s de [ %s ]",expression,result);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(expression);
        dest.writeString(result);
    }

    public static final Parcelable.Creator<Calculation> CREATOR
            = new Parcelable.Creator<Calculation>() {

        public Calculation createFromParcel(Parcel in) {
            return new Calculation(in);
        }

        public Calculation[] newArray(int size) {
            return new Calculation[size];
        }
    };

    private Calculation(Parcel in) {
        expression = in.readString();
        result = in.readString();
    }

}
