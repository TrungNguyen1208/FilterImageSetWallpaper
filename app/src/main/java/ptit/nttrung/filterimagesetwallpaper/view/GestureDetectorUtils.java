package ptit.nttrung.filterimagesetwallpaper.view;

import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;

import java.lang.reflect.Field;

/**
 * Created by TrungNguyen on 2/5/2018.
 */


public final class GestureDetectorUtils {
    private static Object a(Class<?> cls, String str, Object obj) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cls.getDeclaredField(str);
        declaredField.setAccessible(true);
        return declaredField.get(obj);
    }

    public static void a(GestureDetectorCompat gestureDetectorCompat, int i) {
        try {
            Object a = a(GestureDetectorCompat.class, "mImpl", gestureDetectorCompat);
            if (Build.VERSION.SDK_INT > 17) {
                a = a(a.getClass(), "mDetector", a);
                a(a.getClass(), "mTouchSlopSquare", a, Integer.valueOf(i * i));
                return;
            }
            a(a.getClass(), "mTouchSlopSquare", a, Integer.valueOf(i * i));
        } catch (Exception e) {
        }
    }

    private static void a(Class<?> cls, String str, Object obj, Object obj2) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cls.getDeclaredField(str);
        declaredField.setAccessible(true);
        declaredField.set(obj, obj2);
    }
}