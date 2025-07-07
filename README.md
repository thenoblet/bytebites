# ByteBites Food Delivery Platform - Microservices Architecture

## Overview

ByteBites is a cloud-native food delivery platform built with Spring Boot microservices. This architecture provides a scalable, secure, and resilient system for connecting customers with local restaurants.

## Architecture Diagram

```mermaid
graph TD
    subgraph Client Layer
        A[Client] -->|HTTP Requests| C[API Gateway]
    end

    subgraph Infrastructure Layer
        C --> D[Service Discovery]
        C --> E[Config Server]
        C -->|Route to Auth| F[Auth Service]
        C -->|Route to Restaurant| G[Restaurant Service]
        C -->|Route to Order| H[Order Service]
        C -->|Route to Notification| I[Notification Service]
        D -->|Register| F
        D -->|Register| G
        D -->|Register| H
        D -->|Register| I
    end

    subgraph Security Flow
        A -->|Login Request| C
        C -->|Forward Auth| F
        F -->|JWT Token| C
        C -->|Return JWT| A
        C -->|Validate JWT| F
    end

    subgraph Event-Driven Flow
        H -->|OrderPlacedEvent| J[RabbitMQ]
        J --> G
        J --> I
    end

    subgraph Data Store
        F --> K[ByteBites DB]
        G --> K
        H --> K
    end

    style C fill:#4CAF50,stroke:#388E3C
    style F fill:#2196F3,stroke:#1976D2
    style G fill:#FF9800,stroke:#F57C00
    style H fill:#9C27B0,stroke:#7B1FA2
    style I fill:#607D8B,stroke:#455A64
```

#### Database Schema for Services
```mermaid
erDiagram
    USERS ||--o{ RESTAURANTS : owns
    USERS ||--o{ ORDERS : places
    RESTAURANTS ||--o{ MENU_ITEMS : contains
    ORDERS ||--o{ ORDER_ITEMS : contains
    ORDER_ITEMS }o--|| MENU_ITEMS : references
    
    USERS {
        uuid id PK
        varchar(60) name
        varchar(255) email UK
        varchar(255) password
        varchar(20) role
        boolean oauth2_user
        timestamp created_at
        timestamp updated_at
    }
    
    RESTAURANTS {
        uuid id PK
        varchar(255) name
        text address
        text description
        uuid owner_id FK "Not updatable"
        varchar(50) cuisine_type
        timestamp created_at
        timestamp updated_at
    }
    
    MENU_ITEMS {
        uuid id PK
        uuid restaurant_id FK
        varchar(255) name
        text description
        decimal price
        timestamp created_at
        timestamp updated_at
    }
    
    ORDERS {
        uuid id PK
        uuid customer_id FK
        uuid restaurant_id FK
        varchar(20) status
        decimal total_price
        timestamp created_at
        timestamp updated_at
        timestamp cancelled_at
    }
    
    ORDER_ITEMS {
        uuid id PK
        uuid order_id FK
        uuid menu_item_id FK
        varchar(255) menu_item_name
        integer quantity
        decimal price
        timestamp created_at
        timestamp updated_at
    }
    
    USERS ||--|{ ENUM_ROLE : has
    ENUM_ROLE {
        varchar(20) role
    }

    ORDERS ||--|{ ENUM_ORDER_STATUS : status
    ENUM_ORDER_STATUS {
        varchar(20) status
    }

```

## Key Components

### Core Infrastructure Services
- **Discovery Server**: Eureka service registry
- **Config Server**: Centralized configuration management
- **API Gateway**: Routes requests and handles security

### Business Services
- **Auth Service**: Handles authentication and JWT issuance
- **Restaurant Service**: Manages restaurant data and menus
- **Order Service**: Processes food orders
- **Notification Service**: Handles customer notifications

## Security Implementation

- JWT-based authentication
- Role-Based Access Control (RBAC)
- OAuth2 integration for social login
- Resource ownership validation
- Secure inter-service communication

#### Sequence Diagram: Login & JWT Flow
```mermaid
sequenceDiagram
    participant Client
    participant API_Gateway
    participant Auth_Service
    
    Client->>API_Gateway: POST /auth/login
    API_Gateway->>Auth_Service: Forward login request
    Auth_Service-->>API_Gateway: JWT Token
    API_Gateway-->>Client: Return JWT
    loop Subsequent Requests
        Client->>API_Gateway: Request with JWT
        API_Gateway->>Auth_Service: Validate JWT
        Auth_Service-->>API_Gateway: Validation result
        API_Gateway->>Microservice: Forward request
        Microservice-->>Client: Response
    end
```

## Getting Started

### Prerequisites
- Java 17+
- Docker (for PostgreSQL, MongoDB, RabbitMQ and Redis)
- Git

### Service Startup Order
1. Discovery Server
2. Config Server
3. API Gateway
4. Auth Service
5. Restaurant Service
6. Order Service
7. Notification Service

### Installation
1. Clone the repository:
```bash
  git clone https://github.com/thenoblet/bytebites.git
```

2. Start infrastructure services:
```bash
  docker-compose up -d postgres mongodb rabbitmq redis
```

3. Run services in this order:
```
#1. Infrastructure
discovery-server/
config-server/

# 2. Core Services
api-gateway/
auth-service/

# 3. Business Services
restaurant-service/
order-service/
notification-service/
```

## Testing the System

### Authentication Flow
1. Register a user: `POST /auth/register`
2. Login to get JWT: `POST /auth/login`
3. Use JWT for subsequent requests


| Endpoint | Method | Role Required |
|----------|--------|---------------|
| /auth/register | POST | Public |
| /api/v1/restaurants | GET | Authenticated |
| /api/v1/orders | POST | ROLE_CUSTOMER |
| /admin/users | GET | ROLE_ADMIN |

## Event-Driven Communication

The system uses RabbitMQ for:
- Order placement notifications
- Restaurant order processing updates
- Customer notifications

### Event Flow with RabbitMQ
```mermaid
sequenceDiagram
    participant Client
    participant Order_Service
    participant RabbitMQ
    participant Notification_Service
    participant Restaurant_Service
    
    Client->>Order_Service: Place order
    Order_Service->>RabbitMQ: Publish OrderPlacedEvent
    RabbitMQ->>Notification_Service: Consume event
    RabbitMQ->>Restaurant_Service: Consume event
    Notification_Service-->>Client: Send confirmation
    Restaurant_Service-->>Order_Service: Update preparation status
```

## Monitoring

Each service includes Spring Boot Actuator endpoints and Prometheus/Grafana for health checks and metrics.



## License

This project is licensed under the MIT License.
