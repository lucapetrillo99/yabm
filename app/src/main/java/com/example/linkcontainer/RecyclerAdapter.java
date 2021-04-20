package com.example.linkcontainer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> implements Filterable {

    private static final int DESCRIPTION_MAX_LENGTH = 120;
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
      RelativeLayout relativeLayout;

        public MyViewHolder(final View itemView, MainActivity mainActivity) {
            super(itemView);
            link = itemView.findViewById(R.id.url);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            image = itemView.findViewById(R.id.image);
            checkbox = itemView.findViewById(R.id.checkbox);
            shareButton = itemView.findViewById(R.id.share);
            relativeLayout = itemView.findViewById(R.id.relative_layout);
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
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) holder.relativeLayout.getLayoutParams();
            layoutParams.height = 250;
            holder.relativeLayout.setLayoutParams(layoutParams);
            RelativeLayout.LayoutParams checkboxLayout = (RelativeLayout.LayoutParams) holder.checkbox.getLayoutParams();
            checkboxLayout.setMargins(0, 25, 10, 0);
            holder.checkbox.setLayoutParams(checkboxLayout);
        }

        if (bookmarks.get(position).getImage() != null) {
            Picasso.get().load(bookmarks.get(position).getImage())
                    .fit()
                    .centerCrop()
                    .into(holder.image);

            holder.image.setOnClickListener(v -> {
                ImagePreview imagePreview = ImagePreview.newInstance(bookmarks.get(position)
                        .getImage(), bookmarks.get(position).getTitle());
                imagePreview.show(mainActivity.getSupportFragmentManager(), "tag");
            });

        } else {
            holder.image.setVisibility(View.GONE);
            RelativeLayout.LayoutParams descriptionLayoutParams = (RelativeLayout.LayoutParams) holder.description.getLayoutParams();
            RelativeLayout.LayoutParams titleLayoutParams = (RelativeLayout.LayoutParams) holder.title.getLayoutParams();
            descriptionLayoutParams.height = 200;
            descriptionLayoutParams.width = 1000;
            titleLayoutParams.width = 1000;
            holder.description.setLayoutParams(descriptionLayoutParams);
        }

        holder.link.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(bookmarks.get(position).getLink()));
            mainActivity.startActivity(i);
        });

        holder.link.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDialog(position);
                return false;
            }
        });

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, bookmarks.get(position).getLink());
                shareIntent.setType("text/plain");
                mainActivity.startActivity(shareIntent);
                if (mainActivity.isContextualMenuEnable) {
                    mainActivity.removeContextualActionMode();
                }
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
                    if (mainActivity.isContextualMenuEnable) {
                        mainActivity.removeContextualActionMode();
                    }
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

            switch (operation) {
                case 1:
                    for (Bookmark selectedBookmark : list) {
                        bookmarks.remove(selectedBookmark);
                        result = db.deleteBookmark(selectedBookmark.id);
                        if (!result) {
                            break;
                        }
                    }
                    break;
                case 2:
                    for (Bookmark selectedBookmark : list) {
                        bookmarks.remove(selectedBookmark);
                        result = db.addToArchive(selectedBookmark.getId(), selectedBookmark.getCategory());
                        if (!result) {
                            break;
                        }
                    }
                    break;
                case 3:
                    for (Bookmark selectedBookmark : list) {
                        bookmarks.remove(selectedBookmark);
                        result = db.removeFromArchive(selectedBookmark.getId());
                        if (!result) {
                            break;
                        }
                    }
                    break;
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

    public void showDialog(int position) {
        final Dialog dialog = new Dialog(mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet);

        TextView openLink = dialog.findViewById(R.id.open_link);
        TextView copyLink = dialog.findViewById(R.id.copy_link);

        openLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(bookmarks.get(position).getLink()));
                mainActivity.startActivity(i);
                dialog.dismiss();
            }
        });

        copyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) mainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("link", bookmarks.get(position).getLink());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mainActivity.getApplicationContext(), "Link copiato negli appunti", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

}
