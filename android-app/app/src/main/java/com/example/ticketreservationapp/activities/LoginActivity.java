package com.example.ticketreservationapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.api.ApiService;
import com.example.ticketreservationapp.api.RetrofitClient;
import com.example.ticketreservationapp.model.LoginRequest;
import com.example.ticketreservationapp.model.LoginResponse;
import com.example.ticketreservationapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private EditText etIdentifier, etPassword;
    private Button btnLogin, btnGoToRegister;
    private TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etIdentifier = findViewById(R.id.etIdentifier);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        tvMessage = findViewById(R.id.tvMessage);

        btnLogin.setOnClickListener(v -> loginUser());

        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String identifier = etIdentifier.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (identifier.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        tvMessage.setText("Logging in...");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        LoginRequest loginRequest = new LoginRequest(identifier, password);

        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    SessionManager sessionManager = new SessionManager(LoginActivity.this);
                    sessionManager.saveSession(loginResponse.getUserId(), loginResponse.getRole());
                    tvMessage.setText(
                            loginResponse.getMessage() != null
                                    ? loginResponse.getMessage()
                                    : "Login successful"
                    );

                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    String role = loginResponse.getRole();

                    Intent intent;
                    if (role != null && ("ORGANIZER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role))) {
                        intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, HomeActivity.class);
                    }

                    startActivity(intent);
                    finish();
                } else {
                    tvMessage.setText("Login failed. Check your credentials.");
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                tvMessage.setText("Could not connect to backend.");
                Toast.makeText(LoginActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}