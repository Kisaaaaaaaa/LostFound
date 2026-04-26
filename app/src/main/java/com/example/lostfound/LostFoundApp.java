package com.example.lostfound;

import android.app.Application;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.services.core.ServiceSettings;

public class LostFoundApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 在任何高德组件启动前，必须设置隐私合规
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);
        ServiceSettings.updatePrivacyShow(this, true, true);
        ServiceSettings.updatePrivacyAgree(this, true);
    }
}
