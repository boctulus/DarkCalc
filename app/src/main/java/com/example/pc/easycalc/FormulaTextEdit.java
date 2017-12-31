package com.example.pc.easycalc;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

/**
 * Created by pc on 30/12/2017.
 */

public class FormulaTextEdit extends AppCompatEditText {

    public FormulaTextEdit(final Context context) {
        this(context, null, 0);
    }

    public FormulaTextEdit(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FormulaTextEdit(final Context context, final AttributeSet attrs,
                           final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isSuggestionsEnabled(){
        return false;
    }

    @Override
    public void onSelectionChanged(int start, int end) {

        CharSequence text = getText();
        if (text != null) {
            if (start != text.length() || end != text.length()) {
                setSelection(text.length(), text.length());
                return;
            }
        }

        super.onSelectionChanged(start, end);
    }

}