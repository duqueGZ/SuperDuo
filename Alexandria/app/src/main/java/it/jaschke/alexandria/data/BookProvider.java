package it.jaschke.alexandria.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by saj on 24/12/14.
 * Modified by david.duque on 10/12/15.
 */
public class BookProvider extends ContentProvider {

    private static final int BOOK_ID = 100;
    private static final int BOOK = 101;
    private static final int AUTHOR_ID = 200;
    private static final int AUTHOR = 201;
    private static final int CATEGORY_ID = 300;
    private static final int CATEGORY = 301;
    private static final int BOOK_FULL = 500;
    private static final int BOOK_FULL_ID = 501;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sBookFullQueryBuilder;
    private static final SQLiteQueryBuilder sBookQueryBuilder;
    private static final SQLiteQueryBuilder sAuthorQueryBuilder;
    private static final SQLiteQueryBuilder sCategoryQueryBuilder;

    private DbHelper mOpenHelper;

    static{
        sBookFullQueryBuilder = new SQLiteQueryBuilder();
        sBookFullQueryBuilder.setTables(AlexandriaContract.BookEntry.TABLE_NAME
                + " LEFT OUTER JOIN " + AlexandriaContract.AuthorEntry.TABLE_NAME
                + " USING (" + AlexandriaContract.BookEntry._ID + ")"
                + " LEFT OUTER JOIN " + AlexandriaContract.CategoryEntry.TABLE_NAME
                + " USING (" + AlexandriaContract.BookEntry._ID + ")");
        sBookQueryBuilder = new SQLiteQueryBuilder();
        sBookQueryBuilder.setTables(AlexandriaContract.BookEntry.TABLE_NAME);
        sAuthorQueryBuilder = new SQLiteQueryBuilder();
        sAuthorQueryBuilder.setTables(AlexandriaContract.AuthorEntry.TABLE_NAME);
        sCategoryQueryBuilder = new SQLiteQueryBuilder();
        sCategoryQueryBuilder.setTables(AlexandriaContract.CategoryEntry.TABLE_NAME);
    }

    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AlexandriaContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, AlexandriaContract.PATH_BOOKS + "/#", BOOK_ID);
        matcher.addURI(authority, AlexandriaContract.PATH_AUTHORS + "/#", AUTHOR_ID);
        matcher.addURI(authority, AlexandriaContract.PATH_CATEGORIES + "/#", CATEGORY_ID);

        matcher.addURI(authority, AlexandriaContract.PATH_BOOKS, BOOK);
        matcher.addURI(authority, AlexandriaContract.PATH_AUTHORS, AUTHOR);
        matcher.addURI(authority, AlexandriaContract.PATH_CATEGORIES, CATEGORY);

        matcher.addURI(authority, AlexandriaContract.PATH_FULLBOOK + "/#", BOOK_FULL_ID);
        matcher.addURI(authority, AlexandriaContract.PATH_FULLBOOK, BOOK_FULL);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case BOOK_FULL_ID:
                return AlexandriaContract.BookEntry.CONTENT_ITEM_TYPE;
            case BOOK_ID:
                return AlexandriaContract.BookEntry.CONTENT_ITEM_TYPE;
            case AUTHOR_ID:
                return AlexandriaContract.AuthorEntry.CONTENT_ITEM_TYPE;
            case CATEGORY_ID:
                return AlexandriaContract.CategoryEntry.CONTENT_ITEM_TYPE;
            case BOOK_FULL:
                return AlexandriaContract.BookEntry.CONTENT_TYPE;
            case BOOK:
                return AlexandriaContract.BookEntry.CONTENT_TYPE;
            case AUTHOR:
                return AlexandriaContract.AuthorEntry.CONTENT_TYPE;
            case CATEGORY:
                return AlexandriaContract.CategoryEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case BOOK:
                retCursor= sBookQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selection==null? null : selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case AUTHOR:
                retCursor= sAuthorQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CATEGORY:
                retCursor= sCategoryQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case BOOK_ID:
                retCursor= sBookQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        AlexandriaContract.BookEntry._ID + " = ? ",
                        new String[]{Long.valueOf(ContentUris.parseId(uri)).toString()},
                        null,
                        null,
                        sortOrder);
                break;
            case AUTHOR_ID:
                retCursor= sAuthorQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        AlexandriaContract.AuthorEntry._ID + " = ? ",
                        new String[]{Long.valueOf(ContentUris.parseId(uri)).toString()},
                        null,
                        null,
                        sortOrder);
                break;
            case CATEGORY_ID:
                retCursor= sCategoryQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        AlexandriaContract.CategoryEntry._ID + " = ? ",
                        new String[]{Long.valueOf(ContentUris.parseId(uri)).toString()},
                        null,
                        null,
                        sortOrder);
                break;
            case BOOK_FULL_ID:
                String[] bfdProjection = {
                    AlexandriaContract.BookEntry.TABLE_NAME + "."
                            + AlexandriaContract.BookEntry.TITLE,
                    AlexandriaContract.BookEntry.TABLE_NAME + "."
                            + AlexandriaContract.BookEntry.SUBTITLE,
                    AlexandriaContract.BookEntry.TABLE_NAME + "."
                            + AlexandriaContract.BookEntry.IMAGE_URL,
                    AlexandriaContract.BookEntry.TABLE_NAME + "."
                            + AlexandriaContract.BookEntry.DESC,
                    "group_concat(DISTINCT " + AlexandriaContract.AuthorEntry.TABLE_NAME + "."
                            + AlexandriaContract.AuthorEntry.AUTHOR + ") as "
                            + AlexandriaContract.AuthorEntry.AUTHOR,
                    "group_concat(DISTINCT " + AlexandriaContract.CategoryEntry.TABLE_NAME + "."
                            + AlexandriaContract.CategoryEntry.CATEGORY + ") as "
                            + AlexandriaContract.CategoryEntry.CATEGORY
                };
                retCursor = sBookFullQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        bfdProjection,
                        AlexandriaContract.BookEntry.TABLE_NAME + "."
                                + AlexandriaContract.BookEntry._ID + " = ? ",
                        new String[]{Long.valueOf(ContentUris.parseId(uri)).toString()},
                        AlexandriaContract.BookEntry.TABLE_NAME + "."
                                + AlexandriaContract.BookEntry._ID,
                        null,
                        sortOrder);
                break;
            case BOOK_FULL:
                String[] bfProjection = {
                        AlexandriaContract.BookEntry.TABLE_NAME + "."
                                + AlexandriaContract.BookEntry.TITLE,
                        AlexandriaContract.BookEntry.TABLE_NAME + "."
                                + AlexandriaContract.BookEntry.IMAGE_URL,
                        "group_concat(DISTINCT " + AlexandriaContract.AuthorEntry.TABLE_NAME + "."
                                + AlexandriaContract.AuthorEntry.AUTHOR + ") as "
                                + AlexandriaContract.AuthorEntry.AUTHOR,
                        "group_concat(DISTINCT " + AlexandriaContract.CategoryEntry.TABLE_NAME + "."
                                + AlexandriaContract.CategoryEntry.CATEGORY +") as "
                                + AlexandriaContract.CategoryEntry.CATEGORY
                };
                retCursor = sBookFullQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        bfProjection,
                        null,
                        selectionArgs,
                        AlexandriaContract.BookEntry.TABLE_NAME + "."
                                + AlexandriaContract.BookEntry._ID,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown query uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case BOOK: {
                long _id = db.insert(AlexandriaContract.BookEntry.TABLE_NAME, null, values);
                if ( _id > 0 ){
                    returnUri = AlexandriaContract.BookEntry.buildBookUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                getContext().getContentResolver()
                        .notifyChange(AlexandriaContract.BookEntry.buildFullBookUri(_id), null);
                break;
            }
            case AUTHOR:{
                long _id = db.insert(AlexandriaContract.AuthorEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = AlexandriaContract.AuthorEntry
                            .buildAuthorUri(values.getAsLong("_id"));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CATEGORY: {
                long _id = db.insert(AlexandriaContract.CategoryEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = AlexandriaContract.CategoryEntry
                            .buildCategoryUri(values.getAsLong("_id"));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
        }

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;
        // This makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (sUriMatcher.match(uri)) {
            case BOOK:
                rowsDeleted = db.delete(
                        AlexandriaContract.BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case AUTHOR:
                rowsDeleted = db.delete(
                        AlexandriaContract.AuthorEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CATEGORY:
                rowsDeleted = db.delete(
                        AlexandriaContract.CategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                rowsDeleted = db.delete(
                        AlexandriaContract.BookEntry.TABLE_NAME,
                        AlexandriaContract.BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown delete uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case BOOK:
                rowsUpdated = db.update(AlexandriaContract.BookEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case AUTHOR:
                rowsUpdated = db.update(AlexandriaContract.AuthorEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case CATEGORY:
                rowsUpdated = db.update(AlexandriaContract.CategoryEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown update uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}