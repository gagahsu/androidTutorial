package net.macdidi.androidtutorial;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity
            implements ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    // Google API用戶端物件
    private GoogleApiClient googleApiClient;

    // Location請求物件
    private LocationRequest locationRequest;

    // 記錄目前最新的位置
    private Location currentLocation;

    // 顯示目前與儲存位置的標記物件
    private Marker currentMarker, itemMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        // 建立Google API用戶端物件
        configGoogleApiClient();

        // 建立Location請求物件
        configLocationRequest();

        // 讀取記事儲存的座標
        Intent intent = getIntent();
        double lat = intent.getDoubleExtra("lat", 0.0);
        double lng = intent.getDoubleExtra("lng", 0.0);

        // 如果記事已經儲存座標
        if (lat != 0.0 && lng != 0.0) {
            // 建立座標物件
            LatLng itemPlace = new LatLng(lat, lng);
            // 加入地圖標記
            addMarker(itemPlace, intent.getStringExtra("title"),
                    intent.getStringExtra("datetime"));
            // 移動地圖
            moveMap(itemPlace);
        }
        else {
            // 連線到Google API用戶端
            if (!googleApiClient.isConnected()) {
                googleApiClient.connect();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        // 連線到Google API用戶端
        if (!googleApiClient.isConnected() && currentMarker != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 移除位置請求服務
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // 移除Google API用戶端連線
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    // ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        // 已經連線到Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, MapsActivity.this);
    }

    // ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int i) {
        // Google Services連線中斷
        // int參數是連線中斷的代號
    }

    // OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Google Services連線失敗
        // ConnectionResult參數是連線失敗的資訊
    }

    // LocationListener
    @Override
    public void onLocationChanged(Location location) {
        // 位置改變
        // Location參數是目前的位置
        currentLocation = location;
        LatLng latLng = new LatLng(
                location.getLatitude(), location.getLongitude());

        // 設定目前位置的標記
        if (currentMarker == null) {
            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng));
        }
        else {
            currentMarker.setPosition(latLng);
        }

        // 移動地圖到目前的位置
        moveMap(latLng);
    }

    // 建立Google API用戶端物件
    private synchronized void configGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    // 建立Location請求物件
    private void configLocationRequest() {
        locationRequest = new LocationRequest();
        // 設定讀取位置資訊的間隔時間為一秒（1000ms）
        locationRequest.setInterval(1000);
        // 設定讀取位置資訊最快的間隔時間為一秒（1000ms）
        locationRequest.setFastestInterval(1000);
        // 設定優先讀取高精確度的位置資訊（GPS）
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().
                    findFragmentById(R.id.map)).getMap();

            if (mMap != null) {
                // 移除地圖設定
                //setUpMap();
                processController();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        // 建立位置的座標物件
        LatLng place = new LatLng(25.033408, 121.564099);
        // 移動地圖
        moveMap(place);

        // 加入地圖標記
        addMarker(place, "Hello!", " Google Maps v2!");
    }

    // 移動地圖到參數指定的位置
    private void moveMap(LatLng place) {
        // 建立地圖攝影機的位置物件
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(place)
                        .zoom(17)
                        .build();

        // 使用動畫的效果移動地圖
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        addMarker(place, "Hello!", " Google Maps v2!");
    }

    // 在地圖加入指定位置與標題的標記
    private void addMarker(LatLng place, String title, String snippet) {
        BitmapDescriptor icon =
                BitmapDescriptorFactory.fromResource(R.drawable.ic_action_place);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(place)
                .title(title)
                .snippet(snippet)
                .icon(icon);

        // 加入並設定記事儲存的位置標記
        itemMarker = mMap.addMarker(markerOptions);
    }

    private void processController() {
        // 對話框按鈕事件
        final DialogInterface.OnClickListener listener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            // 更新位置資訊
                            case DialogInterface.BUTTON_POSITIVE:
                                // 連線到Google API用戶端
                                if (!googleApiClient.isConnected()) {
                                    googleApiClient.connect();
                                }
                                break;
                            // 清除位置資訊
                            case DialogInterface.BUTTON_NEUTRAL:
                                Intent result = new Intent();
                                result.putExtra("lat", 0);
                                result.putExtra("lng", 0);
                                setResult(Activity.RESULT_OK, result);
                                finish();
                                break;
                            // 取消
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

        // 標記訊息框點擊事件
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                // 如果是記事儲存的標記
                if (marker.equals(itemMarker)) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(MapsActivity.this);

                    ab.setTitle(R.string.title_update_location)
                            .setMessage(R.string.message_update_location)
                            .setCancelable(true);

                    ab.setPositiveButton(R.string.update, listener);
                    ab.setNeutralButton(R.string.clear, listener);
                    ab.setNegativeButton(android.R.string.cancel, listener);

                    ab.show();
                }
            }
        });

        // 標記點擊事件
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // 如果是目前位置標記
                if (marker.equals(currentMarker)) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(MapsActivity.this);

                    ab.setTitle(R.string.title_current_location)
                            .setMessage(R.string.message_current_location)
                            .setCancelable(true);

                    ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent result = new Intent();
                            result.putExtra("lat", currentLocation.getLatitude());
                            result.putExtra("lng", currentLocation.getLongitude());
                            setResult(Activity.RESULT_OK, result);
                            finish();
                        }
                    });
                    ab.setNegativeButton(android.R.string.cancel, null);

                    ab.show();

                    return true;
                }

                return false;
            }
        });
    }
}
