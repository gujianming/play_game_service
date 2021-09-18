package com.iyond.minigame.play_game_service;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** PlayGameServicePlugin */
public class PlayGameServicePlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Context context;
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

  private boolean parseBooleanValue(Object val, boolean defaultValue){
    if(val == null || val.toString().equals("")){
      return defaultValue;
    }

    return Boolean.parseBoolean(val.toString());
  }
  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    final Result fr = result;
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if(call.method.equals("signIn")){
      GoogleSignInOptions.Builder builder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
      if(call.hasArgument("scopeSnapShot") && parseBooleanValue(call.argument("scopeSnapShot"), false)){
        builder.requestScopes(Games.SCOPE_GAMES_SNAPSHOTS, Drive.SCOPE_APPFOLDER);
      }
      GoogleSignInOptions  signInOptions = builder.build();
      GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
      if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
        // Already signed in.
        // The signed in account is stored in the 'account' variable.
        fr.success(new SignInResult(0, account.getAccount()).toMap());
      } else {
        // Haven't been signed-in before. Try the silent sign-in first.
        GoogleSignInClient signInClient = GoogleSignIn.getClient(context, signInOptions);
        signInClient
            .silentSignIn()
            .addOnCompleteListener(
                    task -> {
                      if (task.isSuccessful()) {
                        // The signed in account is stored in the task's result.
                        fr.success(new SignInResult(0, task.getResult().getAccount()).toMap());
                      } else {
                        fr.success(new SignInResult(-1).toMap());
                      }
                    });
      }
    } else if(call.method.equals("saveSnapShot")) {
      final Map<String, Object> retMap = new HashMap<>();
      SnapshotsClient snapshotsClient = Games.getSnapshotsClient(context, GoogleSignIn.getLastSignedInAccount(context));
      snapshotsClient.open(call.argument("name"), true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
              .addOnCompleteListener((task) -> {
                if(task.isSuccessful()){
                  Snapshot snapshot = task.getResult().getData();
                  snapshot.getSnapshotContents().writeBytes(call.argument("data"));
                  SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                          .setDescription(call.argument("description"))
                          .build();

                  snapshotsClient.commitAndClose(snapshot, metadataChange)
                          .addOnCompleteListener((task1 -> {
                            if(task1.isSuccessful()){
                              retMap.put("result", 0);
                              fr.success(retMap);
                            } else {
                              retMap.put("result", -1);
                              retMap.put("exception", task.getException());
                              fr.success(retMap);
                            }
                          }));
                } else {
                  // 保存失败
                  retMap.put("result", -1);
                  retMap.put("exception", task.getException());
                  fr.success(retMap);
                }
              });
    } else if(call.method.equals("loadSnapShot")) {
      final Map<String, Object> retMap = new HashMap<>();
      SnapshotsClient snapshotsClient = Games.getSnapshotsClient(context, GoogleSignIn.getLastSignedInAccount(context));
      snapshotsClient.open(call.argument("name"), false, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
              .addOnCompleteListener((task) -> {
                if(task.isSuccessful()){
                  Snapshot snapshot = task.getResult().getData();
                  try {
                    retMap.put("data", snapshot.getSnapshotContents().readFully());
                    retMap.put("result", 0);
                    fr.success(retMap);
                  } catch (IOException e) {
                    retMap.put("exception", e);
                    retMap.put("result", -1);
                    fr.success(retMap);
                  }
                } else {
                  // 保存失败
                  retMap.put("result", -1);
                  retMap.put("exception", task.getException());
                  fr.success(retMap);
                }
              });
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}
