package com.hfad.lookafter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.BoringLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BooksActivity extends Activity {

    public static final String EXTRA_BOOKN0 = "bookNo";
    private Cursor cursor;
    private SQLiteDatabase database;
    private Book book;
    private Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);
        int bookNo = (Integer)getIntent().getExtras().get(EXTRA_BOOKN0);
        creatingCursor(bookNo);
        readingFromCursor();
        readingContentFromFile();
        displayingData();
    }

    private void creatingCursor(int bookNo) {
        try {
            SQLiteOpenHelper DataBaseHelper = new DatabaseHelper(this);
            database = DataBaseHelper.getWritableDatabase();
            cursor = database.query("BOOKS", new String[]{"AUTHOR", "TITLE", "COVER_RESOURCE_ID", "CONTENT_RESOURCE_ID", "FAVOURITE"}, "_id = ?",
                    new String[]{Integer.toString(bookNo)}, null, null, null);
        } catch (SQLiteException e) {
            showPrompt();
        }
    }

    private void readingFromCursor(){
        if(cursor.moveToFirst()){
            String author = cursor.getString(0);
            String title = cursor.getString(1);
            int cover_id = cursor.getInt(2);
            int content = cursor.getInt(3);
            boolean isFavourite = (cursor.getInt(4) == 1);
            creatingBook(author, title, cover_id, content, isFavourite);
        }
        closing();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_books, menu);
        setActionBarTitle();
        return true;
    }

    private void creatingBook(String author, String title, int cover_id, int content, boolean isFavourite){
        book = new Book(author, title, cover_id, content, isFavourite);
    }

    private void displayingData(){
        ImageView image = (ImageView)findViewById(R.id.pic);
        TextView title = (TextView)findViewById(R.id.title);
        image.setImageResource(book.getCover_resource_id());
        title.setText(book.getAuthor() + ' ' + book.getTitle());
    }

    private void readingContentFromFile(){
        TextView content = (TextView)findViewById(R.id.content);
        BufferedReader reader = null;
        try {
            InputStream inputStream = getResources().openRawResource(book.getContent_resource_id());
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append("\n");
            }
        } catch (IOException e) {
            Log.e("IOException", "readingContentFromFile()");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("IOException", "readingContentFromFile()");
                }
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_favourite:
                onFavouriteClicked();
                return true;
            case R.id.action_settings:
                //Todo: settings action
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onFavouriteClicked(){
        int bookNo = (Integer)getIntent().getExtras().get("bookNo");
        new UpdateBookTask().execute(bookNo);
    }

    private class UpdateBookTask extends AsyncTask<Integer, Void, Boolean> {
        ContentValues bookValues;

        @Override
        protected void onPreExecute(){
            bookValues = new ContentValues();
            Boolean isFavourite = book.isFavourite();
            showMessage(isFavourite);
            bookValues.put("FAVOURITE", !isFavourite);
            book.setFavourite(!isFavourite);
        }

        @Override
        protected Boolean doInBackground(Integer... books) {
            int bookNo = books[0];
            try {
                SQLiteOpenHelper DataBaseHelper = new DatabaseHelper(BooksActivity.this);
                database = DataBaseHelper.getWritableDatabase();
                database.update("BOOKS", bookValues, "_id = ?", new String[]{Integer.toString(bookNo)});
                database.close();
                return true;
            } catch (SQLiteException e) {
                return false;
            }
        }

        protected void onPostExecute(Boolean success){
            if(!success){
                showPrompt();
            }
        }
    }

    private void setActionBarTitle(){
        MenuItem favMenuItem = menu.findItem(R.id.action_favourite);
        if (book.isFavourite()){
            favMenuItem.setTitle(R.string.action_unfavourite);
        } else{
            favMenuItem.setTitle(R.string.action_favourite);
        }
    }


    private void showMessage(boolean isFavourite){
        int message;
        if (isFavourite){
            message = R.string.action_favourite_deleted;
        } else{
            message = R.string.action_favourite_added;
        }
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
    private void closing(){
        cursor.close();
        database.close();
    }

     private void showPrompt(){
         Toast toast = Toast.makeText(this, R.string.database_error, Toast.LENGTH_SHORT);
     }
    }
