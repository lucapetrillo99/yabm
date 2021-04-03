package com.example.linkcontainer;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
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


public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private static final int DESCRIPTION_MAX_LENGTH = 80;
    private ArrayList<Bookmark> bookmarks;
    private Context context;

    public RecyclerAdapter(ArrayList<Bookmark> bookmarkList, Context context) {
        this.bookmarks = bookmarkList;
        this.context = context;

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
      TextView link, title, description;
      ImageView image;
      ImageButton shareButton;

        public MyViewHolder(final View view) {
            super(view);
            link = view.findViewById(R.id.url);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            image = view.findViewById(R.id.image);
            shareButton = view.findViewById(R.id.share);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String description = bookmarks.get(position).getDescription();
        String text = "<a href=" + bookmarks.get(position).getLink() + ">" + bookmarks.get(position).getLink()  +" </a>";
//        holder.link.setText(bookmarks.get(position).getLink());
        holder.link.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
        holder.title.setText(bookmarks.get(position).getTitle());

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
            context.startActivity(i);
        });

        holder.image.setOnClickListener(v -> {
                final Dialog nagDialog = new Dialog(context);
                nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                nagDialog.setCancelable(true);
                nagDialog.setContentView(R.layout.preview_image);
                ImageView ivPreview = nagDialog.findViewById(R.id.preview_image);
                Picasso.get().load(bookmarks.get(position).getImage())
                        .fit()
                        .centerCrop()
                        .into(ivPreview);

                ivPreview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        nagDialog.dismiss();
                    }
                });
                nagDialog.show();
                nagDialog.getWindow().setLayout(1000, 600);
            });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("link", bookmarks.get(position).getLink());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, "Testo copiato negli appunti", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, bookmarks.get(position).getLink());
                shareIntent.setType("text/plain");
                context.startActivity(shareIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (bookmarks == null) {
            return 0;
        } else {
            return bookmarks.size();
        }
    }
}
