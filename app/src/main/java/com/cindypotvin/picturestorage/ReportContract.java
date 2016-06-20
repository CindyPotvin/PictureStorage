package com.cindypotvin.picturestorage;
import android.provider.BaseColumns;

/**
 * This class represents a contract for a projects table containing projects for
 * which to count rows.
 */
public final class ReportContract {

   /**
    * Contains the name of the table to create that contans the row counters.
    */
   public static final String TABLE_NAME = "report";

   /**
    * Contains the SQL query to use to create the table containing the projects.
    */
   public static final String SQL_CREATE_TABLE = "CREATE TABLE " + ReportContract.TABLE_NAME +
           " (" + ReportContract.ReportEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
           + ReportContract.ReportEntry.COLUMN_NAME_TITLE + " TEXT,"
           + ReportContract.ReportEntry.COLUMN_PICTURE_TITLE + " TEXT);";

   /**
    * This class represents the rows for an entry in the project table. The
    * primary key is the _id column from the BaseColumn class.
    */
   public static abstract class ReportEntry implements BaseColumns {
      // Name of the project as shown in the application.
      public static final String COLUMN_NAME_TITLE = "title";
      public static final String COLUMN_PICTURE_TITLE = "picture";
   }
}