package com.example.linkcontainer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import static android.view.View.INVISIBLE;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.categoriesViewHolder>
        implements Filterable {
    private final ArrayList<Category> categories;
    private DatabaseHandler db;
    private final Categories categoriesActivity;
    private final ArrayList<Category> allCategories;
    public static final int PERMISSION_REQUEST_STORAGE = 1000;
    public ImageView categoryImage;
    public ImageButton addImageButton;
    public TextView addImageTitle;
    public Bitmap image;

    public CategoriesAdapter(ArrayList<Category> categories, Categories categoriesActivity) {
        this.categories = categories;
        this.categoriesActivity = categoriesActivity;
        this.allCategories = new ArrayList<>(categories);
    }

    static class categoriesViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton modify, delete;
        CheckBox checkbox;
        View view;

        public categoriesViewHolder(@NonNull View itemView, Categories categoriesActivity) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            modify = itemView.findViewById(R.id.modify);
            delete = itemView.findViewById(R.id.delete);
            checkbox = itemView.findViewById(R.id.checkbox);
            view = itemView;
            view.setOnLongClickListener(categoriesActivity);
        }
    }

    @NonNull
    @Override
    public categoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_list, parent, false);
        return new categoriesViewHolder(view, categoriesActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull categoriesViewHolder holder, int position){

        db = DatabaseHandler.getInstance(categoriesActivity);

        holder.title.setText(categories.get(position).getCategoryTitle());

        if (categoriesActivity.isContextualMenuEnable) {
            if (categories.get(position).getCategoryTitle().equals("Default")) {
                holder.checkbox.setVisibility(View.INVISIBLE);
            }else {
                holder.checkbox.setVisibility(View.VISIBLE);
            }
            holder.modify.setVisibility(View.INVISIBLE);
            holder.delete.setVisibility(INVISIBLE);

        } else {
            holder.checkbox.setVisibility(View.INVISIBLE);
            if (categories.get(position).getCategoryTitle().equals("Default")) {
                holder.modify.setVisibility(INVISIBLE);
                holder.delete.setVisibility(INVISIBLE);
            } else {
                holder.modify.setVisibility(View.VISIBLE);
                holder.delete.setVisibility(View.VISIBLE);
            }
        }

        holder.modify.setOnClickListener(v -> createDialog(position, true, v));

        holder.delete.setOnClickListener(v -> confirmDialog(position, v));

        holder.checkbox.setOnClickListener(v -> categoriesActivity.makeSelection(v, position));

        if (categoriesActivity.areAllSelected) {
            for (int i = 0; i < categories.size(); i++) {
                holder.checkbox.setChecked(true);
            }
        } else {
            for (int i = 0; i < categories.size(); i++) {
                holder.checkbox.setChecked(false);
            }
        }
    }

    public void updateCategories(ArrayList<Category> selectedCategories) {
        UpdateCategories updateCategories = new UpdateCategories(selectedCategories);
        updateCategories.execute();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void createDialog(int position, boolean isModify, View v) {
        LayoutInflater layoutInflater = LayoutInflater.from(v.getRootView().getContext());
        View dialogView = layoutInflater.inflate(R.layout.dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(categoriesActivity)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton("Annulla", null)
                .setCancelable(false)
                .create();

        EditText input = dialogView.findViewById(R.id.user_input);
        TextView title = dialogView.findViewById(R.id.title);
        addImageTitle = dialogView.findViewById(R.id.add_image_title);
        addImageButton = dialogView.findViewById(R.id.add_image_button);
        categoryImage = dialogView.findViewById(R.id.category_image);

        if (isModify) {
            title.setText("Modifica categoria");
        } else {
            title.setText("Nuova categoria");
        }

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if (isModify) {
            input.setText(categories.get(position).getCategoryTitle());
            Log.i("OUSHDOH", String.valueOf(categories.get(position).getCategoryImage().getClass()));

            if (categories.get(position).getCategoryImage() != null) {
                addImageTitle.setVisibility(View.GONE);
                addImageButton.setVisibility(View.GONE);
                categoryImage.setVisibility(View.VISIBLE);
                categoryImage.setImageBitmap(categories.get(position).getCategoryImage());
            }
        } else {
            input.setHint("Inserisci la categoria");
        }

        addImageButton.setOnClickListener(v1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && categoriesActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                categoriesActivity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
            } else {
                categoriesActivity.getImageFromDevice();
            }
        });

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                if (isModify) {
                    Category category = new Category();
                    category.setCategoryTitle(input.getText().toString());
                    category.setCategoryImage(image);
                    if (!categories.get(position).getCategoryTitle().equals(category.getCategoryTitle())) {
                        boolean result = db.updateCategory(category);
                        if (result) {
                            Toast.makeText(v.getRootView().getContext(),
                                    "Categoria modificate correttamente!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            categories.set(position, category);
                            notifyItemChanged(position);
                        } else {
                            Toast.makeText(v.getRootView().getContext(),
                                    "Categoria già esistente!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        dialog.dismiss();
                    }
                } else {
                    if (!input.getText().toString().isEmpty()) {
                        Category category = new Category();
                        category.setCategoryTitle(input.getText().toString());
                        category.setCategoryImage(image);
                        boolean result = db.addCategory(category);
                        if (result) {
                            dialog.dismiss();
                            categories.add(category);
                            notifyItemInserted(getItemCount());
                        } else {
                            Toast.makeText(v.getRootView().getContext(),
                                    "Categoria già esistente!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(v.getRootView().getContext(),
                                "Inserisci il nome di una categoria!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        });
        dialog.show();
    }

    private class UpdateCategories extends AsyncTask<Void, Void, Void> {
        private final ArrayList<Category> list;

        public UpdateCategories(ArrayList<Category> categories) {
            this.list = categories;
        }

        boolean result = true;
        @Override
        protected Void doInBackground(Void... voids) {

            for (Category selectedCategory : list) {
                categories.remove(selectedCategory);
                result = db.deleteCategory(selectedCategory);
                if (!result) {
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!result) {
                Toast.makeText(categoriesActivity, "Impossibile eliminare i segnalibri!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void confirmDialog(int position, View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());

        builder.setMessage("Sei sicuro di voler eliminare la categoia?\nTutti i segnalibri " +
                "verranno eliminati")
                .setCancelable(false)
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton("Sì", (dialogInterface, i) -> {
                    Category category = new Category();
                    category.setCategoryTitle(categories.get(position).getCategoryTitle());
                    categories.remove(position);
                    notifyItemRemoved(position);
                    boolean result = db.deleteCategory(category);

                    if (!result) {
                        Toast.makeText(v.getRootView().getContext(), "Impossibile eliminare la categoria", Toast.LENGTH_LONG).show();
                        categories.add(position, category);
                        notifyItemInserted(position);
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter(){
        @Override
        protected Filter.FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Category> filteredCategories = new ArrayList<>();

            if (constraint.toString().isEmpty()) {
                filteredCategories.addAll(allCategories);
            } else {
                for (Category category: allCategories) {
                    if (category.getCategoryTitle().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredCategories.add(category);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredCategories;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            categories.clear();
            categories.addAll((Collection<? extends Category>) results.values);
            notifyDataSetChanged();
        }
    };
}