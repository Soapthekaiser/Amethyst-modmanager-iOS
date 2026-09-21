package net.kdt.pojavlaunch.uikit;

import java.io.*;
import java.lang.reflect.*;
import java.util.jar.*;
import net.kdt.pojavlaunch.utils.MCOptionUtils;
import net.kdt.pojavlaunch.*;
import org.lwjgl.glfw.*;

public class UIKit {
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_UP = 1;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_MOVE_MOTION = 3;

    private static final String FORGE_INSTALLER_MAIN = "net.minecraftforge.installer.SimpleInstaller";
    private static final String FORGE_INSTALL_CLIENT = "--installClient";

    private static int guiScale;

    private static void patch_FlatLAF_setLinux() {
        String osName = System.getProperty("os.name");
        System.setProperty("os.name", "Linux");
        try {
            Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass("com.formdev.flatlaf.util.SystemInfo");
            clazz.getField("isMacOS").get(null);
        } catch (Throwable e) {
            System.out.println("Skipped patch_FlatLAF_setLinux");
        }
        System.setProperty("os.name", osName);
    }

    private static boolean hasOptionArgument(String[] args) {
        for (String arg : args) {
            if (arg != null && arg.startsWith("--")) {
                return true;
            }
        }
        return false;
    }

    public static void callback_JavaGUIViewController_launchJarFile(final String filepath, String[] args) throws Throwable {
        String mainClass = null;

        try (JarFile jarfile = new JarFile(filepath)) {
            Manifest manifest = jarfile.getManifest();
            if (manifest != null) {
                mainClass = manifest.getMainAttributes().getValue("Main-Class");
            }
        }
        if (mainClass == null) {
            throw new IllegalArgumentException("no main manifest attribute, in \"" + filepath + "\"");
        }

        patch_FlatLAF_setLinux();

        if (args == null) {
            args = new String[0];
        }
        if (FORGE_INSTALLER_MAIN.equals(mainClass) && !hasOptionArgument(args)) {
            args = new String[] { FORGE_INSTALL_CLIENT };
        }

        Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(mainClass);
        Method method = clazz.getMethod("main", String[].class);
        method.invoke(null, new Object[]{args});
    }

    public static void updateMCGuiScale() {
        MCOptionUtils.load();
        String str = MCOptionUtils.get("guiScale");
        guiScale = (str == null ? 0 :Integer.parseInt(str));

        int scale = Math.max(Math.min(GLFW.mGLFWWindowWidth / 320, GLFW.mGLFWWindowHeight / 240), 1);
        if(scale < guiScale || guiScale == 0){
            guiScale = scale;
        }
        updateMCGuiScale(guiScale);
    }

    static {
        System.load(System.getenv("BUNDLE_PATH") + "/AngelAuraAmethyst");
    }

    public static native void showError(String title, String message, boolean exitIfOk);

    private static native void updateMCGuiScale(int scale);
}
