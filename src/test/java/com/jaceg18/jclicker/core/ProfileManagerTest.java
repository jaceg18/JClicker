package com.jaceg18.jclicker.core;

import com.jaceg18.jclicker.util.ClickType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProfileManagerTest {

    @TempDir
    Path tempDir;

    private ProfileManager profileManager;

    @BeforeEach
    void setUp() throws IOException {
        profileManager = new ProfileManager(tempDir);
        profileManager.init();
    }

    @Test
    void testInitCreatesProfilesDirectory() {
        assertTrue(Files.exists(tempDir.resolve("profiles")));
        assertTrue(Files.isDirectory(tempDir.resolve("profiles")));
    }

    @Test
    void testSaveProfile() throws IOException {
        Profile profile = createTestProfile();
        String profileName = "TestProfile";

        profileManager.saveProfile(profileName, profile);

        Path profileFile = tempDir.resolve("profiles").resolve(profileName + ".properties");
        assertTrue(Files.exists(profileFile));
    }

    @Test
    void testSaveProfileWithNullName() throws IOException {
        Profile profile = createTestProfile();
        
        // Should not throw exception, just return early
        assertDoesNotThrow(() -> profileManager.saveProfile(null, profile));
    }

    @Test
    void testSaveProfileWithNullProfile() throws IOException {
        // Should not throw exception, just return early
        assertDoesNotThrow(() -> profileManager.saveProfile("TestProfile", null));
    }

    @Test
    void testLoadProfile() throws IOException {
        Profile originalProfile = createTestProfile();
        String profileName = "LoadTestProfile";

        profileManager.saveProfile(profileName, originalProfile);
        Profile loadedProfile = profileManager.loadProfile(profileName);

        assertNotNull(loadedProfile);
        assertEquals(profileName, loadedProfile.getName());
        assertEquals(originalProfile.getClickType(), loadedProfile.getClickType());
        assertEquals(originalProfile.isUseCurrentLocation(), loadedProfile.isUseCurrentLocation());
        assertEquals(originalProfile.isUseCustomLocation(), loadedProfile.isUseCustomLocation());
        assertEquals(originalProfile.isRepeatTilStopped(), loadedProfile.isRepeatTilStopped());
        assertEquals(originalProfile.getCustomX(), loadedProfile.getCustomX());
        assertEquals(originalProfile.getCustomY(), loadedProfile.getCustomY());
        assertEquals(originalProfile.getToggleBind(), loadedProfile.getToggleBind());
        assertEquals(originalProfile.getDelayMS(), loadedProfile.getDelayMS(), 0.001);
        assertEquals(originalProfile.getRepeatTimes(), loadedProfile.getRepeatTimes());
    }

    @Test
    void testLoadNonExistentProfile() {
        assertThrows(java.io.FileNotFoundException.class, () -> {
            profileManager.loadProfile("NonExistentProfile");
        });
    }

    @Test
    void testDeleteProfile() throws IOException {
        Profile profile = createTestProfile();
        String profileName = "DeleteTestProfile";

        profileManager.saveProfile(profileName, profile);
        Path profileFile = tempDir.resolve("profiles").resolve(profileName + ".properties");
        assertTrue(Files.exists(profileFile));

        profileManager.deleteProfile(profileName);
        assertFalse(Files.exists(profileFile));
    }

    @Test
    void testDeleteNonExistentProfile() {
        // Should throw RuntimeException with IOException cause
        assertThrows(RuntimeException.class, () -> {
            profileManager.deleteProfile("NonExistentProfile");
        });
    }

    @Test
    void testContainsProfile() throws IOException {
        Profile profile = createTestProfile();
        String profileName = "ContainsTestProfile";

        assertFalse(profileManager.containsProfile(profileName));

        profileManager.saveProfile(profileName, profile);
        assertTrue(profileManager.containsProfile(profileName));
    }

    @Test
    void testContainsProfileCaseInsensitive() throws IOException {
        Profile profile = createTestProfile();
        String profileName = "CaseTestProfile";

        profileManager.saveProfile(profileName, profile);
        assertTrue(profileManager.containsProfile(profileName.toLowerCase()));
        assertTrue(profileManager.containsProfile(profileName.toUpperCase()));
    }

    @Test
    void testListProfiles() throws IOException {
        // Initially should be empty or return empty list
        List<String> profiles = profileManager.listProfiles();
        assertNotNull(profiles);

        // Save multiple profiles
        profileManager.saveProfile("Profile1", createTestProfile());
        profileManager.saveProfile("Profile2", createTestProfile());
        profileManager.saveProfile("Profile3", createTestProfile());

        profiles = profileManager.listProfiles();
        assertEquals(3, profiles.size());
        assertTrue(profiles.contains("Profile1"));
        assertTrue(profiles.contains("Profile2"));
        assertTrue(profiles.contains("Profile3"));
    }

    @Test
    void testListProfilesSorted() throws IOException {
        profileManager.saveProfile("Zebra", createTestProfile());
        profileManager.saveProfile("Alpha", createTestProfile());
        profileManager.saveProfile("Beta", createTestProfile());

        List<String> profiles = profileManager.listProfiles();
        assertEquals(3, profiles.size());
        assertEquals("Alpha", profiles.get(0));
        assertEquals("Beta", profiles.get(1));
        assertEquals("Zebra", profiles.get(2));
    }

    @Test
    void testListProfilesWhenDirectoryDoesNotExist() throws IOException {
        // Create a new ProfileManager without calling init
        ProfileManager newManager = new ProfileManager(tempDir.resolve("nonexistent"));
        
        List<String> profiles = newManager.listProfiles();
        assertNotNull(profiles);
        assertTrue(profiles.isEmpty());
    }

    @Test
    void testSaveAndLoadAllClickTypes() throws IOException {
        for (ClickType clickType : ClickType.values()) {
            Profile profile = createTestProfile();
            profile.setClickType(clickType);
            String profileName = "ClickType_" + clickType.name();

            profileManager.saveProfile(profileName, profile);
            Profile loaded = profileManager.loadProfile(profileName);

            assertEquals(clickType, loaded.getClickType());
        }
    }

    @Test
    void testSaveAndLoadWithAllBooleanCombinations() throws IOException {
        boolean[] values = {true, false};
        
        for (boolean useCurrent : values) {
            for (boolean useCustom : values) {
                for (boolean repeatTilStopped : values) {
                    Profile profile = createTestProfile();
                    profile.setUseCurrentLocation(useCurrent);
                    profile.setUseCustomLocation(useCustom);
                    profile.setRepeatTilStopped(repeatTilStopped);
                    String profileName = String.format("Bool_%s_%s_%s", useCurrent, useCustom, repeatTilStopped);

                    profileManager.saveProfile(profileName, profile);
                    Profile loaded = profileManager.loadProfile(profileName);

                    assertEquals(useCurrent, loaded.isUseCurrentLocation());
                    assertEquals(useCustom, loaded.isUseCustomLocation());
                    assertEquals(repeatTilStopped, loaded.isRepeatTilStopped());
                }
            }
        }
    }

    @Test
    void testSaveAndLoadWithEdgeCaseValues() throws IOException {
        Profile profile = new Profile();
        profile.setClickType(ClickType.MIDDLE);
        profile.setCustomX(Integer.MAX_VALUE);
        profile.setCustomY(Integer.MIN_VALUE);
        profile.setToggleBind(999);
        profile.setDelayMS(Double.MAX_VALUE);
        profile.setRepeatTimes(Integer.MAX_VALUE);
        profile.setUseCurrentLocation(true);
        profile.setUseCustomLocation(true);
        profile.setRepeatTilStopped(true);

        String profileName = "EdgeCaseProfile";
        profileManager.saveProfile(profileName, profile);
        Profile loaded = profileManager.loadProfile(profileName);

        assertEquals(Integer.MAX_VALUE, loaded.getCustomX());
        assertEquals(Integer.MIN_VALUE, loaded.getCustomY());
        assertEquals(999, loaded.getToggleBind());
        assertEquals(Double.MAX_VALUE, loaded.getDelayMS());
        assertEquals(Integer.MAX_VALUE, loaded.getRepeatTimes());
    }

    @Test
    void testProfileNameIsSetOnSave() throws IOException {
        Profile profile = createTestProfile();
        String profileName = "NameTestProfile";
        
        // Profile name might be different before save
        profile.setName("DifferentName");
        
        profileManager.saveProfile(profileName, profile);
        Profile loaded = profileManager.loadProfile(profileName);
        
        // After save, the name should be set to the provided name
        assertEquals(profileName, loaded.getName());
    }

    // Helper method to create a test profile with default values
    private Profile createTestProfile() {
        Profile profile = new Profile();
        profile.setClickType(ClickType.LEFT);
        profile.setUseCurrentLocation(true);
        profile.setUseCustomLocation(false);
        profile.setRepeatTilStopped(false);
        profile.setCustomX(100);
        profile.setCustomY(200);
        profile.setToggleBind(32);
        profile.setDelayMS(100.5);
        profile.setRepeatTimes(10);
        return profile;
    }
}

