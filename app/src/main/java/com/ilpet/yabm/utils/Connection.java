package com.ilpet.yabm.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ilpet.yabm.classes.Bookmark;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connection extends Worker {
    private static final String REGEX = "^(([^:/?#]+):)?(//([^/?#]*))?";

    public Connection(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String link = data.getString("link");
        String image = null, description = null, title = null, itemType = null;
        try {
            Document document = Jsoup.connect(link).get();
            if (document != null) {
                Elements metaTags = document.getElementsByTag("meta");
                for (Element element : metaTags) {
                    if (element.attr("property").equals("og:image")) {
                        Pattern pattern = Pattern.compile(REGEX);
                        Matcher matcher = pattern.matcher(element.attr("content"));
                        if (matcher.find()) {
                            if (!Objects.equals(matcher.group(0), "")) {
                                image = element.attr("content");
                            } else {
                                image = matcher.group(0) + element.attr("content");
                            }
                        }
                    } else if (element.attr("property").equals("og:site_name")) {
                        title = element.attr("content");
                    } else if (element.attr("name").equals("description")) {
                        description = element.attr("content");
                    }
                }
                if (title == null) {
                    assert link != null;
                    title = link.split("//")[1].split("/")[0];
                }
                if (description == null && image == null) {
                    itemType = String.valueOf(Bookmark.ItemType.SIMPLE);
                } else if (description == null) {
                    itemType = String.valueOf(Bookmark.ItemType.NO_DESCRIPTION);
                } else if (image == null) {
                    itemType = String.valueOf(Bookmark.ItemType.NO_IMAGE);
                } else {
                    itemType = String.valueOf(Bookmark.ItemType.NORMAL);
                }
            }
        } catch (IOException e) {
            itemType = String.valueOf(Bookmark.ItemType.SIMPLE);
            Data result = new Data.Builder()
                    .putString("image", null)
                    .putString("title", title)
                    .putString("description", null)
                    .putString("itemType", itemType)
                    .build();

            return Result.failure(result);
        }

        Data result = new Data.Builder()
                .putString("image", image)
                .putString("title", title)
                .putString("description", description)
                .putString("itemType", itemType)
                .build();

        return Result.success(result);
    }
}
