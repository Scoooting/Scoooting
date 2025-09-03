# Scoooting #
### Проект по дисциплине "Высокопроизводительные системы". ###
### Аренда электросамокатов. ###

---
Для запуска Spring приложения требуется добавить файл ```application.yml``` 
в директорию ```src/main/resources```.

Структура файла:

```
spring:
  application:
    name: scoooting

  datasource:
    driver-class-name: org.postgresql.Driver
    url: <url БД>
    username: <имя пользователя БД>
    password: <пароль от БД>

app:
  admin:
    username: <имя админа>
    email: <email админа>
    password: <пароль админа>
```