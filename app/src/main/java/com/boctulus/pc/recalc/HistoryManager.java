package com.boctulus.pc.ReCalc;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 01/01/2018.
 */

class HistoryManager implements Parcelable {
    private List<Calculation> lista = new ArrayList<>();
    private int index = -1;

    HistoryManager(){

    }

    public void push(Calculation c){
        lista.add(c);
        index = lista.size()-1;
    }

    public void push(String expression, String result){
        push(new Calculation(expression,result));
    }

    public Calculation pull(){
        index--;
        Calculation res = lista.get(index);
        return res;
    }

    public Calculation next(){
        index++;
        Calculation res = lista.get(index);
        return res;
    }

    public boolean isEmpty(){
        return lista.size()>0;
    }

    public boolean hasPrev(){
        return index>0;
    }

    public boolean hasNext(){
        return index<lista.size()-1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(index);
        dest.writeList(lista);
    }

    public static final Parcelable.Creator<HistoryManager> CREATOR
            = new Parcelable.Creator<HistoryManager>() {

        public HistoryManager createFromParcel(Parcel in) {
            return new HistoryManager(in);
        }

        public HistoryManager[] newArray(int size) {
            return new HistoryManager[size];
        }
    };

    private HistoryManager(Parcel in) {
        index = in.readInt();
        lista = in.readArrayList(Calculation.class.getClassLoader());
    }

}
