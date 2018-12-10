package hackeru.talg.edu.androidproject;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Calendar;
import java.util.Date;

import hackeru.talg.edu.androidproject.InvalidInputDialogs.AlertDialogInvalidDate;
import hackeru.talg.edu.androidproject.InvalidInputDialogs.AlertDialogInvalidMessage;
import hackeru.talg.edu.androidproject.InvalidInputDialogs.AlertDialogInvalidPhoneNumber;
import hackeru.talg.edu.androidproject.InvalidInputDialogs.AlertDialogInvalidTime;
import hackeru.talg.edu.androidproject.InvalidInputDialogs.AlertDialogTooEarlyTime;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final String SMS_JOB_TAG = "delayed-sms-tag";


    private String[] smsScheduleList;

    private FloatingActionButton fabAddMessage;

    private FirebaseAuth mAuth;

    private FirebaseRecyclerAdapter adapter;

    private ProgressDialog progressBar;

    public static final int MAX_LIST_SIZE = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_menu_white_24dp));

        FirebaseApp.initializeApp(this);

        requestSMSPermission();

        recyclerView = findViewById(R.id.mRecyclerView);
        fabAddMessage = findViewById(R.id.fabAddMessage);

        smsScheduleList = new String[MAX_LIST_SIZE];
        for (int i = 0; i < smsScheduleList.length; i++) {
            smsScheduleList[i] = "";
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                LinearLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(true);

        progressBar = new ProgressDialog(this);
        progressBar.setTitle("Loading the list...");
        progressBar.setMessage("Please wait...");
        progressBar.setCancelable(false);
        progressBar.setIndeterminate(true);

        mAuth = FirebaseAuth.getInstance();

        fabAddMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new SMSDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), "smsDialogFrag");
            }
        });

    }

    void updateListAndSend(String phoneNumber, String date, String time, String message,
                           boolean send) {
        if (!testSMSPermission()) {
            requestSMSPermission();
            return;
        }
        if (send == true) {
            sendDelayedSMS(phoneNumber, date, time, message);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_login) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_sms) {
            return true;
        } else if (id == R.id.action_sign_up) {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_intro) {
            Intent intent = new Intent(MainActivity.this, IntroActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Do you want to leave this app?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        System.exit(0);
                    }}
                )
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    protected void sendDelayedSMS(String phoneNo, String dateText, String timeText,
                                  String message) {
        phoneNo = phoneNo.replaceAll("\\D", "");
        if (phoneNo.length() < 10 || phoneNo.length() > 13) {
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
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(
                new GooglePlayDriver(this));

        if (dateText.equals("")) {
            AlertDialogInvalidDate dialog = new AlertDialogInvalidDate();
            dialog.show(getSupportFragmentManager(), "AlertDialogInvalidDate");
            return;
        }
        String[] date;
        date = dateText.split("/");

        if (timeText.equals("")) {
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
        Integer numOfSmses = getNumOfSmses(smsScheduleList);
        myExtrasBundle.putString("job_tag", SMS_JOB_TAG + numOfSmses.toString());
        Job myJob;
        try {
            myJob = createJob(dispatcher, myExtrasBundle);
        } catch (IllegalArgumentException e) {
            AlertDialogTooEarlyTime dialog = new AlertDialogTooEarlyTime();
            dialog.show(getSupportFragmentManager(), "AlertDialogTooEarlyTime");
            return;
        }

        dispatcher.mustSchedule(myJob);

        if (mAuth.getCurrentUser() != null) {
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser mUser = mAuth.getCurrentUser();
            String personId = FirebaseAuth.getInstance().getCurrentUser().getProviderData().get(0).getUid();
            //add message to database
            String messageID = FirebaseDatabase.getInstance().getReference().push().getKey();
            Message messageDB = new Message(phoneNo, message, dateText, timeText, personId);  //messageDB means message database
            FirebaseDatabase.getInstance().getReference().child("messages").child(personId).push().setValue(messageDB);
        } else {
            String messageID = FirebaseDatabase.getInstance().getReference().push().getKey();
            Message messageDB = new Message(phoneNo, message, dateText, timeText, "guest");  //messageDB means message database
            FirebaseDatabase.getInstance().getReference().child("messages").child("guest").push().setValue(messageDB);
        }
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

    private Integer getNumOfSmses(String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == "") {
                return i;
            }
        }
        return array.length;
    }

    public boolean testSMSPermission() {
        int result = ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        int phoneStateResult = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE);

        return result == PackageManager.PERMISSION_GRANTED &&
                phoneStateResult == PackageManager.PERMISSION_GRANTED;
    }

    void requestSMSPermission() {
        long l = 100_000;
        String[] permissions = {Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE};
        ActivityCompat.requestPermissions(this, permissions,
                MY_PERMISSIONS_REQUEST_SEND_SMS);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onStart() {
        super.onStart();
        //progressBar.show();
        initializeRecyclerAdapter();
        if (mAuth.getCurrentUser() != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth.getCurrentUser() != null) {
            adapter.stopListening();
        }
    }

    private void initializeRecyclerAdapter(){
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = "guest";
        if (user != null){
            uid = FirebaseAuth.getInstance().getCurrentUser().getProviderData().get(0).getUid();
        } else {
            return;
        }
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("messages")
                .child(uid)
                .limitToLast(7);

        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(query, Message.class)
                        .build();


        adapter = new FirebaseRecyclerAdapter<Message, ListAdapter.ViewHolder>(options) {
            @Override
            public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item, parent, false);

                return new ListAdapter.ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(ListAdapter.ViewHolder holder, int position, Message model) {
                final String m = model.getMessageContent() + "\nto: " + model.getPhoneNumber()
                        + ", at: " + model.getDate() + " " + model.getTime();

                holder.message.setText(m);
            }

            @Override
            public void onDataChanged() {
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onError(DatabaseError e) {
            }
        };
    }
}
