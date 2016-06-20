package com.cindypotvin.picturestorage;

        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.util.Log;
        import android.graphics.Bitmap;
        import java.io.File;
        import java.io.FileOutputStream;
        import android.graphics.BitmapFactory;

public class ApplicationDatabaseHelper extends SQLiteOpenHelper {
   // If you change the database schema, you must increment the database version.
   public static final int DATABASE_VERSION = 1;
   // The name of the database file on the file system
   public static final String DATABASE_NAME = "Projects.db";

   private Context mContext;
   public ApplicationDatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
      mContext = context;
   }

   /**
    * Gets the picture for the specified report in the database.
    *
    * @param reportId the identifier of the report for which to get the picture.
    *
    * @return the picture for the report, or null if no picture was found.
    */
   public Bitmap getReportPicture(long reportId) {
      String picturePath = getReportPicturePath(reportId);
      if (picturePath == null || picturePath.length() == 0)
         return (null);

      Bitmap reportPicture = BitmapFactory.decodeFile(picturePath);

      return (reportPicture);
   }

   /**
    * Gets the path of the picture for the specified report in the database.
    *
    * @param reportId the identifier of the report for which to get the picture.
    *
    * @return the picture for the report, or null if no picture was found.
    */
   private String getReportPicturePath(long reportId) {
      // Gets the database in the current database helper in read-only mode
      SQLiteDatabase db = getReadableDatabase();

      // After the query, the cursor points to the first database row
      // returned by the request
      Cursor reportCursor = db.query(ReportContract.TABLE_NAME,
              null /**All columns */,
              ReportContract.ReportEntry._ID + " = ? ",
              new String[]{Long.toString(reportId)},
              null,
              null,
              null);

      if (reportCursor != null && reportCursor.moveToFirst())
         {
      // Get the path of the picture from the database row pointed by
      // the cursor using the getColumnIndex method of the cursor.
         int columnIndex = reportCursor.getColumnIndex(ReportContract.ReportEntry.COLUMN_PICTURE_TITLE);
         String picturePath = reportCursor.getString(columnIndex);
         reportCursor.close();
         return (picturePath);
         }
      else
         return (null);
   }

   /**
    * Initialize example data to show when the application is first installed.
    *
    * @param db the database being initialized.
    */
   private void initializeExampleData(SQLiteDatabase db)
      {
      // A lot of code is repeated here that could be factorised in methods,
      // but this is clearer for the example

      // Insert the database row for an example project in the report table in the
      // database
      long reportId;
      ContentValues firstReportValues = new ContentValues();
      firstReportValues.put(ReportContract.ReportEntry.COLUMN_NAME_TITLE, "Incident report for 2nd avenue");
      reportId = db.insert(ReportContract.TABLE_NAME, null, firstReportValues);
      }

   public long createReport()
   {
      SQLiteDatabase db = getWritableDatabase();
      long reportId;
      ContentValues firstReportValues = new ContentValues();
      firstReportValues.put(ReportContract.ReportEntry.COLUMN_NAME_TITLE, "Incident report for 2nd avenue");
      reportId = db.insert(ReportContract.TABLE_NAME, null, firstReportValues);
      return (reportId);
   }

   /**
    * Creates the underlying database with the SQL_CREATE_TABLE queries from
    * the contract classes to create the tables and initialize the data.
    * The onCreate is triggered the first time someone tries to access
    * the database with the getReadableDatabase or
    * getWritableDatabase methods.
    *
    * @param db the database being accessed and that should be created.
    */
   @Override
   public void onCreate(SQLiteDatabase db) {
      // Create the database to contain the data for the reports
      db.execSQL(ReportContract.SQL_CREATE_TABLE);

      initializeExampleData(db);
   }

   /**
    *
    * This method must be implemented if your application is upgraded and must
    * include the SQL query to upgrade the database from your old to your new
    * schema.
    *
    * @param db the database being upgraded.
    * @param oldVersion the current version of the database before the upgrade.
    * @param newVersion the version of the database after the upgrade.
    */
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      // Logs that the database is being upgraded
      Log.i(ApplicationDatabaseHelper.class.getSimpleName(),
              "Upgrading database from version " + oldVersion + " to " + newVersion);
   }

   /**
    * Updates the current picture for the report.
    *
    * @param reportId the identifier of the report for which to save the picture
    * @param picture the picture to save to the internal storage and save path in the database.
    */
   public long updateReportPicture(long reportId, Bitmap picture) {
      // Removes picture from the database if one already exists
      deletePictureFromStorage(reportId);

      // Adds the new picture to the internal storage
      File internalStorage = mContext.getDir("ReportPictures", Context.MODE_PRIVATE);
      File reportFilePath = new File(internalStorage, reportId + ".png");
      String picturePath = reportFilePath.toString();

      FileOutputStream fos = null;
      try {
         fos = new FileOutputStream(reportFilePath);
         picture.compress(Bitmap.CompressFormat.PNG, 100 /*quality*/, fos);
         fos.close();
      }
      catch (Exception ex) {
         Log.i("DATABASE", "Problem updating picture", ex);
         picturePath = "";
      }

      // Updates the database entry for the report to point to the picture
      SQLiteDatabase db = getWritableDatabase();

      ContentValues newPictureValue = new ContentValues();
      newPictureValue.put(ReportContract.ReportEntry.COLUMN_PICTURE_TITLE, picturePath);
      long updatedReportId = db.update(ReportContract.TABLE_NAME,
                                       newPictureValue,
                                       ReportContract.ReportEntry._ID + " = ? ",
                                       new String[]{Long.toString(reportId)});
      db.close();
      return (updatedReportId);
   }

   /**
    * Deletes the specified report from the database, removing also the associated picture from the
    * internal storage if any.
    *
    * @param reportId the report to remove.
    */
   public void deleteReport(long reportId) {
      // Remove picture for report from internal storage
      String picturePath = getReportPicturePath(reportId);
      if (picturePath != null && picturePath.length() != 0) {
         File reportFilePath = new File(picturePath);
         reportFilePath.delete();
      }

      // Remove the report from the database
      SQLiteDatabase db = getWritableDatabase();

      db.delete(ReportContract.TABLE_NAME,
                ReportContract.ReportEntry._ID + " = ? ",
                new String[]{Long.toString(reportId)});
   }

   /**
    * Deletes the picture for the report from the internal storage, if any.
    *
    * @param reportId the report to remove.
    */
   private void deletePictureFromStorage(long reportId) {
      String picturePath = getReportPicturePath(reportId);
      if (picturePath != null && picturePath.length() != 0) {
         File reportFilePath = new File(picturePath);
         reportFilePath.delete();
      }
   }
}

