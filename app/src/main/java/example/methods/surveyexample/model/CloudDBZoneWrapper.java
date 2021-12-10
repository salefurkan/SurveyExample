package example.methods.surveyexample.model;

import static example.methods.surveyexample.push.PushManager.pushToken;
import static example.methods.surveyexample.push.PushManager.sendNotification;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.agconnect.cloud.database.OnSnapshotListener;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class CloudDBZoneWrapper {
    private static final String TAG = "CloudDBZoneWrapper";
    public static int yes,no;
    private AGConnectCloudDB mCloudDB;

    public static final String TAG_dB = "DB_ZONE_WRAPPER";
    private CloudDBZone mCloudDBZone;

    private ListenerHandler mRegister;

    private CloudDBZoneConfig mConfig;

    private UiCallBack mUiCallBack = UiCallBack.DEFAULT;

    private int mBookIndex = 0;

    private ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();

    private OnSnapshotListener<Votes> mSnapshotListener = new OnSnapshotListener<Votes>() {
        @Override
        public void onSnapshot(CloudDBZoneSnapshot<Votes> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
            if (e != null) {
                Log.w(TAG, "onSnapshot: " + e.getMessage());
                return;
            }
            CloudDBZoneObjectList<Votes> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
            List<Votes> bookInfoList = new ArrayList<>();
            try {
                if (snapshotObjects != null) {
                    while (snapshotObjects.hasNext()) {
                        Votes bookInfo = snapshotObjects.next();
                        bookInfoList.add(bookInfo);
                        updateBookIndex(bookInfo);
                    }
                }
            } catch (AGConnectCloudDBException snapshotException) {
                Log.w(TAG, "onSnapshot:(getObject) " + snapshotException.getMessage());
            } finally {
                cloudDBZoneSnapshot.release();
            }
        }
    };

    private void updateBookIndex(Votes bookInfo) {
        try {
            mReadWriteLock.writeLock().lock();
            if (mBookIndex < bookInfo.getId()) {
                mBookIndex = bookInfo.getId();
            }
        } finally {
            mReadWriteLock.writeLock().unlock();
        }
    }

    public CloudDBZoneWrapper() {
        Log.i(TAG, "CloudDBZoneWrapper: worked");
        mCloudDB = AGConnectCloudDB.getInstance();
    }

    public static void initAGConnectCloudDB(Context context) {
        AGConnectCloudDB.initialize(context);
        Log.w(TAG_dB, "initAGConnectCloudDB");
    }

    public void createObjectType() {
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
            Log.w(TAG_dB, "createObjectTypeSuccess ");
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG_dB, "createObjectTypeError: " + e.getMessage());
        }
    }

    public void openCloudDBZone() {
        mConfig = new CloudDBZoneConfig("Pulls",
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Log.w(TAG_dB, "openCloudDBZoneSuccess ");
        try {
            mCloudDBZone = mCloudDB.openCloudDBZone(mConfig, true);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG_dB, "openCloudDBZoneError: " + e.getMessage());
        }
    }

    public void closeCloudDBZone() {
        try {
            mCloudDB.closeCloudDBZone(mCloudDBZone);
            Log.w(TAG_dB, "closeCloudDBZoneSuccess ");
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG_dB, "closeCloudDBZoneError: " + e.getMessage());
        }
    }

    public void openCloudDBZoneV2(Context context) {
        mConfig = new CloudDBZoneConfig("Pulls",
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);

        Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
        openDBZoneTask.addOnSuccessListener(new OnSuccessListener<CloudDBZone>() {
            @Override
            public void onSuccess(CloudDBZone cloudDBZone) {
                Log.i(TAG_dB, "Open cloudDBZone success");
                mCloudDBZone = cloudDBZone;

                getAllUsers(context);
                if (mCloudDB == null) {
                    Log.i(TAG, "onSuccess: mCloudDBZone is null");
                }
                if (cloudDBZone == null) {
                    Log.i(TAG, "onSuccess: cloudDBZone is null");
                }
                // Add subscription after opening cloudDBZone success
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "Open cloudDBZone failed for " + e.getMessage());
            }
        });
    }

    public interface UiCallBack {
        UiCallBack DEFAULT = new UiCallBack() {
            @Override
            public void onAddOrQuery(List<Votes> bookInfoList) {
                Log.i(TAG, "Using default onAddOrQuery");
            }

            @Override
            public void isDataUpsert(Boolean state) {

                Log.i(TAG, "Using add value");
            }

            @Override
            public void updateUiOnError(String errorMessage) {
                Log.i(TAG, "Using default updateUiOnError");
            }
        };

        void onAddOrQuery(List<Votes> bookInfoList);

        void isDataUpsert(Boolean state);

        void updateUiOnError(String errorMessage);
    }

    public void upsertBookInfos(Votes bookInfo,Context context) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Log.i(TAG, "upsertBookInfos: "+bookInfo.getId()+" "+bookInfo.getYes()+" "+bookInfo.getNo());

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(bookInfo);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                Log.i(TAG, "Upsert " + cloudDBZoneResult + " records");
                getAllUsers(context);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, "onFailure: upsert errror");
                mUiCallBack.updateUiOnError("Insert book info failed");
            }
        });
    }

    private void processQueryResult(CloudDBZoneSnapshot<Votes> snapshot) {
        CloudDBZoneObjectList<Votes> bookInfoCursor = snapshot.getSnapshotObjects();
        List<Votes> bookInfoList = new ArrayList<>();
        try {
            while (bookInfoCursor.hasNext()) {
                Votes bookInfo = bookInfoCursor.next();
                bookInfoList.add(bookInfo);
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "processQueryResult: " + e.getMessage());
        } finally {
            snapshot.release();
        }
        mUiCallBack.onAddOrQuery(bookInfoList);
    }

    public void queryAllBooks() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<Votes>> queryTask = mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(Votes.class),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(new OnSuccessListener<CloudDBZoneSnapshot<Votes>>() {
            @Override
            public void onSuccess(CloudDBZoneSnapshot<Votes> snapshot) {
                processQueryResult(snapshot);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                mUiCallBack.updateUiOnError("Query book list from cloud failed");
            }
        });
    }

    public void addCallBacks(UiCallBack uiCallBack) {
        mUiCallBack = uiCallBack;
    }


    private static final class MyHandler extends Handler {
        public void handleMessage(@NonNull Message msg) {
            // dummy
        }

        @Override
        public void publish(LogRecord record) {

        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }
    }

    public void getAllUsers(Context context) {
        if (mCloudDBZone == null) {
            Log.w(TAG_dB, "GET USER DETAIL : CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<Votes>> queryTask = mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(Votes.class),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(new OnSuccessListener<CloudDBZoneSnapshot<Votes>>() {
            @Override
            public void onSuccess(CloudDBZoneSnapshot<Votes> snapshot) {
                userListResult(snapshot,context);
                Log.w(TAG_dB, "GET USER DETAIL : GoResults: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "onFailure: FAILED" + e.getMessage());
                if (mUiCallBack != null) {
                    mUiCallBack.updateUiOnError("GET USER DETAIL : Query user list from cloud failed");
                }
            }
        });
    }

    private void userListResult(CloudDBZoneSnapshot<Votes> snapshot,Context context) {
        CloudDBZoneObjectList<Votes> userInfoCursor = snapshot.getSnapshotObjects();
        List<Votes> userInfoList = new ArrayList<>();
        try {
            while (userInfoCursor.hasNext()) {
                Votes userInfo = userInfoCursor.next();
                userInfoList.add(userInfo);
                Log.w(TAG_dB, "USER DETAIL RESULT : processQueryResult: "+userInfo.getYes()+ " "+userInfo.getNo());
                    yes =userInfoList.get(0).getYes();
                    no =userInfoList.get(0).getNo();
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG_dB, "USER DETAIL RESULT : processQueryResult: " + e.getMessage());
        }
        sendNotification(pushToken,0,context);
        snapshot.release();
        if (mUiCallBack != null) {
            mUiCallBack.onAddOrQuery(userInfoList);

        }
    }
}
