package ptit.nttrung.filterimagesetwallpaper.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.webkit.MimeTypeMap;

/**
 * Created by TrungNguyen on 1/15/2018.
 */

public class Utils {
    public static boolean isNetworkConnected(Context context) {
        try{
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo() != null;
        }catch (Exception e){
            return true;
        }
    }

    public static void setImageAsWallpaperPicker(Context context,Uri path) {
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setType("image/*");

        MimeTypeMap map = MimeTypeMap.getSingleton();
        String mimeType = map.getMimeTypeFromExtension("png");
        intent.setDataAndType(path, mimeType);
        intent.putExtra("mimeType", mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent, "Set as"));
    }
}
