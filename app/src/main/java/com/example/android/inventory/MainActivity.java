package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.android.inventory.data.BookContract.BookEntry;
import com.example.android.inventory.data.BookDbHelper;

/**
 * Displays list of books that were entered and stored in the app.
 */
public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the book data loader */
    private static final int BOOK_LOADER = 0;

    /** Adapter for the ListView */
    BookCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with book data
        ListView bookListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so it only shows when the list has 0 items
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        // Set up an Adapter to create a list item for each row of book data in the Cursor.
        // There is no data until the loader finishes, so pass in null for the Cursor.
        mCursorAdapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(mCursorAdapter);

        // Set up the item click listener
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create new intent to go to EditorActivity
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific book that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link BookEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.books/books/2"
                // if the book with ID 2 was clicked on.
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentBookUri);

                // Launch the {@link EditorActivity} to display the data for the current book.
                startActivity(intent);
            }
        });

        // Start the Loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);

    }

    /**
     * Method to delete all books in the database
     */
    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " entries deleted from inventory database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options on the main screen.  Adds menus to app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar menu
        // TODO: add option to add a supplier. Tricky as buggery, but should be possible by adding
        // an item to the array, and tying supplier and phone arrays together
        switch (item.getItemId()) {
            // For "delete all entries"
            case R.id.action_delete_all_entries:
                deleteAllBooks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define the projection of items that we care about
        String[] projection = {BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, BookEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the BookCursorAdapter with this new cursor containing updated inventory data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}