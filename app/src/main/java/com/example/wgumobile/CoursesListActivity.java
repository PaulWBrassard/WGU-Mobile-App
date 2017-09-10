package com.example.wgumobile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class CoursesListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_COURSE_DETAIL = 1;
    private CursorAdapter cursorAdapter;
    private String termId;
    private String termFilter;
    private Uri termIdUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_list);

        //Set term ID based on which called CoursesListActivity
        Intent intent = getIntent();
        Uri intentUri = intent.getParcelableExtra(DataProvider.CONTENT_TERM_ID);
        if (intentUri != null) termIdUri = intentUri;
        termId = termIdUri.getLastPathSegment();
        termFilter = DBOpenHelper.COURSE_TERM_ID + "=" + termId;

        //Handle request for new course passing corresponding term id
        FloatingActionButton newCourseButton = (FloatingActionButton) findViewById(R.id.fabAddCourse);
        newCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CoursesListActivity.this, CourseEditActivity.class);
                Uri uri = Uri.parse(DataProvider.TERMS_CONTENT_URI + "/" + termId);
                intent.putExtra(DataProvider.CONTENT_TERM_ID, uri);
                startActivity(intent);
            }
        });


        loadData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, DataProvider.COURSES_CONTENT_URI,
                null, termFilter, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    @Override
    public void onBackPressed() {
        //Finish with setting term id as the result to ensure accurate parent activity information loading
        Intent intent = new Intent();
        Uri uri = Uri.parse(DataProvider.TERMS_CONTENT_URI + "/" + termId);
        intent.putExtra(DataProvider.CONTENT_TERM_ID, uri);
        setResult(RESULT_CANCELED, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        //Load corresponding term id for accurate information loading
        termIdUri = data.getParcelableExtra(DataProvider.CONTENT_TERM_ID);
    }

    //Load ListView with course information
    private Loader<Cursor> loadData() {
        String[] from = {DBOpenHelper.COURSE_TITLE, DBOpenHelper.COURSE_START, DBOpenHelper.COURSE_END};
        int[] to = {R.id.rowTitle, R.id.rowDetailOne, R.id.rowDetailTwo};
        cursorAdapter = new SimpleCursorAdapter(this,
                R.layout.row, null, from, to, 0);
        ListView list = (ListView) findViewById(R.id.coursesList);
        list.setAdapter(cursorAdapter);
        //Handle requests to view each courses details
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CoursesListActivity.this, CourseDetailActivity.class);
                Uri uri = Uri.parse(DataProvider.COURSES_CONTENT_URI + "/" + id);
                intent.putExtra(DataProvider.CONTENT_COURSE_ID, uri);
                startActivityForResult(intent, REQUEST_COURSE_DETAIL);
            }
        });

        return getSupportLoaderManager().initLoader(0, null, this);
    }
}
