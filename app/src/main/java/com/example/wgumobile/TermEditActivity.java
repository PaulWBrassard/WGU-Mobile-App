package com.example.wgumobile;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TermEditActivity extends AppCompatActivity {

    private Pattern datePattern = Pattern.compile(DateWatcher.DATE_FORMAT);
    private final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("MM/dd/yy");
    private Matcher matcher;
    private boolean editing;
    private String termFilter;
    private String termId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_edit);

        //Get UI components
        final EditText editTitle = (EditText) findViewById(R.id.editTermTitle);
        final EditText editStart = (EditText) findViewById(R.id.editTermStart);
        editStart.addTextChangedListener(new DateWatcher(editStart));
        final EditText editEnd = (EditText) findViewById(R.id.editTermEnd);
        editEnd.addTextChangedListener(new DateWatcher(editEnd));
        Button submit = (Button) findViewById(R.id.buttonSubmitTerm);

        //If we are editing existing, load term details into editor.
        Intent intent = getIntent();
        Uri intentUri = intent.getParcelableExtra(DataProvider.CONTENT_TERM_ID);
        if (intentUri != null) {
            editing = true;
            termId = intentUri.getLastPathSegment();
            termFilter = DBOpenHelper.TERM_ID + "=" + termId;
            Cursor cursor = getContentResolver().query(intentUri, DBOpenHelper.ALL_TERM_COLUMNS,
                    termFilter, null, null);
            cursor.moveToFirst();
            String title, start, end;
            title = cursor.getString(cursor.getColumnIndex(DBOpenHelper.TERM_TITLE));
            start = cursor.getString(cursor.getColumnIndex(DBOpenHelper.TERM_START));
            end = cursor.getString(cursor.getColumnIndex(DBOpenHelper.TERM_END));
            editTitle.setText(title);
            editStart.setText(start);
            editEnd.setText(end);
        } else editing = false;

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check that dates are in the appropriate format.
                String title, start, end;
                boolean startMatches, endMatches, validTitle;
                start = editStart.getText().toString();
                end = editEnd.getText().toString();
                matcher = datePattern.matcher(start);
                startMatches = matcher.matches();
                matcher = datePattern.matcher(end);
                endMatches = matcher.matches();
                long startTime = 0, endTime = 0;
                try {
                    startTime = DATEFORMAT.parse(editStart.getText().toString()).getTime();
                    endTime = DATEFORMAT.parse(editEnd.getText().toString()).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                boolean validTimeSegment = startTime < endTime; //Course must start before it ends

                //Check that there is a title entered.
                title = editTitle.getText().toString();
                validTitle = (title != null && !title.isEmpty());
                //Save term if the fields are correctly filled.
                if (startMatches && endMatches && validTitle && validTimeSegment) {
                    if (editing) updateTerm(title, start, end); //Update existing term
                    else insertTerm(title, start, end); //Insert new term into db
                    finish();
                } else {
                    Snackbar snackbar;
                    if (!validTimeSegment) {
                        //Term does not start before it ends
                        snackbar = Snackbar.make(view, R.string.start_before_end, Snackbar.LENGTH_SHORT);
                    } else {
                        if (validTitle) {
                            //Date format is invalid
                            snackbar = Snackbar.make(view, R.string.wrong_date_format, Snackbar.LENGTH_SHORT);
                        } else {
                            //Invalid title
                            snackbar = Snackbar.make(view, R.string.no_term_title, Snackbar.LENGTH_SHORT);
                        }
                    }
                    snackbar.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                if (editing) {
                    //Only delete terms that do not have courses
                    if (termHasCourses()) {
                        Toast.makeText(this, R.string.term_has_courses, Toast.LENGTH_SHORT).show();
                    } else {
                        //Delete term from the db
                        String where = DBOpenHelper.TERM_ID + "= ?";
                        String[] selectionArgs = {termId};
                        getContentResolver().delete(DataProvider.TERMS_CONTENT_URI,
                                where, selectionArgs);
                        Toast.makeText(this, R.string.term_deleted, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK, null);
                        finish();
                    }
                }
                return true;
            case android.R.id.home:
                //Handle action bar back arrow same as device back button
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Insert a new term into the db
    private void insertTerm(String title, String start, String end) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.TERM_TITLE, title);
        values.put(DBOpenHelper.TERM_START, start);
        values.put(DBOpenHelper.TERM_END, end);
        getContentResolver().insert(DataProvider.TERMS_CONTENT_URI, values);
    }

    //Update an existing term in the db
    private void updateTerm(String title, String start, String end) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.TERM_TITLE, title);
        values.put(DBOpenHelper.TERM_START, start);
        values.put(DBOpenHelper.TERM_END, end);
        String where = DBOpenHelper.TERM_ID + "= ?";
        String[] selectionArg = {termId};
        getContentResolver().update(DataProvider.TERMS_CONTENT_URI, values,
                where, selectionArg);
    }

    //Return boolean reporting if the term has any courses
    private boolean termHasCourses() {
        boolean result;
        Uri courseUri = DataProvider.COURSES_CONTENT_URI;
        String[] courseColumns = {DBOpenHelper.COURSE_ID};
        String courseFilter = DBOpenHelper.COURSE_TERM_ID + "=" + termId;
        Cursor cursor = getContentResolver().query(courseUri, courseColumns,
                courseFilter, null, null);
        result = cursor.getCount() != 0;
        cursor.close();
        return result;
    }
}
