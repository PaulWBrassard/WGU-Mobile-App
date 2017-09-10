package com.example.wgumobile;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class NotesActivity extends AppCompatActivity {
    private String courseId, courseFilter, notes;
    private EditText notesEdit;
    private LinearLayout linearLayoutNotes;
    private boolean fullScreenImage;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        //Prepare UI components
        linearLayoutNotes = (LinearLayout) findViewById(R.id.linearLayoutNotes);
        linearLayoutNotes.setHorizontalGravity(Gravity.LEFT);
        notesEdit = new EditText(this);

        //Set course ID based on which called NotesActivity
        Intent intent = getIntent();
        Uri intentUri = intent.getParcelableExtra(DataProvider.CONTENT_COURSE_ID);
        //Only load notes information if course id has been received
        if (intentUri != null) {
            courseId = intentUri.getLastPathSegment();
            courseFilter = DBOpenHelper.ASSESSMENT_COURSE_ID + "=" + courseId;
            Cursor cursor = getContentResolver().query(intentUri, DBOpenHelper.ALL_COURSE_COLUMNS,
                    courseFilter, null, null);
            cursor.moveToFirst();
            notes = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_NOTES));
            cursor.close();
            notesEdit.setText(notes);
        }
    }

    @Override
    public void onResume() {
        //Clear linearLayout before rebuilding
        linearLayoutNotes.removeAllViews();
        linearLayoutNotes.addView(notesEdit);

        //Create ImageViews for each Photo that exists
        String photoFilter = DBOpenHelper.PHOTO_COURSE_ID + "=" + courseId;
        Cursor photoCursor = getContentResolver().query(DataProvider.PHOTOS_CONTENT_URI,
                DBOpenHelper.ALL_PHOTO_COLUMNS, photoFilter, null, null);
        while (photoCursor.moveToNext()) {
            final ImageView image = new ImageView(NotesActivity.this);
            image.setPadding(10, 10, 10, 30);
            byte[] imgByte = photoCursor.getBlob(photoCursor.getColumnIndex(DBOpenHelper.PHOTO_BLOB));
            Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            image.setImageBitmap(bitmap);
            image.setAdjustViewBounds(true);
            image.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fullScreenImage) {
                        fullScreenImage = false;
                        image.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                    } else {
                        fullScreenImage = true;
                        image.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT));
                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                    }
                }
            });
            linearLayoutNotes.addView(image);
        }
        photoCursor.close();

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Handle action bar back arrow same as device back button
                onBackPressed();
                return true;
            case R.id.action_share:
                shareBySMS();
                return true;
            case R.id.action_photo:
                takePhoto();
                return true;
            case R.id.action_delete:
                //Delete notes and associated photos and display message
                notesEdit.setText("");
                deletePhotos();
                Toast.makeText(this, R.string.notes_deleted, Toast.LENGTH_SHORT).show();
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { //Successful return from taking a photo
            //Retrieve Bitmap
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            //Store photo in db
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            byte[] photoBytes = outputStream.toByteArray();
            ContentValues values = new ContentValues();
            values.put(DBOpenHelper.PHOTO_COURSE_ID, courseId);
            values.put(DBOpenHelper.PHOTO_BLOB, photoBytes);
            getContentResolver().insert(DataProvider.PHOTOS_CONTENT_URI, values);
            Toast.makeText(this, R.string.photo_added, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        //Save the notes and pass corresponding course Id back to parent activity to ensure accurate information loading
        saveNotes();
        Intent intent = new Intent();
        Uri uri = Uri.parse(DataProvider.COURSES_CONTENT_URI + "/" + courseId);
        intent.putExtra(DataProvider.CONTENT_COURSE_ID, uri);
        setResult(RESULT_CANCELED, intent);
        finish();
        super.onBackPressed();
    }

    //Share entered notes by SMS(text message)
    private void shareBySMS() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:"));
        intent.putExtra("sms_body", notesEdit.getText().toString());
        startActivity(intent);
    }

    //Delete all photos with the corresponding course id from the db
    private void deletePhotos() {
        String where = DBOpenHelper.PHOTO_COURSE_ID + "= ?";
        String[] selectionArgs = {courseId};
        getContentResolver().delete(DataProvider.PHOTOS_CONTENT_URI,
                where, selectionArgs);
    }

    //Use image capture action to take a picture
    private void takePhoto() {
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhoto.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePhoto, REQUEST_IMAGE_CAPTURE);
        }
    }

    //Save the existing notes to the corresponding course in the db
    private void saveNotes() {
        String notes = notesEdit.getText().toString();
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.COURSE_NOTES, notes);
        String where = DBOpenHelper.COURSE_ID + "= ?";
        String[] selectionArg = {courseId};
        getContentResolver().update(DataProvider.COURSES_CONTENT_URI, values,
                where, selectionArg);
    }
}
