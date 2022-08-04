package com.ilpet.yabm.utils;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.ilpet.yabm.R;
import com.ilpet.yabm.classes.Bookmark;
import com.ilpet.yabm.classes.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CategoriesSelectionDialog extends AppCompatDialogFragment {

    private final Activity activity;
    private final String categoryName;
    private final ArrayList<Bookmark> selectedBookmarks;
    private final CategoriesSelectionListener categoriesSelectionListener;

    public CategoriesSelectionDialog(Activity activity, String categoryName,
                                     ArrayList<Bookmark> selectedBookmarks,
                                     CategoriesSelectionListener categoriesSelectionListener) {
        this.activity = activity;
        this.categoryName = categoryName;
        this.selectedBookmarks = selectedBookmarks;
        this.categoriesSelectionListener = categoriesSelectionListener;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.category_selection);
        DatabaseHandler db = DatabaseHandler.getInstance(activity);
        ArrayList<Category> categories = db.getAllCategories(null, null);
        List<String> categoriesToString = categories.stream()
                .map(Category::getCategoryTitle)
                .filter(s -> !s.equals(getString(R.string.archived_bookmarks)))
                .collect(Collectors.toList());

        categoriesToString.remove(categoryName);
        builder.setItems(categoriesToString.toArray(new String[0]), (dialog, item) -> {
            boolean result = db.updateBookmarkCategory(selectedBookmarks, categoriesToString.get(item));
            categoriesSelectionListener.getResult(result);
        });

        return builder.create();
    }
    
    public interface CategoriesSelectionListener {
        void getResult(boolean result);
    }
}
