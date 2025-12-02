package com.jaceg18.jclicker.core;

import com.jaceg18.jclicker.util.ClickType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ProfileManager {

    private final Path profilesDir;

    public ProfileManager(Path baseDir){
        this.profilesDir = baseDir.resolve("profiles");
    }

    public void init() throws IOException {
        Files.createDirectories(profilesDir);
    }

    private Path getProfileFile(String name){
        return profilesDir.resolve(name + ".properties");
    }

    public void deleteProfile(String name){
        try {
            Files.delete(getProfileFile(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean containsProfile(String name){
        try {
            return listProfiles().stream().anyMatch(p -> p.equalsIgnoreCase(name));
        } catch (IOException ignored) {return false;}
    }


    public void saveProfile(String name, Profile profile) throws IOException {
        if (name == null || profile == null) return;
        Properties props = new Properties();

        profile.setName(name);

        props.setProperty("clickType", profile.getClickType().name());
        props.setProperty("useCurrentLocation", Boolean.toString(profile.isUseCurrentLocation()));
        props.setProperty("useCustomLocation", Boolean.toString(profile.isUseCustomLocation()));
        props.setProperty("repeatTilStopped", Boolean.toString(profile.isRepeatTilStopped()));
        props.setProperty("customX", Integer.toString(profile.getCustomX()));
        props.setProperty("customY", Integer.toString(profile.getCustomY()));
        props.setProperty("toggleBind", Integer.toString(profile.getToggleBind()));
        props.setProperty("delayMS", Double.toString(profile.getDelayMS()));
        props.setProperty("repeatTimes", Integer.toString(profile.getRepeatTimes()));

        try (OutputStream out = Files.newOutputStream(getProfileFile(name))) {
            props.store(out, "Profile: " + name);
        }
    }

    public Profile loadProfile(String name) throws IOException {
        Path file = getProfileFile(name);
        if (!Files.exists(file)) {
            throw new FileNotFoundException("Profile not found: " + name);
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        }

        Profile p = new Profile();
        p.setName(name);
        p.setClickType(ClickType.valueOf(props.getProperty("clickType")));
        p.setUseCurrentLocation(Boolean.parseBoolean(props.getProperty("useCurrentLocation")));
        p.setUseCustomLocation(Boolean.parseBoolean(props.getProperty("useCustomLocation")));
        p.setRepeatTilStopped(Boolean.parseBoolean(props.getProperty("repeatTilStopped")));
        p.setCustomX(Integer.parseInt(props.getProperty("customX")));
        p.setCustomY(Integer.parseInt(props.getProperty("customY")));
        p.setToggleBind(Integer.parseInt(props.getProperty("toggleBind")));
        p.setDelayMS(Double.parseDouble(props.getProperty("delayMS")));
        p.setRepeatTimes(Integer.parseInt(props.getProperty("repeatTimes")));

        return p;
    }

    public List<String> listProfiles() throws IOException {
        if (!Files.exists(profilesDir)) {
            return List.of();
        }

        try (var stream = Files.list(profilesDir)) {
            return stream
                    .filter(p -> p.toString().endsWith(".properties"))
                    .map(p -> {
                        String fileName = p.getFileName().toString();
                        return fileName.substring(0, fileName.length() - ".properties".length());
                    })
                    .sorted()
                    .collect(Collectors.toList());
        }
    }


}
