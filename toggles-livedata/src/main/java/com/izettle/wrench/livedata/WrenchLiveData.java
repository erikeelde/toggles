package com.izettle.wrench.livedata;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.izettle.wrench.core.Bolt;
import com.izettle.wrench.core.WrenchProviderContract;

public abstract class WrenchLiveData<T> extends LiveData<T> {
    @NonNull
    private final String key;
    @NonNull
    @Bolt.BoltType
    private final String type;
    @NonNull
    private final Context context;
    @NonNull
    private final BoltContentObserver boltContentObserver;

    WrenchLiveData(@NonNull Context context, @NonNull String key, @NonNull @Bolt.BoltType String type) {
        this.context = context;
        this.key = key;
        this.type = type;
        boltContentObserver = new BoltContentObserver(new Handler());
    }

    @NonNull
    public static WrenchLiveData<String> create(@NonNull Context context, @NonNull String key, @Nullable String def) {
        return new WrenchStringLiveData(context, key, def);
    }

    @NonNull
    public static WrenchLiveData<Boolean> create(@NonNull Context context, @NonNull String key, boolean def) {
        return new WrenchBooleanLiveData(context, key, def);
    }

    @NonNull
    public static WrenchLiveData<Integer> create(@NonNull Context context, @NonNull String key, int def) {
        return new WrenchIntLiveData(context, key, def);
    }

    @NonNull
    public static <T extends Enum<T>> WrenchLiveData<T> create(@NonNull Context context, @NonNull String key, @NonNull Class<T> enumClass, @NonNull T def) {
        return new WrenchEnumLiveData<>(context, key, enumClass, def);
    }

    @Nullable
    private static Bolt getBolt(@NonNull ContentResolver contentResolver, @NonNull @Bolt.BoltType String type, @NonNull String key) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(WrenchProviderContract.boltUri(key),
                    null,
                    null,
                    null,
                    null);
            if (cursor == null) {
                return null;
            }

            if (cursor.moveToFirst()) {
                return Bolt.fromCursor(cursor);
            }


        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return new Bolt(0L, type, key, "");
    }

    @NonNull
    String getType() {
        return type;
    }

    @NonNull
    Context getContext() {
        return context;
    }

    @NonNull
    String getKey() {
        return key;
    }

    @Override
    protected void onActive() {
        ProviderInfo providerInfo = context.getPackageManager().resolveContentProvider(WrenchProviderContract.WRENCH_AUTHORITY, 0);
        if (providerInfo != null) {
            context.getContentResolver()
                    .registerContentObserver(WrenchProviderContract.boltUri(), true, boltContentObserver);

        }
        onChange();
    }

    @Override
    protected void onInactive() {
        context.getContentResolver().unregisterContentObserver(boltContentObserver);
    }

    private void onChange() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                boltChanged(getBolt(context.getContentResolver(), type, key));
            }
        });
    }

    abstract void boltChanged(@Nullable Bolt bolt);

    class BoltContentObserver extends ContentObserver {
        BoltContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            WrenchLiveData.this.onChange();
        }
    }
}