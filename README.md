### Description

set `webdriver.chrome.driver` property to chromedriver which you could download from https://chromedriver.chromium.org/downloads

Update variables in the class with workflow info we need.

Update `startTime` and `startTime` dates.

Run it.

### Note:

Due to specifics of Temporal UI (dynamic element replacing, what makes UI show only 17 rows), terminator processes dates by 12 hours chunks(configurable by `splitRangeByHours` field) 
and processes the specific url until it has no workflows of 
given name and contained text in the input. 