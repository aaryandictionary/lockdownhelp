package com.lockdownhelp.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lockdownhelp.app.Adapters.SelectedFileAdapter;
import com.lockdownhelp.app.Models.Request;
import com.lockdownhelp.app.Models.RequestLocationDetails;
import com.lockdownhelp.app.Models.SelectedImageModel;
import com.lockdownhelp.app.R;
import com.lockdownhelp.app.Utils.DataProcessor;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.model.LatLng;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicineForm extends AppCompatActivity implements SelectedFileAdapter.OnSelectedFileEvents{

    private static final int FILE_SELECT_CODE = 0;

    RecyclerView recycler_selected_Image;
    Button btnUploadImage,btnSendRequest;
    TextView txtMyLocation,txtChangeLocation,txtInstructions;
    RelativeLayout relMyLocation;
    ImageButton imgBtnBack;

    List<SelectedImageModel>selectedImageModels;
    SelectedFileAdapter selectedFileAdapter;

    private StorageReference Folder;
    ProgressDialog progressDialog;

    FirebaseAuth mAuth;

    long estimatedServerTimeMs=0;
    long  offset;

    DataProcessor dataProcessor;
    String categoryIconUrl,categoryTitle;
    List<SelectedImageModel>newSelectedImageModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_form);

        recycler_selected_Image=findViewById(R.id.recycler_selected_Image);
        recycler_selected_Image.setHasFixedSize(true);
        recycler_selected_Image.setLayoutManager(new LinearLayoutManager(this));

        progressDialog=new ProgressDialog(this);
        selectedImageModels=new ArrayList<>();
        selectedFileAdapter=new SelectedFileAdapter(MedicineForm.this,selectedImageModels,MedicineForm.this,"Form");
        recycler_selected_Image.setAdapter(selectedFileAdapter);

        Folder= FirebaseStorage.getInstance().getReference().child("prescriptionImage");
        mAuth=FirebaseAuth.getInstance();

        dataProcessor=DataProcessor.getInstance(this);

        categoryIconUrl=getIntent().getStringExtra("categoryIconUrl");
        categoryTitle=getIntent().getStringExtra("categoryTitle");

        btnUploadImage=findViewById(R.id.btnUploadImage);
        btnSendRequest=findViewById(R.id.btnSendRequest);
        txtMyLocation=findViewById(R.id.txtMyLocation);
        txtChangeLocation=findViewById(R.id.txtChangeLocation);
        relMyLocation=findViewById(R.id.relMyLocation);
        imgBtnBack=findViewById(R.id.imgBtnBack);
        txtInstructions=findViewById(R.id.txtInstruction);

        newData();

        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().start(MedicineForm.this);
            }
        });

        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedImageModels.size()==0){
                 Toast.makeText(MedicineForm.this,"No prescription image file selected",Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        copySelectedImageList();
                    } catch (CloneNotSupportedException e) {
                        Toast.makeText(MedicineForm.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void copySelectedImageList() throws CloneNotSupportedException {

        List<SelectedImageModel>uploadselectedImageModels=new ArrayList<>();
        uploadselectedImageModels.clear();

        for (SelectedImageModel selectedImageModel:selectedImageModels){
            uploadselectedImageModels.add(selectedImageModel.clone());
        }
        saveData(uploadselectedImageModels);
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
                    Toast.makeText(MedicineForm.this,"No Volunteers available",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(MedicineForm.this,"Something went wrong",Toast.LENGTH_SHORT).show();
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
                getAdminAddress(categoryTitle,adminId,distance,adminLocationDetails);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(MedicineForm.this,"Something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAdminAddress(final String categoryTitle, final String adminId, final Double distance, final RequestLocationDetails.AdminLocationDetails adminLocationDetails){

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("Admins").child(adminId).child("address");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("address").exists()){
                    adminLocationDetails.setAddress(dataSnapshot.child("address").getValue(String.class));
                }
                submitRequest(categoryTitle,adminId,distance,adminLocationDetails);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(MedicineForm.this,"Something went wrong",Toast.LENGTH_SHORT).show();
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
        progressDialog.setMessage("Sending medicine request...");

        newData();
        if (newSelectedImageModel.size()!=0){
            progressDialog.show();
            estimatedServerTimeMs = System.currentTimeMillis() + offset;
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Requests");
            final String requestId=databaseReference.push().getKey();

            RequestLocationDetails requestLocationDetails=new RequestLocationDetails();
            RequestLocationDetails.UserLocationDetails userLocationDetails=new RequestLocationDetails.UserLocationDetails();
            userLocationDetails.setLat(Double.parseDouble(dataProcessor.getLat()));
            userLocationDetails.setLng(Double.parseDouble(dataProcessor.getLng()));
            requestLocationDetails.setAdminLocationDetails(adminLocationDetails);
            requestLocationDetails.setUserLocationDetails(userLocationDetails);
            requestLocationDetails.setDistance(distance);

            Request request=new Request();
            request.setRequestStatus("Success");
            request.setRequestTimeStamp(estimatedServerTimeMs);
            request.setRequestTitle("Medicine");
            request.setRequestStatusHeading("Request sent successfully");
            request.setUserName(dataProcessor.getUserName());
            request.setUserLocation(dataProcessor.getHomeLocation());
            request.setRequestViewType("Form");
            request.setCategoryIconUrl(categoryIconUrl);
            request.setRequestStatusMessage("Waiting for acceptance");
            request.setVolunteerId(adminId);
            request.setRequestLocationDetails(requestLocationDetails);
            request.setUserId(mAuth.getCurrentUser().getUid());
            request.setSelectedImageModels(newSelectedImageModel);
            request.setRequestId(requestId);

            databaseReference.child(requestId).setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Intent intent=new Intent(MedicineForm.this,RequestHistoryDetailsType3.class);
                        intent.putExtra("request_id",requestId);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(MedicineForm.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.hide();
                }
            });

        }else{
            Toast.makeText(MedicineForm.this,"Upload doctor's prescription",Toast.LENGTH_SHORT).show();
        }
    }

    private void saveData(List<SelectedImageModel>newSelectedImageModel){
        this.newSelectedImageModel=newSelectedImageModel;
        searchVolunteer(categoryTitle);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK){
                SelectedImageModel selectedImageModel=new SelectedImageModel();
                selectedImageModel.setFileName(getFileName(result.getUri()));
                selectedImageModel.setUri(result.getUri());
                long filesize=new File(result.getUri().getPath()).length();
                selectedImageModel.setFileSize(filesize);
                UploadImage(selectedImageModel);
                progressDialog.setMessage("Uploading Image...");

            }else if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception e=result.getError();
                Toast.makeText(this,"File selection Failed",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void UploadImage(final SelectedImageModel selectedImageModel){
        progressDialog.show();
        final StorageReference ImageName=Folder.child(mAuth.getCurrentUser().getUid()).child(selectedImageModel.getFileName());

        ImageName.putFile(selectedImageModel.getUri()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ImageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        selectedImageModel.setFileUrl(String.valueOf(uri));
                        selectedImageModels.add(selectedImageModel);
                        selectedFileAdapter.notifyDataSetChanged();
                        txtInstructions.setVisibility(View.GONE);
                        progressDialog.hide();
                        //Toast.makeText(NewPost.this,"Uploaded",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MedicineForm.this,"Image upload failed",Toast.LENGTH_SHORT).show();
                        progressDialog.hide();
                    }
                });

            }
        });
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    @Override
    public void onFileListClick(String fileUrl) {
        /*Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(uri.getPath()), "image/*");
        startActivity(intent);*/
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
            startActivity(browserIntent);
    }

    @Override
    public void onRemoveBtnClick(final SelectedImageModel selectedImageModel) {
        progressDialog.setMessage("Removing Image...");
        progressDialog.show();
        final StorageReference ImageName=Folder.child(mAuth.getCurrentUser().getUid()).child(selectedImageModel.getFileName());
        ImageName.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    selectedImageModels.remove(selectedImageModel);
                    selectedFileAdapter.notifyDataSetChanged();
                    if (selectedImageModels.size()==0){
                        txtInstructions.setVisibility(View.VISIBLE);
                    }
                    progressDialog.hide();
                }else{
                    Toast.makeText(MedicineForm.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
                }
            }
        });
    }
}
