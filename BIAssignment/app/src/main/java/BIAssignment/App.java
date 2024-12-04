package BIAssignment;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import javax.imageio.ImageIO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class App {

    private static final String CHROME_DRIVER_PATH = "C:/chromedriver-win64/chromedriver.exe";
    private static String QKART_URL;
    private static String SCREENSHOT_SAVE_PATH;

    public static void readJsonConfig(String relativePath) {
        JSONParser jsonParser = new JSONParser();
        try {
            // Get the absolute path of the JSON file
            String absolutePath = Paths.get(System.getProperty("user.dir"), relativePath).toString();
            try (FileReader reader = new FileReader(absolutePath)) {
                JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
                QKART_URL = (String) jsonObject.get("QKART_URL");
                SCREENSHOT_SAVE_PATH = (String) jsonObject.get("ScrrenshotSaveLocation");
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static WebDriver createDriver() {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--window-size=1920,1200");

        return new ChromeDriver(options);
    }

    public static void printQKartLoadingtime(WebDriver driver) {
        long start = System.currentTimeMillis();
        driver.get(QKART_URL);
        long end = System.currentTimeMillis();
        long loadingTime = (end - start) / 1000; // Convert milliseconds to seconds
        System.out.println("Time taken to load QKart Page: " + loadingTime + " seconds");
    }

    public static void captureFullPageScreenshot(WebDriver driver) {
        try {
            // Delete old screenshots
            File screenshotDir = new File(SCREENSHOT_SAVE_PATH);
            if (screenshotDir.exists() && screenshotDir.isDirectory()) {
                for (File file : screenshotDir.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".png")) {
                        file.delete();
                    }
                }
            }
            // Capture new screenshot
            long currentTimeMillis = System.currentTimeMillis();
            String uniqueFileName = "screenshot_" + currentTimeMillis + ".png";
            Screenshot screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
                    .takeScreenshot(driver);
            ImageIO.write(screenshot.getImage(), "PNG", new File(SCREENSHOT_SAVE_PATH + uniqueFileName));
            System.out.println("Screenshot saved at: " + SCREENSHOT_SAVE_PATH + uniqueFileName);
        } catch (IOException e) {
            System.out.println("Error saving screenshot: " + e.getMessage());
        }
    }

    public static void getProductImageAndURL(WebDriver driver, String productName) throws InterruptedException {
        driver.get(QKART_URL);
        Thread.sleep(5000);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement search = driver.findElement(By.xpath("//input[@name='search']"));
            search.clear();
            search.sendKeys(productName);
            search.sendKeys("\n");

            // Press Enter key
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@class='MuiGrid-root MuiGrid-item MuiGrid-grid-xs-6 MuiGrid-grid-md-3 css-sycj1h']")));
            System.out.println("Search was done, object found");
        } catch (Exception ex) {
            System.out.println("Exception occurred");
        }
        Thread.sleep(5000);
        WebElement priceElement = driver.findElement(By.xpath("//p[contains(text(),'$')]"));
        String price = priceElement.getText();
        System.out.println("Price of the product " + productName + " is " + price);
        Thread.sleep(3000);
        WebElement imageElement = driver
                .findElement(By.xpath("//*[@id='root']/div/div/div[3]/div[1]/div[2]/div/div/img"));
        String imageUrl = imageElement.getAttribute("src");

        System.out.println("Product Name: " + productName + ", Price: " + price);
        System.out.println("Image URL: " + imageUrl);
    }

    public static void main(String[] args) {
        readJsonConfig("/config.json");
        WebDriver driver = createDriver();
        driver.manage().window().maximize();
        String input = String.join(" ", args);
        try {
            printQKartLoadingtime(driver);
            captureFullPageScreenshot(driver);
            getProductImageAndURL(driver, input);
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }
}
