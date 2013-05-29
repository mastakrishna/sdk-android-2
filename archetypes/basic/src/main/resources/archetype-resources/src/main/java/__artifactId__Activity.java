#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.req.OpenRequest;
import com.playhaven.android.view.FullScreen;
import com.playhaven.android.view.PlayHavenView;

/**
 * A simple example that launches a content unit
 */
public class ${artifactId}Activity extends Activity {
    private static final String TAG = ${artifactId}Activity.class.getSimpleName();

    /**
     * Called when the Activity is created
     *
     * @param savedInstanceState from the previous run
     */
    public void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);

        /**
         * Set our layout to src/main/android/res/layout/main.xml
         */
        setContentView(R.layout.main);

        try {
            /**
             * Here, we configure PlayHaven to use the Token and Secret specified in the Dashboard
             * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
             *
             * In this example, we are grabbing the value of the token and secret from
             * src/main/android/res/values/strings.xml
             */
            PlayHaven.configure(this, R.string.token, R.string.secret);

            /**
             * Send an Open Request first.
             */
            (new OpenRequest()).send(this);

            /**
             * Now we start the new Activity.
             *
             * We'll use PlayHaven's convenience method to create the appropriate intent.
             * The first parameter is our Context (ie: this Activity)
             * The second parameter is our placement tag as defined in the Dashboard
             *
             * Once the ad is closed, this Activity will be resumed.
             */
            startActivity(FullScreen.createIntent(this, "${placement}"));
        } catch (PlayHavenException e) {
                Log.e(TAG, "We have encountered an error", e);
        }
    }
}
