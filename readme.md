## Generating large PDF files using JasperReports [![Twitter](https://img.shields.io/twitter/follow/piotr_minkowski.svg?style=social&logo=twitter&label=Follow%20Me)](https://twitter.com/piotr_minkowski)

[![CircleCI](https://circleci.com/gh/piomin/sample-jasperreport-boot.svg?style=svg)](https://circleci.com/gh/piomin/sample-jasperreport-boot)

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-black.svg)](https://sonarcloud.io/dashboard?id=piomin_sample-jasperreport-boot)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=piomin_sample-jasperreport-boot&metric=bugs)](https://sonarcloud.io/dashboard?id=piomin_sample-jasperreport-boot)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=piomin_sample-jasperreport-boot&metric=coverage)](https://sonarcloud.io/dashboard?id=piomin_sample-jasperreport-boot)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=piomin_sample-jasperreport-boot&metric=ncloc)](https://sonarcloud.io/dashboard?id=piomin_sample-jasperreport-boot)

A Spring Boot sample application demonstrating how to generate large PDF reports using JasperReports with memory optimization techniques.

## Overview

Detailed description can be found here: [Generating large PDF files using JasperReports](https://piotrminkowski.com/2017/06/12/generating-large-pdf-files-using-jasperreports/)

This application showcases three different approaches for generating PDF reports with JasperReports:

1. **Standard Report Generation** (`/pdf/{age}`) - Basic in-memory report generation
2. **File Virtualizer** (`/pdf/fv/{age}`) - Uses JRFileVirtualizer to handle large reports by spilling data to disk
3. **Swap File Virtualizer** (`/pdf/sfv/{age}`) - Uses JRSwapFileVirtualizer for efficient memory management during report generation

## Technologies

- **Java 21**
- **Spring Boot 4.0.3**
- **JasperReports 6.21.5** - Report generation engine
- **H2 Database** (for testing)
- **Maven**

## Prerequisites

- JDK 21 or higher
- Maven 3.6+
- (Optional) MySQL database for production data source

## Quick Start

```bash
# Clone the repository
git clone https://github.com/piomin/sample-jasperreport-boot.git
cd sample-jasperreport-boot

# Run the application
mvn spring-boot:run

# Run tests
mvn test
```

The application will start on port **2222** by default.

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/pdf/{age}` | GET | Generate PDF report filtered by age (standard approach) |
| `/pdf/fv/{age}` | GET | Generate PDF using File Virtualizer for memory optimization |
| `/pdf/sfv/{age}` | GET | Generate PDF using Swap File Virtualizer |

### Example Usage

```bash
# Generate a PDF report for age 25
curl -O -J http://localhost:2222/pdf/25

# Generate using File Virtualizer
curl -O -J http://localhost:2222/pdf/fv/25

# Generate using Swap File Virtualizer
curl -O -J http://localhost:2222/pdf/sfv/25
```

## Architecture

### Components

- **JasperController** - REST endpoints for PDF generation with three virtualizer strategies
- **JasperApplication** - Spring Boot application with Beans for:
    - `JasperReport` - Compiled report template (caches `.jasper` file)
    - `JRFileVirtualizer` - Shared file virtualizer for memory-efficient report filling
    - `JRSwapFileVirtualizer` - Alternative swap-based virtualizer

### Report Template

The report template (`report.jrxml`) is compiled on first startup and cached as `personReport.jasper`. It queries a `person` table with an optional age filter parameter.

### Memory Optimization

For large datasets, JasperReports can consume excessive memory. This application demonstrates two virtualizer approaches:

- **JRFileVirtualizer**: Spills report pages to temporary files when memory threshold is reached
- **JRSwapFileVirtualizer**: More efficient disk-based storage using swap files with configurable block size

## Configuration

### Application Properties (`application.yml`)

```yaml
server:
  port: ${PORT:2222}  # Server port (default: 2222)

spring:
  application:
    name: jasper-service
  # datasource:        # Uncomment and configure for MySQL/PostgreSQL
  #   url: jdbc:mysql://localhost:3306/mydb
  #   username: user
  #   password: pass

directory: ./         # Working directory for virtualizer temp files
```

### Database Setup

By default, the application expects a `person` table with columns matching the report template. Configure your datasource in `application.yml`:

- **Development**: Uncomment and configure the MySQL datasource
- **Testing**: H2 in-memory database is used automatically

## Testing

The project includes a concurrent load test (`JasperApplicationTest`) that simulates 20 parallel PDF generation requests to verify thread safety and performance.

```bash
mvn test -Dtest=JasperApplicationTest
```

## Project Structure

```
sample-jasperreport-boot/
├── src/
│   ├── main/
│   │   ├── java/pl/piomin/jasperreport/
│   │   │   ├── JasperApplication.java       # Main class with Beans
│   │   │   └── controller/
│   │   │       └── JasperController.java  # REST endpoints
│   │   └── resources/
│   │       ├── application.yml            # App configuration
│   │       └── report.jrxml               # Jasper report template
│   └── test/
│       ├── java/.../JasperApplicationTest.java
│       └── resources/application-test.yml
├── personReport.jasper                     # Compiled report (generated)
├── personReport.jrxml                      # Report source (optional)
└── pom.xml
```

## License

This project is open source and available under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).