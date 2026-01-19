# E-Commerce Backend API

A minimal e-commerce backend system built with Spring Boot, featuring product management, shopping cart, order processing, and payment integration with webhook support.

## 🚀 Features

- ✅ Product CRUD operations
- ✅ Shopping cart management
- ✅ Order creation from cart
- ✅ Payment processing with webhook callbacks
- ✅ Order status updates
- ✅ Stock management
- ✅ Complete REST API

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data MongoDB**
- **Lombok**
- **Maven**
- **MongoDB**

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MongoDB 4.4+ (running on localhost:27017)
- Postman (for API testing)

## ⚙️ Setup Instructions

### 1. Clone or Extract the Project
```bash
cd ecommerce
```

### 2. Install MongoDB

**Ubuntu/Debian:**
```bash
sudo apt-get install mongodb
sudo systemctl start mongodb
```

**macOS:**
```bash
brew install mongodb-community
brew services start mongodb-community
```

**Windows:**
Download and install from [MongoDB Official Site](https://www.mongodb.com/try/download/community)

### 3. Configure Application

Edit `src/main/resources/application.yaml` if needed:
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/ecommerce
      database: ecommerce

server:
  port: 8080
```

### 4. Build the Project
```bash
mvn clean install
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Product APIs

#### Create Product
```http
POST /api/products
Content-Type: application/json

{
  "name": "Laptop",
  "description": "Gaming Laptop",
  "price": 50000.0,
  "stock": 10
}
```

#### Get All Products
```http
GET /api/products
```

#### Get Product by ID
```http
GET /api/products/{id}
```

### Cart APIs

#### Add to Cart
```http
POST /api/cart/add
Content-Type: application/json

{
  "userId": "user123",
  "productId": "prod123",
  "quantity": 2
}
```

#### Get User Cart
```http
GET /api/cart/{userId}
```

#### Clear Cart
```http
DELETE /api/cart/{userId}/clear
```

### Order APIs

#### Create Order
```http
POST /api/orders
Content-Type: application/json

{
  "userId": "user123"
}
```

#### Get Order Details
```http
GET /api/orders/{orderId}
```

### Payment APIs

#### Create Payment
```http
POST /api/payments/create
Content-Type: application/json

{
  "orderId": "order123",
  "amount": 100000.0
}
```

#### Payment Webhook (called by payment gateway)
```http
POST /api/webhooks/payment
Content-Type: application/json

{
  "orderId": "order123",
  "paymentId": "pay_mock123",
  "status": "SUCCESS"
}
```

## 🔄 Complete Purchase Flow

1. **Create Products**
```bash
   POST /api/products
```

2. **Add Items to Cart**
```bash
   POST /api/cart/add
```

3. **View Cart**
```bash
   GET /api/cart/{userId}
```

4. **Create Order**
```bash
   POST /api/orders
```

5. **Initiate Payment**
```bash
   POST /api/payments/create
```

6. **Webhook Callback** (automatic from payment gateway)
```bash
   POST /api/webhooks/payment
```

7. **Check Order Status**
```bash
   GET /api/orders/{orderId}
```

## 🧪 Testing with Postman

### Import Collection Variables

Create these variables in Postman:

- `baseUrl`: `http://localhost:8080`
- `userId`: `user123` (or any user ID)
- `productId`: Save after creating a product
- `orderId`: Save after creating an order

### Sample Test Scenario

1. Create 3 products
2. Add 2 products to cart
3. View cart
4. Create order
5. Create payment
6. Manually call webhook to simulate payment success
7. Verify order status is "PAID"

## 📊 Database Collections

The application creates the following MongoDB collections:

- `users`
- `products`
- `cart_items`
- `orders`
- `order_items`
- `payments`

## 🐛 Troubleshooting

### MongoDB Connection Error
```
Error: Failed to connect to MongoDB
Solution: Ensure MongoDB is running on localhost:27017
```

### Port Already in Use
```
Error: Port 8080 is already in use
Solution: Change port in application.yaml or stop the conflicting service
```

### Insufficient Stock Error
```
Error: Insufficient stock for product
Solution: Check product stock before adding to cart or creating order
```

## 🏗️ Project Structure
```
com.example.ecommerce/
├── controller/          # REST controllers
├── service/            # Business logic
├── repository/         # Data access layer
├── model/             # Entity classes
├── dto/               # Data transfer objects
├── webhook/           # Webhook handlers
├── client/            # External service clients
├── config/            # Configuration classes
└── exception/         # Exception handling
```

## 🔐 Order Status Flow

- `CREATED` → Initial state when order is created
- `PAID` → Payment successful
- `FAILED` → Payment failed
- `CANCELLED` → Order cancelled

## 🔐 Payment Status Flow

- `PENDING` → Payment initiated
- `SUCCESS` → Payment completed
- `FAILED` → Payment failed

## 📝 Mock Payment Service

For testing without an external payment gateway, the application supports a mock payment service that simulates payment processing with a 3-second delay and automatic webhook callback.

To use mock payment service:
1. Payment is created in PENDING state
2. Mock service processes payment (simulated)
3. After 3 seconds, webhook is called automatically
4. Order status updates to PAID or FAILED

## 🚀 Deployment

### Build JAR
```bash
mvn clean package
```

### Run JAR
```bash
java -jar target/ecommerce-0.0.1-SNAPSHOT.jar
```
