package ptit.nttrung.filterimagesetwallpaper.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ptit.nttrung.filterimagesetwallpaper.R;
import ptit.nttrung.filterimagesetwallpaper.adapter.ThumbnailsAdapter;
import ptit.nttrung.filterimagesetwallpaper.utils.BitmapUtils;
import ptit.nttrung.filterimagesetwallpaper.utils.SpacesItemDecoration;

/**
 * Created by TrungNguyen on 2/26/2018.
 */

public class FilterFragment extends Fragment implements ThumbnailsAdapter.ThumbnailsAdapterListener{
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private ThumbnailsAdapter mAdapter;
    private List<ThumbnailItem> thumbnailItemList = new ArrayList<>();
    private FiltersListFragmentListener listener;
    private String filePath;

    public void setListener(FiltersListFragmentListener listener) {
        this.listener = listener;
    }

    public FilterFragment() {}

    public static FilterFragment newInstance(String filePath){
        FilterFragment filterFragment = new FilterFragment();
        Bundle args = new Bundle();
        args.putString("file_path",filePath);
        filterFragment.setArguments(args);
        return filterFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        this.filePath = args.getString("file_path");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filters_list, container, false);
        ButterKnife.bind(this, view);

        mAdapter = new ThumbnailsAdapter(getActivity(), thumbnailItemList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));
        recyclerView.setAdapter(mAdapter);

        prepareThumbnail(null);

        return view;
    }

    /**
     * Renders thumbnails in horizontal list
     * loads default image from Assets if passed param is null
     *
     * @param bitmap
     */
    public void prepareThumbnail(final Bitmap bitmap) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Bitmap thumbImage;
                if (bitmap == null) {
//                    thumbImage = BitmapUtils.getBitmapFromAssets(getActivity(), FilterActivity.IMAGE_NAME, 100, 100);
                    thumbImage = BitmapUtils.getBitmapFromFile(getActivity(),new File(filePath),100,100);
                } else {
                    thumbImage = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                }

                if (thumbImage == null) return;

                ThumbnailsManager.clearThumbs();
                thumbnailItemList.clear();

                // add normal bitmap first
                ThumbnailItem thumbnailItem = new ThumbnailItem();
                thumbnailItem.image = thumbImage;
                thumbnailItem.filterName = getString(R.string.filter_normal);
                ThumbnailsManager.addThumb(thumbnailItem);

                List<Filter> filters = FilterPack.getFilterPack(getActivity());

                for (Filter filter : filters) {
                    ThumbnailItem tI = new ThumbnailItem();
                    tI.image = thumbImage;
                    tI.filter = filter;
                    tI.filterName = filter.getName();
                    ThumbnailsManager.addThumb(tI);
                }

                thumbnailItemList.addAll(ThumbnailsManager.processThumbs(getActivity()));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        };

        new Thread(runnable).start();
    }

    @Override
    public void onFilterSelected(Filter filter) {
        if (listener != null)
            listener.onFilterSelected(filter);
    }

    public interface FiltersListFragmentListener {
        void onFilterSelected(Filter filter);
    }
}
