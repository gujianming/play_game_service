import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:play_game_service/play_game_service.dart';

void main() {
  const MethodChannel channel = MethodChannel('play_game_service');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('signIn', () async {
    try{
      var ret = await PlayGameService.signIn();
      print(ret);
    } catch (e){
      print(e);
    }
  });
}
