package BankOfTuc.Auth;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import BankOfTuc.User;
import BankOfTuc.Bookkeeping.UserFileManagement;

public class LoginManager {

    private final List<LoginListener> listeners = new ArrayList<>();
    private static final long TIMEOUT = 15 * 60 * 1000; // 15 minutes
    private static final int MAX_ATTEMPTS = 3;
    private static final String LOCK_FILE = "data/login_locks.json";
    private static final String SESSION_FILE = "data/sessions.json";
    private static final ObjectMapper mapper = new ObjectMapper();
    private final UserFileManagement ufm  ;
    // in-memory tracking for failed login attempts and lock timestamps (ms)
    private final Map<String,Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String,Long> lockUntil = new ConcurrentHashMap<>();
    public LoginManager(UserFileManagement ufm) {
        this.ufm = ufm;
        loadLockData();
        startTimeoutChecker();
    }

    //add a listener
    public void addListener(LoginListener l) {
        listeners.add(l);
    }

    /**
     * login method:
     * 1 -> normal login
     * 2 -> needs QR
     * 3 -> already logged in
     * 0 -> wrong username/password
     */
    public int login(String username, String password) {
        long now = System.currentTimeMillis();
        Long until = lockUntil.get(username);
        if (until != null && now < until) {
            return 6; // locked due to too many failed attempts
        }

        User user = ufm.getUserByUsername(username);

        if (user != null && PasswordUtils.verifyPassword(password.toCharArray(), user.getSalt(), user.getHashedPassword())) {

            if(!user.getActive()) return 4;
            
            if (isLoggedIn(user.getUsername())) return 3;

            if (!user.hasQR()) {
                loginUser(user.getUsername());
                // successful login: clear any failed attempts / locks
                failedAttempts.remove(username);
                lockUntil.remove(username);
                saveLockData();
                return 1;
            } else {
                return 2;
            }
        }

        // wrong credentials: increment attempts and possibly lock
        int attempts = failedAttempts.getOrDefault(username, 0) + 1;
        if (attempts >= MAX_ATTEMPTS) {
            long lockTime = System.currentTimeMillis() + TIMEOUT;
            lockUntil.put(username, lockTime);
            failedAttempts.remove(username);
            saveLockData();
            return 6; // newly locked
        } else {
            failedAttempts.put(username, attempts);
            saveLockData();
            return 0;
        }
    }

    /** Returns remaining lock seconds, or 0 if not locked */
    public long getLoginLockRemainingSeconds(String username) {
        Long until = lockUntil.get(username);
        if (until == null) return 0L;
        long now = System.currentTimeMillis();
        long rem = until - now;
        return rem > 0 ? (rem + 999) / 1000 : 0L;
    }

    public boolean qrCodeLogin(String username, String qrcode) {
        User user = ufm.getUserByUsername(username);
        if (user != null && QrUtils.verifyQrCode(user.getQrCode(), qrcode)) {
            loginUser(user.getUsername());
            // clear attempts/locks on successful qr login
            failedAttempts.remove(username);
            lockUntil.remove(username);
            saveLockData();
            return true;
        }
        return false;
    }

    public void logout(String username) {
        logoutUser(username);
        listeners.forEach(l -> l.onLogout(username));
    }

    public boolean isLoggedIn(String username) {
        return getSessions().stream().anyMatch(s -> s.getUsername().equals(username));
    }

    public void activity(String username) {
        List<Session> sessions = getSessions();
        boolean changed = false;
        for (Session s : sessions) {
            if (s.getUsername().equals(username)) {
                s.touch();
                changed = true;
            }
        }
        if (changed) saveSessions(sessions);
    }

    //internal persistent session methods 

    private void loginUser(String username) {
        List<Session> sessions = getSessions();
        sessions.removeIf(s -> s.getUsername().equals(username));
        sessions.add(new Session(username));
        saveSessions(sessions);
    }

    private void logoutUser(String username) {
        List<Session> sessions = getSessions();
        sessions.removeIf(s -> s.getUsername().equals(username));
        saveSessions(sessions);
    }

    private List<Session> getSessions() {
        try {
            File f = new File(SESSION_FILE);
            if (!f.exists()) return new ArrayList<>();
            return mapper.readValue(f, new TypeReference<List<Session>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveSessions(List<Session> sessions) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(SESSION_FILE), sessions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startTimeoutChecker() {
        Thread t = new Thread(() -> {
            while (true) {
                long now = System.currentTimeMillis();
                List<Session> sessions = getSessions();
                boolean changed = false;
    
                Iterator<Session> it = sessions.iterator();
                while (it.hasNext()) {
                    Session s = it.next();
                    if (now - s.getLastActivity() >= TIMEOUT) {
                        it.remove();
                        if (isLoggedIn(s.getUsername())) {
                            listeners.forEach(l -> l.onTimeout(s.getUsername()));
                        }
                        changed = true;
                    }
                }

                // cleanup expired login locks
                boolean lockChanged = false;
                Iterator<Map.Entry<String,Long>> lit = lockUntil.entrySet().iterator();
                while (lit.hasNext()) {
                    Map.Entry<String,Long> e = lit.next();
                    if (e.getValue() <= now) {
                        lit.remove();
                        lockChanged = true;
                    }
                }

                if (changed) saveSessions(sessions);
                if (lockChanged) saveLockData();
    
                try {
                    Thread.sleep(30_000); // Check every 30 seconds
                } catch (InterruptedException ignored) {}
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public long getTimeRemaining(String username) {
        return getSessions().stream()
            .filter(s -> s.getUsername().equals(username))
            .findFirst()
            .map(s -> {
                long timeSinceLastActivity = System.currentTimeMillis() - s.getLastActivity();
                return Math.max(0, TIMEOUT - timeSinceLastActivity);
            })
            .orElse(0L);
    }

    public long getFormattedTimeRemaining(String username) {
        long remainingMs = getTimeRemaining(username);
        if (remainingMs <= 0) return 0;
        
        long seconds = remainingMs / 1000;

        return seconds;
    }

    // persistence for failed attempts / locks
    private static class LockEntry {
        public Integer attempts;
        public Long until;
    }

    private synchronized void saveLockData() {
        try {
            Map<String, LockEntry> out = new HashMap<>();
            // include entries from both maps
            Set<String> keys = new HashSet<>();
            keys.addAll(failedAttempts.keySet());
            keys.addAll(lockUntil.keySet());
            for (String k : keys) {
                LockEntry le = new LockEntry();
                le.attempts = failedAttempts.get(k);
                le.until = lockUntil.get(k);
                out.put(k, le);
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(LOCK_FILE), out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void loadLockData() {
        try {
            File f = new File(LOCK_FILE);
            if (!f.exists()) return;
            Map<String, LockEntry> in = mapper.readValue(f, new TypeReference<Map<String, LockEntry>>() {});
            long now = System.currentTimeMillis();
            for (Map.Entry<String, LockEntry> e : in.entrySet()) {
                String k = e.getKey();
                LockEntry le = e.getValue();
                if (le == null) continue;
                if (le.attempts != null && le.attempts > 0) failedAttempts.put(k, le.attempts);
                if (le.until != null && le.until > now) lockUntil.put(k, le.until);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
