package p.arieftaufikh.cobadriver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DriverLogin extends AppCompatActivity implements View.OnClickListener{

    Spinner spinner;
    ArrayList<String> busid = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    EditText username,password;
    Button btnLogin;
    public ProgressDialog progressDialog;
    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,busid);
        getBus();
        spinner = (Spinner)findViewById(R.id.spinner);
        spinner.setAdapter(arrayAdapter);

        username = (EditText)findViewById(R.id.etID);
        password = (EditText)findViewById(R.id.etPass);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait..");
    }

    @Override
    public void onClick(View v) {
        if (v==btnLogin){
            login();
        }
    }

    public void login(){
        final String user,pass,id;
        user=username.getText().toString().trim();
        pass=password.getText().toString().trim();
        id=spinner.getSelectedItem().toString().trim();
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                Constants.URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String check = jsonObject.getString("error");
                            if (check.equals("0")){
                                SharedPrefManager.getInstance(getApplicationContext()).userLogin(
                                        jsonObject.getString("username"),
                                        id,
                                        jsonObject.getString("busplate")
                                );
                                Toast.makeText(getApplicationContext(),jsonObject.getString("message"),Toast.LENGTH_LONG).show();
                                handler.postDelayed(runnable,1000);
                            }else {
                                Toast.makeText(getApplicationContext(),jsonObject.getString("message"),Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        error.printStackTrace();
                        Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("username",user);
                params.put("password",pass);
                params.put("busid",id);
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void getBus(){
        final JsonArrayRequest request = new JsonArrayRequest(
                Constants.URL_GET_BUS,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i =0;i<response.length();i++){
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                busid.add(jsonObject.getString("bus_id").toString());
                                arrayAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
        );
        RequestHandler.getInstance(this).addToRequestQueue(request);
    }
}
