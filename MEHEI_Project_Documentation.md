# MEHEI Project Documentation

## 1. Project Overview
**MEHEI** is a modern, feature-rich platform designed to connect users with professional Mehendi artists. It allows users to discover artists based on location, view their portfolios, check their tiers and ratings, and book appointments or "Flash Slots" for quick service.

## 2. Key Features and Functions

### User & Artist Discovery
- **Dynamic Location Tracking**: Automatically detects the user's city (using GPS & Reverse Geocoding) to filter artists available in their area.
- **Advanced Filtering**: Users can filter artists by their expertise tier (Master, Apprentice, Associate) or specifically look for **Flash Deals** (immediate/discounted slots).
- **Search Capabilities**: Search for artists by name, event type, or specific styles.

### Booking & Checkout System
- **Real-time Booking Calculator**: Allows users to select their requirements (e.g., number of hands, style) and calculates the total rate and required deposit.
- **Seamless Checkout**: Integrates with Razorpay for secure deposit payments to confirm bookings.
- **Booking History**: Users can view their upcoming and past bookings, track statuses, and interact with the artists via chat.

### Artist Dashboard & Portfolio
- **Profile Management**: Artists can set up their profiles, upload portfolios, and define their specialties (e.g., Bridal, Arabic, Minimalist).
- **Booking Management**: Artists have a dedicated dashboard to accept, track, and manage incoming customer bookings.

## 3. Technology Stack Explanation

The project is built using a modern, scalable client-server architecture.

### Frontend (Android App)
- **Language**: Kotlin
- **UI Framework**: **Jetpack Compose** (Declarative UI for smooth, modern, and reactive interfaces).
- **Architecture**: **MVVM** (Model-View-ViewModel) paired with Unidirectional Data Flow (UDF) via StateFlow and SharedFlow.
- **Dependency Injection**: **Dagger Hilt** (Ensures scalable and testable code by managing dependencies).
- **Navigation**: **Jetpack Navigation Compose** (Handles routing between screens seamlessly).
- **Location Services**: **Google Play Services Location** (`FusedLocationProviderClient`) combined with Android's `Geocoder`.
- **Image Loading**: **Coil** (Lightweight image loading library backed by Kotlin Coroutines).

### Backend (REST API)
- **Framework**: **Spring Boot 3.x** (Java)
- **Database**: **MySQL** (Relational database for strong data consistency).
- **ORM**: **Hibernate / Spring Data JPA** (Maps Java objects to database tables).
- **Security**: **Spring Security & JWT** (JSON Web Tokens) for secure, stateless user authentication and role management.
- **Error Handling**: Uses **RFC 7807 Problem Details** for consistent API error responses globally.

### Additional Components
- **ECC-main**: Contains Rust-based components (`src/config/mod.rs`), potentially used for specialized high-performance tasks or cryptographic functions within the ecosystem.

## 4. Developer Guide & Tutorial

Here is a step-by-step guide on how to get the project running locally.

### Prerequisites
- **Android Studio** (Latest version for Jetpack Compose support).
- **Java Development Kit (JDK) 17+** (For Spring Boot).
- **MySQL Server** installed and running locally.

### Step 1: Running the Backend
1. Open your MySQL client and create the database:
   ```sql
   CREATE DATABASE madhi;
   ```
2. Navigate to the `mehei-backend` directory in your terminal or IntelliJ IDEA.
3. Update the `src/main/resources/application.properties` file with your MySQL credentials if they differ from the defaults:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=YourPassword
   ```
4. Run the Spring Boot application using your IDE or Maven wrapper.

### Step 2: Running the Android App
1. Open the `MEHEI` project folder in **Android Studio**.
2. Allow Gradle to sync and download all dependencies (such as Compose BOM, Hilt, and Retrofit).
3. Connect a physical Android device or start an Android Emulator.
4. Click the **Run 'app'** button (Play icon) in the top toolbar.

> [!TIP]
> **Location Testing**: If testing on an emulator, use the emulator's extended controls (three dots menu) to spoof your GPS location to "Mumbai", "Pune", "Delhi", or "Bangalore" to see the mock artist data populate on the Explore screen.
