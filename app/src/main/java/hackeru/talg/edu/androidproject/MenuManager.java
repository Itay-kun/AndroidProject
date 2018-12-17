package hackeru.talg.edu.androidproject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

public class MenuManager {
    public static void signOut(Context context) {
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            Toast.makeText(context, "You are not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        for (UserInfo user: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if (user.getProviderId().equals("password")) {
                mAuth.signOut();
            } else if (user.getProviderId().equals("google.com")) {
                signOutGoogle(context);
            }
        }
        Toast.makeText(context, "Logged Out", Toast.LENGTH_SHORT).show();
    }


    private static void signOutGoogle(Context context) {
        // Firebase sign out
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut();
    }
}
