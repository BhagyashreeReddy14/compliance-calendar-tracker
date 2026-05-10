# run-backend.ps1
# This script bypasses a common Windows issue where spaces in the JAVA_HOME path break Maven commands.

Write-Host "Starting Compliance Calendar Tracker..." -ForegroundColor Green
Write-Host "Using bundled Apache Maven 3.9.9" -ForegroundColor Cyan

# Clear JAVA_HOME to force Maven to use the Java executable found in the PATH (which works without space issues)
$env:JAVA_HOME=""

# Run the Spring Boot application using the bundled Maven
.\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
