package hackeru.talg.edu.androidproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
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

    private ProgressDialog progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        actvEmail = findViewById(R.id.actvEmail);
        etPasswordSignUp = findViewById(R.id.etPasswordSignUp);
        btnSignUp = findViewById(R.id.btnSignUp);

        mAuth = FirebaseAuth.getInstance();

        progressBar = new ProgressDialog(this);
        progressBar.setTitle("Processing...");
        progressBar.setMessage("Please wait...");
        progressBar.setCancelable(false);
        progressBar.setIndeterminate(true);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.show();
                createAccount(actvEmail.getText().toString(), etPasswordSignUp.getText().toString());
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
        int id = item.getItemId();

        if (id == R.id.action_login) {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_sms) {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_sign_up) {
            return true;
        } else if (id == R.id.action_intro) {
            Intent intent = new Intent(SignUpActivity.this, IntroActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_logout) {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show();
                return true;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Do you want to logout?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MenuManager.signOut(SignUpActivity.this);
                        }}
                    )
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        if(progressBar.isShowing()){
            progressBar.dismiss();
            return;
        }
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

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignUpActivity.this, "Registration was successful",
                                    Toast.LENGTH_SHORT).show();
                            signInEmail(actvEmail.getText().toString(), etPasswordSignUp.getText().toString());
                        } else {
                            progressBar.dismiss();
                            Exception err = task.getException();
                            Toast.makeText(SignUpActivity.this, err.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
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

    private void signInEmail(String email, String password) {
        if (!validateForm()) {
            progressBar.dismiss();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            progressBar.dismiss();
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressBar.dismiss();
                            Exception err = task.getException();
                        }
                    }
                });
    }

}
