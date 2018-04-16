package ptit.nttrung.filterimagesetwallpaper.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ptit.nttrung.filterimagesetwallpaper.R;
import ptit.nttrung.filterimagesetwallpaper.utils.OnMyItemClickListener;
import ptit.nttrung.filterimagesetwallpaper.utils.PInfo;

/**
 * Created by TrungNguyen on 2/27/2018.
 */

public class AppInfoAdapter extends RecyclerView.Adapter<AppInfoAdapter.MyViewHolder> {

    private List<PInfo> list;
    private Context context;
    private OnMyItemClickListener clickListener;

    public void setClickListener(OnMyItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public AppInfoAdapter(Context context, List<PInfo> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);

        return new AppInfoAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PInfo pInfo = list.get(position);
        holder.thumbnail.setImageDrawable(pInfo.iconApp);
        holder.appName.setText(pInfo.appName);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.content)
        LinearLayout content;
        @BindView(R.id.thumbnail)
        ImageView thumbnail;
        @BindView(R.id.app_name)
        TextView appName;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        clickListener.onItemClick(getAdapterPosition());
                    }
                }
            });
        }
    }
}
