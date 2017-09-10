package com.example.wgumobile;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CourseEditActivity extends AppCompatActivity {

    private Pattern datePattern = Pattern.compile(DateWatcher.DATE_FORMAT);
    private Matcher matcher;
    private String termId, courseId, courseFilter;
    private Boolean editing;
    private CheckBox startDateAlert, endDateAlert;
    private EditText editTitle, editStart, editEnd;
    private final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("MM/dd/yy");
    private long startTime = 0, endTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_edit);

        //Get UI components
        editTitle = (EditText) findViewById(R.id.editCourseTitle);
        editStart = (EditText) findViewById(R.id.editCourseStart);
        editStart.addTextChangedListener(new DateWatcher(editStart));
        editEnd = (EditText) findViewById(R.id.editCourseEnd);
        editEnd.addTextChangedListener(new DateWatcher(editEnd));
        final EditText editMentorName = (EditText) findViewById(R.id.editCourseMentorName);
        final EditText editMentorPhone = (EditText) findViewById(R.id.editCourseMentorPhone);
        final EditText editMentorEmail = (EditText) findViewById(R.id.editCourseMentorEmail);
        final Spinner statusSpinner = (Spinner) findViewById(R.id.editCourseStatus);
        startDateAlert = (CheckBox) findViewById(R.id.courseAlertStart);
        endDateAlert = (CheckBox) findViewById(R.id.courseAlertEnd);
        Button submit = (Button) findViewById(R.id.buttonSubmitCourse);

        //Set term ID matching caller
        Intent intent = getIntent();
        Uri termUri = intent.getParcelableExtra(DataProvider.CONTENT_TERM_ID);
        termId = termUri.getLastPathSegment();

        //If we are editing existing, load course details into editor.
        Uri courseUri = intent.getParcelableExtra(DataProvider.CONTENT_COURSE_ID);
        if (courseUri != null) {
            editing = true;
            courseId = courseUri.getLastPathSegment();
            courseFilter = DBOpenHelper.COURSE_ID + "=" + courseId;
            Cursor cursor = getContentResolver().query(courseUri, DBOpenHelper.ALL_TERM_COLUMNS,
                    courseFilter, null, null);
            cursor.moveToFirst();
            String title, start, end, status, mName, mPhone, mEmail;
            title = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_TITLE));
            start = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_START));
            end = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_END));
            status = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_STATUS));
            mName = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_MENTOR_NAME));
            mPhone = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_MENTOR_PHONE));
            mEmail = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_MENTOR_EMAIL));
            cursor.close();
            editTitle.setText(title);
            editStart.setText(start);
            editEnd.setText(end);
            editMentorName.setText(mName);
            editMentorPhone.setText(mPhone);
            editMentorEmail.setText(mEmail);
            switch (status) {
                case "In Progress":
                    statusSpinner.setSelection(1);
                    break;
                case "Completed":
                    statusSpinner.setSelection(2);
                    break;
                case "Dropped":
                    statusSpinner.setSelection(3);
                    break;
                case "Plan To Take":
                    statusSpinner.setSelection(4);
                    break;
            }
        } else editing = false;

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Save input
                String title, start, end, mentorName, mentorPhone, mentorEmail, status;
                boolean startMatches, endMatches, validTitle, validTimeSegment;

                title = editTitle.getText().toString();
                start = editStart.getText().toString();
                end = editEnd.getText().toString();
                mentorName = editMentorName.getText().toString();
                mentorPhone = editMentorPhone.getText().toString();
                mentorEmail = editMentorEmail.getText().toString();
                status = String.valueOf(statusSpinner.getSelectedItem());
                //Check that dates are in the appropriate format.
                matcher = datePattern.matcher(start);
                startMatches = matcher.matches();
                matcher = datePattern.matcher(end);
                endMatches = matcher.matches();
                try {
                    startTime = DATEFORMAT.parse(editStart.getText().toString()).getTime();
                    endTime = DATEFORMAT.parse(editEnd.getText().toString()).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                validTimeSegment = startTime < endTime; //Course must start before it ends
                //Check that there is a title entered.
                validTitle = (!title.isEmpty());
                //Save course if the fields are correctly filled.
                if (startMatches && endMatches && validTitle && validTimeSegment) {
                    if (editing) {
                        //Editing requires update of existing course information
                        updateCourse(title, start, end, status,
                                mentorName, mentorPhone, mentorEmail);
                    } else {
                        //New course requires insertion into db
                        insertCourse(title, start, end, status,
                                mentorName, mentorPhone, mentorEmail);
                    }
                    manageAlerts();
                    //Finish with setting result as the corresponding course Id to ensure accurate parent activity information loading
                    Intent intent = new Intent();
                    Uri uri = Uri.parse(DataProvider.COURSES_CONTENT_URI + "/" + courseId);
                    intent.putExtra(DataProvider.CONTENT_COURSE_ID, uri);
                    setResult(RESULT_CANCELED, intent);
                    finish();
                } else {
                    //Invalid form entry
                    Snackbar snackbar;
                    if (!validTimeSegment) { //Course doesn't start before end date
                        snackbar = Snackbar.make(view, getString(R.string.start_before_end), Snackbar.LENGTH_SHORT);
                    } else {
                        if (validTitle) {
                            //Dates are not in correct format
                            snackbar = Snackbar.make(view, R.string.wrong_date_format, Snackbar.LENGTH_SHORT);
                        } else {
                            //Invalid title
                            snackbar = Snackbar.make(view, R.string.no_course_title, Snackbar.LENGTH_SHORT);
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
                    //Remove the course from the db
                    String where = DBOpenHelper.COURSE_ID + "= ?";
                    String[] selectionArgs = {courseId};
                    getContentResolver().delete(DataProvider.COURSES_CONTENT_URI,
                            where, selectionArgs);
                    Toast.makeText(this, R.string.course_deleted, Toast.LENGTH_SHORT).show();
                }
                //Finish with term id and course id as result to ensure accurate parent activity information loading
                Intent intent = new Intent();
                Uri termUri = Uri.parse(DataProvider.TERMS_CONTENT_URI + "/" + termId);
                intent.putExtra(DataProvider.CONTENT_TERM_ID, termUri);
                Uri courseUri = Uri.parse(DataProvider.COURSES_CONTENT_URI + "/" + courseId);
                intent.putExtra(DataProvider.CONTENT_COURSE_ID, courseUri);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            case android.R.id.home:
                //Handle action bar back arrow same as device back button
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Check if alerts are requested on start and/or end dates and schedule accordingly
    private void manageAlerts() {
        //Prepare pendingIntent for start date
        int startId = 456;
        Intent startNIntent = new Intent(this, NotificationReceiver.class);
        startNIntent.putExtra(NotificationReceiver.ID, startId);
        startNIntent.putExtra(NotificationReceiver.TITLE, getString(R.string.course_starting));
        startNIntent.putExtra(NotificationReceiver.TEXT, editTitle.getText().toString() + getString(R.string.starting_today));
        PendingIntent startPendingIntent = PendingIntent.getBroadcast(this, startId,
                startNIntent, PendingIntent.FLAG_ONE_SHOT);

        //Prepare pendingIntent for end date
        int endId = 789;
        Intent endEIntent = new Intent(this, NotificationReceiver.class);
        endEIntent.putExtra(NotificationReceiver.ID, endId);
        endEIntent.putExtra(NotificationReceiver.TITLE, getString(R.string.course_ending));
        endEIntent.putExtra(NotificationReceiver.TEXT, editTitle.getText().toString() + getString(R.string.ending_today));
        PendingIntent endPendingIntent = PendingIntent.getBroadcast(this, endId,
                endEIntent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (startDateAlert.isChecked()) {
            //Start date alert requested, register this with the alarm manager
            alarmManager.set(AlarmManager.RTC, startTime,
                    startPendingIntent);
        } else {
            //Start date alert not needed, ensure old start date alert is cleared
            alarmManager.cancel(startPendingIntent);
        }
        if (endDateAlert.isChecked()) {
            //End date alert requested, register this with the alarm manager
            alarmManager.set(AlarmManager.RTC, endTime,
                    endPendingIntent);
        } else {
            //End date alert not needed, ensure old end date alert is cleared
            alarmManager.cancel(endPendingIntent);
        }
    }

    //Update an existing course in the db
    private void updateCourse(String title, String start, String end, String status, String mentorName, String mentorPhone, String mentorEmail) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.COURSE_TITLE, title);
        values.put(DBOpenHelper.COURSE_START, start);
        values.put(DBOpenHelper.COURSE_END, end);
        values.put(DBOpenHelper.COURSE_STATUS, status);
        values.put(DBOpenHelper.COURSE_MENTOR_NAME, mentorName);
        values.put(DBOpenHelper.COURSE_MENTOR_PHONE, mentorPhone);
        values.put(DBOpenHelper.COURSE_MENTOR_EMAIL, mentorEmail);
        String where = DBOpenHelper.COURSE_ID + "= ?";
        String[] selectionArg = {courseId};
        getContentResolver().update(DataProvider.COURSES_CONTENT_URI, values,
                where, selectionArg);
    }

    //Insert a new course into the db
    private void insertCourse(String courseTitle, String courseStart, String courseEnd,
                              String status, String mentorName, String mentorPhone,
                              String mentorEmail) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.COURSE_TITLE, courseTitle);
        values.put(DBOpenHelper.COURSE_START, courseStart);
        values.put(DBOpenHelper.COURSE_END, courseEnd);
        values.put(DBOpenHelper.COURSE_TERM_ID, termId);
        values.put(DBOpenHelper.COURSE_STATUS, status);
        values.put(DBOpenHelper.COURSE_MENTOR_NAME, mentorName);
        values.put(DBOpenHelper.COURSE_MENTOR_PHONE, mentorPhone);
        values.put(DBOpenHelper.COURSE_MENTOR_EMAIL, mentorEmail);
        getContentResolver().insert(DataProvider.COURSES_CONTENT_URI, values);
    }
}
