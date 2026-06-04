package io.eclipse.arcana.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        useAssetsAsWorkingDirectory();
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(createCoreApplication(), getDefaultConfiguration());
    }

    private static com.badlogic.gdx.ApplicationListener createCoreApplication() {
        try {
            Class<?> openerType = Class.forName("io.eclipse.arcana.DebugWindowOpener");
            Object opener = Proxy.newProxyInstance(
                Lwjgl3Launcher.class.getClassLoader(),
                new Class<?>[] { openerType },
                (proxy, method, args) -> {
                    if ("open".equals(method.getName()) && args != null && args.length == 1) {
                        openDebugWindow(args[0]);
                    }
                    return null;
                });

            Class<?> coreType = Class.forName("io.eclipse.arcana.Core");
            Constructor<?> constructor = coreType.getConstructor(openerType);
            return (com.badlogic.gdx.ApplicationListener) constructor.newInstance(opener);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create Arcana core application.", e);
        }
    }

    private static void useAssetsAsWorkingDirectory() {
        File assets = findAssetsDirectory();
        if (assets != null) {
            System.setProperty("arcana.assets.dir", assets.getAbsolutePath());
            System.setProperty("user.dir", assets.getAbsolutePath());
        }
    }

    private static void openDebugWindow(Object context) {
        if (!(Gdx.app instanceof Lwjgl3Application)) return;

        Lwjgl3WindowConfiguration configuration = new Lwjgl3WindowConfiguration();
        configuration.setTitle("Arcana Debug");
        configuration.setWindowedMode(760, 860);
        int x = Math.max(0, Lwjgl3ApplicationConfiguration.getDisplayMode().width - 790);
        configuration.setWindowPosition(x, 60);
        setWindowIcons(configuration);

        try {
            Class<?> debugPanelType = Class.forName("io.eclipse.arcana.DebugPanel");
            Class<?> contextType = Class.forName("io.eclipse.arcana.DebugContext");
            com.badlogic.gdx.ApplicationListener debugPanel =
                (com.badlogic.gdx.ApplicationListener) debugPanelType.getConstructor(contextType).newInstance(context);
            ((Lwjgl3Application) Gdx.app).newWindow(debugPanel, configuration);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to open Arcana debug window.", e);
        }
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Arcana");
        //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.

        configuration.setWindowedMode(1600, 1000);
        // 물리 픽셀 단위로 렌더링 — DPI 스케일링(125%, 150% 등) 환경에서도 선명하게 출력
        configuration.setHdpiMode(HdpiMode.Pixels);
        //// You can change these files; they are in lwjgl3/src/main/resources/ .
        //// They can also be loaded from the root of assets/ .
        setWindowIcons(configuration);

        //// This could improve compatibility with Windows machines with buggy OpenGL drivers, Macs
        //// with Apple Silicon that have to emulate compatibility with OpenGL anyway, and more.
        //// This uses the dependency `com.badlogicgames.gdx:gdx-lwjgl3-angle` to function.
        //// You would need to add this line to lwjgl3/build.gradle , below the dependency on `gdx-backend-lwjgl3`:
        ////     implementation "com.badlogicgames.gdx:gdx-lwjgl3-angle:$gdxVersion"
        //// You can choose to add the following line and the mentioned dependency if you want; they
        //// are not intended for games that use GL30 (which is compatibility with OpenGL ES 3.0).
        //// Know that it might not work well in some cases.
//        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);

        return configuration;
    }

    private static void setWindowIcons(Lwjgl3ApplicationConfiguration configuration) {
        File iconDir = iconDirectory();
        if (iconDir == null) return;

        configuration.setWindowIcon(Files.FileType.Absolute,
            new File(iconDir, "libgdx128.png").getAbsolutePath(),
            new File(iconDir, "libgdx64.png").getAbsolutePath(),
            new File(iconDir, "libgdx32.png").getAbsolutePath(),
            new File(iconDir, "libgdx16.png").getAbsolutePath());
    }

    private static void setWindowIcons(Lwjgl3WindowConfiguration configuration) {
        File iconDir = iconDirectory();
        if (iconDir == null) return;

        configuration.setWindowIcon(Files.FileType.Absolute,
            new File(iconDir, "libgdx128.png").getAbsolutePath(),
            new File(iconDir, "libgdx64.png").getAbsolutePath(),
            new File(iconDir, "libgdx32.png").getAbsolutePath(),
            new File(iconDir, "libgdx16.png").getAbsolutePath());
    }

    private static File iconDirectory() {
        File cwd = new File(System.getProperty("user.dir"));
        File projectRoot = "assets".equals(cwd.getName()) ? cwd.getParentFile() : cwd;
        File iconDir = new File(projectRoot, "lwjgl3/src/main/resources");
        return new File(iconDir, "libgdx128.png").isFile() ? iconDir : null;
    }

    private static File findAssetsDirectory() {
        File cwd = new File(System.getProperty("user.dir"));
        File[] candidates = {
            cwd,
            new File(cwd, "assets"),
            new File(cwd, "../assets"),
            new File(cwd, "../../assets")
        };

        for (File candidate : candidates) {
            File normalized = candidate.getAbsoluteFile();
            if (new File(normalized, "TitleA/titleA.png").isFile()) {
                return normalized;
            }
        }
        return null;
    }
}
