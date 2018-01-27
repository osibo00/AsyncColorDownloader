package productions.darthplagueis.asynccolordownloader;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AsyncFragment extends Fragment {

    public static final String TAG_ASYNC_FRAGMENT = "async_fragment";
    private static TaskStatusCallBack callBack;
    private static ColorDataBase colorDataBase;
    private static boolean doesDatabaseExist;
    private ColorSwitcher colorSwitcher;
    private boolean isTaskExecuting = false;

    public AsyncFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TaskStatusCallBack) {
            callBack = (TaskStatusCallBack) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TaskStatusCallBack");
        }
        colorDataBase = ColorDataBase.getInstance(context);
        File dbFile = context.getDatabasePath(ColorDataBase.DATABASE_NAME);
        doesDatabaseExist = dbFile.exists();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callBack = null;
    }

    private static class ColorDownloader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            return colorDownload(strings[0]);
        }

        @Override
        protected void onPostExecute(String data) {
            if (doesDatabaseExist) {
                ColorSwitcher colorSwitcher = new ColorSwitcher();
                colorSwitcher.execute();
                Log.d("ColorDownloader", "onPostExecute: database exists");
                return;
            }
            if (data == null) return;
            Type collectionType = new TypeToken<HashMap<String, String>>() {
            }.getType();
            Gson gson = new Gson();
            HashMap<String, String> hexColors = gson.fromJson(new StringReader(data), collectionType);
            Log.d("ColorDownloader", "onPostExecute: Size " + hexColors.size());
            for (Map.Entry<String, String> entry : hexColors.entrySet()) {
                colorDataBase.insertColor(entry.getKey(), entry.getValue());
            }
            ColorSwitcher colorSwitcher = new ColorSwitcher();
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

    public void startColorDownloadTask() {
        if (!isTaskExecuting) {
            ColorDownloader colorDownloader = new ColorDownloader();
            colorDownloader.execute("https://raw.githubusercontent.com/operable/cog/master/priv/css-color-names.json");
            isTaskExecuting = true;
        }
    }

    private static class ColorSwitcher extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {
            if (callBack != null) {
                callBack.onPreExecute();
            }
            Log.d("ColorSwitcher", "onPreExecute: executing");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                int count = 1;
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
            if (callBack != null) {
                callBack.onProgressUpdate(values[0], values[1], newColor(values[0]), values[2]);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (callBack != null) {
                callBack.onPostExecute();
            }
        }

        @Override
        protected void onCancelled(Void aVoid) {
            if (callBack != null) {
                callBack.onCancelled();
            }
        }

        private String newColor(String color) {
            StringBuilder sb = new StringBuilder(color);
            sb.insert(1, "66");
            return sb.toString();
        }
    }

    public void startColorSwitcherTask() {
        if (!isTaskExecuting) {
            colorSwitcher = new ColorSwitcher();
            colorSwitcher.execute();
            isTaskExecuting = true;
        }
    }

    public void cancelColorSwitcherTask() {
        if (isTaskExecuting) {
            colorSwitcher.cancel(true);
            isTaskExecuting = false;
        }
    }

    public void updateExecutingStatus(boolean isExecuting) {
        this.isTaskExecuting = isExecuting;
    }

    public interface TaskStatusCallBack {
        void onPreExecute();

        void onProgressUpdate(String hexValue, String colorName, String newColor, String count);

        void onPostExecute();

        void onCancelled();
    }

}
