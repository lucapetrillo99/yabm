package com.example.linkcontainer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CategoriesMenuAdapter extends RecyclerView.Adapter<CategoriesMenuAdapter.categoriesMenuViewHolder> {
    private final ArrayList<Category> categories;
    private final MainActivity mainActivity;

    public CategoriesMenuAdapter(ArrayList<Category> categories, MainActivity mainActivity) {
        this.categories = categories;
        this.mainActivity = mainActivity;
        Drawable drawable = ResourcesCompat.getDrawable(mainActivity.getResources(),
                R.drawable.ic_all_bookmarks, null);
        Category category = new Category();
        category.setCategoryTitle("Tutti i segnalibri");
        category.setCategoryImage(drawableToBitmap(drawable));
        categories.add(0, category);
    }

    static class categoriesMenuViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView categoryImage;

        public categoriesMenuViewHolder(@NonNull View itemView) {
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
        return new categoriesMenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull categoriesMenuViewHolder holder, int position) {
        holder.categoryImage.setImageBitmap(categories.get(position).getCategoryImage());
        holder.title.setText(categories.get(position).getCategoryTitle());

        holder.itemView.setOnClickListener(v ->
                mainActivity.filterByCategory(categories.get(position).getCategoryTitle()));
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


