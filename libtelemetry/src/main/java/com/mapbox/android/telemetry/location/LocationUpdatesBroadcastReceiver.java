package com.mapbox.android.telemetry.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.telemetry.AppStateUtils;
import com.mapbox.android.telemetry.MapboxTelemetry;
import java.util.List;

/**
 * Broadcast receiver through which location updates are reported.
 * This receiver is optimized for the background location updates use case.
 */
public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = "LocationUpdateReceiver";
  static final String ACTION_LOCATION_UPDATED =
    "com.mapbox.android.telemetry.location.locationupdatespendingintent.action.LOCATION_UPDATED";

  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      if (intent == null) {
        Log.w(TAG, "intent == null");
        return;
      }

      final String action = intent.getAction();
      if (!ACTION_LOCATION_UPDATED.equals(action)) {
        return;
      }

      LocationEngineResult result = LocationEngineResult.extractResult(intent);
      if (result == null) {
        Log.w(TAG, "LocationEngineResult == null");
        return;
      }

      LocationCollectionClient collectionClient =  LocationCollectionClient.getInstance();
      final MapboxTelemetry telemetry = collectionClient.getTelemetry();
      final String sessionId = collectionClient.getSessionId();
      final List<Location> locations = result.getLocations();
      AppStateUtils.getAppState(context, new AppStateUtils.GetAppStateCallback() {
        @Override
        public void onReady(AppStateUtils.AppState state) {
          for (final Location location : locations) {
            if (isThereAnyNaN(location) || isThereAnyInfinite(location)) {
              continue;
            }
            telemetry.push(LocationMapper.create(location, state.toString(), sessionId));
          }
        }
      });

    } catch (Throwable throwable) {
      // TODO: log silent crash
      Log.e(TAG, throwable.toString());
    }
  }

  private static boolean isThereAnyNaN(Location location) {
    return Double.isNaN(location.getLatitude()) || Double.isNaN(location.getLongitude())
      || Double.isNaN(location.getAltitude()) || Float.isNaN(location.getAccuracy());
  }

  private static boolean isThereAnyInfinite(Location location) {
    return Double.isInfinite(location.getLatitude()) || Double.isInfinite(location.getLongitude())
      || Double.isInfinite(location.getAltitude()) || Float.isInfinite(location.getAccuracy());
  }
}
