package com.awesomecorp.sammy.taproutemetro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.awesomecorp.sammy.taproutemetro.Camera.HiddenCameraFragment;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScannerActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener{

    private HiddenCameraFragment mHiddenCameraFragment;
    private QRCodeReaderView qrCodeReaderView;
    DocumentReference documentReference;
    SharedPreferences preferences;
    RelativeLayout layout;
    TextView result;
    double fareTotal=1000;
    boolean flashOn=false;
    CardView cardView;
    CardView cardText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        qrCodeReaderView = findViewById(R.id.qrdecoderview);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        cardView= findViewById(R.id.overlay);
        layout=findViewById(R.id.lineRed);
        result= findViewById(R.id.result1);
        cardText= findViewById(R.id.overlay2);

        final ImageView flash=findViewById(R.id.imageView3);
        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flashOn) {
                    qrCodeReaderView.setTorchEnabled(false);
                    flash.setImageResource(R.drawable.flash);
                }
                else {
                    qrCodeReaderView.setTorchEnabled(true);
                    flash.setImageResource(R.drawable.flashoff);
                }
                flashOn= !flashOn;
            }
        });
        preferences=getApplicationContext().getSharedPreferences("Logged",MODE_PRIVATE);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*cardView.setVisibility(View.INVISIBLE);
                layout.setVisibility(View.VISIBLE);
                qrCodeReaderView.setQRDecodingEnabled(true);*/
            }
        });
        configureCamera();
    }

    void configureCamera(){
        qrCodeReaderView.setQRDecodingEnabled(true);
        qrCodeReaderView.setAutofocusInterval(2000L);
        qrCodeReaderView.setBackCamera();
    }


    void fetchData() {

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Log.e("Here", "Doc Exists");
                    Toast.makeText(getApplicationContext(), "Journey found", Toast.LENGTH_LONG)
                            .show();
                    boolean firstScan=false;
                    final String userid = documentSnapshot.getString("userid");
                    String lastUID = preferences.getString("lastUID","");
                    if (!lastUID.equals(userid)){
                        SharedPreferences.Editor editor=preferences.edit();
                        editor.putString("lastUID",userid);
                        editor.commit();
                        firstScan=true;
                    }
                    else{
                        firstScan=false;
                        qrCodeReaderView.setQRDecodingEnabled(true);
                    }

                    Log.e("Userid", userid);

                    if (firstScan){
                        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users")
                                .document(userid);

                        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String nameOfUser = documentSnapshot.getString("name");
                                fareTotal=documentSnapshot.getDouble("balance");
                                Log.e("UserRef", "Success");
                                if (nameOfUser == null) {
                                    nameOfUser = "Hello User";
                                } else {
                                    nameOfUser = "Hello " + nameOfUser;
                                }
                                Log.e("User Name", nameOfUser);
                                result.setText(nameOfUser);
                                qrCodeReaderView.setQRDecodingEnabled(true);
                                displayOverlay(userid,nameOfUser);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                result.setText("Hello User");
                                qrCodeReaderView.setQRDecodingEnabled(true);
                                displayOverlay(userid,"User");
                            }
                        });
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Already scanned.",Toast.LENGTH_LONG).show();
                    }


                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                qrCodeReaderView.setQRDecodingEnabled(true);
            }
        });



    }

    void checkEntryExit(final String uid, String name){

        String status=preferences.getString("status","Entry");
        String station=preferences.getString("station","Station 1");
        final String stationNumber=preferences.getString("number","1");

        Map<String,String> data=new HashMap<>();
        CollectionReference metroTrip=FirebaseFirestore.getInstance().collection("metro_trip");
        if (status.equals(Constants.status[0])){

            Toast.makeText(getApplicationContext()," Welcome to "
                    + station + ", " + name,Toast.LENGTH_SHORT).show();

            data.put("start",stationNumber);

            metroTrip.document(uid).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Here", "DocumentSnapshot successfully written!");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("Here", "Error writing document", e);
                }
            });
        }
        if (status.equals(Constants.status[1])){

            Toast.makeText(getApplicationContext()," Hope you had a good ride till "
                    + station + ", " + name,Toast.LENGTH_SHORT).show();

            metroTrip.document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(final DocumentSnapshot documentSnapshot) {
                    String query="to";
                    String x=documentSnapshot.getString("start");
                    double starting= Double.parseDouble(x);
                    double ending= Double.parseDouble(stationNumber);
                    if(ending>starting){
                        query= x+query+stationNumber;
                    }else {
                        query= stationNumber+query+x;
                    }

                    CollectionReference fares=FirebaseFirestore.getInstance().collection("metro_fare");
                    Log.e("query",query);
                    Query reqdFares= fares.whereEqualTo("id",query);
                    reqdFares.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            List<DocumentSnapshot> documentSnapshotList
                                    =documentSnapshots.getDocuments();

                            for (DocumentSnapshot documentSnapshot1:documentSnapshotList) {
                                final double fareLeft = fareTotal - documentSnapshot1.getDouble("fare");
                                Log.e("Fare Left",fareLeft+"");
                                DocumentReference userRef = FirebaseFirestore.getInstance().collection("users")
                                        .document(uid);
                                userRef.update("balance", fareLeft).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.e("Success", "Updated balance");
                                        Toast.makeText(getApplicationContext(),
                                                "Updated balance: " + fareLeft, Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("Here", "Error writing document", e);
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Here", "Error writing document", e);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("Here", "Error writing document", e);
                }
            });
        }



    }


    void displayOverlay(String userid, String username){
        // result.setText(preferences.getString("UID","None"));
        cardView.setVisibility(View.VISIBLE);
        layout.setVisibility(View.GONE);
        cardText.setVisibility(View.VISIBLE);

        int SPLASH_TIME_OUT = 3000;
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                cardView.setVisibility(View.GONE);
                cardText.setVisibility(View.INVISIBLE);
                layout.setVisibility(View.VISIBLE);
                qrCodeReaderView.setQRDecodingEnabled(true);
            }
        }, SPLASH_TIME_OUT);

        checkEntryExit(userid,username);
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        Log.e("Read", text);
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,200);
        //Toast.makeText(getApplicationContext(),"QR code scanned",Toast.LENGTH_LONG).show();
        startCamera();



        try {
            documentReference= FirebaseFirestore.getInstance().document("journey/"+text);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (documentReference==null){
            documentReference= FirebaseFirestore.getInstance().document("journey/anything");
        }

        result.setText(text);
        fetchData();
    }


    private void startCamera(){
        Intent Cn = new Intent(ScannerActivity.this , CameraActivity.class);
        startActivity(Cn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrCodeReaderView.startCamera();
        qrCodeReaderView.setQRDecodingEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrCodeReaderView.stopCamera();
    }
}
