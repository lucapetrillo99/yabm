package com.ilpet.yabm.adapters;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ilpet.yabm.R;
import com.ilpet.yabm.activities.MainActivity;
import com.ilpet.yabm.classes.Category;
import com.ilpet.yabm.utils.dialogs.PasswordDialog;

import java.util.ArrayList;

public class CategoriesMenuAdapter extends RecyclerView.Adapter<CategoriesMenuAdapter.categoriesMenuViewHolder> {
    private final ArrayList<Category> categories;
    private final MainActivity mainActivity;
    private final String startCategory;
    private int selectedPosition = -1;
    private int touches = 0;
    private @ColorInt int color;
    private final Resources.Theme theme;
    private boolean unlock = false;

    @SuppressLint("UseCompatLoadingForDrawables")
    public CategoriesMenuAdapter(ArrayList<Category> categories, MainActivity mainActivity, String startCategory) {
        this.categories = categories;
        this.mainActivity = mainActivity;
        this.startCategory = startCategory;
        TypedValue typedValue = new TypedValue();
        theme = mainActivity.getTheme();
        theme.resolveAttribute(R.attr.backgroundColor, typedValue, true);
        Category category = new Category();
        category.setCategoryTitle(mainActivity.getString(R.string.all_bookmarks_title));
        category.setCategoryImage(mainActivity.getDrawable(R.drawable.ic_all));
        categories.add(0, category);
    }

    static class categoriesMenuViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        RelativeLayout relativeLayout;
        ImageView lockedCategory, categoryImage;

        public categoriesMenuViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.category_setting_title);
            relativeLayout = itemView.findViewById(R.id.menu_item);
            lockedCategory = itemView.findViewById(R.id.lock);
            categoryImage = itemView.findViewById(R.id.category_image);
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
        holder.title.setText(categories.get(holder.getAbsoluteAdapterPosition()).getCategoryTitle());
        holder.relativeLayout.setBackgroundColor(color);
        holder.categoryImage.setImageDrawable(categories.get(holder.getAbsoluteAdapterPosition()).getCategoryImage());


        if (!categories.get(holder.getAbsoluteAdapterPosition()).getCategoryTitle().equals(
                mainActivity.getString(R.string.all_bookmarks_title))) {
            if (categories.get(holder.getAbsoluteAdapterPosition()).getPasswordProtection().equals(
                    Category.CategoryProtection.LOCK)) {
                holder.lockedCategory.setImageResource(R.drawable.ic_lock);
            } else {
                holder.lockedCategory.setImageResource(0);
            }
        }

        if (selectedPosition == holder.getAbsoluteAdapterPosition()) {
            holder.relativeLayout.setBackground(ResourcesCompat.getDrawable(mainActivity.getResources(),
                    R.drawable.category_background, null));
            if (unlock) {
                holder.lockedCategory.setImageResource(R.drawable.ic_unlock);
            }
        } else {
            holder.relativeLayout.setBackgroundColor(color);
        }

        holder.itemView.setOnClickListener(v -> {
            unlock = false;
            if (categories.get(holder.getAbsoluteAdapterPosition()).getCategoryTitle().
                    equals(mainActivity.getString(R.string.all_bookmarks_title))) {
                loadFilteredBookmarks(holder);
            } else {
                if (!(selectedPosition == holder.getAbsoluteAdapterPosition())) {
                    if (categories.get(holder.getAbsoluteAdapterPosition()).getPasswordProtection().
                            equals(Category.CategoryProtection.LOCK)) {
                        PasswordDialog passwordDialog = new PasswordDialog(mainActivity,
                                result -> {
                                    if (result) {
                                        loadFilteredBookmarks(holder);
                                    }
                                    unlock = result;
                                });
                        passwordDialog.show(mainActivity.getSupportFragmentManager(),
                                "Password dialog");
                    } else {
                        loadFilteredBookmarks(holder);
                    }
                }
            }
        });

        if (touches == 0) {
            if (categories.get(holder.getAbsoluteAdapterPosition()).getCategoryTitle().
                    equals(startCategory)) {
                holder.relativeLayout.setBackground(ResourcesCompat.getDrawable(mainActivity.
                        getResources(), R.drawable.category_background, null));
            }
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void loadFilteredBookmarks(categoriesMenuViewHolder holder) {
        mainActivity.filterByCategory(categories.get(
                holder.getAbsoluteAdapterPosition()).getCategoryTitle());
        if (categories.get(holder.getAbsoluteAdapterPosition()).
                getCategoryTitle().equals(startCategory)) {
            holder.relativeLayout.setBackground(ResourcesCompat.
                    getDrawable(mainActivity.getResources(),
                            R.drawable.category_background, null));
        }
        selectedPosition = holder.getAbsoluteAdapterPosition();
        touches++;
        notifyDataSetChanged();
    }
}
