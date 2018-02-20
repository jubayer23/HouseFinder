package com.creative.housefinder.Utility;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.creative.housefinder.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jubayer on 5/11/2017.
 */

public class CommonMethods {

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(String path,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return  BitmapFactory.decodeFile(path, options);
    }
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static File createDirectoryWithFileName(String folder_name, String file_name){

        final File f = new File(
                Environment.getExternalStorageDirectory() + "/"
                        + folder_name + "/"
                        + file_name);
        File dirs = new File(f.getParent());
        if (!dirs.exists())
            dirs.mkdirs();
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;

    }

    public static File createDirectory(String folder_name){

        final File f = new File(
                Environment.getExternalStorageDirectory() + "/"
                        + folder_name );
        //File dirs = new File(f.getParent());
        if (!f.exists())
            f.mkdirs();
        return f;

    }


    /**
     * Directory that files are to be read from and written to
     **/
    protected static final File DIRECTORY =
            new File(Environment.getExternalStorageDirectory(), "HouseFinder");

    public static final String FILE_NAME = "house_coordinates.json";

    public static void copyRawFileToExternalMemory(Context context){

        AssetManager assetManager = context.getAssets();
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = assetManager.openFd( "house_coordinates.json");

            // Create new file to copy into.
            //File file = new File(Environment.getExternalStorageDirectory() + java.io.File.separator + "NewFile.dat");
            String filename = FILE_NAME;
            File exportDir = DIRECTORY;
            File file = new File(exportDir, filename);

            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            try {
                file.createNewFile();
                copyFdToFile(assetFileDescriptor.getFileDescriptor(), file);
               // return true;
            } catch (IOException e) {
                e.printStackTrace();
               // return false;
            }



        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void copyFdToFile(FileDescriptor src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    public static void showKeyboardForcely(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void hideKeyboardForcely(Context context, EditText myEditText){
        InputMethodManager imm = (InputMethodManager)context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
    }

    public static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Bitmap getBitmapFromImageFilePath(String path){
        File image = new File(path);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
       return  BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
    }
    public static String convertBitmapToBase64(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static boolean isImage(String file_name){
        file_name = file_name.toLowerCase();
        if(file_name.contains("png") ||
                file_name.contains("jpeg") ||
                file_name.contains("jpg")||
                file_name.contains("gif")) {
            return true;
        }else{
            return false;
        }
    }

    public static double roundFloatToFiveDigitAfterDecimal(double value){

        return (double) Math.round(value * 100000d) / 100000d;

    }


    /*"yyyy-MM-dd HH:mm:ss"*/
    public static String currentDate(String format){
        DateFormat dateFormat = new SimpleDateFormat(format);
        Date date = new Date();
        return  dateFormat.format(date);
    }
}