package com.example.linkcontainer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> implements Filterable {

    private static final int DESCRIPTION_MAX_LENGTH = 80;
    private ArrayList<Bookmark> bookmarks;
    private MainActivity mainActivity;
    private DatabaseHandler db;
    private ArrayList<Bookmark> allBookmarks;

    public RecyclerAdapter(ArrayList<Bookmark> bookmarkList, MainActivity mainActivity) {
        this.bookmarks = bookmarkList;
        this.mainActivity = mainActivity;
        this.allBookmarks = new ArrayList<>(bookmarks);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
      TextView link, title, description;
      ImageView image;
      ImageButton shareButton;
      View view;
      CheckBox checkbox;

        public MyViewHolder(final View itemView, MainActivity mainActivity) {
            super(itemView);
            link = itemView.findViewById(R.id.url);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            image = itemView.findViewById(R.id.image);
            checkbox = itemView.findViewById(R.id.checkbox);
            shareButton = itemView.findViewById(R.id.share);
            view = itemView;
            view.setOnLongClickListener(mainActivity);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_list, parent, false);
        db = DatabaseHandler.getInstance(mainActivity);
        return new MyViewHolder(itemView, mainActivity);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String description = bookmarks.get(position).getDescription();
        String text = "<a href=" + bookmarks.get(position).getLink() + ">" + bookmarks.get(position).getLink()  +" </a>";
        holder.link.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
        holder.title.setText(bookmarks.get(position).getTitle());

        if (mainActivity.isContextualMenuEnable) {
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.image.setVisibility(View.INVISIBLE);
        } else {
            holder.checkbox.setVisibility(View.INVISIBLE);
            holder.image.setVisibility(View.VISIBLE);
        }

        if (description != null) {
            if (description.length() > DESCRIPTION_MAX_LENGTH) {
                description = description.substring(0, DESCRIPTION_MAX_LENGTH) + "...";
            }
            holder.description.setText(description);
        } else {
            holder.description.setText("");
        }

        Picasso.get().load(bookmarks.get(position).getImage())
                .fit()
                .centerCrop()
                .into(holder.image);

        holder.link.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(bookmarks.get(position).getLink()));
            mainActivity.startActivity(i);
        });

        holder.image.setOnClickListener(v -> {
                final Dialog nagDialog = new Dialog(mainActivity);
                nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                nagDialog.setCancelable(true);
                nagDialog.setContentView(R.layout.preview_image);
                ImageView ivPreview = nagDialog.findViewById(R.id.preview_image);
                ImageButton closeButton = nagDialog.findViewById(R.id.close_button);
                Picasso.get().load(bookmarks.get(position).getImage())
                        .fit()
                        .centerInside()
                        .into(ivPreview);

            closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        nagDialog.dismiss();
                    }
                });
                nagDialog.show();
                nagDialog.getWindow().setLayout(1000, 1200);
            });

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, bookmarks.get(position).getLink());
                shareIntent.setType("text/plain");
                mainActivity.startActivity(shareIntent);
            }
        });

        if (!mainActivity.isArchiveModeEnabled) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String category = db.getCategoryById(bookmarks.get(position).getCategory());
                    Intent intent = new Intent(mainActivity, InsertLink.class);
                    intent.putExtra("bookmark", bookmarks.get(position));
                    intent.putExtra("category", category);
                    mainActivity.startActivity(intent);
                }
            });
        }


        holder.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.makeSelection(v, position);
            }
        });

        if (mainActivity.areAllSelected) {
            for (int i = 0; i < bookmarks.size(); i++) {
                holder.checkbox.setChecked(true);
            }
        } else {
            for (int i = 0; i < bookmarks.size(); i++) {
                holder.checkbox.setChecked(false);
            }
        }
    }

    public void updateBookmarks(ArrayList<Bookmark> selectedBookmarks, int operation) {
       UpdateBookmarks updateBookmarks = new UpdateBookmarks(selectedBookmarks, operation);
       updateBookmarks.execute();
       notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (bookmarks == null) {
            return 0;
        } else {
            return bookmarks.size();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateBookmarks extends AsyncTask<Void, Void, Void> {
        private final int operation;
        private final ArrayList<Bookmark> list;

        public UpdateBookmarks(ArrayList<Bookmark> bookmarks, int operation) {
            this.list = bookmarks;
            this.operation = operation;
        }


        boolean result = true;
        @Override
        protected Void doInBackground(Void... voids) {
            if (operation == 1) {
                for (Bookmark selectedBookmark : list) {
                    bookmarks.remove(selectedBookmark);
                    result = db.deleteBookmark(selectedBookmark.id);
                    if (!result) {
                        break;
                    }
                }
            } else {
                for (Bookmark selectedBookmark : list) {
                    bookmarks.remove(selectedBookmark);
                    result = db.addToArchive(selectedBookmark.id);
                    if (!result) {
                        break;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!result) {
                Toast.makeText(mainActivity, "Impossibile eliminare i segnalibri!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter(){
        @Override
        protected Filter.FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Bookmark> filteredBookmarks = new ArrayList<>();

            if (constraint.toString().isEmpty()) {
                filteredBookmarks.addAll(allBookmarks);
            } else {
                for (Bookmark bookmark: allBookmarks) {
                    if (bookmark.getLink().toLowerCase().contains(constraint.toString().toLowerCase())
                            || bookmark.getTitle().toLowerCase().contains(constraint.toString().toLowerCase())
                            || bookmark.getDescription().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredBookmarks.add(bookmark);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredBookmarks;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            bookmarks.clear();
            bookmarks.addAll((Collection<? extends Bookmark>) results.values);
            notifyDataSetChanged();
        }
    };

}
