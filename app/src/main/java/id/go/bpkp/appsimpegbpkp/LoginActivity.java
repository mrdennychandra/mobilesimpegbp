package id.go.bpkp.appsimpegbpkp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import id.go.bpkp.appsimpegbpkp.http.ApiInterface;
import id.go.bpkp.appsimpegbpkp.http.RestClient;
import id.go.bpkp.appsimpegbpkp.http.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText email, mPassword;
    private Button btnLogin;
    private ApiInterface api;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = (EditText) findViewById(R.id.txt_username);
        mPassword = (EditText) findViewById(R.id.txt_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        api = RestClient.getClient().create(ApiInterface.class);

        //dapatkan shared preference isLogin
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        String isLogin = settings.getString("pref_islogin", "");

        //jika isLogin = 1 maka langsung alihkan ke MainAcitivity
        if (!isLogin.equals("")) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String username = email.getText().toString();
                String password = mPassword.getText().toString();
                if (username.equals("")) {
                    email.setError("email harus diisi");
                    return;
                }
                if (mPassword.equals("")) {
                    email.setError("password harus diisi");
                    return;
                }
                login(username, password);
            }

        });
    }

    private void login(String username, String password) {
        Call<User> call = api.login(username, password);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("authenticating...");
        mProgressDialog.show();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User>
                    response) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }

                User result = response.body();
                if (response.isSuccessful()) {
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("pref_token", result.token);
                    editor.putString("pref_islogin", "1");
                    editor.putString("pref_id", result.id);
                    editor.putString("pref_email", result.email);
                    editor.putString("pref_username", email.getText().toString());
                    editor.commit();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "cek username atau password anda", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("Retrofit Get", t.toString());
                Toast.makeText(LoginActivity.this, t.toString(), Toast.LENGTH_LONG).show();
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }
}
