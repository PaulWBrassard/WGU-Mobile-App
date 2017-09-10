package com.example.wgumobile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TermDetailActivity extends AppCompatActivity {
    private Uri uri;
    private String termFilter;
    private String termId;
    private static final int REQUEST_COURSE_LIST = 1;
    private static final int REQUEST_TERM_EDIT = 2;
    private TextView termTitle, termTimes;
    private Button coursesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_detail);

        //Get UI components
        termTitle = (TextView) findViewById(R.id.termDetailTitle);
        termTimes = (TextView) findViewById(R.id.termDetailTimes);
        coursesButton = (Button) findViewById(R.id.courseListButton);
        coursesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TermDetailActivity.this, CoursesListActivity.class);
                Uri uri = Uri.parse(DataProvider.TERMS_CONTENT_URI + "/" + termId);
                intent.putExtra(DataProvider.CONTENT_TERM_ID, uri);
                intent.putExtra(DataProvider.CONTENT_TERM_FILTER, termFilter);
                startActivityForResult(intent, REQUEST_COURSE_LIST);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                //Only delete the term from the db if it has no courses
                boolean hasCourses = termHasCourses();
                if (hasCourses) {
                    Toast.makeText(this, R.string.term_has_courses, Toast.LENGTH_SHORT).show();
                } else {
                    //Delete the term and display advisement message
                    getContentResolver().delete(DataProvider.TERMS_CONTENT_URI, termFilter, null);
                    Toast.makeText(this, R.string.term_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                }
                return true;
            case R.id.action_edit:
                //Send the corresponding term Id to edit activity to ensure correct information loading
                Intent intent = new Intent(TermDetailActivity.this, TermEditActivity.class);
                Uri uri = Uri.parse(DataProvider.TERMS_CONTENT_URI + "/" + termId);
                intent.putExtra(DataProvider.CONTENT_TERM_ID, uri);
                startActivityForResult(intent, REQUEST_TERM_EDIT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TERM_EDIT && resultCode == RESULT_OK) {
            //Term was deleted in editing activity so leave details as they no longer exist
            this.finish();
        }
    }

    @Override
    protected void onResume() {
        loadTermData();
        super.onResume();
    }

    //Load term details corresponding to received term id
    private void loadTermData() {
        Intent intent = getIntent();
        Uri intentUri = intent.getParcelableExtra(DataProvider.CONTENT_TERM_ID);
        //Only load the term details if a term id was received
        if (intentUri != null) {
            uri = intentUri;
            termId = uri.getLastPathSegment();
            termFilter = DBOpenHelper.TERM_ID + "=" + termId;
            Cursor cursor = getContentResolver().query(uri, DBOpenHelper.ALL_TERM_COLUMNS,
                    termFilter, null, null);
            cursor.moveToFirst();
            String title = cursor.getString(cursor.getColumnIndex(DBOpenHelper.TERM_TITLE));
            String start = cursor.getString(cursor.getColumnIndex(DBOpenHelper.TERM_START));
            String end = cursor.getString(cursor.getColumnIndex(DBOpenHelper.TERM_END));
            String times = start + " - " + end;
            cursor.close();

            termTitle.setText(title);
            termTimes.setText(times);
        }
    }

    //Return a boolean reporting if the term has any courses
    private boolean termHasCourses() {
        boolean result;
        Uri courseUri = DataProvider.COURSES_CONTENT_URI;
        String[] courseColumns = {DBOpenHelper.COURSE_ID};
        //Pull courses that have this term's id
        String courseFilter = DBOpenHelper.COURSE_TERM_ID + "=" + termId;
        Cursor cursor = getContentResolver().query(courseUri, courseColumns,
                courseFilter, null, null);
        //No data from the db access means there are no corresponding courses
        result = cursor.getCount() != 0;
        cursor.close();
        return result;
    }
}
