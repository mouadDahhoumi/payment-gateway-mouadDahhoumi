#!/bin/bash

# Build the Spring Boot application using Maven
./mvnw clean package

# Build Docker images
docker-compose build

# Start containers
docker-compose up
