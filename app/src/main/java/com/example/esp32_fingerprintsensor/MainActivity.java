package com.example.esp32_fingerprintsensor;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getName();
    private BiometricPrompt myBiometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupUI();
        setupBiometricPrompt();
        setupButtonClickListener();
    }

    private void setupUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupBiometricPrompt() {
        Executor newExecutor = Executors.newSingleThreadExecutor();
        FragmentActivity activity = this;

        myBiometricPrompt = new BiometricPrompt(activity, newExecutor, new BiometricPrompt.AuthenticationCallback() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.e(TAG, "Authentication error: " + errString);
                showMessage("Authentication error: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Fingerprint recognised successfully");
                String ip = ((TextView) findViewById(R.id.IP_Address)).getText().toString();
                sendMessage(ip, "MakerTutor\n");
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.d(TAG, "Fingerprint not recognised");
                showMessage("Fingerprint not recognised");
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Scan your fingerprint to unlock.")
                .setSubtitle("If you use face unlock, it can be done as well")
                .setDescription("Maker Tutor Arduino/ESP32 Project Unlock door with Fingerprint sensor from Android ")
                .setNegativeButtonText("Cancel")
                .build();
    }

    private void setupButtonClickListener() {
        findViewById(R.id.button).setOnClickListener(v -> {
            TextView iptxt = findViewById(R.id.IP_Address);
            if (isValidIP(iptxt.getText().toString())) {
                myBiometricPrompt.authenticate(promptInfo);
                Log.d(TAG, "Test send data to esp32");
            } else {
                showInvalidIpDialog();
            }
        });
    }

    private boolean isValidIP(String ip) {
        String regex = "^((25[0-5])|(2[0-4]\\d)|(1\\d\\d)|([1-9]\\d)|\\d)(\\.((25[0-5])|(2[0-4]\\d)|(1\\d\\d)|([1-9]\\d)|\\d)){3}$";
        return ip.matches(regex);
    }

    private void showInvalidIpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setMessage("Enter a valid ESP32 IP address.");
        builder.setPositiveButton("OK", (dialog, id) -> {
            // Do something
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showMessage(String message) {
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(() -> {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        });
    }

    private void sendMessage(final String ip, final String msg) {
        Runnable runSend = () -> {
            try (Socket s = new Socket(ip, 80);
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

                out.write(msg);
                out.flush();
                Log.i("Sender", msg);
                showMessage("Message has been sent.");
            } catch (UnknownHostException e) {
                Log.e(TAG, "No device found on this IP address.", e);
                showMessage("No device found on this IP address.");
            } catch (Exception e) {
                Log.e(TAG, "Connection failed. Please try again.", e);
                showMessage("Connection failed. Please try again.");
            }
        };
        new Thread(runSend).start();
    }
}
