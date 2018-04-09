package hackeru.talg.edu.projectandroid3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;  //TODO: add scrolling option to recyclerView
    //TODO: add remove option for the recyclerView
    private RecyclerView.Adapter mAdapter;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final String SMS_JOB_TAG = "delayed-sms-tag";

    private String[] smsScheduleList;

    private EditText etPhoneNumber;
    private EditText etMessage;
    private TextView tvDate;
    private TextView tvTime;
    private Button buttonDate;
    private Button buttonTime;
    String phoneNo;
    String message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        requestSMSPermission();

        recyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etMessage = findViewById(R.id.etMessage);

        buttonDate = findViewById(R.id.buttonDate);
        buttonTime = findViewById(R.id.buttonTime);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);

        smsScheduleList = new String[9];
        for (int i = 0; i < smsScheduleList.length; i++) {
            smsScheduleList[i] = "";
        }


        mAdapter = new ListAdapter(smsScheduleList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!testSMSPermission()) {
                    requestSMSPermission();
                    return;
                }
                String part1 = "To: " + etPhoneNumber.getText().toString();
                String part2 = "at: " + tvDate.getText().toString() + " " + tvTime.getText().toString();
                String part3 = "message: " + etMessage.getText().toString();
                for (int i = 0; i < smsScheduleList.length; i++) {
                    if (smsScheduleList[i] == "") {
                        smsScheduleList[i] = part1 + ", " + part2 + ", " + part3;
                        mAdapter.notifyDataSetChanged();
                        break;
                    } else if (i == smsScheduleList.length - 1) {
                        //TODO: add alert: "list is full"
                        return;
                    }
                }
                sendDelayedSMS();
                return;
            }
        });

        buttonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment newFragmentb = new DatePickerFragment();
                newFragmentb.setTvDate(tvDate);
                DialogFragment newFragment = newFragmentb;
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        buttonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment newFragmentb = new TimePickerFragment();
                newFragmentb.setTvTime(tvTime);
                DialogFragment newFragment = newFragmentb;
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });
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

    protected void sendDelayedSMS() {
        phoneNo = etPhoneNumber.getText().toString();
        if (etPhoneNumber.length() != 10) {
            // TODO: add an alert message
        }
        message = etMessage.getText().toString();
        if (etMessage.getText().toString() == "") {
            //TODO: add an alert message
        }
        if (!testSMSPermission()) {
            requestSMSPermission();
            return;
        }
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

        String dateText = tvDate.getText().toString();
        if (dateText == "Choose Date") {
            //TODO: add an alert message
        }
        String[] date;
        date = dateText.split("/");
        String timeText = tvTime.getText().toString();
        if (timeText == "Choose Time") {
            //TODO: add an alert message
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
            //TODO: add an alert message
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
        int windowEnd = windowStart + 60*5;
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
            Long timeShift = 3L*60*60*1000;
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

    private Integer getNumOfSmss (String[] array) {
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
