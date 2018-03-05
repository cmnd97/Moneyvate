package com.cmnd97.moneyvate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (NfcAdapter.getDefaultAdapter(this) == null) {
            Toast.makeText(this, "Sorry. This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }


        setContentView(R.layout.activity_login);
        fetchLoginPrefs();


    }

    public void fetchLoginPrefs() {
        EditText usernameField = (EditText) findViewById(R.id.username_field);
        EditText passwordField = (EditText) findViewById(R.id.password_field);
        CheckBox rememberCheckBox = (CheckBox) findViewById(R.id.remember_checkbox);
        if (Utility.getSavedRememberCheck(this)) {
            usernameField.setText(Utility.getSavedUsername(this));
            passwordField.setText(Utility.getSavedPassword(this));
            rememberCheckBox.setChecked(true);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public void tryLogin(View view) {
        LoginFragment fragment = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.login_fragment);
        fragment.tryLogin(view);
    }

    public void switchToRegScreen(View view) {

        findViewById(R.id.login_ll).setVisibility(View.GONE);
        findViewById(R.id.register_ll).setVisibility(View.VISIBLE);
    }

    public void tryRegister(View view) {
        RegisterFragment fragment = (RegisterFragment) getSupportFragmentManager().findFragmentById(R.id.register_fragment);
        fragment.tryRegister(view);
    }

    public void switchToLoginScreen(View view) {
        findViewById(R.id.login_ll).setVisibility(View.VISIBLE);
        findViewById(R.id.register_ll).setVisibility(View.GONE);
    }

    public static class LoginFragment extends Fragment {

        public LoginFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_login, container, false);
            return rootView;
        }

        public void tryLogin(View view) {

            EditText usernameField = (EditText) getActivity().findViewById(R.id.username_field);
            EditText passwordField = (EditText) getActivity().findViewById(R.id.password_field);

            String username = usernameField.getText().toString();
            String password = passwordField.getText().toString();

            if (!username.isEmpty() && !password.isEmpty()) {
                LoginTask loginTask = new LoginTask(getActivity());
                loginTask.execute("login", username, password);
            }
        }

        public class LoginTask extends AsyncTask<String, Void, String> {
            Context context;
            AlertDialog alertDialog;

            LoginTask(Context ctx) {
                context = ctx;
            }

            @Override
            protected String doInBackground(String... params) {
                String type = params[0];
                String login_url = "http://www.moneyvate.ga/moneyvate/";

                if (type.equals("login")) {
                    try {
                        login_url += ("login.php");
                        String user_name = params[1];
                        String password = params[2];
                        URL url = new URL(login_url);
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        OutputStream outputStream = httpURLConnection.getOutputStream();
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                        String post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(user_name, "UTF-8") + "&"
                                + URLEncoder.encode("user_password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
                        bufferedWriter.write(post_data);
                        bufferedWriter.flush();
                        bufferedWriter.close();
                        outputStream.close();
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                        String result = "";
                        String line = "";
                        while ((line = bufferedReader.readLine()) != null) {
                            result += line;
                        }
                        bufferedReader.close();
                        inputStream.close();
                        httpURLConnection.disconnect();
                        return result;
                    } catch (Exception e) {
                        e.toString();
                    }
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Login Status");
            }

            @Override
            protected void onPostExecute(String result) {
                alertDialog.setMessage("Could not log in");
                if (!result.equals("OK")) {
                    Utility.saveUsernameAndPassword(getActivity(), null, null, false);
                    alertDialog.show();

                } else {
                    EditText user = (EditText) getActivity().findViewById(R.id.username_field);
                    EditText pass = (EditText) getActivity().findViewById(R.id.password_field);
                    String username = user.getText().toString();
                    String password = pass.getText().toString();
                    Boolean rememberCheck = ((CheckBox) getActivity().findViewById(R.id.remember_checkbox)).isChecked();
                    Utility.saveUsernameAndPassword(getActivity(), username, password, rememberCheck);

                    Intent toMain = new Intent(getActivity(), MainActivity.class);
                    toMain.putExtra("userid", username);
                    startActivity(toMain);

                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    getActivity().finish();
                }
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }
        }
    }

    public static class RegisterFragment extends Fragment {

        public RegisterFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_register, container, false);
            return rootView;
        }


        public void tryRegister(View view) {
            EditText user = (EditText) getActivity().findViewById(R.id.reg_username_field);
            EditText pass = (EditText) getActivity().findViewById(R.id.reg_password_field);
            EditText fn = (EditText) getActivity().findViewById(R.id.reg_firstname_field);
            EditText ln = (EditText) getActivity().findViewById(R.id.reg_lastname_field);
            String username = user.getText().toString();
            String password = pass.getText().toString();
            String firstname = fn.getText().toString();
            String lastname = fn.getText().toString();

            if (!username.isEmpty() && !password.isEmpty()) {
                RegisterTask regTask = new RegisterTask(getActivity());
                regTask.execute("register", username, password, firstname, lastname);
            }
        }


        public class RegisterTask extends AsyncTask<String, Void, String> {
            Context context;
            AlertDialog alertDialog;

            RegisterTask(Context ctx) {
                context = ctx;
            }

            @Override
            protected String doInBackground(String... params) {
                String type = params[0];
                String login_url = "http://www.moneyvate.ga/moneyvate/";

                if (type.equals("register"))
                    try {
                        login_url += ("registration.php");
                        String user_name = params[1];
                        String password = params[2];
                        String first_name = params[3];
                        String last_name = params[4];
                        URL url = new URL(login_url);
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        OutputStream outputStream = httpURLConnection.getOutputStream();
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                        String post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(user_name, "UTF-8") + "&"
                                + URLEncoder.encode("user_password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8") + "&"
                                + URLEncoder.encode("first_name", "UTF-8") + "=" + URLEncoder.encode(first_name, "UTF-8") + "&"
                                + URLEncoder.encode("last_name", "UTF-8") + "=" + URLEncoder.encode(last_name, "UTF-8");
                        bufferedWriter.write(post_data);
                        bufferedWriter.flush();
                        bufferedWriter.close();
                        outputStream.close();
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                        String result = "";
                        String line = "";
                        while ((line = bufferedReader.readLine()) != null) {
                            result += line;
                        }
                        bufferedReader.close();
                        inputStream.close();
                        httpURLConnection.disconnect();
                        return result;
                    } catch (Exception e) {
                        e.toString();
                    }

                return null;
            }

            @Override
            protected void onPreExecute() {
                alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Registration Status");
            }

            @Override
            protected void onPostExecute(String result) {
                alertDialog.setMessage(result);
                if (!result.equals("OK"))
                    alertDialog.setMessage("Error during registration. Please try again using a different username");
                alertDialog.show();
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }
        }
    }
}
