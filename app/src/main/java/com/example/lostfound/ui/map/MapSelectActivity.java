package com.example.lostfound.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.example.lostfound.databinding.ActivityMapSelectBinding;

import java.security.MessageDigest;
import java.util.Locale;

public class MapSelectActivity extends AppCompatActivity implements GeocodeSearch.OnGeocodeSearchListener {

    private static final String TAG = "AMapDebug";
    private ActivityMapSelectBinding binding;
    private AMap aMap;
    private GeocodeSearch geocoderSearch;
    private LatLng selectedLatLng;
    private String selectedAddress;
    private boolean isFirstLocation = true;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    // 默认坐标：武汉 (114.3350, 30.5096)
    private static final LatLng DEFAULT_LATLNG = new LatLng(30.5096, 114.3350);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 打印调试信息，帮助排查 1804 错误
        printDebugInfo();

        binding = ActivityMapSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.mapView.onCreate(savedInstanceState);
        initMap();

        try {
            geocoderSearch = new GeocodeSearch(this);
            geocoderSearch.setOnGeocodeSearchListener(this);
        } catch (AMapException e) {
            Log.e(TAG, "初始化异常: " + e.getErrorMessage());
        }

        // 搜索按钮点击事件
        binding.btnSearch.setOnClickListener(v -> {
            String keyword = binding.etSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchAddress(keyword);
                // 隐藏键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        binding.btnConfirm.setOnClickListener(v -> {
            if (selectedLatLng != null && selectedAddress != null && !selectedAddress.contains("正在") && !selectedAddress.contains("失败")) {
                Intent intent = new Intent();
                intent.putExtra("lat", selectedLatLng.latitude);
                intent.putExtra("lng", selectedLatLng.longitude);
                intent.putExtra("address", selectedAddress);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "请选择一个有效的地址", Toast.LENGTH_SHORT).show();
            }
        });

        checkPermission();
    }

    private void printDebugInfo() {
        try {
            Log.d(TAG, "Checking PackageName: " + getPackageName());
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA1");
                md.update(signature.toByteArray());
                byte[] digest = md.digest();
                StringBuilder toHex = new StringBuilder();
                for (byte b : digest) {
                    toHex.append(String.format(Locale.US, "%02X:", b));
                }
                String sha1 = toHex.substring(0, toHex.length() - 1);
                Log.d(TAG, "REAL RUNTIME SHA1 (Update this in AMap Console): " + sha1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Debug info error", e);
        }
    }

    private void searchAddress(String keyword) {
        if (geocoderSearch != null) {
            GeocodeQuery query = new GeocodeQuery(keyword, "");
            geocoderSearch.getFromLocationNameAsyn(query);
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void initMap() {
        if (aMap == null) aMap = binding.mapView.getMap();

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        myLocationStyle.showMyLocation(true);
        aMap.setMyLocationStyle(myLocationStyle);

        aMap.setOnMyLocationChangeListener(location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();

                if (isFirstLocation) {
                    isFirstLocation = false;
                    LatLng targetLatLng;
                    
                    if (Math.abs(lat) < 0.001 && Math.abs(lng) < 0.001) {
                        targetLatLng = DEFAULT_LATLNG;
                    } else {
                        targetLatLng = new LatLng(lat, lng);
                    }
                    
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng, 16));
                    getAddressByLatlng(targetLatLng);
                }
            }
        });

        aMap.setMyLocationEnabled(true);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);

        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override public void onCameraChange(CameraPosition cameraPosition) {}
            @Override public void onCameraChangeFinish(CameraPosition cameraPosition) {
                if (!isFirstLocation) {
                    selectedLatLng = cameraPosition.target;
                    getAddressByLatlng(selectedLatLng);
                }
            }
        });
    }

    private void getAddressByLatlng(LatLng latLng) {
        if (geocoderSearch != null && Math.abs(latLng.latitude) > 0.001) {
            runOnUiThread(() -> binding.tvAddress.setText("正在解析地址..."));
            LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
            RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
            geocoderSearch.getFromLocationAsyn(query);
        }
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == 1000 && result != null && result.getRegeocodeAddress() != null) {
            RegeocodeAddress addr = result.getRegeocodeAddress();
            String formatAddr = addr.getFormatAddress();
            if (formatAddr == null || formatAddr.isEmpty()) {
                formatAddr = addr.getProvince() + addr.getCity() + addr.getDistrict();
            }
            selectedAddress = formatAddr;
            runOnUiThread(() -> binding.tvAddress.setText(selectedAddress));
        } else {
            Log.e(TAG, "逆地理编码失败，错误码: " + rCode);
            runOnUiThread(() -> {
                binding.tvAddress.setText("解析失败(" + rCode + ")，请检查Key配置");
                selectedAddress = null;
            });
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        if (rCode == 1000 && result != null && result.getGeocodeAddressList() != null 
                && !result.getGeocodeAddressList().isEmpty()) {
            LatLonPoint point = result.getGeocodeAddressList().get(0).getLatLonPoint();
            LatLng target = new LatLng(point.getLatitude(), point.getLongitude());
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 16));
            selectedAddress = result.getGeocodeAddressList().get(0).getFormatAddress();
            runOnUiThread(() -> binding.tvAddress.setText(selectedAddress));
        } else {
            Log.e(TAG, "地理编码失败，错误码: " + rCode);
            Toast.makeText(this, "地址搜索失败(" + rCode + ")", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.length > 0 
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (aMap != null) aMap.setMyLocationEnabled(true);
        }
    }

    @Override protected void onResume() { super.onResume(); binding.mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); binding.mapView.onPause(); }
    @Override protected void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); binding.mapView.onSaveInstanceState(outState); }
    @Override protected void onDestroy() { super.onDestroy(); binding.mapView.onDestroy(); }
}
