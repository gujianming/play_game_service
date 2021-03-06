import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class PluginResult {
  bool success = false;
  String? message;

  PluginResult(Map<dynamic, dynamic> map) {
    this.success = map["success"];
    this.message = map["exception"];
  }
}
class SignInResult extends PluginResult {
  String? email;

  SignInResult(Map<dynamic, dynamic> map) : super(map) {
    if(this.success){
      this.email = map["data"];
    }
  }
}

/// the result of LoadSnapshot
class LoadSnapshotResult extends PluginResult {
  Uint8List? data;

  LoadSnapshotResult(Map<dynamic, dynamic> map) : super(map) {
    if (this.success) {
      this.data = map["data"];
    }
  }
}

/// Main Class, provider useful method
class PlayGameService {
  /// the channel communicate with platform
  static const MethodChannel _channel =
  const MethodChannel('play_game_service');

  /// SignIn with google account, before you do anything, you must sign in
  /// @param scopeSnapShot set to ture if you want play with snapshots
  static Future<SignInResult> signIn({bool scopeSnapShot = false}) async {
    try {
      Map<dynamic, dynamic> result = await _channel
          .invokeMethod('signIn', {"scopeSnapShot": scopeSnapShot});
      return SignInResult(result);
    } catch (e) {
      return SignInResult({"success": false, "exception": e.toString()});
    }
  }

  /// Get the snapshot data with the given name
  /// @param name the snapshot's name you want get
  static Future<LoadSnapshotResult> loadSnapShot(String name) async {
    try {
      Map<dynamic, dynamic> map =
      await _channel.invokeMethod('loadSnapShot', {"name": name});
      return LoadSnapshotResult(map);
    } catch (e) {
      return LoadSnapshotResult({"success": false, "exception": e.toString()});
    }
  }

  /// Save data to the snapshot with the given name
  /// @param name the snapshot's name
  /// @param data a byte array you want to save
  /// @param description the description of the snapshot, will add to it's metedata
  static Future<PluginResult> saveSnapShot(String name, Uint8List data,
      String description) async {
    try {
      Map<dynamic, dynamic> result = await _channel.invokeMethod('saveSnapShot',
          {"name": name, "data": data, "description": description});
      return PluginResult(result);
    } catch (e) {
      return PluginResult({"success": false, "exception": e.toString()});
    }
  }

  static Future<PluginResult> submitScore(String leaderBoardId,
      int score) async {
    try {
      Map<dynamic, dynamic> result = await _channel
          .invokeMethod('submitScore', {"id": leaderBoardId, "score": score});
      return PluginResult(result);
    } catch (e) {
      return PluginResult({"success": false, "exception": e.toString()});
    }
  }

  static Future<PluginResult> increment(String achivementId) async {
    try {
      Map<dynamic, dynamic> result =
      await _channel.invokeMethod('increment', {"id": achivementId});
      return PluginResult(result);
    } catch (e) {
      return PluginResult({"success": false, "exception": e.toString()});
    }
  }

  static void showLeaderboards() async {
    try {
      await _channel.invokeMethod('showLeaderboards');
    } catch (e) {
    }
  }

  static void showAchievements() async {
    try {
      await _channel.invokeMethod('showAchievements');
    } catch (e) {
    }
  }
}
