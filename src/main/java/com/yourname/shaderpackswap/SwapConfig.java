package com.yourname.shaderpackswap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwapConfig {
    // Key: Shader Name (or special keys), Value: List of Resource Pack IDs to enable
    public Map<String, List<String>> packsToEnable = new HashMap<>();

    // Key: Shader Name (or special keys), Value: List of Resource Pack IDs to disable
    public Map<String, List<String>> packsToDisable = new HashMap<>();

    public static final String SHADERS_OFF = "SHADERS_OFF";
    public static final String SHADERS_ON_GLOBAL = "SHADERS_ON_GLOBAL";

    public List<String> getPacksToEnable(String shaderName) {
        return packsToEnable.computeIfAbsent(shaderName, k -> new ArrayList<>());
    }

    public List<String> getPacksToDisable(String shaderName) {
        return packsToDisable.computeIfAbsent(shaderName, k -> new ArrayList<>());
    }
}
