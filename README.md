# Airbnb Clone - Full Stack Application (Spring Boot + React)

## Project Overview
This is a **full-stack Airbnb clone** built using **Spring Boot** for the backend and **React** for the frontend. It replicates key functionalities of Airbnb, allowing users to browse listings, book stays, and manage their properties.

## Tech Stack
- **Backend:** Spring Boot, Spring Security, JPA, Hibernate, PostgreSQL, JWT Authentication
- **Frontend:** React.js, Redux, Tailwind CSS
- **Database:** PostgreSQL
- **APIs:** RESTful APIs with Swagger documentation
- **Authentication:** JWT-based authentication with role-based access control

## Features
### User Features:
- Sign up & login (JWT authentication)
- Browse available listings
- Filter properties based on location, price, and amenities
- Book stays and manage reservations

### Host Features:
- List new properties with images, descriptions, and pricing
- Edit and delete property listings
- View and manage bookings

### Admin Features:
- Manage users and properties
- Review flagged listings

## Installation Guide
### Backend Setup
1. Clone the repository:
   ```sh
   git clone https://github.com/98001yash/airBnb-application.git
   cd airBnb-application/backend
   ```
2. Configure **application.properties** for database settings:
3. Run the backend:
   ```sh
   mvn spring-boot:run
   ```

### Frontend Setup
1. Navigate to the frontend directory:
   ```sh
   cd ../frontend
   ```
2. Install dependencies:
   ```sh
   npm install
   ```
3. Start the frontend:
   ```sh
   npm start
   ```

## API Endpoints
| Endpoint         | Method | Description |
|-----------------|--------|-------------|
| `/auth/signup`  | POST   | User registration |
| `/auth/login`   | POST   | User authentication |
| `/listings`     | GET    | Fetch all listings |
| `/listings/{id}`| GET    | Fetch listing details |
| `/bookings`     | POST   | Create a booking |

## Future Enhancements
- Payment gateway integration (Stripe, PayPal)
- Advanced search with Google Maps API
- Reviews and ratings for listings
- AI-based property recommendations

## Contributing
Feel free to contribute by opening issues and pull requests.

## License
This project is licensed under the MIT License.

---
**Made with ❤️ by Yash Chauhan**

