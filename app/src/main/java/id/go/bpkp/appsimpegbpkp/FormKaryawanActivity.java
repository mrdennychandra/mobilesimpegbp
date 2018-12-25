package id.go.bpkp.appsimpegbpkp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import id.go.bpkp.appsimpegbpkp.http.ApiInterface;
import id.go.bpkp.appsimpegbpkp.http.Divisi;
import id.go.bpkp.appsimpegbpkp.http.Karyawan;
import id.go.bpkp.appsimpegbpkp.http.RestClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static id.go.bpkp.appsimpegbpkp.http.RestClient.BASE_IMAGE;

public class FormKaryawanActivity extends AppCompatActivity {

    private EditText txtNama, txtEmail, txtTelpon, txtTglLahir;
    private ImageView imgPath;
    private Spinner spDivisi;
    private RadioGroup rbJkGroup;
    private RadioButton rbJk,rbL,rbP;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private Button btnSave;
    private List<Divisi> divisis;
    private ApiInterface api;
    ProgressDialog mProgressDialog;
    String tglLahirDb = "";

    //foto
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 200;
    private Uri file;
    private String imagePath;
    private Karyawan karyawan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_karyawan);
        setTitle("Form Karyawan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txtNama = (EditText) findViewById(R.id.txt_nama);
        txtEmail = (EditText) findViewById(R.id.txt_email);
        txtTelpon = (EditText) findViewById(R.id.txt_telpon);
        txtTglLahir = (EditText) findViewById(R.id.txt_tgllahir);
        imgPath = (ImageView) findViewById(R.id.img_foto);
        spDivisi = (Spinner) findViewById(R.id.sp_divisi);
        rbJkGroup = (RadioGroup) findViewById(R.id.rb_jk_group);
        rbL = (RadioButton) findViewById(R.id.rb_l);
        rbP = (RadioButton) findViewById(R.id.rb_p);
        btnSave = (Button) findViewById(R.id.btn_save);
        Intent intent = getIntent();
        karyawan = (Karyawan) intent.getSerializableExtra("karyawan");
        if(karyawan != null){
            txtNama.setText(karyawan.nama);
            txtEmail.setText(karyawan.email);
            txtTelpon.setText(karyawan.telpon);
            txtTglLahir.setText(karyawan.tgllahir);
            if (karyawan.jeniskelamin.equalsIgnoreCase("L")) {
                rbL.setSelected(true);
            } else{
                rbP.setSelected(true);
            }
            //Glide.with(this).load(BASE_IMAGE + karyawan.foto)
                    //.into(imgPath);
            //simpan gambar di internal storage
            Glide.with(this)
                    .asBitmap()
                    .load(BASE_IMAGE + karyawan.foto)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource,
                                                    @Nullable com.bumptech.glide.request.transition.Transition
                                                            <? super Bitmap> transition) {
                            imgPath.setImageBitmap(resource);
                            imagePath = saveImage(resource, karyawan.id);
                            //Toast.makeText(FormKaryawanActivity.this,imagePath,Toast.LENGTH_SHORT).show();
                        }

                    });
        }
        api = RestClient.getClient().create(ApiInterface.class);
        getDivisi();
        txtTglLahir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(FormKaryawanActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                txtTglLahir.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                tglLahirDb = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //validasi
                if (txtNama.getText().toString().equals("")) {
                    txtNama.setError("Nama harus diisi");
                    return;
                }
                if (txtEmail.getText().toString().equals("")) {
                    txtEmail.setError("Email harus diisi");
                    return;
                }
                if (karyawan == null) {
                    karyawan = new Karyawan();
                    rbJk = (RadioButton) findViewById(rbJkGroup.getCheckedRadioButtonId());
                    karyawan.nama = txtNama.getText().toString();
                    karyawan.email = txtEmail.getText().toString();
                    karyawan.foto = imagePath;
                    karyawan.jeniskelamin = rbJk.getText().toString();
                    karyawan.telpon = txtTelpon.getText().toString();
                    Divisi selectedItem = (Divisi) spDivisi.getSelectedItem();
                    karyawan.iddivisi = selectedItem.id;
                    insert(karyawan);
                } else {
                    //update
                    rbJk = (RadioButton) findViewById(rbJkGroup.getCheckedRadioButtonId());
                    karyawan.nama = txtNama.getText().toString();
                    karyawan.email = txtEmail.getText().toString();
                    karyawan.foto = imagePath;
                    karyawan.jeniskelamin = rbJk.getText().toString();
                    karyawan.telpon = txtTelpon.getText().toString();
                    Divisi selectedItem = (Divisi) spDivisi.getSelectedItem();
                    karyawan.iddivisi = selectedItem.id;
                    update(karyawan);
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        imgPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseDialog();
            }
        });

    }

    //mendapatkan divisi
    private void getDivisi() {
        Call<List<Divisi>> call = api.getDivisi();
        call.enqueue(new Callback<List<Divisi>>() {
            @Override
            public void onResponse(Call<List<Divisi>> call, Response<List<Divisi>>
                    response) {
                divisis = response.body();
                //divisis get API
                ArrayAdapter<Divisi> spinnerArrayAdapter = new ArrayAdapter<Divisi>(FormKaryawanActivity.this,
                        android.R.layout.simple_spinner_item, divisis);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                spDivisi.setAdapter(spinnerArrayAdapter);
                for (int i = 0; i < divisis.size(); i++) {
                    if (divisis.get(i).nama.equals(karyawan.namadivisi)) {
                        spDivisi.setSelection(i);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Divisi>> call, Throwable t) {
                Log.e("Retrofit Get", t.toString());
                Toast.makeText(FormKaryawanActivity.this, t.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    ////////////////////////////////////////// camera/gallery //////////////////////////////////////////
    //foto
    private void chooseDialog() {
        CharSequence menu[] = new CharSequence[]{"Take From Galery", "Open Camera"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Picture");
        builder.setItems(menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    gallery();
                } else {
                    takePicture();
                }
            }
        });
        builder.show();

    }

    //camera
    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = Uri.fromFile(getOutputMediaFile());
        //Toast.makeText(this,file.toString(),Toast.LENGTH_SHORT).show();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
        startActivityForResult(intent, 100);
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
    }

    private void gallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }


    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e("EditProfileActivity", "getRealPathFromURI Exception : " + e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String saveImage(Bitmap image, String fileName) {
        String savedImagePath = null;
        String imageFileName = "JPEG_" + fileName + ".jpg";
        File storageDir = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + "");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                //perkecil
                image.compress(Bitmap.CompressFormat.JPEG, 60, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Add the image to the system gallery
            galleryAddPic(savedImagePath);
            //Toast.makeText(DetailEventActivity.this, "IMAGE SAVED", Toast.LENGTH_LONG).show();
        }
        return savedImagePath;
    }

    private void galleryAddPic(String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    ////////////////////////////////////////// camera/gallery //////////////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                imgPath.setImageURI(file);
                imagePath = file.getPath();
            }
        } else {
            if (resultCode == RESULT_OK) {
                imgPath.setImageURI(data.getData());
                imagePath = getRealPathFromURI(this, data.getData());
            }
        }
        Toast.makeText(this, imagePath, Toast.LENGTH_SHORT).show();
    }

    private void insert(final Karyawan karyawan) {
        RequestBody reqFile = null;
        File file = new File(imagePath);//path image
        reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("foto", file.getName(), reqFile);
        RequestBody nama = RequestBody.create(MediaType.parse("text/plain"), karyawan.nama);
        RequestBody tgllahir = RequestBody.create(MediaType.parse("text/plain"), tglLahirDb);
        RequestBody email = RequestBody.create(MediaType.parse("text/plain"), karyawan.email);
        RequestBody telpon = RequestBody.create(MediaType.parse("text/plain"), karyawan.telpon);
        RequestBody jeniskelamin = RequestBody.create(MediaType.parse("text/plain"), karyawan.jeniskelamin);
        RequestBody iddivisi = RequestBody.create(MediaType.parse("text/plain"), karyawan.iddivisi);

        mProgressDialog = new ProgressDialog(FormKaryawanActivity.this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("menyimpan...");
        mProgressDialog.show();

        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
        Call<String> call = api.insertKaryawan(body,
                nama,
                tgllahir,
                email,
                telpon,
                jeniskelamin,
                iddivisi);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String>
                    response) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                String result = response.body();
                if (response.isSuccessful()) {
                    Toast.makeText(FormKaryawanActivity.this, "data berhasil disimpan", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(FormKaryawanActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(FormKaryawanActivity.this, "data tidak berhasil disimpan", Toast.LENGTH_SHORT).show();
                }
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("Retrofit Get", t.toString());
                Toast.makeText(FormKaryawanActivity.this, t.toString(), Toast.LENGTH_LONG).show();
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    private void update(final Karyawan karyawan) {
        RequestBody reqFile = null;
        File file = new File(imagePath);//path image
        reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("foto", file.getName(), reqFile);
        RequestBody nama = RequestBody.create(MediaType.parse("text/plain"), karyawan.nama);
        RequestBody tgllahir = RequestBody.create(MediaType.parse("text/plain"), tglLahirDb);
        RequestBody email = RequestBody.create(MediaType.parse("text/plain"), karyawan.email);
        RequestBody telpon = RequestBody.create(MediaType.parse("text/plain"), karyawan.telpon);
        RequestBody jeniskelamin = RequestBody.create(MediaType.parse("text/plain"), karyawan.jeniskelamin);
        RequestBody iddivisi = RequestBody.create(MediaType.parse("text/plain"), karyawan.iddivisi);
        RequestBody id = RequestBody.create(MediaType.parse("text/plain"), karyawan.id);

        mProgressDialog = new ProgressDialog(FormKaryawanActivity.this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("menyimpan...");
        mProgressDialog.show();

        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
        Call<String> call = api.updateKaryawan(body,
                nama,
                tgllahir,
                email,
                telpon,
                jeniskelamin,
                iddivisi, id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String>
                    response) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                String result = response.body();
                if (response.isSuccessful()) {
                    Toast.makeText(FormKaryawanActivity.this, "data berhasil disimpan", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(FormKaryawanActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(FormKaryawanActivity.this, "data tidak berhasil disimpan", Toast.LENGTH_SHORT).show();
                }
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("Retrofit Get", t.toString());
                Toast.makeText(FormKaryawanActivity.this, t.toString(), Toast.LENGTH_LONG).show();
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    //back button diatas
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
