package in.neonex.jauntbee.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import in.neonex.jauntbee.R;

public class TestActivity extends Activity {
    private Button mButton,mNext;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mButton = findViewById(R.id.signOut);
        mNext = findViewById(R.id.next);

        mAuth = FirebaseAuth.getInstance();
        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() == null){

                    Intent intent  = new Intent(TestActivity.this,LoginActivity.class);
                    startActivity(intent);
                }

            }
        };
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
            }
        });
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(TestActivity.this,DestinationActivity.class);
                startActivity(intent);
            }
        });
    }
}
