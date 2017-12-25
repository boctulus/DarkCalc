package com.example.pc.easycalc;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;


public class MainActivity extends AppCompatActivity {

    private double total = Double.NaN;
    private double val1;
    private double val2;

    private char op1;
    private char op2;

    private  ClipboardManager clipboardManager;

    static final String TAG = "DEBUG";



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);


        //to remove "information bar" above the action bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //to remove the action bar (title bar)

        getSupportActionBar().hide();


        /* Evito colapsen los displays */

        final LinearLayout display = findViewById(R.id.display);

        display.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                /* Toca apagar el listener para evitar se llame cada vez */
                display.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                display.setMinimumHeight(display.getHeight());
            }
        });

        final LinearLayout hot_display_cont = findViewById(R.id.hotDisplayContainer);

        hot_display_cont.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                /* Toca apagar el listener para evitar se llame cada vez */
                hot_display_cont.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                hot_display_cont.setMinimumHeight(hot_display_cont.getHeight());
            }
        });


        EditText res_display = findViewById(R.id.resDisplay);

        if (res_display.getText().toString() == "EASY CALC")
            res_display.setText("");

        res_display.setEnabled(true);
        res_display.setFocusableInTouchMode(true);
        res_display.setFocusable(true);
        res_display.requestFocus();
        //res_display.setEnableSizeCache(false);
        //res_display.setMovementMethod(null);
        //res_display.setMaxHeight(330);


        // Register the context menu to the the EditText
        registerForContextMenu(res_display);


        LinearLayout root = (LinearLayout) findViewById(R.id.root);

        for (int rix = 0; rix< root.getChildCount(); rix++){
            View elem = (View) root.getChildAt(rix);

            // no es un contenedor
            if (elem.getId() == R.id.display)
                continue;

            LinearLayout row = (LinearLayout) elem;

            int childCount = row.getChildCount();
            for (int i= 0; i < childCount; i++){
                Button boton = (Button) row.getChildAt(i);
                //boton.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));

                boton.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v){
                        EditText display = findViewById(R.id.resDisplay);
                        Button b = (Button) v;

                        String newEntry = b.getText().toString();
                        String buffer = display.getText().toString();

                        switch (newEntry)
                        {
                            case "C":
                                display.setText("");
                                total = 0;
                                op1 = '\u0000';
                                return;

                            case "M+":
                                return;

                            case "M-":
                                return;

                            case "DEL":

                            case "\u232b":
                                if (buffer.length()>0)
                                    display.setText( buffer.substring(0,buffer.length()-1) );
                                return;

                            // Operators

                            case "+":
                                Log.d("DEBUG",buffer.substring(buffer.length()-1));

                                if (!buffer.endsWith(newEntry))
                                    display.setText(buffer + newEntry);
                                return;

                            case "-":
                                if (!buffer.endsWith(newEntry))
                                    display.setText(buffer + newEntry);
                                return;

                            case "×":
                                if (!buffer.endsWith(newEntry))
                                    display.setText(buffer + newEntry);
                                return;

                            case "÷":
                                if (!buffer.endsWith(newEntry))
                                    display.setText(buffer + newEntry);
                                return;

                            case "1/x":
                                display.setText(buffer + newEntry);
                                return;

                            case "√":
                                display.setText(buffer + newEntry);
                                return;

                            case "%":
                                // si el operador anterior no es + o -, ignorar
                                // si se repite, ignorar

                                if (!buffer.endsWith(newEntry))
                                    display.setText(buffer + newEntry);

                                return;

                            case "=":
                                return;


                            // Dot

                            case ".":
                                if (!buffer.contains("."))
                                    display.setText(buffer + ".");
                                return;

                            // Digits

                            default:
                                display.setText(buffer + newEntry);
                                break;
                        }


                        //Log.d("DEBUG", b.getText().toString());


                    }
                });

            }


            disableSoftInput((EditText) findViewById(R.id.hotDisplay));
            disableSoftInput((EditText) findViewById(R.id.resDisplay));

            //getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        }



    } // end fn


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        //AutoResizeEditText display = findViewById(R.id.resDisplay);
        //savedInstanceState.putString("res",display.getText().toString());
        // etc.

        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        //String res = savedInstanceState.getString("res");
        //AutoResizeEditText display = findViewById(R.id.resDisplay);
        //display.setText(res);

    }


    /**
     * Disable soft keyboard from appearing, use in conjunction with android:windowSoftInputMode="stateAlwaysHidden|adjustNothing"
     * @param editText
     */
    public static void disableSoftInput(EditText editText) {
        if (Build.VERSION.SDK_INT >= 11) {
            editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
            editText.setTextIsSelectable(true);
        } else {
            editText.setRawInputType(InputType.TYPE_NULL); // none
            editText.setFocusable(true);
        }


    }

    /* Menu contextual flotante */

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemcopy:
                copyText();
                return true;
            case R.id.itempaste:
                pasteText();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextmenu, menu);


        MenuItem paste_action = menu.findItem(R.id.itempaste);
        if (!(clipboardManager.hasPrimaryClip())) {
            paste_action.setEnabled(false);
        }else{
            paste_action.setEnabled(true);
        }


    }


    /* Clipboard */

    public void copyText(){
        EditText display = findViewById(R.id.resDisplay);

        //save data to clipboard
        ClipData clipData = ClipData.newPlainText("RESULTADO", display.getText().toString());
        clipboardManager.setPrimaryClip(clipData);

    }

    public void pasteText(){
        EditText display = findViewById(R.id.resDisplay);

        if (!clipboardManager.hasPrimaryClip())
          return;

        try{
            ClipData clipData=clipboardManager.getPrimaryClip();

            int curPos=display.getSelectionStart();
            String textToPaste=clipData.getItemAt(0).getText().toString();
            String oldText=display.getText().toString();
            String textBeforeCursor=oldText.substring(0,curPos);
            String textAfterCursor=oldText.substring(curPos);
            String newText=textBeforeCursor+textToPaste+textAfterCursor;

            display.setText(newText);

        }catch(NullPointerException e){}



    }



}
