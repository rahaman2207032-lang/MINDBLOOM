# Mental Health Tracking Application

A comprehensive mental health tracking application with desktop (JavaFX) and backend (Spring Boot) components.

## Features

1. Daily mood tracking
2. Journal/diary entries
3. Meditation or breathing exercises
4. Sleep tracking
5. Habit tracking
6.  Anxiety/stress level monitoring
7.  Gratitude journal
8.  Therapy session notes
9.  Mood trends and analytics
10. Reminders/notifications
11. Mental health assessment with scoring
12. Progress reports
13.  Psychiatrist session requests (Zoom integration)
14. Community forum
15. User and Instructor profiles
16. Session management for instructors
17. Automated session recommendations
18. User/Instructor authentication

## Project Structure


## Prerequisites

- Java JDK 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6+
- IntelliJ IDEA (recommended)
- Android Studio (for Android app)

## Setup Instructions

### Backend Setup

1. Navigate to `backend/` folder
2. Create PostgreSQL database: `mental_health_db`
3. Configure database in `src/main/resources/application.properties`
4. Run: `mvn spring-boot:run`
5. Backend will start at: `http://localhost:8080`

### Desktop Setup

1. Navigate to `desktop/` folder
2. Make sure backend is running
3. Run: `mvn javafx:run`

### Android Setup

Coming soon... 

## Technologies

- **Backend**: Spring Boot, Spring Security, PostgreSQL, JPA/Hibernate
- **Desktop**: JavaFX, FXML, CSS
- **Android**: Native Java, Material Design
- **Authentication**: JWT (JSON Web Tokens)

## Authors

- Mahmuda Rahaman

