package com.example.employee_data_app;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    // Fields
    private EditText mFullName, mEmail, mPassword, mConfirmPwd, mPhone;
    private Button mRegisterBtn;
    private TextView mLoginBtn;
    private ProgressBar progressBar;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // View binding
        initView();

        // Firebase initialization
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Click listeners
        setClickListeners();
    }

    // View binding
    private void initView() {
        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.Email);
        mPassword = findViewById(R.id.password);
        mConfirmPwd = findViewById(R.id.confirm_password);
        mPhone = findViewById(R.id.phone);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mLoginBtn = findViewById(R.id.createTxt);
        progressBar = findViewById(R.id.progressBar);

        ImageView imageViewShowHidePwd = findViewById(R.id.imageView_show_hide_pwd);
        imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHidePwd.setOnClickListener(v -> togglePasswordVisibility(mPassword, imageViewShowHidePwd));

        ImageView imageViewShowHideConfirmPwd = findViewById(R.id.imageView_show_hide_confirm_pwd);
        imageViewShowHideConfirmPwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHideConfirmPwd.setOnClickListener(v -> togglePasswordVisibility(mConfirmPwd, imageViewShowHideConfirmPwd));
    }

    // Click listeners
    private void setClickListeners() {
        mRegisterBtn.setOnClickListener(v -> registerUser());
        mLoginBtn.setOnClickListener(v -> goToLogin());
    }

    // Register user
    private void registerUser() {
        // Validation
        if (validateInput()) {
            progressBar.setVisibility(View.VISIBLE);
            createUser();
        }
    }

    // Go to login activity
    private void goToLogin() {
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    // Validation
    private boolean validateInput() {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String fullName = mFullName.getText().toString().trim();
        String phone = mPhone.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            showError(mFullName, "Please enter your Full Name");
            return false;
        } else if (TextUtils.isEmpty(email)) {
            showError(mEmail, "Please enter your Email");
            return false;
        } else if (!isValidEmail(email)) {
            showError(mEmail, "Please enter a valid email address");
            return false;
        } else if (TextUtils.isEmpty(phone)) {
            showError(mPhone, "Mobile No. is required");
            return false;
        } else if (!isValidMobile(phone)) {
            showError(mPhone, "Mobile No. should start 09 with 11 digits ex. 09XXXXXXX");
            return false;
        } else if (!validatePassword(password)) {
            return false;
        } else if (!password.equals(mConfirmPwd.getText().toString().trim())) {
            showError(mConfirmPwd, "Password Confirmation does not match");
            return false;
        }
        return true;
    }

    // Create user in Firebase
    private void createUser() {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String fullName = mFullName.getText().toString().trim();
        String phone = mPhone.getText().toString().trim();

        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sendVerificationEmail();
                        saveUserData(fullName, email, phone);
                        goToLogin();
                    } else {
                        Toast.makeText(Register.this, "Error !" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    // Send email verification
    private void sendVerificationEmail() {
        FirebaseUser fuser = fAuth.getCurrentUser();
        fuser.sendEmailVerification()
                .addOnSuccessListener(aVoid -> Toast.makeText(Register.this, "Verification Email Has been Sent.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Log.d(TAG, "onFailure: Verification Link is not Sent." + e.getMessage()));
    }

    // Save user data in Firestore
    private void saveUserData(String fullName, String email, String phone) {
        userID = fAuth.getCurrentUser().getUid();
        DocumentReference documentReference = fStore.collection("users").document(userID);
        Map<String, Object> user = new HashMap<>();
        user.put("fName", fullName);
        user.put("email", email);
        user.put("phone", phone);
        user.put("isUser", "1");
        documentReference.set(user).addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: user Profile is created for" + userID));
    }

    // Show error message for EditText
    private void showError(EditText editText, String message) {
        editText.setError(message);
    }

    // Validate email format
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    // Validate mobile number format
    private boolean isValidMobile(String mobile) {
        String mobilePattern = "09\\d{9}";
        return mobile.matches(mobilePattern);
    }

    // Validation for password
    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            showError(mPassword, "Password is required");
            return false;
        } else if (password.length() < 6) {
            showError(mPassword, "Password should be at least 6 characters long");
            return false;
        }
        return true;
    }

    // Toggle password visibility
    private void togglePasswordVisibility(EditText editText, ImageView imageView) {
        if (editText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_show_pwd);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_hide_pwd);
        }
    }
}
