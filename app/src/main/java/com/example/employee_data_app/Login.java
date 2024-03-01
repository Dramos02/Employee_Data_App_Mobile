package com.example.employee_data_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    // Views
    private EditText mEmail, mPassword;
    private Button mLoginBtn;
    private TextView mCreateBtn, forgetTextLink;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Views
        initView();

        // Firebase Initialization
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Click Listeners
        setClickListeners();
    }

    // Initialize Views
    private void initView() {
        mEmail = findViewById(R.id.Email);
        mPassword = findViewById(R.id.password);
        mLoginBtn = findViewById(R.id.loginBtn);
        mCreateBtn = findViewById(R.id.createTxt);
        progressBar = findViewById(R.id.progressBarLogin);
        forgetTextLink = findViewById(R.id.forgotPassword);

        ImageView imageViewShowHidePwd = findViewById(R.id.imageView_show_Login_hide_pwd);
        imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);

        imageViewShowHidePwd.setOnClickListener(v -> togglePasswordVisibility(mPassword, imageViewShowHidePwd));
    }

    // Set Click Listeners
    private void setClickListeners() {
        mLoginBtn.setOnClickListener(v -> loginUser());
        mCreateBtn.setOnClickListener(v -> goToRegister());
        forgetTextLink.setOnClickListener(v -> showResetPasswordDialog());
    }

    // Login User
    private void loginUser() {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (validateInput(email, password)) {
            progressBar.setVisibility(View.VISIBLE);
            authenticateUser(email, password);
        }
    }

    // Validate Input
    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            showError(mEmail, "Please enter your Email");
            return false;
        } else if (!isValidEmail(email)) {
            showError(mEmail, "Please enter a valid email address");
            return false;
        } else if (password.isEmpty()) {
            showError(mPassword, "Please enter your Password");
            return false;
        }
        return true;
    }

    // Authenticate User
    private void authenticateUser(String email, String password) {
        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = fAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(Login.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                            checkUserAccessLevel(user.getUid());
                            resetFields();
                        } else {
                            showVerificationDialog();
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(Login.this, "Error !" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void showVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email Verification");
        builder.setMessage("Please verify your email to access your profile. A verification link has been sent to your email.");

        // Add the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Go to Register Activity
    private void goToRegister() {
        startActivity(new Intent(getApplicationContext(), Register.class));
    }

    // Show Reset Password Dialog
    private void showResetPasswordDialog() {
        EditText resetMail = new EditText(this);
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setTitle("Reset Password ?");
        passwordResetDialog.setMessage("Enter Your Email to Receive Reset Link.");
        passwordResetDialog.setView(resetMail);

        passwordResetDialog.setPositiveButton("Yes", (dialog, which) -> {
            String mail = resetMail.getText().toString();
            sendResetPasswordEmail(mail);
        });

        passwordResetDialog.setNegativeButton("No", (dialog, which) -> {});

        passwordResetDialog.create().show();
    }

    // Send Reset Password Email
    private void sendResetPasswordEmail(String email) {
        fAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> Toast.makeText(Login.this, "Reset Link Sent to Your Email", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Login.this, "Error! Reset Link is Not Sent" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Toggle Password Visibility
    private void togglePasswordVisibility(EditText editText, ImageView imageView) {
        if (editText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_show_pwd);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_hide_pwd);
        }
    }

    // Validate Email Format
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Show Error Message
    private void showError(EditText editText, String errorMessage) {
        editText.setError(errorMessage);
        editText.requestFocus();
    }

    // Reset Fields
    private void resetFields() {
        mEmail.setText("");
        mPassword.setText("");
    }

    // Check User Access Level
    private void checkUserAccessLevel(String uid) {
        DocumentReference df = fStore.collection("users").document(uid);
        df.get().addOnSuccessListener(documentSnapshot -> {
            Log.d("TAG", "onSuccess:" + documentSnapshot.getData());

            if (documentSnapshot.getString("isAdmin") != null) {
                startActivity(new Intent(getApplicationContext(), AdminActivity.class));
                finish();
            }

            if (documentSnapshot.getString("isUser") != null) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }
}
