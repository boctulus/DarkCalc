package com.example.pc.easycalc;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 01/01/2018.
 */

class HistoryManager {
    private List<Calculation> lista = new ArrayList<>();
    private int index = -1;

    public void push(Calculation c){
        lista.add(c);
        index = lista.size()-1;
    }

    public void push(String expression, String result){
        push(new Calculation(expression,result));
    }

    @Nullable
    public Calculation pull(){
        if (index>0) {
            Calculation res = lista.get(index);
            index--;
            return res;
        }
        return null;
    }

    @Nullable
    public Calculation next(){
        if (hasNext()) {
            index++;
            Calculation res = lista.get(index);
            return res;
        }
        return null;
    }

    public boolean isEmpty(){
        return lista.size()>0;
    }

    public boolean isValid(){
        return !isEmpty() && index>0;
    }

    public boolean hasNext(){
        return index<lista.size()-1;
    }

}
