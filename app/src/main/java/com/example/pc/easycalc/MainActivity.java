package com.example.pc.easycalc;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.TextView;

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
    private int     deviceWidth;
    private int     deviceHeight;

    private boolean has_error  = false;
    private boolean is_number  = false;
    private int     number_len = 0;
    private boolean has_dot    = false;
    private double  acc        = 0;
    private String  formula    = "";

    private DisplayManger display;
    private HistoryManager   history  = new HistoryManager();
    private ClipboardManager clipboardManager;

    private static final int[] longClickFunctions = new int[] { R.id.del, R.id.sqr, R.id.par, R.id.sin,
            R.id.cos, R.id.tan, R.id.p2x, R.id.p10x,
            R.id.pex, R.id.log2, R.id.ln, R.id.log,
            R.id.fact, R.id.sum, R.id.pxy  };

    private static final String DEBUG = "DEBUG";


    private void hideBar(){
        //to remove "information bar" above the action bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //to remove the action bar (title bar)

        getSupportActionBar().hide();
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        TextView rdisp1 = findViewById(R.id.disp1);
        savedInstanceState.putString("disp1",rdisp1.getText().toString());

        AutoResizeEditText rdisp2 = findViewById(R.id.disp2);
        savedInstanceState.putString("disp2",rdisp2.getText().toString());

        savedInstanceState.putBoolean("has_error",has_error);
        savedInstanceState.putBoolean("has_dot",has_dot);
        savedInstanceState.putBoolean("is_number",is_number);
        savedInstanceState.putDouble("acc",acc);
        savedInstanceState.putInt("number_len",number_len);

        //////////////////////////////////////////////////////////////
        savedInstanceState.putParcelable("historia", history);
        //////////////////////////////////////////////////////////////

        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        String rdisp1 = savedInstanceState.getString("disp1");
        TextView disp1 = findViewById(R.id.disp1);
        disp1.setText("");
        disp1.append(rdisp1);

        String rdisp2 = savedInstanceState.getString("disp2");
        EditText disp2 = findViewById(R.id.disp2);
        disp2.setText("");
        disp2.append(rdisp2);

        has_error = savedInstanceState.getBoolean("has_error");
        has_dot = savedInstanceState.getBoolean("has_dot");
        is_number = savedInstanceState.getBoolean("is_number");
        acc = savedInstanceState.getDouble("acc");
        number_len = savedInstanceState.getInt("number_len");

        //////////////////////////////////////////////////////////////
        history =  savedInstanceState.getParcelable("historia");
        //////////////////////////////////////////////////////////////

    }



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideBar();

        // Reserved keywords
        String[] constant_lst = new String[]{"Pi","PI","π","e","NA"};
        String[] function_lst = new String[]{"sin","cos","tan","asin","acos","atan", "sinh","cosh","tanh","asinh","acosh","atanh","sum","prod","ln","log","log2","log10"};

        final List<String> functions = Arrays.asList(function_lst);
        final List<String> constants = Arrays.asList(constant_lst);
        display = new DisplayManger();

        final TextView           disp1_et = findViewById(R.id.disp1);
        final AutoResizeEditText disp2_et = findViewById(R.id.disp2);

        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);


        //disp1_et.setEnabled(true);
        //disp1_et.setFocusableInTouchMode(true);
        //disp1_et.setFocusable(true);
        //disp1_et.requestFocus();
        //disp1_et.setMovementMethod(null);
        disp1_et.setCursorVisible(false);
        disp1_et.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);


        disp2_et.setEnabled(true);
        disp2_et.setFocusable(true);
        disp2_et.setFocusableInTouchMode(true);
        disp2_et.requestFocus();
        disp2_et.setEnableSizeCache(false);
        //disp2_et.setMovementMethod(null);
        disp2_et.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);


        // Register the context menu to the the EditText
        //registerForContextMenu(disp1_et);
        registerForContextMenu(disp2_et);

        final LinearLayout root             = findViewById(R.id.root);
        final LinearLayout displayLayout    = findViewById(R.id.display);
        final LinearLayout kbLayout         = findViewById(R.id.keyboard);
        final LinearLayout disp1_contLayout = findViewById(R.id.disp1Container);
        final LinearLayout disp2_contLayout = findViewById(R.id.disp2Container);

        /*
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout(){
                deviceWidth  = root.getWidth();
                deviceHeight = root.getHeight();

                LinearLayout row = findViewById(R.id.buttonRow2);

                if (isPortait() && ((double) deviceHeight / deviceWidth < 1.65)){
                    row.setVisibility(LinearLayout.GONE);
                }

            }
        });
        */


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


        for (int rfn : longClickFunctions){

            try {

                findViewById(rfn).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (display.getText().isEmpty())
                            return false;

                        Button b = (Button) v;
                        String fn = b.getText().toString();

                        //history.push(display.getText(),"");
                        boolean alredy_op = display.getText().startsWith("(");

                        switch (v.getId()) {

                            case R.id.del:
                                clear();
                                break;

                            case R.id.par:
                                display.setText((!alredy_op ? "(" : "") + display.getText() + (!alredy_op ? ")" : ""));
                                break;

                            case R.id.fact:
                                display.setText((!alredy_op ? "(" : "") + display.getText() + (!alredy_op ? ")!" : "!"));
                                break;

                            case R.id.p2x:
                                display.setText("2^"+ (!alredy_op ? "(" : "") + display.getText() + (!alredy_op ? ")" : ""));
                                break;

                            case R.id.p10x:
                                display.setText("10^"+  (!alredy_op ? "(" : "") + display.getText() + (!alredy_op ? ")" : ""));
                                break;

                            case R.id.pex:
                                display.setText("e^"+ (!alredy_op ? "(" : "") + display.getText() + (!alredy_op ? ")" : ""));
                                break;

                            case R.id.pxy:
                                display.setText((!alredy_op ? "(" : "") + display.getText() + (!alredy_op ? ")^" : "^"));
                                break;

                            default:
                                display.setText(fn + (!alredy_op ? "(" : "") + display.getText() + (!alredy_op ? ")" : ""));
                                break;

                        }

                        return true;

                    }
                });
            }catch (Exception ex) {

            }
        }


        for (int rix = 0; rix< kbLayout.getChildCount(); rix++){
            final View elem = (View) kbLayout.getChildAt(rix);

            LinearLayout row = (LinearLayout) elem;

            int childCount = row.getChildCount();
            for (int i= 0; i < childCount; i++){
                Button boton = (Button) row.getChildAt(i);

                boton.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v){
                        Button b = (Button) v;

                        String newEntry = b.getText().toString();

                        Calculation c;

                        if (has_error)
                            clear();

                        String bufferDisplay2 = display.getText();

                        switch ( b.getId())
                        {
                            case R.id.c:
                                clear();
                                break;

                            case R.id.mc:
                                acc = 0;
                                break;

                            case R.id.ms:
                                try {
                                    acc = Double.valueOf(display.getText());
                                }catch(NumberFormatException e) {

                                }
                                break;
                            /*
                            case R.id.mp:
                                try {
                                    acc += Double.valueOf(display.getText());
                                }catch(NumberFormatException e) {

                                }
                                break;

                            case R.id.mm:
                                if (is_number)
                                    acc-=Double.valueOf(display.getText());
                                break;
                            */
                            case R.id.mr:
                                try
                                {
                                    String strval = String.valueOf(acc);
                                    Double.parseDouble(strval);

                                    strval = formatCurrency(strval).replace(",","");

                                    if (strval.equals("0"))
                                        break;

                                    if (is_number)
                                        display.append("+"+strval);
                                    else
                                        display.append(strval);

                                    is_number = true;
                                    number_len= strval.length();
                                }
                                catch(NumberFormatException e)
                                {
                                    //not a double
                                }
                                break;

                            case R.id.del:
                                display.backspace();
                                return;

                            // Operators

                            case R.id.add:
                                if (!isPrevChar("+"))
                                    if (isPrevChar("−") || isPrevChar("×") ||
                                            isPrevChar("÷")  ) {
                                        display.backspace();
                                        display.appendAfterCursor("+");
                                    }
                                    else
                                        display.appendAfterCursor(newEntry);

                                // esta parte debe cambiar:
                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.sub:
                                if (!isPrevChar("−"))
                                    if (isPrevChar("+")) {
                                        display.backspace();
                                        display.appendAfterCursor("−");
                                    }else
                                    if (isPrevChar("×") || isPrevChar("÷"))
                                        display.appendAfterCursor("(−"); // hace falta el "(" ?
                                    else
                                        display.appendAfterCursor("−");

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.mul:
                                if (bufferDisplay2.isEmpty())
                                    return;

                                if (!isPrevChar("×"))
                                    if (isPrevChar("+") || isPrevChar("−") || isPrevChar("÷") ) {
                                        display.backspace();
                                        display.appendAfterCursor("×");
                                    }else
                                        display.appendAfterCursor(newEntry);

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.div:
                                if (bufferDisplay2.isEmpty())
                                    return;

                                if (!isPrevChar("÷"))
                                    if (isPrevChar("+") || isPrevChar("−") || isPrevChar("×") ) {
                                        display.backspace();
                                        display.appendAfterCursor("÷");
                                    }else
                                        display.appendAfterCursor(newEntry);

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.per:
                                if (bufferDisplay2.isEmpty() || bufferDisplay2.contains("%"))
                                    return;

                                display.appendAfterCursor(newEntry);

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            // Functions

                            case R.id.inv:
                                if (bufferDisplay2.isEmpty())
                                    display.appendAfterCursor("1÷(");
                                else {
                                    display.setText("");
                                    display.appendAfterCursor("1÷("+bufferDisplay2+")");
                                }

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.sqr:
                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (isPrevChar("(") || isPrevChar("+") || isPrevChar("−") ||
                                        isPrevChar("×") || isPrevChar("÷"))
                                    display.appendAfterCursor(newEntry+"(");
                                else
                                    display.appendAfterCursor("×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.sin:
                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (isPrevChar("(") || isPrevChar("+") || isPrevChar("−") ||
                                        isPrevChar("×") || isPrevChar("÷"))
                                    display.appendAfterCursor(newEntry+"(");
                                else
                                    display.appendAfterCursor("×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.cos:
                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (isPrevChar("(") || isPrevChar("+") || isPrevChar("−") ||
                                        isPrevChar("×") || isPrevChar("÷"))
                                    display.appendAfterCursor(newEntry+"(");
                                else
                                    display.appendAfterCursor("×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.tan:
                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (isPrevChar("(") || isPrevChar("+") || isPrevChar("−") ||
                                        isPrevChar("×") || isPrevChar("÷"))
                                    display.appendAfterCursor(newEntry+"(");
                                else
                                    display.appendAfterCursor("×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.ln:
                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (isPrevChar("(") || isPrevChar("+") || isPrevChar("−") ||
                                        isPrevChar("×") || isPrevChar("÷"))
                                    display.appendAfterCursor(newEntry+"(");
                                else
                                    display.appendAfterCursor("×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.log2:
                                if (bufferDisplay2.isEmpty())
                                    display.setText("log2"+"(");
                                else
                                if (isPrevChar("(") || isPrevChar("+") || isPrevChar("−") ||
                                        isPrevChar("×") || isPrevChar("÷"))
                                    display.appendAfterCursor("log2"+"(");
                                else
                                    display.appendAfterCursor("×"+"log2"+"(");

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.log:
                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (isPrevChar("(") || isPrevChar("+") || isPrevChar("−") ||
                                        isPrevChar("×") || isPrevChar("÷"))
                                    display.appendAfterCursor(newEntry+"(");
                                else
                                    display.appendAfterCursor("×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.pex:
                                if (bufferDisplay2.isEmpty())
                                    display.setText("e^"+"(");
                                else
                                if (isPrevChar("(") || isPrevChar("+") || isPrevChar("−") ||
                                        isPrevChar("×") || isPrevChar("÷"))
                                    display.appendAfterCursor("e^(");
                                else
                                    display.appendAfterCursor("×e^(");

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.p2x:
                                if (bufferDisplay2.isEmpty())
                                    display.setText("2^"+"(");
                                else
                                if (isPrevChar("(") || isPrevChar("+") || isPrevChar("−") ||
                                        isPrevChar("×") || isPrevChar("÷"))
                                    display.appendAfterCursor("2^"+"(");
                                else
                                    display.appendAfterCursor("×2^"+"(");

                                break;

                            case R.id.p10x:
                                if (bufferDisplay2.isEmpty())
                                    display.setText("10^"+"(");
                                else
                                if (isPrevChar("(") || isPrevChar("+") || isPrevChar("−") ||
                                        isPrevChar("×") || isPrevChar("÷"))
                                    display.appendAfterCursor("10^"+"(");
                                else
                                    display.appendAfterCursor("×10^"+"(");

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.pxy:
                                if (bufferDisplay2.isEmpty() || isPrevChar("(") || isPrevChar("+") ||
                                        isPrevChar("−") || isPrevChar("×") || isPrevChar("÷")) {
                                    showError();
                                }
                                else
                                    display.appendAfterCursor("^(");

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            // Custom functions

                            case R.id.sum:
                                if (bufferDisplay2.isEmpty())
                                    display.setText(newEntry+"(");
                                else
                                if (isPrevChar("(") || isPrevChar("+") || isPrevChar("−") ||
                                        isPrevChar("×") || isPrevChar("÷"))
                                    display.appendAfterCursor(newEntry+"(");
                                else
                                    display.appendAfterCursor("×"+newEntry+"(");

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            case R.id.fact:
                                if (bufferDisplay2.isEmpty())
                                    return;
                                else
                                    display.appendAfterCursor("!");
                                break;

                            /*
                            case R.id.prod:
                                if (bufferDisplay2.isEmpty())
                                    display.setText("prod"+"(");
                                else
                                    display.appendAfterCursor("×"+"prod"+"(");

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
                                if (isPrevChar("(") || isPrevChar("+") || isPrevChar("−") ||
                                        isPrevChar("×") || isPrevChar("÷"))
                                    display.appendAfterCursor(newEntry);
                                else
                                    display.appendAfterCursor("×"+newEntry);

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            // Parentheses

                            case R.id.par:

                                if (bufferDisplay2.isEmpty())
                                    display.appendAfterCursor("(");
                                else
                                if (isPrevChar("("))
                                    display.appendAfterCursor("(");
                                else
                                if (is_number) {
                                    if (balancedParentheses(bufferDisplay2))
                                        display.appendAfterCursor("×(");
                                    else
                                        display.appendAfterCursor(")");
                                }else
                                if (isPrevChar(")"))
                                    if (balancedParentheses(bufferDisplay2))
                                        display.appendAfterCursor("×(");
                                    else
                                        display.appendAfterCursor(")");
                                else {
                                    boolean fn_found = false;
                                    for (String fn : functions) {
                                        if (isPrevChar(fn)){
                                            display.appendAfterCursor("(");
                                            fn_found = true;
                                            break;
                                        }
                                    }
                                    if (!fn_found)
                                        if (isPrevChar("+") || isPrevChar("−") ||
                                                isPrevChar("×") || isPrevChar("÷"))
                                            display.appendAfterCursor("(");
                                        else
                                            // asumo es una constante como 'π' o 'e'
                                            display.appendAfterCursor(")");
                                }

                                is_number = false;
                                has_dot   = false;
                                number_len= 0;
                                break;

                            // Dot

                            case R.id.dot:
                                if (is_number && !has_dot) {
                                    display.appendAfterCursor(".");
                                    has_dot   = true;
                                    is_number = true;
                                    number_len++;
                                }else
                                if (bufferDisplay2.isEmpty() || isPrevChar("(") || isPrevChar("+") ||
                                        isPrevChar("−") || isPrevChar("×") || isPrevChar("÷")) {
                                    display.appendAfterCursor("0.");
                                    has_dot   = true;
                                    is_number = true;
                                    number_len= 2;
                                }

                                //Log.d(DEBUG,"Has dot? "+ (has_dot ? "YES" : "NO"));
                                break;

                            case R.id.undo:
                                if (history.hasPrev()){
                                    c = history.pull();
                                    setFormula(c.result);
                                    display.setText(prettySymbols(c.expression));


                                }
                                return;

                            case R.id.redo:
                                if (history.hasNext()){
                                    c = history.next();
                                    setFormula(c.result);
                                    display.setText(prettySymbols(c.expression));

                                }
                                return;

                            /*
                                EQUALS

                            */
                            case R.id.equ:

                                String inputExpr = doBalance(display.getText());

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
                                    String inputExpr_preparser = inputExpr;
                                    inputExpr = preCalcParser(inputExpr);
                                    //Log.d(DEBUG,"EXPRESION: "+inputExpr);

                                    Expression expression = new ExpressionBuilder(inputExpr).function(sum).function(percent).operator(factorial).build();

                                    double result = expression.evaluate();
                                    String strRes = Double.toString(result);

                                    // Guardo en el historial
                                    history.push(inputExpr_preparser,strRes);

                                    has_dot   = (strRes.contains("."));
                                    has_error = (strRes.contains("NaN") || Double.isInfinite(result));
                                    is_number = !has_error;

                                    if (is_number)
                                        number_len= strRes.length();

                                    if (has_error)
                                        showError();
                                    else {

                                        disp1_et.setText("");
                                        disp1_et.append(prettySymbols(inputExpr));
                                        display.setText(formatCurrency(strRes).replace(",",""));

                                        //Log.d(DEBUG,"FORMULA: "+display.getFormula());
                                    }


                                } catch (Exception ex) {
                                    showError();
                                    //Log.d(DEBUG,"ERROR: "+ex.toString());
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

                                // "00"
                                if (newEntry.equals("0") && display.getText().equals("0"))
                                    return;

                                // "00"
                                if (newEntry.equals("0") && number_len==1 && display.getText().endsWith("0"))
                                    return;

                                number_len++;

                                for (String cte : constants) {
                                    if (isPrevChar(cte)){
                                        display.appendAfterCursor("×");
                                        break;
                                    }
                                }

                                is_number = true;

                            default:
                                display.appendAfterCursor(newEntry);
                                break;
                        }

                        //
                        //Log.d(DEBUG,"FORMULA HASTA AHORA: "+display.getFormula());
                        displayLayout.requestFocus();
                        displayLayout.requestFocusFromTouch();

                    }
                });


            }


            //disableSoftInput((EditText) findViewById(R.id.disp1));
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

            /*
             Deberia parsear la expresion y determinar si termina con una cifra para setear
             is_number , has_dot y number_len
              */

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

    public boolean isPortait()
    {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private static String implode(String[] arr, String glue, int cant)
    {
        boolean first = true;
        StringBuilder str = new StringBuilder();
        for (int i=0;i<cant;i++) {
            if (!first) str.append(glue);
            str.append(arr[i]);
            first = false;
        }
        return str.toString();
    }

    private static String preCalcParser(String s){
        String[] expr_lst;
        String monto;
        String inc;

        String operators = "-+%";
        expr_lst = s.split("(?=["+operators+"])|(?<=["+operators+"])");

        if (expr_lst[expr_lst.length-1].equals("%")){
            inc   = expr_lst[expr_lst.length-3]+expr_lst[expr_lst.length-2];
            monto = implode(expr_lst,"",expr_lst.length-3);
            s = String.format("percent(%s,%s)",monto,inc);
        }
        return s;
    }


    /* Custom math functions */


    public static final Function percent = new Function("percent",2) {
        @Override
        public double apply(double... args) {
            double monto = args[0];
            double inc = args[1];

            return monto*(1 + inc*0.01);
        }
    };


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

    private String prevChar(){
        EditText d2et      = findViewById(R.id.disp2);
        String buffer      = d2et.getText().toString();
        int cursorPosition = d2et.getSelectionStart();

        return (buffer.length()>0 ?  buffer.substring(cursorPosition-1,cursorPosition) : "");
    }

    private boolean isPrevChar(String s){
        return prevChar().equals(s);
    }

    // Necesita hacer referencia a la posicion del cursor
    private boolean isLastCharDigit(){
        String s = prevChar();
        return  (s.equals("0") || s.equals("1")  || s.equals("2")  || s.equals("3")  || s.equals("4")  || s.equals("5")  ||
                s.equals("6")  || s.equals("7")  || s.equals("8") || s.equals("9") );
    }

    // Clear both displays
    private void clear(){
        display.clear();
        TextView disp1_et = findViewById(R.id.disp1);
        disp1_et.setText("");
    }

    public void showError(){
        copyToFormulaDisplay();
        display.setError(" ERROR ");
    }

    private void copyToFormulaDisplay(){
        TextView disp1_et = findViewById(R.id.disp1);
        String txt = display.getText();
        disp1_et.setText(txt);
    }


    /*
        Los siguientes metodos podrian incluirse en una clase Formula que extenderia a TextView
     */
    private void setFormula(String s){
        final TextView disp1_et = findViewById(R.id.disp1);
        formula = s;
        disp1_et.setText("");
        disp1_et.append(s);
    }

    // debe reemplazar "#" si lo hay por su expresion correspondiente
    public String getFormula(){
        return doBalance(formula);
    }


    /*
        Debe pasar a ser una clase que extienda a AutoResizeEditTExt
        sin referencias a otro display (no acoplada)
   */
    class DisplayManger
    {
        final private  EditText d2et = findViewById(R.id.disp2);
        //  private boolean sync   = true;

        public String getText(){
            return d2et.getText().toString();
        }

        public void setText(String s){

            d2et.setText("");
            d2et.append(s);

            //if (sync)
            //   formula = s;

            //display.requestFocus();
            //display.requestFocusFromTouch();
        }

        public void append(String s){

            d2et.append(s);
            d2et.setSelection(d2et.getText().toString().length());

            //if (sync)
            //   formula += s;
        }

        public void appendAfterCursor(String s){
            if (s.isEmpty())
                throw new RuntimeException("String vacio");

            String buffer = getText();
            int cursorPosition = d2et.getSelectionStart();

            String strtmp0 = buffer.substring(0, cursorPosition);
            d2et.setText("");
            d2et.append(strtmp0);

            d2et.append(s);
            if (buffer.length() >= cursorPosition) {
                String strtmp1 = buffer.substring(cursorPosition, buffer.length());
                d2et.append(strtmp1);
            }

            d2et.setSelection(cursorPosition+1);
        }

        public void backspace(){
            String buffer   = d2et.getText().toString();

            int cursorPosition = d2et.getSelectionStart();
            Log.d(DEBUG,"Pos: "+String.valueOf(cursorPosition)); ///

            if (has_error) {
                clear();
                return;
            }

            if (!buffer.isEmpty() && cursorPosition!=0) {

                String strtmp0 = buffer.substring(0, cursorPosition-1);
                d2et.setText("");
                d2et.append(strtmp0);

                if (buffer.length() >= cursorPosition) {
                    String strtmp1 = buffer.substring(cursorPosition, buffer.length());
                    d2et.append(strtmp1);
                }

                d2et.setSelection(cursorPosition-1);

                // ya no seria endsWidth pues es respecto del cursor
                // tampoco tiene sentido mantener "has_dot" excepto se utilice para el ultimo numero de la expresion
                if (buffer.endsWith("."))
                    has_dot = false;
                else
                if (isLastCharDigit()){
                    number_len--;
                }
            }

            // puede haber borrado el ultimo caracter
            if (getText().isEmpty()) {
                is_number  = false;
                number_len = 0;
            }


            //if (sync){
            //    if (!formula.isEmpty())
            //        formula = formula.substring(0,formula.length()-1);
            //}

            //display.requestFocus();
            //display.requestFocusFromTouch();
        }

        public void setError(String msg){
            has_error = true;
            is_number = false;
            has_dot   = false;
            setText(msg);
            //Log.d(DEBUG,msg+" *****");
        }


        public void clear(){
            has_error  = false;
            is_number  = false;
            has_dot    = false;
            number_len = 0;
            //formula    = "";
            d2et.setText("");
        }



    }



}
