package com.ilpet.yabm.adapters;

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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.ilpet.yabm.R;
import com.ilpet.yabm.activities.InsertBookmarkActivity;
import com.ilpet.yabm.activities.MainActivity;
import com.ilpet.yabm.classes.Bookmark;
import com.ilpet.yabm.utils.DatabaseHandler;
import com.ilpet.yabm.utils.ImagePreview;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.MyViewHolder> implements Filterable {
    private static final int DESCRIPTION_MAX_LENGTH = 120;
    private final ArrayList<Bookmark> bookmarks;
    private final MainActivity mainActivity;
    private DatabaseHandler db;
    private final ArrayList<Bookmark> allBookmarks;

    public BookmarksAdapter(ArrayList<Bookmark> bookmarkList, MainActivity mainActivity) {
        this.bookmarks = bookmarkList;
        this.mainActivity = mainActivity;
        this.allBookmarks = new ArrayList<>(bookmarks);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView link, title, description;
        ImageView image, options;
        ImageButton shareButton;
        View view;
        CheckBox checkbox;
        RelativeLayout relativeLayout;

        public MyViewHolder(final View itemView, MainActivity mainActivity) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            link = itemView.findViewById(R.id.link);
            description = itemView.findViewById(R.id.description);
            image = itemView.findViewById(R.id.image);
            options = itemView.findViewById(R.id.bookmark_options);
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

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String description = bookmarks.get(position).getDescription();
        holder.title.setText(bookmarks.get(position).getTitle());
        holder.link.setText(bookmarks.get(position).getLink());

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
        }

        holder.options.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(mainActivity, holder.options);
            popup.getMenuInflater().inflate(R.menu.bookmark_menu_options, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                switch (id) {
                    case R.id.modify_bookmark:
                        String category = db.getCategoryById(bookmarks.get(position).getCategory());
                        Intent intent = new Intent(mainActivity, InsertBookmarkActivity.class);
                        intent.putExtra("bookmark", bookmarks.get(position));
                        intent.putExtra("category", category);
                        mainActivity.startActivity(intent);
                        mainActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        break;
                    case R.id.delete_bookmark:
                        mainActivity.confirmDialog(bookmarks.get(position).getId(), position);
                        break;
                }
                return true;
            });
            popup.show();
            });

        if (description != null && bookmarks.get(position).getImage() == null) {
            holder.image.setVisibility(View.GONE);
            RelativeLayout.LayoutParams descriptionLayoutParams = (RelativeLayout.LayoutParams) holder.description.getLayoutParams();
            RelativeLayout.LayoutParams titleLayoutParams = (RelativeLayout.LayoutParams) holder.title.getLayoutParams();
            descriptionLayoutParams.height = 200;
            descriptionLayoutParams.width = 750;
            titleLayoutParams.width = 1000;
            holder.description.setLayoutParams(descriptionLayoutParams);
        } else if (description == null && bookmarks.get(position).getImage() == null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) holder.relativeLayout.getLayoutParams();
            layoutParams.height = 250;
            holder.relativeLayout.setLayoutParams(layoutParams);
            RelativeLayout.LayoutParams checkboxLayout = (RelativeLayout.LayoutParams) holder.checkbox.getLayoutParams();
            checkboxLayout.setMargins(0, 25, 10, 0);
            holder.checkbox.setLayoutParams(checkboxLayout);
        }

        holder.title.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(bookmarks.get(position).getLink()));
            Intent chooser = Intent.createChooser(intent, "Apri con");
            mainActivity.startActivity(chooser);
        });

        holder.title.setOnLongClickListener(v -> {
            showDialog(position);
            return false;
        });

        holder.shareButton.setOnClickListener(arg0 -> {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, bookmarks.get(position).getLink());
            shareIntent.setType("text/plain");
            mainActivity.startActivity(shareIntent);
            if (mainActivity.isContextualMenuEnable) {
                mainActivity.removeContextualActionMode();
            }
        });

        holder.checkbox.setOnClickListener(v -> mainActivity.makeSelection(v, position));

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
        UpdateBookmarks updateBookmarks = new UpdateBookmarks(selectedBookmarks, operation, this);
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

    private static class UpdateBookmarks extends AsyncTask<Void, Void, Void> {
        private final int operation;
        private final ArrayList<Bookmark> list;
        private final WeakReference<BookmarksAdapter> activityReference;

        public UpdateBookmarks(ArrayList<Bookmark> bookmarks, int operation, BookmarksAdapter context) {
            this.list = bookmarks;
            this.operation = operation;
            activityReference = new WeakReference<>(context);
        }

        boolean result = true;

        @Override
        protected Void doInBackground(Void... voids) {
            BookmarksAdapter bookmarksAdapter = activityReference.get();
            switch (operation) {
                case 1:
                    for (Bookmark selectedBookmark : list) {
                        bookmarksAdapter.bookmarks.remove(selectedBookmark);
                        result = bookmarksAdapter.db.deleteBookmark(selectedBookmark.getId());
                        if (!result) {
                            break;
                        }
                    }
                    break;
                case 2:
                    for (Bookmark selectedBookmark : list) {
                        bookmarksAdapter.bookmarks.remove(selectedBookmark);
                        result = bookmarksAdapter.db.addToArchive(selectedBookmark.getId(), selectedBookmark.getCategory());
                        if (!result) {
                            break;
                        }
                    }
                    break;
                case 3:
                    for (Bookmark selectedBookmark : list) {
                        bookmarksAdapter.bookmarks.remove(selectedBookmark);
                        result = bookmarksAdapter.db.removeFromArchive(selectedBookmark.getId());
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
            BookmarksAdapter bookmarksAdapter = activityReference.get();
            if (!result) {
                Toast.makeText(bookmarksAdapter.mainActivity, "Impossibile eliminare i segnalibri!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected Filter.FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Bookmark> filteredBookmarks = new ArrayList<>();

            if (constraint.toString().isEmpty()) {
                filteredBookmarks.addAll(allBookmarks);
            } else {
                for (Bookmark bookmark : allBookmarks) {
                    if (bookmark.getTitle() == null) {
                        if (bookmark.getLink().toLowerCase().contains(constraint.toString().toLowerCase())
                                || bookmark.getDescription().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredBookmarks.add(bookmark);
                        }
                    } else if (bookmark.getDescription() == null) {
                        if (bookmark.getLink().toLowerCase().contains(constraint.toString().toLowerCase())
                                || bookmark.getTitle().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredBookmarks.add(bookmark);
                        }
                    } else {
                        if (bookmark.getLink().toLowerCase().contains(constraint.toString().toLowerCase())
                                || bookmark.getTitle().toLowerCase().contains(constraint.toString().toLowerCase())
                                || bookmark.getDescription().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredBookmarks.add(bookmark);
                        }
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredBookmarks;

            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            bookmarks.clear();
            bookmarks.addAll((ArrayList<Bookmark>) results.values);
            notifyDataSetChanged();
        }
    };

    public void removeBookmark(int position) {
        allBookmarks.remove(position);
    }

    public void showDialog(int position) {
        final Dialog dialog = new Dialog(mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.link_actions_dialog);

        TextView openLink = dialog.findViewById(R.id.open_link);
        TextView copyLink = dialog.findViewById(R.id.copy_link);

        openLink.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(bookmarks.get(position).getLink()));
            mainActivity.startActivity(i);
            dialog.dismiss();
        });

        copyLink.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) mainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("link", bookmarks.get(position).getLink());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(mainActivity.getApplicationContext(), "Link copiato negli appunti", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.ActivityAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

}