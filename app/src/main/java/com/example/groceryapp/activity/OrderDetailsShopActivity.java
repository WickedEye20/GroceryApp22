package com.example.groceryapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.groceryapp.Constants;
import com.example.groceryapp.R;
import com.example.groceryapp.Singleton;
import com.example.groceryapp.adapters.AdapterOrderShop;
import com.example.groceryapp.adapters.AdapterOrderedItem;
import com.example.groceryapp.models.ModelOrderedItems;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetailsShopActivity extends AppCompatActivity {

    private String orderId, orderBy, myLatitude, myLongitude, buyerLatitude, buyerLongitude, deliveryFee, timing;

    private ImageButton backBtn, editStatusBtn, mapBtn;
    private TextView orderIdTv, dateTv, orderStatusTv, buyerEmailTv, buyerPhoneTv, totalItemTv, amountTv, deliveryAddressTv, paymentModeTv, deliveryTv, timingTv;
    private RecyclerView itemsRv;

    private ArrayList<ModelOrderedItems> orderedItemsArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    private FirebaseAuth mAuth;
    FirebaseInstallations firebaseInstallations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_shop);

        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");

        mAuth = FirebaseAuth.getInstance();

        backBtn = findViewById(R.id.backBtn);
        editStatusBtn = findViewById(R.id.editStatusBtn);
        mapBtn = findViewById(R.id.mapBtn);
        orderIdTv = findViewById(R.id.orderIdTV);
        paymentModeTv = findViewById(R.id.paymentMode);
        deliveryTv = findViewById(R.id.deliveryType);
        timingTv = findViewById(R.id.deliveryTime);
        dateTv = findViewById(R.id.dateTV);
        orderStatusTv = findViewById(R.id.orderStatusTV);
        buyerEmailTv = findViewById(R.id.buyerEmailTV);
        buyerPhoneTv = findViewById(R.id.buyerPhoneTV);
        totalItemTv = findViewById(R.id.totalItemTV);
        amountTv = findViewById(R.id.amountTV);
        deliveryAddressTv = findViewById(R.id.deliveryAddressTV);
        itemsRv = findViewById(R.id.itemsRV);

        loadMyInfo();
        loadBuyerInfo();
        loadOrdersdetail();
        loadOrdersItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        editStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editStatusDialog();
            }
        });
    }

    private void editStatusDialog() {
        final String[] options = {"In Progress", "Completed", "Cancelled"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Status")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedOption = options[which];
                        editOrderStatus(selectedOption);
                    }
                }).show();
    }

    private void editOrderStatus(final String selectedOption) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", "" + selectedOption);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String message = "Order is now " + selectedOption;
                        Toast.makeText(OrderDetailsShopActivity.this, message, Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(orderId, message);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OrderDetailsShopActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadOrdersdetail() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String orderBy = "" + dataSnapshot.child("orderBy").getValue();
                        String orderCost = "" + dataSnapshot.child("orderCost").getValue();
                        String orderId = "" + dataSnapshot.child("orderId").getValue();
                        String orderStatus = "" + dataSnapshot.child("orderStatus").getValue();
                        String orderTime = "" + dataSnapshot.child("orderTime").getValue();
                        String orderFrom = "" + dataSnapshot.child("orderFrom").getValue();
                        String paymentMode = "" + dataSnapshot.child("paymentMode").getValue();
                        String delivery = "" + dataSnapshot.child("Delivery").getValue();
                        String timing = "" + dataSnapshot.child("Time").getValue();


                        orderCost = orderCost.replaceAll("\\(.*[\\)]", "").replaceAll(" \\(", "").replaceAll("\\)", "");

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formateDate = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();

                        if (orderStatus.equals("In Progress")) {
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        } else if (orderStatus.equals("Completed")) {
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                        } else {
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed01));
                        }

                        orderIdTv.setText(orderId);
                        paymentModeTv.setText(paymentMode);
                        deliveryTv.setText(delivery);
                        timingTv.setText(timing);
                        orderStatusTv.setText(orderStatus);
                        double total = Double.parseDouble(orderCost);
                        amountTv.setText("₹" + total);
                        dateTv.setText(formateDate);

                        findAddress(buyerLatitude, buyerLongitude);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            deliveryAddressTv.setText(address + ", " + city + ", " + state + ", " + country);
        } catch (Exception e) {

        }
    }

    String totalItems;

    private void loadOrdersItems() {
        orderedItemsArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid()).child("Orders").child(orderId).child("items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        orderedItemsArrayList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelOrderedItems modelOrderedItems = ds.getValue(ModelOrderedItems.class);
                            orderedItemsArrayList.add(modelOrderedItems);
                        }
                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsShopActivity.this, orderedItemsArrayList);
                        itemsRv.setAdapter(adapterOrderedItem);
                        totalItems = "" + dataSnapshot.getChildrenCount();
                        totalItemTv.setText(totalItems);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        myLatitude = "" + dataSnapshot.child("latitude").getValue();
                        myLongitude = "" + dataSnapshot.child("longitude").getValue();
                        deliveryFee = "" + dataSnapshot.child("deliveryFee").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadBuyerInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        buyerLatitude = "" + dataSnapshot.child("latitude").getValue();
                        buyerLongitude = "" + dataSnapshot.child("longitude").getValue();
                        String email = "" + dataSnapshot.child("email").getValue();
                        String phone = "" + dataSnapshot.child("phone").getValue();

                        buyerEmailTv.setText(email);
                        buyerPhoneTv.setText(phone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void openMap() {
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + buyerLatitude + "," + buyerLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }





    private void prepareNotificationMessage(String orderId, String message) {


        //send message to user when order status changes
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC; //must be same as subscribed by user
        String NOTIFICATION_TITLE = "Your Order" + "" + orderId;
        String NOTIFICATION_MESSAGE = "" + message;
        String NOTIFICATION_TYPE = "OrderStatusChanged";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", orderBy);
            notificationBodyJo.put("sellerUid", mAuth.getUid());
            notificationBodyJo.put("orderId", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);

            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo);


            sendFcmNotification(notificationJo);

        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    private void sendFcmNotification(JSONObject notificationJo) {
        //send volley request

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DEPRECATED_GET_OR_POST, "https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after sending start order detail activity


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //failed start order detail activity


            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + Constants.FCM_KEY);

                return headers;
            }
        };

        //enque the volley request
//        Singleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}
