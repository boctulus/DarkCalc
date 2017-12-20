package com.example.pc.easycalc;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private double total = Double.NaN;
    private double val1;
    private double val2;

    private char op1;
    private char op2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //to remove "information bar" above the action bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //to remove the action bar (title bar)
        getSupportActionBar().hide();


        TextView display = findViewById(R.id.display);
        display.setText("");

        LinearLayout root = (LinearLayout) findViewById(R.id.root);

        for (int rix = 0; rix< root.getChildCount(); rix++){
            View elem = (View) root.getChildAt(rix);

            // el TextView no es un contenedor
            if (elem.getId() == R.id.display)
                continue;

            LinearLayout row = (LinearLayout) elem;

            int childCount = row.getChildCount();
            for (int i= 0; i < childCount; i++){
                Button boton = (Button) row.getChildAt(i);
                //boton.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));

                boton.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v){
                        TextView display = findViewById(R.id.display);
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

        }


    } // end fn



}
