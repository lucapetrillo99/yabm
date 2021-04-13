package com.example.linkcontainer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collection;

import static android.view.View.INVISIBLE;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.categoriesViewHolder>
        implements Filterable {
    private ArrayList<String> categories;
    private DatabaseHandler db;
    private Categories categoriesActivity;
    private ArrayList<String> allCategories;

    public CategoriesAdapter(ArrayList<String> categories, Categories categoriesActivity) {
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

        holder.title.setText(categories.get(position));

        if (categoriesActivity.isContextualMenuEnable) {
            if (categories.get(position).equals("Default")) {
                holder.checkbox.setVisibility(View.INVISIBLE);
            }else {
                holder.checkbox.setVisibility(View.VISIBLE);
            }
            holder.modify.setVisibility(View.INVISIBLE);
            holder.delete.setVisibility(INVISIBLE);

        } else {
            holder.checkbox.setVisibility(View.INVISIBLE);
            if (categories.get(position).equals("Default")) {
                holder.modify.setVisibility(INVISIBLE);
                holder.delete.setVisibility(INVISIBLE);
            } else {
                holder.modify.setVisibility(View.VISIBLE);
                holder.delete.setVisibility(View.VISIBLE);
            }
        }

        holder.modify.setOnClickListener(v -> createDialog(position, true, v));

        holder.delete.setOnClickListener(v -> confirmDialog(position, v));

        holder.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoriesActivity.makeSelection(v, position);
            }
        });

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

    public void updateCategories(ArrayList<String> selectedCategories) {
        UpdateCategories updateCategories = new UpdateCategories(selectedCategories);
        updateCategories.execute();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (categories == null) {
            return 0;
        } else {
            return categories.size();
        }
    }

    @SuppressLint("SetTextI18n")
    public void createDialog(int position, boolean isModify, View v) {
        LayoutInflater layoutInflater = LayoutInflater.from(v.getRootView().getContext());
        View dialogView = layoutInflater.inflate(R.layout.dialog, null);
        AlertDialog.Builder alertbox = new AlertDialog.Builder(v.getRootView().getContext());
        alertbox.setView(dialogView);
        final EditText input = dialogView.findViewById(R.id.user_input);
        TextView title = dialogView.findViewById(R.id.title);
        if (isModify) {
           title.setText("Modifica categoria");
        } else {
            title.setText("Nuova categoria");
        }

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if (isModify) {
            input.setText(categories.get(position));
        } else {
            input.setHint("Inserisci la categoria");
        }

        alertbox.setPositiveButton("OK", (arg0, arg1) -> {
            if (isModify) {
                String category = input.getText().toString();
                if (!categories.get(position).equals(category)) {

                    String id = db.getCategoryId(categories.get(position));
                    boolean result = db.updateCategory(category, id);
                    if (result) {
                        Toast.makeText(v.getRootView().getContext(),
                                "Categoria modificate correttamente!", Toast.LENGTH_LONG).show();
                        categories.set(position, category);
                        notifyItemChanged(position);
                    } else {
                        Toast.makeText(v.getRootView().getContext(),
                                "Categoria già esistente!", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                boolean result = db.addCategory(input.getText().toString());
                if (result) {
                    categories.add(input.getText().toString());
                    notifyItemInserted(getItemCount());
                } else {
                    Toast.makeText(v.getRootView().getContext(),
                            "Categoria già esistente!", Toast.LENGTH_LONG).show();
                }
            }
        });
        alertbox.setNegativeButton("Annulla", (arg0, arg1) -> { });
        alertbox.show();
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateCategories extends AsyncTask<Void, Void, Void> {
        private final ArrayList<String> list;

        public UpdateCategories(ArrayList<String> categories) {
            this.list = categories;
        }

        boolean result = true;
        @Override
        protected Void doInBackground(Void... voids) {

            for (String selectedCategory : list) {
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
                    String category = categories.get(position);
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
            ArrayList<String> filteredCategories = new ArrayList<>();
            Log.i("AHIDAH", allCategories.toString());

            if (constraint.toString().isEmpty()) {
                filteredCategories.addAll(allCategories);
            } else {
                for (String bookmark: allCategories) {
                    if (bookmark.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredCategories.add(bookmark);
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
            categories.addAll((Collection<? extends String>) results.values);
            notifyDataSetChanged();
        }
    };
}