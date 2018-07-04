package hackeru.talg.edu.androidproject;

import android.content.Intent;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

public class LoginActivity extends AppCompatActivity{
    private FirebaseAuth mAuth;
    private static final String TAG_EMAIL = "EmailPassword";
    private static final String TAG_GOOGLE = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final int NOT_LOGGED_IN = 0;
    private static final int LOGGED_IN_EMAIL = 1;
    private static final int LOGGED_IN_GOOGLE = 2;


    private AutoCompleteTextView actvEmailLogin;
    private EditText etPasswordLogin;
    private Button btnLoginEmail;
    private Button btnLoginGoogle;
    private TextView tvLoginStatus;
    private GoogleSignInClient mGoogleSignInClient;

    private static int loggedInWith = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        actvEmailLogin = findViewById(R.id.actvEmailLogin);
        etPasswordLogin = findViewById(R.id.etPasswordLogin);
        btnLoginEmail = findViewById(R.id.btnLoginEmail);
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
        tvLoginStatus = findViewById(R.id.tvLoginStatus);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        updateUI(mAuth.getCurrentUser());

        btnLoginEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loggedInWith != NOT_LOGGED_IN) {
                    if (loggedInWith != LOGGED_IN_EMAIL) {
                        return;
                    } else {
                        signOut();
                    }
                } else {
                    signInEmail(actvEmailLogin.getText().toString(), etPasswordLogin.getText().toString());
                }
            }
        });

        btnLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loggedInWith != NOT_LOGGED_IN) {
                    if (loggedInWith != LOGGED_IN_GOOGLE) {
                        return;
                    } else {
                        signOut();
                    }
                } else {
                    signInGoogle();
                }
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
        if (id == R.id.action_login) {
            return true;
        } else if (id == R.id.action_sms) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_sign_up) {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed()
    {
        finish();
    }

    private void signInEmail(String email, String password) {
        Log.d(TAG_EMAIL, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG_EMAIL, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            loggedInWith = LOGGED_IN_EMAIL;
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG_EMAIL, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            //mStatusTextView.setText(R.string.auth_failed);
                        }
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = actvEmailLogin.getText().toString();
        if (TextUtils.isEmpty(email)) {
            actvEmailLogin.setError("Required.");
            valid = false;
        } else {
            actvEmailLogin.setError(null);
        }

        String password = etPasswordLogin.getText().toString();
        if (TextUtils.isEmpty(password)) {
            etPasswordLogin.setError("Required.");
            valid = false;
        } else {
            etPasswordLogin.setError(null);
        }

        return valid;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG_GOOGLE, "Google sign in failed", e);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG_GOOGLE, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        //showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG_GOOGLE, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            loggedInWith = LOGGED_IN_GOOGLE;
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG_GOOGLE, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        // [START_EXCLUDE]
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            tvLoginStatus.setText("Logged in");
            changeButtonsToLoggedInMode();
        } else {
            loggedInWith = NOT_LOGGED_IN;
            tvLoginStatus.setText("Logged out");
            changeButtonsToLoggedOutMode();
        }
    }

    private void changeButtonsToLoggedInMode() {
            switch (loggedInWith) {
                case LOGGED_IN_EMAIL:
                    btnLoginEmail.setText("Logout");
                    btnLoginGoogle.setText("Already logged in");
                    btnLoginGoogle.setBackgroundColor(Color.GRAY);
                    break;
                case LOGGED_IN_GOOGLE:
                    btnLoginGoogle.setText("Logout");
                    btnLoginEmail.setText("Already logged in");
                    btnLoginEmail.setBackgroundColor(Color.GRAY);
                    break;
            }
    }

    private void changeButtonsToLoggedOutMode() {
        int colorEmailButton =  Color.parseColor("#43C4A4");
        int colorGoogleButton =  Color.parseColor("#CF021B");
        btnLoginEmail.setText("Sign in with email");
        btnLoginEmail.setBackgroundColor(colorEmailButton);
        btnLoginGoogle.setText("Sign in with google");
        btnLoginGoogle.setBackgroundColor(colorGoogleButton);
    }

    private void signOut() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        for (UserInfo user: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if (user.getProviderId().equals("password")) {
                mAuth.signOut();
                updateUI(null);
            } else if (user.getProviderId().equals("google.com")) {
                signOutGoogle();
            }
        }
        Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
    }


    private void signOutGoogle() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }
}




