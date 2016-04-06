package com.projetinfomobile;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Map;
import java.util.regex.Pattern;

import Model.DatabaseInterface;
import Model.UserData;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    private EditText mUsernameEntry;
    private EditText mPasswordEntry;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Firebase.setAndroidContext(this);

        // Puts an email from your contact list in the email field
        String possibleEmail = "";
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                possibleEmail = account.name;
                break;
            }
        }

        // Set up the login form.
        mUsernameEntry = (EditText) findViewById(R.id.username);
        mUsernameEntry.setText(possibleEmail, TextView.BufferType.EDITABLE);

        mPasswordEntry = (EditText) findViewById(R.id.password);
        mPasswordEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onResume(){
        super.onResume();
        showProgress(false);
        mUsernameEntry.requestFocus();
        DatabaseInterface.Instance().Logout();
    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mUsernameEntry.setError(null);
        mPasswordEntry.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameEntry.getText().toString();
        String password = mPasswordEntry.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordEntry.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordEntry;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameEntry.setError(getString(R.string.error_field_required));
            focusView = mUsernameEntry;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            DatabaseInterface.Instance().LoginWithPassword(mUsernameEntry.getText().toString(), mPasswordEntry.getText().toString(), authResultHandler, userCreationHandler);
        }
    }

    // Handles login results
    Firebase.AuthResultHandler authResultHandler = new Firebase.AuthResultHandler() {
        @Override
        public void onAuthenticated(AuthData authData) {
            // Go to main activity if we logged in
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            // Show the error to the user if there is one
            ShowSnackBar(firebaseError.getMessage());
            showProgress(false);
            switch (firebaseError.getCode()){
                case  FirebaseError.INVALID_EMAIL:
                    mUsernameEntry.setError("Invalid email");
                    mUsernameEntry.requestFocus();
                case FirebaseError.INVALID_PASSWORD:
                    mPasswordEntry.setError("Invalid password");
                    mPasswordEntry.requestFocus();
                    break;
                default:
                    break;
            }
        }
    };

    // Handles signin result
    Firebase.ValueResultHandler<Map<String, Object>> userCreationHandler = new Firebase.ValueResultHandler<Map<String, Object>>() {
        @Override
        public void onSuccess(Map<String, Object> result) {
            // Null if we have to complete the users info
            if(result == null){
                Intent intent = new Intent(LoginActivity.this, UserInfoCompletionActivity.class);
                startActivity(intent);
            }
        }
        @Override
        public void onError(FirebaseError firebaseError) {
            ShowSnackBar(firebaseError.getMessage());
            showProgress(false);
            switch (firebaseError.getCode()){
                case FirebaseError.EMAIL_TAKEN:
                    mUsernameEntry.setError("This email address is already registered");
                    mUsernameEntry.requestFocus();
                    break;
                case FirebaseError.INVALID_EMAIL:
                    mUsernameEntry.setError("This email address is invalid");
                    mUsernameEntry.requestFocus();
                    break;
                default:
                    break;
            }
        }
    };

    public void ShowSnackBar(String message){
        Snackbar.make(findViewById(R.id.login_activity_layout), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

