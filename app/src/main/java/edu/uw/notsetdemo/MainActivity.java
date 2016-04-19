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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
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

    private static final int REQUEST_PICTURE_CODE = 1;
    private static final int SMS_SENT_CODE = 2;
    private static final int NOTIFY_DEMO_CODE = 3;

    private static final int TEST_NOTIFY_ID = 0;

    public static final String ACTION_SMS_STATUS = "edu.uw.notsetdemo.ACTION_SMS_STATUS";

    private int notifies = 0;
    private ShareActionProvider mShareActionProvider;


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

        //dynamic BroadcastReceiver registry
//        IntentFilter batteryFilter = new IntentFilter();
//        batteryFilter.addAction(Intent.ACTION_BATTERY_LOW);
//        batteryFilter.addAction(Intent.ACTION_BATTERY_OKAY);
//        batteryFilter.addAction(Intent.ACTION_POWER_CONNECTED);
//        batteryFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
//        this.registerReceiver(new MyReceiver(), batteryFilter);

        SharedPreferences prefs = getSharedPreferences("TestPrefs", MODE_PRIVATE);
        notifies = prefs.getInt("Notifies", 0);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //save some details when app stops
        SharedPreferences prefs = getSharedPreferences("TestPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("Notifies",notifies);
        editor.commit();
    }

    public void callNumber(View v) {
        Log.v(TAG, "Call button pressed");

        //implicit intent
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:206-685-1622"));

        if(intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);

    }

    public void takePicture(View v) {
        Log.v(TAG, "Camera button pressed");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_PICTURE_CODE);
        }

    }

    public void sendMessage(View v) {
        Log.v(TAG, "Message button pressed");

        SmsManager smsManager = SmsManager.getDefault();

        Intent intent = new Intent(ACTION_SMS_STATUS);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, SMS_SENT_CODE, intent, 0);

        smsManager.sendTextMessage("5554", null, "This is a message!", pendingIntent, null);

    }

    public void notify(View v){
        Log.v(TAG, "Notify button pressed");

        notifies++;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("pref_notify",true)){

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle("You're on notice!")
                    .setContentText("This notice has been generated "+notifies+" times");
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            builder.setVibrate(new long[]{0,500,500,5000});
            builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(SecondActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(NOTIFY_DEMO_CODE, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(TEST_NOTIFY_ID, builder.build()); //post the notification!

        }
        else {
            Toast.makeText(this, "This notice has been generated "+notifies+" times", Toast.LENGTH_LONG).show();
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

        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:206-685-1622"));

        mShareActionProvider.setShareIntent(intent);

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
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.menu_item_click:
                Log.v(TAG, "Extra button pressed");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
