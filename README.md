<div align="center">

# 🏦 BankOfTUC

**A full-featured banking management system built in Java**

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Last Commit](https://img.shields.io/github/last-commit/kdani3/uml-project?style=for-the-badge&logo=git&logoColor=white&color=4A90D9)](https://github.com/kdani3/uml-project/commits/main)
[![Repo Size](https://img.shields.io/github/repo-size/kdani3/uml-project?style=for-the-badge&logo=github&logoColor=white&color=6A1B9A)](https://github.com/kdani3/uml-project)
[![Stars](https://img.shields.io/github/stars/kdani3/uml-project?style=for-the-badge&logo=starship&color=FFD700)](https://github.com/kdani3/uml-project/stargazers)
[![Forks](https://img.shields.io/github/forks/kdani3/uml-project?style=for-the-badge&logo=git&color=28A745)](https://github.com/kdani3/uml-project/network/members)

---

*BankOfTuc is a comprehensive, dual-interface banking platform supporting individual customers, business accounts, and administrators — featuring international transfers, QR-based two-factor authentication, PDF statement generation, and a built-in time simulation engine.*

**This project was entirely developed as part of the UML course of the Technical University of Crete (TUC)** 
---
*nothing is tested for real world use or prod, be careful or don't we got perfect grade*
</div>

---

## Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [Usage](#-usage)
- [Project Structure](#-project-structure)
- [Running Tests](#-running-tests)

---

## ✨ Features

### 🔐 Security & Authentication
- **Two-Factor Authentication (TOTP)** via QR code scan — on-screen QR generated with ZXing, validated with a TOTP library
- **Session management** with login lock-out after repeated failed attempts
- **BCrypt-strength password hashing** via Apache Commons Codec
- Secure `FallbackAdminCreation` for first-run bootstrapping

### 👤 Account Types
| Type | Capabilities |
|------|-------------|
| **Individual Customer** | Personal accounts, SEPA/SWIFT transfers, bill payments, recurring payments |
| **Company Customer** | Business accounts, invoice issuance, bulk payment management |
| **Administrator** | Full customer oversight, transfer/payment auditing, account control |

### 💸 Banking Operations
- **SEPA Transfers** — single-euro-payments-area standard, gateway + processor pattern
- **SWIFT Transfers** — international wire transfers with correspondent bank routing
- **Inter-Bank Transfers** — internal routing between accounts within the system
- **Self-Transfers** — move funds between your own accounts instantly
- **Bill Payments** — issue, track, and pay bills stored in CSV
- **Recurring Payments** — schedule automatic payments on a cron-like cycle via `RecurringPaymentScheduler`

### 📄 Reporting & Export
- **PDF Statement Generator** — full transaction history exported as a styled PDF using Flying Saucer + iText, rendered with a bundled `DejaVuSans` font for Unicode support
- **CSV Audit Logs** — persistent `payments.csv` and `transfers.csv` logs for every transaction
- **IBAN Utilities** — validation and generation helpers compliant with the IBAN standard

### 🖥️ Dual Interface
- **CLI** — rich command-line interface with dedicated sub-menus for transfers, payments, billing, history, and admin operations
- **GUI** — full Swing-based graphical interface (MigLayout) with separate dashboards for customers, companies, and admins; includes a **Time Simulation Panel** for advancing the internal clock to trigger scheduled events

### ⏱️ Time Simulation Engine
BankOfTuc ships with a `SimulatedClock` + `TimeService` that lets you fast-forward the internal system date. This is particularly useful for:
- Testing recurring payment triggers
- Simulating month-end statements
- Verifying interest and fee accrual logic

---

## 🏗️ Architecture

BankOfTuc is organized into clearly separated modules, following a UML-designed class hierarchy:

```
BankOfTuc/
├── Accounting/       — BankAccount, IBAN utilities, factory
├── Auth/             — Login, session, TOTP/QR, password hashing
├── Bookkeeping/      — JSON/CSV serialization, file persistence (Gson + Jackson)
├── CLI/              — Full command-line interface
├── FileIO/           — Email utilities, PDF generation, .env reader
├── GUI/              — Swing dashboards, login/register frames, time simulation
├── Logging/          — Payment and transfer audit loggers
├── Payments/         — Bills, recurring payments, scheduling
├── Services/         — SEPA/SWIFT service layer, simulated clock
├── Transfers/        — Transfer types, gateways, processors
└── testing/          — Test runner with JSON-driven test cases
```

### Class Hierarchy

```
User
 ├── Admin
 └── Customer
      ├── IndividualCustomer
      └── CompanyCustomer

Transfer
 ├── SepaTransfer
 ├── swiftTransfer
 ├── InterBank
 └── SelfTransfer

TransferGateway
 ├── SepaTransferGateway
 └── (Swift routing)
```

---

## 🛠️ Tech Stack

| Library | Version | Purpose |
|---------|---------|---------|
| **ZXing** | 3.5.4 | QR code generation for 2FA |
| [**samdjstevens/java-TOTP**](https://github.com/samdjstevens/java-totp) | 1.7.1 | Time-based one-time password validation |
| **Gson** | 2.10.1 | JSON serialization / deserialization |
| **Jackson** | 2.20.0 | Advanced JSON data binding |
| **Flying Saucer** | 9.1.22 | HTML→PDF rendering engine |
| **iText** | 2.1.7 | PDF document generation |
| **MigLayout** | 3.7.4 | Swing GUI layout manager |
| **Commons Codec** | 1.19.0 | Password hashing utilities |

---

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- All `.jar` dependencies are included in the `libs/` directory — no build tool required

### Clone the repository

```bash
git clone https://github.com/kdani3/uml-project.git
cd uml-project
```

### Compile

```bash
javac -cp "libs/*" -d out $(find BankOfTuc -name "*.java")
```

### Run — GUI mode

```bash
java -cp "out:libs/*" CLI.GuiMain
```

### Run — CLI mode

```bash
java -cp "out:libs/*" CLI.Main
```

> **Note:** A `.env` file is expected in the project root for email configuration. Copy `.env.example` (if provided) and fill in your SMTP credentials.

---

## 💻 Usage

### First Run

On first launch, `FallbackAdminCreation` will bootstrap a default admin account if no users exist. Log in with the generated credentials and use the **Admin Dashboard** to create customer accounts.

### GUI Walkthrough

| Screen | Description |
|--------|-------------|
| **Login / Register** | Authenticate or create a new account with TOTP setup |
| **Customer Dashboard** | View accounts, initiate transfers, pay bills, view history |
| **Company Dashboard** | Business account management and invoice issuance |
| **Admin Panel** | Manage all customers, view all payments and transfers |
| **Time Simulation Panel** | Advance the system clock to trigger recurring events |

### CLI Commands (examples)

```
> transfer sepa        — Initiate a SEPA transfer
> transfer swift       — Initiate a SWIFT wire transfer
> bill pay             — Pay an outstanding bill
> history view         — View paginated transaction history
> admin customers      — (Admin) List and manage all customers
```

---

## 📁 Project Structure

```
uml-project/
├── BankOfTuc/         — All Java source files (see Architecture above)
├── data/
│   ├── customers.json
│   ├── users.json
│   ├── bills.csv
│   ├── recurring_payments.csv
│   ├── sessions.json
│   └── login_locks.json
├── logs/
│   ├── payments.csv   — Full payment audit log
│   └── transfers.csv  — Full transfer audit log
├── libs/              — Bundled JAR dependencies
└── resources/
    └── DejaVuSans.ttf — Unicode font for PDF generation
```

---

## 🧪 Running Tests

The project includes a comprehensive `TestRunner` driven by a JSON test-case file:

```bash
java -cp "out:libs/*" testing.TestRunner
```

Test input data lives in `BankOfTuc/testing/test_users_input.json`.

---

## 🙏 Special Thanks

Once again to [samdjstevens](https://github.com/samdjstevens/java-totp) for exhibiting the most noble kindness as to provide the community with a java totp library.

Thanks to **George Karelias and Sons™** for blessing the human species with the blue packaged tobacco.

<div align="center">

Made with 🚬 and Java · [kdani3](https://github.com/kdani3)

</div>
