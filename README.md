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
