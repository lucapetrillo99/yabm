package com.ilpet.yabm.adapters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ilpet.yabm.R;
import com.ilpet.yabm.activities.MainActivity;
import com.ilpet.yabm.classes.Category;

import java.util.ArrayList;

public class CategoriesMenuAdapter extends RecyclerView.Adapter<CategoriesMenuAdapter.categoriesMenuViewHolder> {
    private final ArrayList<Category> categories;
    private final MainActivity mainActivity;
    private final String startCategory;
    private int selectedPosition = -1;
    private int touches = 0;

    public CategoriesMenuAdapter(ArrayList<Category> categories, MainActivity mainActivity, String startCategory) {
        this.categories = categories;
        this.mainActivity = mainActivity;
        this.startCategory = startCategory;
        Drawable drawable = ResourcesCompat.getDrawable(mainActivity.getResources(),
                R.drawable.ic_all_bookmarks, null);
        Category category = new Category();
        category.setCategoryTitle(mainActivity.getString(R.string.all_bookmarks_title));
        category.setCategoryImage(drawableToBitmap(drawable));
        categories.add(0, category);
    }

    static class categoriesMenuViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView categoryImage;
        RelativeLayout relativeLayout;

        public categoriesMenuViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.category_setting_image);
            title = itemView.findViewById(R.id.category_setting_title);
            relativeLayout = itemView.findViewById(R.id.menu_item);

        }
    }

    @NonNull
    @Override
    public categoriesMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_menu_item, parent, false);
        return new categoriesMenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull categoriesMenuViewHolder holder, int position) {
        holder.categoryImage.setImageBitmap(categories.get(position).getCategoryImage());
        holder.title.setText(categories.get(position).getCategoryTitle());

        if(selectedPosition==position) {
            holder.relativeLayout.setBackground(ResourcesCompat.getDrawable(mainActivity.getResources(),
                    R.drawable.category_background, null));
        }
        else {
            holder.relativeLayout.setBackgroundColor(Color.parseColor("#303030"));
        }

        if (categories.get(position).getCategoryTitle().equals(mainActivity.getString(R.string.default_bookmarks))) {
            holder.categoryImage.setImageDrawable(ResourcesCompat.getDrawable(mainActivity.getResources(),
                    R.drawable.ic_default, null));

        } else if (categories.get(position).getCategoryTitle().equals(mainActivity.getString(R.string.archived_bookmarks))) {
            holder.categoryImage.setImageDrawable(ResourcesCompat.getDrawable(mainActivity.getResources(),
                    R.drawable.ic_archive, null));
        }

        holder.itemView.setOnClickListener(v -> {
            mainActivity.filterByCategory(categories.get(position).getCategoryTitle());
            if (categories.get(position).getCategoryTitle().equals(startCategory)) {
                holder.relativeLayout.setBackground(ResourcesCompat.getDrawable(mainActivity.getResources(),
                        R.drawable.category_background, null));
            }
            selectedPosition = position;
            touches ++;
            notifyDataSetChanged();
        });

        if (touches == 0) {
            if (categories.get(position).getCategoryTitle().equals(startCategory)) {
                holder.relativeLayout.setBackground(ResourcesCompat.getDrawable(mainActivity.getResources(),
                        R.drawable.category_background, null));
            }
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}


