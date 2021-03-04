package com.lockdownhelp.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.animation.ArgbEvaluator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lockdownhelp.app.Adapters.ViewPagerAdapter;
import com.lockdownhelp.app.Models.Request;
import com.lockdownhelp.app.Models.RequestLocationDetails;
import com.lockdownhelp.app.Models.ViewPagerModel;
import com.lockdownhelp.app.R;
import com.lockdownhelp.app.Utils.DataProcessor;
import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPagerFormType extends AppCompatActivity implements ViewPagerAdapter.ViewPagerEvents {

    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    List<ViewPagerModel>viewPagerModels;
    List<String>choice;
    Integer[]colors=null;
    ArgbEvaluator argbEvaluator=new ArgbEvaluator();

    TextView txtSubTitle;

    List<ViewPagerModel>savedResponse;

    long estimatedServerTimeMs=0;
    long  offset;

    Button btnSubmit;

    ChipGroup chipGroup;
    String question; String selectionType,categoryIconUrl,categoryTitle,subCategoryId;
    int size;

    EditText textResponse;

    DataProcessor dataProcessor;

    FirebaseAuth mAuth;

    private int lastPosition = 0;
    float sumPosAndOffset;
    ProgressDialog progressDialog;

    AVLoadingIndicatorView progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager_form_type);

        viewPagerModels=new ArrayList<>();
        savedResponse=new ArrayList<>();

        mAuth=FirebaseAuth.getInstance();

        dataProcessor=DataProcessor.getInstance(this);
        viewPager=findViewById(R.id.viewPager);
        btnSubmit=findViewById(R.id.btnSubmit);
        progress=findViewById(R.id.progress);
        txtSubTitle=findViewById(R.id.txtSubTitle);
        //viewPager.setPadding(130,0,130,0);
        progressDialog=new ProgressDialog(ViewPagerFormType.this);
        categoryIconUrl=getIntent().getStringExtra("categoryIconUrl");
        categoryTitle=getIntent().getStringExtra("categoryTitle");
        subCategoryId=getIntent().getStringExtra("subCategoryId");
        txtSubTitle.setText(categoryTitle);
        newData();
       /* Integer[]colors_temp={
                getResources().getColor(R.color.colorRed),
                getResources().getColor(R.color.colorYellow),
                getResources().getColor(R.color.colorGreen)
        };


        colors=colors_temp;*/

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (position<(viewPagerAdapter.getCount()-1)&& position<(colors.length-1)){
                    viewPager.setBackgroundColor((Integer)argbEvaluator.evaluate(
                                                            positionOffset,
                                                            colors[position],
                                                            colors[position+1]
                    ));
                }else{
                    viewPager.setBackgroundColor(colors[colors.length-1]);
                }

                if (!TextUtils.equals(viewPagerModels.get(position).getSelectionType(),""))
                populateList();

            }

            @Override
            public void onPageSelected(int position) {
                //Toast.makeText(ViewPagerFormType.this,viewPagerModels.get(position).getQuestion(),Toast.LENGTH_LONG).show();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        loadQuestions();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    private void loadQuestions(){
        progress.show();
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("SubCategories").child(subCategoryId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    viewPagerModels.clear();
                    int i=0;
                    colors=new Integer[(int)dataSnapshot.getChildrenCount()];
                    for (DataSnapshot questionSnapshot:dataSnapshot.getChildren()){
                        ViewPagerModel viewPagerModel=questionSnapshot.getValue(ViewPagerModel.class);
                        if (TextUtils.equals(viewPagerModel.getQuestionStatus(),"Active")){
                            choice=new ArrayList<>();
                            colors[i]=Color.parseColor(questionSnapshot.child("backgroundColor").getValue(String.class));
                            i++;
                            for (DataSnapshot choiceSnapshot:questionSnapshot.child("choice").getChildren()){
                                choice.add(choiceSnapshot.getValue(String.class));
                            }
                            viewPagerModel.setResponses(choice);
                            viewPagerModels.add(viewPagerModel);
                        }
                    }

                   /* ViewPagerModel viewPagerModel=new ViewPagerModel();
                    viewPagerModel.setSelectionType("");
                    viewPagerModels.add(viewPagerModel);*/

                    viewPagerAdapter=new ViewPagerAdapter(viewPagerModels,ViewPagerFormType.this,ViewPagerFormType.this);
                    viewPager.setAdapter(viewPagerAdapter);
                    progress.hide();
                }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progress.hide();
                Toast.makeText(ViewPagerFormType.this,"Something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemChecked(CompoundButton buttonView, boolean isChecked, String question, String selectionType, TextView textView) {
        //Toast.makeText(ViewPagerFormType.this,"entered"+question,Toast.LENGTH_SHORT).show();

        //Toast.makeText(this,isChecked+"",Toast.LENGTH_SHORT).show();




            ViewPagerModel viewPagerModel=new ViewPagerModel();
            viewPagerModel.setQuestion(question);
            viewPagerModel.setSelectionType(selectionType);
            List<String>checked=new ArrayList<>();
            checked.add(buttonView.getText().toString());
            viewPagerModel.setResponses(checked);
        int respCheck;
            int questCheck=checkQuestion(savedResponse,question);
            if (questCheck>=0){
                 respCheck=checkResponse(savedResponse,buttonView.getText().toString(),question);
                if (respCheck>=0){
                    if (!isChecked){
                        //printHint(savedResponse,questCheck,respCheck,textView);
                        savedResponse.get(questCheck).getResponses().remove(buttonView.getText().toString());
                    }
                }else{
                    if (isChecked){
                        savedResponse.get(questCheck).getResponses().add(buttonView.getText().toString());
                        //printHint(savedResponse,questCheck,respCheck,textView);
                    }
                }
                //Toast.makeText(ViewPagerFormType.this,"Exist",Toast.LENGTH_SHORT).show();
            }else{

                savedResponse.add(viewPagerModel);
                //Toast.makeText(ViewPagerFormType.this,"Doesn't exist",Toast.LENGTH_SHORT).show();
            }





        /*for (ViewPagerModel viewPagerModel:savedResponse) {
            if (viewPagerModel != null && TextUtils.equals(viewPagerModel.getQuestion(), question)) {
                if (isChecked){
                    viewPagerModel.getResponses().add(buttonView.getText().toString());
                    savedResponse.set(savedResponse.indexOf(viewPagerModel),viewPagerModel);
                    Toast.makeText(ViewPagerFormType.this,"if checked"+question,Toast.LENGTH_SHORT).show();
                }else{
                    savedResponse.remove(viewPagerModel);
                    Toast.makeText(ViewPagerFormType.this,"else"+question,Toast.LENGTH_SHORT).show();
                }
            }else{
                if (isChecked) {
                    ViewPagerModel viewPagerModel1 = new ViewPagerModel();
                    viewPagerModel1.setQuestion(question);
                    viewPagerModel1.setSelectionType(selectionType);
                    List<String> name = new ArrayList<>();
                    name.add(buttonView.getText().toString());
                    viewPagerModel1.setResponses(name);
                    savedResponse.add(viewPagerModel1);
                    Toast.makeText(ViewPagerFormType.this,"else checked"+question,Toast.LENGTH_SHORT).show();
                }
            }
        }*/
    }



    private int checkQuestion(List<ViewPagerModel>savedResponse,String question){
        for (ViewPagerModel viewPagerModel:savedResponse){
            if (viewPagerModel!=null&&TextUtils.equals(viewPagerModel.getQuestion(),question)){
                return savedResponse.indexOf(viewPagerModel);
            }
        }
        return -1;
    }

    private int checkResponse(List<ViewPagerModel>savedResponse,String response,String question){
        for (ViewPagerModel viewPagerModel:savedResponse){
            if (viewPagerModel!=null&&TextUtils.equals(viewPagerModel.getQuestion(),question)){
                for (String name:viewPagerModel.getResponses()){
                    if (name!=null && TextUtils.equals(name,response)){
                        return viewPagerModel.getResponses().indexOf(name);
                    }
                }
            }

        }
        return -1;
    }

    @Override
    public void onListPopulate(ChipGroup chipGroup, String question, String selectionType,int size,EditText textResponse) {
        this.chipGroup=chipGroup;
        this.question=question;
        this.size=size;
        this.textResponse=textResponse;
    }

    @Override
    public void onTextChange(String text, String question, String selectionType) {

        int check=checkText(question);
            if (check>=0){
                savedResponse.get(check).getResponses().set(0,text);
            }else{
                ViewPagerModel viewPagerModel1=new ViewPagerModel();
                viewPagerModel1.setQuestion(question);
                viewPagerModel1.setSelectionType(selectionType);
                List<String>name=new ArrayList<>();
                name.add(text);
                viewPagerModel1.setResponses(name);
                savedResponse.add(viewPagerModel1);
            }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void loadSubmitData(final ImageView imageView, final TextView textView) {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("CovidTest").child("submit");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Glide.with(ViewPagerFormType.this).load(dataSnapshot.child("questionIconUrl").getValue(String.class)).into(imageView);
                textView.setText(dataSnapshot.child("question").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void searchVolunteer(final String categoryTitle){
        progressDialog.setMessage("Searching volunteer...");
        progressDialog.show();

        final HashMap<String,Double> hashMap=new HashMap();
        final DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("adminsLocation").child(dataProcessor.getUserState()).child(dataProcessor.getUserDistrict());
        //final DatabaseReference userRef=FirebaseDatabase.getInstance().getReference("Users");
        final GeoFire geoFire=new GeoFire(databaseReference);

        final DatabaseReference adminRef=FirebaseDatabase.getInstance().getReference().child("Admins");


        GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(Double.parseDouble(dataProcessor.getLat()), Double.parseDouble(dataProcessor.getLng())),2000);
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                if (dataSnapshot.child("dept").exists()){
                    for (DataSnapshot depSnap:dataSnapshot.child("dept").getChildren()){
                        if (TextUtils.equals(depSnap.child("categoryName").getValue(String.class),categoryTitle)&&TextUtils.equals(depSnap.child("categoryCheck").getValue(String.class),"Active")){
                            final LatLng userLatLng=new LatLng(Double.parseDouble(dataProcessor.getLat()),Double.parseDouble(dataProcessor.getLng()));
                            final LatLng AdminLatLng=new LatLng(location.latitude,location.longitude);
                            hashMap.put(dataSnapshot.getKey(),CalculationByDistance(userLatLng,AdminLatLng));
                            break;
                        }
                    }
                }
            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                Map.Entry<String,Double>min=null;
                for (Map.Entry<String,Double>entry:hashMap.entrySet()){
                    if (min==null||min.getValue()>entry.getValue()){
                        min=entry;
                    }
                }
                //Toast.makeText(SubCategoryListActivity.this,"Distance is "+min.getValue()+" for "+min.getKey(),Toast.LENGTH_SHORT).show();
                if (min!=null){
                    getAdminDetails(categoryTitle,min.getKey(),min.getValue());
                }else{
                    Toast.makeText(ViewPagerFormType.this,"No Volunteers available",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(ViewPagerFormType.this,"Something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAdminDetails(final String categoryTitle, final String adminId, final Double distance){
        final RequestLocationDetails.AdminLocationDetails adminLocationDetails=new RequestLocationDetails.AdminLocationDetails();
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("adminsLocation").child(dataProcessor.getUserState()).child(dataProcessor.getUserDistrict());
        GeoFire geoFire=new GeoFire(databaseReference);

        geoFire.getLocation(adminId, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                adminLocationDetails.setLat(location.latitude);
                adminLocationDetails.setLng(location.longitude);
                submitRequest(categoryTitle,adminId,distance,adminLocationDetails);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(ViewPagerFormType.this,"Something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        return Radius * c;
    }


    private void submitRequest(String categoryTitle, String adminId, Double distance, RequestLocationDetails.AdminLocationDetails adminLocationDetails){
        newData();
        if (savedResponse.size() != 0) {
            progressDialog.setMessage("Submitting request...");

            estimatedServerTimeMs = System.currentTimeMillis() + offset;

            RequestLocationDetails requestLocationDetails=new RequestLocationDetails();
            RequestLocationDetails.UserLocationDetails userLocationDetails=new RequestLocationDetails.UserLocationDetails();
            userLocationDetails.setLat(Double.parseDouble(dataProcessor.getLat()));
            userLocationDetails.setLng(Double.parseDouble(dataProcessor.getLng()));
            requestLocationDetails.setAdminLocationDetails(adminLocationDetails);
            requestLocationDetails.setUserLocationDetails(userLocationDetails);
            requestLocationDetails.setDistance(distance);


            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Requests");
            final String requestId = databaseReference.push().getKey();
            Request request = new Request();
            request.setRequestViewType("ViewPager");
            request.setVolunteerId(adminId);
            request.setRequestId(requestId);
            request.setCategoryIconUrl(categoryIconUrl);
            request.setUserId(mAuth.getCurrentUser().getUid());
            request.setRequestStatusMessage("Waiting for acceptance");
            request.setRequestStatusHeading("Request sent successfully");
            request.setRequestTitle(categoryTitle);
            request.setRequestStatus("Success");
            request.setRequestLocationDetails(requestLocationDetails);
            request.setUserName(dataProcessor.getUserName());
            request.setUserLocation(dataProcessor.getHomeLocation());
            request.setRequestTimeStamp(estimatedServerTimeMs);
            request.setViewPagerModels(savedResponse);
            assert requestId != null;
            databaseReference.child(requestId).setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        progressDialog.dismiss();
                        Intent intent=new Intent(ViewPagerFormType.this,RequestHistoryDetailsType4.class);
                        intent.putExtra("request_id",requestId);
                        startActivity(intent);
                        finish();
                    }else{
                        progressDialog.dismiss();
                        Toast.makeText(ViewPagerFormType.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            progressDialog.dismiss();
            Toast.makeText(ViewPagerFormType.this,"Please answer all the questions",Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onSubmitClick() {
        if (savedResponse.size()!=viewPagerAdapter.getCount()-1){
            Toast.makeText(ViewPagerFormType.this,"Please answer all the questions",Toast.LENGTH_SHORT).show();
        }else{
            searchVolunteer(categoryTitle);
        }
    }

    public void newData(){
        DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                offset = snapshot.getValue(Long.class);
                estimatedServerTimeMs = System.currentTimeMillis() + offset;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    private int checkText(String question){
        for (ViewPagerModel viewPagerModel:savedResponse){
            if (viewPagerModel!=null&&TextUtils.equals(viewPagerModel.getQuestion(),question)){
               return savedResponse.indexOf(viewPagerModel);
            }
        }
        return -1;
    }


    private void populateList(){
        for (ViewPagerModel viewPagerModel:savedResponse) {
            if (viewPagerModel != null && TextUtils.equals(viewPagerModel.getQuestion(), question)) {

//                Toast.makeText(ViewPagerFormType.this,question,Toast.LENGTH_SHORT).show();

                for (int i=0;i<size;i++){

                        Chip chip=(Chip) chipGroup.getChildAt(i);
                        if (chip!=null){
                            if (viewPagerModel.getResponses().contains(chip.getText().toString())){
                                chip.setChecked(true);
                                //Toast.makeText(ViewPagerFormType.this,chip.getText(),Toast.LENGTH_SHORT).show();
                            }else{
                                chip.setChecked(false);
                            }
                        }


                    if (!TextUtils.equals(selectionType,"Single")||!TextUtils.equals(selectionType,"Multiple")){
                        if (viewPagerModel.getResponses().size()!=0){
                            textResponse.setText(viewPagerModel.getResponses().get(0));
                        }
                    }

                    }

                }
            }
        }

}
