package com.example.wgumobile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CourseDetailActivity extends AppCompatActivity {
    private Uri uri;
    private String courseFilter;
    private String courseId, termId;
    private static final int REQUEST_NOTES = 1;
    private static final int REQUEST_COURSE_EDIT = 2;
    private static final int REQUEST_ASSESSMENT_LIST = 3;
    private TextView termTitle, termTimes, courseStatus, mentorInfo;
    private Button assessmentsButton, notesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        //Get UI components
        termTitle = (TextView) findViewById(R.id.courseDetailTitle);
        termTimes = (TextView) findViewById(R.id.courseDetailTimes);
        courseStatus = (TextView) findViewById(R.id.courseDetailStatus);
        mentorInfo = (TextView) findViewById(R.id.courseDetailMentor);
        assessmentsButton = (Button) findViewById(R.id.assessmentListButton);
        assessmentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseDetailActivity.this, AssessmentsListActivity.class);
                Uri uri = Uri.parse(DataProvider.COURSES_CONTENT_URI + "/" + courseId);
                intent.putExtra(DataProvider.CONTENT_COURSE_ID, uri);
                intent.putExtra(DataProvider.CONTENT_COURSE_FILTER, courseFilter);
                startActivityForResult(intent, REQUEST_ASSESSMENT_LIST);
            }
        });
        notesButton = (Button) findViewById(R.id.courseNotesButton);
        notesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseDetailActivity.this, NotesActivity.class);
                Uri uri = Uri.parse(DataProvider.COURSES_CONTENT_URI + "/" + courseId);
                intent.putExtra(DataProvider.CONTENT_COURSE_ID, uri);
                intent.putExtra(DataProvider.CONTENT_COURSE_FILTER, courseFilter);
                startActivityForResult(intent, REQUEST_NOTES);
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
            case R.id.action_edit:
                //Pass course id and term id to edit activity for information loading
                Intent intent = new Intent(CourseDetailActivity.this, CourseEditActivity.class);
                Uri courseUri = Uri.parse(DataProvider.COURSES_CONTENT_URI + "/" + courseId);
                intent.putExtra(DataProvider.CONTENT_COURSE_ID, courseUri);
                Uri termUri = Uri.parse(DataProvider.TERMS_CONTENT_URI + "/" + termId);
                intent.putExtra(DataProvider.CONTENT_TERM_ID, termUri);
                startActivityForResult(intent, REQUEST_COURSE_EDIT);
                return true;
            case R.id.action_delete:
                //Remove course from the db
                getContentResolver().delete(DataProvider.COURSES_CONTENT_URI, courseFilter, null);
                Toast.makeText(this, R.string.course_deleted, Toast.LENGTH_SHORT).show();
                finishWithResult();
                return true;
            case android.R.id.home:
                //Handle action bar back arrow same as device back button
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_COURSE_EDIT && resultCode == RESULT_OK) {
            //Edit resulted in deletion, leave details as they no longer exist
            setResult(RESULT_OK, data);
            this.finish();
        }
        if (requestCode == REQUEST_COURSE_EDIT && resultCode == RESULT_CANCELED) {
            //Load course id to ensure correct detail of information
            if (data != null)
                uri = data.getParcelableExtra(DataProvider.CONTENT_COURSE_ID);
        }
    }

    @Override
    public void onBackPressed() {
        finishWithResult();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        loadCourseData();
        super.onResume();
    }

    //Finish with setting the term id as a result to ensure accurate information loading
    private void finishWithResult() {
        Intent intent = new Intent();
        Uri uri = Uri.parse(DataProvider.TERMS_CONTENT_URI + "/" + termId);
        intent.putExtra(DataProvider.CONTENT_TERM_ID, uri);
        setResult(RESULT_OK, intent);
        finish();
    }

    //Load all course details corresponding to the received course id
    private void loadCourseData() {
        Intent intent = getIntent();
        Uri intentUri = intent.getParcelableExtra(DataProvider.CONTENT_COURSE_ID);
        //Only attempt to load info from database if course id has been received
        if (intentUri != null) {
            uri = intentUri;
            courseId = uri.getLastPathSegment();
            courseFilter = DBOpenHelper.COURSE_ID + "=" + courseId;
            Cursor cursor = getContentResolver().query(uri, DBOpenHelper.ALL_COURSE_COLUMNS,
                    courseFilter, null, null);
            cursor.moveToFirst();
            courseId = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_ID));
            termId = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_TERM_ID));
            String title, start, end, times, status, mentorName, mentorPhone, mentorEmail, mentor;
            title = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_TITLE));
            start = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_START));
            end = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_END));
            status = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_STATUS));
            mentorName = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_MENTOR_NAME));
            mentorPhone = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_MENTOR_PHONE));
            mentorEmail = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_MENTOR_EMAIL));
            cursor.close();
            times = start + " - " + end;
            mentor = String.format("Mentor:\n%s\n%s\n%s",
                    mentorName, mentorPhone, mentorEmail);

            termTitle.setText(title);
            termTimes.setText(times);
            courseStatus.setText(status);
            switch (status) {
                case "In Progress":
                    courseStatus.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorInProgress));
                    break;
                case "Completed":
                    courseStatus.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCompleted));
                    break;
                case "Dropped":
                    courseStatus.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorDropped));
                    break;
                case "Plan To Take":
                    courseStatus.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPlanToTake));
                    break;
            }
            mentorInfo.setText(mentor);
        }
    }
}
