package kien.instanttranslator.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

public class Network {

  public static boolean isAvailable(Context context) {

    ConnectivityManager cm = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);

    if ( null != cm ) {

      NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());

      return null != capabilities &&
             (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
              capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }

    return false;
  }
}
