package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BookContract {

    private BookContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.books";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.books/books/ is a valid path for
     * looking at book data. content://com.example.android.books/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_BOOKS = "books";

    /**
     * Inner class that defines constant values for the books database table.
     * Each entry in the table represents a single book.
     */

    public static final class BookEntry implements BaseColumns {

        // Content URI to access book data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of books.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single book.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public static final String TABLE_NAME = "books";

        public static final String _ID = BaseColumns._ID;

        public static final String COLUMN_BOOK_NAME = "name";
        public static final String COLUMN_BOOK_PRICE = "price";
        public static final String COLUMN_BOOK_QUANTITY = "quantity";
        public static final String COLUMN_BOOK_SUPPLIER = "supplier";
        public static final String COLUMN_BOOK_PHONE = "phone";

        public static final int SUPPLIER_UNKNOWN = 0;
        public static final int SUPPLIER_OXFORD = 1;
        public static final int SUPPLIER_CAMBRIDGE = 2;
        public static final int SUPPLIER_PENGUIN = 3;
        public static final int SUPPLIER_MACMILLAN = 4;
    }
}