package com.example.wgumobile;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class DataProvider extends ContentProvider {

    private static final String AUTHORITY = "com.example.wgumobile.termsprovider";
    private static final String TERMS_PATH = "terms";
    private static final String COURSES_PATH = "courses";
    private static final String ASSESSMENTS_PATH = "assessments";
    private static final String PHOTOS_PATH = "photos";
    public static final Uri TERMS_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + TERMS_PATH);
    public static final Uri COURSES_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + COURSES_PATH);
    public static final Uri ASSESSMENTS_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + ASSESSMENTS_PATH);
    public static final Uri PHOTOS_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + PHOTOS_PATH);

    //Identifiers for requested operation
    private static final int TERMS = 1;
    private static final int TERMS_ID = 2;
    private static final int COURSES = 3;
    private static final int COURSES_ID = 4;
    private static final int ASSESSMENTS = 5;
    private static final int ASSESSMENTS_ID = 6;
    private static final int PHOTOS = 7;
    private static final int PHOTOS_ID = 8;

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONTENT_TERM_ID = "TermID";
    public static final String CONTENT_TERM_FILTER = "TermFilter";
    public static final String CONTENT_COURSE_ID = "CourseID";
    public static final String CONTENT_COURSE_FILTER = "CourseFilter";
    public static final String CONTENT_ASSESSMENT_ID = "AssessmentID";

    static {
        uriMatcher.addURI(AUTHORITY, TERMS_PATH, TERMS);
        uriMatcher.addURI(AUTHORITY, TERMS_PATH + "/#", TERMS_ID);
        uriMatcher.addURI(AUTHORITY, COURSES_PATH, COURSES);
        uriMatcher.addURI(AUTHORITY, COURSES_PATH + "/#", COURSES_ID);
        uriMatcher.addURI(AUTHORITY, ASSESSMENTS_PATH, ASSESSMENTS);
        uriMatcher.addURI(AUTHORITY, ASSESSMENTS_PATH + "/#", ASSESSMENTS_ID);
        uriMatcher.addURI(AUTHORITY, PHOTOS_PATH, PHOTOS);
        uriMatcher.addURI(AUTHORITY, PHOTOS_PATH + "/#", PHOTOS_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        DBOpenHelper helper = new DBOpenHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int uriType = uriMatcher.match(uri);
        String table, orderBy;
        String[] columns;
        //Choose table, selection, columns, orderBy based on type of request
        switch (uriType) {
            case TERMS_ID:
                selection = DBOpenHelper.TERM_ID + "=" + uri.getLastPathSegment();
            case TERMS:
                table = DBOpenHelper.TABLE_TERMS;
                columns = DBOpenHelper.ALL_TERM_COLUMNS;
                orderBy = DBOpenHelper.TERM_START;
                break;
            case COURSES_ID:
                selection = DBOpenHelper.COURSE_ID + "=" + uri.getLastPathSegment();
            case COURSES:
                table = DBOpenHelper.TABLE_COURSES;
                columns = DBOpenHelper.ALL_COURSE_COLUMNS;
                orderBy = DBOpenHelper.COURSE_START;
                break;
            case ASSESSMENTS_ID:
                selection = DBOpenHelper.ASSESSMENT_ID + "=" + uri.getLastPathSegment();
            case ASSESSMENTS:
                table = DBOpenHelper.TABLE_ASSESSMENTS;
                columns = DBOpenHelper.ALL_ASSESSMENT_COLUMNS;
                orderBy = DBOpenHelper.ASSESSMENT_DUE_DATE;
                break;
            case PHOTOS_ID:
                selection = DBOpenHelper.PHOTO_ID + "=" + uri.getLastPathSegment();
            case PHOTOS:
                table = DBOpenHelper.TABLE_PHOTOS;
                columns = DBOpenHelper.ALL_PHOTO_COLUMNS;
                orderBy = DBOpenHelper.PHOTO_ID;
                break;
            default:
                throw new IllegalArgumentException("No URI Match: " + uri);
        }

        return database.query(table, columns,
                selection, null, null, null, orderBy);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int uriType = uriMatcher.match(uri);
        long id;
        String path;
        //Choose correct path and id based on requested type
        switch (uriType) {
            case TERMS_ID:
            case TERMS:
                id = database.insert(DBOpenHelper.TABLE_TERMS, null, values);
                path = TERMS_PATH;
                break;
            case COURSES_ID:
            case COURSES:
                id = database.insert(DBOpenHelper.TABLE_COURSES, null, values);
                path = COURSES_PATH;
                break;
            case ASSESSMENTS_ID:
            case ASSESSMENTS:
                id = database.insert(DBOpenHelper.TABLE_ASSESSMENTS, null, values);
                path = ASSESSMENTS_PATH;
                break;
            case PHOTOS_ID:
            case PHOTOS:
                id = database.insert(DBOpenHelper.TABLE_PHOTOS, null, values);
                path = PHOTOS_PATH;
                break;
            default:
                throw new IllegalArgumentException("No URI Match: " + uri);
        }
        return Uri.parse(path + "/" + id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        String table;
        //Choose correct table based on requested type
        switch (uriType) {
            case TERMS_ID:
            case TERMS:
                table = DBOpenHelper.TABLE_TERMS;
                break;
            case COURSES_ID:
            case COURSES:
                table = DBOpenHelper.TABLE_COURSES;
                break;
            case ASSESSMENTS_ID:
            case ASSESSMENTS:
                table = DBOpenHelper.TABLE_ASSESSMENTS;
                break;
            case PHOTOS_ID:
            case PHOTOS:
                table = DBOpenHelper.TABLE_PHOTOS;
                break;
            default:
                throw new IllegalArgumentException("No URI Match: " + uri);
        }
        return database.delete(table, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        String table;
        //Choose correct table based on requested type
        switch (uriType) {
            case TERMS_ID:
            case TERMS:
                table = DBOpenHelper.TABLE_TERMS;
                break;
            case COURSES_ID:
            case COURSES:
                table = DBOpenHelper.TABLE_COURSES;
                break;
            case ASSESSMENTS_ID:
            case ASSESSMENTS:
                table = DBOpenHelper.TABLE_ASSESSMENTS;
                break;
            case PHOTOS_ID:
            case PHOTOS:
                table = DBOpenHelper.TABLE_PHOTOS;
                break;
            default:
                throw new IllegalArgumentException("No URI Match: " + uri);
        }
        return database.update(table, values, selection, selectionArgs);
    }
}
