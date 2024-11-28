package com.codefy.pricescrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SeleniumTest {
    public static void main(String[] args) throws InterruptedException {

        String MAIL = "muratsayrim@hotmail.com.tr";  // E-posta adresinizi girin
        String PASSWORD = "mrt1907eR!";    // Şifrenizi girin
        String prompt = "Merhaba"; // Gönderilecek metin
        // ChromeDriver'ın yolunu ayarla
        System.setProperty("webdriver.chrome.driver", "/Users/muratsayrim/IdeaProjects/pricescrapper/src/main/java/webdriver/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=./");
        options.addArguments("detach=true");
        options.addArguments("excludeSwitches=enable-logging");

        WebDriver driver = new ChromeDriver(options);

        try {
            // OpenAI giriş sayfasına git
            driver.get("https://chatgpt.com/");
        //    Thread.sleep(65000);
//
        //    // "Giriş Yap" butonunu tıkla
        //    WebElement loginButton = driver.findElements(By.tagName("button")).get(0);
        //    loginButton.click();
        //    Thread.sleep(15000);
//
        //    // E-posta alanını doldur
        //    WebElement emailInput = driver.findElements(By.tagName("input")).get(1);
        //    emailInput.sendKeys(MAIL);
//
        //    // Devam butonuna bas
        //    WebElement continueButton = driver.findElements(By.tagName("button")).get(0);
        //    continueButton.click();
//
        //    // Şifre alanını doldur
        //    Thread.sleep(15000);
        //    WebElement passwordInput = driver.findElements(By.tagName("input")).get(2);
        //    passwordInput.sendKeys(PASSWORD);
//
        //    // Giriş butonunu tıkla
        //    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        //    WebElement loginPasswordButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("_button-login-password")));
        //    loginPasswordButton.click();
        Thread.sleep(30000);

            // Prompt gönder
            WebElement textArea = driver.findElements(By.tagName("textarea")).get(0);
            textArea.sendKeys(prompt);
            Thread.sleep(5000);
            textArea.sendKeys(Keys.ENTER);
            Thread.sleep(30000);

            // Sonuçları oku
            List<WebElement> paragraphElements = driver.findElements(By.tagName("p"));
            List<String> results = new ArrayList<>();
            for (WebElement element : paragraphElements) {
                results.add(element.getText());
            }

            // Sonuçları yazdır
            System.out.println("Sonuçlar:");
            for (String result : results) {
                System.out.println(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Tarayıcıyı kapatma
            driver.quit();
        }

    }
}