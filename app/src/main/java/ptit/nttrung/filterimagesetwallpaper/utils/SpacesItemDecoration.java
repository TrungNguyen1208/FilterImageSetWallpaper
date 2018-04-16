package ptit.nttrung.filterimagesetwallpaper.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by TrungNguyen on 2/2/2018.
 */

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) == state.getItemCount() - 1) {
//            outRect.left = space;
            outRect.left = 0;
            outRect.right = 0;
        } else {
            outRect.right = space;
            outRect.left = 0;
        }
    }
}
