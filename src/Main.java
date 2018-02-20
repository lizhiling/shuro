import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    static private int targetYear = 2018;
    static private WebDriverWait wait;
    static private WebDriver driver;

    public static void main(String[] args) {
        Properties props = System.getProperties();
        props.setProperty("webdriver.chrome.driver", "chromedriver");
        Scanner scanner = new Scanner(System.in);
        System.out.println("*********** Take Shuro Records ***********");
        System.out.println("Input target year:");
        targetYear = scanner.nextInt();
        System.out.println("Input target month:");
        int targetMonth = scanner.nextInt();
        System.out.println("Input target day:");
        int targetDay = scanner.nextInt();
        Date target = buildDate(targetYear, targetMonth, targetDay);

        System.out.println("Employee Number:");
        String en = scanner.nextLine();

        driver = new ChromeDriver();

        driver.get("http://192.168.187.207/cws/shuro/main");
        wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("uid"))).sendKeys(en);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("Login"))).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("就労管理"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("勤務実績入力（期間入力用）"))).click();

        int i = 1;
        while (true) {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("APPROVALGRD")));
            WebElement element;
            Date date;
            do {
                if (i >= driver.findElements(By.cssSelector("#APPROVALGRD tr")).size() - 1) {
                    System.out.println("Next month's page.");
                    driver.findElement(By.id("TONXTTM")).click();
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("APPROVALGRD")));
                    i = 1;
                }

                element = driver.findElements(By.cssSelector("#APPROVALGRD tr")).get(i);
                i++;
                date = validDateTr(element);
            } while (date == null);

            if (targetAchieved(date, target)) {
                System.out.println("Done, target date arrived.");
                break;
            }

            takeAttendance(element);
            System.out.println("Attendance taken for " + date);
        }
        driver.close();
    }

    private static Date validDateTr(WebElement element) {
        List<WebElement> targetTds;
        targetTds = element.findElements(By.className("mg_normal"));
        if (targetTds.size() < 6 || targetTds.get(2).getText().indexOf("勤務") != 0) {
            targetTds = element.findElements(By.className("mg_saved"));
            if (targetTds.size() < 6 || !targetTds.get(5).getText().equals("-")) {
                return null;
            }
        }

        int month = Integer.parseInt(targetTds.get(0).findElement(By.id("MONTH")).getText());
        int day = Integer.parseInt(targetTds.get(0).findElement(By.id("DAY")).getText());
        return buildDate(targetYear, month, day);
    }

    private static boolean targetAchieved(Date date, Date target) {
        return date != null && date.after(target);
    }

    private static Date buildDate(int year, int month, int day) {
        return new Date(year - 1900, month - 1, day);
    }

    private static void takeAttendance(WebElement tr) {
        tr.findElement(By.cssSelector("td:nth-of-type(5)")).click();

        while (Integer.parseInt(driver.findElement(By.id("H")).getText()) < 8) {
            WebElement copy = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#PM_PANEL_ENTRY_TIME_WIDGET_CONTAINER_AREA > div > div > div:nth-child(5) > span")));
            try {
                copy.click();
            } catch (Exception e) {
                System.out.println("Error occurred when click 'copy from recently...', retrying");
            }
            try {
                Thread.sleep(300L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                driver.close();
                System.exit(0);
            }
        }

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("PM_PANEL_ENTRY_MESSAGE_TBL")));
        driver.findElement(By.id("btnNext1")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dSave1"))).click();
    }
}
