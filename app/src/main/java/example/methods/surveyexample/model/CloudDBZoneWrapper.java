package example.methods.surveyexample.model;

import static example.methods.surveyexample.push.PushManager.pushToken;
import static example.methods.surveyexample.push.PushManager.sendNotification;

import android.content.Context;
import android.content.SharedPreferences;
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
    public static int yes, no;
    private AGConnectCloudDB mCloudDB;

    public static final String TAG_dB = "DB_ZONE_WRAPPER";
    private CloudDBZone mCloudDBZone;

    private ListenerHandler mRegister;

    private CloudDBZoneConfig mConfig;

    private UiCallBack mUiCallBack = UiCallBack.DEFAULT;

    private int mBookIndex = -1;

    private ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();

    private OnSnapshotListener<Users> mSnapshotListener = new OnSnapshotListener<Users>() {
        @Override
        public void onSnapshot(CloudDBZoneSnapshot<Users> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
            if (e != null) {
                Log.w(TAG, "onSnapshot: " + e.getMessage());
                return;
            }
            CloudDBZoneObjectList<Users> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
            List<Users> userInfoList = new ArrayList<>();
            try {
                if (snapshotObjects != null) {
                    while (snapshotObjects.hasNext()) {
                        Users userInfo = snapshotObjects.next();
                        userInfoList.add(userInfo);
                        updateUserIndex(userInfo);
                        //updateBookIndex(bookInfo);
                    }
                }
            } catch (AGConnectCloudDBException snapshotException) {
                Log.w(TAG, "onSnapshot:(getObject) " + snapshotException.getMessage());
            } finally {
                cloudDBZoneSnapshot.release();
            }
        }
    };

    private void updateUserIndex(Users users) {
        try {
            mReadWriteLock.writeLock().lock();
            if (mBookIndex < users.getId()) {
                mBookIndex = users.getId();
                Log.w(TAG, "updateUserIndex: " + mBookIndex);
            }
        } finally {
            mReadWriteLock.writeLock().unlock();
        }
    }

    public int getUserIndex() {
        try {
            mReadWriteLock.readLock().lock();
            return mBookIndex;
        } finally {
            mReadWriteLock.readLock().unlock();
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
            openCloudDBZone();
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

    public void openCloudDBZoneV2(Context context, String email) {
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

                getAllBooks(context, false);
                getAllUsers(context, email);

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
            public void onUserAddOrQuery(List<Users> userList) {
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

        void onUserAddOrQuery(List<Users> userList);

        void isDataUpsert(Boolean state);

        void updateUiOnError(String errorMessage);
    }

    public void upsertUserInfos(Users userInfo, Context context) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(userInfo);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                Log.i(TAG, "Upsert " + cloudDBZoneResult + " records 123");
                getAllUsers(context);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                mUiCallBack.updateUiOnError("Insert book info failed");
            }
        });
    }

    public void upsertBookInfos(Votes bookInfo, Context context) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Log.i(TAG, "upsertBookInfos: " + bookInfo.getId() + " " + bookInfo.getYes() + " " + bookInfo.getNo());

        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("check", true);
        editor.commit();

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(bookInfo);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                Log.i(TAG, "Upsert " + cloudDBZoneResult + " records");


                getAllBooks(context, true);
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

    public void getAllUsers(Context context, String signInUserEmail) {
        if (mCloudDBZone == null) {
            Log.w(TAG_dB, "GET USER DETAIL : CloudDBZone is null, try re-open it USERS");
            return;
        }
        Task<CloudDBZoneSnapshot<Users>> queryTask = mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(Users.class),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(new OnSuccessListener<CloudDBZoneSnapshot<Users>>() {
            @Override
            public void onSuccess(CloudDBZoneSnapshot<Users> snapshot) {
                userListResult(snapshot, context, signInUserEmail);
                Log.w(TAG_dB, "GET USER DETAIL : GoResults: USERS");
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

    public void getAllUsers(Context context) {
        if (mCloudDBZone == null) {
            Log.w(TAG_dB, "GET USER DETAIL : CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<Users>> queryTask = mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(Users.class),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(new OnSuccessListener<CloudDBZoneSnapshot<Users>>() {
            @Override
            public void onSuccess(CloudDBZoneSnapshot<Users> snapshot) {
                userListResult(snapshot, context);
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

    public void getAllBooks(Context context, Boolean control) {
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
                bookListResult(snapshot, context, control);
                Log.w(TAG_dB, "GET USER DETAIL : GoResults: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "onFailure: FAILED " + e.getMessage());
                if (mUiCallBack != null) {
                    mUiCallBack.updateUiOnError("GET USER DETAIL : Query user list from cloud failed");
                }
            }
        });
    }

    private void userListResult(CloudDBZoneSnapshot<Users> snapshot, Context context, String signInUserEmail) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        CloudDBZoneObjectList<Users> userInfoCursor = snapshot.getSnapshotObjects();
        List<Users> userInfoList = new ArrayList<>();

        try {
            while (userInfoCursor.hasNext()) {
                Users userInfo = userInfoCursor.next();
                userInfoList.add(userInfo);
                Log.w(TAG_dB, "USER DETAIL RESULT : processQueryResult: " + userInfo.getEmail() + " " + userInfo.getId());
                Log.w(TAG_dB, "userListResult: " + signInUserEmail);

                updateUserIndex(userInfo);
                if (signInUserEmail.equals(userInfo.getEmail())) {
                    editor.putBoolean("check", true);
                    editor.commit();
                    Log.w(TAG_dB, "USER DETAIL RESULT ENTERED : processQueryResult: " + signInUserEmail);
                } else {

                    editor.putBoolean("check", false);
                    editor.commit();
                }
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG_dB, "USER DETAIL RESULT : processQueryResult: " + e.getMessage());
        }
        snapshot.release();

        //updateUserIndex(userInfoList.get(userInfoList.size()));

        if (mUiCallBack != null) {
            mUiCallBack.onUserAddOrQuery(userInfoList);

        }
    }

    private void userListResult(CloudDBZoneSnapshot<Users> snapshot, Context context) {
        CloudDBZoneObjectList<Users> userInfoCursor = snapshot.getSnapshotObjects();
        List<Users> userInfoList = new ArrayList<>();

        try {
            while (userInfoCursor.hasNext()) {
                Users userInfo = userInfoCursor.next();
                userInfoList.add(userInfo);
                Log.w(TAG_dB, "USER DETAIL RESULT : processQueryResult: ss " + userInfo.getEmail() + " " + userInfo.getId());
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG_dB, "USER DETAIL RESULT : processQueryResult: " + e.getMessage());
        }
        snapshot.release();
        if (mUiCallBack != null) {
            mUiCallBack.onUserAddOrQuery(userInfoList);

        }
    }

    private void bookListResult(CloudDBZoneSnapshot<Votes> snapshot, Context context, Boolean control) {
        CloudDBZoneObjectList<Votes> userInfoCursor = snapshot.getSnapshotObjects();
        List<Votes> userInfoList = new ArrayList<>();
        try {
            while (userInfoCursor.hasNext()) {
                Votes userInfo = userInfoCursor.next();
                userInfoList.add(userInfo);
                Log.w(TAG_dB, "USER DETAIL RESULT : processQueryResult: " + userInfo.getYes() + " " + userInfo.getNo());
                yes = userInfoList.get(0).getYes();
                no = userInfoList.get(0).getNo();
                if (context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getBoolean("check", false) & control) {
                    sendNotification(pushToken, 0, context);
                }
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG_dB, "USER DETAIL RESULT : processQueryResult: " + e.getMessage());
        }
        snapshot.release();
        if (mUiCallBack != null) {
            mUiCallBack.onAddOrQuery(userInfoList);

        }
    }
}
