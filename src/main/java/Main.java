import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class Main {

  // Configure
  public static final String BASE_URL = "https://example.com/namespaces/default/workflows?status=OPEN&workflowName=";
  public static final String WORKFLOW_NAME = "SomeWorkflowName";
  public static final String WORKFLOW_INPUT_TEXT_TO_CONTAIN = "Notional Prefund. PolicyElection id:";
  public static String startTime = "2021-12-21T00:00:00";
  public static String endTime = "2021-12-29T00:00:00";

  public static final String SEARCH_URL_TEMPLATE = BASE_URL + WORKFLOW_NAME + "&startTime=%s&endTime=%s";
  public static final int splitRangeByHours = 12;

  public static final String WORKFLOW_LINK_CSS_SELECTOR = "a[data-cy='workflow-link']";
  public static final String WORKFLOW_INPUT_XPATH = "/html/body/main/section/section/dl/div[6]/dd/div/pre/code";
  public static final String TERMINATE_BUTTON_SELECTOR = "/html/body/main/section/section/aside/div/a";
  public static final String TERMINATE_BUTTON_IN_MODAL_WINDOW_SELECTOR = "/html/body/main/section/section/div/div/div[2]/footer/a[1]";

  public static void main(String[] args) throws InterruptedException {
    System.out.println("Number of terminated workflows: " + terminateWorkflowsBetweenDates());
  }

  private static int terminateWorkflowsBetweenDates() throws InterruptedException {
    List<String> searchUrls = new ArrayList<>();

    do {
      LocalDateTime from = LocalDateTime.parse(startTime, DateTimeFormatter.ISO_DATE_TIME);
      LocalDateTime to = from.plusHours(splitRangeByHours);
      searchUrls.add(SEARCH_URL_TEMPLATE.formatted(from, to));
      startTime = to.format(DateTimeFormatter.ISO_DATE_TIME);

    } while (LocalDateTime.parse(startTime, DateTimeFormatter.ISO_DATE_TIME)
        .isBefore(LocalDateTime.parse(endTime, DateTimeFormatter.ISO_DATE_TIME)));

    int allTerminatedWorkflowCounter = 0;
    for (String searchUrl : searchUrls) {
      allTerminatedWorkflowCounter += terminateWorkflowsBySearchUrlUntilExists(searchUrl);
    }
    return allTerminatedWorkflowCounter;

  }

  private static int terminateWorkflowsBySearchUrlUntilExists(String searchUrl) throws InterruptedException {
    int allTerminatedWorkflowCounter = 0;
    int terminatedWorkflowCounter;
    do {
      terminatedWorkflowCounter = terminateWorkflowsBySearchUrl(searchUrl);
      allTerminatedWorkflowCounter += terminatedWorkflowCounter;
    } while (terminatedWorkflowCounter != 0);
    return allTerminatedWorkflowCounter;
  }

  private static int terminateWorkflowsBySearchUrl(String searchUrl) throws InterruptedException {
    WebDriver driver = new ChromeDriver();
    driver.navigate()
        .to(searchUrl);

    Thread.sleep(3000);

    Set<String> workflowUrls = driver.findElements(By.cssSelector(WORKFLOW_LINK_CSS_SELECTOR))
        .stream()
        .map(row -> row.getAttribute("href"))
        .collect(Collectors.toSet());

    int terminatedWorkflows = 0;

    System.out.println("Number of located workflows: " + workflowUrls.size());
    System.out.println("Search url: " + searchUrl);
    for (String url : workflowUrls) {
      driver.get(url);
      Thread.sleep(1800);
      WebElement workflowInput = driver.findElement(By.xpath(WORKFLOW_INPUT_XPATH));
      if (workflowInput.getText()
          .contains(WORKFLOW_INPUT_TEXT_TO_CONTAIN)) {
        driver.findElement(By.xpath(TERMINATE_BUTTON_SELECTOR))
            .click();
        Thread.sleep(500);
        driver.findElement(By.xpath(TERMINATE_BUTTON_IN_MODAL_WINDOW_SELECTOR))
            .click();
        Thread.sleep(1000);

        System.out.println("Terminated workflow link: " + url);
        terminatedWorkflows++;
      }
    }
    driver.quit();
    return terminatedWorkflows;
  }
}
