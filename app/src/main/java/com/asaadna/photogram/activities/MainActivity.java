package com.asaadna.photogram.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.asaadna.photogram.R;
import com.asaadna.photogram.fragments.CameraFragment;
import com.asaadna.photogram.fragments.HomeFragment;
import com.asaadna.photogram.fragments.PendingFragment;
import com.asaadna.photogram.fragments.ProfileFragment;
import com.asaadna.photogram.fragments.SearchFragment;
import com.asaadna.photogram.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
            //
        }

        //LOGOUT

        TextView titleView = findViewById(R.id.titleTextView);
        //Title View is our exit
        titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish(); //exit the app and do not go back to the login activity..
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch(item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_search:
                    selectedFragment = new SearchFragment();
                    break;
                case R.id.nav_pending:
                    selectedFragment = new PendingFragment();
                    break;
                case R.id.nav_profile:
                    selectedFragment = new ProfileFragment();
                    break;
                case R.id.nav_camera:
                    selectedFragment = new CameraFragment();
                    break;
            }

            assert selectedFragment != null;
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();
            return true;
        }
    };
}