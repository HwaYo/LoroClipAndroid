package com.loroclip;

import android.accounts.Account;
import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.Pair;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.keen.client.android.AndroidKeenClientBuilder;
import io.keen.client.java.KeenClient;
import io.keen.client.java.KeenProject;

/**
 * Created by angdev on 15. 6. 2..
 */
public class EventPublisher {
    private static EventPublisher sInstance;
    private static final Object sLock = new Object();
    private boolean mInitialized;
    private KeenClient mClient;
    private String mUdid;
    private Context mContext;

    public static EventPublisher getInstance() {
        if (sInstance == null) {
            synchronized (sLock) {
                if (sInstance == null) {
                    sInstance = new EventPublisher();
                }
            }
        }
        return sInstance;
    }

    private EventPublisher() {
        mInitialized = false;
    }

    public void initialize(Context context) {
        if (mInitialized) {
            return;
        }

        mContext = context;
        mUdid = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        KeenClient client = new AndroidKeenClientBuilder(context).build();
        Resources res = context.getResources();
        String keenProjectId = res.getString(R.string.keen_project_id);
        String keenReadKey = res.getString(R.string.keen_read_key);
        String keenWriteKey = res.getString(R.string.keen_write_key);
        client.setDefaultProject(new KeenProject(keenProjectId, keenWriteKey, keenReadKey));
        KeenClient.initialize(client);

        mClient = KeenClient.client();
        mInitialized = true;
    }

    public void publishEvent(String key) {
        publishEvent(key, (Map)null);
    }

    public void publishEvent(String key, Pair<String, Object>... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (Pair<String, Object> pair : pairs) {
            if (PrimitiveTypeDetector.isPrimitiveType(pair.second)) {
                map.put(pair.first, pair.second);
            } else {
                map.put(pair.first, ObjectMapper.mapObject(pair.second));
            }
        }
        publishEvent(key, map);
    }

    public void publishEvent(String key, Map object) {
        if (object == null) {
            object = new HashMap<String, Object>();
        }

        preludeEvent(object);
        mClient.addEventAsync(key, object);
    }

    private void preludeEvent(Map object) {
        object.put("from", "android");
        object.put("udid", mUdid);
        object.put("uid", getCurrentAccountUid());
    }

    private String getCurrentAccountUid() {
        Account account = LoroClipAccount.getInstance().getPrimaryAccount(mContext);
        if (account == null) {
            return null;
        }
        return account.name;
    }

    private static class ObjectMapper {
        public static Map mapObject(Object object) {
            Gson gson = new Gson();
            String jsonString = gson.toJson(object);
            Map<String, Object> mapped = new HashMap<>();
            mapped = gson.fromJson(jsonString, mapped.getClass());

            return mapped;
        }
    }

    private static class PrimitiveTypeDetector {
        private static final Set<Class<?>> sWrapperTypes = getWrapperTypes();

        public static boolean isPrimitiveType(Object object) {
            return sWrapperTypes.contains(object.getClass());
        }

        private static Set<Class<?>> getWrapperTypes() {
            Set<Class<?>> ret = new HashSet<Class<?>>();
            ret.add(Boolean.class);
            ret.add(Character.class);
            ret.add(Byte.class);
            ret.add(Short.class);
            ret.add(Integer.class);
            ret.add(Long.class);
            ret.add(Float.class);
            ret.add(Double.class);
            ret.add(Void.class);
            return ret;
        }
    }
}
