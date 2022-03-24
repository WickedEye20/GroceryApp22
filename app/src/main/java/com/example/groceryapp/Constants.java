package com.example.groceryapp;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class Constants{



    public static final String FCM_KEY = "AAAANXy2Mdg:APA91bHHc9DG8jITsHHNBfvzXi1AHF26xhVG8rPYBlf-Li_FnsER5Snhk77RCLU9mgGOTDSJ3JH3ZubVbRi0FD3fL2oJH9mPy7o3CMYDuXxg0wFUiXkJLuXxgdKSYQyTgDOLEVZA37ok";
    public static final String FCM_TOPIC = "PUSH_NOTIFICATIONS";

    public static final String[] productCategories = {
      "Beverages", "Beauty and Personal Care",
      "Baby Kid", "Biscuits Snacks & Chocolates",
      "Breakfast & Dairy", "Cooking Needs",
      "Frozen Food", "Fruits", "Pet Care", "Pharmacy",
      "Vegetables", "Others"
    };


    public static final String[] productCategories1 = {
            "All", "Beverages", "Beauty and Personal Care",
            "Baby Products", "Biscuits Snacks & Chocolates",
            "Breakfast & Dairy", "Cooking Needs",
            "Frozen Food", "Fruits", "Pet Care", "Pharmacy",
            "Vegetables", "Others"
    };
}
