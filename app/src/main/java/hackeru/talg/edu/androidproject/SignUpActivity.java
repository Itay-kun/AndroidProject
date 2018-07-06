package hackeru.talg.edu.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity{
    private FirebaseAuth mAuth;
    private static final String TAG = "EmailPassword";

    private AutoCompleteTextView actvEmail;
    private EditText etPasswordSignUp;
    private Button btnSignUp;
    private Button btnBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        actvEmail = findViewById(R.id.actvEmail);
        etPasswordSignUp = findViewById(R.id.etPasswordSignUp);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnBack = findViewById(R.id.btnBack);

        mAuth = FirebaseAuth.getInstance();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(actvEmail.getText().toString(), etPasswordSignUp.getText().toString());
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sign_up) {
            return true;
        } else if (id == R.id.action_sms) {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_login) {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed()
    {
        finish();
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = actvEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            actvEmail.setError("Required.");
            valid = false;
        } else {
            actvEmail.setError(null);
        }

        String password = etPasswordSignUp.getText().toString();
        if (TextUtils.isEmpty(password)) {
            etPasswordSignUp.setError("Required.");
            valid = false;
        } else {
            etPasswordSignUp.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}
