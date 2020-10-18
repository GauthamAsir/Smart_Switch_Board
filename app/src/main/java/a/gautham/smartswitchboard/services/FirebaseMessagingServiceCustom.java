package a.gautham.smartswitchboard.services;

import android.app.Notification;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import a.gautham.smartswitchboard.helpers.NotificationHelper;

public class FirebaseMessagingServiceCustom extends com.google.firebase.messaging.FirebaseMessagingService {

    FirebaseFirestore firestore;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        sendNotification(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    private void sendNotification(RemoteMessage remoteMessage) {

        firestore = FirebaseFirestore.getInstance();

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        assert notification != null;
        String title = notification.getTitle();
        String content = notification.getBody();

        NotificationHelper helper = new NotificationHelper(this);

        Notification.Builder builder;
        builder = helper.rjSweetsChannelNotification(title, content);

        //Generating Random SendNotification ID to show all notification
        helper.getManager().notify(new Random().nextInt(), builder.build());

    }
}