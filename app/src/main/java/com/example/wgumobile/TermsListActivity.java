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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class TermsListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_TERM_EDIT = 1;
    private CursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_list);

        //Handle request for new term
        FloatingActionButton newTermButton = (FloatingActionButton) findViewById(R.id.fabAddTerm);
        newTermButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TermsListActivity.this, TermEditActivity.class);
                startActivityForResult(intent, REQUEST_TERM_EDIT);
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
        return new CursorLoader(this, DataProvider.TERMS_CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    //Load ListView with date from each term in db
    private Loader<Cursor> loadData() {
        String[] from = {DBOpenHelper.TERM_TITLE, DBOpenHelper.TERM_START, DBOpenHelper.TERM_END};
        int[] to = {R.id.rowTitle, R.id.rowDetailOne, R.id.rowDetailTwo};
        cursorAdapter = new SimpleCursorAdapter(this,
                R.layout.row, null, from, to, 0);
        ListView list = (ListView) findViewById(R.id.termsList);
        list.setAdapter(cursorAdapter);
        //Handle request to view any term's details
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(TermsListActivity.this, TermDetailActivity.class);
                Uri uri = Uri.parse(DataProvider.TERMS_CONTENT_URI + "/" + id);
                intent.putExtra(DataProvider.CONTENT_TERM_ID, uri);
                startActivity(intent);
            }
        });

        return getSupportLoaderManager().initLoader(0, null, this);
    }
}
