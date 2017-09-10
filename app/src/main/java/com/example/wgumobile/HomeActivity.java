package com.example.wgumobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    private final static String PREFERENCE = "WGUStudent";
    private final static String STUDENT_NAME = "studentName";
    private TextView greeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Define UI Programmatically
        LinearLayout layout = new LinearLayout(this);
        ImageView image = new ImageView(this);
        image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.owlwgu));
        greeting = new TextView(this);
        greeting.setText(getString(R.string.welcome) + " " + getStudentName());
        Button termsListButton = new Button(this);
        termsListButton.setText(getString(R.string.view_terms));
        ArrayList<View> uiComponents = new ArrayList<>();
        uiComponents.add(image);
        uiComponents.add(greeting);
        uiComponents.add(termsListButton);

        //Configure UI layout

        //Orientation independent details
        greeting.setGravity(Gravity.CENTER);
        termsListButton.setGravity(Gravity.CENTER);
        //Orientation specific details
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER_HORIZONTAL);
            greeting.setPadding(0, 0, 0, 50);
            greeting.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            termsListButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        } else { //Landscape
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            image.setPadding(200, 0, 0, 0);
            greeting.setPadding(50, 0, 100, 0);
        }

        //Connect UI components
        for (View v : uiComponents) {
            layout.addView(v);
        }
        setContentView(layout);

        //Handle request to view terms
        termsListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, TermsListActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_user:
                //Allow user to enter their name for display
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.enter_name);
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("ENTER", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Send user response to be saved
                        String response = input.getText().toString();
                        setStudentName(response);
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Use SharedPreferences to set User's name
    private void setStudentName(String name) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFERENCE, MODE_PRIVATE).edit();
        editor.putString(STUDENT_NAME, name);
        editor.apply();
        greeting.setText(getString(R.string.welcome) + " " + name);
    }

    //Use SharedPreferences to get User's name
    private String getStudentName() {
        SharedPreferences shared = getSharedPreferences(PREFERENCE, MODE_PRIVATE);
        String name = shared.getString(STUDENT_NAME, getString(R.string.student));
        return name;
    }
}
