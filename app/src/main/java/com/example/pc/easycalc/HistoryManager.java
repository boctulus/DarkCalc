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

}
