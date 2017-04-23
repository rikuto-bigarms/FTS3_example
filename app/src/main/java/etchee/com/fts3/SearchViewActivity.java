package etchee.com.fts3;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.Calendar;

import static android.content.ContentValues.TAG;
import static etchee.com.fts3.DataContract.GlobalConstants.DATABASE_CREATE;
import static etchee.com.fts3.DataContract.GlobalConstants.DATABASE_NAME;
import static etchee.com.fts3.DataContract.GlobalConstants.DATABASE_VERSION;
import static etchee.com.fts3.DataContract.GlobalConstants.FTS_VIRTUAL_TABLE;
import static etchee.com.fts3.DataContract.GlobalConstants.KEY_ADDRESS;
import static etchee.com.fts3.DataContract.GlobalConstants.KEY_ADDRESS1;
import static etchee.com.fts3.DataContract.GlobalConstants.KEY_ADDRESS2;
import static etchee.com.fts3.DataContract.GlobalConstants.KEY_CITY;
import static etchee.com.fts3.DataContract.GlobalConstants.KEY_CUSTOMER;
import static etchee.com.fts3.DataContract.GlobalConstants.KEY_NAME;
import static etchee.com.fts3.DataContract.GlobalConstants.KEY_SEARCH;
import static etchee.com.fts3.DataContract.GlobalConstants.KEY_STATE;
import static etchee.com.fts3.DataContract.GlobalConstants.KEY_ZIP;

/**
 * Created by rikutoechigoya on 2017/04/23.
 */

public class SearchViewActivity extends Activity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    private ListView mListView;
    private SearchView searchView;

    private TextView inspectionDate;
    private TextView customerText;
    private TextView nameText;
    private TextView addressText;
    private TextView cityText;
    private TextView stateText;
    private TextView zipCodeText;

    //define in the later section in this class
    private DatabaseHelper mDbHelper;
    //sample class
    private SQLiteDatabase mDb;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = (SearchView) findViewById(R.id.search);
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        mListView = (ListView) findViewById(R.id.list);
        inspectionDate = (TextView) findViewById(R.id.inspectionDate);
        displayDate();

        //create FTS database
        openDbInstance();

        //Clean all Customers
        deleteAllCustomers();
        //Add some Customer data as a sample
        insertCustomer("PIZZA1", "Pizza Hut", "1107 West Adams Boulevard", "", "Los Angeles", "CA", "90007");
        insertCustomer("PIZZA2", "Pizza Hut", "1562 West Pico Boulevard", "", "Los Angeles", "CA", "90015");
        insertCustomer("PIZZA3", "Pizza Hut", "718 South Los Angeles Street", "", "Los Angeles", "CA", "90014");
        insertCustomer("PIZZA4", "Pizza Hut", "2542 West Temple Street", "", "Los Angeles", "CA", "90026");
        insertCustomer("PIZZA5", "Pizza Hut", "4329 North Figueroa Street", "", "Los Angeles", "CA", "90065");
        insertCustomer("PIZZA6", "Pizza Hut", "4351 South Central Avenue", "", "Los Angeles", "CA", "90011");
        insertCustomer("SUB1", "Subway", "975 West Jefferson", "", "Los Angeles", "CA", "90007");
        insertCustomer("SUB2", "Subway", "2805 South Figueroa Street", "", "Los Angeles", "CA", "90007");
        insertCustomer("SUB3", "Subway", "198 South Vermont Avenue", "", "Los Angeles", "CA", "90004");
        insertCustomer("SUB4", "Subway", "504 West Olympic Boulevard", "", "Los Angeles", "CA", "90015");

    }


    /**
     * Method to put customer values into the SQL
     */
    public long insertCustomer(String customer, String name, String address1, String address2, String city, String state, String zipCode) {

        ContentValues initialValues = new ContentValues();
        String searchValue = customer + " " +
                name + " " +
                address1 + " " +
                city + " " +
                state + " " +
                zipCode;
        initialValues.put(KEY_CUSTOMER, customer);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_ADDRESS1, address1);
        initialValues.put(KEY_ADDRESS2, address2);
        initialValues.put(KEY_CITY, city);
        initialValues.put(KEY_STATE, state);
        initialValues.put(KEY_ZIP, zipCode);
        initialValues.put(KEY_SEARCH, searchValue);

        return mDb.insert(FTS_VIRTUAL_TABLE, null, initialValues);
    }

    /**
     * Takes userInput, search thru the db and then returns cursor to iterate in the adapter.
     *
     * @param inputText User input text sent from the UI
     * @return cursor to iterate thru to show results in ListView
     * @throws SQLException if search fails.
     */
    public Cursor searchCustomer(String inputText) throws SQLException {
        Log.w(TAG, inputText);
        String query = "SELECT docid as _id," +
                KEY_CUSTOMER + "," +
                KEY_NAME + "," +
                "(" + KEY_ADDRESS1 + "||" +
                "(case when " + KEY_ADDRESS2 + "> '' then '\n' || " + KEY_ADDRESS2 + " else '' end)) as " + KEY_ADDRESS + "," +
                KEY_ADDRESS1 + "," +
                KEY_ADDRESS2 + "," +
                KEY_CITY + "," +
                KEY_STATE + "," +
                KEY_ZIP +
                " from " + FTS_VIRTUAL_TABLE +
                " where " + KEY_SEARCH + " MATCH '" + inputText + "';";
        Cursor mCursor = mDb.rawQuery(query, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public boolean deleteAllCustomers() {

        int doneDelete;
        doneDelete = mDb.delete(FTS_VIRTUAL_TABLE, null, null);
        Log.w(TAG, Integer.toString(doneDelete));
        return doneDelete > 0;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close();
    }

    public boolean onQueryTextChange(String newText) {
        showResults(newText + "*");
        return false;
    }

    public boolean onQueryTextSubmit(String query) {
        showResults(query + "*");
        return false;
    }

    public boolean onClose() {
        showResults("");
        return false;
    }

    private void showResults(String query) {

        Cursor cursor = searchCustomer((query != null ? query.toString() : "@@@@"));

        if (cursor == null) {
            //
        } else {
            // Specify the columns we want to display in the result
            String[] from = new String[]{
                    KEY_CUSTOMER,
                    KEY_NAME,
                    KEY_ADDRESS,
                    KEY_CITY,
                    KEY_STATE,
                    KEY_ZIP};

            // Specify the Corresponding layout elements where we want the columns to go
            int[] to = new int[]{R.id.scustomer,
                    R.id.sname,
                    R.id.saddress,
                    R.id.scity,
                    R.id.sstate,
                    R.id.szipCode};

            // Create a simple cursor adapter for the definitions and apply them to the ListView
            SimpleCursorAdapter customers = new SimpleCursorAdapter(this, R.layout.customerresult, cursor, from, to);
            mListView.setAdapter(customers);

            // Define the on-click listener for the list items
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Get the cursor, positioned to the corresponding row in the result set
                    Cursor cursor = (Cursor) mListView.getItemAtPosition(position);

                    // Get the state's capital from this row in the database.
                    String customer = cursor.getString(cursor.getColumnIndexOrThrow("customer"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String city = cursor.getString(cursor.getColumnIndexOrThrow("city"));
                    String state = cursor.getString(cursor.getColumnIndexOrThrow("state"));
                    String zipCode = cursor.getString(cursor.getColumnIndexOrThrow("zipCode"));

                    //Check if the Layout already exists
                    LinearLayout customerLayout = (LinearLayout) findViewById(R.id.customerLayout);
                    if (customerLayout == null) {
                        //Inflate the Customer Information View
                        LinearLayout leftLayout = (LinearLayout) findViewById(R.id.rightLayout);
                        View customerInfo = getLayoutInflater().inflate(R.layout.selected_customer_detail, leftLayout, false);
                        leftLayout.addView(customerInfo);
                    }

                    //Get References to the TextViews
                    customerText = (TextView) findViewById(R.id.customer);
                    nameText = (TextView) findViewById(R.id.name);
                    addressText = (TextView) findViewById(R.id.address);
                    cityText = (TextView) findViewById(R.id.city);
                    stateText = (TextView) findViewById(R.id.state);
                    zipCodeText = (TextView) findViewById(R.id.zipCode);

                    // Update the parent class's TextView
                    customerText.setText(customer);
                    nameText.setText(name);
                    addressText.setText(address);
                    cityText.setText(city);
                    stateText.setText(state);
                    zipCodeText.setText(zipCode);

                    searchView.setQuery("", true);
                }
            });
        }
    }

    private void displayDate() {

        final Calendar c = Calendar.getInstance();

        inspectionDate.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(c.get(Calendar.MONTH) + 1).append("/")
                        .append(c.get(Calendar.DAY_OF_MONTH)).append("/")
                        .append(c.get(Calendar.YEAR)).append(" "));
    }

    public SearchViewActivity openDbInstance() throws SQLException {
        mDbHelper = new DatabaseHelper(this);
        mDb = mDbHelper.getReadableDatabase();
        return this;
    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }



    /**
     * Inner class to Create db itself
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }
    }
}
