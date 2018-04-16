package ptit.nttrung.filterimagesetwallpaper.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import ptit.nttrung.filterimagesetwallpaper.R;
import ptit.nttrung.filterimagesetwallpaper.view.ProgressDialogFragment;

/**
 * Created by TrungNguyen on 10/17/2017.
 */

public class BaseActivity extends AppCompatActivity {

    private static final String TAG_DIALOG_FRAGMENT = "tagDialogFragment";
    Toolbar toolbar;
    private MenuItem inboxMenuItem;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        bindViews();
    }

    protected void bindViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setupToolbar();
    }

    public void setContentViewWithoutInject(int layoutResId) {
        super.setContentView(layoutResId);
    }

    protected void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
        }
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public MenuItem getInboxMenuItem() {
        return inboxMenuItem;
    }

    public void showProgressDialog(String message) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getExistingDialogFragment();
        if (prev == null) {
            ProgressDialogFragment fragment = ProgressDialogFragment.getInstance();
            fragment.show(ft, TAG_DIALOG_FRAGMENT);
        }
    }

    public void hideProgressDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getExistingDialogFragment();
        if (prev != null) {
            try {
                ft.remove(prev).commit();
            } catch (Exception e) {
                ft.remove(prev).commitAllowingStateLoss();
            }
        }
    }

    private Fragment getExistingDialogFragment() {
        return getSupportFragmentManager().findFragmentByTag(TAG_DIALOG_FRAGMENT);
    }

    public void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
