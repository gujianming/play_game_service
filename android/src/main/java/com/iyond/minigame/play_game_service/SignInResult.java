package com.iyond.minigame.play_game_service;

import android.accounts.Account;

import java.util.HashMap;
import java.util.Map;

public class SignInResult {
    private int result;
    private String name;
    private String type;

    public SignInResult() {
    }

    public SignInResult(int result, Account account) {
        this.result = result;
        this.name = account.name;
        this.type = account.type;
    }
    public SignInResult(int result) {
        this.result = result;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("result", this.result);
        map.put("name", name);
        map.put("type", type);
        return map;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
