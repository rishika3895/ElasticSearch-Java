# Elasticsearch with Java Spring Boot

This application demonstrates the integration of Elasticsearch Cloud with Spring Boot, providing a powerful search engine for a product catalog with advanced search capabilities.

## Features

- Product catalog with sample data
- Elasticsearch Cloud integration for advanced search capabilities
- Secure SSL connection to Elasticsearch Cloud
- Clean web interface to visualize results
- Advanced search features including relevance scoring and fuzzy matching

## Prerequisites

- Java 11 or higher
- Maven
- Elasticsearch Cloud account (configured)
- MySQL Database

## Configuration

The application uses the following configurations:

- Elasticsearch Cloud connection with SSL
- Basic authentication for secure access
- MySQL database for product data storage
- Spring Data Elasticsearch for repository management

## Implementation Details

- **Elasticsearch Search**: Uses the Elasticsearch Java High-Level REST Client
- **Security**: SSL context and basic authentication for Elasticsearch Cloud
- **UI**: Built with Spring Boot, Thymeleaf, and Bootstrap

## Notes

- Elasticsearch provides advanced search capabilities like relevance scoring and fuzzy matching
- The application uses connection pooling and timeout configurations for optimal performance
- Secure communication is ensured through SSL and authentication

## Getting Started

1. Configure your Elasticsearch Cloud credentials in `application.properties`
2. Set up your MySQL database
3. Run the application using: `mvn spring-boot:run`
4. Access the application at: `http://localhost:8081`