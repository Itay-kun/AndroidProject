package hackeru.talg.edu.androidproject;


import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class SMSDialogFragment extends DialogFragment {

    private EditText etPhoneNumber;
    private EditText etMessage;
    private TextView tvDate;
    private TextView tvTime;
    private Button btnDate;
    private Button btnTime;
    private FloatingActionButton fabContacts;

    private final int REQUEST_CODE=99;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_smsdialog, container, false);
        WindowManager.LayoutParams wmlp = getDialog().getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER;
        return v;
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        etPhoneNumber = v.findViewById(R.id.etPhoneNumber);
        etMessage = v.findViewById(R.id.etMessage);
        btnDate = v. findViewById(R.id.btnDate);
        btnTime = v. findViewById(R.id.btnTime);
        fabContacts = v.findViewById(R.id.fabContacts);
        tvDate = v. findViewById(R.id.tvDate);
        tvTime = v. findViewById(R.id.tvTime);



        Button addSMSBtn = v.findViewById(R.id.addSMSBtn);
        addSMSBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNo = etPhoneNumber.getText().toString();

                String message = etMessage.getText().toString();

                String dateText = tvDate.getText().toString();
                String timeText = tvTime.getText().toString();

                MainActivity a = (MainActivity) getActivity();

                phoneNo = phoneNo.replaceAll("\\D", "");
                if (phoneNo.length() < 10 || phoneNo.length() > 13) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.dialog_title)
                            .setMessage(R.string.dialog_message_phone)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                    return;
                }

                if (message.isEmpty()) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.dialog_title)
                            .setMessage(R.string.dialog_message_message)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                    return;
                }

                if (dateText.equals("")) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.dialog_title)
                            .setMessage(R.string.dialog_message_date)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                    return;
                }

                if (timeText.equals("")) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.dialog_title)
                            .setMessage(R.string.dialog_message_time)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                Date strDate = new Date();
                Date today = new Date();
                String dateTime = dateText + " " + timeText;
                try {
                    strDate = sdf.parse(dateTime);
                } catch (Exception e) {

                }
                if (today.after(strDate)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.dialog_title)
                            .setMessage(R.string.dialog_message_too_early)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                    return;
                }

                a.updateListAndSend(phoneNo, dateText, timeText, message, true);

                //kill myself(dialog)
                dismiss();

                return;
            }
        });

        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment newFragmentb = new DatePickerFragment();
                newFragmentb.setTvDate(tvDate);
                DialogFragment newFragment = newFragmentb;
                newFragment.show(getChildFragmentManager(), "datePicker");
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment newFragmentb = new TimePickerFragment();
                newFragmentb.setTvTime(tvTime);
                DialogFragment newFragment = newFragmentb;
                newFragment.show(getChildFragmentManager(), "timePicker");
            }
        });
        fabContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, 1);
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 1) && (resultCode == RESULT_OK)) {
            Cursor cursor = null;
            try {
                Uri uri = data.getData();
                cursor = getActivity().getContentResolver().query(uri, new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER }, null, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    String phone = cursor.getString(0);
                    // Do something with phone
                    etPhoneNumber.setText(phone);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
