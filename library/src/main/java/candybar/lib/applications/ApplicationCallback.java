package candybar.lib.applications;

import androidx.annotation.NonNull;

/**
 * Author: Dani Mahardhika
 * Created on: 10/31/2017
 * https://github.com/danimahardhika
 */

interface ApplicationCallback {

    @NonNull
    CandyBarApplication.Configuration onInit();
}
