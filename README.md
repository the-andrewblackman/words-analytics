**API Endpoints**

-------------------
**Admin Endpoints**
-------------------

**Get All Accounts:**

Method: GET

URL: http://localhost:8082/api/bank/accounts

**Get All Checking Accounts:**

Method: GET

URL: http://localhost:8082/api/bank/checking

**Get All Savings Accounts:**

Method: GET

URL: http://localhost:8082/api/bank/savings

**Get All Transactions:**

Method: GET

URL: http://localhost:8082/api/bank/txn/all

**Get Transactions by Account ID & Checking ID:**

Method: GET

URL: http://localhost:8082/api/bank/txn/checking/{account ID}/{checking ID}

**Get Transactions by Account ID ID & Savings ID:**

Method: GET

URL: http://localhost:8082/api/bank/txn/savings/{account ID}/{savings ID}

-------------------
**User Endpoints**
-------------------

**Get Checking Accounts by Account ID:**

Method: GET

URL: http://localhost:8082/api/bank/checking/{ID}

**Get Savings Accounts by Account ID:**

Method: GET

URL: http://localhost:8082/api/bank/savings/{ID}

**Get Transactions by Checking ID:**

Method: GET

URL: http://localhost:8082/api/bank/txn/checking/{ID}

**Get Transactions by Savings ID:**

Method: GET

URL: http://localhost:8082/api/bank/txn/savings/{ID}

-------------------
**Create Endpoints**
-------------------

**Create User Account:**

Method: POST

URL: http://localhost:8082/api/bank/accounts/create

Body: { "name": "name of user" }

**Create Checking Account:**

Method: POST

URL: http://localhost:8082/api/bank/checking/create

Body: { "accountName": "checking account name", "userName": "name of user" }

**Create Savings Account:**

Method: POST

URL: http://localhost:8082/api/bank/savings/create

Body: { "accountName": "savings account name", "userName": "name of user" }

-------------------
**Delete Endpoints**
-------------------

**Delete Checking Account by ID:**

Method: DELETE

URL: http://localhost:8082/api/bank/checking/delete?id={checking account ID}

**Delete Savings Account by ID:**

Method: DELETE

URL: http://localhost:8082/api/bank/savings/delete?id={savings account ID}
