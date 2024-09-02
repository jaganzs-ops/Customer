package customeer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class Ide {
	
	public static void main (String[] args)  {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Customer ID For 'Multiple' or 'Single' Account ");
        String inputType = scanner.nextLine().toLowerCase();
       
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run Chrome in headless mode
        options.addArguments("--disable-gpu"); // Disable GPU acceleration
        options.addArguments("--no-sandbox"); // Necessary for some environments
        options.addArguments("--window-size=1920,1080"); // Set window size for headless mode
        WebDriver driver = new ChromeDriver();
        if (inputType.equalsIgnoreCase("multiple")) {
            System.out.print("Enter the path to the Excel file: ");
            String excelFilePath = scanner.nextLine();
            String path = excelFilePath;
            processExcelFile(path, driver);
        } else {
            System.out.print("Enter your Amazon email: ");
            String email = scanner.nextLine();
            System.out.print("Enter your Amazon password: ");
            String password = scanner.nextLine();
            processSingleAccount(email, password, driver);
        }
        scanner.close();
        driver.quit();
    }
    private static void processSingleAccount(String email, String password, WebDriver driver) {
        String customerId = loginAndGetCustomerId(email, password, driver);
        // Create a JTextArea to allow copying of the email and customer ID
        JTextArea textArea = new JTextArea(2, 30);
        textArea.setText("Email: " + email + "\nCustomer ID: " + customerId);
        textArea.setEditable(false);
        // Show the dialog with JTextArea
        JOptionPane.showMessageDialog(null, new JScrollPane(textArea), "Account Information", JOptionPane.INFORMATION_MESSAGE);
    }
    private static void processExcelFile(String path, WebDriver driver) {
        try (FileInputStream fileInputStream = new FileInputStream(path);
             XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(fileInputStream)) { // Use XSSFWorkbook for .xlsx
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String email = row.getCell(0).getStringCellValue();
                String password = row.getCell(1).getStringCellValue();
                String customerId = loginAndGetCustomerId(email, password, driver);
                row.createCell(2).setCellValue(customerId);
            }
            try (FileOutputStream fileOutputStream = new FileOutputStream(path)) {
                workbook.write(fileOutputStream);
            }
            JOptionPane.showMessageDialog(null, "Customer IDs have been written to the Excel file.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String loginAndGetCustomerId(String email, String password, WebDriver driver) {
        String customerId = "Not found";
        try {
            driver.get("https://www.amazon.com/ap/signin?openid.pape.max_auth_age=0&openid.return_to=https%3A%2F%2Fwww.amazon.com%2F%3Fref_%3Dnav_custrec_signin&openid.identity=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.assoc_handle=usflex&openid.mode=checkid_setup&openid.claimed_id=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0");
            WebElement usernameField = driver.findElement(By.id("ap_email"));
            usernameField.sendKeys(email);
            WebElement continueButton = driver.findElement(By.id("continue"));
            continueButton.click();
            WebElement passwordField = driver.findElement(By.id("ap_password"));
            passwordField.sendKeys(password);
            WebElement signInButton = driver.findElement(By.id("signInSubmit"));
            signInButton.click();
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            driver.get("view-source:https://www.amazon.com/hz/contact-us/foresight/hubgateway");
            String pageSource = driver.getPageSource();
            String customerIdPrefix = "\"customerId\":\"";
            int startIndex = pageSource.indexOf(customerIdPrefix);
            if (startIndex != -1) {
                startIndex += customerIdPrefix.length();
                int endIndex = pageSource.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    customerId = pageSource.substring(startIndex, endIndex);
                }
            }
            driver.get("https://www.amazon.com/ap/signin?openid.pape.max_auth_age=0&openid.return_to=https%3A%2F%2Fwww.amazon.com%2F%3Fref_%3Dnav_custrec_signin&openid.identity=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.assoc_handle=usflex&openid.mode=checkid_setup&openid.claimed_id=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0");
                WebElement Switchbutton = driver.findElement(By.id("ap_switch_account_link"));
                Switchbutton.click();
                WebElement signout = driver.findElement(By.xpath("/html/body/div[1]/div[1]/div[2]/div/div[2]/div/div/div[1]/div/div/div[2]/div[1]/div[1]/div/div/a"));
                signout.click();
                driver.manage().timeouts().implicitlyWait(2,TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customerId;
    }
}

