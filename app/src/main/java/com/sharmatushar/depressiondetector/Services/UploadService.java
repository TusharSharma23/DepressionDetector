package com.sharmatushar.depressiondetector.Services;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.sharmatushar.depressiondetector.Constants.NetworkLinks.BASE_URL;
import static com.sharmatushar.depressiondetector.Constants.NetworkLinks.UPLOAD_ACTIVITY;

public class UploadService extends JobService {
    public UploadService() {
    }

    @Override
    @SuppressLint("StaticFieldLeak")
    public boolean onStartJob(final JobParameters params) {
        Log.d("Tushar", "Inside upload service...............");

        new AsyncTask<Void, Void, JSONObject>() {

            @Override
            protected JSONObject doInBackground(Void... voids) {
                PersistableBundle bundle = params.getExtras();
                String fileName = bundle.getString("file_name");
                String filePath = bundle.getString("file_path");
                Log.d("Tushar", "Fetched file_name: " + fileName);
                Log.d("Tushar", "Fetched file_path: " + filePath);
                assert filePath != null;
                File file = new File(filePath);

                return sendCSVFile(bundle.getString("login_id"), fileName, file);
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                Log.d("Tushar", "Server returned: " + jsonObject.toString());

                PersistableBundle bundle = params.getExtras();
                String filePath = bundle.getString("file_path");
                assert filePath != null;
                File file = new File(filePath);
                boolean result = file.delete();
                if (result) {
                    Log.d("Tushar", "Deleted file: " + filePath);
                } else {
                    Log.d("Tushar", "Unable to delete file: " + filePath);
                }

                jobFinished(params, false);
            }
        }.execute();

        return true;
    }

    private JSONObject sendCSVFile(String loginId, String fileName, File csvFile) {
        OkHttpClient httpClient = new OkHttpClient();
        Log.d("Tushar", "Created HttpClient");
        String url = BASE_URL + UPLOAD_ACTIVITY;
        Log.d("Tushar", "Calling url: " + url);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("UserId", loginId)
                .addFormDataPart("activity_file", fileName, RequestBody.create(MediaType.parse("file"), csvFile))
                .build();
        Log.d("Tushar", "Created request body");

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Log.d("Tushar", "Created request.");

        try {
            Log.d("Tushar", "Creating response.");
            Response response = httpClient.newCall(request).execute();
            String responseString = Objects.requireNonNull(response.body()).string();
            Log.d("Tushar", "Got Response: " + responseString);
            return new JSONObject(responseString);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method is called if the system has determined that you must stop execution of your job
     * even before you've had a chance to call {@link #jobFinished(JobParameters, boolean)}.
     *
     * <p>This will happen if the requirements specified at schedule time are no longer met. For
     * example you may have requested WiFi with
     * {@link JobInfo.Builder#setRequiredNetworkType(int)}, yet while your
     * job was executing the user toggled WiFi. Another example is if you had specified
     * {@link JobInfo.Builder#setRequiresDeviceIdle(boolean)}, and the phone left its
     * idle maintenance window. You are solely responsible for the behavior of your application
     * upon receipt of this message; your app will likely start to misbehave if you ignore it.
     * <p>
     * Once this method returns, the system releases the wakelock that it is holding on
     * behalf of the job.</p>
     *
     * @param params The parameters identifying this job, as supplied to
     *               the job in the {@link #onStartJob(JobParameters)} callback.
     * @return {@code true} to indicate to the JobManager whether you'd like to reschedule
     * this job based on the retry criteria provided at job creation-time; or {@code false}
     * to end the job entirely.  Regardless of the value returned, your job must stop executing.
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}
