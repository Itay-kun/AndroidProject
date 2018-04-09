package hackeru.talg.edu.projectandroid3;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.textservice.TextInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import android.support.v4.app.DialogFragment;
import android.app.TimePickerDialog.OnTimeSetListener;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    TextView tvTime;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (tvTime == null) {
            return;
        }
        String timeText = hourOfDay + ":" + minute;
        tvTime.setText(timeText);
    }

    public void setTvTime(TextView tv) {
        this.tvTime = tv;
    }
}
