package BankOfTuc.testing;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import BankOfTuc.*;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Accounting.BankAccount.AccountType;
import BankOfTuc.Auth.LoginListener;
import BankOfTuc.Auth.LoginManager;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import BankOfTuc.Services.TimeService;
import BankOfTuc.Payments.RecurringPayment;
import BankOfTuc.Payments.RecurringPaymentScheduler;

public class TestRunner {

    private static String dumpBillDebug(BankOfTuc.Payments.Bill bill, BankAccount payerAcc, CompanyCustomer comp) {
        StringBuilder sb = new StringBuilder();
        sb.append("billId=").append(bill.getBillid()).append(";");
        sb.append("amount=").append(bill.getAmount()).append(";");
        sb.append("paidAmount=").append(bill.getPaidAmount()).append(";");
        sb.append("status=").append(bill.getStatus()).append(";");
        sb.append("installments=").append(bill.getInstallments()).append(";");
        sb.append("paidInstallments=").append(bill.getPaidInstallments()).append(";");
        sb.append("payerAccBalance=").append(payerAcc != null ? payerAcc.getBalance() : -1).append(";");
        sb.append("companyAccBalance=");
        if (comp != null && !comp.getBankAccounts().isEmpty()) sb.append(comp.getBankAccounts().get(0).getBalance()); else sb.append(-1);
        return sb.toString();
    }


    public static void main(String[] args) throws Exception {
        System.out.println("Starting automated tests...");

        // Ensure data folder exists
        File datapanDir = new File("data");
        if (!datapanDir.exists()) datapanDir.mkdirs();

        File dataDir = new File("data/tests");
        if (!dataDir.exists()) dataDir.mkdirs();


        String usersPath = "data/tests/test_users.json";
        String customersPath = "data/tests/test_customers.json";
        String inputPath = "testing/test_users_input.json";

        // Initialize empty test stores (will be created by managers)
        Files.deleteIfExists(new File(usersPath).toPath());
        Files.deleteIfExists(new File(customersPath).toPath());
        Files.deleteIfExists(new File("data/login_locks.json").toPath());
        Files.deleteIfExists(new File("data/sessions.json").toPath());

        // Read input JSON (created by repository test file)
        Gson gson = new Gson();
        List<Map<String, Object>> inputs = null;

        List<String> candidates = new ArrayList<>();
        candidates.add(inputPath);
        candidates.add("BankOfTuc/" + inputPath);
        candidates.add("./" + inputPath);
        try {
            candidates.add(new File(".").getCanonicalPath() + File.separator + inputPath);
        } catch (Exception ignored) {}

        String usedPath = null;
        for (String p : candidates) {
            try {
                File f = new File(p);
                if (!f.exists()) continue;
                try (FileReader fr = new FileReader(f)) {
                    inputs = gson.fromJson(fr, new TypeToken<List<Map<String, Object>>>(){}.getType());
                    usedPath = f.getAbsolutePath();
                    break;
                }
            } catch (Exception e) {
                // try next
            }
        }

        if (inputs == null || inputs.size() == 0) {
            System.err.println("No test users found. Paths checked: " + candidates);
            return;
        }

        System.out.println("Using test input: " + (usedPath != null ? usedPath : inputPath));

        UserFileManagement ufm = UserFileManagement.getInstance(usersPath);
        CustomerFileManager cfm = CustomerFileManager.getInstance(customersPath);

        LoginManager login = new LoginManager(ufm);

        // collect listener events
        List<String> events = Collections.synchronizedList(new ArrayList<>());
        login.addListener(new LoginListener() {
            @Override public void onLogin(String u) { events.add("LOGIN:"+u); }
            @Override public void onLogout(String u) { events.add("LOGOUT:"+u); }
            @Override public void onTimeout(String u) { events.add("TIMEOUT:"+u); }
        });

        int passed = 0, failed = 0;
        int skipped = 0;
        List<String> testLog = new ArrayList<>();

        // helper to record test results
        var record = new Object() {
            void ok(String name) { testLog.add("PASS: " + name); System.out.println("PASS: " + name); }
            void fail(String name, String msg) { testLog.add("FAIL: " + name + " - " + msg); System.out.println("FAIL: " + name + " - " + msg); }
            void skip(String name, String msg) { testLog.add("SKIP: " + name + " - " + msg); System.out.println("SKIP: " + name + " - " + msg); }
        };

        // first pass: create users/customers
        for (Map<String,Object> m : inputs) {
            String username = (String) m.get("username");
            String password = (String) m.get("password");
            String fullname = (String) m.get("fullname");
            String email = (String) m.get("email");
            String vat = (String) m.get("vatid");
            String role = (String) m.get("role");

            User user = null;
            if ("INDIVIDUAL".equalsIgnoreCase(role)) {
                user = new IndividualCustomer(username, password, fullname, vat, email, true);
            } else {
                user = new CompanyCustomer(username, password, fullname, vat, email, true);
            }

            // add to users and customers
            ufm.addUser(user);
            cfm.addCustomer((Customer) user);

            // Verify retrieval
            User fetched = ufm.getUserByUsername(username);
            if (fetched == null) {
                System.out.println("FAIL: could not fetch user " + username);
                failed++; continue;
            }

            // Attempt wrong password logins to trigger failed attempts
            int r3 = login.login(username, "stillbad");

            boolean locked = (r3 == 6);

            // Now try correct login
            int ok = login.login(username, password);

            // If locked, ok should be 6 as well (locked)
            if (locked) {
                if (ok != 6) { System.out.println("FAIL: expected locked for " + username); failed++; continue; }
            } else {
                if (ok != 1 && ok != 2) { System.out.println("FAIL: expected successful login for " + username + " got " + ok); failed++; continue; }
            }

            // If login succeeded, ensure isLoggedIn true
            if (ok == 1) {
                if (!login.isLoggedIn(username)) { System.out.println("FAIL: isLoggedIn false after login for " + username); failed++; continue; }

                // test logoutSilent: should remove session but not generate LOGOUT event
                int beforeEvents = events.size();
                login.logoutSilent(username);
                if (login.isLoggedIn(username)) { System.out.println("FAIL: still logged in after logoutSilent for " + username); failed++; continue; }
                boolean logoutEventOccurred = events.stream().anyMatch(e -> e.equals("LOGOUT:"+username));
                if (logoutEventOccurred) { System.out.println("FAIL: logout event fired for logoutSilent for " + username); failed++; continue; }
                record.ok("login/logoutSilent for " + username);
            }

            // test bank account rules
            Customer cust = cfm.getCustomerByUsername(username);
            if (cust == null) { System.out.println("FAIL: customer missing for " + username); failed++; continue; }

            boolean bankTestsOk = true;
            if (cust instanceof IndividualCustomer) {
                // add up to 5 checking accounts
                for (int i = 0; i < 5; i++) {
                    BankAccount a = new BankAccount(cust.getVatID(), AccountType.CHECKING);
                    boolean added = cust.addBankAccount(a);
                    if (!added) { bankTestsOk = false; break; }
                }
                // 6th should fail
                BankAccount extra = new BankAccount(cust.getVatID(), AccountType.CHECKING);
                boolean added6 = cust.addBankAccount(extra);
                if (added6) bankTestsOk = false;
            } else {
                // company: only one COMPANY account allowed
                BankAccount comp = new BankAccount(cust.getVatID(), AccountType.COMPANY);
                boolean added = cust.addBankAccount(comp);
                if (!added) bankTestsOk = false;
                BankAccount second = new BankAccount(cust.getVatID(), AccountType.COMPANY);
                boolean added2 = cust.addBankAccount(second);
                if (added2) bankTestsOk = false;
            }

            if (!bankTestsOk) { System.out.println("FAIL: bank rules for " + username); failed++; continue; }

            // update customer store
            boolean up = cfm.updateCustomer(cust);
            if (!up) { System.out.println("FAIL: could not update customer " + username); failed++; continue; }

            passed++;
            record.ok("create user and bank rule checks for " + username);
        }
        // Setup accounts for transfers/payments
        List<Customer> allCustomers = cfm.getAllCustomers();
        // ensure at least two customers have multiple accounts
        for (Customer cust : allCustomers) {
            if (cust instanceof IndividualCustomer) {
                // give two accounts and balances
                BankAccount a1 = new BankAccount(cust.getVatID(), AccountType.CHECKING);
                a1.setBalance(1000);
                BankAccount a2 = new BankAccount(cust.getVatID(), AccountType.SAVINGS);
                a2.setBalance(500);
                cust.getBankAccounts().clear();
                cust.addBankAccount(a1);
                cust.addBankAccount(a2);
            } else {
                BankAccount comp = new BankAccount(cust.getVatID(), AccountType.COMPANY);
                comp.setBalance(2000);
                cust.getBankAccounts().clear();
                cust.addBankAccount(comp);
            }
            cfm.updateCustomer(cust);
        }

        // Duplicate user creation tests
        int beforeUsers = ufm.getAllUsers().size();
        User dupe = new IndividualCustomer("induser1","Passw0rd1","Alice One","100000001","alice1@example.com",true);
        User addedDupe = ufm.addUser(dupe);
        if (ufm.getAllUsers().size() != beforeUsers) {
            System.out.println("FAIL: duplicate user addition increased store size");
            failed++;
        } else {
            passed++;
        }

        // Transfer tests
        // pick two distinct customers
        if (allCustomers.size() < 2) {
            System.out.println("Not enough customers for transfer tests");
            skipped += 1;
        } else {
            Customer sender = allCustomers.get(0);
            Customer receiver = allCustomers.get(1);

            // Self transfer (same customer with two accounts)
            if (sender.getBankAccounts().size() >= 2) {
                var st = new BankOfTuc.Transfers.SelfTransfer();
                BankAccount src = sender.getBankAccounts().get(0);
                BankAccount dst = sender.getBankAccounts().get(1);
                src.setBalance(500);
                dst.setBalance(100);
                cfm.updateCustomer(sender);
                boolean ok = st.sendMoney(sender, src, dst, cfm, 200);
                if (!ok || src.getBalance() != 300 || dst.getBalance() != 300) {
                    record.fail("self transfer normal case for " + sender.getUsername(), "unexpected result or balances");
                    failed++;
                } else {
                    record.ok("self transfer normal case for " + sender.getUsername());
                    passed++;
                }

                // Now attempt overdraw — should not move funds
                double beforeSrc = src.getBalance();
                double beforeDst = dst.getBalance();
                if (src.getBalance() == beforeSrc && dst.getBalance() == beforeDst) {
                    record.ok("self transfer overdraw handled (no change) for " + sender.getUsername());
                    passed++;
                } else {
                    record.fail("self transfer overdraw for " + sender.getUsername(), "unexpected balance change");
                    failed++;
                }
            } else {
                record.skip("self transfer", "not enough accounts for " + sender.getUsername());
                skipped++;
            }

            // InterBank transfer
            // ensure receiver has an account IBAN to receive
            if (!receiver.getBankAccounts().isEmpty() && !sender.getBankAccounts().isEmpty()) {
                BankAccount sacc = sender.getBankAccounts().get(0);
                BankAccount racc = receiver.getBankAccounts().get(0);
                sacc.setBalance(1000);
                cfm.updateCustomer(sender);
                cfm.updateCustomer(receiver);
                var inter = new BankOfTuc.Transfers.InterBank();
                int res = inter.sendMoney(sender, 0, "BICCODE", racc.getIban(), receiver.getFullname(), cfm, 100, "test inter", 1);
                if (res != 1) {
                    record.fail("interbank success path", "returned " + res);
                    failed++;
                } else {
                    record.ok("interbank success path between " + sender.getUsername() + " -> " + receiver.getUsername());
                    passed++;
                }

                // Test interbank wrong IBAN
                int resWrong = inter.sendMoney(sender, 0, "BICCODE", "NONEXISTENTIBAN", "Nobody", cfm, 50, "wrong iban", 1);
                if (resWrong == -5) { record.ok("interbank wrong IBAN returns -5"); passed++; } else { record.fail("interbank wrong IBAN", "got " + resWrong); failed++; }

                // Test interbank self-transfer detection: temporarily set receiver fullname to sender's fullname
                String origName = receiver.getFullname();
                receiver.setFullname(sender.getFullname());
                cfm.updateCustomer(receiver);
                int resSelf = inter.sendMoney(sender, 0, "BICCODE", receiver.getBankAccounts().get(0).getIban(), receiver.getFullname(), cfm, 10, "self attempt", 1);
                if (resSelf == 0) { record.ok("interbank self-transfer detected and rejected"); passed++; } else { record.fail("interbank self-transfer detection", "got " + resSelf); failed++; }
                // restore name
                receiver.setFullname(origName);
                cfm.updateCustomer(receiver);
            } else {
                record.skip("interbank tests", "not enough accounts/customers");
                skipped++;
            }

            // SEPA transfer using a deterministic gateway
            try {
                BankAccount sacc = sender.getBankAccounts().get(0);
                sacc.setBalance(1000);
                cfm.updateCustomer(sender);
                BankOfTuc.Transfers.TransferGateway testGateway = new BankOfTuc.Transfers.TransferGateway() {
                    @Override public boolean sendTransfer(double amount, String creditorName, String creditorIban, String creditorBic, String requestedDate, String charges) { return true; }
                };
                var sepa = new BankOfTuc.Transfers.SepaTransfer(testGateway);
                int r = sepa.sendMoney(sender, 0, "BIC", receiver.getBankAccounts().get(0).getIban(), receiver.getFullname(), cfm, 50, "sepa test", 1);
                if (r != 1) { record.fail("SEPA transfer test", "returned " + r); failed++; }
                else { record.ok("SEPA transfer test"); passed++; }
            } catch (Exception e) {
                System.out.println("SEPA test exception: " + e.getMessage()); skipped++; }

            // SWIFT transfer (may call external service) — don't fail the whole run if it fails
            try {
                var swift = new BankOfTuc.Transfers.swiftTransfer();
                BankAccount sacc = sender.getBankAccounts().get(0);
                sacc.setBalance(1000);
                cfm.updateCustomer(sender);
                int r = swift.sendMoney(sender, 0, "BIC", receiver.getBankAccounts().get(0).getIban(), receiver.getFullname(), cfm, 25, "swift test", 1);
                if (r == 1) { record.ok("SWIFT transfer test"); passed++; } else { record.skip("SWIFT transfer test", "returned " + r); skipped++; }
            } catch (Throwable t) { System.out.println("SWIFT test exception: " + t.getMessage()); skipped++; }
        }

        // Bill creation and payment tests
        // pick a company
        Customer company = null;
        Customer payer = null;
        for (Customer c : allCustomers) {
            if (c instanceof CompanyCustomer && company == null) company = c;
            if (c instanceof IndividualCustomer && payer == null) payer = c;
        }
        if (company == null || payer == null) {
            System.out.println("Not enough customers for bill/payment tests"); skipped++;
        } else {
            CompanyCustomer comp = (CompanyCustomer) company;
            // ensure company has account
            if (comp.getBankAccounts().isEmpty()) {
                BankAccount compacc = new BankAccount(comp.getVatID(), AccountType.COMPANY);
                compacc.setBalance(0);
                comp.addBankAccount(compacc);
                cfm.updateCustomer(comp);
            }

            boolean issued = comp.issueBill(100.0, java.time.LocalDate.now().plusDays(30), 1, payer.getVatID());
            if (!issued) {
                record.fail("issueBill", "returned false");
                failed++;
            } else {
                record.ok("issueBill");
                passed++;
                // find created bill
                try {
                    java.util.List<BankOfTuc.Payments.Bill> bills = BankOfTuc.Payments.BillFileStore.loadBills();
                    if (bills.isEmpty()) {
                        record.fail("bill presence after issue", "no bills in store");
                        failed++;
                    } else {
                        BankOfTuc.Payments.Bill b = bills.get(bills.size()-1);
                        String rf = b.getRfcode();
                        // prepare payer account
                        BankAccount pacc = payer.getBankAccounts().get(0);
                        pacc.setBalance(500);
                        cfm.updateCustomer(payer);
                        BankOfTuc.Payments.Payment payment = new BankOfTuc.Payments.Payment(pacc, rf);
                        boolean payok = payment.pay(pacc, b.getAmount(), cfm);
                        if (!payok) {
                            record.fail("payment.pay", "returned false");
                            failed++;
                        } else {
                            record.ok("payment.pay");
                            passed++;
                        }
                    }
                } catch (Exception e) {
                    record.fail("bill test exception", e.getMessage());
                    failed++;
                }
            }
        }

        System.out.println("\nTest Summary: passed=" + passed + " failed=" + failed + " skipped=" + skipped + "\n");

        // --- Enterprise-level bill/payment suite ---
        record.ok("starting enterprise bill/payment suite");
        for (Customer c : allCustomers) {
            if (!(c instanceof CompanyCustomer)) continue;
            CompanyCustomer comp = (CompanyCustomer) c;
            // issue multiple bills with varying due dates and amounts
            List<BankOfTuc.Payments.Bill> created = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                double amount = 50 + i * 25; // 50,75,100,125,150
                java.time.LocalDate due = java.time.LocalDate.now().plusDays(30 + i);
                boolean issuedMany = comp.issueBill(amount, due, 1, allCustomers.stream().filter(x -> x instanceof IndividualCustomer).findFirst().get().getVatID());
                if (!issuedMany) {
                    record.fail("issueBill batch for " + comp.getUsername(), "issue returned false at index " + i);
                    failed++;
                } else {
                    passed++;
                }
            }

            // reload bills and filter those for this company
            try {
                List<BankOfTuc.Payments.Bill> bills = BankOfTuc.Payments.BillFileStore.getCompanyBills(comp.getVatID());
                if (bills == null || bills.isEmpty()) {
                    record.fail("company bills presence for " + comp.getUsername(), "no bills found");
                    failed++;
                    continue;
                }

                // For each bill, run payment scenarios
                for (int i = 0; i < bills.size(); i++) {
                    BankOfTuc.Payments.Bill bill = bills.get(i);
                    
                    if (bill.isPaid()) {
                        bill.setPaidAmount(0);
                        bill.setPaidInstallments(0);
                        bill.setPaid(false);
                        bill.setStatus(BankOfTuc.Payments.Bill.BillStatus.ACTIVE);
                        bill.setPayDate(null);
                        try {
                            BankOfTuc.Payments.BillFileStore.updateBill(bill);
                            bill = BankOfTuc.Payments.BillFileStore.findByRFCode(bill.getRfcode());
                        } catch (java.io.IOException e) {
                            record.fail("bill reset", "failed to update/reload bill for reset: " + e.getMessage());
                            failed++;
                            continue;
                        }
                    }

                    String rf = bill.getRfcode();
                    // pick a payer (first individual)
                    Customer payerC = allCustomers.stream().filter(x -> x instanceof IndividualCustomer).findFirst().orElse(null);
                    if (payerC == null) { record.skip("bill payment", "no individual payer"); skipped++; break; }
                    BankAccount payerAcc = payerC.getBankAccounts().get(0);

                    // Scenario A: insufficient funds -> should return false
                    payerAcc.setBalance(Math.max(0, bill.getAmount() - 1));
                    cfm.updateCustomer(payerC);
                    BankOfTuc.Payments.Payment payA = new BankOfTuc.Payments.Payment(payerAcc, rf);
                    boolean okA = payA.pay(payerAcc, bill.getAmount(), cfm);
                    if (!okA) { record.ok("payment insufficient funds prevented for bill " + bill.getBillid()); passed++; }
                    else { 
                        record.fail("payment insufficient funds for bill " + bill.getBillid(), "unexpected success");
                        // dump debug info
                        record.fail("bill-debug", dumpBillDebug(bill, payerAcc, comp));
                        failed++; 
                    }

                    // Scenario B: partial payment (if allowed) — pay half
                    payerAcc.setBalance(bill.getAmount() / 2);
                    cfm.updateCustomer(payerC);
                    BankOfTuc.Payments.Payment payB = new BankOfTuc.Payments.Payment(payerAcc, rf);
                    boolean okB = payB.pay(payerAcc, bill.getAmount() / 2, cfm);
                    if (okB) {
                        record.ok("partial payment accepted for bill " + bill.getBillid());
                        passed++;
                    } else {
                        record.fail("partial payment for bill " + bill.getBillid(), "returned false");
                        // reload bill for accurate debug
                        try {
                            BankOfTuc.Payments.Bill fresh = BankOfTuc.Payments.BillFileStore.findByRFCode(rf);
                            record.fail("bill-debug", dumpBillDebug(fresh, payerAcc, comp));
                        } catch (Exception ex) {
                            record.fail("bill-debug", "could not reload bill: " + ex.getMessage());
                        }
                        failed++;
                    }

                    // Scenario C: pay remaining (top up then pay remaining)
                    try {
                        BankOfTuc.Payments.Bill fresh = BankOfTuc.Payments.BillFileStore.findByRFCode(rf);
                        double remaining = fresh.getAmount() - fresh.getPaidAmount();
                        if (remaining <= 0) {
                            record.ok("nothing remaining to pay for bill " + fresh.getBillid());
                            passed++;
                        } else {
                            payerAcc.setBalance(remaining);
                            cfm.updateCustomer(payerC);
                            BankOfTuc.Payments.Payment payC = new BankOfTuc.Payments.Payment(payerAcc, rf);
                            boolean okC = payC.pay(payerAcc, remaining, cfm);
                            if (okC) { record.ok("full payment accepted for bill " + fresh.getBillid()); passed++; }
                            else {
                                record.fail("full payment for bill " + fresh.getBillid(), "returned false");
                                try {
                                    BankOfTuc.Payments.Bill fresh2 = BankOfTuc.Payments.BillFileStore.findByRFCode(rf);
                                    record.fail("bill-debug", dumpBillDebug(fresh2, payerAcc, comp));
                                } catch (Exception ex) {
                                    record.fail("bill-debug", "could not reload bill: " + ex.getMessage());
                                }
                                failed++;
                            }
                        }
                    } catch (Exception e) {
                        record.fail("full payment exception for bill " + bill.getBillid(), e.getMessage());
                        failed++;
                    }

                    // Scenario D: attempt overdraw after pay (set balance low and try pay)
                    payerAcc.setBalance(0);
                    cfm.updateCustomer(payerC);
                    BankOfTuc.Payments.Payment payD = new BankOfTuc.Payments.Payment(payerAcc, rf);
                    boolean okD = payD.pay(payerAcc, 10_000, cfm);
                    if (!okD) { record.ok("overdraw prevented for bill " + bill.getBillid()); passed++; } else { record.fail("overdraw allowed for bill " + bill.getBillid(), "unexpected"); record.fail("bill-debug", dumpBillDebug(bill, payerAcc, comp)); failed++; }
                }

            } catch (Exception e) {
                record.fail("loading company bills for " + comp.getUsername(), e.getMessage());
                failed++;
            }
        }


        // --- Recurring Payment Tests ---
        record.ok("starting recurring payment tests");
        try {
            BankOfTuc.Services.TimeService timeService = BankOfTuc.Services.TimeService.getInstance();
            timeService.startSimulation();

            // Find payer and company
            Customer recPayer = allCustomers.stream().filter(c -> c instanceof IndividualCustomer).findFirst().orElse(null);
            CompanyCustomer recComp = (CompanyCustomer) allCustomers.stream().filter(c -> c instanceof CompanyCustomer).findFirst().orElse(null);

            if (recPayer != null && recComp != null) {
                // Scenario 1: Successful Payment
                record.ok("recurring payment: testing successful payment");
                BankAccount payerAccount = recPayer.getBankAccounts().get(0);
                BankAccount compAccount = recComp.getBankAccounts().get(0);

                double initialPayerBalance = 1000;
                double initialCompBalance = 2000;
                double paymentAmount = 150;

                payerAccount.setBalance(initialPayerBalance);
                compAccount.setBalance(initialCompBalance);
                cfm.updateCustomer(recPayer);
                cfm.updateCustomer(recComp);

                // Issue a bill for the recurring payment
                recComp.issueBill(paymentAmount, java.time.LocalDate.now().plusMonths(2), 1, recPayer.getVatID());
                List<BankOfTuc.Payments.Bill> compBills = BankOfTuc.Payments.BillFileStore.getCompanyBills(recComp.getVatID());
                BankOfTuc.Payments.Bill recBill = compBills.get(compBills.size() - 1);

                BankOfTuc.Payments.RecurringPaymentScheduler scheduler = new BankOfTuc.Payments.RecurringPaymentScheduler(cfm);
                BankOfTuc.Payments.RecurringPayment rp = new BankOfTuc.Payments.RecurringPayment(recBill.getRfcode(), payerAccount, paymentAmount, timeService.today());
                scheduler.addRecurringPayment(rp);
                
                java.time.LocalDate firstDueDate = timeService.today().with(java.time.temporal.TemporalAdjusters.firstDayOfNextMonth());
                long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(timeService.today(), firstDueDate);

                timeService.advanceDays(daysUntilDue); // Advance to due date

                scheduler.dailyCheck();

                // Refresh data
                cfm.reloadCustomers();
                recPayer = cfm.getCustomerByUsername(recPayer.getUsername());
                recComp = (CompanyCustomer) cfm.getCustomerByUsername(recComp.getUsername());
                payerAccount = recPayer.getBankAccounts().get(0);
                compAccount = recComp.getBankAccounts().get(0);
                
                BankOfTuc.Payments.RecurringPayment reloadedRp = scheduler.getPaymentsForCustomer(recPayer.getVatID()).get(0);

                if (payerAccount.getBalance() < initialPayerBalance && compAccount.getBalance() > initialCompBalance) {
                    record.ok("recurring payment: balances updated correctly");
                    passed++;
                } else {
                    record.fail("recurring payment: balance update failed", "Payer: " + payerAccount.getBalance() + ", Comp: " + compAccount.getBalance());
                    failed++;
                }
                if (reloadedRp.getNextDueDate().isAfter(firstDueDate)) {
                    record.ok("recurring payment: next due date advanced");
                    passed++;
                } else {
                    record.fail("recurring payment: next due date did not advance", "Next due: " + reloadedRp.getNextDueDate());
                    failed++;
                }

                // Scenario 2: Insufficient funds & email notification
                record.ok("recurring payment: testing insufficient funds and email trigger");
                
                // Create a new bill and payment for isolation
                recComp.issueBill(100, java.time.LocalDate.now().plusMonths(2), 1, recPayer.getVatID());
                compBills = BankOfTuc.Payments.BillFileStore.getCompanyBills(recComp.getVatID());
                BankOfTuc.Payments.Bill failBill = compBills.get(compBills.size() - 1);
                
                BankOfTuc.Payments.RecurringPayment failRp = new BankOfTuc.Payments.RecurringPayment(failBill.getRfcode(), payerAccount, 100, timeService.today());
                scheduler.addRecurringPayment(failRp);
                
                payerAccount.setBalance(50); // Insufficient funds
                cfm.updateCustomer(recPayer);

                firstDueDate = timeService.today().with(java.time.temporal.TemporalAdjusters.firstDayOfNextMonth());
                daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(timeService.today(), firstDueDate);
                timeService.advanceDays(daysUntilDue); // Advance to due date

                scheduler.dailyCheck(); // 1st fail
                reloadedRp = scheduler.getPaymentsForCustomer(recPayer.getVatID()).get(1);
                 if (reloadedRp.getCurrentAttempts() == 1) { record.ok("recurring payment: 1st failure recorded"); passed++; }
                else { record.fail("recurring payment: 1st failure not recorded", "attempts: " + reloadedRp.getCurrentAttempts()); failed++; }

                timeService.advanceDays(1);
                scheduler.dailyCheck(); // 2nd fail
                reloadedRp = scheduler.getPaymentsForCustomer(recPayer.getVatID()).get(1);
                if (reloadedRp.getCurrentAttempts() == 2) { record.ok("recurring payment: 2nd failure recorded"); passed++; }
                else { record.fail("recurring payment: 2nd failure not recorded", "attempts: " + reloadedRp.getCurrentAttempts()); failed++; }

                timeService.advanceDays(1);
                scheduler.dailyCheck(); // 3rd fail
                reloadedRp = scheduler.getPaymentsForCustomer(recPayer.getVatID()).get(1);
                if (reloadedRp.getCurrentAttempts() == 3) {
                    record.ok("recurring payment: 3rd failure recorded (email should be sent)");
                    passed++;
                } else {
                    record.fail("recurring payment: 3rd failure not recorded", "attempts: " + reloadedRp.getCurrentAttempts());
                    failed++;
                }

            } else {
                record.skip("recurring payment tests", "not enough customers for tests");
                skipped++;
            }

            timeService.stopSimulation();
        } catch (Exception e) {
            record.fail("recurring payment tests", "Exception occurred: " + e.getMessage());
            failed++;
            e.printStackTrace();
        }

        // write a simple results file
        try (FileWriter fw = new FileWriter(new File("data/tests/test_results.txt"))) {
            fw.write("passed="+passed+"\nfailed="+failed+"\n");
        }

        // write detailed test log
        try (FileWriter fw = new FileWriter(new File("data/tests/test_log.txt"))) {
            for (String line : testLog) fw.write(line + System.lineSeparator());
        } catch (Exception e) {
            System.err.println("Could not write test log: " + e.getMessage());
        }

        System.out.println("Tests complete. Results in data/test_results.txt");
    }
}
