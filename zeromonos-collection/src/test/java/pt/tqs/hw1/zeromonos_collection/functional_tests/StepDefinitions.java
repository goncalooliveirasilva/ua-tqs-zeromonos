// package pt.tqs.hw1.zeromonos_collection.functional_tests;
// package pt.tqs.hw1.zeromonos_collection;

// import io.cucumber.java.After;
// import io.cucumber.java.en.Given;
// import io.cucumber.java.en.Then;
// import io.cucumber.java.en.When;
// import io.cucumber.datatable.DataTable;
// import org.openqa.selenium.*;
// import org.openqa.selenium.chrome.ChromeDriver;
// import org.openqa.selenium.support.ui.ExpectedConditions;
// import org.openqa.selenium.support.ui.WebDriverWait;
// import io.github.bonigarcia.wdm.WebDriverManager;

// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;

// import java.time.Duration;
// import java.util.Map;

// public class StepDefinitions {
    
//     private WebDriver driver;
//     private WebDriverWait wait;
//     private String savedToken;

//     @After
//     public void tearDown() {
//         if (driver != null) {
//             driver.quit();
//         }
//     }

//     @Given("the user is on the homepage")
//     public void openHomePage() {
//         WebDriverManager.chromedriver().setup();
//         driver = new ChromeDriver();
//         wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//         driver.get("http://localhost:5173");
//     }

//     @Given("I am on the registration page")
//     public void gotoRegistrationPageCitizen() {
//         driver.findElement(By.id("get-started-button")).click();
//         wait.until(ExpectedConditions.urlContains("/login"));
//     }

//     @When("I register with the following details:")
//     public void register(DataTable dataTable) {
//         Map<String, String> d = dataTable.asMap();
        
//         driver.findElement(By.id("register-username")).sendKeys(d.get("username"));
//         driver.findElement(By.id("register-email")).sendKeys(d.get("email"));
//         driver.findElement(By.id("register-password")).sendKeys(d.get("password"));
//         driver.findElement(By.id("register-submit")).click();
//     }

//     @Then("my account should be created successfully")
//     public void accountCreationSuccess() {
//         wait.until(ExpectedConditions.urlContains("/citizen/dashboard"));
//     }

//     @Then("I should be logged in")
//     public void loggedIn() {
//         assertTrue(driver.getCurrentUrl().contains("/citizen/dashboard"));
//     }

//     @Given("I am logged in as a citizen")
//     public void loginAsCitizen() {
//         driver.get("http://localhost:5173/citizen/dashboard");
//         wait.until(ExpectedConditions.presenceOfElementLocated(By.id("booking-district")));
//     }

//     @When("I create a booking with the following details:")
//     public void createBooking(DataTable dataTable) {
//         Map<String, String> d = dataTable.asMap();

//         driver.findElement(By.id("booking-district")).click();
//         driver.findElement(By.id("district-option-" + d.get("district").toLowerCase())).click();

//         wait.until(ExpectedConditions.elementToBeClickable(By.id("booking-municipality")));
//         driver.findElement(By.id("booking-municipality")).click();
//         driver.findElement(By.id("municipality-option-" + d.get("municipality").toLowerCase())).click();

//         wait.until(ExpectedConditions.elementToBeClickable(By.id("booking-village")));
//         driver.findElement(By.id("booking-village")).click();
//         driver.findElement(By.id("village-option-" + d.get("village").toLowerCase())).click();

//         driver.findElement(By.id("booking-postalcode")).clear();
//         driver.findElement(By.id("booking-postalcode")).sendKeys(d.get("postalCode"));

//         WebElement dateField = driver.findElement(By.id("booking-date"));
//         dateField.clear();
//         dateField.sendKeys(d.get("date"));
//         dateField.sendKeys(Keys.TAB);
        
//         wait.until(ExpectedConditions.elementToBeClickable(By.id("booking-time")));
//         driver.findElement(By.id("booking-time")).click();
//         driver.findElement(By.id("time-option-" + d.get("time").replace(":", ""))).click();

//         driver.findElement(By.id("booking-description")).clear();
//         driver.findElement(By.id("booking-description")).sendKeys(d.get("description"));

//         driver.findElement(By.id("booking-submit")).click();
//     }

//     @Then("the booking should be created successfully")
//     public void bookingCreatedSuccessfully() {
//         wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("booking-token")));
//     }

//     @Then("I should receive a booking token")
//     public void bookingTokenReceived() {
//         WebElement token = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("booking-token")));
//         savedToken = token.getText();
//         assertNotNull(savedToken);
//         assertFalse(savedToken.trim().isEmpty());
//         assertTrue(token.isDisplayed());
//     }

//     @Given("I have a valid booking token")
//     public void haveValidToken() {
//         assertNotNull(savedToken);
//         driver.get("http://localhost:5173");
//         wait.until(ExpectedConditions.presenceOfElementLocated(By.id("booking-token-input")));
//     }

//     @When("I request the booking details using the token")
//     public void requestBookingDetails() {
//         WebElement tokenInput = driver.findElement(By.id("booking-token-input"));
//         tokenInput.clear();
//         tokenInput.sendKeys(savedToken);
//         driver.findElement(By.id("booking-token-button")).click();
//     }

//     @Then("I should see the booking information including:")
//     public void bookingDetails(DataTable dataTable) {
//         Map<String, String> expected = dataTable.asMap();

//         WebElement bookingCard = wait.until(
//             ExpectedConditions.visibilityOfElementLocated(By.id("booking-card"))
//         );

//         String cardText = bookingCard.getText();
        
//         if (!expected.get("district").isEmpty()) {
//             assertTrue(cardText.contains(expected.get("district")));
//         }
//         assertTrue(cardText.contains(expected.get("municipality")));
//         assertTrue(cardText.contains(expected.get("village")));
//         assertTrue(cardText.contains(expected.get("postalCode")));
//         assertTrue(cardText.contains(expected.get("date")));
//         assertTrue(cardText.contains(expected.get("time")));
//         assertTrue(cardText.contains(expected.get("description")));
//         assertTrue(cardText.contains(expected.get("state")));
//     }

//     @Given("I am logged in as a staff member")
//     public void loginAsStaff() {
//         driver.get("http://localhost:5173/staff/dashboard");
//         wait.until(ExpectedConditions.presenceOfElementLocated(By.id("staff-filter-district")));
//     }

//     @Given("there are existing bookings in the system")
//     public void ensureExistingBookings() {
//         wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bookings-table")));
//         WebElement tableBody = driver.findElement(By.id("bookings-table-body"));
//         assertTrue(!tableBody.findElements(By.tagName("tr")).isEmpty());
//     }

//     @When("I filter bookings by district {string} and municipality {string}")
//     public void filterBookings(String district, String municipality) {
//         driver.findElement(By.id("staff-filter-district")).click();
//         driver.findElement(By.id("staff-district-option-" + district.toLowerCase())).click();

//         try {
//             Thread.sleep(500);
//         } catch (InterruptedException e) {
//             e.printStackTrace();
//         }

//         driver.findElement(By.id("staff-filter-municipality")).click();
//         driver.findElement(By.id("staff-municipality-option-" + municipality.toLowerCase())).click();

//         // Wait for table to update
//         try {
//             Thread.sleep(1000);
//         } catch (InterruptedException e) {
//             e.printStackTrace();
//         }
//     }

//     @Then("I should see only bookings matching the selected district and municipality")
//     public void assertFilteredBookings() {
//         WebElement tableBody = wait.until(
//             ExpectedConditions.presenceOfElementLocated(By.id("bookings-table-body"))
//         );
        
//         var rows = tableBody.findElements(By.tagName("tr"));
//         assertTrue(rows.size() > 0);
        
//         for (WebElement row : rows) {
//             String rowText = row.getText();
//             assertTrue(rowText.contains("Lisbon"));
//         }
//     }

//     @Given("a booking exists with state {string}")
//     public void bookingExistsWithState(String state) {
//         wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bookings-table")));
//         WebElement stateChip = wait.until(
//             ExpectedConditions.presenceOfElementLocated(By.id("booking-state-chip-received"))
//         );
//         assertTrue(stateChip.isDisplayed());
//     }

//     @When("I update the booking state to {string}")
//     public void updateBookingState(String newState) {
//         WebElement updateButton = wait.until(
//             ExpectedConditions.elementToBeClickable(By.id("update-state-button-0"))
//         );
//         updateButton.click();

//         wait.until(ExpectedConditions.presenceOfElementLocated(By.id("update-state-dialog")));

//         driver.findElement(By.id("new-state-select")).click();
//         driver.findElement(By.id("state-option-" + newState.toLowerCase())).click();

//         driver.findElement(By.id("confirm-update-state-button")).click();

//         wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("update-state-dialog")));
//     }

//     @Then("the booking state should be updated to {string}")
//     public void assertBookingStateUpdated(String newState) {
//         WebElement chip = wait.until(
//             ExpectedConditions.visibilityOfElementLocated(
//                 By.id("booking-state-chip-" + newState.toLowerCase())
//             )
//         );
//         assertTrue(chip.isDisplayed());
//     }

//     @Then("the booking history should include a record for this state change")
//     public void assertBookingHistory() {
//         WebElement viewButton = wait.until(
//             ExpectedConditions.elementToBeClickable(By.id("view-details-button-0"))
//         );
//         viewButton.click();

//         wait.until(ExpectedConditions.presenceOfElementLocated(By.id("booking-details-dialog")));

//         WebElement historySection = wait.until(
//             ExpectedConditions.visibilityOfElementLocated(By.id("state-history-section"))
//         );
//         assertTrue(historySection.isDisplayed());
//         assertTrue(
//             historySection.getText().contains("State History"));
//     }
// }