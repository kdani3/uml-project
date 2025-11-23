package BankOfTuc.auth;

import java.io.File;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import BankOfTuc.JSONUtils;
import BankOfTuc.User;

public class LoginManager {

    private final List<LoginListener> listeners = new ArrayList<>();
    private static final long TIMEOUT = 15 * 60 * 1000; // 15 minutes
    private static final String SESSION_FILE = "sessions.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public LoginManager() {
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
        User user = JSONUtils.getUserByUsername(username);

        if (user != null && PasswordUtils.verifyPassword(password.toCharArray(), user.getSalt(), user.getHashedPassword())) {

            if (isLoggedIn(username)) return 3;

            String qr =user.getQrCode() ;
            if (qr == null || qr.trim().isEmpty() || qr.trim().equalsIgnoreCase("null")) {
                loginUser(username);
                return 1;
            } else {
                return 2;
            }
        }

        return 0;
    }

    public boolean qrCodeLogin(String username, String qrcode) {
        User user = JSONUtils.getUserByUsername(username);
        if (user != null && QrUtils.verifyQrCode(user.getQrCode(), qrcode)) {
            loginUser(username);
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

    // ----------------- Internal persistent session methods -----------------

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

    // ----------------- Timeout checker -----------------

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
                        listeners.forEach(l -> l.onTimeout(s.getUsername()));
                        changed = true;
                    }
                }

                if (changed) saveSessions(sessions);

                try { Thread.sleep(30_000); } catch (InterruptedException ignored) {}
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
