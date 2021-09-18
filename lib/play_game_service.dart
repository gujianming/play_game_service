import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class SignInResult{
  int? result;
  String? name;
  String? type;

  SignInResult({Map<dynamic, dynamic>? map}) {
    if(map == null){
      this.result = -1;
    } else {
      this.result = map["result"];
      if(this.result == 0){
        name = map["name"];
        type = map["type"];
      }
    }
  }
}
class PlayGameService {
  static const MethodChannel _channel =
      const MethodChannel('play_game_service');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<SignInResult> signIn({bool scopeSnapShot = false}) async {
    try{
      Map<dynamic, dynamic> result = await _channel.invokeMethod('signIn', {"scopeSnapShot": scopeSnapShot});
      return SignInResult(map: result);
    } on PlatformException {
      return SignInResult();
    }
  }

  static Future<Map<dynamic, dynamic>> loadSnapShot(String name) async {
    return await _channel.invokeMethod('loadSnapShot', {"name": name});
  }

  static Future<bool> saveSnapShot(String name, Uint8List data, String description) async {
    try{
      Map<dynamic, dynamic> result = await _channel.invokeMethod('saveSnapShot', {"name": name, "data": data, "description": description});
      if(result["result"] != 0){
        print(result["exception"]);
        return false;
      }
      return true;
    } on PlatformException {
      return false;
    }
  }
}
