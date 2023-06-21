package org.homeschoolpebt.app.utils;

import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import({WebDriverConfiguration.class})
@ActiveProfiles("test")
public abstract class AbstractBasePageTest {
  
  private static final String UPLOADED_JPG_FILE_NAME = "test.jpeg";
  
  @Autowired
  protected RemoteWebDriver driver;

  @Autowired
  protected Path path;

  protected String baseUrl;

  @LocalServerPort
  protected String localServerPort;

  protected Page testPage;

  @BeforeEach
  protected void setUp() throws IOException {
    initTestPage();
    baseUrl = "http://localhost:%s".formatted(localServerPort);
    driver.navigate().to(baseUrl);
  }

  protected void initTestPage() {
    testPage = new Page(driver);
  }

  @SuppressWarnings("unused")
  public void takeSnapShot(String fileWithPath) {
    TakesScreenshot screenshot = driver;
    Path sourceFile = screenshot.getScreenshotAs(OutputType.FILE).toPath();
    Path destinationFile = new File(fileWithPath).toPath();
    try {
      Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void assertPageTitle(String desiredTitle) {
    try {
      new WebDriverWait(driver, Duration.ofMillis(1000)).until(d -> d.getTitle().contains(desiredTitle));
    } catch (TimeoutException e) {
      takeSnapShot("test-failure.png");
      throw e;
    }
  }

  protected void uploadFile(String filepath, String dzName) {
    var hiddenInputSelector = By.className("dz-hidden-input");
    new WebDriverWait(driver, Duration.ofSeconds(1)).until(
      ExpectedConditions.presenceOfElementLocated(hiddenInputSelector)
    );
    WebElement upload = driver.findElement(hiddenInputSelector);

    upload.sendKeys(TestUtils.getAbsoluteFilepathString(filepath));

    var fileDetailsSelector = By.className("file-details");
    new WebDriverWait(driver, Duration.ofSeconds(1)).until(
      ExpectedConditions.textMatches(fileDetailsSelector, Pattern.compile("delete"))
    );
  }

  protected void uploadJpgFile(String dzName) {
    uploadFile(UPLOADED_JPG_FILE_NAME, dzName);

    // wait for upload to complete
    new WebDriverWait(driver, Duration.ofSeconds(3))
      .until(ExpectedConditions.javaScriptThrowsNoExceptions("if (!window[\"isUploadComplete" + dzName + "\"]) { throw new Exception(\"upload incomplete\"); }"));

    assertThat(driver.findElement(By.id("dropzone-" + dzName)).getText().replace("\n", ""))
        .contains(UPLOADED_JPG_FILE_NAME);
  }
}
