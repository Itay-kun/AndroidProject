package hackeru.talg.edu.androidproject;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import hackeru.talg.edu.androidproject.InvalidInputDialogs.AlertDialogInvalidDate;
import hackeru.talg.edu.androidproject.InvalidInputDialogs.AlertDialogInvalidMessage;
import hackeru.talg.edu.androidproject.InvalidInputDialogs.AlertDialogInvalidPhoneNumber;
import hackeru.talg.edu.androidproject.InvalidInputDialogs.AlertDialogInvalidTime;
import hackeru.talg.edu.androidproject.InvalidInputDialogs.AlertDialogTooEarlyTime;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;  //TODO: add scrolling option to recyclerView
    //TODO: add remove option for the recyclerView
    private RecyclerView.Adapter mAdapter;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final String SMS_JOB_TAG = "delayed-sms-tag";


    private String[] smsScheduleList;

    private Button btnLoadList;

    private FirebaseAuth mAuth;

    private FirebaseUser currentUser;

    private DatabaseReference messageRef;
    private static boolean listIsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseApp fbApp = FirebaseApp.initializeApp(this);

        requestSMSPermission();

        recyclerView = findViewById(R.id.mRecyclerView);
        btnLoadList = findViewById(R.id.btnLoadList);

        smsScheduleList = new String[9];
        for (int i = 0; i < smsScheduleList.length; i++) {
            smsScheduleList[i] = "";
        }


        mAdapter = new ListAdapter(smsScheduleList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        mAuth = FirebaseAuth.getInstance();

        btnLoadList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser = mAuth.getCurrentUser();
                FirebaseDatabase database;
                if (currentUser != null) {
                    database = FirebaseDatabase.getInstance();
                    messageRef = database.getReference("message");
                    messageRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //if (listIsInitialized == false) {
                                collectMessages((Map<String, Object>) dataSnapshot.
                                        getValue());
                            //    listIsInitialized = true;
                            //}
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });

                    mAdapter.notifyDataSetChanged();
            }
        }
        });
/*

            mAdapter.notifyDataSetChanged();
            //updateListAndSend("123","123","123","123", false);
            //removeFromList();
        }*/

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new SMSDialogFragment();
/*                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;
                dialogFragment.getDialog().getWindow().setLayout((6 * width)/7, (4 * height)/5);*/
                dialogFragment.show(getSupportFragmentManager(), "smsDialogFrag");
            }
        });

/*        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });*/
    }

    //TODO:
    void updateListAndSend(String phoneNumber, String date, String time, String message,
                           boolean send) {
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
                        MainActivity.this.finish();
                    }}
                )
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    protected void sendDelayedSMS(String phoneNo, String dateText, String timeText,
                                  String message) {
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
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(
                new GooglePlayDriver(this));

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
            String personId = "";
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser mUser = mAuth.getCurrentUser();
            for (UserInfo user: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                if (user.getProviderId().equals("password")) {
                    //TODO: add password login userId
                } else if (user.getProviderId().equals("google.com")) {
                    GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
                    personId = acct.getId();
                }
            }
            //add message to database
            String messageID = FirebaseDatabase.getInstance().getReference().push().getKey();
            Message messageDB = new Message(phoneNo, message, dateText, timeText, personId);  //messageDB means message database
            FirebaseDatabase.getInstance().getReference().child("message").child(messageID).setValue(messageDB);
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

    private void collectMessages(Map<String,Object> messagesMap) {

        //ArrayList<Long> messagesList = new ArrayList<>();
        if(messagesMap == null) {
            return;
        }

        //iterate through each message, ignoring their messageID
        for (Map.Entry<String, Object> entry : messagesMap.entrySet()){
            Message m = new Message();
            //Get message map
            Map<String, Object> singleMessage = Map.class.cast(entry.getValue());
            //Get fields and append to list
            //messagesList.add(singleMessage.get("message"));
            int count = 0;
            for (Map.Entry<String, Object> entry2 : singleMessage.entrySet()) {
                switch (count){
                    case 0: m.setMessageContent((String) entry2.getValue());
                            break;
                    case 1: m.setPhoneNumber((String) entry2.getValue());
                            break;
                    case 2: m.setDate((String) entry2.getValue());
                            break;
                    case 3: m.setTime((String) entry2.getValue());
                            break;
                    case 4: m.setPersonId((String) entry2.getValue());
                            break;
                }
                count++;
            }
            updateListAndSend(m.getPhoneNumber(), m.getMessageContent(),
                    m.getDate(), m.getTime(), false);
        }

    }

/*    private void removeFromList(String phoneNumber, String date, String time, String message) {
        String part1 = "To: " + phoneNumber;
        String part2 = "at: " + date + " " + time;
        String part3 = "message: " + message;
        for (int i = 0; i < smsScheduleList.length; i++) {
            if (!smsScheduleList[i].isEmpty()) {
                if (smsScheduleList[i].equals(part1 + ", " + part2 + ", " + part3)) {
                    smsScheduleList[i] = "";
                }
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    }*/

/*    private sortList() {
        for (int i = 0; i < smsScheduleList.length * smsScheduleList.length; i++) {
            String current;
            if (!smsScheduleList[i].isEmpty()) {
                current = smsScheduleList[i];
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }*/

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

        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();

        } else {
            //Toast.makeText(this, "Not Granted", Toast.LENGTH_SHORT).show();
        }
    }


}
