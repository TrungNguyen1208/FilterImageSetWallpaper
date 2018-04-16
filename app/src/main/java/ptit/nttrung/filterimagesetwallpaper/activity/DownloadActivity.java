package ptit.nttrung.filterimagesetwallpaper.activity;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ptit.nttrung.filterimagesetwallpaper.R;
import ptit.nttrung.filterimagesetwallpaper.utils.FileManager;

/**
 * Created by TrungNguyen on 2/6/2018.
 */

public class DownloadActivity extends BaseActivity {

    @BindView(R.id.image_preview)
    ImageView imagePreview;
    @BindView(R.id.download_button)
    Button downloadButton;
    @BindView(R.id.image_details_loader)
    ProgressBar loader;
    @BindView(R.id.image_details_imageview_holder)
    RelativeLayout imageHolder;
    @BindView(R.id.btn_set_background)
    Button setBgButton;

    private ProgressDialog progressDialog;
    private Handler handler = new Handler();

    public static final String imageUrl =
            "http://images.gapptech.net/wallpapers//800x1280/city_england_london_5376.jpg";
    private String type = ".png"; //fallback to ".png"
    private Uri path;
    private WallpaperManager wallpaperManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);

        wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        path = Uri.parse(imageUrl);
        updateButton();

        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .dontAnimate()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        hideLoader();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        hideLoader();

                        imagePreview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onImageClick();
                            }
                        });

                        Bitmap bitmap = ((GlideBitmapDrawable) resource.getCurrent()).getBitmap();
                        Palette palette = new Palette.Builder(bitmap).generate();
                        int defaultColor = 0xFF333333;
                        int color = palette.getDarkMutedColor(defaultColor);
                        imageHolder.setBackgroundColor(color);
                        return false;
                    }
                })
                .into(imagePreview);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Downloading...");
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        setBgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUrl != null) {
                    FileManager fileManager = new FileManager();
                    boolean imageExists = fileManager.fileExists(path.getLastPathSegment());
                    if (imageExists) {
                        File file = new File(Environment.getExternalStorageDirectory() + FileManager.DIRECTORY_BASE + "/" + path.getLastPathSegment());
                        Intent intent = new Intent(DownloadActivity.this, SetBackgroundActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("file_path",file.getPath());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                } else {
                    downdloadImage();
                }
            }
        });
    }

    private void showLoader() {
        loader.animate().alpha(1.0f).setDuration(300).start();
    }

    private void hideLoader() {
        loader.animate().alpha(0.0f).setDuration(300).start();
    }

    private void onImageClick() {
        Intent intent = new Intent(DownloadActivity.this, SetBackgroundActivity.class);
    }

    private void updateButton() {
        if (imageUrl != null) {
            FileManager fileManager = new FileManager();
            boolean imageExists = fileManager.fileExists(path.getLastPathSegment());
            Log.e("TAG", "imageExists " + imageExists);
            Log.e("TAG", "path.getLastPath " + path.getLastPathSegment());
            if (imageExists) {
                downloadButton.setClickable(false);
                downloadButton.setAlpha(0.5f);
            } else {
                if (!downloadButton.isClickable()) {
                    downloadButton.setClickable(true);
                    downloadButton.setText(R.string.action_save);
                }
                downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setClickable(false);
                        downdloadImage();
                    }
                });
            }
        }
    }

    private void downdloadImage() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(imageUrl).build();

                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    float file_size = response.body().contentLength();

                    File myDir = new File(Environment.getExternalStorageDirectory() + FileManager.DIRECTORY_BASE);
                    if (!myDir.exists()) myDir.mkdirs();

                    File file = new File(Environment.getExternalStorageDirectory() + FileManager.DIRECTORY_BASE + "/" + path.getLastPathSegment());

                    BufferedInputStream inputStream = new BufferedInputStream(response.body().byteStream());
                    OutputStream stream = new FileOutputStream(file);

                    byte[] data = new byte[8192];
                    float total = 0;
                    int read_bytes = 0;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.show();
                        }
                    });

                    while ((read_bytes = inputStream.read(data)) != -1) {
                        total = total + read_bytes;
                        stream.write(data, 0, read_bytes);
                        progressDialog.setProgress((int) ((total / file_size) * 100));
                    }

                    progressDialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(file));
                    sendBroadcast(intent);

                    stream.flush();
                    stream.close();
                    response.body().close();

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DownloadActivity.this, "Tai ko thanh cong", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        thread.start();
    }

//    public void setBackgroundToFile(final File file) {
//        setBackgroundToBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
//    }

//    private void setBackgroundToBitmap(final Bitmap sourceBitmap) {
//        final int sourceWidth = sourceBitmap.getWidth();
//        final int sourceHeight = sourceBitmap.getHeight();
//        final int letterboxedWidth = wallpaperManager.getDesiredMinimumWidth();
//        final int letterboxedHeight = wallpaperManager.getDesiredMinimumHeight();
//
//        final float resizeRatio = (float) letterboxedHeight / sourceHeight;
//        final int resizedWidth = (int) Math.ceil(sourceWidth * resizeRatio);
//
//        final Bitmap letterboxedBitmap = Bitmap.createBitmap(letterboxedWidth, letterboxedHeight, Bitmap.Config.ARGB_8888);
//
//        final Canvas canvas = new Canvas(letterboxedBitmap);
//        canvas.drawRGB(0, 0, 0);
//
//        final Matrix transformations = new Matrix();
//        transformations.postScale(resizeRatio, resizeRatio);
//        transformations.postTranslate(Math.round(((float) letterboxedWidth - resizedWidth) / 2), 0);
//        canvas.drawBitmap(sourceBitmap, transformations, null);
//
//        try {
//            wallpaperManager.setBitmap(letterboxedBitmap);
//            Toast.makeText(this, "SetWallpaper thanh cong", Toast.LENGTH_SHORT).show();
//        } catch (final IOException e) {
//            e.printStackTrace();
//        }
//    }
}
