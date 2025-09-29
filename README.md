# Scoooting #
### Проект по дисциплине "Высокопроизводительные системы". ###
### Аренда электросамокатов. ###

---
После клонирования репозитория ввести команду:
```
docker compose up --build -d
```

## MVP database architecture

```mermaid
---
config:
  layout: dagre
  theme: redux-dark-color
title: Scoooting
---
erDiagram
    %% Reference Tables (for flexible enums only)
    transport_statuses {
        bigserial id PK
        varchar name "NOT NULL, UNIQUE"
    }
    
    user_roles {
        bigserial id PK
        varchar name "NOT NULL, UNIQUE"
    }
    
    rental_statuses {
        bigserial id PK
        varchar name "NOT NULL, UNIQUE"
    }
    
    cities {
        bigserial id PK
        varchar name "NOT NULL"
        real center_latitude "NOT NULL"
        real center_longitude "NOT NULL"
        integer radius_km "NOT NULL, DEFAULT 25"
    }
    
    %% Core Tables
    users {
        bigserial id PK
        varchar email "NOT NULL, UNIQUE"
        varchar name "NOT NULL"
        varchar password_hash "NOT NULL"
        bigint role_id FK
        bigint city_id FK
        integer bonuses "DEFAULT 0"
    }
    
    transports {
        bigserial id PK
        varchar transport_type "NOT NULL (enum in code)"
        bigint status_id FK
        bigint city_id FK "NULL if outside cities"
        real latitude "NOT NULL"
        real longitude "NOT NULL"
    }
    
    %% Key Transport Details (simplified)
    electric_kick_scooters {
        bigint transport_id "PK, FK"
        varchar model "NOT NULL"
        decimal battery_level "0-100"
        integer max_speed "km/h"
    }
    
    electric_scooters {
        bigint transport_id "PK, FK"
        varchar model "NOT NULL"
        decimal battery_level "0-100"
        integer max_speed "km/h"
        boolean has_storage_box
    }
    
    electric_bicycles {
        bigint transport_id "PK, FK"
        varchar model "NOT NULL"
        decimal battery_level "0-100"
        integer gear_count "DEFAULT 7"
    }
    
    gas_motorcycles {
        bigint transport_id "PK, FK"
        varchar model "NOT NULL"
        decimal fuel_level "0-100"
        integer engine_size "cc"
    }
    
    %% Business Logic
    rentals {
        bigserial id PK
        bigint user_id FK
        bigint transport_id FK
        bigint status_id FK
        timestamp start_time "DEFAULT NOW()"
        timestamp end_time
        real start_latitude "NOT NULL"
        real start_longitude "NOT NULL"
        real end_latitude
        real end_longitude
        decimal total_cost
        integer duration_minutes
        decimal distance_km
    }
    
    %% Many-to-Many: User Favorites
    user_favorite_transports {
        bigserial id PK
        bigint user_id FK
        bigint transport_id FK
        timestamp added_at "DEFAULT NOW()"
        varchar note "optional user note"
    }
    
    %% Many-to-Many: Transport Maintenance
    maintenance_records {
        bigserial id PK
        bigint transport_id FK
        bigint operator_id FK "user who performed maintenance"
        varchar maintenance_type "NOT NULL"
        varchar description
        decimal cost
        timestamp performed_at "DEFAULT NOW()"
    }
    
    %% One-to-Many Relationships
    user_roles ||--o{ users : "has role"
    cities ||--o{ users : "registered in"
    cities ||--o{ transports : "located in (optional)"
    
    transport_statuses ||--o{ transports : "has status"
    rental_statuses ||--o{ rentals : "has status"
    
    users ||--o{ rentals : "makes"
    transports ||--o{ rentals : "rented as"
    
    %% One-to-One (Transport Details)
    transports ||--o| electric_kick_scooters : "details"
    transports ||--o| electric_scooters : "details"
    transports ||--o| electric_bicycles : "details"
    transports ||--o| gas_motorcycles : "details"
    
    %% Many-to-Many Relationships
    users ||--o{ user_favorite_transports : "favorites"
    transports ||--o{ user_favorite_transports : "favorited by"
    
    users ||--o{ maintenance_records : "performs maintenance"
    transports ||--o{ maintenance_records : "maintained"
```

#### or a pic if mermaid doesn't work:

|<img width="3840" height="1499" alt="Untitled diagram _ Mermaid Chart-2025-09-28-225001" src="https://github.com/user-attachments/assets/370db482-e16c-4b9f-b7c7-63006dee0f8f" />|
|-|

## Microservices MVP architectural diagram

```mermaid
---
config:
  layout: elk
title: Scoooting
---
flowchart LR
 subgraph Infrastructure["Infrastructure"]
        Eureka["Eureka Server<br>Service Registry<br>:8761"]
        Config["Config Server<br>Configuration<br>:8888"]
        Gateway["API Gateway<br>Entry Point<br>:8080"]
  end
 subgraph Microservices["Microservices"]
        UserSvc["User Service<br>WebFlux +R2DBC<br>:8081"]
        RentalSvc["Rental Service<br>WebFlux + JDBC<br>:8082"]
        TransportSvc["Transport Service<br>MVC + JDBC<br>:8083"]
  end
    Client["Client/Frontend"] -- HTTP --> Gateway
    Gateway -- Route /api/users/** --> UserSvc
    Gateway -- Route /api/rentals/** --> RentalSvc
    Gateway -- Route /api/transports/** --> TransportSvc
    UserSvc -. "> 1. Register at startup<br>> 2. Heartbeat every 30s" .-> Eureka
    RentalSvc -. "> 1. Register at startup<br>> 2. Heartbeat every 30s" .-> Eureka
    TransportSvc -. "> 1. Register at startup<br>> 2. Heartbeat every 30s" .-> Eureka
    Gateway -. Discover services<br>Load balance .-> Eureka
    UserSvc -. "Fetch config at startup<br>GET /user-service/default" .-> Config
    RentalSvc -. "Fetch config at startup<br>GET /rental-service/default" .-> Config
    TransportSvc -. "Fetch config at startup<br>GET /transport-service/default" .-> Config
    RentalSvc -- Feign: GET /transports/id<br>Circuit Breaker --> TransportSvc
    RentalSvc -- Feign: GET /users/id --> UserSvc
    UserSvc -- Spring JDBC<br>Blocking --> UserDB["User DB<br>PostgreSQL<br>users_db"]
    RentalSvc -- "Spring JDBC<br>Blocking" --> RentalDB["Rental DB<br>PostgreSQL<br>rentals_db"]
    TransportSvc -- R2DBC<br>Reactive --> TransportDB["Transport DB<br>PostgreSQL<br>transports_db"]
    style Eureka fill:#e1f5ff
    style Config fill:#e1f5ff
    style Gateway fill:#ffe1e1
    style UserSvc fill:#fff4e1
    style RentalSvc fill:#e1ffe1
    style TransportSvc fill:#f0e1ff
```

#### or a pic if mermaid doesn't work:

|<img width="3840" height="2222" alt="Mermaid Chart - Create complex, visual diagrams with text  A smarter way of creating diagrams -2025-09-29-130110" src="https://github.com/user-attachments/assets/aaa9b082-6d34-4661-acbd-4691dfbc71d0" />|
|-|
