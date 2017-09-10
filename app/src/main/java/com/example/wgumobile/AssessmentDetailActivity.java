package com.example.wgumobile;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class AssessmentDetailActivity extends AppCompatActivity {
    private Uri uri;
    private String assessmentFilter;
    private String assessmentId, courseId;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_ASSESSMENT_EDIT = 2;
    private TextView assessmentTitle, assessmentDueDate, assessmentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_detail);

        //Get UI Components
        assessmentTitle = (TextView) findViewById(R.id.assessmentDetailTitle);
        assessmentDueDate = (TextView) findViewById(R.id.assessmentDetailDueDate);
        assessmentType = (TextView) findViewById(R.id.assessmentDetailType);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_assessment_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_photo:
                takePhoto();
                return true;
            case R.id.action_edit:
                //Start AssessmentEditActivity
                Intent intent = new Intent(AssessmentDetailActivity.this, AssessmentEditActivity.class);
                Uri assessmentUri = Uri.parse(DataProvider.ASSESSMENTS_CONTENT_URI + "/" + assessmentId);
                intent.putExtra(DataProvider.CONTENT_ASSESSMENT_ID, assessmentUri);
                Uri courseUri = Uri.parse(DataProvider.COURSES_CONTENT_URI + "/" + courseId);
                intent.putExtra(DataProvider.CONTENT_COURSE_ID, courseUri);
                startActivityForResult(intent, REQUEST_ASSESSMENT_EDIT);
                return true;
            case R.id.action_delete:
                //Remove assessment from db and display message
                getContentResolver().delete(DataProvider.ASSESSMENTS_CONTENT_URI, assessmentFilter, null);
                Toast.makeText(this, R.string.assessment_deleted, Toast.LENGTH_SHORT).show();
                finishWithResult();
                return true;
            case android.R.id.home:
                //Handle actionbar arrow same as device back button
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { //Photo successfully taken
            //Retrieve Bitmap
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            //Store photo in db and display message
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            byte[] photoBytes = outputStream.toByteArray();
            ContentValues values = new ContentValues();
            values.put(DBOpenHelper.PHOTO_COURSE_ID, courseId);
            values.put(DBOpenHelper.PHOTO_BLOB, photoBytes);
            getContentResolver().insert(DataProvider.PHOTOS_CONTENT_URI, values);
            Toast.makeText(this, R.string.photo_to_notes, Toast.LENGTH_SHORT).show();
        }
        if (requestCode == REQUEST_ASSESSMENT_EDIT && resultCode == RESULT_OK) {
            //Edit activity resulted in deletion so leave the details that no longer exist
            setResult(RESULT_OK, data);
            this.finish();
        }
        if (requestCode == REQUEST_ASSESSMENT_EDIT && resultCode == RESULT_CANCELED) {
            //Load assessment Id to ensure display of information
            if (data != null)
                uri = data.getParcelableExtra(DataProvider.CONTENT_ASSESSMENT_ID);
        }

    }

    @Override
    public void onBackPressed() {
        finishWithResult();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        loadAssessmentData();
        super.onResume();
    }

    //Pass assessmentId back to the ListActivity for accurate selection
    private void finishWithResult() {
        Intent intent = new Intent();
        Uri uri = Uri.parse(DataProvider.ASSESSMENTS_CONTENT_URI + "/" + assessmentId);
        intent.putExtra(DataProvider.CONTENT_ASSESSMENT_ID, uri);
        setResult(RESULT_OK, intent);
        finish();
    }

    //Start intent to take the photo
    private void takePhoto() {
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhoto.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePhoto, REQUEST_IMAGE_CAPTURE);
        }
    }

    //Load title, dueDate, and type based on received assessment Id
    private void loadAssessmentData() {
        Intent intent = getIntent();
        Uri intentUri = intent.getParcelableExtra(DataProvider.CONTENT_ASSESSMENT_ID);
        //Only attempt to load assessment details from db if an assessment Id was received
        if (intentUri != null) {
            uri = intentUri;
            assessmentId = uri.getLastPathSegment();
            assessmentFilter = DBOpenHelper.ASSESSMENT_ID + "=" + assessmentId;
            Cursor cursor = getContentResolver().query(uri, DBOpenHelper.ALL_ASSESSMENT_COLUMNS,
                    assessmentFilter, null, null);
            cursor.moveToFirst();
            courseId = cursor.getString(cursor.getColumnIndex(DBOpenHelper.ASSESSMENT_COURSE_ID));
            String title = cursor.getString(cursor.getColumnIndex(DBOpenHelper.ASSESSMENT_TITLE));
            String dueDate = cursor.getString(cursor.getColumnIndex(DBOpenHelper.ASSESSMENT_DUE_DATE));
            String type = cursor.getString(cursor.getColumnIndex(DBOpenHelper.ASSESSMENT_TYPE));
            cursor.close();

            assessmentTitle.setText(title);
            assessmentDueDate.setText(dueDate);
            assessmentType.setText(type);
        }
    }
}
