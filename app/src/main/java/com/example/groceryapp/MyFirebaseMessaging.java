package com.example.groceryapp;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.groceryapp.activity.OrderDetailsBuyerActivity;
import com.example.groceryapp.activity.OrderDetailsShopActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.BitSet;
import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String NOTIFICATION_CHANNEL_ID = "MY_NOTIFICATION_CHANNEL_ID";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();


        String notificationType = remoteMessage.getData().get("notificationType");

        if (notificationType != null && notificationType.equals("NewOrder")) {
            String buyerUid = remoteMessage.getData().get("buyerUid");
            String sellerUid = remoteMessage.getData().get("sellerUid");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String notificationDescription = remoteMessage.getData().get("notificationMessage");

            //user is signed and is same user to which notification is sent
            if (firebaseUser != null && firebaseAuth.getUid().equals(sellerUid))
                showNotification(orderId, sellerUid, buyerUid, notificationTitle, notificationDescription, notificationType);
        }
        if (notificationType != null && notificationType.equals("OrderStatusChanged")) {
            String buyerUid = remoteMessage.getData().get("buyerUid");
            String sellerUid = remoteMessage.getData().get("sellerUid");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String notificationDescription = remoteMessage.getData().get("notificationMessage");

            if (firebaseUser != null && firebaseAuth.getUid().equals(buyerUid)) {
                //user is signed and is same user to which notification is sent
                showNotification(orderId, sellerUid, buyerUid, notificationTitle, notificationDescription, notificationType);

            }
        }

    }

    private void showNotification(String orderId, String sellerUid, String buyerId, String notificationTitle, String notificationDescription, String notificationType){
        //notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //id for notification
        int notificationID = new Random().nextInt(3000);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            setupNotificationChannel(notificationManager);
        }

        //handle notification clicks
        Intent intent = null;
        if (notificationType.equals("NewOrder")){
            intent = new Intent(this, OrderDetailsShopActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderBy", buyerId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        else if (notificationType.equals("OrderStatusChanged")) {
            intent = new Intent(this, OrderDetailsBuyerActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderFrom", sellerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //large icons
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.logo);

        Uri notificationSounUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.drawable.logo)
                .setLargeIcon(largeIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDescription)
                .setSound(notificationSounUri)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(largeIcon)
                        .bigLargeIcon(null))
                .setContentIntent(pendingIntent);

        //show notification
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "Some Sample";
        String channelDescription = "Channel Description";

        NotificationChannel notificationChannel= new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        if (notificationManager != null){
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
