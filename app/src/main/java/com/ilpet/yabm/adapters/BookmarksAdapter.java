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
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
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

import java.util.ArrayList;

public class BookmarksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private static final int DELETE_OPTION = 1;
    private static final int ARCHIVE_OPTION = 2;
    private static final int UNARCHIVE_OPTION = 3;
    private static final int DESCRIPTION_MAX_LENGTH = 120;
    private static final int SIMPLE = 0;
    private static final int NO_DESCRIPTION = 1;
    private static final int NO_IMAGE = 2;
    private static final int NORMAL = 3;
    private final ArrayList<Bookmark> bookmarks;
    private final MainActivity mainActivity;
    private final ArrayList<Bookmark> allBookmarks;
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
    private DatabaseHandler db;

    public BookmarksAdapter(ArrayList<Bookmark> bookmarkList, MainActivity mainActivity) {
        this.bookmarks = bookmarkList;
        this.mainActivity = mainActivity;
        this.allBookmarks = new ArrayList<>(bookmarks);
    }

    @Override
    public int getItemViewType(int position) {
        Bookmark bookmark = bookmarks.get(position);
        if (bookmark.getType() == Bookmark.ItemType.SIMPLE) {
            return SIMPLE;
        } else if (bookmark.getType() == Bookmark.ItemType.NO_DESCRIPTION) {
            return NO_DESCRIPTION;
        } else if (bookmark.getType() == Bookmark.ItemType.NO_IMAGE) {
            return NO_IMAGE;
        } else if (bookmark.getType() == Bookmark.ItemType.NORMAL) {
            return NORMAL;
        } else {
            return -1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        db = DatabaseHandler.getInstance(mainActivity);
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case SIMPLE:
                view = layoutInflater.inflate(R.layout.bookmark_list_simple, parent, false);
                return new MainViewHolder(view, mainActivity);
            case NO_DESCRIPTION:
                view = layoutInflater.inflate(R.layout.bookmark_list_no_image, parent, false);
                MainViewHolder mainViewHolderImage = new MainViewHolder(view, mainActivity);
                mainViewHolderImage.setImage();
                return mainViewHolderImage;
            case NO_IMAGE:
                view = layoutInflater.inflate(R.layout.bookmark_list, parent, false);
                MainViewHolder mainViewHolderDescription = new MainViewHolder(view, mainActivity);
                mainViewHolderDescription.setDescription();
                return mainViewHolderDescription;
            case NORMAL:
                view = layoutInflater.inflate(R.layout.bookmark_list, parent, false);
                MainViewHolder mainViewHolder = new MainViewHolder(view, mainActivity);
                mainViewHolder.setImage();
                mainViewHolder.setDescription();
                return mainViewHolder;
            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        setOptions((MainViewHolder) holder, position);
        String description = bookmarks.get(position).getDescription();
        switch (holder.getItemViewType()) {
            case NORMAL:
                Picasso.get().load(bookmarks.get(position).getImage())
                        .fit()
                        .centerCrop()
                        .into(((MainViewHolder) holder).image);

                ((MainViewHolder) holder).image.setOnClickListener(v -> {
                    ImagePreview imagePreview = ImagePreview.newInstance(bookmarks.get(position)
                            .getImage(), bookmarks.get(position).getTitle());
                    imagePreview.show(mainActivity.getSupportFragmentManager(), "tag");
                });

                if (description.length() > DESCRIPTION_MAX_LENGTH) {
                    description = description.substring(0, DESCRIPTION_MAX_LENGTH) + "...";
                }
                ((MainViewHolder) holder).description.setText(description);
                break;
            case NO_DESCRIPTION:
                Picasso.get().load(bookmarks.get(position).getImage())
                        .fit()
                        .centerCrop()
                        .into(((MainViewHolder) holder).image);
                break;
            case NO_IMAGE:
                if (description.length() > DESCRIPTION_MAX_LENGTH) {
                    description = description.substring(0, DESCRIPTION_MAX_LENGTH) + "...";
                }
                ((MainViewHolder) holder).description.setText(description);
                break;
            case SIMPLE:
            default:
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void setOptions(MainViewHolder holder, int position) {
        holder.title.setText(bookmarks.get(position).getTitle());
        holder.link.setText(bookmarks.get(position).getLink());

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
        switch (operation) {
            case DELETE_OPTION:
                bookmarks.removeAll(selectedBookmarks);
                db.deleteBookmarks(selectedBookmarks);
                notifyDataSetChanged();
                break;
            case ARCHIVE_OPTION:
                bookmarks.removeAll(selectedBookmarks);
                db.archiveBookmarks(selectedBookmarks);
                notifyDataSetChanged();
                break;
            case UNARCHIVE_OPTION:
                bookmarks.removeAll(selectedBookmarks);
                for (Bookmark bookmark : selectedBookmarks) {
                    db.removeFromArchive(bookmark.getId());
                }
                notifyDataSetChanged();
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (bookmarks == null) {
            return 0;
        } else {
            return bookmarks.size();
        }
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

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

    static class MainViewHolder extends RecyclerView.ViewHolder {
        TextView link, title, description;
        ImageView image, options;
        ImageButton shareButton;
        View view;
        CheckBox checkbox;
        RelativeLayout relativeLayout;

        public MainViewHolder(@NonNull View itemView, MainActivity mainActivity) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            link = itemView.findViewById(R.id.link);
            options = itemView.findViewById(R.id.bookmark_options);
            checkbox = itemView.findViewById(R.id.checkbox);
            shareButton = itemView.findViewById(R.id.share);
            relativeLayout = itemView.findViewById(R.id.relative_layout);
            view = itemView;
            view.setOnLongClickListener(mainActivity);
        }

        public void setDescription() {
            description = view.findViewById(R.id.description);
        }

        public void setImage() {
            image = view.findViewById(R.id.image);
        }
    }
}