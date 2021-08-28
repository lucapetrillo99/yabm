package com.ilpet.yabm.adapters;

import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
    private @ColorInt int color;
    private final Resources.Theme theme;

    public CategoriesMenuAdapter(ArrayList<Category> categories, MainActivity mainActivity, String startCategory) {
        this.categories = categories;
        this.mainActivity = mainActivity;
        this.startCategory = startCategory;
        TypedValue typedValue = new TypedValue();
        theme = mainActivity.getTheme();
        theme.resolveAttribute(R.attr.backgroundColor, typedValue, true);
        Category category = new Category();
        category.setCategoryTitle(mainActivity.getString(R.string.all_bookmarks_title));
        categories.add(0, category);
    }

    static class categoriesMenuViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        RelativeLayout relativeLayout;

        public categoriesMenuViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.category_setting_title);
            relativeLayout = itemView.findViewById(R.id.menu_item);
        }
    }

    @NonNull
    @Override
    public categoriesMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_menu_item, parent, false);
        TypedValue backgroundValue = new TypedValue();
        theme.resolveAttribute(R.color.light_black, backgroundValue, true);
        color = backgroundValue.data;
        return new categoriesMenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull categoriesMenuViewHolder holder, int position) {
        holder.title.setText(categories.get(position).getCategoryTitle());
        holder.relativeLayout.setBackgroundColor(color);

        if (selectedPosition == position) {
            holder.relativeLayout.setBackground(ResourcesCompat.getDrawable(mainActivity.getResources(),
                    R.drawable.category_background, null));
        }
        else {
            holder.relativeLayout.setBackgroundColor(color);
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
}


