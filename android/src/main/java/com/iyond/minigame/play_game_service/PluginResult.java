package com.iyond.minigame.play_game_service;

import java.util.HashMap;
import java.util.Map;

public class PluginResult {
    private boolean success;
    private String exception;
    private Object data;

    public PluginResult setData(Object data){
        this.data = data;
        return this;
    }
    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("success", success);
        map.put("data", data);
        map.put("exception", exception);

        return map;
    }

    public PluginResult(){
        this.success = true;
    }
    public PluginResult(String exception){
        this.success = false;
        this.exception = exception;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public Object getData() {
        return data;
    }
}
