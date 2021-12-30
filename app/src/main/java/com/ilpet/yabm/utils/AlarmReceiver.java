package com.ilpet.yabm.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.ilpet.yabm.R;
import com.ilpet.yabm.classes.Bookmark;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "BOOKMARKS_CHANNEL";
    private static final String CHANNEL_NAME = "Promemoria Segnalibri";
    private static final String ACTIVITY_PATH = "com.ilpet.yabm.activities.InsertBookmarkActivity";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getStringExtra("category") != null) {
            int notificationId = intent.getIntExtra("notificationId", 0);
            Bundle args = intent.getBundleExtra("data");
            Bookmark bookmark = (Bookmark) args.getSerializable("bookmark");
            String category = intent.getStringExtra("category");

            Intent mainIntent = new Intent(Intent.ACTION_VIEW);
            mainIntent.setData(Uri.parse(bookmark.getLink()));

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;

                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
                notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);

            }

            Intent modifyIntent = new Intent();
            modifyIntent.setClassName("com.ilpet.yabm", ACTIVITY_PATH);
            modifyIntent.putExtra("bookmark", bookmark);
            modifyIntent.putExtra("category", category);
            modifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent modifyPendingIntent =
                    PendingIntent.getActivity(
                            context, 0, modifyIntent, PendingIntent.FLAG_IMMUTABLE);

            Drawable d = ContextCompat.getDrawable(context, R.drawable.ic_app_logo_round);
            Bitmap appIcon = drawableToBitmap(d);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(bookmark.getTitle())
                    .setShowWhen(true)
                    .setLargeIcon(appIcon)
                    .setContentText(bookmark.getLink())
                    .setContentIntent(contentIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(bookmark.getDescription()))
                    .addAction(R.drawable.ic_stat_name, context.getString(R.string.modify),
                            modifyPendingIntent);

            notificationManager.notify(notificationId, builder.build());
        } else {
            Uri uri = intent.getData();
            BackupHandler.getInstance(context).createBackup(uri);
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}