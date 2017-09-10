package com.example.wgumobile;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {

    //Constants for database
    private static final String DATABASE_NAME = "wgu.db";
    private static final int DATABASE_VERSION = 8;

    //Constants for tables and columns
    public static final String TABLE_TERMS = "terms";
    public static final String TERM_ID = "_id";
    public static final String TERM_TITLE = "title";
    public static final String TERM_START = "start";
    public static final String TERM_END = "end";

    public static final String TABLE_COURSES = "courses";
    public static final String COURSE_ID = "_id";
    public static final String COURSE_TERM_ID = "termId";
    public static final String COURSE_TITLE = "title";
    public static final String COURSE_START = "start";
    public static final String COURSE_END = "end";
    public static final String COURSE_STATUS = "status";
    public static final String COURSE_MENTOR_NAME = "mentorName";
    public static final String COURSE_MENTOR_PHONE = "mentorPhone";
    public static final String COURSE_MENTOR_EMAIL = "mentorEmail";
    public static final String COURSE_NOTES = "notes";

    public static final String TABLE_ASSESSMENTS = "assessments";
    public static final String ASSESSMENT_ID = "_id";
    public static final String ASSESSMENT_COURSE_ID = "courseId";
    public static final String ASSESSMENT_TITLE = "title";
    public static final String ASSESSMENT_TYPE = "type";
    public static final String ASSESSMENT_DUE_DATE = "dueDate";

    public static final String TABLE_PHOTOS = "photos";
    public static final String PHOTO_ID = "_id";
    public static final String PHOTO_COURSE_ID = "courseId";
    public static final String PHOTO_BLOB = "blob";

    public static final String[] ALL_TERM_COLUMNS =
            {TERM_ID, TERM_TITLE, TERM_START, TERM_END};
    public static final String[] ALL_COURSE_COLUMNS =
            {COURSE_ID, COURSE_TERM_ID, COURSE_TITLE, COURSE_START, COURSE_END,
                    COURSE_STATUS, COURSE_MENTOR_NAME, COURSE_MENTOR_PHONE,
                    COURSE_MENTOR_EMAIL, COURSE_NOTES};
    public static final String[] ALL_ASSESSMENT_COLUMNS =
            {ASSESSMENT_ID, ASSESSMENT_COURSE_ID, ASSESSMENT_TITLE,
                    ASSESSMENT_TYPE, ASSESSMENT_DUE_DATE};
    public static final String[] ALL_PHOTO_COLUMNS =
            {PHOTO_ID, PHOTO_COURSE_ID, PHOTO_BLOB};

    //SQL to create db
    private static final String TERMS_TABLE_CREATE =
            "CREATE TABLE " + TABLE_TERMS + " (" +
                    TERM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TERM_TITLE + " TEXT, " +
                    TERM_START + " TEXT," +
                    TERM_END + " TEXT" +
                    ")";
    private static final String COURSES_TABLE_CREATE =
            "CREATE TABLE " + TABLE_COURSES + " (" +
                    COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COURSE_TERM_ID + " TEXT, " +
                    COURSE_TITLE + " TEXT, " +
                    COURSE_START + " TEXT, " +
                    COURSE_END + " TEXT, " +
                    COURSE_STATUS + " TEXT, " +
                    COURSE_MENTOR_NAME + " TEXT, " +
                    COURSE_MENTOR_PHONE + " TEXT, " +
                    COURSE_MENTOR_EMAIL + " TEXT," +
                    COURSE_NOTES + " TEXT" +
                    ")";
    private static final String ASSESSMENT_TABLE_CREATE =
            "CREATE TABLE " + TABLE_ASSESSMENTS + " (" +
                    ASSESSMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ASSESSMENT_COURSE_ID + " TEXT, " +
                    ASSESSMENT_TITLE + " TEXT," +
                    ASSESSMENT_TYPE + " TEXT," +
                    ASSESSMENT_DUE_DATE + " TEXT" +
                    ")";
    private static final String PHOTO_TABLE_CREATE =
            "CREATE TABLE " + TABLE_PHOTOS + " (" +
                    PHOTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PHOTO_COURSE_ID + " TEXT, " +
                    PHOTO_BLOB + " BLOB" +
                    ")";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create the tables for the db
        db.execSQL(TERMS_TABLE_CREATE);
        db.execSQL(COURSES_TABLE_CREATE);
        db.execSQL(ASSESSMENT_TABLE_CREATE);
        db.execSQL(PHOTO_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop the old tables and recreate new ones
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TERMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSESSMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTOS);
        onCreate(db);
    }
}
