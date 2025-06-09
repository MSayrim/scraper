package com.codefy.pricescrapper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SeleniumTest {

    public static void main(String[] args) {
        // Initialize WebDriver using the setupDriver method
        WebDriver driver = setupDriver("chrome", false); // false for non-headless mode

        try {
            // Create HTTP client
            HttpClient client = HttpClient.newHttpClient();

            // Get ingredients list
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/mip/ingredient/list-all-scrape"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray ingredients = jsonResponse.getJSONArray("responseData");

                for (int i = 0; i < ingredients.length(); i++) {
                    JSONObject ingredient = ingredients.getJSONObject(i);
                    String productName = ingredient.getString("name");
                    UUID ingredientId = UUID.fromString(ingredient.getString("id"));

                    // Navigate to Migros website
                    driver.get("https://www.migros.com.tr/");

                    // Wait for search box and perform search
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                    WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("product-search-combobox--trigger")));
                    searchBox.sendKeys(productName);
                    searchBox.sendKeys(Keys.RETURN);

                    try {
                        // Wait for product elements to load
                        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//a[@id='product-name']")));

                        // Get product elements and prices
                        List<WebElement> productElements = driver.findElements(By.xpath("//a[@id='product-name']"));
                        List<WebElement> productPrices = driver.findElements(By.cssSelector(".price-container fe-product-price .price span"));
                        // Create price list
                        JSONArray priceList = new JSONArray();

                        for (int j = 0; j < productElements.size(); j++) {
                            WebElement nameElem = productElements.get(j);
                            WebElement priceElem = productPrices.get(j*2);
                            String priceText = priceElem.getText().trim();

                            // Skip items with non-numeric price
                            if (!priceText.matches(".*\\d.*")) {
                                continue;
                            }

                            JSONObject priceData = new JSONObject();
                            priceData.put("ingredientID", ingredientId);
                            priceData.put("name", nameElem.getText());
                            priceData.put("url", nameElem.getAttribute("href"));
                            priceData.put("price", priceText);
                            priceData.put("processed", false);

                            priceList.put(priceData);
                        }

                        // Send POST request if price list is not empty
                        if (priceList.length() > 0) {
                            System.out.println("Sending JSON data: " + priceList.toString());

                            HttpRequest postRequest = HttpRequest.newBuilder()
                                    .uri(URI.create("http://localhost:8080/mip/scrape-data/save-all"))
                                    .header("Content-Type", "application/json")
                                    .POST(HttpRequest.BodyPublishers.ofString(priceList.toString()))
                                    .build();

                            HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

                            if (postResponse.statusCode() == 200) {
                                System.out.println("Prices successfully saved for " + productName);
                            } else {
                                System.out.println("Failed to save prices for " + productName +
                                        ". Error code: " + postResponse.statusCode() +
                                        " - " + postResponse.body());
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("Could not get prices for product: " + productName);
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Could not fetch products. Response code: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Ensure the driver is closed properly
            if (driver != null) {
                driver.quit();
            }
        }
    }

    // Method to set up WebDriver based on browser type
    private static WebDriver setupDriver(String browserType, boolean headless) {
        WebDriver driver = null;

        switch (browserType.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                chromeOptions.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-extensions");
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }
                driver = new ChromeDriver(chromeOptions);
                break;

            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addPreference("browser.download.folderList", 2);
                firefoxOptions.addPreference("browser.download.dir", "/path/to/downloads");
                firefoxOptions.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf,application/octet-stream");
                firefoxOptions.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.5615.49 Safari/537.36");
                firefoxOptions.addArguments("--disable-blink-features=AutomationControlled");
                firefoxOptions.addPreference("pdfjs.disabled", true);
                if (headless) {
                    firefoxOptions.addArguments("--headless=new");
                }
                driver = new FirefoxDriver(firefoxOptions);
                break;

            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                if (headless) {
                    edgeOptions.addArguments("--headless");
                }
                driver = new EdgeDriver(edgeOptions);
                break;

            default:
                throw new IllegalArgumentException("Unsupported browser type: " + browserType);
        }

        return driver;
    }


}