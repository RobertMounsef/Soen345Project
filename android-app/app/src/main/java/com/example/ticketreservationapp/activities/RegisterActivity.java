package com.example.ticketreservationapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.api.ApiService;
import com.example.ticketreservationapp.api.RetrofitClient;
import com.example.ticketreservationapp.model.RegisterRequest;
import com.example.ticketreservationapp.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    private EditText etName, etEmail, etPhone, etRegisterPassword;
    private CheckBox cbOrganizerRole;
    private Button btnRegister, btnBackToLogin;
    private TextView tvRegisterMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        cbOrganizerRole = findViewById(R.id.cbOrganizerRole);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        tvRegisterMessage = findViewById(R.id.tvRegisterMessage);

        btnRegister.setOnClickListener(v -> registerUser());

        btnBackToLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etRegisterPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Name, email, and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        tvRegisterMessage.setText("Creating account...");

        String role = cbOrganizerRole.isChecked() ? "ORGANIZER" : "CUSTOMER";

        RegisterRequest registerRequest =
                new RegisterRequest(name, email, phone, password, role);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.register(registerRequest).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                btnRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    tvRegisterMessage.setText("Account created successfully. You can now log in.");
                    Toast.makeText(RegisterActivity.this,
                            "Registration successful",
                            Toast.LENGTH_SHORT).show();

                    finish();
                } else {
                    tvRegisterMessage.setText("Registration failed.");
                    Toast.makeText(RegisterActivity.this,
                            "Registration failed",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                tvRegisterMessage.setText("Could not connect to backend.");
                Toast.makeText(RegisterActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}