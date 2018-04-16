package ptit.nttrung.filterimagesetwallpaper.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ptit.nttrung.filterimagesetwallpaper.R;
import ptit.nttrung.filterimagesetwallpaper.adapter.AppInfoAdapter;
import ptit.nttrung.filterimagesetwallpaper.utils.OnMyItemClickListener;
import ptit.nttrung.filterimagesetwallpaper.utils.PInfo;
import ptit.nttrung.filterimagesetwallpaper.utils.SpacesItemDecoration;
import ptit.nttrung.filterimagesetwallpaper.utils.Utils;

/**
 * Created by TrungNguyen on 2/26/2018.
 */

public class MoreFragment extends Fragment {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private List<PInfo> list;
    private AppInfoAdapter adapter;
    private String filePath;
    private PackageManager pm;
    private File file;
    private Uri uri;

    public MoreFragment() {
    }

    public static MoreFragment newInstance(String filePath) {
        MoreFragment moreFragment = new MoreFragment();
        Bundle args = new Bundle();
        args.putString("file_path", filePath);
        moreFragment.setArguments(args);
        return moreFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        this.filePath = args.getString("file_path");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        ButterKnife.bind(this, view);

        this.list = getListApp();
        this.adapter = new AppInfoAdapter(getActivity(), this.list);
        this.adapter.setClickListener(new OnMyItemClickListener() {
            @Override
            public void onItemClick(int position) {
                PInfo pInfo = MoreFragment.this.list.get(position);
//                Toast.makeText(getContext(), pInfo.packageName, Toast.LENGTH_SHORT).show();
//                launchApp(pInfo.packageName);
                Utils.setImageAsWallpaperPicker(getContext(),MoreFragment.this.uri);
            }
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<PInfo> getListApp() {
        pm = getContext().getPackageManager();

        uri = Uri.fromFile(new File(this.filePath));

        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setType("image/*");

        MimeTypeMap map = MimeTypeMap.getSingleton();
        String mimeType = map.getMimeTypeFromExtension("jpg");
        intent.setDataAndType(uri, mimeType);
        intent.putExtra("mimeType", mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        List<ResolveInfo> ril = this.pm.queryIntentActivities(intent, 0);
        List<PInfo> list = new ArrayList<PInfo>();
        String name = null;

        try {
            for (ResolveInfo ri : ril) {
                PInfo pInfo = new PInfo();
                if (ri.activityInfo != null) {
                    Resources res = this.pm.getResourcesForApplication(ri.activityInfo.applicationInfo);
                    if (ri.activityInfo.labelRes != 0) {
                        name = res.getString(ri.activityInfo.labelRes);
                    } else {
                        name = ri.activityInfo.applicationInfo.loadLabel(this.pm).toString();
                    }
                    pInfo.packageName = ri.activityInfo.applicationInfo.packageName;
                    pInfo.appName = name;
                    pInfo.iconApp = this.pm.getApplicationIcon(pInfo.packageName);
                }
                list.add(pInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private void launchApp(String packageName) {
        Log.e("TAG packageName", packageName);
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        intent.setAction(Intent.ACTION_ATTACH_DATA);
//        intent.setType("image/*");

        MimeTypeMap map = MimeTypeMap.getSingleton();
        String mimeType = map.getMimeTypeFromExtension("jpg");
        Log.e("Tag filepath", filePath);
//        intent.setDataAndType(Uri.parse("file://" + filePath), "image/*");
//        intent.putExtra("mimeType", mimeType);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setDataAndType(android.support.v4.content.FileProvider.getUriForFile(getContext(), packageName + ".provider", new File(filePath)),"image/*");
        }else {
            intent.setDataAndType(this.uri,"image/*");
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(intent);
    }
}
