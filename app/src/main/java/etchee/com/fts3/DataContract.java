package etchee.com.fts3;

import android.provider.ContactsContract;

/**
 * Created by rikutoechigoya on 2017/04/23.
 */

public final class DataContract {

    private DataContract() {
    }

    public static final class GlobalConstants {
        /**
         * all the column names in the DB
         **/
        public static final String KEY_ROWID = "rowid";
        public static final String KEY_CUSTOMER = "customer";
        public static final String KEY_NAME = "name";
        public static final String KEY_ADDRESS = "address";
        public static final String KEY_ADDRESS1 = "address1";
        public static final String KEY_ADDRESS2 = "address2";
        public static final String KEY_CITY = "city";
        public static final String KEY_STATE = "state";
        public static final String KEY_ZIP = "zipCode";
        public static final String KEY_SEARCH = "searchData";

        public static final String DATABASE_NAME = "CustomerData";
        public static final String FTS_VIRTUAL_TABLE = "CustomerInfo";
        public static final int DATABASE_VERSION = 1;

        //Create a FTS3 Virtual Table for fast searches
        public static final String DATABASE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE + " USING fts3(" +
                        KEY_CUSTOMER + "," +
                        KEY_NAME + "," +
                        KEY_ADDRESS1 + "," +
                        KEY_ADDRESS2 + "," +
                        KEY_CITY + "," +
                        KEY_STATE + "," +
                        KEY_ZIP + "," +
                        KEY_SEARCH + "," +
                        " UNIQUE (" + KEY_CUSTOMER + "));";
    }


}
