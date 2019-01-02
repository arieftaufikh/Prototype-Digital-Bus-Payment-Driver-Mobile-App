package p.arieftaufikh.cobadriver;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    TextView plate,username,total,start,finish;
    Button btnLogout,btnFilter;
    ListView listView;
    CustomAdapater adapater = new CustomAdapater();

    private DatePickerDialog.OnDateSetListener DateListenerStart,DateListenerEnd;

    ArrayList<String> trans_no = new ArrayList<>();
    ArrayList<String> account = new ArrayList<>();
    ArrayList<String> fare = new ArrayList<>();
    ArrayList<String> datetime = new ArrayList<>();

    //ArrayAdapter arrayAdapter;
    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait..");

        plate = (TextView)findViewById(R.id.txtBusPlate);
        username = (TextView)findViewById(R.id.txtUsername);
        total = (TextView)findViewById(R.id.txtTotal);
        btnLogout = (Button)findViewById(R.id.btnLogout);
        btnFilter = (Button)findViewById(R.id.btnFilter);
        listView = (ListView)findViewById(R.id.transactionList);
        start = (TextView)findViewById(R.id.txtStart);
        finish = (TextView)findViewById(R.id.txtFinish);


        //getTransaction();
        //arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, trans_no);
        //CustomAdapater adapater = new CustomAdapater();

        DateListenerStart = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                String date = year + "-" + month + "-" + day;
                start.setText(date);
            }
        };

        DateListenerEnd = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                String date = year + "-" + month + "-" + day;
                finish.setText(date);
            }
        };


        listView.setAdapter(adapater);
        plate.setText(SharedPrefManager.getInstance(this).getBusPlate());
        username.setText(SharedPrefManager.getInstance(this).getUsername());
        btnLogout.setOnClickListener(this);
        btnFilter.setOnClickListener(this);

        start.setOnClickListener(this);
        finish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v==btnLogout){
            Logout();
        }else if(v==start){
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    MainActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog,
                    DateListenerStart,
                    year,month,day
            );
            dialog.show();
        }else if(v==finish){
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    MainActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog,
                    DateListenerEnd,
                    year,month,day
            );
            dialog.show();
        }else if (v==btnFilter){
            getTransaction();
            listView.setAdapter(adapater);
        }
        listView.setAdapter(adapater);
    }

    public void getTransaction(){
        progressDialog.show();
        final String username = SharedPrefManager.getInstance(getApplicationContext()).getUsername().trim();
        final String dateStart = start.getText().toString().trim();
        final String dateFinish = finish.getText().toString().trim();
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                Constants.URL_GET_TRANS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            trans_no.clear();
                            account.clear();
                            fare.clear();
                            datetime.clear();
                            int counter=0;
                            int income=0;
                            while (counter<response.length()){
                                JSONObject jsonObject = jsonArray.getJSONObject(counter);
                                trans_no.add(jsonObject.getString("transaction_no"));
                                account.add(jsonObject.getString("account_id"));
                                fare.add(jsonObject.getString("fare"));
                                datetime.add(jsonObject.getString("date_time"));
                                income=income+Integer.valueOf(jsonObject.getString("fare"));
                                total.setText(String.valueOf(income));
                                counter++;
                                adapater.notifyDataSetChanged();
                            }
                            //CustomAdapater adapater = new CustomAdapater();
                            //adapater.notifyDataSetChanged();
                            //listView.setAdapter(adapater);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                        progressDialog.dismiss();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("username",username);
                params.put("start",dateStart);
                params.put("end",dateFinish);
                return params;
            }
        };
        RequestHandler.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    public void Logout(){
        final String busid = SharedPrefManager.getInstance(getApplicationContext()).getBusid();

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                Constants.URL_LOGOUT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String check = jsonObject.getString("error");
                            if (check.equals("0")){
                                Toast.makeText(getApplicationContext(),jsonObject.getString("message"),Toast.LENGTH_LONG).show();
                                SharedPrefManager.getInstance(getApplicationContext()).logout();
                                finish();
                                startActivity(new Intent(getApplicationContext(), DriverLogin.class));
                            }else{
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
                        Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("busid",busid);
                return params;
            }
        };

        RequestHandler.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    class CustomAdapater extends BaseAdapter{
        @Override
        public int getCount() {
            return trans_no.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.custom_view,null);

            TextView no = (TextView)convertView.findViewById(R.id.transNo);
            TextView acc = (TextView)convertView.findViewById(R.id.transAcc);
            TextView tfare = (TextView)convertView.findViewById(R.id.transFare);
            TextView time = (TextView)convertView.findViewById(R.id.transTime);

            no.setText(trans_no.get(position).toString());
            acc.setText(account.get(position).toString());
            tfare.setText(fare.get(position).toString());
            time.setText(datetime.get(position).toString());

            return convertView;
        }
    }
}
