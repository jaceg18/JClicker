package com.jaceg18.jclicker.core;

import com.jaceg18.jclicker.util.ClickType;

public class Profile {

    /**
     * A profile manager is going to be made, should we load all on startup? So we can switch with ease?
     * Profile manager will access resource files in which the profiles are saved.
     */

    private String name; // This will be the name when the user saves that profile, and answers the prompted name request.
    private ClickType clickType;
    private boolean useCurrentLocation, useCustomLocation, repeatTilStopped;
    private int customX = 0, customY = 0;
    private int toggleBind;
    private double delayMS;
    private int repeatTimes;

    public Profile(){}

    public Profile(String name){this.name = name;}

    public void setClickType(ClickType clickType) {this.clickType = clickType;}
    public void setUseCurrentLocation(boolean useCurrentLocation) {this.useCurrentLocation = useCurrentLocation;}
    public void setUseCustomLocation(boolean useCustomLocation) {this.useCustomLocation = useCustomLocation;}
    public void setRepeatTilStopped(boolean repeatTilStopped) {this.repeatTilStopped = repeatTilStopped;}
    public void setCustomX(int customX) {this.customX = customX;}
    public void setCustomY(int customY) {this.customY = customY;}
    public void setToggleBind(int toggleBind) {this.toggleBind = toggleBind;}
    public void setDelayMS(double delayMS) {this.delayMS = delayMS;}
    public void setRepeatTimes(int repeatTimes) {this.repeatTimes = repeatTimes;}
    public void setName(String name){this.name = name;}
    public String getName(){return name;}
    public ClickType getClickType() {return clickType;}
    public boolean isUseCurrentLocation() {return useCurrentLocation;}
    public boolean isUseCustomLocation() {return useCustomLocation;}
    public boolean isRepeatTilStopped() {return repeatTilStopped;}
    public int getCustomX() {return customX;}
    public int getCustomY() {return customY;}
    public int getToggleBind() {return toggleBind;}
    public double getDelayMS() {return delayMS;}
    public int getRepeatTimes() {return repeatTimes;}





}
