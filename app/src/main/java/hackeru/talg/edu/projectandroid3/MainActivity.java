package hackeru.talg.edu.projectandroid3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.Calendar;
import java.util.Date;

import hackeru.talg.edu.projectandroid3.InvalidInputDialogs.AlertDialogInvalidDate;
import hackeru.talg.edu.projectandroid3.InvalidInputDialogs.AlertDialogInvalidMessage;
import hackeru.talg.edu.projectandroid3.InvalidInputDialogs.AlertDialogInvalidPhoneNumber;
import hackeru.talg.edu.projectandroid3.InvalidInputDialogs.AlertDialogInvalidTime;
import hackeru.talg.edu.projectandroid3.InvalidInputDialogs.AlertDialogTooEarlyTime;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;  //TODO: add scrolling option to recyclerView
    //TODO: add remove option for the recyclerView
    private RecyclerView.Adapter mAdapter;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final String SMS_JOB_TAG = "delayed-sms-tag";

    private String[] smsScheduleList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        requestSMSPermission();

        recyclerView = findViewById(R.id.mRecyclerView);


        smsScheduleList = new String[9];
        for (int i = 0; i < smsScheduleList.length; i++) {
            smsScheduleList[i] = "";
        }


        mAdapter = new ListAdapter(smsScheduleList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SMSDialogFragment().show(getSupportFragmentManager(), "smsDialogFrag");
            }
        });


    }

    //TODO:
    void doIt(String phoneNumber, String date, String time, String message) {
        if (!testSMSPermission()) {
            requestSMSPermission();
            return;
        }
        String part1 = "To: " + phoneNumber;
        String part2 = "at: " + date + " " + time;
        String part3 = "message: " + message;
        for (int i = 0; i < smsScheduleList.length; i++) {
            if (smsScheduleList[i].isEmpty()) {
                smsScheduleList[i] = part1 + ", " + part2 + ", " + part3;
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
        sendDelayedSMS(phoneNumber, date, time, message);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void sendDelayedSMS(String phoneNo, String dateText, String timeText, String message) {
        if (phoneNo.length() != 10) {
            AlertDialogInvalidPhoneNumber dialog = new AlertDialogInvalidPhoneNumber();
            dialog.show(getSupportFragmentManager(), "AlertDialogInvalidPhoneNumber");
            return;
        }

        if (message.isEmpty()) {
            AlertDialogInvalidMessage dialog = new AlertDialogInvalidMessage();
            dialog.show(getSupportFragmentManager(), "AlertDialogInvalidMessage");
            return;
        }

        if (!testSMSPermission()) {
            requestSMSPermission();
            return;
        }
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

        if (dateText.equals("Choose Date")) {
            AlertDialogInvalidDate dialog = new AlertDialogInvalidDate();
            dialog.show(getSupportFragmentManager(), "AlertDialogInvalidDate");
            return;
        }
        String[] date;
        date = dateText.split("/");

        if (timeText.equals("Choose Time")) {
            AlertDialogInvalidTime dialog = new AlertDialogInvalidTime();
            dialog.show(getSupportFragmentManager(), "AlertDialogInvalidMessage");
            return;
        }
        String[] time;
        time = timeText.split(":");


        Bundle myExtrasBundle = new Bundle();
        myExtrasBundle.putString("phoneNo", phoneNo);
        myExtrasBundle.putString("message", message);
        myExtrasBundle.putString("minute", time[1]);
        myExtrasBundle.putString("hour", time[0]);
        myExtrasBundle.putString("day", date[0]);
        myExtrasBundle.putString("month", date[1]);
        myExtrasBundle.putString("year", date[2]);
        Integer numOfSmss = getNumOfSmss(smsScheduleList);
        myExtrasBundle.putString("job_tag", SMS_JOB_TAG + numOfSmss.toString());
        Job myJob;
        try {
            myJob = createJob(dispatcher, myExtrasBundle);
        } catch (IllegalArgumentException e) {
            AlertDialogTooEarlyTime dialog = new AlertDialogTooEarlyTime();
            dialog.show(getSupportFragmentManager(), "AlertDialogTooEarlyTime");
            return;
        }

        dispatcher.mustSchedule(myJob);
    }

    public static Job createJob(FirebaseJobDispatcher dispatcher, Bundle extras) {
        int windowStart;
        try {
            windowStart = calculateStartTime(extras);
        } catch (IllegalArgumentException e) {
            throw e;
        }
        int windowEnd = windowStart + 60 * 5;
        Job job = dispatcher.newJobBuilder()
                .setService(SmsJobService.class)
                .setTag(SMS_JOB_TAG + extras.getString("job_tag"))
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setRecurring(false)
                .setTrigger(Trigger.executionWindow(windowStart, windowEnd))
                .setReplaceCurrent(false)
                .setExtras(extras)
                .build();
        return job;
    }

    public static int calculateStartTime(Bundle extras) {
        Integer minute = Integer.valueOf(extras.getString("minute"));
        Integer hour = Integer.valueOf(extras.getString("hour"));
        Integer day = Integer.valueOf(extras.getString("day"));
        Integer month = Integer.valueOf(extras.getString("month")) - 1;
        Integer year = Integer.valueOf(extras.getString("year"));

        Calendar calendar = Calendar.getInstance();

        Date date = new Date(year - 1900, month, day, hour, minute);
        Long startTime = date.getTime();
        Date d2 = new Date(startTime);
        calendar.set(year, month, day,
                hour, minute, 0);
        int result;
        try {
            Long timeShift = 3L * 60 * 60 * 1000;
            Long now = System.currentTimeMillis();
            Date d3 = new Date(now);
            Long then = (startTime - now);
            if (then < 0) {
                throw new IllegalArgumentException();
            }
            result = then.intValue() / 1000;
        } catch (IllegalArgumentException e) {
            throw e;
        }
        return result;
    }

    private Integer getNumOfSmss(String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == "") {
                return i;
            }
        }
        return array.length;
    }

    public boolean testSMSPermission() {
        int result = ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        int phoneStateResult = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        return result == PackageManager.PERMISSION_GRANTED &&
                phoneStateResult == PackageManager.PERMISSION_GRANTED;
    }

    void requestSMSPermission() {
        long l = 100_000;
        String[] permissions = {Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE};
        ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_SEND_SMS);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Not Granted", Toast.LENGTH_SHORT).show();
        }
    }
}
