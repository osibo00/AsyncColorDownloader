package productions.darthplagueis.asynccolordownloader;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static ColorDataBase colorDataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ColorDownloader downloader = new ColorDownloader(this);
        downloader.execute("https://raw.githubusercontent.com/operable/cog/master/priv/css-color-names.json");
    }

    private static class ColorDownloader extends AsyncTask<String, Void, String> {

        private WeakReference<MainActivity> activityReference;

        // only retain a weak reference to the activity
        // allows for there not to be context leaks
        ColorDownloader(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            return colorDownload(strings[0]);
        }

        @Override
        protected void onPostExecute(String data) {
            MainActivity activity = activityReference.get();
            if (activity == null) return;
            if (data == null) {
                Toast.makeText(activity, "Data is null", Toast.LENGTH_LONG).show();
                return;
            }
            Type collectionType = new TypeToken<HashMap<String, String>>() {
            }.getType();
            Gson gson = new Gson();
            HashMap<String, String> hexColors = gson.fromJson(new StringReader(data), collectionType);
            colorDataBase = new ColorDataBase(activity);
            for (Map.Entry<String, String> entry : hexColors.entrySet()) {
                colorDataBase.insertColor(entry.getKey(), entry.getValue());
            }
            ColorSwitcher colorSwitcher = new ColorSwitcher(activity);
            colorSwitcher.execute();
        }

        private String colorDownload(String urlString) {
            InputStream inputStream = null;
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder strings = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    strings.append(line);
                }
                return new String(strings);
            } catch (Exception e) {
                return null;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class ColorSwitcher extends AsyncTask<Void, String, Void> {

        private WeakReference<MainActivity> activityReference;

        ColorSwitcher(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity activity = activityReference.get();
            if (activity == null) return;
            ProgressBar progressBar = activity.findViewById(R.id.progress_bar);
            LinearLayout topLayout = activity.findViewById(R.id.top_layout);
            TextView bottomText = activity.findViewById(R.id.bottom_text_view);
            Button button = activity.findViewById(R.id.main_button);
            progressBar.setVisibility(View.INVISIBLE);
            topLayout.setVisibility(View.VISIBLE);
            bottomText.setVisibility(View.VISIBLE);
            button.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                int count = 0;
                HashMap<String, String> colorValuesMap = colorDataBase.getColorMap();
                for (String color : colorValuesMap.keySet()) {
                    publishProgress(colorValuesMap.get(color), color, String.valueOf(count++));
                    Thread.sleep(150);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            MainActivity activity = activityReference.get();
            if (activity == null) return;
            LinearLayout topLayout = activity.findViewById(R.id.top_layout);
            LinearLayout bottomLayout = activity.findViewById(R.id.bottom_layout);
            TextView topText = activity.findViewById(R.id.top_text_view);
            TextView bottomText = activity.findViewById(R.id.bottom_text_view);
            topLayout.setBackgroundColor(Color.parseColor(values[0]));
            bottomLayout.setBackgroundColor(Color.parseColor(newColor(values[0])));
            topText.setText(values[1]);
            bottomText.setTextColor(Color.parseColor(values[0]));
            bottomText.setText(values[2]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            MainActivity activity = activityReference.get();
            if (activity == null) return;
            TextView topText = activity.findViewById(R.id.top_text_view);
            TextView bottomText = activity.findViewById(R.id.bottom_text_view);
            Button button = activity.findViewById(R.id.main_button);
            String done = "Done!";
            int currentTextColor = bottomText.getCurrentTextColor();
            topText.setTextColor(Color.BLACK);
            topText.setText(done);
            bottomText.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
            button.setBackgroundColor(currentTextColor);
        }

        private String newColor(String color) {
            StringBuilder sb = new StringBuilder(color);
            sb.insert(1, "66");
            return sb.toString();
        }
    }

    public void buttonOnClick(View view) {
        ColorSwitcher colorSwitcher = new ColorSwitcher(this);
        colorSwitcher.execute();
    }
}
