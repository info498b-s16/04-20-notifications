package edu.uw.notsetdemo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "**Main**";

    public static final String ACTION_SMS_SENT = "edu.uw.notsetdemo.ACTION_SMS_SENT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View launchButton = findViewById(R.id.btnLaunch);
        launchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG, "Launch button pressed");

                //explicit intent
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                intent.putExtra("edu.uw.notsetdemo.message", "Hello from MainActivity!");

                startActivity(intent);
            }
        });

        //shared preferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("message", "Hello world");
        editor.commit();

        Log.v(TAG, prefs.getString("message",""));
    }

    public void callNumber(View v) {
        Log.v(TAG, "Call button pressed");

        //implicit intent
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:206-685-1622"));

        if(intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);

    }

    private static int REQUEST_PICTURE_CODE = 1;

    public void takePicture(View v) {
        Log.v(TAG, "Camera button pressed");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_PICTURE_CODE);
        }

    }

    private static final int SMS_SEND_CODE = 2;

    public void sendMessage(View v) {
        Log.v(TAG, "Message button pressed");

        SmsManager smsManager = SmsManager.getDefault();

        Intent smsIntent = new Intent(ACTION_SMS_SENT); //implicit intent

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, SMS_SEND_CODE, smsIntent, 0);

        smsManager.sendTextMessage("5554", null, "This is a message!", pendingIntent, null);

    }

    private static final int NOTIFY_CODE = 0;

    private int notifyCount = 0;

    public void notify(View v){
        Log.v(TAG, "Notify button pressed");

        notifyCount++;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean showPopup = prefs.getBoolean("pref_show_notification",true);

        if(showPopup) {


            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle("You're on notice!")
                    .setContentText("This is notication " + notifyCount);

            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            builder.setVibrate(new long[]{0, 500, 500, 500});
            builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);


            Intent intent = new Intent(this, SecondActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(SecondActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pendingIntent); //what to happen when clicked

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFY_CODE, builder.build());
        }
        else {
            Toast.makeText(this, "Popup!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_PICTURE_CODE && resultCode == RESULT_OK){
            //I got picture!!
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap)extras.get("data");

            ImageView imageView = (ImageView)findViewById(R.id.imgThumbnail);
            imageView.setImageBitmap(imageBitmap);

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_item_notify:
                notify(null);
                return true;
            case R.id.menu_item_prefs:
                Log.v(TAG, "Settings button pressed");

                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);

                return true;
            case R.id.menu_item_click:
                Log.v(TAG, "Extra button pressed");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
