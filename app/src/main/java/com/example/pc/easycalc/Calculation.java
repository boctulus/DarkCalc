package com.example.pc.easycalc;

/**
 * Created by pc on 01/01/2018.
 */

class Calculation {
    public String expression;
    public String result;

    Calculation(String e, String r){
        expression = e;
        result = r;
    }

    public String toString(){
        return String.format("%s de [ %s ]",expression,result);
    }
}
