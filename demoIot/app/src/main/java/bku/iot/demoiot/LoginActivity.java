package bku.iot.demoiot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText serverEditText;
    private EditText keyEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        serverEditText = findViewById(R.id.Server);
        keyEditText = findViewById(R.id.Key);
        loginButton = findViewById(R.id.btnLogin);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String server = serverEditText.getText().toString();
                String key = keyEditText.getText().toString();

                if (validateInput(server, key)) {
                    saveCredentials(server, key);
                    navigateToMainInterface();
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid server or incorrect password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateInput(String server, String key) {
        // Add your validation logic here
        if (server.equals("Jun_nior")) {
            return true;
        }
        return true;
    }

    private void saveCredentials(String server, String key) {
        SharedPreferences sharedPreferences = getSharedPreferences("IoTPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Server", server);
        editor.putString("Key", key);
        editor.apply();
    }

    private void navigateToMainInterface() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}