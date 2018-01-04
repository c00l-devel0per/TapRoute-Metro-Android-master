package com.awesomecorp.sammy.taproutemetro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {


    private FirebaseAuth auth;
    Activity activity;
    String TAG="Splash Login";
    private final int CAMERA_REQUEST_CODE=200;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        preferences=getApplicationContext().getSharedPreferences("Logged",MODE_PRIVATE);
        activity=this;
        auth = FirebaseAuth.getInstance();
        getPerms(activity);
    }

    void checkLogin(){
        boolean loggedIn=preferences.getBoolean("logged",false);
        Intent intent;
        if (loggedIn){
            Toast.makeText(activity,"Logged In",Toast.LENGTH_SHORT).show();
            String username= preferences.getString("username"," ");
            String password=preferences.getString("password"," ");
            logIn(username,password);
        }else {
                intent = new Intent(activity, LoginActivity.class);
                startActivity(intent);
        }
    }

    void getPerms(Activity activity){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

            }
            else {
                checkLogin();
            }
        }
        else{
            checkLogin();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==CAMERA_REQUEST_CODE){
            if (grantResults.length>0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permissions granted", Toast.LENGTH_SHORT).show();
                    checkLogin();
                } else {
                    Toast.makeText(getApplicationContext(), "Permissions not granted", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    void logIn(String username,String password){
        auth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            Intent intent;
                            intent=new Intent(activity,ScannerActivity.class);
                            startActivity(intent);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(activity,"Login Failed. Check internet connection",Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });

    }
}
