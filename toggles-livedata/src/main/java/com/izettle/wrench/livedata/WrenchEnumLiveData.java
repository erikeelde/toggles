package com.izettle.wrench.livedata;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.izettle.wrench.core.Bolt;
import com.izettle.wrench.core.Nut;
import com.izettle.wrench.core.WrenchProviderContract;

class WrenchEnumLiveData<T extends Enum<T>> extends WrenchLiveData<T> {
    @NonNull
    private final Class<T> enumClass;
    @NonNull
    private final T defValue;

    WrenchEnumLiveData(@NonNull Context context, @NonNull String key, @NonNull Class<T> enumClass, @NonNull T defValue) {
        super(context, key, Bolt.TYPE.ENUM);
        this.enumClass = enumClass;
        this.defValue = defValue;
    }

    @Nullable
    private Uri insertBolt(@NonNull Bolt bolt) {
        return getContext().getContentResolver().insert(WrenchProviderContract.boltUri(), bolt.toContentValues());
    }

    private void insertNut(@NonNull Nut nut) {
        getContext().getContentResolver().insert(WrenchProviderContract.nutUri(), nut.toContentValues());
    }

    @Override
    void boltChanged(@Nullable Bolt bolt) {
        if (bolt == null) {
            postValue(defValue);
            return;
        }

        if (bolt.getId() == 0) {
            bolt = bolt.copy(bolt.getId(), getType(), getKey(), String.valueOf(defValue));
            Uri uri = insertBolt(bolt);
            if (uri == null) {
                throw new IllegalStateException("uri was null after insert");
            }
            bolt.setId(Long.parseLong(uri.getLastPathSegment()));

            for (T t : enumClass.getEnumConstants()) {
                insertNut(new Nut(bolt.getId(), t.toString()));
            }
        }

        if (bolt.getValue() == null) {
            throw new IllegalStateException("bolt value cannot be null for enum");
        }
        postValue(Enum.valueOf(enumClass, bolt.getValue()));
    }
}
