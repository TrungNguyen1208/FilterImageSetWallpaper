package ptit.nttrung.filterimagesetwallpaper.activity;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.isseiaoki.simplecropview.CropImageView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zomato.photofilters.imageprocessors.Filter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ptit.nttrung.filterimagesetwallpaper.R;
import ptit.nttrung.filterimagesetwallpaper.adapter.ViewPagerAdapter;
import ptit.nttrung.filterimagesetwallpaper.fragment.FilterFragment;
import ptit.nttrung.filterimagesetwallpaper.fragment.MoreFragment;
import ptit.nttrung.filterimagesetwallpaper.utils.BitmapUtils;

/**
 * Created by TrungNguyen on 2/23/2018.
 */

public class SetBackgroundActivity extends BaseActivity {

    @BindView(R.id.image_preview)
    CropImageView imagePreview;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    Bitmap originalImage;
    // to backup image with filter applied
    Bitmap filteredImage;

    // the final image after applying
    // brightness, saturation, contrast
    Bitmap finalImage;

    FilterFragment filterFragment;
    MoreFragment moreFragment;
    ViewPagerAdapter adapter;
    WallpaperManager wallpaperManager;

    private String filePath;
    private File file;

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_background);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Đặt làm hình nền");

        Intent intent = getIntent();
        filePath = intent.getExtras().getString("file_path");
        file = new File(filePath);

        loadImage();

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void loadImage() {
        originalImage = BitmapUtils.getBitmapFromFile(this, file, 360, 360);
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

//        Glide.with(this)
//                .load(new File(filePath))
//                .into(imagePreview);
        imagePreview.setImageBitmap(originalImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_set, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.action_set) {
            saveImageToGallery();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void setupViewPager(ViewPager viewPager) {
        // adding filter fragment
        filterFragment = FilterFragment.newInstance(filePath);
        filterFragment.setListener(new FilterFragment.FiltersListFragmentListener() {
            @Override
            public void onFilterSelected(Filter filter) {
                // applying the selected filter
                filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
                // preview filtered image
                imagePreview.setImageBitmap(filter.processFilter(filteredImage));

                finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);
            }
        });

        // adding more fragment
        moreFragment = MoreFragment.newInstance(filePath);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(filterFragment, "Bộ Lọc");
        adapter.addFragment(moreFragment, "Khác");
        viewPager.setAdapter(adapter);
    }

    private void saveImageToGallery() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final String path = BitmapUtils.insertImage(getContentResolver(), finalImage, System.currentTimeMillis() + "_profile.jpg", null);
                            Log.e("TAG path", path);
                            if (!TextUtils.isEmpty(path)) {
                                setBackgroundFromFile();
                            } else {
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Unable to save image!", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void setBackgroundFromFile() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels << 1; // best wallpaper width is twice screen width

        final Bitmap bitmap = BitmapUtils.getResizedBitmap(finalImage, width, height);

        wallpaperManager = WallpaperManager.getInstance(getApplicationContext());

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog("Đang xử lý");
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    wallpaperManager.setBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                hideProgressDialog();
                if (aBoolean == true) {
                    Toast.makeText(SetBackgroundActivity.this, "Thay ảnh thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SetBackgroundActivity.this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}
