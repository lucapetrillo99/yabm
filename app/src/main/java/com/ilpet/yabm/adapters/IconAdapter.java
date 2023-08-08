package com.ilpet.yabm.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ilpet.yabm.R;
import com.ilpet.yabm.activities.CategoriesActivity;
import com.maltaisn.icondialog.data.Icon;
import com.maltaisn.icondialog.pack.IconPack;
import com.maltaisn.icondialog.pack.IconPackLoader;
import com.maltaisn.iconpack.defaultpack.IconPackDefault;

import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.ViewHolder> {
    private final List<Icon> iconList;
    private OnItemClickListener clickListener;
    private final Activity activity;

    public interface OnItemClickListener {
        void onItemClick(Drawable icon);
    }

    public IconAdapter(Activity activity) {
        this.activity = activity;
        this.iconList = loadIconPack();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.icon_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Drawable iconDrawable = iconList.get(position).getDrawable();
        holder.iconImageView.setImageDrawable(iconDrawable);
    }

    @Override
    public int getItemCount() {
        return iconList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.icon_image);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Drawable icon = iconList.get(position).getDrawable();
                        clickListener.onItemClick(icon);
                    }
                }
            });
        }
    }

    @NonNull
    private List<Icon> loadIconPack() {
        IconPackLoader loader = new IconPackLoader(activity);

        IconPack iconPack = IconPackDefault.createDefaultIconPack(loader);
        iconPack.loadDrawables(loader.getDrawableLoader());

        return iconPack.getAllIcons();
    }
}
