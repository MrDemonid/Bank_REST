## Описание.

Программа в целом соответствует ТЗ, как я его понял.

Помимо шифрования паролей пользователей, отдельно шифруются номера банковских карт (AES/GCM/NoPadding),
с добавлением случайных данных к номерам, для усиления стойкости.
Поскольку один и тот же номер после шифрования будет разной последовательностью байт, то для поиска карты
по её номеру в сущность введено поле хэша номера карты. Хэш получаем по HmacSHA256. Ключи для шифрования
задаются в `application.yml` (`var.card.hmac.secret` и `var.card.aes.secret`), но в проде конечно лучше использовать
специализированные сервисы хранения чувствительных данных.

`API` разделены строго по ролям `USER` или `ADMIN`. Поэтому, если пользователь является админом, то ему так же
необходимо дать право `USER`, для доступа к сервисам `USER`. Сделано для удобства, можно временно лишать
кого-то части прав, без заморочек в коде. Например:
```http request
POST http://localhost:8080/api/users/create
Authorization: Bearer <тут токен>
Content-Type: application/json

{
  "userName": "Ivan",
  "password": "test-password",
  "email": "ivan@rambler.ru",
  "enabled": true,
  "roles": ["ADMIN","USER"]
}
```

#### CORS.
Настроен для http://localhost:9000 и https://example.bank.com, просто в демонстрационных целях. 
Для `Swagger` не настроен, поэтому он будет работать только с  адресов без `cross-origin` запросов, 
т.е. с того же адреса, с которого запущен сервер. 

В комментариях swaggerSecurity() закомментирован фильтр для
разрешения доступа с любых адресов в dev-режиме.


## Запуск.

По умолчанию включен режим разработчика (`dev`). Если он не нужен, то отключаем
его в `application.yml`, удалив активный профиль:
```text
spring.application.profiles.active: dev
```
Далее, настраиваем переменные окружения для базы данных в `initialize/.env.mysql` и создаем контейнер:
```shell
docker-compose up -d
```
Потом, по необходимости задаём переменные окружения:
```text
BANK_CARDS_PORT - порт приложения (по дефолту 8080).
OAUTH2_ISSUER_URI - полный адрес сервера аутентификации и авторизации (по дефолту http://localhost:8080).
CLIENT_ID - Идентификатор клиента, зарегистрированного на сервере аутентификации.
CLIENT_SECRET - Его секрет.
MYSQL_USER - имя пользователя для доступа программы к БД (по дефолту admin).
MYSQL_PASSWORD - его пароль (по дефолту admin).
```
Для прода настраиваем пути разрешенного редиректа в процессе аутентификации:
```text
spring.security.oauth2.auth-clients.default.redirect-uris 
```

В `application-dev.yml` настройки для режима разработчика. В них включён клиент для сервера аутентификации, позволяющий
легко авторизоваться через `CLIENT_CREDENTIALS`. Также в эти настройки вынесен `Swagger`, поскольку для работы
предоставленных им сервисов необходима авторизация с правами `ADMIN`, а это проще всего сделать в режиме `dev`. К тому же
в ТЗ не нашел упоминания, должен ли `Swagger` работать в проде, или только в `dev`. Нас учили, что его лучше закрывать для прода,
я так и сделал.

Теперь можно запускать приложение.


## Тестирование API.


### Тест CORS.

Просто шлем preflight-запрос на разрешенные в CorsConfigurationSource адреса
(http://localhost:9000 и https://example.bank.com). Например:
```shell
curl -i -X OPTIONS http://localhost:8080/api/users \
  -H "Origin: http://localhost:9000" \
  -H "Access-Control-Request-Method: GET"
```
Должен вернуться ответ, вида:
```shell
Access-Control-Allow-Origin: http://localhost:9000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Authorization, Cache-Control, Content-Type
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```


### Unit-тесты.

Тесты есть для сервисного слоя, слоя контроллеров и для маппера карт. Всего 93 теста. Запускаем стандартно:
```shell
 mvn clean test
```

### Интеграционные тесты.

Не было в ТЗ, добавил ради эксперимента Testcontainers. Тесты находятся в папке `src/test/java/com/example/bankcards/integrations`.

Подключается Docker, Ryuk, создается контейнер MySql, Liquibase прогоняет все миграции. 
После этого стартует тестовый контекст Spring Boot. Тесты показали, что сервисный слой в порядке.

### Тесты через Http-клиента IDEA.

Удобны тем, что позволяют быстро и легко проверять API по мере их написания. Файлы запросов находятся в папке ./tools. 
IDEA поддерживает автоматическое получение Jwt-Токенов, в соответствии с данными из `http-client.env.json`. Поэтому,
остается только открыть http-файл, в меню `Run with` выставить профиль `dev` и запустить его.

Например, чтобы создать пользователя, открываем `tools/user_controller/create_user.http`, корректируем параметры запроса
и выполняем:
```http request
### @env=dev
# Добавление нового пользователя.
POST {{host}}/api/users/create
Authorization: Bearer {{$auth.token("local-oauth")}}
Content-Type: application/json

{
  "userName": "Andrey",
  "password": "test-password",
  "email": "ndrey7@rambler.ru",
  "enabled": true,
  "roles": ["ADMIN","USER"]
}
```
```http request
HTTP/1.1 200

{
  "userId": "812ec722-9ade-4d65-abf5-d97caf65ef31",
  "userName": "Andrey",
  "email": "ndrey7@rambler.ru",
  "enabled": true,
  "roles": [
    "ROLE_USER",
    "ROLE_ADMIN"
  ]
}
```


### Swagger.

Поскольку всё API использует роли, то необходимо авторизоваться, через кнопку Authorize, где
вводим ClientID и Secret (из `application-dev.yml`). После этого можно выполнять запросы
по всем API.

Не так удобно, как http-запросы IDEA, но в целом наглядно и работает.

### Curl

Описывать нет смысла, разве что получение Jwt-токена для запросов:
```shell
curl -X POST http://localhost:8080/oauth2/token \
-H "Authorization: Basic $(echo -n 'apmid:secret' | base64)" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "grant_type=client_credentials&scope=read"
```
