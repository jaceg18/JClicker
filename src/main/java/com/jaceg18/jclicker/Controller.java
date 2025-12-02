package com.jaceg18.jclicker;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.jaceg18.jclicker.core.Profile;
import com.jaceg18.jclicker.core.ProfileManager;
import com.jaceg18.jclicker.util.ClickType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;

public class Controller implements NativeKeyListener {


    private final ExecutorService clickExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean clicking = false;

    @FXML
    public ComboBox<ClickType> mouseButtonCombo;
    @FXML
    public ComboBox<String> profileCombo;
    @FXML
    public CheckBox useCurrentBox, untilStopBox, useCustomBox;
    @FXML
    public TextField repeatField, delayField, yField, xField;
    @FXML
    public Label profileLabel, statusLabel;
    @FXML
    public Button toggleButton, loadProfileButton, deleteProfileButton;
    @FXML
    public MenuBar menuBar;
    @FXML
    public MenuItem saveProfile, openBindOptions, openAbout, openHelpProfile;

    private int toggleKey = NativeKeyEvent.VC_F6;
    private boolean toggled = false;

    private ProfileManager profileManager;

    public void initialize() {
        LogManager.getLogManager().reset();
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            showErrorDialog("Initialization Error",
                    "Failed to register native keyboard hook.\n" +
                            "The application may not function correctly.\n\n" +
                            "Error: " + e.getMessage());
        }
        GlobalScreen.addNativeKeyListener(this);

        profileManager = new ProfileManager(getBaseDir());
        try {
            profileManager.init();
            refreshProfileList();
        } catch (IOException e) {
            showErrorDialog("Profile Error",
                    "Failed to initialize profile system.\n" +
                            "Profiles may not be available.\n\n" +
                            "Error: " + e.getMessage());
        }

        mouseButtonCombo.getItems().addAll(
                ClickType.LEFT,
                ClickType.RIGHT,
                ClickType.MIDDLE
        );

        saveProfile.setOnAction(e -> saveProfile());
        openBindOptions.setOnAction(e -> handleBinds());
        openAbout.setOnAction(e -> handleAbout());
        openHelpProfile.setOnAction(e -> handleHelp());
        updateToggleButtonText();

        initUiBindings();

        statusLabel.setText("Status: Off");
    }

    private void initUiBindings() {
        useCurrentBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                useCustomBox.setSelected(false);
            }
            updateLocationFieldsEnabled();
        });

        useCustomBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                useCurrentBox.setSelected(false);
            }
            updateLocationFieldsEnabled();
        });

        untilStopBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateRepeatFieldEnabled();
        });

        updateLocationFieldsEnabled();
        updateRepeatFieldEnabled();
    }

    private void updateLocationFieldsEnabled() {
        boolean customLocation = useCustomBox.isSelected();
        xField.setDisable(!customLocation);
        yField.setDisable(!customLocation);
    }

    private void updateRepeatFieldEnabled() {
        boolean untilStopped = untilStopBox.isSelected();
        repeatField.setDisable(untilStopped);
    }

    private void updateToggleButtonText() {
        String keyText = NativeKeyEvent.getKeyText(toggleKey);
        toggleButton.setText((toggled ? "Toggle Off " : "Toggle On ") + keyText);
    }

    private void refreshProfileList() throws IOException {
        profileCombo.getItems().setAll(profileManager.listProfiles());
    }

    private ClickConfig buildClickConfigFromUi() {
        ClickConfig c = new ClickConfig();
        c.clickType = mouseButtonCombo.getValue();
        c.repeatUntilStopped = untilStopBox.isSelected();
        c.repeatTimes = parseIntOrDefault(repeatField.getText(), 0);
        c.delayMs = parseDoubleOrDefault(delayField.getText(), 100.0);
        c.useCurrentLocation = useCurrentBox.isSelected();
        c.useCustomLocation = useCustomBox.isSelected();
        c.customX = parseIntOrDefault(xField.getText(), 0);
        c.customY = parseIntOrDefault(yField.getText(), 0);
        return c;
    }

    private int getButtonMask(ClickType clickType) {
        if (clickType == null) return InputEvent.BUTTON1_DOWN_MASK;

        return switch (clickType) {
            case LEFT -> InputEvent.BUTTON1_DOWN_MASK;
            case RIGHT -> InputEvent.BUTTON2_DOWN_MASK;
            case MIDDLE -> InputEvent.BUTTON3_DOWN_MASK;
        };
    }


    @FXML
    private void loadProfile() {
        String sel = profileCombo.getValue();
        if (sel == null || sel.isEmpty()) return;

        try {
            Profile profile = profileManager.loadProfile(sel);

            mouseButtonCombo.setValue(profile.getClickType());
            useCurrentBox.setSelected(profile.isUseCurrentLocation());
            useCustomBox.setSelected(profile.isUseCustomLocation());
            untilStopBox.setSelected(profile.isRepeatTilStopped());

            xField.setText(String.valueOf(profile.getCustomX()));
            yField.setText(String.valueOf(profile.getCustomY()));
            repeatField.setText(String.valueOf(profile.getRepeatTimes()));
            delayField.setText(String.valueOf(profile.getDelayMS()));

            toggleKey = profile.getToggleBind();
            updateToggleButtonText();
            updateLocationFieldsEnabled();
            updateRepeatFieldEnabled();

            profileLabel.setText("Profile Loaded: " + profile.getName());
            statusLabel.setText("Status: Profile loaded");

        } catch (IOException e) {
            showErrorDialog("Load Profile Error",
                    "Failed to load profile: " + sel + "\n\n" +
                            "Error: " + e.getMessage());
        }
    }

    private void saveProfile() {
        String name;
        do {
            name = JOptionPane.showInputDialog(
                    null,
                    "Please enter a unique profile name:",
                    "Save Profile",
                    JOptionPane.PLAIN_MESSAGE
            );

            // User hit cancel or closed dialog
            if (name == null) {
                return;
            }

            name = name.trim();

            // Validate profile name
            if (name.isEmpty()) {
                showErrorDialog("Invalid Name", "Profile name cannot be empty.");
                continue;
            }

            if (!isValidProfileName(name)) {
                showErrorDialog("Invalid Name",
                        "Profile name contains invalid characters.\n" +
                                "Please use only letters, numbers, spaces, hyphens, and underscores.");
                continue;
            }

            if (profileManager.containsProfile(name)) {
                showErrorDialog("Profile Exists",
                        "A profile with that name already exists.\n" +
                                "Please choose a different name.");
                continue;
            }

            break;
        } while (true);

        Profile p = new Profile(name);
        p.setClickType(mouseButtonCombo.getValue());

        int x = parseIntOrDefault(xField.getText(), 0);
        int y = parseIntOrDefault(yField.getText(), 0);
        p.setCustomX(x);
        p.setCustomY(y);

        double delayMS = parseDoubleOrDefault(delayField.getText(), 100.0);
        p.setDelayMS(delayMS);

        int repeatTimes = parseIntOrDefault(repeatField.getText(), 0);
        p.setRepeatTimes(repeatTimes);

        p.setRepeatTilStopped(untilStopBox.isSelected());
        p.setUseCurrentLocation(useCurrentBox.isSelected());
        p.setUseCustomLocation(useCustomBox.isSelected());
        p.setToggleBind(toggleKey);

        try {
            profileManager.saveProfile(name, p);
            refreshProfileList();
            statusLabel.setText("Status: Profile saved");
        } catch (IOException e) {
            showErrorDialog("Save Profile Error",
                    "Failed to save profile: " + name + "\n\n" +
                            "Error: " + e.getMessage());
        }
    }

    private int parseIntOrDefault(String text, int def) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception ignored) {
            return def;
        }
    }

    private double parseDoubleOrDefault(String text, double def) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception ignored) {
            return def;
        }
    }

    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText(App.appName + " by " + App.author);
        alert.setContentText(
                "Version " + App.version + "\n\n" +
                        "Hotkey controlled auto-clicker with profiles.\n" +
                        "Built with JavaFX and JNativeHook."
        );
        alert.showAndWait();
    }


    private void handleHelp() {
        String sb = """
                Click Type:
                  - Choose LEFT, RIGHT, or MIDDLE click.
                
                Repeat:
                  - 'Repeat until stopped': keeps clicking until you toggle off.
                  - Repeat field: number of clicks if 'until stopped' is not checked.
                
                Location:
                  - Use current location: clicks wherever the mouse currently is.
                  - Use custom location: clicks at X/Y coordinates you specify.
                
                Profiles:
                  - Save different combinations of click type, repeat, location, and delay.
                  - Toggle key is also stored per profile.
                """;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Profile Help");
        alert.setHeaderText("How JClicker works");
        alert.setContentText(sb);
        alert.getDialogPane().setMinWidth(500);
        alert.showAndWait();
    }

    private static final int[] SUPPORTED_TOGGLE_KEYS = {
            NativeKeyEvent.VC_F1,
            NativeKeyEvent.VC_F2,
            NativeKeyEvent.VC_F3,
            NativeKeyEvent.VC_F4,
            NativeKeyEvent.VC_F5,
            NativeKeyEvent.VC_F6,
            NativeKeyEvent.VC_F7,
            NativeKeyEvent.VC_F8,
            NativeKeyEvent.VC_F9,
            NativeKeyEvent.VC_F10,
            NativeKeyEvent.VC_F11,
            NativeKeyEvent.VC_F12,
    };

    private void handleBinds() {
        List<String> keyNames = Arrays.stream(SUPPORTED_TOGGLE_KEYS)
                .mapToObj(NativeKeyEvent::getKeyText)
                .toList();

        String currentKeyName = NativeKeyEvent.getKeyText(toggleKey);
        ChoiceDialog<String> dialog = new ChoiceDialog<>(currentKeyName, keyNames);
        dialog.setTitle("Toggle Key");
        dialog.setHeaderText("Select the toggle hotkey");
        dialog.setContentText("Hotkey:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String chosen = result.get();
        for (int keyCode : SUPPORTED_TOGGLE_KEYS) {
            if (NativeKeyEvent.getKeyText(keyCode).equals(chosen)) {
                toggleKey = keyCode;
                break;
            }
        }

        updateToggleButtonText();
        statusLabel.setText("Status: Toggle key set to " + chosen);
    }


    private Path getBaseDir() {
        String appData = System.getenv("APPDATA");
        Path base;

        if (appData != null && !appData.isBlank()) {
            base = Paths.get(appData, "JClicker");
        } else {
            base = Paths.get(System.getProperty("user.home"), ".jclicker");
        }

        try {
            Files.createDirectories(base);
        } catch (IOException e) {
            base = Paths.get("").toAbsolutePath();
        }

        return base;
    }



    @FXML
    public void onToggleButtonAction() {
        performToggle();
    }

    private void performToggle() {
        if (!toggled) {
            String validationError = validateConfig();
            if (validationError != null) {
                statusLabel.setText("Status: " + validationError);
                return;
            }

            ClickConfig config = buildClickConfigFromUi();
            toggled = true;
            clicking = true;
            updateToggleButtonText();
            statusLabel.setText("Status: On");

            startClicker(config);
        } else {
            clicking = false;
            toggled = false;
            updateToggleButtonText();
            statusLabel.setText("Status: Off");
        }
    }

    private void startClicker(ClickConfig config) {
        clickExecutor.submit(() -> {
            try {
                Robot robot = new Robot();
                int buttonMask = getButtonMask(config.clickType);

                int count = 0;

                while (clicking) {
                    if (!config.repeatUntilStopped && count >= config.repeatTimes) {
                        break;
                    }

                    if (config.useCustomLocation) {
                        robot.mouseMove(config.customX, config.customY);
                    }

                    robot.mousePress(buttonMask);
                    robot.mouseRelease(buttonMask);

                    count++;

                    long sleepMillis = (long) config.delayMs;
                    if (sleepMillis < 1) {
                        sleepMillis = 1;
                    }

                    Thread.sleep(sleepMillis);
                }
            } catch (Exception ex) {
                Platform.runLater(() ->
                        statusLabel.setText("Status: Error: " + ex.getMessage()));
            } finally {
                clicking = false;
                toggled = false;
                Platform.runLater(() -> {
                    updateToggleButtonText();
                    if (!statusLabel.getText().startsWith("Status: Error")) {
                        statusLabel.setText("Status: Off");
                    }
                });
            }
        });
    }


    private String validateConfig() {

        if (mouseButtonCombo.getValue() == null) {
            return "Select a click type first";
        }

        boolean repeatUntilStop = untilStopBox.isSelected();
        int repeatTimes = parseIntOrDefault(repeatField.getText(), 0);

        if (!repeatUntilStop && repeatTimes <= 0) {
            return "Set repeat count or enable 'until stopped'";
        }

        boolean useCurrent = useCurrentBox.isSelected();
        boolean useCustom = useCustomBox.isSelected();

        if (!useCurrent && !useCustom) {
            return "Select a location option";
        }

        if (useCustom) {
            try {
                Integer.parseInt(xField.getText().trim());
                Integer.parseInt(yField.getText().trim());
            } catch (NumberFormatException e) {
                return "Custom location requires valid X/Y values";
            }
        }

        return null;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == toggleKey) {
            Platform.runLater(this::performToggle);
        }
    }


    @FXML
    public void onDeleteProfileClicked() {
        String selected = profileCombo.getValue();
        if (selected == null || selected.isEmpty()) return;
        if (!profileManager.containsProfile(selected)) return;

        try {
            profileManager.deleteProfile(selected);
            refreshProfileList();
            statusLabel.setText("Status: Profile deleted");
        } catch (RuntimeException e) {
            showErrorDialog("Delete Profile Error",
                    "Failed to delete profile: " + selected + "\n\n" +
                            "Error: " + e.getMessage());
        } catch (IOException e) {
            showErrorDialog("Refresh Error",
                    "Profile deleted but failed to refresh list.\n\n" +
                            "Error: " + e.getMessage());
        }
    }

    private boolean isValidProfileName(String name) {
        // Allow letters, numbers, spaces, hyphens, and underscores
        // Prevent path traversal and invalid file name characters
        if (name == null || name.isEmpty()) {
            return false;
        }

        // Check for invalid characters for file names
        String invalidChars = "<>:\"/\\|?*";
        for (char c : invalidChars.toCharArray()) {
            if (name.indexOf(c) >= 0) {
                return false;
            }
        }

        // Prevent reserved names on Windows
        String upperName = name.toUpperCase();
        String[] reserved = {"CON", "PRN", "AUX", "NUL",
                "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
                "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
        for (String reservedName : reserved) {
            if (upperName.equals(reservedName) || upperName.startsWith(reservedName + ".")) {
                return false;
            }
        }

        // Prevent names starting/ending with spaces or dots
        if (name.trim().length() != name.length() || name.startsWith(".") || name.endsWith(".")) {
            return false;
        }

        return true;
    }

    private void showErrorDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Cleanup resources when application closes.
     * Should be called from App class on stage close.
     */
    public void cleanup() {
        // Stop clicking if active
        clicking = false;
        toggled = false;

        // Shutdown executor service
        if (clickExecutor != null && !clickExecutor.isShutdown()) {
            clickExecutor.shutdown();
            try {
                // Wait a short time for tasks to complete
                if (!clickExecutor.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS)) {
                    clickExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                clickExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Unregister native hook
        try {
            GlobalScreen.removeNativeKeyListener(this);
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            // Ignore cleanup errors
        }
    }

    private static class ClickConfig {
        ClickType clickType;
        boolean repeatUntilStopped;
        int repeatTimes;
        double delayMs;
        boolean useCurrentLocation;
        boolean useCustomLocation;
        int customX;
        int customY;
    }

}
