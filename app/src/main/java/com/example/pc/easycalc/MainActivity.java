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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity
{
    private boolean has_error  = false;
    private boolean is_number  = false;
    private boolean has_dot    = false;
    //private int digit_count   = 0;
    private boolean is_result  = false;

    List<String> history = new ArrayList<>();

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
    public void onSaveInstanceState(Bundle savedInstanceState) {

        EditText rdisp1 = findViewById(R.id.disp1);
        savedInstanceState.putString("disp1",rdisp1.getText().toString());

        AutoResizeEditText rdisp2 = findViewById(R.id.disp2);
        savedInstanceState.putString("disp2",rdisp2.getText().toString());

        savedInstanceState.putBoolean("has_error",has_error);
        savedInstanceState.putBoolean("has_dot",has_dot);
        savedInstanceState.putBoolean("is_number",is_number);
        //savedInstanceState.putInt("digit_count",digit_count);

        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        String rdisp1 = savedInstanceState.getString("disp1");
        EditText disp1 = findViewById(R.id.disp1);
        disp1.setText(rdisp1);

        String rdisp2 = savedInstanceState.getString("disp2");
        EditText disp2 = findViewById(R.id.disp2);
        disp2.setText(rdisp2);

        has_error = savedInstanceState.getBoolean("has_error");
        has_dot = savedInstanceState.getBoolean("has_dot");
        is_number = savedInstanceState.getBoolean("is_number");
        //digit_count = savedInstanceState.getInt("digit_count");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideBar();

        // Reserved keywords
        String[] constant_lst = new String[]{"Pi","PI","π","e","NA"};
        String[] function_lst    = new String[]{"sin","cos","tan","arcsin","arccos","arctan", "sinh","cosh","tanh","arcsinh","arccosh","arcanh","sum","prod","ln","log","log2","log10"};

        final List<String> functions = Arrays.asList(function_lst);
        final List<String> constants = Arrays.asList(constant_lst);

        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);


        final EditText disp1_et = findViewById(R.id.disp1);
        disp1_et.setMovementMethod(null);
        disp1_et.setFocusable(false);
        disp1_et.setFocusableInTouchMode(false);
        disp1_et.setCursorVisible(false);
        disp1_et.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        final AutoResizeEditText disp2_et = findViewById(R.id.disp2);
        disp2_et.setEnabled(true);
        disp2_et.setFocusableInTouchMode(true);
        disp2_et.setFocusable(true);
        disp2_et.requestFocus();
        disp2_et.setEnableSizeCache(false);
        disp2_et.setCursorVisible(false);
        disp2_et.setMovementMethod(null);
        //disp2_et.setMaxHeight(330);
        disp2_et.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        // Register the context menu to the the EditText
        registerForContextMenu(disp2_et);

        final LinearLayout display = findViewById(R.id.display);
        final LinearLayout kb =  findViewById(R.id.keyboard);
        final LinearLayout disp1_cont = findViewById(R.id.disp1Container);


        /*
        * Evito colapsos 
        * */

        disp1_cont.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                /* Toca apagar el listener para evitar se llame cada vez */
                disp1_cont.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                disp1_cont.setMinimumHeight(disp1_cont.getHeight());
            }
        });

        final LinearLayout disp2_cont = findViewById(R.id.disp2Container);

        disp2_cont.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                /* Toca apagar el listener para evitar se llame cada vez */
                disp2_cont.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                disp2_cont.setMinimumHeight(disp2_cont.getHeight());
            }
        });

        kb.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                /* Toca apagar el listener para evitar se llame cada vez */
                kb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                kb.setMinimumHeight(kb.getHeight());
            }
        });


        for (int rix = 0; rix< kb.getChildCount(); rix++){
            final View elem = (View) kb.getChildAt(rix);

            LinearLayout row = (LinearLayout) elem;

            int childCount = row.getChildCount();
            for (int i= 0; i < childCount; i++){
                Button boton = (Button) row.getChildAt(i);

                boton.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v){
                        Button b = (Button) v;

                        int id = b.getId();
                        String newEntry = b.getText().toString();
                        String bufferDisplay2 = disp2_et.getText().toString();

                        //Log.d(DEBUG,newEntry+" ("+id+")");

                        switch (id)
                        {
                            case R.id.c:
                                clearEntry();
                                break;

                            case R.id.mp:
                                break;

                            case R.id.mm:
                                break;

                            case R.id.del:
                                disp1_et.setText("");
                                is_result = false;

                                if (has_error) {
                                    clearEntry();
                                    break;
                                }

                                if (bufferDisplay2.endsWith("."))
                                    has_dot = false;

                                if (bufferDisplay2.length()>0) {
                                    disp2_et.setText(bufferDisplay2.substring(0, bufferDisplay2.length() - 1));
                                }

                                //Log.d(DEBUG,"Has dot? "+ (has_dot ? "YES" : "NO"));
                                break;

                            // Operators

                            case R.id.add:
                                if (!bufferDisplay2.endsWith("+"))
                                    if (bufferDisplay2.endsWith("−") || bufferDisplay2.endsWith("×") ||
                                            bufferDisplay2.endsWith("÷")  )
                                        disp2_et.setText( bufferDisplay2.substring(0,bufferDisplay2.length()-1)+"+" );
                                    else
                                        disp2_et.setText(bufferDisplay2 + newEntry);

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.sub:
                                if (!bufferDisplay2.endsWith("−"))
                                    if (bufferDisplay2.endsWith("+"))
                                        disp2_et.setText( bufferDisplay2.substring(0,bufferDisplay2.length()-1)+"−" );
                                    else
                                    if (bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                        disp2_et.setText(bufferDisplay2 + "(−");
                                    else
                                        disp2_et.setText(bufferDisplay2 + "−");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.mul:
                                if (!bufferDisplay2.endsWith("×"))
                                    if (bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") || bufferDisplay2.endsWith("÷") )
                                        disp2_et.setText( bufferDisplay2.substring(0,bufferDisplay2.length()-1)+"×" );
                                    else
                                        disp2_et.setText(bufferDisplay2 + newEntry);

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.div:
                                if (!bufferDisplay2.endsWith("÷"))
                                    if (bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") || bufferDisplay2.endsWith("×") )
                                        disp2_et.setText( bufferDisplay2.substring(0,bufferDisplay2.length()-1)+"÷" );
                                    else
                                        disp2_et.setText(bufferDisplay2 + newEntry);

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.per:
                                if (!bufferDisplay2.endsWith("%"))
                                    disp2_et.setText(bufferDisplay2 + newEntry);

                                is_number = false;
                                has_dot   = false;
                                break;

                            // Functions

                            case R.id.inv:
                                if (bufferDisplay2.isEmpty())
                                    disp2_et.setText("1÷(");
                                else
                                    disp2_et.setText("1÷("+bufferDisplay2+")");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.sqr:
                                if (bufferDisplay2.isEmpty())
                                    disp2_et.setText(newEntry+"(");
                                else
                                    if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                            bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                        disp2_et.setText(bufferDisplay2+newEntry+"(");
                                    else
                                        disp2_et.setText(bufferDisplay2+"×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.sin:
                                if (bufferDisplay2.isEmpty())
                                    disp2_et.setText(newEntry+"(");
                                else
                                    if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                            bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                        disp2_et.setText(bufferDisplay2+newEntry+"(");
                                    else
                                        disp2_et.setText(bufferDisplay2+"×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.cos:
                                if (bufferDisplay2.isEmpty())
                                    disp2_et.setText(newEntry+"(");
                                else
                                    if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                            bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                        disp2_et.setText(bufferDisplay2+newEntry+"(");
                                    else
                                        disp2_et.setText(bufferDisplay2+"×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.tan:
                                if (bufferDisplay2.isEmpty())
                                    disp2_et.setText(newEntry+"(");
                                else
                                    if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                            bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                        disp2_et.setText(bufferDisplay2+newEntry+"(");
                                    else
                                        disp2_et.setText(bufferDisplay2+"×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.ln:
                                if (bufferDisplay2.isEmpty())
                                    disp2_et.setText(newEntry+"(");
                                else
                                    if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                            bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                        disp2_et.setText(bufferDisplay2+newEntry+"(");
                                    else
                                        disp2_et.setText(bufferDisplay2+"×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.log2:
                                if (bufferDisplay2.isEmpty())
                                    disp2_et.setText("log2"+"(");
                                else
                                    if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                            bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                        disp2_et.setText(bufferDisplay2+"log2"+"(");
                                    else
                                        disp2_et.setText(bufferDisplay2+"×"+"log2"+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.log:
                                if (bufferDisplay2.isEmpty())
                                    disp2_et.setText(newEntry+"(");
                                else
                                    if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                            bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                        disp2_et.setText(bufferDisplay2+newEntry+"(");
                                    else
                                        disp2_et.setText(bufferDisplay2+"×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.pex:
                                if (bufferDisplay2.isEmpty())
                                    disp2_et.setText("e^"+"(");
                                else
                                    if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                            bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                        disp2_et.setText(bufferDisplay2+"e^(");
                                    else
                                        disp2_et.setText(bufferDisplay2+"×e^(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.p2x:
                                if (bufferDisplay2.isEmpty())
                                    disp2_et.setText("2^"+"(");
                                else
                                    if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                            bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                        disp2_et.setText(bufferDisplay2+"2^"+"(");
                                    else
                                        disp2_et.setText(bufferDisplay2+"×2^"+"(");

                                break;

                            case R.id.p10x:
                                if (bufferDisplay2.isEmpty())
                                    disp2_et.setText("10^"+"(");
                                else
                                if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                        bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                    disp2_et.setText(bufferDisplay2+"10^"+"(");
                                else
                                    disp2_et.setText(bufferDisplay2+"×10^"+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.pxy:
                                if (bufferDisplay2.isEmpty() || bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") ||
                                        bufferDisplay2.endsWith("−") || bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷")) {
                                    setError(null);
                                }
                                else
                                    disp2_et.setText(bufferDisplay2+"^(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            // Custom functions

                            case R.id.sum:
                                if (bufferDisplay2.isEmpty())
                                    disp2_et.setText(newEntry+"(");
                                else
                                    disp2_et.setText(bufferDisplay2+"×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.xp:
                                if (bufferDisplay2.isEmpty())
                                    disp2_et.setText("prod"+"(");
                                else
                                    disp2_et.setText(bufferDisplay2+"×"+"prod"+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            // Constants

                            case R.id.e:
                            case R.id.pi:
                            case R.id.na:

                                if (bufferDisplay2.isEmpty())
                                    disp2_et.setText(newEntry);
                                else
                                    if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                            bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                        disp2_et.setText(bufferDisplay2+newEntry);
                                    else
                                        disp2_et.setText(bufferDisplay2+"×"+newEntry);

                                is_number = false;
                                has_dot   = false;
                                break;

                            // Parentheses

                            case R.id.par:

                                if (bufferDisplay2.isEmpty())
                                    disp2_et.append("(");
                                else
                                    if (bufferDisplay2.endsWith("("))
                                        disp2_et.append("(");
                                    else
                                        if (is_number) {
                                            if (balancedParentheses(bufferDisplay2))
                                                disp2_et.append("×(");
                                            else
                                                disp2_et.append(")");
                                        }else
                                            if (bufferDisplay2.endsWith(")"))
                                                if (balancedParentheses(bufferDisplay2))
                                                    disp2_et.append("×(");
                                                else
                                                    disp2_et.append(")");
                                            else {
                                                boolean fn_found = false;
                                                for (String fn : functions) {
                                                    if (bufferDisplay2.endsWith(fn)){
                                                        disp2_et.append("(");
                                                        fn_found = true;
                                                        break;
                                                    }
                                                }
                                                if (!fn_found)
                                                    if (bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                                            bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                                        disp2_et.append("(");
                                                    else
                                                        // asumo es una constante como 'π' o 'e'
                                                        disp2_et.append(")");
                                            }

                                is_number = false;
                                has_dot   = false;
                                break;

                            /*
                                EQUALS

                            */
                            case R.id.equ:
                                int diff = countMatches("(",bufferDisplay2)-countMatches(")",bufferDisplay2);

                                if (diff>0) {
                                    for (int i = 0; i < diff; i++)
                                        disp2_et.append(")");

                                    bufferDisplay2 = disp2_et.getText().toString();
                                }

                                String inputExpr = bufferDisplay2;

                                inputExpr = inputExpr.replace("\\u2212","-");
                                inputExpr = inputExpr.replace("−","-");
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
                                inputExpr = inputExpr.replace("NA","(6.022140857E-23)");

                                // Elimina separador de miles para el ExpressionBuilder
                                //inputExpr = inputExpr.replace(",","");

                                //Log.d(DEBUG,inputExpr);

                                try {
                                    // Create an Expression (A class from exp4j library)
                                    Expression expression = new ExpressionBuilder(inputExpr).build();

                                    // Calculate the result and display
                                    double result = expression.evaluate();
                                    String strRes = Double.toString(result);

                                    history.add(inputExpr);
                                    disp1_et.setText(prettyFormat(inputExpr));
                                    disp2_et.setText(formatCurrency(strRes).replace(",",""));

                                    has_dot   = (strRes.contains("."));
                                    has_error = (strRes=="NaN");
                                    is_number = (strRes!="NaN");
                                    is_result = true;

                                    //Log.d(DEBUG,"Has dot? "+ (has_dot ? "YES" : "NO"));

                                } catch (Exception ex) {
                                    Log.d(DEBUG,"ERROR -----------");
                                    setError(null);
                                }

                                break;


                            // Dot

                            case R.id.dot:
                                if (is_number && !has_dot) {
                                    disp2_et.setText(bufferDisplay2 + ".");
                                    has_dot   = true;
                                    is_number = true;
                                }else
                                    if (bufferDisplay2.isEmpty() || bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") ||
                                            bufferDisplay2.endsWith("−") || bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷")) {
                                        disp2_et.setText(bufferDisplay2 + "0.");
                                        has_dot   = true;
                                        is_number = true;
                                    }

                                //Log.d(DEBUG,"Has dot? "+ (has_dot ? "YES" : "NO"));
                                break;

                            // Digits

                            case R.id.d0:
                                //Log.d(DEBUG,"bufferDisplay2 "+bufferDisplay2);
                                //Log.d(DEBUG,"newEntry "+newEntry);

                                // No permito algo como "0000"
                                //if (bufferDisplay2=="0" && newEntry=="0")
                                 //   disp2_et.append(".");

                            case R.id.d1:
                            case R.id.d2:
                            case R.id.d3:
                            case R.id.d4:
                            case R.id.d5:
                            case R.id.d6:
                            case R.id.d7:
                            case R.id.d8:
                            case R.id.d9:

                                for (String cte : constants) {
                                    if (bufferDisplay2.endsWith(cte)){
                                        disp2_et.append("×");
                                        break;
                                    }
                                }

                                //if (digit_count >=4)
                                //    Log.d(DEBUG,"FORMATEAR NUMERO EN "+bufferDisplay2);

                                is_number = true;
                                //digit_count++;

                            default:
                                disp2_et.append(newEntry);
                                break;
                        }

                        //


                    }
                });


            }


            disableSoftInput((EditText) findViewById(R.id.disp1));
            disableSoftInput((EditText) findViewById(R.id.disp2));

        }



    } // end fn


    private String prettyFormat(String s){
        s = s.replace("-","−");
        s = s.replace("*","×");
        s = s.replace("/","÷");
        s = s.replace("sqrt","√");
        s = s.replace("pi","π");
        s = s.replace("sum(","∑(");
        return s;
    }

    private boolean openedParentheses(String expr){
        return (countMatches("(",expr)>countMatches(")",expr));
    }

    private boolean closedParentheses(String expr){
        return (countMatches("(",expr)<countMatches(")",expr));
    }

    private boolean balancedParentheses(String expr){
        return (countMatches("(",expr)==countMatches(")",expr));
    }

    private boolean noneParentheses(String expr){
        return ((countMatches("(",expr)==0 && countMatches(")",expr)==0));
    }

    private void setError(String msg){
        has_error = true;
        EditText disp2_et = findViewById(R.id.disp2);
        disp2_et.setText(msg);
    }

    private void setError(){
        setError("ERROR");
    }

    private void clearMemory(){
        // ...
    }

    private void clearEntry(){
        has_error  = false;
        is_number  = false;
        has_dot    = false;
        //digit_count = 0;
        is_result  = false;

        EditText disp1_et = findViewById(R.id.disp1);
        disp1_et.setText("");

        EditText disp2_et = findViewById(R.id.disp2);
        disp2_et.setText("");
    }

    private void clearAll(){
        clearEntry();
        clearMemory();
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
        EditText display = findViewById(R.id.disp2);

        //save data to clipboard
        ClipData clipData = ClipData.newPlainText("RESULTADO", display.getText().toString());
        clipboardManager.setPrimaryClip(clipData);

    }

    public void pasteText(){
        EditText display = findViewById(R.id.disp2);

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

    public static String formatCurrency(String string) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

        symbols.setGroupingSeparator(',');  // ',' por ejemplo
        symbols.setDecimalSeparator('.');
        formatter.setDecimalFormatSymbols(symbols);
        //formatter.setMinimumFractionDigits(10);
        formatter.setMaximumFractionDigits(8);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        return formatter.format(Double.parseDouble(string));
    }

}
