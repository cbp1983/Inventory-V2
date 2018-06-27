package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventory.data.BookDbHelper;
import com.example.android.inventory.data.BookContract.BookEntry;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the book inventory loader
     */
    private static final int EXISTING_BOOK_LOADER = 0;

    /**
     * Content URI for the existing book, null if it's a new book
     */
    private Uri mCurrentBookUri;

    /**
     * EditText field to enter the book's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the book's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the book's quantity in stock
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the book's supplier
     */
    private Spinner mSupplierSpinner;

    /**
     * EditText field to enter the book's supplier's phone number
     */
    private EditText mPhoneEditText;

    /**
     * Buttons to increase or decrease quantity
     */
    private Button mAddButton;
    private Button mMinusButton;

    /** Variables used to update Quantity with the buttons above */
    String quantityText = "0";
    int quantityInt;

    /**
     * Supplier of the book. The possible values are:
     * 0 for unknown, 1 for Oxford University Press, 2 for Cambridge University Press, 3 for
     * Penguin, 4 for MacMillan.
     */
    private int mSupplier = BookEntry.SUPPLIER_UNKNOWN;

    /**
     * Button to call the supplier with the number in the phone number EditText field.
     */
    private Button mPhoneButton;

    /**
     * Boolean flag that keeps track of whether the book has been edited (true) or not (false)
     */
    private boolean mBookHasChanged = false;

    /**
     * This OnTouchListener listens for the user tapping the view, implying modification to the item
     * and therefore changing the mBookHasChanged to True.
     */

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_activity);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new book or editing an existing one.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // If the intent DOES NOT contain a book content URI, then we know that we are
        // creating a new book.
        if (mCurrentBookUri == null) {
            // This is a new book, so change the app bar to say "Add a Book"
            setTitle(getString(R.string.editor_activity_title_new_book));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a book that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing book, so change app bar to say "Edit Book"
            setTitle(getString(R.string.editor_activity_title_edit_book));

            // Initialize a loader to read the book data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mSupplierSpinner = (Spinner) findViewById(R.id.spinner_supplier);
        mPhoneEditText = (EditText) findViewById(R.id.edit_phone_number);
        mAddButton = (Button) findViewById(R.id.increase_button);
        mMinusButton = (Button) findViewById(R.id.decrease_button);
        mPhoneButton = (Button) findViewById(R.id.call_button);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierSpinner.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);

        setupSpinner();

        // Make the buttons increase or decrease quantity
        // Set an int variable for quantity to interact with the text in mQuantityEditText

        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantityText = mQuantityEditText.getText().toString().trim();
                quantityInt = !quantityText.equals("")?Integer.parseInt(quantityText) : 0;
                if (quantityInt > 0) {
                    quantityInt = quantityInt - 1;
                    quantityText = Integer.toString(quantityInt);
                    mQuantityEditText.setText(quantityText);
                }
                else {
                    return;
                }
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantityText = mQuantityEditText.getText().toString().trim();
                quantityInt = !quantityText.equals("")?Integer.parseInt(quantityText) : 0;
                quantityInt = quantityInt + 1;
                quantityText = Integer.toString(quantityInt);
                mQuantityEditText.setText(quantityText);
            }
        });

        // This code lets you call the number in the phone number field with the phone button.
        mPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                String callNumber = mPhoneEditText.getText().toString();
                phoneIntent.setData(Uri.parse("tel:" + callNumber));
                startActivity(phoneIntent);
            }
        });
    }

    /**
     * Setup the dropdown spinner that allows the user to select the supplier of the book.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter supplierSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        supplierSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSupplierSpinner.setAdapter(supplierSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.supplier_oxford))) {
                        mSupplier = BookEntry.SUPPLIER_OXFORD; // Oxford University Press
                        mPhoneEditText.setText("+44 1536 452657"); // Sets phone number to +44 1536 452657
                    } else if (selection.equals(getString(R.string.supplier_cambridge))) {
                        mSupplier = BookEntry.SUPPLIER_CAMBRIDGE; // Cambridge University Press
                        mPhoneEditText.setText("+44 1223 358331"); // Sets phone number to +44 1223 358331
                    } else if (selection.equals(getString(R.string.supplier_penguin))) {
                        mSupplier = BookEntry.SUPPLIER_PENGUIN; // Penguin Random House
                        mPhoneEditText.setText("+44 1206 256000"); // Sets phone number to +44 1206 256000
                    } else if (selection.equals(getString(R.string.supplier_macmillan))) {
                        mSupplier = BookEntry.SUPPLIER_MACMILLAN; // MacMillan
                        mPhoneEditText.setText("+44 1256 329242"); // Sets phone number to +44 1256 329242
                    } else {
                        mSupplier = BookEntry.SUPPLIER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplier = 0; // Unknown
            }
        });
    }

    /**
     * Get user input from the editor and save the book into the database
     */

    private void saveBook() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();

        // Check if this is a new book (as opposed to the editing of an existing book)
        // And check if all the fields in the editor are blank
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(phoneString) &&
                mSupplier == 0) {
            // With no fields modified, we can exit early without creating a new book entry,
            // and without modifying ContentValues or ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys, and editor attributes
        // are the values.
        ContentValues values = new ContentValues();

        // Add values into the ContentValues object.
        values.put(BookEntry.COLUMN_BOOK_NAME, nameString);
        values.put(BookEntry.COLUMN_BOOK_PHONE, phoneString);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER, mSupplier);

        // If the quantity or price are not given by the user, default to 0
        String price = "0";
        int quantity = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = priceString;
        }
        values.put(BookEntry.COLUMN_BOOK_PRICE, price);
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);


        // Determine if this is a new book or not by checking if there's a value for mCurrentBookUri
        if (mCurrentBookUri == null) {
            // This is a new book, so add a new book to the provider and return the URI
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            // Show a toast message showing if insertion was successful
            if (newUri == null) {
                // A null newUri means there was an error, so display error message
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_LONG).show();
            } else {
                // Otherwise insertion was successful; show this with a toast
                Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an existing book, so update the book with the URI mCurrentBookUri
            // and pass in the new ContentValues. Pass in null for the selection and selectionArgs
            // because mCurrentBookUri will already identify the correct row in the database that we
            // want to modify.
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

            // Show a toast message regarding whether this was successful or not
            if (rowsAffected == 0) {
                // If no rows were affected, there was an error with the update
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_LONG).show();
            } else {
                // Otherwise it was successful and we can display an appropriate toast
                Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Sanity check
                sanityCheck();
                // Save book to database
                if (sanityCheck()) {
                    saveBook();
                    finish();
                    return true;
                }
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This is a sanity check method to make sure there are no null values.
     *
     */
    public boolean sanityCheck() {
        // Check for empty values for Name and Phone number.  If they are not present, show a toast
        // message with the error. Supplier defaults to unknown, and quantity and price are set
        // to default to 0 in code below, so only Name and Phone are unaccounted for. Otherwise,
        // proceed with saving the book data.
        String nameString = mNameEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();

        if (nameString.matches("")) {
            Toast.makeText(this, getString(R.string.name_missing), Toast.LENGTH_SHORT).show();
            return false;
        } else if (phoneString.matches("")) {
            Toast.makeText(this, getString(R.string.phone_missing), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all the possible attributes, define a projection with all tables
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_PHONE,
                BookEntry.COLUMN_BOOK_SUPPLIER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, mCurrentBookUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Leave early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Move to the first row of the cursor and read data from it
        if (cursor.moveToFirst()) {
            // Find the columns we want
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int phoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PHONE);
            int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER);

            // Extract the value we want from the cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int supplier = cursor.getInt(supplierColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(price);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPhoneEditText.setText(phone);

            // Supplier comes from a spinner, so map the constant values from the database into 
            // one of the dropdown options, then call setSelection so that option is displayed
            // on screen as the current selection.
            switch (supplier) {
                case BookEntry.SUPPLIER_OXFORD:
                    mSupplierSpinner.setSelection(1);
                    break;
                case BookEntry.SUPPLIER_CAMBRIDGE:
                    mSupplierSpinner.setSelection(2);
                    break;
                case BookEntry.SUPPLIER_PENGUIN:
                    mSupplierSpinner.setSelection(3);
                    break;
                case BookEntry.SUPPLIER_MACMILLAN:
                    mSupplierSpinner.setSelection(4);
                    break;
                default:
                    mSupplierSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mPhoneEditText.setText("");
        mSupplierSpinner.setSelection(0);
    }


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}

