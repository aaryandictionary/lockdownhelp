package com.lockdownhelp.app.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;

import com.lockdownhelp.app.MainActivity;
import com.lockdownhelp.app.MapActivity;
import com.lockdownhelp.app.Models.AddressModel;
import com.lockdownhelp.app.Models.User;
import com.lockdownhelp.app.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class BottomSheetOtpDialog extends BottomSheetDialogFragment {
    private BottomSheetListener mListener;
    private  EditText textOtp;
    private String mVerificationId;
    private FirebaseAuth mAuth;
    PhoneAuthProvider.ForceResendingToken mResendToken;
     TextView txtResendOtp,txtError,txtMobileNumber;
    ContentLoadingProgressBar progressBar;
    String number;
    DataProcessor dataProcessor;
    Button btnVerifyOtp;
    CountDownTimer countDownTimer;
    TextView txtChangeMobile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.bottom_sheet_otp_dialog,container,false);

        mAuth = FirebaseAuth.getInstance();


         btnVerifyOtp=view.findViewById(R.id.btnVerifyOtp);
         txtChangeMobile=view.findViewById(R.id.txtChangeMobile);
      txtResendOtp=view.findViewById(R.id.txtResendOtp);
        txtError=view.findViewById(R.id.txtError);
         textOtp=view.findViewById(R.id.textOtp);
         progressBar=view.findViewById(R.id.progress);
        txtMobileNumber=view.findViewById(R.id.txtMobileNumber);
         number = this.getArguments().getString("mobile");

        txtMobileNumber.setText(number);

         dataProcessor=DataProcessor.getInstance(getContext());
        sendVerificationCode(number);
        btnVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(textOtp.getText().toString())&&textOtp.getText().length()==6){
                    verifyVerificationCode(textOtp.getText().toString());
                }else{
                    Toast.makeText(getContext(),"Enter a valid OTP",Toast.LENGTH_SHORT).show();
                    progressBar.hide();
                }
            }
        });

        textOtp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtError.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtChangeMobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onChangeClick();
                dismiss();
            }
        });

        txtResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (TextUtils.equals(txtResendOtp.getText(),"Resend")){
                   resendVerificationCode(number);
                   progressBar.show();
                   txtResendOtp.setVisibility(View.GONE);

                   txtError.setVisibility(View.GONE);
               }
            }
        });

        return view;
    }

    public interface  BottomSheetListener{
        void onChangeClick();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mListener=(BottomSheetListener)context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()+ "must implement BottomSheetListener");
        }
    }

    private void sendVerificationCode(String mobile) {
        progressBar.show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private void resendVerificationCode(String mobile){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
               "+91"+ mobile,
                60  ,
                TimeUnit.SECONDS,
                getActivity(),
                mCallbacks,
                mResendToken);
    }

    private void startTimer(){
        txtResendOtp.setVisibility(View.VISIBLE);
        progressBar.hide();

       countDownTimer= new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished/1000!=0){
                    txtResendOtp.setText("Resend in " + millisUntilFinished / 1000+" seconds");
                }else{
                    countDownTimer.onFinish();
                }
            }

            public void onFinish() {
                countDownTimer.cancel();
                txtResendOtp.setText("Resend");
                txtResendOtp.setTextColor(getResources().getColor(R.color.colorLightBlue));
            }

        }.start();
    }


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                textOtp.setText(code);
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            countDownTimer.cancel();
            progressBar.hide();
            txtResendOtp.setText("Resend");
            txtResendOtp.setVisibility(View.VISIBLE);
            progressBar.hide();

            txtError.setVisibility(View.VISIBLE);
            txtError.setText("Failed to send OTP");
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            mVerificationId = s;
            progressBar.hide();
            startTimer();
            mResendToken = forceResendingToken;
        }
    };

    private void verifyVerificationCode(String otp) {
        textOtp.setEnabled(false);
        txtChangeMobile.setEnabled(false);
        btnVerifyOtp.setEnabled(false);
        countDownTimer.cancel();
        txtResendOtp.setText("Verifying...");
        txtResendOtp.setTextColor(getResources().getColor(R.color.colorGrey));
        progressBar.hide();
        txtResendOtp.setVisibility(View.VISIBLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful we will start the profile activity
                            User user=new User();
                            user.setUserId(mAuth.getCurrentUser().getUid());
                            user.setUserMobile(number);
                            user.setToken(FirebaseInstanceId.getInstance().getToken());
                            saveUser(user);

                        }else {

                            try {
                                throw task.getException();
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                               txtError.setText("Invalid verification code");
                               textOtp.requestFocus();
                            }  catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                                txtError.setText("Something went wrong");
                            }

                            txtError.setVisibility(View.VISIBLE);
                            countDownTimer.cancel();
                            txtResendOtp.setText("Resend");
                            txtResendOtp.setTextColor(getResources().getColor(R.color.colorLightBlue));
                            textOtp.setEnabled(true);
                            txtChangeMobile.setEnabled(true);
                            btnVerifyOtp.setEnabled(true);
                        }

                    }
                });
    }

    private void saveUser(final User user){
        final DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("Users");

        dbUser.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    AddressModel addressModel=dataSnapshot.child("address").getValue(AddressModel.class);
                    dataProcessor.setDistrictAndState(addressModel.getDistrict(),addressModel.getState());
                    User user1=dataSnapshot.getValue(User.class);
                    dataProcessor.setProfile(user1.getUserName(),user1.getUserAddress(),user1.getUserMobile());
                    setLatLng(mAuth.getCurrentUser().getUid(),addressModel.getState(),addressModel.getDistrict(),addressModel.getAddress(),user);
                }else{
                    if (user!=null){
                        dbUser.child(user.getUserId()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){
                                    dataProcessor.setProfile("None","None",user.getUserMobile());
                                    Intent intent = new Intent(getContext(), MapActivity.class);
                                    intent.putExtra("openType","MainActivity");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    getActivity().finish();
                                }else{
                                    textOtp.setEnabled(true);
                                    txtChangeMobile.setEnabled(true);
                                    btnVerifyOtp.setEnabled(true);
                                    Toast.makeText(getContext(),"Something went wrong",Toast.LENGTH_SHORT).show();
                                }
                                countDownTimer.cancel();

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                txtError.setVisibility(View.VISIBLE);
                countDownTimer.cancel();
                txtError.setText("Something went wrong");
                txtResendOtp.setText("Resend");
                txtResendOtp.setTextColor(getResources().getColor(R.color.colorLightBlue));
                textOtp.setEnabled(true);
                txtChangeMobile.setEnabled(true);
                btnVerifyOtp.setEnabled(true);
            }
        });

    }

    private void setLatLng(final String userId, String state, String district, final String address, final User user){
        final DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("Users");

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("usersLocation").child(state).child(district);
        GeoFire geoFire=new GeoFire(databaseReference);

        geoFire.getLocation(userId, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location!=null){
                    dataProcessor.setLatLng(String.valueOf(location.latitude),String.valueOf(location.longitude),address);
                    dbUser.child(userId).child("token").setValue(FirebaseInstanceId.getInstance().getToken()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                //intent.putExtra("openType","MainActivity");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                getActivity().finish();
                                countDownTimer.cancel();
                            }else{
                                textOtp.setEnabled(true);
                                txtChangeMobile.setEnabled(true);
                                btnVerifyOtp.setEnabled(true);
                                Toast.makeText(getContext(),"Something went wrong",Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                                dismiss();
                            }
                        }
                    });


                }else{
                    if (user!=null){
                        dbUser.child(user.getUserId()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){
                                    dataProcessor.setProfile("None","None",user.getUserMobile());
                                    Intent intent = new Intent(getContext(), MapActivity.class);
                                    intent.putExtra("openType","MainActivity");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    getActivity().finish();
                                }else{
                                    textOtp.setEnabled(true);
                                    txtChangeMobile.setEnabled(true);
                                    btnVerifyOtp.setEnabled(true);
                                    Toast.makeText(getContext(),"Something went wrong",Toast.LENGTH_SHORT).show();
                                }

                                countDownTimer.cancel();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                countDownTimer.cancel();
                txtError.setVisibility(View.VISIBLE);
                txtError.setText("Something went wrong");
                txtResendOtp.setText("Resend");
                txtResendOtp.setTextColor(getResources().getColor(R.color.colorLightBlue));
                textOtp.setEnabled(true);
                txtChangeMobile.setEnabled(true);
                btnVerifyOtp.setEnabled(true);
            }
        });
    }
}
