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

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.ilpet.yabm.R;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "SAMPLE_CHANNEL";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getStringExtra("message") != null) {
            int notificationId = intent.getIntExtra("notificationId", 0);
            String message = intent.getStringExtra("message");
            String link = intent.getStringExtra("link");

            Intent mainIntent = new Intent(Intent.ACTION_VIEW);
            mainIntent.setData(Uri.parse(link));

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence channelName = "Promemoria Segnalibri";
                int importance = NotificationManager.IMPORTANCE_HIGH;

                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
                notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);

            }

            Drawable d = ContextCompat.getDrawable(context, R.drawable.ic_app_logo_round);
            Bitmap appIcon = drawableToBitmap(d);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(context.getString(R.string.reminder))
                    .setShowWhen(true)
                    .setLargeIcon(appIcon)
                    .setContentText(message)
                    .setContentIntent(contentIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            notificationManager.notify(notificationId, builder.build());
        } else {
            Uri uri = intent.getData();
            BackupHandler.getInstance(context).createBackup(uri);
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
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
