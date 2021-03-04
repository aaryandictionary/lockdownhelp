package com.lockdownhelp.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.lockdownhelp.app.Adapters.MyRequestsAdapter;
import com.lockdownhelp.app.Models.MyRequestsModel;
import com.lockdownhelp.app.R;
import com.lockdownhelp.app.Utils.RequestBottomSheetType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyRequests extends AppCompatActivity implements MyRequestsAdapter.OnRequestClickEvents {

    RecyclerView recycler_my_requests;
    List<MyRequestsModel>myRequestsModels;
    MyRequestsAdapter myRequestsAdapter;

    FirebaseAuth mAuth;
    RequestBottomSheetType requestBottomSheetType;

    ImageButton imgBtnBack;

    AVLoadingIndicatorView progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        recycler_my_requests=findViewById(R.id.recycler_my_requests);
        imgBtnBack=findViewById(R.id.imgBtnBack);
        progress=findViewById(R.id.progress);
        recycler_my_requests.setHasFixedSize(true);
        recycler_my_requests.setLayoutManager(new LinearLayoutManager(this));

        mAuth=FirebaseAuth.getInstance();

        myRequestsModels=new ArrayList<>();

        setDataToRecycler();

        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void setDataToRecycler(){
        progress.show();
        Query databaseReference= FirebaseDatabase.getInstance().getReference("Requests").orderByChild("userId").equalTo(mAuth.getCurrentUser().getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myRequestsModels.clear();
                for (DataSnapshot requestSnapshot:dataSnapshot.getChildren()){
                    MyRequestsModel myRequests=requestSnapshot.getValue(MyRequestsModel.class);
                    myRequestsModels.add(myRequests);
                }
                Collections.reverse(myRequestsModels);
                myRequestsAdapter=new MyRequestsAdapter(MyRequests.this,myRequestsModels,MyRequests.this);
                recycler_my_requests.setAdapter(myRequestsAdapter);
                progress.hide();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progress.hide();
                Toast.makeText(MyRequests.this,"Something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMyRequestClick(String requestId, String requestViewType) {
        Intent intent;

        switch (requestViewType){
            case "List":
                 intent=new Intent(MyRequests.this,RequestHistoryDetailsType1.class);
                intent.putExtra("request_id",requestId);
                startActivity(intent);
                break;
            case "ViewPager":
                intent=new Intent(MyRequests.this,RequestHistoryDetailsType4.class);
                intent.putExtra("request_id",requestId);
                startActivity(intent);
                break;
            case "BottomSheetNone":
            case "BottomSheetSpinner":
            case "BottomSheet":
                intent=new Intent(MyRequests.this,RequestHistoryDetailsType2.class);
                intent.putExtra("request_id",requestId);
                startActivity(intent);
                break;
            case "Form":
                intent=new Intent(MyRequests.this,RequestHistoryDetailsType3.class);
                intent.putExtra("request_id",requestId);
                startActivity(intent);
                break;

        }

    }
}
