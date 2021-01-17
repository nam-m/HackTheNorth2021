package com.example.hackthenorth2021;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.GridView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    GridLayout cardHabitat_for_humanity;
    GridLayout cardOasis_clothing_bank;
    GridLayout cardThe_Salvation_Army;
    GridLayout cardFred_Victor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        cardHabitat_for_humanity = findViewById(R.id.cardHabit_for_humanity);
        cardOasis_clothing_bank =  findViewById(R.id.cardOasis_clothing_bank);
        cardThe_Salvation_Army = findViewById(R.id.cardThe_Salvation_Army);
        cardFred_Victor = findViewById(R.id.cardFred_Victor);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_bar);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.search:
                        startActivity(new Intent(getApplicationContext(), LocationActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.home:
                        return true;
                }
                return true;
            }
        });
    }
}