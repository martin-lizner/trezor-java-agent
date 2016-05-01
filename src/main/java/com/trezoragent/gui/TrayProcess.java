package com.trezoragent.gui;

import com.trezoragent.mouselistener.JNIMouseHook;
import com.trezoragent.mouselistener.MouseClickOutsideComponentEvent;
import com.trezoragent.sshagent.DeviceService;
import com.trezoragent.sshagent.KeepKeyService;
import com.trezoragent.sshagent.SSHAgent;
import com.trezoragent.sshagent.TrezorService;
import com.trezoragent.utils.AgentConstants;
import com.trezoragent.utils.AgentUtils;
import com.trezoragent.utils.ExceptionHandler;
import com.trezoragent.utils.LocalizedLogger;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author martin.lizner
 */
public class TrayProcess {

    private static TrayIcon trayIcon;
    private static JNIMouseHook MOUSE_HOOK;
    private final static String VISIBLE_PROPERTY = "visible";

    public static SSHAgent agent;
    public static DeviceService deviceService;

    public static Properties settings;
    public static String deviceType;

    protected static void start() throws Exception {
        agent = new SSHAgent();

        if (agent.isCreatedCorrectly()) {

            File settingsFile = new File(System.getProperty("user.home") + File.separator + AgentConstants.SETTINGS_FILE_NAME);
            if (!settingsFile.exists()) { // TODO: release file?
                try {
                    settings = AgentUtils.initSettingsFile(settingsFile); // create default settings file
                    Logger.getLogger(TrayProcess.class.getName()).log(Level.INFO, "New settings file created: {0}", new Object[]{settingsFile.getPath()});
                } catch (Exception ex) {
                    TrayProcess.createError(LocalizedLogger.getLocalizedMessage("INIT_SETTINGS_FILE_ERROR", ex.getLocalizedMessage()), false);
                    Logger.getLogger(TrayProcess.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                settings = new Properties();
                settings.load(new FileInputStream(settingsFile));
                Logger.getLogger(TrayProcess.class.getName()).log(Level.INFO, "Existing settings file loaded: {0}", new Object[]{settingsFile.getPath()});
            }

            String deviceTypeProperty = AgentUtils.readSetting(settings, AgentConstants.SETTINGS_KEY_DEVICE, AgentConstants.TREZOR_LABEL);

            // start device USB communication
            switch (deviceTypeProperty.toLowerCase()) {
                case (AgentConstants.SETTINGS_KEEPKEY_DEVICE): // TODO: switch to enum?
                    deviceType = AgentConstants.KEEPKEY_LABEL;
                    deviceService = KeepKeyService.startKeepKeyService();
                    break;
                default:
                    deviceType = AgentConstants.TREZOR_LABEL;
                    deviceService = TrezorService.startTrezorService();
            }

            SwingUtilities.invokeLater(new Runnable() { // start GUI
                @Override
                public void run() {
                    TrayProcess.createAndShowGUI();
                }
            });

            agent.startMainLoop(); // start SSH Agent emulating Pageant and listening Windows requests
        }
    }

    private static void createAndShowGUI() {
        if (!SystemTray.isSupported()) {
            Logger.getLogger(StartAgentGUI.class.getName()).log(Level.SEVERE, "SYSTRAY_NOT_SUPPORTED");
            agent.exitProcess();
            return;
        }

        trayIcon = new TrayIcon(TrayProcess.createImage(AgentConstants.ICON16_PATH, AgentConstants.ICON_DESCRIPTION), AgentConstants.APP_PUBLIC_NAME);
        final SystemTray tray = SystemTray.getSystemTray();
        final AgentPopUpMenu popUpMenu = new AgentPopUpMenu(tray, trayIcon, agent, deviceService);

        MOUSE_HOOK = new JNIMouseHook(popUpMenu);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popUpMenu.setLocation(e.getX(), e.getY());
                    popUpMenu.setInvoker(popUpMenu);
                    popUpMenu.setVisible(true);
                }
            }
        });

        popUpMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e instanceof MouseClickOutsideComponentEvent) {
                    popUpMenu.setVisible(false);
                }
            }
        });

        popUpMenu.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(VISIBLE_PROPERTY)) {
                    if (evt.getNewValue().equals(Boolean.TRUE)) {
                        if (!MOUSE_HOOK.isIsHooked()) {
                            MOUSE_HOOK.setMouseHook();
                        }
                    } else if (MOUSE_HOOK.isIsHooked()) {
                        MOUSE_HOOK.unsetMouseHook();
                    }
                }
            }
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            Logger.getLogger(StartAgentGUI.class.getName()).log(Level.SEVERE, "TRAY_ICON_LOAD_ERROR", e);
            return;
        }
        trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, LocalizedLogger.getLocalizedMessage("APPLICATION_INFO", AgentConstants.VERSION));
            }
        });
    }

    // Display exceptions to GUI
    public static void handleException(Throwable ex) {
        String exceptionKey = ExceptionHandler.getErrorKeyForException(ex);
        createError(LocalizedLogger.getLocalizedMessage(exceptionKey, ex), true);
        Logger.getLogger(SSHAgent.class.getName()).log(Level.SEVERE, "", ex);
    }

    public static void createWarning(String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(AgentConstants.APP_PUBLIC_NAME, message, TrayIcon.MessageType.WARNING);
        }
        Logger.getLogger(SSHAgent.class.getName()).log(Level.WARNING, message);
    }

    public static void createErrorWindow(String message) {
        JOptionPane.showMessageDialog(null, message, AgentConstants.APP_PUBLIC_NAME, JOptionPane.ERROR_MESSAGE);
        Logger.getLogger(SSHAgent.class.getName()).log(Level.SEVERE, message);
    }

    protected static Image createImage(String path, String description) {
        URL imageURL = StartAgentGUI.class.getResource(path);
        if (imageURL == null) {
            Logger.getLogger(StartAgentGUI.class.getName()).log(Level.SEVERE, "RESOURCE_NOT_FOUND", path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }

    public static void createError(String message, boolean addLogLinkMessage) {
        String message2 = message;
        if (addLogLinkMessage) {
            message2 = message.concat("\n").concat(LocalizedLogger.getLocalizedMessage(AgentConstants.LINK_TO_LOG_KEY));
        }
        if (trayIcon != null) {
            trayIcon.displayMessage(AgentConstants.APP_PUBLIC_NAME, message2, TrayIcon.MessageType.ERROR);
        }
        Logger.getLogger(SSHAgent.class.getName()).log(Level.SEVERE, message);
    }

    public static void createInfo(String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(AgentConstants.APP_PUBLIC_NAME, message, TrayIcon.MessageType.INFO);
        }
        Logger.getLogger(SSHAgent.class.getName()).log(Level.INFO, message);
    }

}
