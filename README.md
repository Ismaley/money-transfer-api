# Money Transfer API
A rest api to manage operations over a money account.

## Technologies used
* Kotlin 1.3
* Micronaut Framework
* H2 in-memory DB
* JPA / Hibernate

## Building and Running

To build the project run the below command in project's root folder
```
./gradlew clean build
```

To run the application run the below command in project's root folder after building the project
```
cd build/libs && java -jar money-transfer-api-0.1.jar
```

## Testing

The project's code is covered with unit and integration tests. The integration tests covers all the operations done by the api along with error cases as well.

To run the project's tests run the below command in project's root folder
```
./gradlew clean test
```

## API documentation
Application exposes following REST endpoints.

The User and Account apis provide services for managing users and accounts in the system.

### User endpoints

| Http method | Endpoint                                        | Description                                                    |
|-------------|-------------------------------------------------|----------------------------------------------------------------|
| POST        | /users                                          | Creates a new user                                             |
| GET         | /users/{userId}                                 | Retrieves the user's information                               |


### Account endpoints

| Http method | Endpoint                                        | Description                                                         |
|-------------|-------------------------------------------------|---------------------------------------------------------------------|
| POST        | /accounts                                       | Creates a new account with starting balance 0                       |
| GET         | /accounts/{accountId}?userId={userId}           | Retrieves the user's account information                            |
| POST        | /accounts/{accountId}/transfers                 | Transfers an amount of money from current account to another account|
| POST        | /accounts/{accountId}/deposits                  | Deposits an amount of money to current account                      |
| POST        | /accounts/{accountId}/withdrawals               | Withdraws an amount of money from current account                   |
| GET         | /accounts/{accountId}/transactions?userId={userId}              | Retrieves a list of transactions performed on current account       |



### POST /users
Creates a new user.
Sample request:
```json
{
  "name": "Jhon Doe",
  "documentNumber": "123.123.001-61",
  "birthDate": "1986-07-28"
}
```

Sample response:
```json
{
  "id": "6f85ffba-f375-431c-bcc0-ab444b2c97c4",
  "name": "Jhon Doe",
  "documentNumber": "123.123.001-61",
  "createdAt": "2019-07-30T01:17:25.114"
}
```

### GET /users/{userId}
Retrieves the user's information.
Sample response:
```json
{
  "id": "6f85ffba-f375-431c-bcc0-ab444b2c97c4",
  "name": "Jhon Doe",
  "documentNumber": "123.123.001-61",
  "createdAt": "2019-07-30T01:17:25.114"
}
```

### POST /accounts
creates a new account. 

* Returns 201 on success.

Sample request:
```json
{
	"userId": "6f85ffba-f375-431c-bcc0-ab444b2c97c4"
}
```

Sample success response:
```json
{
    "number": 3,
    "balance": 0,
    "createdAt": "2019-07-30T01:18:02.988"
}
```

* Returns 400 if user does not exists in system.

Sample error response:
```json
{
    "message": "user with id: 6f85ffba-f375-431c-bcc0-ab444b2c97c4 does not exist"
}
```

### GET /accounts/{accountId}?userId={userId} 
Retrieves the account's information.

Sample success response:
```json
{
    "number": 3,
    "balance": 0,
    "createdAt": "2019-07-30T01:18:02.988"
}
```

* Returns 400 if given user does not own the account.

Sample error response:
```json
{
    "message": "You do not own this account to retrieve it's information"
}
```

### POST /accounts/{accountId}/transfers
Transfers an amount of money from current account to another account.

Sample request:
```json
{
	"userId": "6f85ffba-f375-431c-bcc0-ab444b2c97c4",
	"destinationAccountId": 3,
	"amount": 60.99
}
```
Sample success response:
```json
{
    "sourceAccountNumber": 1,
    "destinationAccountNumber": 3,
    "amount": 60.99,
    "createdAt": "2019-07-30T01:18:02.988"
}
```

* Returns status code: 400 if given user does not own the account.
* Returns status code: 400 if source account does not have sufficient funds.
* Returns status code: 400 if amount to deposit is 0 or below.
* Returns status code: 404 if any of the provided account numbers do not exist.

Sample error response:
```json
{
    "message": "Insufficient funds to transfer"
}
```

### POST /accounts/{accountId}/deposits
Deposits an amount of money to current account.

Sample request:
```json
{
	"userId": "6f85ffba-f375-431c-bcc0-ab444b2c97c4",
	"amount": 130.99
}
```
Sample success response:
```json
{
    "number": 1,
    "balance": 130.99,
    "createdAt": "2019-07-30T01:17:42.497"
}
```

* Returns status code: 404 if the provided account number does not exist.
* Returns status code: 400 if amount to deposit is 0 or below.
* Returns status code: 400 if given user id does not exist.

Sample error response:
```json
{
    "message": "amount must be greater than 0"
}
```

### POST /accounts/{accountId}/withdrawals
Withdraws an amount of money from current account.

Sample request:
```json
{
	"userId": "6f85ffba-f375-431c-bcc0-ab444b2c97c4",
	"amount": 130.99
}
```
Sample success response:
```json
{
    "number": 1,
    "balance": 130.99,
    "createdAt": "2019-07-30T01:17:42.497"
}
```

* Returns status code: 404 if the provided account number does not exist.
* Returns status code: 400 if source account does not have sufficient funds.
* Returns status code: 400 if amount to withdraw is 0 or below.
* Returns status code: 400 if given user id does not exist.
* Returns status code: 400 if given user id does not own the account.

Sample error response:
```json
{
    "message": "Insufficient funds to withdraw"
}
```

### GET /users/{userId}?userId={userId} 
Retrieves a list of transactions performed on current account
Sample response:
```json
[{
  "amount": 130.99,
  "createdBy": "Jhon Doe",
  "transactionType": "DEPOSIT",
  "createdAt": "2019-07-30T01:17:25.114"
},
{
  "amount": 130.99,
  "createdBy": "Jhon Doe",
  "transactionType": "WITHDRAW",
  "createdAt": "2019-07-30T01:17:25.114"
}]
```

* Returns status code: 404 if the provided account number does not exist.
* Returns status code: 400 if given user id does not exist.
* Returns status code: 400 if given user id does not own the account.

Sample error response:
```json
{
    "message": "You do not own this account to withdraw money"
}
```


