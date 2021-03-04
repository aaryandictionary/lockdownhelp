package com.lockdownhelp.app.Utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.lockdownhelp.app.MyRequests;
import com.lockdownhelp.app.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class NotificationService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size()>0){
            Map<String,String>payload=remoteMessage.getData();
            
            showNotification(payload);
        }
    }

    private void showNotification(Map<String, String> payload) {
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.logo);
        builder.setContentTitle(payload.get("title"));
        builder.setContentText(payload.get("message"));
        builder.setAutoCancel(true);

        builder.setColor(getResources().getColor(R.color.colorPrimaryDark));



        Intent resultIntent=new Intent(this, MyRequests.class);
        TaskStackBuilder stackBuilder=TaskStackBuilder.create(this);

        stackBuilder.addNextIntent(resultIntent);


        PendingIntent resultPendingIntent=stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0,builder.build());
    }


}
