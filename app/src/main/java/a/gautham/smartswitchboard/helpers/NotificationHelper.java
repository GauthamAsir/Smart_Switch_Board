package a.gautham.smartswitchboard.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.annotation.RequiresApi;

import a.gautham.smartswitchboard.R;

public class NotificationHelper extends ContextWrapper {

    private static final String RJSWEETS_CHANNEL_ID = "smart_switch_board";
    private static final String RJSWEETS_CHANNEL_NAME = "Smart Switch Board";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {

        NotificationChannel rjChannel = new NotificationChannel(RJSWEETS_CHANNEL_ID, RJSWEETS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        rjChannel.enableLights(true);
        rjChannel.enableVibration(true);
        rjChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(rjChannel);
    }

    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    public Notification.Builder rjSweetsChannelNotification(String title, String body) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(getApplicationContext(), RJSWEETS_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                    .setAutoCancel(true);
        }
        return new Notification.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setAutoCancel(true);
    }
}
