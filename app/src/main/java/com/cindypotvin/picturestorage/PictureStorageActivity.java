package com.cindypotvin.picturestorage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
// Creates a report on application starts, you can add a picture to the report, or remove the report
// and the picture.
public class PictureStorageActivity extends ActionBarActivity {
   private ApplicationDatabaseHelper mDatabase;
   private static final int REQUEST_CODE = 1;
   private Bitmap mBitmap;
   private ImageView mImageView;
   private long mReportId;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_picture_storage);

      mImageView = (ImageView) findViewById(R.id.picture_to_save);
      // Gets the database helper to access the database for the application
      mDatabase = new ApplicationDatabaseHelper(this);
      mReportId = mDatabase.createReport();
      mBitmap  = mDatabase.getReportPicture(mReportId);
      if (mBitmap != null)
         mImageView.setImageBitmap(mBitmap);
   }

   public void takePicture(View View) {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      startActivityForResult(intent, REQUEST_CODE);
   }

   public void deleteReport(View view){
      mDatabase.deleteReport(mReportId);
      mImageView.setImageBitmap(null);
      }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      InputStream stream = null;
      if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
         try {
            // Empty existing bitmap, if any
            if (mBitmap != null)
               mBitmap.recycle();

            stream = getContentResolver().openInputStream(data.getData());
            mBitmap = BitmapFactory.decodeStream(stream);
            mImageView.setImageBitmap(mBitmap);

            // Save bitmap to internal database and to internal storage
            mDatabase.updateReportPicture(mReportId, mBitmap);

         } catch (FileNotFoundException e) {
            e.printStackTrace();
         } finally {
            if (stream != null) {
               try {
                  stream.close();
               } catch (IOException e) {
                  e.printStackTrace();
               }
            }
         }
      }
   }
}
