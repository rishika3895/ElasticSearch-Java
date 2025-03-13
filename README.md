# Elasticsearch vs Traditional Search Comparison

This application demonstrates the performance difference between traditional Java-based search and Elasticsearch. It provides a web interface to search through a product catalog and displays the search time for both approaches in milliseconds.

## Features

- Product catalog with sample data
- Traditional search using Java collections and string matching
- Elasticsearch integration for advanced search capabilities
- Performance comparison with execution times
- Clean web interface to visualize results

## Prerequisites

- Java 11 or higher
- Maven
- Elasticsearch 7.17.0 running on localhost:9200

## How to Run

### 1. Start Elasticsearch

Make sure Elasticsearch is running on localhost:9200. You can download and run Elasticsearch from the [official website](https://www.elastic.co/downloads/elasticsearch).

```bash
# Start Elasticsearch (after installation)
./bin/elasticsearch
```

### 2. Build and Run the Application

```bash
# Navigate to the project directory
cd elastic

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### 3. Access the Application

Open your browser and navigate to [http://localhost:8080](http://localhost:8080)

## How It Works

1. The application loads a sample product catalog on startup
2. Products are indexed in Elasticsearch
3. When you search:
   - Traditional search filters products using Java's stream API and string matching
   - Elasticsearch search uses its query DSL with field boosting
   - Both approaches are timed and results are displayed side by side

## Implementation Details

- **Traditional Search**: Implemented in Java using stream filtering and string matching
- **Elasticsearch Search**: Uses the Elasticsearch Java High-Level REST Client
- **Performance Measurement**: System.currentTimeMillis() for timing both approaches
- **UI**: Built with Spring Boot, Thymeleaf, and Bootstrap

## Notes

- For small datasets, traditional search might be faster due to Elasticsearch network overhead
- As the dataset grows, Elasticsearch will significantly outperform traditional search
- Elasticsearch provides more advanced search capabilities like relevance scoring and fuzzy matching