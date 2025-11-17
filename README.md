# Employee Expense Management System

**A secure, scalable backend solution for managing employee expense reimbursements with a multi-level approval workflow.**

![Java](https://img.shields.io/badge/Java-17-brightgreen) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green) ![Oracle DB](https://img.shields.io/badge/Oracle%20DB-19c-blue) ![JWT Auth](https://img.shields.io/badge/Auth-JWT-yellowgreen)

## Project Overview

This is a **full-featured internal tool** I developed from scratch to automate employee expense reimbursement processes. It mirrors real-world financial workflows found in enterprises and banks — perfect preparation for Core Banking and Digital Banking systems.

Key highlights:
- **Multi-stage approval workflow** with a robust state machine (DRAFT → PENDING_MANAGER → PENDING_FINANCE → APPROVED → PAID)
- **Role-Based Access Control (RBAC)** implemented with Spring Security (Employee, Manager, Finance, Admin roles)
- Clean, production-ready architecture with layered design, comprehensive error handling, and full audit trail
- Secure JWT authentication
- Fully tested with JUnit 5 and Mockito

## Core Features

| Feature                        | Description                                                                                  |
|--------------------------------|----------------------------------------------------------------------------------------------|
| Expense Submission & Editing   | Employees create, edit, and submit expense requests with receipt attachments               |
| Multi-Level Approval           | Manager review → Finance review → Mark as Paid (with rejection paths)                        |
| State Machine Workflow        | Prevents invalid transitions and guarantees data integrity                                   |
| Audit Trail                    | Every action logged with actor, timestamp, and comments                                      |
| Secure Authentication          | JWT-based login with role-based endpoint protection                                          |
| RESTful API Design             | Clear, intuitive endpoints with proper HTTP methods and status codes                        |

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.2
- **Framework**: Spring Security, Spring Data JPA
- **Database**: Oracle Database (easily adaptable to other RDBMS)
- **Testing**: JUnit 5, Mockito
- **Authentication**: JWT
- **Build Tool**: Maven

## Workflow Summary
![Basic Expense Request Flow](https://github.com/quyenptb/employee-expense-management/blob/main/app-basic-workflow.png?raw=true "Basic Expense Request Flow")

## Sequence Diagram
### Login get JWT
![Login](https://github.com/quyenptb/employee-expense-management/blob/main/src/main/resources/get-jwt-token.png?raw=true)
### Expense request submitted to manager
![Expense request submitted to manager](https://github.com/quyenptb/employee-expense-management/blob/main/src/main/resources/expense-request-submit-to-manager.png?raw=true)
### Manager approves the request
![Manager approves the request](https://github.com/quyenptb/employee-expense-management/blob/main/src/main/resources/manager-approve-expense-request.png?raw=true)
### Manager rejects the request
![Manager rejects the request](https://github.com/quyenptb/employee-expense-management/blob/main/src/main/resources/manager-reject-expense-request.png?raw=true)
### Finance approves the request
![Finance approves the request](https://github.com/quyenptb/employee-expense-management/blob/main/src/main/resources/finance-approve-expense-request.png?raw=true)
### Finace marks request as PAID
![Finace marks request as PAID](https://github.com/quyenptb/employee-expense-management/blob/main/src/main/resources/finance-mark-expense-request-as-paid.png?raw=true)

## Setup

```bash
git clone https://github.com/quyenptb/employee-expense-management.git
cd employee-expense-management
./mvnw spring-boot:run
API base URL: http://localhost:8080
Full Postman collection included in repository.
