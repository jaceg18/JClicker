package com.jaceg18.jclicker.core;

import com.jaceg18.jclicker.util.ClickType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = new Profile();
    }

    @Test
    void testDefaultConstructor() {
        Profile p = new Profile();
        assertNotNull(p);
        assertNull(p.getName());
        assertNull(p.getClickType());
        assertFalse(p.isUseCurrentLocation());
        assertFalse(p.isUseCustomLocation());
        assertFalse(p.isRepeatTilStopped());
        assertEquals(0, p.getCustomX());
        assertEquals(0, p.getCustomY());
        assertEquals(0, p.getToggleBind());
        assertEquals(0.0, p.getDelayMS());
        assertEquals(0, p.getRepeatTimes());
    }

    @Test
    void testConstructorWithName() {
        String name = "TestProfile";
        Profile p = new Profile(name);
        assertEquals(name, p.getName());
    }

    @Test
    void testNameGetterAndSetter() {
        String name = "MyProfile";
        profile.setName(name);
        assertEquals(name, profile.getName());
    }

    @Test
    void testClickTypeGetterAndSetter() {
        profile.setClickType(ClickType.LEFT);
        assertEquals(ClickType.LEFT, profile.getClickType());

        profile.setClickType(ClickType.RIGHT);
        assertEquals(ClickType.RIGHT, profile.getClickType());

        profile.setClickType(ClickType.MIDDLE);
        assertEquals(ClickType.MIDDLE, profile.getClickType());
    }

    @Test
    void testUseCurrentLocationGetterAndSetter() {
        profile.setUseCurrentLocation(true);
        assertTrue(profile.isUseCurrentLocation());

        profile.setUseCurrentLocation(false);
        assertFalse(profile.isUseCurrentLocation());
    }

    @Test
    void testUseCustomLocationGetterAndSetter() {
        profile.setUseCustomLocation(true);
        assertTrue(profile.isUseCustomLocation());

        profile.setUseCustomLocation(false);
        assertFalse(profile.isUseCustomLocation());
    }

    @Test
    void testRepeatTilStoppedGetterAndSetter() {
        profile.setRepeatTilStopped(true);
        assertTrue(profile.isRepeatTilStopped());

        profile.setRepeatTilStopped(false);
        assertFalse(profile.isRepeatTilStopped());
    }

    @Test
    void testCustomXGetterAndSetter() {
        int x = 100;
        profile.setCustomX(x);
        assertEquals(x, profile.getCustomX());

        profile.setCustomX(0);
        assertEquals(0, profile.getCustomX());

        profile.setCustomX(-50);
        assertEquals(-50, profile.getCustomX());
    }

    @Test
    void testCustomYGetterAndSetter() {
        int y = 200;
        profile.setCustomY(y);
        assertEquals(y, profile.getCustomY());

        profile.setCustomY(0);
        assertEquals(0, profile.getCustomY());

        profile.setCustomY(-75);
        assertEquals(-75, profile.getCustomY());
    }

    @Test
    void testToggleBindGetterAndSetter() {
        int bind = 65; // 'A' key
        profile.setToggleBind(bind);
        assertEquals(bind, profile.getToggleBind());

        profile.setToggleBind(0);
        assertEquals(0, profile.getToggleBind());
    }

    @Test
    void testDelayMSGetterAndSetter() {
        double delay = 100.5;
        profile.setDelayMS(delay);
        assertEquals(delay, profile.getDelayMS(), 0.001);

        profile.setDelayMS(0.0);
        assertEquals(0.0, profile.getDelayMS());

        profile.setDelayMS(1000.0);
        assertEquals(1000.0, profile.getDelayMS());
    }

    @Test
    void testRepeatTimesGetterAndSetter() {
        int times = 10;
        profile.setRepeatTimes(times);
        assertEquals(times, profile.getRepeatTimes());

        profile.setRepeatTimes(0);
        assertEquals(0, profile.getRepeatTimes());

        profile.setRepeatTimes(100);
        assertEquals(100, profile.getRepeatTimes());
    }

    @Test
    void testCompleteProfileConfiguration() {
        profile.setName("CompleteProfile");
        profile.setClickType(ClickType.LEFT);
        profile.setUseCurrentLocation(true);
        profile.setUseCustomLocation(false);
        profile.setRepeatTilStopped(true);
        profile.setCustomX(500);
        profile.setCustomY(300);
        profile.setToggleBind(32); // Space key
        profile.setDelayMS(250.75);
        profile.setRepeatTimes(50);

        assertEquals("CompleteProfile", profile.getName());
        assertEquals(ClickType.LEFT, profile.getClickType());
        assertTrue(profile.isUseCurrentLocation());
        assertFalse(profile.isUseCustomLocation());
        assertTrue(profile.isRepeatTilStopped());
        assertEquals(500, profile.getCustomX());
        assertEquals(300, profile.getCustomY());
        assertEquals(32, profile.getToggleBind());
        assertEquals(250.75, profile.getDelayMS(), 0.001);
        assertEquals(50, profile.getRepeatTimes());
    }
}

