package soul.leccionrafael;

/**
 * Created by soul on 24/08/17.
 */
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.ContentValues.TAG;

/**
 * Created by Soul on 8/20/2017.
 */

public class mService extends IntentService {


    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    public mService() {
        super(mService.class.getName());
    }

    public mService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "Service Started!");

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String url = intent.getStringExtra("url");

        Bundle bundle = new Bundle();

        if (!TextUtils.isEmpty(url)) {
            /* Update UI: Download Service is Running */
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);

            try {
                String[] results = downloadData(url);

                /* Sending result back to activity */
                if (null != results && results.length > 0) {
                    bundle.putStringArray("result", results);
                    receiver.send(STATUS_FINISHED, bundle);

                }
            } catch (Exception e) {

                /* Sending error message back to activity */
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
            }
        }
        Log.d(TAG, "Service Stopping!");
        this.stopSelf();



    }


    private String[] downloadData(String requestUrl) throws IOException, DownloadException {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        /* forming th java.net.URL object */
        URL url = new URL(requestUrl);
        urlConnection = (HttpURLConnection) url.openConnection();

        /* optional request header */
        urlConnection.setRequestProperty("Content-Type", "application/json");

        /* optional request header */
        urlConnection.setRequestProperty("Accept", "application/json");

        /* for Get request */
        urlConnection.setRequestMethod("GET");
        int statusCode = urlConnection.getResponseCode();

        /* 200 represents HTTP OK */
        if (statusCode == 200) {
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            String response = convertInputStreamToString(inputStream);
            Log.e("@@dum","Tested " + response + " bytes");
            String[] results = parseResult(response);

            return results;
        } else {
            throw new DownloadException("Failed to fetch data!!");
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

            /* Close Stream */
        if (null != inputStream) {
            inputStream.close();
        }

        return result;
    }

    public class DownloadException extends Exception {

        public DownloadException(String message) {
            super(message);
        }

        public DownloadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private String[] parseResult(String result) {

        String[] mvp_list = null;
        try {
            JSONObject response = new JSONObject(result);

            JSONArray posts = response.optJSONArray("");

            mvp_list = new String[posts.length()];

            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);

                String user_id = post.optString("userId");
                String e_id = post.optString("id");
                String title = post.optString("title");
                String body = post.optString("body");

                mvp_list[i] = user_id;

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mvp_list;
    }


}