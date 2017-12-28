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
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity
{
    private boolean has_error = false;
    private boolean is_number = false;
    private boolean has_dot   = false;

    private  ClipboardManager clipboardManager;

    static final String DEBUG = "DEBUG";


    private void hideBar(){
        //to remove "information bar" above the action bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //to remove the action bar (title bar)

        getSupportActionBar().hide();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideBar();

        // Reserved keywords
        String[] constantStr_lst = new String[]{"Pi","PI","π","e","NA"};
        String[] function_lst    = new String[]{"sin","cos","tan","arcsin","arccos","arctan", "sinh","cosh","tanh","arcsinh","arccosh","arcanh","sum","prod","ln","log","log2","log10"};
        final List<String> functions = Arrays.asList(function_lst);

        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);


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


        AutoResizeEditText res_display = findViewById(R.id.resDisplay);
        res_display.setEnabled(true);
        res_display.setFocusableInTouchMode(true);
        res_display.setFocusable(true);
        res_display.requestFocus();
        res_display.setEnableSizeCache(false);
        //res_display.setMovementMethod(null);
        //res_display.setMaxHeight(330);
        //res_display.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        //if (res_display.getText().toString() == "EASY CALC")
        //   res_display.setText("");


        // Register the context menu to the the EditText
        registerForContextMenu(res_display);


        LinearLayout root = (LinearLayout) findViewById(R.id.root);

        for (int rix = 0; rix< root.getChildCount(); rix++){
            final View elem = (View) root.getChildAt(rix);

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

                        int id = b.getId();
                        String newEntry = b.getText().toString();
                        String buffer = display.getText().toString();

                        //Log.d(DEBUG,newEntry+" ("+id+")");

                        switch (id)
                        {
                            case R.id.c:
                                clearEntry();
                                return;

                            case R.id.mp:
                                return;

                            case R.id.mm:
                                return;

                            case R.id.del:
                                if (has_error) {
                                    clearEntry();
                                    return;
                                }

                                if (buffer.length()>0)
                                    display.setText( buffer.substring(0,buffer.length()-1) );
                                return;

                            // Operators

                            case R.id.add:
                                if (!buffer.endsWith("+"))
                                    if (buffer.endsWith("-") || buffer.endsWith("×") ||
                                            buffer.endsWith("÷")  )
                                        display.setText( buffer.substring(0,buffer.length()-1)+"+" );
                                    else
                                        display.setText(buffer + newEntry);

                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.sub:
                                if (!buffer.endsWith("\u2212"))
                                    if (buffer.endsWith("+"))
                                        display.setText( buffer.substring(0,buffer.length()-1)+"-" );
                                    else
                                        if (buffer.endsWith("×") || buffer.endsWith("÷"))
                                            display.setText(buffer + "(-");
                                         else
                                            display.setText(buffer + "-");

                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.mul:
                                if (!buffer.endsWith("×"))
                                    if (buffer.endsWith("+") || buffer.endsWith("-") || buffer.endsWith("÷") )
                                        display.setText( buffer.substring(0,buffer.length()-1)+"×" );
                                    else
                                        display.setText(buffer + newEntry);

                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.div:
                                if (!buffer.endsWith("÷"))
                                    if (buffer.endsWith("+") || buffer.endsWith("-") || buffer.endsWith("×") )
                                        display.setText( buffer.substring(0,buffer.length()-1)+"÷" );
                                    else
                                        display.setText(buffer + newEntry);

                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.per:
                                // si el operador anterior no es + o -, ignorar
                                // si se repite, ignorar

                                if (!buffer.endsWith(newEntry))
                                    display.setText(buffer + newEntry);

                                is_number = false;
                                has_dot   = false;
                                return;

                           // Functions

                            case R.id.inv:
                                if (buffer.isEmpty())
                                    display.setText("1/(");
                                else
                                    display.setText("1÷("+buffer+")");

                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.sqr:
                                if (buffer.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                    if (buffer.endsWith("(") || buffer.endsWith("+") || buffer.endsWith("-") ||
                                            buffer.endsWith("×") || buffer.endsWith("÷"))
                                        display.setText(buffer+newEntry+"(");
                                    else
                                        display.setText(buffer+"×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.sin:
                                if (buffer.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (buffer.endsWith("(") || buffer.endsWith("+") || buffer.endsWith("-") ||
                                        buffer.endsWith("×") || buffer.endsWith("÷"))
                                        display.setText(buffer+newEntry+"(");
                                    else
                                        display.setText(buffer+"×"+newEntry+"(");
                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.cos:
                                if (buffer.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (buffer.endsWith("(") || buffer.endsWith("+") || buffer.endsWith("-") ||
                                        buffer.endsWith("×") || buffer.endsWith("÷"))
                                        display.setText(buffer+newEntry+"(");
                                    else
                                        display.setText(buffer+"×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.tan:
                                if (buffer.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (buffer.endsWith("(") || buffer.endsWith("+") || buffer.endsWith("-") ||
                                        buffer.endsWith("×") || buffer.endsWith("÷"))
                                        display.setText(buffer+newEntry+"(");
                                    else
                                        display.setText(buffer+"×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.ln:
                                if (buffer.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (buffer.endsWith("(") || buffer.endsWith("+") || buffer.endsWith("-") ||
                                        buffer.endsWith("×") || buffer.endsWith("÷"))
                                        display.setText(buffer+newEntry+"(");
                                    else
                                        display.setText(buffer+"×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.log2:
                                if (buffer.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (buffer.endsWith("(") || buffer.endsWith("+") || buffer.endsWith("-") ||
                                        buffer.endsWith("×") || buffer.endsWith("÷"))
                                        display.setText(buffer+newEntry+"(");
                                    else
                                        display.setText(buffer+"×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.log:
                                if (buffer.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (buffer.endsWith("(") || buffer.endsWith("+") || buffer.endsWith("-") ||
                                        buffer.endsWith("×") || buffer.endsWith("÷"))
                                        display.setText(buffer+newEntry+"(");
                                    else
                                        display.setText(buffer+"×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.pex:
                                if (buffer.isEmpty())
                                    display.setText("e^"+"(");
                                else
                                if (buffer.endsWith("(") || buffer.endsWith("+") || buffer.endsWith("-") ||
                                        buffer.endsWith("×") || buffer.endsWith("÷"))
                                        display.setText(buffer+"e^(");
                                    else
                                        display.setText(buffer+"×e^(");

                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.p2x:
                                if (buffer.isEmpty())
                                    display.setText("2^"+"(");
                                else
                                if (buffer.endsWith("(") || buffer.endsWith("+") || buffer.endsWith("-") ||
                                        buffer.endsWith("×") || buffer.endsWith("÷"))
                                         display.setText(buffer+"2^"+"(");
                                    else
                                        display.setText(buffer+"×2^"+"(");

                                return;

                            case R.id.p10x:
                                if (buffer.isEmpty())
                                    display.setText("10^"+"(");
                                else
                                if (buffer.endsWith("(") || buffer.endsWith("+") || buffer.endsWith("-") ||
                                        buffer.endsWith("×") || buffer.endsWith("÷"))
                                        display.setText(buffer+"10^"+"(");
                                    else
                                        display.setText(buffer+"×10^"+"(");

                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.pxy:
                                if (buffer.isEmpty() || buffer.endsWith("(") || buffer.endsWith("+") ||
                                        buffer.endsWith("-") || buffer.endsWith("×") || buffer.endsWith("÷")) {
                                    setError(null);
                                }
                                else
                                    display.setText(buffer+"^(");

                                is_number = false;
                                has_dot   = false;
                                return;

                            // Custom functions

                            case R.id.sum:
                                if (buffer.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                    display.setText(buffer+"×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                return;

                            case R.id.xp:
                                if (buffer.isEmpty())
                                    display.setText("prod"+"(");
                                else
                                    display.setText(buffer+"×"+"prod"+"(");

                                is_number = false;
                                has_dot   = false;
                                return;

                            // Constants

                            case R.id.e:
                            case R.id.pi:
                            case R.id.na:

                                if (buffer.isEmpty())
                                    display.setText(newEntry);
                                else
                                    if (buffer.endsWith("(") || buffer.endsWith("+") || buffer.endsWith("-") ||
                                            buffer.endsWith("×") || buffer.endsWith("÷"))
                                        display.setText(buffer+newEntry);
                                    else
                                        display.setText(buffer+"×"+newEntry);

                                is_number = false;
                                has_dot   = false;
                                return;

                            // Parenthesis

                            case R.id.par:

                                if (buffer.isEmpty())
                                    display.append("(");
                                else
                                    if (buffer.endsWith("("))
                                        display.append("(");
                                    else
                                        if (is_number) {
                                            if (balancedParenthesis(buffer))
                                                display.append("×(");
                                            else
                                                display.append(")");
                                        }else
                                            if (buffer.endsWith(")"))
                                                if (balancedParenthesis(buffer))
                                                    display.append("×(");
                                                else
                                                    display.append(")");
                                            else {
                                                boolean fn_found = false;
                                                for (String fn : functions) {
                                                    if (buffer.endsWith(fn)){
                                                        display.append("(");
                                                        fn_found = true;
                                                        break;
                                                    }
                                                }
                                                if (!fn_found)
                                                    if (buffer.endsWith("+") || buffer.endsWith("-") ||
                                                            buffer.endsWith("×") || buffer.endsWith("÷"))
                                                        display.append("(");
                                                    else
                                                        // asumo es una constante como 'π' o 'e'
                                                        display.append(")");
                                            }

                                is_number = false;
                                has_dot   = false;
                                return;

                            /*
                                EQUALS

                            */
                            case R.id.equ:
                                String inputExpr = buffer;

                                inputExpr = inputExpr.replace("\\u2212","-");
                                inputExpr = inputExpr.replace("×","*");
                                inputExpr = inputExpr.replace("÷","/");
                                inputExpr = inputExpr.replace("x²","^2");
                                inputExpr = inputExpr.replace("√","sqrt");
                                inputExpr = inputExpr.replace("log₂(  ","log2(");
                                inputExpr = inputExpr.replace("log(","log10(");
                                inputExpr = inputExpr.replace("ln(","log(");
                                inputExpr = inputExpr.replace("π","pi");
                                inputExpr = inputExpr.replace("Pi","pi");
                                inputExpr = inputExpr.replace("PI","pi");
                                inputExpr = inputExpr.replace("∑(","sum(");
                                inputExpr = inputExpr.replace("NA","(6.022140857*(10^(-23)))");

                                Log.d(DEBUG,inputExpr);

                                // Create an Expression (A class from exp4j library)
                                Expression expression = new ExpressionBuilder(inputExpr).build();
                                try {
                                    // Calculate the result and display
                                    double result = expression.evaluate();
                                    display.setText(Double.toString(result));
                                } catch (Exception ex) {
                                    // Display an error message
                                    setError(null);
                                }

                                is_number = false;
                                has_dot   = false;
                                return;


                            // Dot

                            case R.id.dot:
                                if (is_number && !has_dot)
                                    display.setText(buffer + ".");
                                is_number = true;
                                has_dot   = true;
                                return;

                            // Digits

                            case R.id.d0:
                            case R.id.d1:
                            case R.id.d2:
                            case R.id.d3:
                            case R.id.d4:
                            case R.id.d5:
                            case R.id.d6:
                            case R.id.d7:
                            case R.id.d8:
                            case R.id.d9:

                                is_number = true;

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

        }



    } // end fn

    private boolean openedParenthesis(String expr){
        return (!expr.isEmpty() && (countMatches("(",expr)>countMatches(")",expr)));
    }

    private boolean closedParenthesis(String expr){
        return (!expr.isEmpty() && (countMatches("(",expr)<countMatches(")",expr)));
    }

    private boolean balancedParenthesis(String expr){
        return (!expr.isEmpty() && (countMatches("(",expr)==countMatches(")",expr)));
    }

    private void setError(String msg){
        has_error = true;
        EditText res_display = findViewById(R.id.resDisplay);
        res_display.setText(msg.isEmpty() ? "ERROR" : msg);
    }

    private void clearMemory(){
        // ...
    }

    private void clearEntry(){
        has_error = false;
        is_number = false;
        has_dot   = false;
        EditText res_display = findViewById(R.id.resDisplay);
        res_display.setText("");
    }

    private void clearAll(){
        clearEntry();
        clearMemory();
    }


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

    // Utiles

    public static int countMatches(String findStr, String str){
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf(findStr, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += findStr.length();
            }
        }

        return count;
    }

}
