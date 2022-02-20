package com.ilpet.yabm.utils;

import android.content.Context;
import android.widget.Toast;

import com.ilpet.yabm.classes.Bookmark;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connection implements Runnable {
    private static final String REGEX = "^(([^:/?#]+):)?(//([^/?#]*))?";
    private final String url;
    private final String title;
    private final Boolean isEditMode;
    private final Bookmark bookmark;
    private final Context context;

    public Connection(String url, String title, Boolean isEditMode, Context context, Bookmark bookmark) {
        this.url = url;
        this.title = title;
        this.isEditMode = isEditMode;
        this.context = context;
        this.bookmark = bookmark;
    }

    @Override
    public void run() {
        try {
            Document document = Jsoup.connect(url).get();
            if (document != null) {
                Elements metaTags = document.getElementsByTag("meta");
                for (Element element : metaTags) {
                    if (element.attr("property").equals("og:image")) {
                        Pattern pattern = Pattern.compile(REGEX);
                        Matcher matcher = pattern.matcher(element.attr("content"));
                        if (matcher.find()) {
                            if (!Objects.equals(matcher.group(0), "")) {
                                bookmark.setImage(element.attr("content"));
                            } else {
                                bookmark.setImage(matcher.group(0) + element.attr("content"));
                            }
                        }
                    } else if (element.attr("property").equals("og:site_name")) {
                        if (!isEditMode) {
                            if (title.isEmpty()) {
                                bookmark.setTitle(element.attr("content"));
                            } else {
                                bookmark.setTitle(title);
                            }
                        } else {
                            if (title.isEmpty()) {
                                bookmark.setTitle(element.attr("content"));
                            }
                        }
                    } else if (element.attr("name").equals("description")) {
                        bookmark.setDescription(element.attr("content"));
                    }
                }
                bookmark.setLink(url);
                if (!isEditMode) {
                    if (bookmark.getTitle() == null && title.isEmpty()) {
                        bookmark.setTitle(url.split("//")[1].split("/")[0]);
                    }
                } else {
                    if (bookmark.getTitle() == null && title.isEmpty()) {
                        bookmark.setTitle(url.split("//")[1].split("/")[0]);
                    } else if (!title.isEmpty() && bookmark.getTitle() != null) {
                        if (!bookmark.getTitle().equals(title)) {
                            bookmark.setTitle(title);
                        }
                    }
                }
                if (bookmark.getDescription() == null && bookmark.getImage() == null) {
                    bookmark.setType(Bookmark.ItemType.SIMPLE);
                } else if (bookmark.getDescription() == null && bookmark.getImage() != null) {
                    bookmark.setType(Bookmark.ItemType.NO_DESCRIPTION);
                } else if (bookmark.getDescription() != null && bookmark.getImage() == null) {
                    bookmark.setType(Bookmark.ItemType.NO_IMAGE);
                } else {
                    bookmark.setType(Bookmark.ItemType.NORMAL);
                }
            } else {
                bookmark.setLink(url);
                if (!isEditMode) {
                    if (bookmark.getTitle() == null && title.isEmpty()) {
                        bookmark.setTitle(url.split("//")[1].split("/")[0]);
                    }
                } else {
                    if (bookmark.getTitle() == null && title.isEmpty()) {
                        bookmark.setTitle(url.split("//")[1].split("/")[0]);
                    } else if (!title.isEmpty() && bookmark.getTitle() != null) {
                        if (!bookmark.getTitle().equals(title)) {
                            bookmark.setTitle(title);
                        }
                    }
                }
                bookmark.setType(Bookmark.ItemType.SIMPLE);
                Toast.makeText(context,
                        "Non è possibile recuperare le informazioni per questo link!", Toast.LENGTH_LONG)
                        .show();
            }
        } catch (IOException e) {
            bookmark.setLink(url);
            if (!isEditMode) {
                if (bookmark.getTitle() == null && title.isEmpty()) {
                    bookmark.setTitle(url.split("//")[1].split("/")[0]);
                }
            } else {
                if (bookmark.getTitle() == null && title.isEmpty()) {
                    bookmark.setTitle(url.split("//")[1].split("/")[0]);
                } else if (!title.isEmpty() && bookmark.getTitle() != null) {
                    if (!bookmark.getTitle().equals(title)) {
                        bookmark.setTitle(title);
                    }
                }
            }
            bookmark.setType(Bookmark.ItemType.SIMPLE);
            Toast.makeText(context,
                    "Impossibile recuperare altre informazioni. " +
                            "\nRiprova più tardi", Toast.LENGTH_LONG)
                    .show();
        }
    }

    public Bookmark getBookmark() {
        return bookmark;
    }

}