package com.izettle.wrench.livedata;

import android.content.Context;
import android.net.Uri;

import com.izettle.wrench.core.Bolt;
import com.izettle.wrench.core.WrenchProviderContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class WrenchBooleanLiveData extends WrenchLiveData<Boolean> {
    private final boolean defValue;

    WrenchBooleanLiveData(@NonNull Context context, @NonNull String key, boolean defValue) {
        super(context, key, Bolt.TYPE.BOOLEAN);

        this.defValue = defValue;
    }

    @Override
    void boltChanged(@Nullable Bolt bolt) {
        if (bolt == null) {
            setValue(defValue);
            return;
        }

        if (bolt.getId() == 0) {
            bolt = bolt.copy(bolt.getId(), getType(), getKey(), String.valueOf(defValue));
            Uri uri = getContext().getContentResolver().insert(WrenchProviderContract.boltUri(), bolt.toContentValues());
            if (uri == null) {
                throw new IllegalStateException("uri was null after insert");
            }
            bolt.setId(Long.parseLong(uri.getLastPathSegment()));
        }
        setValue(Boolean.valueOf(bolt.getValue()));
    }
}
