package com.awesomecorp.sammy.taproutemetro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    //taproutemetro
    private FirebaseAuth auth;
    Activity activity;

    String TAG="Login Activity";
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    ProgressBar progressBar;
    String station;
    String stationNumber;
    String Status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        activity=this;
        preferences=getApplicationContext().getSharedPreferences("Logged",MODE_PRIVATE);
        editor=preferences.edit();

        auth=FirebaseAuth.getInstance();

        progressBar=findViewById(R.id.loginProgress);

        Spinner metros = findViewById(R.id.spinner);
        Spinner status = findViewById(R.id.spinner2);

        metros.setOnItemSelectedListener(this);
        status.setOnItemSelectedListener(this);

        ArrayAdapter m = new ArrayAdapter(this,android.R.layout.simple_spinner_item,
                                                                Constants.metroStations);
        m.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        metros.setAdapter(m);

        ArrayAdapter s = new ArrayAdapter(this,android.R.layout.simple_spinner_item,
                Constants.status);
        s.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        status.setAdapter(s);


        TextView login = findViewById(R.id.buttonLogin);

        final EditText password = findViewById(R.id.password);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass=password.getText().toString().trim();
                progressBar.setVisibility(View.VISIBLE);
                logIn("metroadmin@gmail.com",pass);
            }
        });
    }



    void logIn(final String username, final String password){

        auth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            editor.putString("username",username);
                            editor.putString("password",password);
                            editor.putString("status",Status);
                            editor.putString("station",station);
                            editor.putString("number",stationNumber);
                            editor.putBoolean("logged",true);
                            editor.commit();

                            Intent intent= new Intent(activity,ScannerActivity.class);
                            finish();
                            startActivity(intent);

                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(activity, "Login Failed. Check internet connection", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position,long id) {

        int spinnerId = arg0.getId();
        switch (spinnerId) {
            case R.id.spinner:
                stationNumber="" + (position+1);
                station=Constants.metroStations[position];
                Toast.makeText(getApplicationContext(), Constants.metroStations[position],
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.spinner2:
                Status=Constants.status[position];
                Toast.makeText(getApplicationContext(), Constants.status[position],
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        int spinnerId= arg0.getId();
        switch (spinnerId){
            case R.id.spinner:
                stationNumber="1";
                station=Constants.metroStations[0];
                break;

            case R.id.spinner2:
                Status=Constants.status[0];
                break;
        }
    }
}
