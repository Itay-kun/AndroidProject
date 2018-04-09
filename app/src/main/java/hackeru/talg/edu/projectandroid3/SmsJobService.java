package hackeru.talg.edu.projectandroid3;

import android.os.Bundle;
import android.telephony.SmsManager;

import com.firebase.jobdispatcher.JobService;

public class SmsJobService extends JobService {
    @Override
    public boolean onStartJob(final com.firebase.jobdispatcher.JobParameters job) {
        runJob(job);
        return false;
    }

    @Override
    public boolean onStopJob(final com.firebase.jobdispatcher.JobParameters job) {
        return false;
    }

    private void runJob(final com.firebase.jobdispatcher.JobParameters job) {
        try {
            Bundle extras = job.getExtras();
            String phoneNo = extras.get("phoneNo").toString();
            String message = extras.get("message").toString();
            String strPhone;
            if (phoneNo.length() <= 4) {
                strPhone = "0547162040";
            } else {
                strPhone = phoneNo;
            }
            String strMessage = message;
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(strPhone, null, strMessage, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
