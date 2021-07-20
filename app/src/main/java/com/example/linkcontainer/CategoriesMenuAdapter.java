package com.example.linkcontainer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CategoriesMenuAdapter extends RecyclerView.Adapter<CategoriesMenuAdapter.categoriesMenuViewHolder> {
    private ArrayList<Category> categories;
    private MainActivity mainActivity;

    public CategoriesMenuAdapter(ArrayList<Category> categories, MainActivity mainActivity) {
        this.categories = categories;
        this.mainActivity = mainActivity;
        Bitmap icon = BitmapFactory.decodeResource(mainActivity.getResources(),
                R.drawable.ic_all_bookmarks);
        Category category = new Category(null, "Tutti i segnalibri", icon);

    }

    static class categoriesMenuViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView categoryImage;

        public categoriesMenuViewHolder(@NonNull View itemView, MainActivity mainActivity) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.category_setting_image);
            title = itemView.findViewById(R.id.category_setting_title);
        }
    }

    @NonNull
    @Override
    public categoriesMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_menu_item, parent, false);
        return new categoriesMenuViewHolder(view, mainActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull categoriesMenuViewHolder holder, int position) {
        holder.categoryImage.setImageBitmap(categories.get(position).getCategoryImage());
        holder.title.setText(categories.get(position).getCategoryTitle());
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
