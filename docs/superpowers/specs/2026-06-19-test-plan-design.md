# Library System E2E-Led Test Plan Design

## 1. Overview
This document outlines the testing strategy and implementation plan for the `my-library-system` project. The strategy adopts an **E2E-led approach**, heavily relying on Playwright for full-stack end-to-end testing, while supplementing with targeted backend unit tests for complex business logic.

## 2. Architecture & Tech Stack
- **Backend Unit Testing**: JUnit 5 + Mockito. Focused strictly on the `library-business` service layer.
- **Frontend / E2E Testing**: Playwright. Placed within the `library-web` directory to simulate real user interactions against a running frontend and backend.
- **Methodology**: Test-Driven Development (TDD). Tests will be written to fail first, followed by the minimal implementation required to pass.

## 3. Backend Testing Strategy (Core Logic)
To avoid testing trivial CRUD operations and Controller routing, backend tests will focus on complex business rules.

### Target: `BorrowServiceImpl`
- **Focus Area**: Borrowing rules and inventory management.
- **Test Scenarios**:
  - A user cannot borrow a book if they have overdue books.
  - A user cannot borrow a book if they have reached their maximum borrow limit.
  - Successfully borrowing a book decreases the available inventory.

## 4. E2E Testing Strategy (Playwright)
Playwright will be used to validate the entire system flow from the user's perspective.

### Setup
- Initialize Playwright in `library-web`.
- Configure `playwright.config.js` with the local dev server URL (e.g., `http://localhost:5173`).

### Core Scenario 1: User Authentication Flow
- **File**: `tests/e2e/login.spec.js`
- **Flow**:
  1. Navigate to the login page.
  2. Enter valid credentials.
  3. Click the login button.
  4. **Assertion**: Verify redirection to the Dashboard and the presence of the user's profile information.

### Core Scenario 2: Book Borrowing Flow
- **File**: `tests/e2e/borrow.spec.js`
- **Flow**:
  1. Login as a standard reader.
  2. Navigate to the "Book List" page.
  3. Search for an available book.
  4. Click the "Borrow" button.
  5. **Assertion**: Verify a success notification appears.
  6. **Assertion**: Verify the book's available stock is reduced or the record appears in "My Borrows".

## 5. Implementation Steps
1. **Environment Setup**: Run `npm init playwright@latest` in `library-web`.
2. **Backend Unit Tests**: Implement JUnit tests for `BorrowServiceImpl`.
3. **E2E Login Test**: Implement and pass `login.spec.js`.
4. **E2E Borrow Test**: Implement and pass `borrow.spec.js`.
