package tomiks;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
	static WebDriver driver = null;

	public static String getDir() throws URISyntaxException {
		return new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
				.toURI()).getPath();
	}

	public static void main(String[] args) {



		String profilePath = "";

		try {
			profilePath = getDir().replace('\\', '/');
			int li = profilePath.lastIndexOf('/');
			profilePath = new StringBuilder(profilePath).replace(li, profilePath.length(), "").toString();

			Files.createDirectory(Paths.get(profilePath + "/profile"));
		} catch (URISyntaxException | IOException e) {
			System.out.println(e.getMessage());
		}

		System.setProperty("webdriver.chrome.driver", profilePath + "/chrome/chromedriver.exe");

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--window-size=480,640");
		options.addArguments("--allow-profiles-outside-user-dir");
		options.addArguments("--enable-profile-shortcut-manager");
		options.addArguments("user-data-dir=" + profilePath+"/profile");
		options.addArguments("--profile-directory=Profile 1");

		driver = new ChromeDriver(options);

		try {
			driver.get("https://hh.ru/applicant/resumes");

			try {
				while (driver.findElement(By.className("account-login-page")).isDisplayed()) {
					System.out.println("Please login to hh.ru");
					Thread.sleep(5000);
				}

			} catch (NoSuchElementException e) {
				System.out.println("success login?");

				if (SystemTray.isSupported()) {

					Thread.sleep(1500);

					String windowTitle = driver.getTitle() + " - Google Chrome";
					WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);

					if (hwnd != null) {
						User32.INSTANCE.ShowWindow(hwnd, WinUser.SW_HIDE);
					}


					SystemTray tray = SystemTray.getSystemTray();

					Image image = Toolkit.getDefaultToolkit().getImage(profilePath + "/image.jpg");

					PopupMenu popup = new PopupMenu();

					MenuItem exitItem = new MenuItem("Выход");
					exitItem.addActionListener(e1 -> {
						driver.quit();
						System.exit(0);
					});

					MenuItem showItem = new MenuItem("Показать окно");
					showItem.addActionListener(e1 -> {
						if (hwnd != null) {
							User32.INSTANCE.ShowWindow(hwnd, WinUser.SW_SHOW);
						}
					});

					MenuItem hideItem = new MenuItem("Скрыть окно");
					hideItem.addActionListener(e1 -> {
						if (hwnd != null) {
							User32.INSTANCE.ShowWindow(hwnd, WinUser.SW_HIDE);
						}
					});

					popup.add(showItem);
					popup.add(hideItem);
					popup.add(exitItem);

					TrayIcon trayIcon = new TrayIcon(image, "hh auto updater", popup);
					trayIcon.setImageAutoSize(true);

					try {
						tray.add(trayIcon);
					} catch (AWTException ex) {
						System.err.println("Не удалось добавить иконку в трей: " + ex.getMessage());
					}
				} else {
					System.err.println("Системный трей не поддерживается!");
				}

				new Thread(() -> {
					while (true) {
						try {
							Thread.sleep(1500);
						} catch (InterruptedException ignored) {
						}
						update();
					}
				}).start();
			}

			System.out.println("quit");

		} catch (InterruptedException ignored) {
		} finally {
			Scanner scanner = new Scanner(System.in);
			System.out.println("Нажмите Enter для завершения...");
			scanner.nextLine();
			scanner.close();

			driver.quit();
		}

	}

	public static void update() {
		try {
			WebElement element = driver.findElement(By.xpath("//button[.//*[last()][contains(text(), 'Поднять в поиске')]]"));
			element.click();
			System.out.println("click");

			try {
				Thread.sleep(1000);
				driver.get("https://hh.ru/applicant/resumes");
			} catch (Exception ignored) {
			}
		} catch (Exception ignored) {
			try {
				Thread.sleep(15 * 60 * 1000);
				driver.get("https://hh.ru/applicant/resumes");
			} catch (Exception ignored2) {
			}
		}

	}
}