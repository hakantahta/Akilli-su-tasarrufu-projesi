package com.example.suproje2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView textViewWaterUsage;
    private TextView textViewWelcome;
    private ProgressBar progressBarWaterUsage;
    private ImageView imageview_icon;
    private Vibrator vibrator;
    private static final String CHANNEL_ID = "my_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewWaterUsage = findViewById(R.id.textViewWaterUsage);
        textViewWelcome = findViewById(R.id.textViewWelcome);
        progressBarWaterUsage = findViewById(R.id.progressBarWaterUsage);
        imageview_icon = findViewById(R.id.imageview_icon);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        createNotificationChannel();

        // Firebase veritabanından su kullanımı verilerini almak için ValueEventListener kullanıyoruz
        FirebaseDatabase.getInstance().getReference().child("su_kullanimi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        // Veritabanından su kullanımı değerini al
                        int waterUsage = dataSnapshot.getValue(Integer.class);

                        // TextView ve ProgressBar'u güncelle
                        textViewWaterUsage.setText("Su Kullanımı: " + waterUsage + "%");
                        progressBarWaterUsage.setProgress(waterUsage);

                        // Su kullanımı 2'yi aştığında telefonu titret
                        if (waterUsage > 2) {
                            vibratePhone();
                            showNotification();
                        }
                    } else {
                        // Veri alınamadığında kullanıcıya Toast mesajı göster
                        Toast.makeText(MainActivity.this, "Veri alınamadı", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    // Hata durumunda kullanıcıya Toast mesajı göster
                    Toast.makeText(MainActivity.this, "Bir hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Veritabanı hatası durumunda yapılacak işlemler
                Toast.makeText(MainActivity.this, "Veritabanı hatası: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Telefonu titreten metod
    private void vibratePhone() {
        if (vibrator.hasVibrator()) {
            long[] pattern = {0, 500, 200, 500}; // Titreşim deseni: dinlenme, titreşim, dinlenme, titreşim
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Benim mesajım";
            String description = "Mesaj içeriğim";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification() {
        String title = "Su Kullanımı Uyarısı "+textViewWaterUsage.getText().toString();
        String message = "Hey! Doğasever. Su kullanımına dikkat etme vaktin geldi!";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.su)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());

        //Bildirimin gönderildiğinden emin olmak için log kaydına bilgi çektim
        Log.d("Notification", "Bildirim gösterildi");

    }
}