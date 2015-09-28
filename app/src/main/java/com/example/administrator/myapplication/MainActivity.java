package com.example.administrator.myapplication;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

public class MainActivity extends Activity {

    EditText translateEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Calls for the AsyncTask to execute when the translate button is clicked
    public void onTranslateClick(View view) {

        EditText translateEditText = (EditText) findViewById(R.id.editText);

        // If the user entered words to translate then get the JSON data
        if(!isEmpty(translateEditText)){

            Toast.makeText(this, "Getting Courses",
                    Toast.LENGTH_LONG).show();

            // Calls for the method doInBackground to execute
            new SaveTheFeed().execute();

        } else {

            // Post an error message if they didn't enter words
            Toast.makeText(this, "Enter Course Prefix",
                    Toast.LENGTH_SHORT).show();

        }

    }

    // Check if the user entered words to translate
    // Returns false if not empty
    protected boolean isEmpty(EditText editText){

        // Get the text in the EditText convert it into a string, delete whitespace
        // and check length
        return editText.getText().toString().trim().length() == 0;

    }

    // Allows you to perform background operations without locking up the user interface
    // until they are finished
    // The void part is stating that it doesn't receive parameters, it doesn't monitor progress
    // and it won't pass a result to onPostExecute
    class SaveTheFeed extends AsyncTask<Void, Void, Void>{

        // Holds JSON data in String format
        String jsonString = "";

        // Will hold the translations that will be displayed on the screen
        String result = "";

        // Everything that should execute in the background goes here
        // You cannot edit the user interface from this method
        @Override
        protected Void doInBackground(Void... voids) {

            // Get access to the EditText so we can get the text in it
            EditText translateEditText = (EditText) findViewById(R.id.editText);

            // Get the text from EditText
            String wordsToTranslate = translateEditText.getText().toString();

            // Replace spaces in the String that was entered with + so they can be passed
            // in a URL
            wordsToTranslate = wordsToTranslate.replace(" ", "+");

            // Client used to grab data from a provided URL
            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());

            // Provide the URL for the post request
            HttpPost httpPost = new HttpPost("api.svsu.edu/courses?" + "prefix=" + wordsToTranslate);


            // Define that the data expected is in JSON format
            httpPost.setHeader("Content-type", "application/json");

            // Allows you to input a stream of bytes from the URL
            InputStream inputStream = null;

            try{

                // The client calls for the post request to execute and sends the results back
                HttpResponse response = httpClient.execute(httpPost);

                // Holds the message sent by the response
                HttpEntity entity = response.getEntity();

                // Get the content sent
                inputStream = entity.getContent();

                // A BufferedReader is used because it is efficient
                // The InputStreamReader converts the bytes into characters
                // My JSON data is UTF-8 so I read that encoding
                // 8 defines the input buffer size
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

                // Storing each line of data in a StringBuilder
                StringBuilder sb = new StringBuilder();

                String line = null;

                // readLine reads all characters up to a \n and then stores them
                while((line = reader.readLine()) != null){

                    sb.append(line + "\n");

                }

                // Save the results in a String
                jsonString = sb.toString();

                // Create a JSONObject by passing the JSON data
                JSONObject jObject = new JSONObject(jsonString);

                // Get the Array named translations that contains all the translations
                JSONArray jArray = jObject.getJSONArray("translations");

                // Cycles through every translation in the array
                outputTranslations(jArray);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        // Called after doInBackground finishes executing
        @Override
        protected void onPostExecute(Void aVoid) {

            // Put the translations in the TextView
            TextView translationTextView = (TextView) findViewById(R.id.translationTextView);

            translationTextView.setText(result);

        }

        protected void outputTranslations(JSONArray jsonArray){

            // Used to get the translation using a key
            String[] languages = {"courses"};

            // Save all the translations by getting them with the key
            try{

                for(int i = 0; i < jsonArray.length(); i++){

                    JSONObject translationObject =
                            jsonArray.getJSONObject(i);

                    result = result + languages[i] + " : " +
                            translationObject.getString(languages[i]) + "\n";

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

}
