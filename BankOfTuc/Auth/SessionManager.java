package BankOfTuc.Auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SessionManager {

    private static final String SESSION_FILE = "data/sessions.json";
    private static final long TIMEOUT = 15 * 60 * 1000; // 15 minutes
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void login(String username) {
        List<Session> sessions = loadSessions();
        sessions.removeIf(s -> s.getUsername().equals(username));
        sessions.add(new Session(username));
        saveSessions(sessions);
    }

    public static void logout(String username) {
        List<Session> sessions = loadSessions();
        sessions.removeIf(s -> s.getUsername().equals(username));
        saveSessions(sessions);
    }

    public static boolean isLoggedIn(String username) {
        List<Session> sessions = loadSessions();
        long now = System.currentTimeMillis();
        boolean changed = false;

        Iterator<Session> it = sessions.iterator();
        while (it.hasNext()) {
            Session s = it.next();
            if (now - s.getLastActivity() > TIMEOUT) {
                it.remove();
                changed = true;
            }
        }

        if (changed) saveSessions(sessions);

        return sessions.stream().anyMatch(s -> s.getUsername().equals(username));
    }

    public static void refresh(String username) {
        List<Session> sessions = loadSessions();
        for (Session s : sessions) {
            if (s.getUsername().equals(username)) s.touch();
        }
        saveSessions(sessions);
    }

    private static List<Session> loadSessions() {
        try {
            File f = new File(SESSION_FILE);
            if (!f.exists()) return new ArrayList<>();
            return mapper.readValue(f, new TypeReference<List<Session>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static void saveSessions(List<Session> sessions) {
        File f = new File(SESSION_FILE);
        File parentDir = f .getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs(); // creates all missing parent folders
        }
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(SESSION_FILE), sessions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
