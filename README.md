# airBnb-application

# airBnbApp

**airBnbApp** is a full-stack web application built to replicate the core functionality of Airbnb. The project consists of a Spring Boot backend for handling the server-side logic and a React frontend that provides an intuitive user interface for browsing, listing, and booking properties. The platform enables users to search for properties, create bookings, manage their profiles, and interact with other users.

## Features

### Frontend (React)
- **Responsive UI**: A clean and user-friendly interface built with React.
- **Property Listings**: Browse through various properties available for booking with sorting and filtering options.
- **User Authentication**: Register, login, and manage user sessions.
- **Booking System**: Users can book properties based on availability.
- **Profile Management**: Users can view and update their personal profiles.
- **Property Search**: Search for properties based on location, price range, and amenities.
  
### Backend (Spring Boot)
- **RESTful API**: The backend is built with Spring Boot, providing RESTful APIs to handle frontend requests.
- **User Authentication**: Implemented using JWT (JSON Web Token) for secure user authentication and authorization.
- **Database**: Utilizes a relational database (e.g., PostgreSQL, MySQL) for storing user and property data.
- **CRUD Operations**: Create, read, update, and delete operations for properties and bookings.
- **Booking Management**: Allows users to create, update, and view their bookings.
- **Property Management**: Admins can add, edit, and remove property listings.
- **Error Handling**: Centralized exception handling using `@ControllerAdvice`.

## Tech Stack

### Frontend:
- **React**: For building a dynamic and responsive user interface.
- **Axios**: For making HTTP requests to the backend API.
- **React Router**: For handling routing within the application.
- **Material-UI**: For styling the frontend and providing ready-to-use UI components.

### Backend:
- **Spring Boot**: For building the backend REST API and handling business logic.
- **Spring Security**: For securing the application and handling JWT-based authentication.
- **JPA / Hibernate**: For ORM (Object-Relational Mapping) and database operations.
- **PostgreSQL / MySQL**: Database for storing data related to users, properties, and bookings.
- **JWT (JSON Web Token)**: For managing user authentication and authorization.

## Installation and Setup

### Prerequisites:
- **Java 11 or above** for the Spring Boot backend
- **Node.js** (v12 or above) for the React frontend
- **npm** or **yarn** for managing frontend dependencies
- **PostgreSQL** or **MySQL** for the database

### Backend Setup (Spring Boot):
1. Clone the repository:
   ```bash
   git clone https://github.com/98001yash/airBnbApp.git
