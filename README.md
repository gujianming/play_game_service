# play_game_service

A Google Play Games Services plugin for flutter

## Getting Started

1. add dependencies

    ```yml
    dependencies:
      play_game_service: ^0.2.2
    ```

1. import requirements

    ```dart
    import 'package:play_game_service/play_game_service.dart';
    ```

1. SignIn

    ```dart
    // set scopeSnapShot=false if you don't want play with snapshots;
    var ret = await PlayGameService.signIn(scopeSnapShot: true);
    if(ret.success){
      if(ret.email != null){
        // you can get user's email from ret.email
      }
    }
    ```

1. Save Game Data

    ```dart
    var ret = await PlayGameService.saveSnapShot("HashiTogether", new Uint8List(10), "description");
    if(ret.success){
      // save successful
    }
    ```

1. Load Game Data

    ```dart
    var ret = await PlayGameService.signIn(scopeSnapShot: true);
    if (ret.success) {
      var loadResult = await PlayGameService.loadSnapShot("HashiTogether");
      if (loadResult.success) {
        Uint8List data = loadResult.data!;
        // TODO Now you have data, do what you want.
      }
    }
    ```

1. Show Leaderboards

    ```dart
    PlayGameService.showLeaderboards();
    ```

1. Submit score

    ```dart
    // leaderBoardId is the leaderboard's id which you added in google play console
    PlayGameService.submitScore(leaderBoardId, count);
    ```

1. Show Achievement

    ```dart
    PlayGameService.showAchievements();
    ```

1. Achievement increment

    ```dart
    // ACHIEVEMENT_ID is the achievement's id which you added in google play console
    PlayGameService.increment(ACHIEVEMENT_ID);
    ```
