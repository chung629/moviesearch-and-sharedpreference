package com.example.startproject2;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyFragment extends Fragment implements AutoPermissionsListener {

    EditText name;
    EditText birthday;
    EditText email;
    EditText password;
    RadioGroup radioGroup;
    RadioButton radioButton1;
    RadioButton radioButton2;
    private DatePickerDialog.OnDateSetListener callback;

    ImageView cirImageView;
    File file;
    ViewGroup tempGroup;
    LinearLayout sign;
    signatureView signature;
    Button eraser;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_my, container, false);

        //VIEW id mapping
        name = rootView.findViewById(R.id.editText);
        birthday = rootView.findViewById(R.id.editText2);
        email = rootView.findViewById(R.id.editText3);
        password = rootView.findViewById(R.id.editText4);
        radioGroup = rootView.findViewById(R.id.radioGroup);
        radioButton1 = rootView.findViewById(R.id.radioButton);
        radioButton2 = rootView.findViewById(R.id.radioButton2);
        cirImageView = rootView.findViewById(R.id.imageView4);
        tempGroup = rootView;
        sign = rootView.findViewById(R.id.sign);
        signature = new signatureView(rootView.getContext());
        sign.addView(signature);
        eraser = rootView.findViewById(R.id.eraser);
        eraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signature.eraser();
            }
        });
        // 클릭시 달력 출력
        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), callback, 2019,
                        11, 15);
                datePickerDialog.show();
            }
        });
        callback = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                birthday.setText(year + "년" + (month+1) + "월" + day + "일");
            }
        };

        cirImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        AutoPermissions.Companion.loadAllPermissions(getActivity(), 101);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("save", MODE_PRIVATE);
        name.setText(sharedPreferences.getString("name", ""));
        birthday.setText(sharedPreferences.getString("birthday", ""));
        email.setText(sharedPreferences.getString("email", ""));
        password.setText(sharedPreferences.getString("password", ""));
        radioGroup.check(sharedPreferences.getInt("radiogroup", 0));
        radioGroup.jumpDrawablesToCurrentState();

        file = createFile();
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), null);
            cirImageView.setImageBitmap(bitmap);
        }

        File tempFile = new File(getActivity().getExternalFilesDir(null),
                "signature.png");
        Bitmap bitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
        signature.changeBitmap(bitmap);
    }
    //상태 저장
    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("save", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("name", name.getText().toString());
        editor.putString("birthday", birthday.getText().toString());
        editor.putString("email", email.getText().toString());
        editor.putString("password", password.getText().toString());
        editor.putInt("radiogroup", radioGroup.getCheckedRadioButtonId());
        editor.putBoolean("radiobutton1", radioButton1.isChecked());
        editor.putBoolean("radiobutton2", radioButton2.isChecked());
        editor.commit();

        File signatureFile = new File(getActivity().getExternalFilesDir(null),
                "signature.png");
        Bitmap bitmap = signature.mBitmap;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        try {
            FileOutputStream fos = new FileOutputStream(signatureFile);
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void takePicture(){
        if(file == null) {
            file = createFile();
        }
        Uri fileUri = FileProvider.getUriForFile(tempGroup.getContext(),"com.example.startproject2.fileprovider", file );
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if(intent.resolveActivity(tempGroup.getContext().getPackageManager())!= null){
            startActivityForResult(intent, 101);
        }
    }
    private File createFile(){
        String filename = "capture4.jpg";
        File storageDir = tempGroup.getContext().getExternalFilesDir(null);
        File outFile = new File(storageDir, filename);
        return outFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            cirImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onDenied(int i, String[] strings) {
        Toast.makeText(tempGroup.getContext(), "permissions denied : " + strings.length, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGranted(int i, String[] strings) {
        Toast.makeText(tempGroup.getContext(), "permissions granted : " + strings.length, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(getActivity(), requestCode, permissions, this);
    }
}