package com.example.groceryapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.groceryapp.Constants;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shreyaspatil.EasyUpiPayment.EasyUpiPayment;
import com.shreyaspatil.EasyUpiPayment.listener.PaymentStatusListener;
import com.shreyaspatil.EasyUpiPayment.model.TransactionDetails;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity implements PaymentStatusListener {

    private TextInputEditText upiEt, nameEt, noteEt;
    private Button payBtn, payBtnCash;
    private TextView amountTv, UserName, UserEmail, UserPhone, UserAddress;

    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;

    private String shopId, myLatitude, myLongitude, grandTotal, homeDelivery, timeData, username, email, address, phone;

    final int UPI_PAYMENT = 1;

    Date c = Calendar.getInstance().getTime();
    SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault());
    String transacId = df.format(c);

    final String timestamp = "" + System.currentTimeMillis();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        amountTv = findViewById(R.id.amountTV);
        UserName = findViewById(R.id.UserName);
        UserEmail = findViewById(R.id.UserEmail);
        UserPhone = findViewById(R.id.UserPhone);
        UserAddress = findViewById(R.id.UserAddress);
        upiEt = findViewById(R.id.upiET);
        nameEt = findViewById(R.id.nameET);
        noteEt = findViewById(R.id.noteET);
        payBtn = findViewById(R.id.payBtn);
        payBtnCash = findViewById(R.id.payBtnCash);




        username = getIntent().getStringExtra("UserName");
        phone = getIntent().getStringExtra("UserPhone");
        email = getIntent().getStringExtra("UserEmail");
        address = getIntent().getStringExtra("UserAddress");

        UserName.setText(username);
        UserEmail.setText(email);
        UserAddress.setText(address);
        UserPhone.setText(phone);

        homeDelivery = getIntent().getStringExtra("Home Delivery");
        timeData = getIntent().getStringExtra("dropdown");
        shopId = getIntent().getStringExtra("ShopId");
        myLatitude = getIntent().getStringExtra("Latitude");
        myLongitude = getIntent().getStringExtra("Longitude");
        grandTotal = getIntent().getStringExtra("GrandTotal");
        grandTotal = grandTotal.replace("₹", "").replaceAll("\\(.*?\\)", "").trim();
        amountTv.setText("₹" + grandTotal);
        Log.d("Amount", amountTv.getText() + "");

        mAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);



        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = grandTotal;
                String upiId = upiEt.getText().toString();
                String name = nameEt.getText().toString();
                String note = noteEt.getText().toString();

                if (TextUtils.isEmpty(upiId) && TextUtils.isEmpty(name) && TextUtils.isEmpty(note)) {
                    Toast.makeText(PaymentActivity.this, "Please enter all the details..", Toast.LENGTH_SHORT).show();
                }
                else {
                    payUsingUpi(amount, upiId, name, note, transacId);
                }
            }
        });

        payBtnCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.setMessage("Placing Order....");
                mProgressDialog.show();

                final String timestamp = "" + System.currentTimeMillis();
                String cost = grandTotal;

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("orderId", timestamp);
                hashMap.put("orderTime", timestamp);
                hashMap.put("orderStatus", "In Progress");
                hashMap.put("orderCost", cost);
                hashMap.put("orderBy", "" + mAuth.getUid());
                hashMap.put("OrderFrom", "" + shopId);
                hashMap.put("latitude", myLatitude);
                hashMap.put("longitude", myLongitude);
                hashMap.put("paymentMode", "Cash");
                hashMap.put("Delivery", homeDelivery);
                hashMap.put("Time", timeData);


                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopId).child("Orders");
                ref.child(timestamp).setValue(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getUid());
                                ref1.child("CartItem").child(shopId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String finalPrice = "" + ds.child("finalPrice").getValue();
                                            String productCategory = "" + ds.child("prductCategory").getValue();
                                            String ItemImage = "" + ds.child("profileImage").getValue();
                                            String quantity = "" + ds.child("quantity").getValue();
                                            String title = "" + ds.child("title").getValue();
                                            String pId = "" + ds.child("productId").getValue();

                                            HashMap<String, String> hashMap1 = new HashMap<>();
                                            hashMap1.put("finalPrice", finalPrice);
                                            hashMap1.put("productCategory", productCategory);
                                            hashMap1.put("ItemImage", ItemImage);
                                            hashMap1.put("quantity", quantity);
                                            hashMap1.put("title", title);
                                            hashMap1.put("pId", pId);

                                            Log.d("title", title);

                                            ref.child(timestamp).child("items").child(pId).setValue(hashMap1);
                                        }
                                        mProgressDialog.dismiss();
                                        Toast.makeText(PaymentActivity.this, "Order Placed Successfully...", Toast.LENGTH_SHORT).show();


                                        ref1.child("CartItem").removeValue().equals(shopId);


//                                Intent intent = new Intent(PaymentActivity.this, OrderDetailsBuyerActivity.class);
//                                intent.putExtra("orderFrom", shopId);
//                                intent.putExtra("orderId", timestamp);
//                                startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                prepareNotificationMessage(timestamp);

                                finish();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mProgressDialog.dismiss();
                                Toast.makeText(PaymentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }

        });

        upiEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        nameEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        noteEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(PaymentActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void payUsingUpi(String amount, String upiId, String name, String note, String transactionId) {
        final EasyUpiPayment easyUpiPayment = new EasyUpiPayment.Builder()
                .with(this)
                // on below line we are adding upi id.
                .setPayeeVpa(upiId)
                // on below line we are setting name to which we are making oayment.
                .setPayeeName(name)
                // on below line we are passing transaction id.
                .setTransactionId(transactionId)
                // on below line we are passing transaction ref id.
                .setTransactionRefId(transactionId)
                // on below line we are adding description to payment.
                .setDescription(note)
                // on below line we are passing amount which is being paid.
                .setAmount(amount)
                // on below line we are calling a build method to build this ui.
                .build();
        // on below line we are calling a start
        // payment method to start a payment.
        easyUpiPayment.startPayment();
        // on below line we are calling a set payment
        // status listener method to call other payment methods.
        easyUpiPayment.setPaymentStatusListener(this);
    }


    @Override
    public void onTransactionCompleted(TransactionDetails transactionDetails) {
        // Transaction Completed
        Log.d("TransactionDetails", transactionDetails.toString());
    }

    @Override
    public void onTransactionSuccess() {
        // Payment Success
        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        submitOrder();
    }

    @Override
    public void onTransactionSubmitted() {
        // Payment Pending
        Toast.makeText(this, "Pending | Submitted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTransactionFailed() {
        // Payment Failed
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTransactionCancelled() {
        // Payment Cancelled by User
        Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAppNotFound() {
        // this method is called when the users device is not having any app installed for making payment.
        Toast.makeText(this, "No app found for making transaction..", Toast.LENGTH_SHORT).show();
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == UPI_PAYMENT) {
//
//            if (isConnectionAvailable(this)) {
//                    if (data != null) {
//                        String text = data.getStringExtra("response");
//                        Log.d("UPI", "onActivityResult: " + text);
//                        ArrayList<String> dataList = new ArrayList<>();
//                        dataList.add(text);
//                        upiPaymentDataOperation(text);
//                    } else {
//                        Log.d("UPI", "onActivityResult: " + "Return data is null");
//                        ArrayList<String> dataList = new ArrayList<>();
//                        dataList.add("nothing");
////                        upiPaymentDataOperation(dataList);
//                    }
//
//            }
//        }
//    }

//    private void upiPaymentDataOperation(String data) {
//            String str = data;
//            Log.d("UPIPAY", "upiPaymentDataOperation: " + str);
//            String paymentCancel = "";
//            if (str == null) str = "discard";
//            String status = "";
//            String approvalRefNo = "";
//            String response[] = str.split("&");
//            for (int i = 0; i < response.length; i++) {
//                String equalStr[] = response[i].split("");
//                if (equalStr.length >= 2) {
//                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
//                        status = equalStr[1].toLowerCase();
//                    }
//                } else {
//                    paymentCancel = "Payment cancelled by user.";
//                }
//            }
//                if (status.equals("success")) {
//                    //Code to handle successful transaction here.
//                    Toast.makeText(this, "Transaction successful.", Toast.LENGTH_SHORT).show();
//                    submitOrder();
//                    Log.d("UPI", "responseStr: " + approvalRefNo);
//                } else if ("Payment cancelled by user.".equals(paymentCancel)) {
//                    Toast.makeText(this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
//                }
//    }

//    public static boolean isConnectionAvailable(Context context) {
//        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connectivityManager != null) {
//            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
//            if (netInfo != null && netInfo.isConnected()
//                    && netInfo.isConnectedOrConnecting()
//                    && netInfo.isAvailable()) {
//                return true;
//            }
//        }
//        return false;
//    }





    private void submitOrder() {
        mProgressDialog.setMessage("Placing Order....");
        mProgressDialog.show();

        final String timestamp = ""+System.currentTimeMillis();
        String cost = grandTotal;

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", timestamp);
        hashMap.put("orderTime", timestamp);
        hashMap.put("orderStatus", "In Progress");
        hashMap.put("orderCost", cost);
        hashMap.put("orderBy", ""+mAuth.getUid());
        hashMap.put("OrderFrom", ""+shopId);
        hashMap.put("latitude", myLatitude);
        hashMap.put("longitude", myLongitude);
        hashMap.put("TransactionID", transacId);
        hashMap.put("paymentMode", "UPI");
        hashMap.put("Delivery", homeDelivery);
        hashMap.put("Time", timeData);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopId).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                       final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getUid());
                        ref1.child("CartItem").child(shopId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()){
                                    String finalPrice =""+ds.child("finalPrice").getValue();
                                    String productCategory =""+ds.child("prductCategory").getValue();
                                    String ItemImage = ""+ds.child("profileImage").getValue();
                                    String quantity = ""+ds.child("quantity").getValue();
                                    String title = ""+ds.child("title").getValue();
                                    String pId = ""+ds.child("productId").getValue();

                                    HashMap<String, String> hashMap1 = new HashMap<>();
                                    hashMap1.put("finalPrice", finalPrice);
                                    hashMap1.put("productCategory", productCategory);
                                    hashMap1.put("ItemImage", ItemImage);
                                    hashMap1.put("quantity", quantity);
                                    hashMap1.put("title", title);
                                    hashMap1.put("pId", pId);

                                    Log.d("title", title);

                                    ref.child(timestamp).child("items").child(pId).setValue(hashMap1);
                                }
                                mProgressDialog.dismiss();
                                Toast.makeText(PaymentActivity.this, "Order Placed Successfully...", Toast.LENGTH_SHORT).show();

                                ref1.child("CartItem").removeValue().equals(shopId);


//                                Intent intent = new Intent(CartActivity.this, OrderDetailsBuyerActivity.class);
//                                intent.putExtra("orderFrom", shopId);
//                                intent.putExtra("orderId", timestamp);
//                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        prepareNotificationMessage(timestamp);

                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast.makeText(PaymentActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void prepareNotificationMessage(String orderId){
        //send message to seller when order placed
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC; //must be same as subscribed by user
        String NOTIFICATION_TITLE = "New Order" + orderId;
        String NOTIFICATION_MESSAGE = "Congratulations ...! You Have New Order.";
        String NOTIFICATION_TYPE = "NewOrder";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", mAuth.getUid());
            notificationBodyJo.put("sellerUid", shopId);
            notificationBodyJo.put("orderId", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);

            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo);



            sendFcmNotification(notificationJo, orderId);
        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    private void sendFcmNotification(JSONObject notificationJo, String orderid) {
        //send volley request

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DEPRECATED_GET_OR_POST,"https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
//                after sending start order detail activity
                Intent intent = new Intent(PaymentActivity.this, OrderDetailsBuyerActivity.class);
                intent.putExtra("orderFrom", shopId);
                intent.putExtra("orderId", orderid);
                startActivity(intent);
                finish();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //failed start order detail activity


            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" +Constants.FCM_KEY);

                return headers;
            }
        };

        //enque the volley request
//        Singleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}
