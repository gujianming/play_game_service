# play_game_service

A Google Play Games Services plugin for flutter

## Getting Started

1. add dependencies

    ```yml
    dependencies:
      play_game_service: ^0.0.1
    ```

1. import requirements

    ```dart
    import 'package:play_game_service/play_game_service.dart';
    ```

1. SignIn

    ```dart
    // set scopeSnapShot=false if you don't want play with snapshots;
    var ret = await PlayGameService.signIn(scopeSnapShot: true);
    ```

1. Save Game Data

    ```dart
    var ret = await PlayGameService.saveSnapShot("HashiTogether", new Uint8List(10), "description");
    ```

1. Load Game Data

    ```dart
    var map = await PlayGameService.loadSnapShot("HashiTogether");
    if(map["result"] == 0){
        Uint8List data = map["data"];
        // TODO Now you have data, do what you want.
        return true;
    } else {
        return false;
    }
    ```
