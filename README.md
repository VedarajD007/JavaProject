 # Resource Management App

A **Java Swing + MySQL** desktop application for managing company resources with role‑based access control.  
Admins can insert, delete, and view resource requests, while users can view resources and request access.

---

## 🚀 Features

- **Role‑based dashboards**
  - 👤 **User Dashboard**: View resources, request access.
  - 🔧 **Admin Dashboard**: Insert, delete resources, view requests.

- **Resource Management**
  - Add new resources with name, timeline, quantity, and cost.
  - Delete resources by ID.
  - View all resources in a table.

- **Access Requests**
  - Users can request access.
  - Admins can view pending requests.

- **Authentication**
  - Simple login system with hardcoded credentials for demo.
  - Different UI for admin vs. user.

---

## 🛠️ Tech Stack

- **Java Swing** – GUI framework  
- **JDBC (MySQL Connector)** – Database connectivity  
- **MySQL** – Backend database  
- **Maven/Gradle (optional)** – For dependency management  

---

## 📂 Database Schema

### `resource` table
```sql
CREATE TABLE resource (
    resource_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_name VARCHAR(100),
    timeline VARCHAR(50),
    quantity INT,
    cost DOUBLE
);
