package productions.darthplagueis.asynccolordownloader;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements AsyncFragment.TaskStatusCallBack {

    private AsyncFragment asyncFragment;
    private ProgressBar progressBar;
    private LinearLayout topLayout;
    private RelativeLayout bottomLayout;
    private TextView topText;
    private TextView bottomText;
    private Button button;
    private final String TAG_PROGRESS_BAR = "progress_bar";
    private final String TAG_TOP_LAYOUT = "top_layout";
    private final String TAG_BOTTOM_LAYOUT = "bottom_layout";
    private final String TAG_TOP_TEXT = "top_text";
    private final String TAG_TOP_TEXT01 = "top_text01";
    private final String TAG_BOTTOM_TEXT = "bottom_text";
    private final String TAG_BUTTON = "button";
    private final String TAG_BUTTON01 = "button01";
    private final String TAG_DID_COLORS_DOWNLOAD = "did_colors_download";
    private boolean didColorsDownload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progress_bar);
        topLayout = findViewById(R.id.top_layout);
        bottomLayout = findViewById(R.id.bottom_layout);
        topText = findViewById(R.id.top_text_view);
        bottomText = findViewById(R.id.bottom_text_view);
        button = findViewById(R.id.main_button);

        if (savedInstanceState != null) {
            progressBar.setVisibility(savedInstanceState.getInt(TAG_PROGRESS_BAR));
            try {
                didColorsDownload = savedInstanceState.getBoolean(TAG_DID_COLORS_DOWNLOAD);
                topLayout.setVisibility(savedInstanceState.getIntArray(TAG_TOP_LAYOUT)[0]);
                topLayout.setBackgroundColor(savedInstanceState.getIntArray(TAG_TOP_LAYOUT)[1]);
                bottomLayout.setVisibility(savedInstanceState.getIntArray(TAG_BOTTOM_LAYOUT)[0]);
                bottomLayout.setBackgroundColor(savedInstanceState.getIntArray(TAG_BOTTOM_LAYOUT)[1]);
                topText.setVisibility(savedInstanceState.getIntArray(TAG_TOP_TEXT)[0]);
                topText.setTextColor(savedInstanceState.getIntArray(TAG_TOP_TEXT)[1]);
                topText.setText(savedInstanceState.getString(TAG_TOP_TEXT01));
                bottomText.setVisibility(savedInstanceState.getIntArray(TAG_BOTTOM_TEXT)[0]);
                bottomText.setTextColor(savedInstanceState.getIntArray(TAG_BOTTOM_TEXT)[1]);
                button.setVisibility(savedInstanceState.getIntArray(TAG_BUTTON)[0]);
                button.setBackgroundColor(savedInstanceState.getIntArray(TAG_BUTTON)[1]);
                button.setText(savedInstanceState.getString(TAG_BUTTON01));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        asyncFragment = (AsyncFragment) fragmentManager
                .findFragmentByTag(AsyncFragment.TAG_ASYNC_FRAGMENT);
        if (asyncFragment == null) {
            asyncFragment = new AsyncFragment();
            fragmentManager.beginTransaction()
                    .add(asyncFragment, AsyncFragment.TAG_ASYNC_FRAGMENT)
                    .commit();
        }

        if (!didColorsDownload) {
            if (asyncFragment != null) {
                asyncFragment.startColorDownloadTask();
                didColorsDownload = true;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TAG_DID_COLORS_DOWNLOAD, didColorsDownload);
        outState.putInt(TAG_PROGRESS_BAR, progressBar.getVisibility());
        int[] topLayoutArray = new int[]{topLayout.getVisibility(), ((ColorDrawable) topLayout.getBackground()).getColor()};
        outState.putIntArray(TAG_TOP_LAYOUT, topLayoutArray);
        int[] bottomLayoutArray = new int[]{bottomLayout.getVisibility(), ((ColorDrawable) bottomLayout.getBackground()).getColor()};
        outState.putIntArray(TAG_BOTTOM_LAYOUT, bottomLayoutArray);
        int[] topTextArray = new int[]{topText.getVisibility(), topText.getCurrentTextColor()};
        outState.putIntArray(TAG_TOP_TEXT, topTextArray);
        outState.putString(TAG_TOP_TEXT01, topText.getText().toString());
        int[] bottomTextArray = new int[]{bottomText.getVisibility(), bottomText.getCurrentTextColor()};
        outState.putIntArray(TAG_BOTTOM_TEXT, bottomTextArray);
        int[] buttonArray = new int[]{button.getVisibility(), ((ColorDrawable) topLayout.getBackground()).getColor()};
        outState.putIntArray(TAG_BUTTON, buttonArray);
        outState.putString(TAG_BUTTON01, button.getText().toString());
    }

    @Override
    public void onPreExecute() {
        progressBar.setVisibility(View.VISIBLE);
        topLayout.setVisibility(View.VISIBLE);
        bottomText.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
        button.setText(null);
    }

    @Override
    public void onProgressUpdate(String hexValue, String colorName, String newColor, String count) {
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor(hexValue), android.graphics.PorterDuff.Mode.MULTIPLY);
        topLayout.setBackgroundColor(Color.parseColor(hexValue));
        bottomLayout.setBackgroundColor(Color.parseColor(newColor));
        topText.setText(colorName);
        bottomText.setTextColor(Color.parseColor(hexValue));
        bottomText.setText(count);
        button.setBackgroundColor(Color.parseColor(hexValue));
    }

    @Override
    public void onPostExecute() {
        progressBar.setVisibility(View.GONE);
        topText.setTextColor(Color.BLACK);
        String done = "Done!";
        topText.setText(done);
        bottomText.setVisibility(View.GONE);
        button.setVisibility(View.VISIBLE);
        button.setTextColor(Color.BLACK);
        button.setText(getString(R.string.start_again_spaced));
        if (asyncFragment != null) {
            asyncFragment.updateExecutingStatus(false);
        }
    }

    @Override
    public void onCancelled() {

    }

    public void buttonOnClick(View view) {
        if (asyncFragment != null) {
            asyncFragment.startColorSwitcherTask();
        }
    }
}
