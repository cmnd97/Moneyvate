package com.cmnd97.moneyvate;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import static com.cmnd97.moneyvate.R.id.locView;


public class MainActivity extends AppCompatActivity {
    ViewPager viewPager;
    String userid;
    public static final String TAG = "NFCDemo";
    private NfcAdapter NFCAdapter;
    ProfileFragment profileFragment = new ProfileFragment();
    TaskFragment taskFragment = new TaskFragment();
    ScanFragment scanFragment = new ScanFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userid = getIntent().getExtras().getString("userid");
        setContentView(R.layout.activity_main);
        NFCAdapter = NfcAdapter.getDefaultAdapter(this);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        UIFragmentPagerAdapter adapter = new UIFragmentPagerAdapter(getSupportFragmentManager(), this, profileFragment, scanFragment, taskFragment);

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
        viewPager.setOffscreenPageLimit(2);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        new DataFetchTask(this).execute("fetchUserName");
        new DataFetchTask(this).execute("fetchTagLocations");
        new DataFetchTask(this).execute("fetchUserTasks");


    }

    public void logOut(View v) {

        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }

    public ArrayList<com.cmnd97.moneyvate.Tag> getStoredTags() {
        return taskFragment.getStoredTags();
    }

    public void sendTaskForCreation(View v) {

        String locName = ((AutoCompleteTextView) findViewById(locView)).getText().toString().trim();
        String DateAndTime = ((EditText) findViewById(R.id.dateView)).getText().toString().trim() + " " + ((EditText) findViewById(R.id.timeView)).getText().toString().trim();
        String tagToSend = "";
        ArrayList<com.cmnd97.moneyvate.Tag> tags = getStoredTags();
        if (!locName.equals("") && !DateAndTime.equals("")) {
            for (int i = 0; i < tags.size(); i++) {
                if (locName.equals(tags.get(i).description)) {
                    tagToSend = tags.get(i).id;
                    break;
                }
            }
            if (findViewById(R.id.task_creator_layout).getVisibility() == View.VISIBLE)
                new TaskCreationTask(this).execute(tagToSend, DateAndTime);
        } else
            ((TextView) findViewById(R.id.task_creation_result)).setText("");
    }

    public void setUpTaskCreator(View v) {
        profileFragment.setUpTask();
        findViewById(R.id.task_creator_layout).setVisibility((findViewById(R.id.task_creator_layout).getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
        ((Button) v).setText(findViewById(R.id.task_creator_layout).getVisibility() == View.VISIBLE ? "CLOSE" : "NEW TASK");


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                getNFCPermission();
                break;
            default:
                break;
        }
    }

    public void goToNFCSettings(View v) {
        startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
    }


    public void sendTag(String readTagID) {
        new SendTagTask(this).execute(readTagID);
    }

    void getNFCPermission() {
        if (!NFCAdapter.isEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)//for newer versions
                requestPermissions(new String[]{Manifest.permission.NFC}, 10);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, NFCAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, NFCAdapter);

        super.onPause();
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType("text/plain");
        } catch (Exception e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    @Override
    protected void onNewIntent(Intent intent) {
// method gets called, when the user attaches a Tag to the device.
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
            if (intent.getType().equals("text/plain")) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                scanFragment.setServerMessage("Tag not understood.");
                scanFragment.setCircleImage("X");
            }
        } else {
            scanFragment.setServerMessage("Tag not understood.");
            scanFragment.setCircleImage("X");
        }
    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();

            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        // return readText(ndefRecord);
                        return readText(ndefRecord, tag);
                    } catch (UnsupportedEncodingException e) {
                        Log.e("Moneyvate", "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record, Tag tag) throws UnsupportedEncodingException {
            String tagIdHex = bytesToHexString(tag.getId());

            /*
            byte[] payload = record.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 0063;
          return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);

          */
            return tagIdHex;
        }

        private String bytesToHexString(byte[] src) {
            StringBuilder stringBuilder = new StringBuilder("Moneyvate-");
            if (src == null || src.length <= 0) {
                return null;
            }

            char[] buffer = new char[2];
            for (int i = 0; i < src.length; i++) {
                buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
                buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
                System.out.println(buffer);
                stringBuilder.append(buffer);
            }

            return stringBuilder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                scanFragment.sendTagToServer(result);
            }
        }
    }

    private class DataFetchTask extends AsyncTask<String, Void, String> {
        Context context;
        String fetchAction = null;

        DataFetchTask(Context ctx) {
            context = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            fetchAction = params[0];
            String fetch_url = "http://www.moneyvate.ga/moneyvate/fetchuserdata.php";

            try {
                URL url = new URL(fetch_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                String post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userid, "UTF-8") + "&"
                        + URLEncoder.encode("fetchAction", "UTF-8") + "=" + URLEncoder.encode(fetchAction, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                String result = "";
                String line;
                while ((line = bufferedReader.readLine()) != null)
                    result += line;

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

        }

        @Override
        protected void onPostExecute(String result) {

            String[] fromServer;

            if (result != null) {
                fromServer = result.split("<br />");

                if (fetchAction.equals("fetchUserName")) {
                    profileFragment.setUserName(fromServer[0], fromServer[1]);
                }

                if (fetchAction.equals("fetchTagLocations"))
                    taskFragment.storeLocations(fromServer);

                if (fetchAction.equals("fetchUserTasks"))
                    taskFragment.storeTasks(fromServer);


            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    private class SendTagTask extends AsyncTask<String, Void, String> {

        Context context;

        SendTagTask(Context ctx) {
            context = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            String readTagID = params[0];
            String tagProcUrl = "http://www.moneyvate.ga/moneyvate/processtag.php";
            try {
                URL url = new URL(tagProcUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userid, "UTF-8") + "&"
                        + URLEncoder.encode("tag_input", "UTF-8") + "=" + URLEncoder.encode(readTagID, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                result = bufferedReader.readLine();

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

            } catch (Exception e) {
                e.toString();
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String result) {
            scanFragment.setTagName(result.split("#")[0]);
            scanFragment.setCircleImage(result.split("#")[1]);
            scanFragment.setServerMessage(result.split("#")[2]);
            new DataFetchTask(getApplicationContext()).execute("fetchUserTasks");


        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    private class TaskCreationTask extends AsyncTask<String, Void, String> {

        Context context;

        TaskCreationTask(Context ctx) {
            context = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            String readTagID = params[0];
            String dateAndTime = params[1];
            String tagProcUrl = "http://www.moneyvate.ga/moneyvate/createnewtask.php";
            try {
                URL url = new URL(tagProcUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userid, "UTF-8") + "&"
                        + URLEncoder.encode("tag_id", "UTF-8") + "=" + URLEncoder.encode(readTagID, "UTF-8") + "&"
                        + URLEncoder.encode("deadline", "UTF-8") + "=" + URLEncoder.encode(dateAndTime, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                result = bufferedReader.readLine();

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

            } catch (Exception e) {
                e.toString();
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String result) {
            new DataFetchTask(getApplicationContext()).execute("fetchUserTasks");
            ((TextView) findViewById(R.id.task_creation_result)).setText(result);

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
