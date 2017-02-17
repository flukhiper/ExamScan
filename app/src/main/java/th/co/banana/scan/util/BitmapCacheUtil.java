package th.co.banana.scan.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class BitmapCacheUtil {
   public static File getSavePath() {
       File path;
       if (hasSDCard()) { // SD card
           path = new File(getSDCardPath() + "/Tegaky/");
           path.mkdir();
       } else {
           path = Environment.getDataDirectory();
       }
       return path;
   }
   public static String getCacheFilename() {
       File f = getSavePath();
       return f.getAbsolutePath() + "/cache.png";
   }

   public static File loadFromFile(String filename) {
       try {
           File f = new File(filename);
           if (!f.exists()) { return null; }
//           Bitmap tmp = BitmapFactory.decodeFile(filename);
           return f;
       } catch (Exception e) {
           Log.d("Exception",e.getMessage());
           return null;
       }
   }
   public static File loadFromCacheFile() {
       return loadFromFile(getCacheFilename());
   }
   public static void saveToCacheFile(Bitmap bmp) {
       saveToFile(getCacheFilename(),bmp);
   }
   public static void saveToFile(String filename,Bitmap bmp) {
       try {
           FileOutputStream out = new FileOutputStream(filename);
           bmp.compress(CompressFormat.PNG, 100, out);
           out.flush();
           out.close();
       } catch(Exception e) {
           Log.d("Exception",e.getMessage());
       }
   }

   public static boolean hasSDCard() { // SD????????
       String status = Environment.getExternalStorageState();
       return status.equals(Environment.MEDIA_MOUNTED);
   }
   public static String getSDCardPath() {
       File path = Environment.getExternalStorageDirectory();
       return path.getAbsolutePath();
   }

}
