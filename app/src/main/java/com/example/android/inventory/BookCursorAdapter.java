package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.BookContract.BookEntry;

/**
 * THe BookCursorAdapter is an adapter for a ListView that uses a Cursor of book data as a source.
 * It creates list items for each new row of data in the Cursor.
 */



public class BookCursorAdapter extends CursorAdapter {


    /** Variables used to update Quantity with the buttons above */
    String bookQuantity;
    int quantityInt;


    /**
     * Creates a new BookCursorAdapter, Context context, Cursor c
     */

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current book can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find individual views in list_item.xml that we want to be able to modify in the list
        // item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.book_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.book_price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.book_quantity);


        /**
         * This section is for getting the phone number. It doesn't work. Why?
         */
        TextView phoneTextView = (TextView) view.findViewById(R.id.phone_number_test_view);
        int phoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PHONE);
        String phoneNumber = cursor.getString(phoneColumnIndex);
        phoneTextView.setText(phoneNumber);

        // Find the columns of data we're interested in binding
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        final int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);

        // Get the attributes from the cursor for the current book
        String bookName = cursor.getString(nameColumnIndex);
        String bookPrice = cursor.getString(priceColumnIndex);
        bookQuantity = cursor.getString(quantityColumnIndex);

        // Update the TextViews with the data from the cursor on the current book
        nameTextView.setText(bookName);
        priceTextView.setText(bookPrice);
        quantityTextView.setText(bookQuantity);

        // Sale button function to decrease quantity by 1, as long as the value is not 0 or below
        // We also need to produce a URI function here, and assign the _ID to a variable

        Button saleButton = (Button) view.findViewById(R.id.sale_button);
        final int quantityId = cursor.getInt(cursor.getColumnIndex(BookEntry._ID));
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantityInt = Integer.parseInt(bookQuantity);
                if (quantityInt > 0) {
                    quantityInt = quantityInt - 1;

                    Uri quantityUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, quantityId);
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantityInt);
                    context.getContentResolver().update(quantityUri, values, null, null);
                }
                else {
                    Toast.makeText(context, R.string.out_of_stock, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Call button. Pressing this calls the supplier using the phone number stored for the book
        // This method reuses the quantityId variable from above
/**
        Button phoneButton = (Button) view.findViewById(R.id.phone_item_button);
        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                int phoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PHONE);
                String phoneNumber = cursor.getString(phoneColumnIndex);
                phoneIntent.setData(Uri.parse("tel:" + phoneNumber));
                context.startActivity(phoneIntent);
            }
        });
*/

    }
}
