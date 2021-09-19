package com.iyond.minigame.play_game_service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * PlayGameServicePlugin
 */
public class PlayGameServicePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Context context;
    private ActivityPluginBinding activityPluginBinding;
    private Activity activity;
    private static final int RC_SIGNIN = 9001;
    private static final int RC_SNAPSHOT = 9002;

    private Result lastResult;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "play_game_service");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
    }

    //  private <T> T parseValue(Object val, T defaultValue, Function<Object, T> parse){
//    if(val == null || val.toString().equals("")){
//      return defaultValue;
//    }
//
//    return parse.apply(val);
//  }

    private boolean parseBooleanValue(Object val, boolean defaultValue) {
        if (val == null || val.toString().equals("")) {
            return defaultValue;
        }

        return Boolean.parseBoolean(val.toString());
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        final Result fr = result;
        if (call.method.equals("signIn")) {
            GoogleSignInOptions.Builder builder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
            if (call.hasArgument("scopeSnapShot") && parseBooleanValue(call.argument("scopeSnapShot"), false)) {
                builder.requestScopes(Games.SCOPE_GAMES_SNAPSHOTS, Drive.SCOPE_APPFOLDER);
            }
            GoogleSignInOptions signInOptions = builder.build();
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
                // Already signed in.
                // The signed in account is stored in the 'account' variable.
                fr.success(new PluginResult().toMap());
            } else {
                // Haven't been signed-in before. Try the silent sign-in first.
                GoogleSignInClient signInClient = GoogleSignIn.getClient(context, signInOptions);
                signInClient
                        .silentSignIn()
                        .addOnCompleteListener(
                                task -> {
                                    if (task.isSuccessful()) {
                                        // The signed in account is stored in the task's result.
                                        fr.success(new PluginResult().toMap());
                                    } else {
                                        // silent sign-in failed, try to sign-in explicitly
                                        lastResult = fr;
                                        Intent intent = signInClient.getSignInIntent();
                                        activity.startActivityForResult(intent, RC_SIGNIN);
                                    }
                                });
            }
        } else if (call.method.equals("saveSnapShot")) {
            SnapshotsClient snapshotsClient = Games.getSnapshotsClient(context, GoogleSignIn.getLastSignedInAccount(context));
            snapshotsClient.open(call.argument("name"), true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
                    .addOnCompleteListener((task) -> {
                        if (task.isSuccessful()) {
                            Snapshot snapshot = task.getResult().getData();
                            snapshot.getSnapshotContents().writeBytes(call.argument("data"));
                            SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                                    .setDescription(call.argument("description"))
                                    .build();

                            snapshotsClient.commitAndClose(snapshot, metadataChange)
                                    .addOnCompleteListener((task1 -> {
                                        if (task1.isSuccessful()) {
                                            fr.success(new PluginResult().toMap());
                                        } else {
                                            fr.success(new PluginResult(task.getException()).toMap());
                                        }
                                    }));
                        } else {
                            // 保存失败
                            fr.success(new PluginResult(task.getException()).toMap());
                        }
                    });
        } else if (call.method.equals("loadSnapShot")) {
            SnapshotsClient snapshotsClient = Games.getSnapshotsClient(context, GoogleSignIn.getLastSignedInAccount(context));
            snapshotsClient.open(call.argument("name"), false, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
                    .addOnCompleteListener((task) -> {
                        if (task.isSuccessful()) {
                            Snapshot snapshot = task.getResult().getData();
                            try {
                                fr.success(new PluginResult().setData(snapshot.getSnapshotContents().readFully()).toMap());
                            } catch (IOException e) {
                                fr.success(new PluginResult(e).toMap());
                            }
                        } else {
                            // 保存失败
                            fr.success(new PluginResult(task.getException()).toMap());
                        }
                    });
        } else if (call.method.equals("increment")) {
            Games.getAchievementsClient(context, GoogleSignIn.getLastSignedInAccount(context))
                    .incrementImmediate(call.argument("id"), 1)
                    .addOnSuccessListener((task) -> {
                        fr.success(new PluginResult().toMap());
                    }).addOnFailureListener((e) -> {
                fr.success(new PluginResult(e).toMap());
            });
            ;
        } else if (call.method.equals("submitScore")) {
            Games.getLeaderboardsClient(context, GoogleSignIn.getLastSignedInAccount(context))
                    .submitScoreImmediate(call.argument("id"), call.argument("score"))
                    .addOnSuccessListener((task) -> {
                        fr.success(new PluginResult().toMap());
                    }).addOnFailureListener((e) -> {
                fr.success(new PluginResult(e).toMap());
            });
        } else if (call.method.equals("showAchievements")) {
            Games.getAchievementsClient(context, GoogleSignIn.getLastSignedInAccount(context))
                    .getAchievementsIntent().addOnSuccessListener((intent) -> {
                activity.startActivityForResult(intent, 0);
            });
        } else if (call.method.equals("showLeaderboards")) {
            Games.getLeaderboardsClient(context, GoogleSignIn.getLastSignedInAccount(context))
                    .getAllLeaderboardsIntent().addOnSuccessListener((intent) -> {
                activity.startActivityForResult(intent, 0);
            });
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
        this.activityPluginBinding = activityPluginBinding;
        this.activity = activityPluginBinding.getActivity();
        activityPluginBinding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {
        onAttachedToActivity(activityPluginBinding);
    }

    @Override
    public void onDetachedFromActivity() {
        if (this.activityPluginBinding != null) {
            activityPluginBinding.removeActivityResultListener(this);
            activityPluginBinding = null;
            activity = null;
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(lastResult != null){
            if(requestCode == RC_SIGNIN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
                if (result.isSuccess()) {
                    // The signed in account is stored in the result.
                    GoogleSignInAccount signedInAccount = result.getSignInAccount();
                    lastResult.success(new PluginResult().toMap());
                } else {
                    lastResult.success(new PluginResult(new Exception("Login failed")).toMap());
                }
                return true;
            }

            lastResult = null;
        }
        return false;
    }
}
