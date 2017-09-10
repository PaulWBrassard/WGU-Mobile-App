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

public class AssessmentEditActivity extends AppCompatActivity {

    private Pattern datePattern = Pattern.compile(DateWatcher.DATE_FORMAT);
    private Matcher matcher;
    private String courseId, assessmentId, assessmentFilter;
    private EditText editTitle, editDueDate;
    private Spinner typeSpinner;
    private CheckBox dueDateAlert;
    private Button submit;
    private boolean editing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_edit);

        //Get UI components
        editTitle = (EditText) findViewById(R.id.editAssessmentTitle);
        editDueDate = (EditText) findViewById(R.id.editAssessmentDueDate);
        editDueDate.addTextChangedListener(new DateWatcher(editDueDate));
        typeSpinner = (Spinner) findViewById(R.id.editAssessmentType);
        submit = (Button) findViewById(R.id.buttonSubmitAssessment);
        dueDateAlert = (CheckBox) findViewById(R.id.assessmentAlertDueDate);

        //Set course ID matching caller
        Intent intent = getIntent();
        Uri courseUri = intent.getParcelableExtra(DataProvider.CONTENT_COURSE_ID);
        courseId = courseUri.getLastPathSegment();

        //If we are editing existing, load assessment details into editor.
        Uri assessmentUri = intent.getParcelableExtra(DataProvider.CONTENT_ASSESSMENT_ID);
        if (assessmentUri != null) {
            editing = true;
            assessmentId = assessmentUri.getLastPathSegment();
            assessmentFilter = DBOpenHelper.TERM_ID + "=" + assessmentId;
            Cursor cursor = getContentResolver().query(assessmentUri, DBOpenHelper.ALL_ASSESSMENT_COLUMNS,
                    assessmentFilter, null, null);
            cursor.moveToFirst();
            String title, dueDate, type;
            title = cursor.getString(cursor.getColumnIndex(DBOpenHelper.ASSESSMENT_TITLE));
            dueDate = cursor.getString(cursor.getColumnIndex(DBOpenHelper.ASSESSMENT_DUE_DATE));
            type = cursor.getString(cursor.getColumnIndex(DBOpenHelper.ASSESSMENT_TYPE));
            cursor.close();
            editTitle.setText(title);
            editDueDate.setText(dueDate);
            switch (type) {
                case "Objective":
                    typeSpinner.setSelection(1);
                    break;
                case "Performance":
                    typeSpinner.setSelection(2);
                    break;
            }
        } else editing = false;

        //Create action handling
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check that dates are in the appropriate format.
                String title, dueDate, type;
                boolean validType, dueDateMatches, validTitle;
                dueDate = editDueDate.getText().toString();
                type = String.valueOf(typeSpinner.getSelectedItem());
                matcher = datePattern.matcher(dueDate);
                dueDateMatches = matcher.matches();
                //Check that there is a title entered.
                title = editTitle.getText().toString();
                validType = (!type.equals("Select Type"));
                validTitle = (!title.isEmpty());
                //Save term if the fields are correctly filled.
                if (dueDateMatches && validType && validTitle) {
                    if (editing) {
                        //Editing needs just an update to the info
                        updateAssessment(title, dueDate, type);
                    } else {
                        //New assessment needs insertion
                        insertAssessment(title, dueDate, type);
                    }
                    manageAlert();
                    //Finish with passing back assessmentId for Detail or List Activity correct information loading
                    Intent intent = new Intent();
                    Uri uri = Uri.parse(DataProvider.ASSESSMENTS_CONTENT_URI + "/" + assessmentId);
                    intent.putExtra(DataProvider.CONTENT_ASSESSMENT_ID, uri);
                    setResult(RESULT_CANCELED, intent);
                    finish();
                } else {
                    //Invalid form entry
                    Snackbar snackbar;
                    if (validTitle) {
                        //Date and/or type is the issue
                        snackbar = Snackbar.make(view, R.string.wrong_date_or_type, Snackbar.LENGTH_SHORT);
                    } else {
                        //Invalid title
                        snackbar = Snackbar.make(view, R.string.no_assessment_title, Snackbar.LENGTH_SHORT);
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
                    //If editing, remove assessment from db
                    String where = DBOpenHelper.ASSESSMENT_ID + "= ?";
                    String[] selectionArgs = {assessmentId};
                    getContentResolver().delete(DataProvider.ASSESSMENTS_CONTENT_URI,
                            where, selectionArgs);
                    Toast.makeText(this, R.string.assessment_deleted, Toast.LENGTH_SHORT).show();
                }
                //Finish with passing back corresponding courseId to ensure parent activity information loading
                Intent intent = new Intent();
                Uri uri = Uri.parse(DataProvider.COURSES_CONTENT_URI + "/" + courseId);
                intent.putExtra(DataProvider.CONTENT_COURSE_ID, uri);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            case android.R.id.home:
                //Handle action bar back arrow the same as device back button
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Check if an alert is requested and schedule if necessary
    private void manageAlert() {
        //Use custom receiver class and set id, title, and text for notification
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        int notificationId = 123;
        notificationIntent.putExtra(NotificationReceiver.ID, notificationId);
        notificationIntent.putExtra(NotificationReceiver.TITLE, getString(R.string.assessment_due));
        notificationIntent.putExtra(NotificationReceiver.TEXT, editTitle.getText().toString() + getString(R.string.due_today));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId,
                notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        long dueDateTime = 0;
        //Extract time from dueDate entered
        try {
            dueDateTime = new SimpleDateFormat("MM/dd/yy").parse(editDueDate.getText().toString()).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (dueDateAlert.isChecked()) {
            //Register the notification to be sent day assessment is due
            alarmManager.set(AlarmManager.RTC, dueDateTime,
                    pendingIntent);
        } else {
            //No alert requested, ensure old notification is gone
            alarmManager.cancel(pendingIntent);
        }
    }

    //Update an existing assessment in the db
    private void updateAssessment(String title, String dueDate, String type) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.ASSESSMENT_COURSE_ID, courseId);
        values.put(DBOpenHelper.ASSESSMENT_TITLE, title);
        values.put(DBOpenHelper.ASSESSMENT_DUE_DATE, dueDate);
        values.put(DBOpenHelper.ASSESSMENT_TYPE, type);
        String where = DBOpenHelper.ASSESSMENT_ID + "= ?";
        String[] selectionArgs = {assessmentId};
        getContentResolver().update(DataProvider.ASSESSMENTS_CONTENT_URI, values,
                where, selectionArgs);
    }

    //Insert a new assessment into the db
    private void insertAssessment(String title, String dueDate, String type) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.ASSESSMENT_COURSE_ID, courseId);
        values.put(DBOpenHelper.ASSESSMENT_TITLE, title);
        values.put(DBOpenHelper.ASSESSMENT_DUE_DATE, dueDate);
        values.put(DBOpenHelper.ASSESSMENT_TYPE, type);
        getContentResolver().insert(DataProvider.ASSESSMENTS_CONTENT_URI, values);
    }
}
