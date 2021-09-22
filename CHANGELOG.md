## 0.0.1

* Support signIn, upload snapshot, download snapshot

## 0.1.1

* score=70, to little. Add comments, change return type, remove auto generated code

## 0.2.1

* support sign-in explicitly; support leaderboards and achievements

## 0.2.2

* Won't try to get anything about user; unify the return type

## 0.2.3

* fix unsupported type exception

## 0.2.4

* if you already signed in with no Games.SCOPE_GAMES_SNAPSHOTS and Drive.SCOPE_APPFOLDER, then you request thease scopes, GoogleSignIn.hasPermissions(last-signed-account) return true. it lead to backup failed

## 0.2.5

* request email while user sign in
