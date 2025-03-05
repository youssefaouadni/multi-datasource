# 📌 Multi-Data Source Spring Boot Project

## 🚀 Overview
This project implements a **multi-tenant, multi-data source** setup in a Spring Boot application. It dynamically switches databases based on tenant identification provided in HTTP requests.

## ✨ Features
✅ Dynamic data source routing based on tenant context.  
✅ Custom scope for per-tenant bean management.  
✅ Automatic cleanup of inactive tenant data sources.  
✅ Interceptor for extracting tenant information from requests.  
✅ Schema creation for new tenants.  

---

## 🛠️ Installation
### 1️⃣ Clone the repository:
```sh
git clone <repository-url>
cd <project-directory>
```
### 2️⃣ Configure database connection in `application.properties`:
```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.datasource.url=jdbc:mysql://localhost:3306/default_db
```
### 3️⃣ Build and run the project:
```sh
./mvnw spring-boot:run
```

---

## 🔗 API Usage
### ➡️ To interact with a specific tenant, include the `X-Tenant-Id` header in your API requests:
```sh
curl -H "X-Tenant-Id: tenant_123" http://localhost:8080/api/resource
```
### ➕ To create a new tenant:
```sh
curl -X POST http://localhost:8080/api/tenants -H "Content-Type: application/json" -d '{"tenantId": "new_tenant"}'
```

---

## 📂 Project Structure
📁 **config/** - Configuration files, including multi-tenant setup.  
📁 **datasources/** - Data source management and routing.  
📁 **interceptors/** - Custom interceptors for tenant extraction.  
📁 **services/** - Business logic related to tenants and data management.  
📁 **repositories/** - JPA repositories for interacting with the database.  

---

## 🤝 Contributing
1️⃣ Fork the repository.  
2️⃣ Create a feature branch:
```sh
git checkout -b feature-name
```
3️⃣ Commit your changes:
```sh
git commit -m "Add new feature"
```
4️⃣ Push to your fork and create a pull request.

---

## 📜 License
This project is licensed under the **MIT License**.

