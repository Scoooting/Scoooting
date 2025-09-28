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
	direction LR
	transport_types {
		bigserial id PK ""  
		varchar name  "NOT NULL, UNIQUE"  
	}
	transport_statuses {
		bigserial id PK ""  
		varchar name  "NOT NULL, UNIQUE"  
	}
	user_roles {
		bigserial id PK ""  
		varchar name  "NOT NULL, UNIQUE"  
	}
	rental_statuses {
		bigserial id PK ""  
		varchar name  "NOT NULL, UNIQUE"  
	}
	users {
		bigserial id PK ""  
		varchar email  "NOT NULL, UNIQUE"  
		varchar name  "NOT NULL"  
		varchar password_hash  "NOT NULL"  
		bigint role_id FK ""  
		integer bonuses  "DEFAULT 0"  
	}
	transports {
		bigserial id PK ""  
		bigint transport_type_id FK ""  
		bigint status_id FK ""  
		real latitude  "NOT NULL"  
		real longitude  "NOT NULL"  
	}
	scooters {
		bigint transport_id  "PK, FK"  
		varchar model  "NOT NULL"  
		decimal battery_level  "0-100, DEFAULT 100"  
	}
	motorcycles {
		bigint transport_id  "PK, FK"  
		varchar model  "NOT NULL"  
		decimal fuel_level  "0-100, DEFAULT 100"  
		integer engine_size  "cc"  
	}
	bicycles {
		bigint transport_id  "PK, FK"  
		varchar model  "NOT NULL"  
		integer gear_count  "DEFAULT 7"  
		boolean is_electric  "DEFAULT false"  
	}
	e_bikes {
		bigint transport_id  "PK, FK"  
		varchar model  "NOT NULL"  
		decimal battery_level  "0-100, DEFAULT 100"  
		integer gear_count  "DEFAULT 7"  
	}
	e_scooters {
		bigint transport_id  "PK, FK"  
		varchar model  "NOT NULL"  
		decimal battery_level  "0-100, DEFAULT 100"  
	}
	rentals {
		bigserial id PK ""  
		bigint user_id FK ""  
		bigint transport_id FK ""  
		bigint status_id FK ""  
		timestamp start_time  "NOT NULL, DEFAULT NOW()"  
		timestamp end_time  ""  
		real start_latitude  "NOT NULL"  
		real start_longitude  "NOT NULL"  
		real end_latitude  ""  
		real end_longitude  ""  
		decimal total_cost  ""  
		integer duration_minutes  ""  
	}
	users}o--||user_roles:"has role"
	users||--o{rentals:"makes"
	transports}o--||transport_types:"of type"
	transports}o--||transport_statuses:"has status"
	transports||--o{rentals:"rented as"
	rentals}o--||rental_statuses:"has status"
	transports||--o|scooters:"details"
	transports||--o|motorcycles:"details"
	transports||--o|bicycles:"details"
	transports||--o|e_bikes:"details"
	transports||--o|e_scooters:"details"
```

#### or a pic if mermaid doesn't work:

<img width="1983" height="3840" alt="Untitled diagram _ Mermaid Chart-2025-09-28-215438" src="https://github.com/user-attachments/assets/708f6efd-e762-40b0-8c23-bb26117bd1fe" />
