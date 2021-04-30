package com.example.linkcontainer;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import io.reactivex.rxjava3.core.Observable;

public class Utils {

    public static Observable<Document> getJsoupContent(String url) {

        return Observable.fromCallable(() -> {
            try {
                return Jsoup.connect(url).timeout(0).get();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }
}
