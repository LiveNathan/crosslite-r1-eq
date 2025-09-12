# Project Overview

This project is a command-line utility for converting CrossLite EQ settings files (.txt) to d&b audiotechnik R1 format (.rcp). It is a Java application built with Spring Boot and Spring Shell, and it can be compiled into a native executable using GraalVM.

## Key Technologies

*   **Java 21**
*   **Spring Boot 3**
*   **Spring Shell 3**
*   **Maven**
*   **GraalVM**

## Architecture

The application follows a simple, modular architecture:

*   **`ConversionCommands`**: Defines the command-line interface (CLI) using Spring Shell. It handles user input and orchestrates the conversion process.
*   **`FileConversionService`**: Contains the core business logic for reading, converting, and writing files.
*   **`CrossLiteParser`**: Parses the input CrossLite EQ settings from a text file.
*   **`EqConverter`**: Converts the parsed CrossLite settings to the R1 format, including clamping values to the supported ranges.
*   **`R1Writer`**: Generates the final XML output in the d&b R1 format.

# Building and Running

## Prerequisites

*   Java 21
*   Maven

## Building from Source

To build the project and create a JAR file, run the following command:

```bash
./mvnw clean package
```

## Running the Application

Once the project is built, you can run the application using the following command:

```bash
java -jar target/crosslite-r1-eq-0.0.1-SNAPSHOT.jar
```

This will start the Spring Shell, and you can use the commands defined in `ConversionCommands`.

## Available Commands

*   `convert-file`: Convert a single CrossLite file to R1 format.
*   `convert-directory`: Convert all .txt files in a directory to R1 format.
*   `help-conversion`: Show detailed help for conversion commands.

## Building a Native Executable

To build a native executable using GraalVM, run the following command:

```bash
./mvnw native:compile
```

This will create an executable file in the `target` directory.

# Development Conventions

*   **Testing**: The project includes unit tests for the parser, converter, and writer components. Tests are located in the `src/test/java` directory and can be run using the `./mvnw test` command. The project uses **AssertJ** for assertions.
*   **Code Style**: The code follows standard Java conventions.
