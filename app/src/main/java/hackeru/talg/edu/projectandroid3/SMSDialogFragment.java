package hackeru.talg.edu.projectandroid3;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class SMSDialogFragment extends DialogFragment {

    private EditText etPhoneNumber;
    private EditText etMessage;
    private TextView tvDate;
    private TextView tvTime;
    private Button buttonDate;
    private Button buttonTime;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_smsdialog, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        etPhoneNumber = v.findViewById(R.id.etPhoneNumber);
        etMessage = v.findViewById(R.id.etMessage);
        buttonDate = v. findViewById(R.id.buttonDate);
        buttonTime = v. findViewById(R.id.buttonTime);
        tvDate = v. findViewById(R.id.tvDate);
        tvTime = v. findViewById(R.id.tvTime);




        FloatingActionButton fab =  v.findViewById(R.id.fabAddSms);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String phoneNo = etPhoneNumber.getText().toString();

                String message = etMessage.getText().toString();

                String dateText = tvDate.getText().toString();
                String timeText = tvTime.getText().toString();

                MainActivity a = (MainActivity) getActivity();

                a.doIt(phoneNo, dateText, timeText, message);

                //kill myself(dialog)
                dismiss();

                return;
            }
        });

        buttonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment newFragmentb = new DatePickerFragment();
                newFragmentb.setTvDate(tvDate);
                DialogFragment newFragment = newFragmentb;
                newFragment.show(getChildFragmentManager(), "datePicker");
            }
        });

        buttonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment newFragmentb = new TimePickerFragment();
                newFragmentb.setTvTime(tvTime);
                DialogFragment newFragment = newFragmentb;
                newFragment.show(getChildFragmentManager(), "timePicker");
            }
        });
    }
}
