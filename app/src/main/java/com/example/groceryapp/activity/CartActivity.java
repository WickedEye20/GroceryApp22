package com.example.groceryapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.adapters.AdapterCart;
import com.example.groceryapp.adapters.AdapterProductBuyer;
import com.example.groceryapp.models.ModelCart;
import com.example.groceryapp.models.ModelProduct;
import com.example.groceryapp.models.ModelShop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private Button placeOrder;
    private RecyclerView cartItemRv;
    private TextView grandTotal, detailCost, detailDelivery, detailTotal, item, deliveryLay;
    private LinearLayout cartIsEmpty, radioCheckLay;
    private TableLayout tableLayout;
    private RadioGroup radioGroup;

    private RadioButton home;
    private String myLatitude, myLongitude, myPhone, shopId, deliveryFee, data, name, email, address;

    private FirebaseAuth mAuth;

    private ProgressDialog mProgressDialog;

    private ArrayList<ModelCart> cartProductList;
    private AdapterCart mAdapterCart;

    private final String[] bankNames={"BOI","SBI","HDFC","PNB","OBC"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        shopId = getIntent().getStringExtra("ShopUid");

        backBtn = findViewById(R.id.backBtn);
        cartItemRv = findViewById(R.id.cartItemRV);
        grandTotal = findViewById(R.id.grandTotalTV);
        item = findViewById(R.id.item);
        placeOrder = findViewById(R.id.placeOrder);
        cartIsEmpty = findViewById(R.id.cartIsEmpty);
        detailCost = findViewById(R.id.detailCost);
        detailDelivery = findViewById(R.id.detailDelivery);
        deliveryLay = findViewById(R.id.deliveryLay);
        radioCheckLay = findViewById(R.id.radioCheckLayout);

        detailTotal = findViewById(R.id.detailTotal);
        tableLayout = findViewById(R.id.tableLayout);
        radioGroup = findViewById(R.id.radioCheck);



        mAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loadCartItems();
        loadMyInfo();


        //dropdown for time
        Spinner spin = (Spinner) findViewById(R.id.spinner);

        List<String> branchList = new ArrayList<>();

        branchList.add("10:00 AM - 11:00 AM");
        branchList.add("11:00 AM - 12:00 PM");
        branchList.add("1:00 PM - 2:00 PM");
        branchList.add("5:00 PM - 6:00 PM");
        branchList.add("6:00 PM - 7:00 PM");

        ArrayAdapter<String> branchListAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, branchList);
        branchListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(branchListAdapter);




        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                spin.setSelection(position);
                data = (String) spin.getSelectedItem();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }



        });




        placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")){
                    Toast.makeText(CartActivity.this, "Please enter your address in your profile before placing order", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (myPhone.equals("") || myPhone.equals("null")){
                    Toast.makeText(CartActivity.this, "Please enter your phone number in your profile before placing order", Toast.LENGTH_SHORT).show();
                    return;
                }

                home = findViewById(radioGroup.getCheckedRadioButtonId());
                String homeDelivery = home.getText().toString();
                Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                intent.putExtra("ShopId", shopId);
                intent.putExtra("Latitude", myLatitude);
                intent.putExtra("Longitude", myLongitude);
                intent.putExtra("Home Delivery", homeDelivery);
                intent.putExtra("Takeaway", homeDelivery);
                intent.putExtra("dropdown", data);

                intent.putExtra("UserName", name);
                intent.putExtra("UserEmail", email);
                intent.putExtra("UserPhone", myPhone);
                intent.putExtra("UserAddress", address);
//                int upiTotal = Integer.parseInt(grandTotal.getText().toString())+Integer.valueOf(deliveryFee);
//                Log.d("upiTotal", upiTotal+"");
                intent.putExtra("GrandTotal", grandTotal.getText().toString().trim());
                startActivity(intent);
            }
        });


    }


    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            name = ""+ds.child("name").getValue();
                            email = ""+ds.child("email").getValue();
                            myPhone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();
                            String deliveryFee = ""+ds.child("deliveryFee").getValue();
                            address = ""+ds.child("address").getValue();
                            myLatitude = ""+ds.child("latitude").getValue();
                            myLongitude = ""+ds.child("longitude").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadCartItems() {

        cartProductList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid()).child("CartItem").child(shopId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        cartProductList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelCart modelCart = ds.getValue(ModelCart.class);
                            cartProductList.add(modelCart);
                        }
                        mAdapterCart = new AdapterCart(CartActivity.this, cartProductList);
                        cartItemRv.setAdapter(mAdapterCart);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        ref.child(mAuth.getUid()).child("CartItem").child(shopId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    placeOrder.setVisibility(View.VISIBLE);
                    tableLayout.setVisibility(View.VISIBLE);
                    cartIsEmpty.setVisibility(View.GONE);
                    grandTotalPrice((Map<String, Object>) dataSnapshot.getValue());
                } else {
                    grandTotal.setVisibility(View.GONE);
                    item.setVisibility(View.GONE);
                    cartIsEmpty.setVisibility(View.VISIBLE);
                    placeOrder.setVisibility(View.GONE);
                    tableLayout.setVisibility(View.GONE);
                    radioCheckLay.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void grandTotalPrice(Map<String, Object> items) {
        ArrayList<String> total = new ArrayList<>();
        Double sum = 0.0;
        final Double[] detailTotals = {0.0};

        for (Map.Entry<String, Object> entry : items.entrySet()) {
            Map singleUser = (Map) entry.getValue();
            total.add((String) singleUser.get("finalPrice"));
        }
        for (int i = 0; i < total.size(); i++) {
            total.set(i, total.get(i).replace("₹", ""));
            sum += Double.parseDouble(total.get(i));
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        final Double finalSum = sum;
        ref.child(shopId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                deliveryFee = ""+dataSnapshot.child("deliveryFee").getValue();

                detailDelivery.setText("₹"+deliveryFee);
                detailTotals[0] = finalSum + Double.parseDouble(deliveryFee);
                detailTotal.setText("₹"+String.valueOf(detailTotals[0]));
                grandTotal.setText("₹"+String.valueOf(detailTotals[0])+ " "+"(" + total.size()+")");

                //radio btn
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        switch (i){
                            case R.id.radioHome:
                                detailDelivery.setText("₹"+deliveryFee);
                                detailTotals[0] = finalSum + Double.parseDouble(deliveryFee);
                                detailTotal.setText("₹"+String.valueOf(detailTotals[0]));
                                grandTotal.setText("₹"+String.valueOf(detailTotals[0])+ " "+"(" + total.size()+")");
                                deliveryLay.setVisibility(View.VISIBLE);
                                detailDelivery.setVisibility(View.VISIBLE);
                                break;
                            case R.id.radioTakeaway:
                                detailDelivery.setText("₹"+00);
                                detailTotals[0] = detailTotals[0] - Double.parseDouble(deliveryFee);
                                detailTotal.setText("₹"+String.valueOf(detailTotals[0]));
                                grandTotal.setText("₹"+String.valueOf(detailTotals[0])+ " "+"(" + total.size()+")");
                                break;
                        }
                    }
                });

                Log.d("delivery fee", deliveryFee);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        detailCost.setText("₹"+sum);
        grandTotal.setText("₹"+String.valueOf(detailTotals[0])+ " "+"(" + total.size()+")");
    }
}
