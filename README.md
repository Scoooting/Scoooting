# Scoooting #
### Проект по дисциплине "Высокопроизводительные системы". ###
### Аренда электросамокатов. ###

---
После клонирования репозитория ввести команду (сейчас не работает):
```
docker compose up --build -d
```

## Тестирование

Для запуска тестов следует предварительно собрать образы всех микросервисов

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

## Circuit Breaker

### FSM

|<img width="1190" height="1098" alt="image_2025-10-26_01-35-09" src="https://github.com/user-attachments/assets/1f61303a-18f8-4d58-8369-2bd91a03482e" />|
|-|

### Demo

|1. CLOSED -> OPEN |<img width="1280" height="1019" alt="image" src="https://github.com/user-attachments/assets/a4fc9e17-cc10-4498-84d1-43bf095b56b9" />|
|-|-|
|**2. OPEN -> HALF_OPEN**|<img width="1076" height="673" alt="image" src="https://github.com/user-attachments/assets/77a2fb46-c0e0-4fcb-a8eb-e807896f5d6b" />|
|**3. HALF_OPEN -> CLOSED**|<img width="1280" height="540" alt="image" src="https://github.com/user-attachments/assets/b1d16a5d-07e7-4043-a15d-be5323e2160f" />|


