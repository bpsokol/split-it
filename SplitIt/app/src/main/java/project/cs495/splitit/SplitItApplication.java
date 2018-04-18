package project.cs495.splitit;

import android.app.Application;

import com.microblink.ReceiptSdk;

public class SplitItApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ReceiptSdk.sdkInitialize(getApplicationContext());
    }
}
