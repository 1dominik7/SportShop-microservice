# 🏪 Ecommerce Microservice Store – Sport-Shop

A full-featured **e-commerce platform** built in a **microservices architecture**, supporting:

- User management
- Product catalog
- Order processing
- Marketing campaigns
- Payment integration

Designed for selling **sportswear, footwear, and accessories**, this system is ready to scale with robust backend services and a modern frontend.

---

## Services

- **eureka** – registration and discovery service
- **config** – central application configuration
- **gateway** – API Gateway
- **user** – user management
- **product** – products and categories
- **order** – order management
- **payment** – payments
- **marketing** – marketing campaigns

## 🛠️ Technologies Used

### 🧩 Frontend

- **React** – UI library
- **Redux** – state management
- **TanStack Query** – data fetching & caching
- **Tailwind CSS** – utility-first styling
- **Material UI (MUI)** – UI components
- **React Toastify** – toast notifications
- **Vitest** – unit testing

### 🔧 Backend

- **Java 17 + Spring Boot** – core framework
- **Docker** – containerization
- **Stripe** – card payments integration (more payment methods coming soon)
- **Kafka** – asynchronous messaging/events
- **RabbitMQ** – message queues
- **Keycloak** – authorization/authentication (OAuth2)

### 🗄️ Databases

- **MongoDB** – NoSQL database (user-service)
- **PostgreSQL** – relational database (marketing-service,order-service, payment-service)
- **MySQL** – relational database (product-service)

### 📈 Monitoring & Tracing

- **Grafana** – metrics visualization
- **Prometheus** – metrics collection
- **Zipkin** – distributed tracing

## 🚀 CI/CD Pipeline

Automated with **GitHub Actions**:

- Runs unit tests for each service
- Builds Docker images
- Pushes images to DockerHub
- Deploys to remote VPS using SSH + Docker Compose

---

## 🧪 Local Development

To run the entire system locally:

```bash
docker-compose up --build
```

## 🧩 Changelog

### 🚧 v3 — In progress (React Native mobile app)
- Building mobile app version in **React Native**
- Integration with existing backend API (in progress)
- UI redesign for mobile flow

### 🆕 v2 — Released on 2025-10-29
- Changed view of **option** and **variation** in admin panel -> now grouped & sorted
- Added new features to admin panel:
  - Manage **shipping methods**
  - Manage **order statuses**
- Added **statistics dashboard** in admin panel to track statistics of products and orders
- Integrated new **PayU payment method**

### 🪄 v1 — Released on 2025-08-10
- Initial release of the application
- Core backend and frontend
