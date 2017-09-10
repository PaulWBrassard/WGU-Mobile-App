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

public class AssessmentsListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_ASSESSMENT_DETAIL = 1;
    private CursorAdapter cursorAdapter;
    private String courseId;
    private String courseFilter;
    private Uri courseIdUri;

    //Load assessment details into the ListView
    private Loader<Cursor> loadData() {
        String[] from = {DBOpenHelper.ASSESSMENT_TITLE, DBOpenHelper.ASSESSMENT_DUE_DATE, DBOpenHelper.ASSESSMENT_TYPE};
        int[] to = {R.id.assessmentRowTitle, R.id.assessmentRowDetailOne, R.id.assessmentRowDetailTwo};
        cursorAdapter = new SimpleCursorAdapter(this,
                R.layout.assessment_row, null, from, to, 0);
        ListView list = (ListView) findViewById(R.id.assessmentsList);
        list.setAdapter(cursorAdapter);
        //Handle clicks to follow list item over to corresponding DetailActivity
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AssessmentsListActivity.this, AssessmentDetailActivity.class);
                Uri uri = Uri.parse(DataProvider.ASSESSMENTS_CONTENT_URI + "/" + id);
                intent.putExtra(DataProvider.CONTENT_ASSESSMENT_ID, uri);
                startActivityForResult(intent, REQUEST_ASSESSMENT_DETAIL);
            }
        });

        return getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessments_list);

        //Set course ID based on which called AssessmentsListActivity
        Intent intent = getIntent();
        Uri intentUri = intent.getParcelableExtra(DataProvider.CONTENT_COURSE_ID);
        if (intentUri != null) courseIdUri = intentUri;
        courseId = courseIdUri.getLastPathSegment();
        courseFilter = DBOpenHelper.ASSESSMENT_COURSE_ID + "=" + courseId;

        //Handle request for new assessment and send course Id for accurate linkage
        FloatingActionButton newAssessmentButton = (FloatingActionButton) findViewById(R.id.fabAddAssessment);
        newAssessmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AssessmentsListActivity.this, AssessmentEditActivity.class);
                Uri uri = Uri.parse(DataProvider.COURSES_CONTENT_URI + "/" + courseId);
                intent.putExtra(DataProvider.CONTENT_COURSE_ID, uri);
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
        return new CursorLoader(this, DataProvider.ASSESSMENTS_CONTENT_URI,
                null, courseFilter, null, null);
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
        //Finish with setting the corresponding course Id as the result
        Intent intent = new Intent();
        Uri uri = Uri.parse(DataProvider.COURSES_CONTENT_URI + "/" + courseId);
        intent.putExtra(DataProvider.CONTENT_COURSE_ID, uri);
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
        //Retrieve corresponding course Id from child activity to ensure correct assessment list loading
        courseIdUri = data.getParcelableExtra(DataProvider.CONTENT_COURSE_ID);
    }
}
