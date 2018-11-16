package com.univas.teusalesapp.teusalesapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ECMessasingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        mostrarNotificacao(remoteMessage.getNotification());
    }


    public void teste(){



    }


    public void mostrarNotificacao(RemoteMessage.Notification notification) {
        String titulo = notification.getTitle();
        String mensagem = notification.getBody();

        Intent intent = new Intent(this, ChatActivity.class);


        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Notification notificacao = builder.setContentTitle(titulo)
                .setContentText("").setSmallIcon(R.drawable.messages).
                        setContentIntent(pendingIntent).
                        setAutoCancel(true).
                        build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,notificacao);

    }
}
