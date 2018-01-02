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
import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.operator.Operator;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
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

    private HistoryManager history = new HistoryManager();

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
        disp1.setText("");
        disp1.append(rdisp1);

        String rdisp2 = savedInstanceState.getString("disp2");
        EditText disp2 = findViewById(R.id.disp2);
        disp2.setText("");
        disp2.append(rdisp2);

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
        String[] function_lst    = new String[]{"sin","cos","tan","asin","acos","atan", "sinh","cosh","tanh","asinh","acosh","atanh","sum","prod","ln","log","log2","log10"};

        final List<String> functions = Arrays.asList(function_lst);
        final List<String> constants = Arrays.asList(constant_lst);
        final DisplayManger display = new DisplayManger();

        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);


        final EditText disp1_et = findViewById(R.id.disp1);
        //disp1_et.setEnabled(true);
        //disp1_et.setFocusableInTouchMode(true);
        //disp1_et.setFocusable(true);
        //disp1_et.requestFocus();
        //disp1_et.setMovementMethod(null);
        //disp1_et.setMaxHeight(330);
        disp1_et.setCursorVisible(false);
        disp1_et.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);


        final AutoResizeEditText disp2_et = findViewById(R.id.disp2);
        disp2_et.setEnabled(true);
        disp2_et.setFocusable(true);
        disp2_et.setFocusableInTouchMode(true);
        disp2_et.requestFocus();
        disp2_et.setEnableSizeCache(false);
        //disp2_et.setMovementMethod(null);
        disp1_et.setCursorVisible(false);
        disp2_et.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);


        // Register the context menu to the the EditText
        //registerForContextMenu(disp1_et);
        registerForContextMenu(disp2_et);

        final LinearLayout displayLayout    = findViewById(R.id.display);
        final LinearLayout kbLayout         = findViewById(R.id.keyboard);
        final LinearLayout disp1_contLayout = findViewById(R.id.disp1Container);
        final LinearLayout disp2_contLayout = findViewById(R.id.disp2Container);


        /*
        * Evito colapsos
        * */


        displayLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                displayLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                displayLayout.setMinimumHeight(displayLayout.getHeight());
            }
        });

        disp1_contLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                disp1_contLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                disp1_contLayout.setMinimumHeight(disp1_contLayout.getHeight());
            }
        });


        disp2_contLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                disp2_contLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                disp2_contLayout.setMinimumHeight(disp2_contLayout.getHeight());
            }
        });

        kbLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                kbLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                kbLayout.setMinimumHeight(kbLayout.getHeight());
            }
        });

        disp2_et.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                disp2_et.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                disp2_et.setMaxHeight(disp2_et.getHeight());
            }
        });


        for (int rix = 0; rix< kbLayout.getChildCount(); rix++){
            final View elem = (View) kbLayout.getChildAt(rix);

            LinearLayout row = (LinearLayout) elem;

            int childCount = row.getChildCount();
            for (int i= 0; i < childCount; i++){
                Button boton = (Button) row.getChildAt(i);

                boton.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v){
                        Button b = (Button) v;

                        int id = b.getId();
                        String newEntry = b.getText().toString();

                        Calculation c;

                        if (has_error)
                            display.clear();

                        String bufferDisplay2 = display.getText();

                        switch (id)
                        {
                            case R.id.c:
                                display.clear();
                                break;

                            case R.id.mp:
                                break;

                            case R.id.mm:
                                break;

                            case R.id.del:
                                display.backspace();
                                return;

                            // Operators

                            case R.id.add:
                                if (!bufferDisplay2.endsWith("+"))
                                    if (bufferDisplay2.endsWith("−") || bufferDisplay2.endsWith("×") ||
                                            bufferDisplay2.endsWith("÷")  ) {
                                        display.backspace();
                                        display.append("+");
                                    }
                                    else
                                        display.append(newEntry);

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.sub:
                                if (!bufferDisplay2.endsWith("−"))
                                    if (bufferDisplay2.endsWith("+")) {
                                        display.backspace();
                                        display.append("−");
                                    }else
                                    if (bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                        display.append("(−");
                                    else
                                        display.append( "−");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.mul:
                                if (bufferDisplay2.isEmpty())
                                    return;

                                if (!bufferDisplay2.endsWith("×"))
                                    if (bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") || bufferDisplay2.endsWith("÷") ) {
                                        display.backspace();
                                        display.append("×");
                                    }else
                                        display.append(newEntry);

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.div:
                                if (bufferDisplay2.isEmpty())
                                    return;

                                if (!bufferDisplay2.endsWith("÷"))
                                    if (bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") || bufferDisplay2.endsWith("×") ) {
                                        display.backspace();
                                        display.append("÷");
                                    }else
                                        display.append(newEntry);

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.per:
                                if (bufferDisplay2.isEmpty())
                                    return;

                                if (!bufferDisplay2.endsWith("%"))
                                    display.append(newEntry);

                                is_number = false;
                                has_dot   = false;
                                break;

                            // Functions

                            case R.id.inv:
                                if (bufferDisplay2.isEmpty())
                                    display.append("1÷(");
                                else {
                                    display.setText("");
                                    display.append("1÷("+bufferDisplay2+")");
                                }

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.sqr:
                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                        bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                    display.append(newEntry+"(");
                                else
                                    display.append("×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.sin:
                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                        bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                    display.append(newEntry+"(");
                                else
                                    display.append("×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.cos:
                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                        bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                    display.append(newEntry+"(");
                                else
                                    display.append("×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.tan:
                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                        bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                    display.append(newEntry+"(");
                                else
                                    display.append("×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.ln:
                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                        bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                    display.append(newEntry+"(");
                                else
                                    display.append("×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.log2:
                                if (bufferDisplay2.isEmpty())
                                    display.setText("log2"+"(");
                                else
                                if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                        bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                    display.append("log2"+"(");
                                else
                                    display.append("×"+"log2"+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.log:
                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                        bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                    display.append(newEntry+"(");
                                else
                                    display.append("×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.pex:
                                if (bufferDisplay2.isEmpty())
                                    display.setText("e^"+"(");
                                else
                                if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                        bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                    display.append("e^(");
                                else
                                    display.append("×e^(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.p2x:
                                if (bufferDisplay2.isEmpty())
                                    display.setText("2^"+"(");
                                else
                                if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                        bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                    display.append("2^"+"(");
                                else
                                    display.append("×2^"+"(");

                                break;

                            case R.id.p10x:
                                if (bufferDisplay2.isEmpty())
                                    display.setText("10^"+"(");
                                else
                                if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                        bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                    display.append("10^"+"(");
                                else
                                    display.append("×10^"+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.pxy:
                                if (bufferDisplay2.isEmpty() || bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") ||
                                        bufferDisplay2.endsWith("−") || bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷")) {
                                    display.setError();
                                }
                                else
                                    display.append("^(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            // Custom functions

                            case R.id.sum:
                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                    display.append("×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                break;

                            case R.id.fact:
                                if (bufferDisplay2.isEmpty())
                                    return;
                                else
                                    display.append("!");
                                break;

                            /*
                            case R.id.prod:
                                if (bufferDisplay2.isEmpty())
                                    display.setText("prod"+"(");
                                else
                                    display.append("×"+"prod"+"(");

                                is_number = false;
                                has_dot   = false;
                                break;
                            */

                            // Constants

                            case R.id.e:
                            case R.id.pi:
                            case R.id.na:

                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry);
                                else
                                if (bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                        bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                    display.append(newEntry);
                                else
                                    display.append("×"+newEntry);

                                is_number = false;
                                has_dot   = false;
                                break;

                            // Parentheses

                            case R.id.par:

                                if (bufferDisplay2.isEmpty())
                                    display.append("(");
                                else
                                if (bufferDisplay2.endsWith("("))
                                    display.append("(");
                                else
                                if (is_number) {
                                    if (balancedParentheses(bufferDisplay2))
                                        display.append("×(");
                                    else
                                        display.append(")");
                                }else
                                if (bufferDisplay2.endsWith(")"))
                                    if (balancedParentheses(bufferDisplay2))
                                        display.append("×(");
                                    else
                                        display.append(")");
                                else {
                                    boolean fn_found = false;
                                    for (String fn : functions) {
                                        if (bufferDisplay2.endsWith(fn)){
                                            display.append("(");
                                            fn_found = true;
                                            break;
                                        }
                                    }
                                    if (!fn_found)
                                        if (bufferDisplay2.endsWith("+") || bufferDisplay2.endsWith("−") ||
                                                bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷"))
                                            display.append("(");
                                        else
                                            // asumo es una constante como 'π' o 'e'
                                            display.append(")");
                                }

                                is_number = false;
                                has_dot   = false;
                                break;

                            // Dot

                            case R.id.dot:
                                if (is_number && !has_dot) {
                                    display.append(".");
                                    has_dot   = true;
                                    is_number = true;
                                }else
                                if (bufferDisplay2.isEmpty() || bufferDisplay2.endsWith("(") || bufferDisplay2.endsWith("+") ||
                                        bufferDisplay2.endsWith("−") || bufferDisplay2.endsWith("×") || bufferDisplay2.endsWith("÷")) {
                                    display.append("0.");
                                    has_dot   = true;
                                    is_number = true;
                                }

                                //Log.d(DEBUG,"Has dot? "+ (has_dot ? "YES" : "NO"));
                                break;

                            case R.id.undo:
                                c = history.pull();

                                if (c!=null){
                                    display.setFormula("");
                                    display.setText(prettySymbols(c.expression));
                                }

                                return;

                            case R.id.redo:
                                c = history.next();

                                if (c!=null){
                                    display.setFormula("");
                                    display.setText(prettySymbols(c.expression));
                                }
                                return;

                            /*
                                EQUALS

                            */
                            case R.id.equ:

                                String inputExpr = doBalance(display.getText());
                                ///////display.setText(inputExpr);

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
                                    Expression expression = new ExpressionBuilder(inputExpr).function(sum).operator(factorial).build();

                                    double result = expression.evaluate();
                                    String strRes = Double.toString(result);

                                    // Guardo en el historial
                                    history.push(inputExpr,strRes);

                                    has_dot   = (strRes.contains("."));
                                    has_error = (strRes.contains("NaN") || Double.isInfinite(result));
                                    is_number = !has_error;
                                    is_result = true;

                                    if (has_error)
                                        display.setError();
                                    else {

                                        disp1_et.setText("");
                                        disp1_et.append(prettySymbols(inputExpr));
                                        display.setText(formatCurrency(strRes).replace(",",""));

                                        Log.d(DEBUG,"FORMULA: "+display.getFormula());
                                    }


                                    //Log.d(DEBUG,"Has dot? "+ (has_dot ? "YES" : "NO"));

                                } catch (Exception ex) {
                                    display.setError();
                                    Log.d(DEBUG,ex.toString());
                                }

                                break;

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

                                for (String cte : constants) {
                                    if (bufferDisplay2.endsWith(cte)){
                                        display.append("×");
                                        break;
                                    }
                                }

                                //if (digit_count >=4)
                                //    Log.d(DEBUG,"FORMATEAR NUMERO EN "+bufferDisplay2);

                                is_number = true;
                                //digit_count++;

                            default:
                                display.append(newEntry);
                                break;
                        }

                        //
                        Log.d(DEBUG,"FORMULA HASTA AHORA: "+display.getFormula());

                    }
                });


            }


            disableSoftInput((EditText) findViewById(R.id.disp1));
            disableSoftInput((EditText) findViewById(R.id.disp2));

        }



    } // end fn


    private String prettySymbols(String s){
        s = s.replace("-","−");
        s = s.replace("*","×");
        s = s.replace("/","÷");
        s = s.replace("sqrt","√");
        s = s.replace("pi","π");
        s = s.replace("sum(","∑(");
        return s;
    }

    private boolean balancedParentheses(String expr){
        return (countMatches("(",expr)==countMatches(")",expr));
    }

    private void clearMemory(){
        // ...
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
        EditText disp2_et = findViewById(R.id.disp2);

        //save data to clipboard
        ClipData clipData = ClipData.newPlainText("RESULTADO", disp2_et.getText().toString());
        clipboardManager.setPrimaryClip(clipData);

    }

    public void pasteText(){
        EditText disp2_et = findViewById(R.id.disp2);

        if (!clipboardManager.hasPrimaryClip())
            return;

        try{
            ClipData clipData=clipboardManager.getPrimaryClip();

            int curPos = disp2_et.getSelectionStart();
            String textToPaste=clipData.getItemAt(0).getText().toString();

            String oldText = disp2_et.getText().toString();
            String textBeforeCursor  = oldText.substring(0,curPos);
            String textAfterCursor   = oldText.substring(curPos);
            String newText = textBeforeCursor+textToPaste+textAfterCursor;

            disp2_et.setText("");
            disp2_et.append(newText);
            disp2_et.requestFocus();

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
        formatter.setMaximumFractionDigits(6);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        return formatter.format(Double.parseDouble(string));
    }


    // Balancea parentesis de una expresion matematica
    private String doBalance(String expr)
    {
        int diff = countMatches("(",expr)-countMatches(")",expr);

        if (diff>0) {
            for (int i = 0; i < diff; i++)
                expr += ")";
        }

        return expr;
    }


    public static final Function sum = new Function("sum") {
        @Override
        public double apply(double... args) {
            double sum = 0;

            for (int i=0; i<args.length; i++) {
                if ((double) args[i] != args[i]) {
                    throw new IllegalArgumentException(String.format("Argument %d has to be an integer",i));
                }
                if (args[i] < 0) {
                    throw new IllegalArgumentException(String.format("The argument %d can not be less than zero",i));
                }
            }

            if (args.length==1)
                for (int i= 0; i<=(int) args[0];i++) {
                    sum += i;
                }
            else
                if (args.length==2)
                    for (int i= (int) args[0]; i<=(int) args[1];i++) {
                        sum += i;
                    }
                 else
                    throw new IllegalArgumentException("Sum() requires one or two arguments!");

            return sum;
        }
    };

    public static final Function prod = new Function("prod") {
        @Override
        public double apply(double... args) {
            double res = 1;

            for (int i=0; i<args.length; i++) {
                if ((double) args[i] != args[i]) {
                    throw new IllegalArgumentException(String.format("Argument %d has to be an integer",i));
                }
                if (args[i] < 0) {
                    throw new IllegalArgumentException(String.format("The argument %d can not be less than zero",i));
                }
            }

            if (args.length==1)
                for (int i= 0; i<=(int) args[0];i++) {
                    res *= i;
                }
            else
            if (args.length==2)
                for (int i= (int) args[0]; i<=(int) args[1];i++) {
                    res *= i;
                }
            else
                throw new IllegalArgumentException("prod() requires one or two arguments!");

            return res;
        }
    };

    Operator factorial = new Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {

        @Override
        public double apply(double... args) {
            final int arg = (int) args[0];
            if ((double) arg != args[0]) {
                throw new IllegalArgumentException("Operand for factorial has to be an integer");
            }
            if (arg < 0) {
                throw new IllegalArgumentException("The operand of the factorial can not be less than zero");
            }
            double result = 1;
            for (int i = 1; i <= arg; i++) {
                result *= i;
            }
            return result;
        }
    };


    /*
        La idea es sincronizar los cambios con el display1 para las formulas
   */
    class DisplayManger
    {
        private boolean sync   = true;
        private String formula = "";

        final private  EditText disp1_et = findViewById(R.id.disp1);
        final private  EditText disp2_et = findViewById(R.id.disp2);


        public String getText(){
            return disp2_et.getText().toString();
        }

        // debe reemplazar "#" si lo hay por su expresion correspondiente
        public String getFormula(){
            return doBalance(formula);
        }

        public void setFormula(String s){
            formula = s;
        }

        public void setText(String s){
            disp2_et.setText("");
            disp2_et.append(s);

            //Log.d(DEBUG,"ANT. SET TEXT: "+formula);

            if (sync)
                formula = s;

            //Log.d(DEBUG,"DSP. SET TEXT: "+formula);
        }

        public void append(String s){
            disp2_et.append(s);

            Log.d(DEBUG,"ANT. APPEND: "+formula);

            if (sync)
                formula += s;

            Log.d(DEBUG,"DSP. APPEND: "+formula);
        }

        public void backspace(){
            String buffer = disp2_et.getText().toString();

            if (has_error) {
                clear();
                return;
            }

            if (!buffer.isEmpty()) {
                disp2_et.setText(buffer.substring(0, buffer.length() - 1));

                if (buffer.endsWith("."))
                    has_dot = false;
            }

            if (sync){
                if (!formula.isEmpty())
                    formula = formula.substring(0,formula.length()-1);
            }
        }

        public void setError(String msg){
            has_error = true;
            is_number = false;
            has_dot   = false;
            setText(msg);
            Log.d(DEBUG,msg+" *****");
        }

        public void setError(){
            copy();
            setError(" ERROR ");
        }


        public void clear(){
            has_error  = false;
            is_number  = false;
            has_dot    = false;
            //digit_count = 0;
            is_result  = false;
            formula    = "";

            disp1_et.setText("");
            disp2_et.setText("");
        }

        public void copy(){
            String txt = getText();
            disp1_et.setText(txt);
        }


    }



}
