import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        UserRepository repo = new UserRepository();
        LoginScreen screen = new LoginScreen(new AuthService(repo));

        screen.signup("alice", "secret123");
        screen.login("alice", "secret123");   
        screen.login("alice", "wrong");       

        repo.databaseDown = true;             // simulate a database crash
        screen.login("alice", "secret123");   // correct password, but DB is down
    }
}

// ---------------- DATA LAYER ----------------
class UserRepository {
    private final Map<String, String> users = new HashMap<>();
    boolean databaseDown = false;

    void save(String username, String password) {
        users.put(username, password);
    }

    String findPassword(String username) {
        try {
            if (databaseDown) throw new SQLException("Connection to database lost");
            return users.get(username);
        } catch (SQLException e) {
            System.out.println("   [DB] " + e.getMessage());
            return null;                     
        }
    }
}

// ---------------- LOGIC LAYER ----------------
class AuthService {
    private final UserRepository repo;
    AuthService(UserRepository repo) { this.repo = repo; }

    void signup(String username, String password) {
        repo.save(username, password);
    }

    boolean login(String username, String password) {
        try {
            String stored = repo.findPassword(username);
            return stored.equals(password);   
        } catch (Exception e) {
            return false;                     
        }
    }
}

// ---------------- PRESENTATION LAYER ----------------
class LoginScreen {
    private final AuthService service;
    LoginScreen(AuthService service) { this.service = service; }

    void signup(String username, String password) {
        try {
            service.signup(username, password);
            System.out.println("Signup OK: " + username);
        } catch (Exception e) {
            System.out.println("Signup failed");
        }
    }

    void login(String username, String password) {
        try {
            if (service.login(username, password)) System.out.println("Welcome, " + username + "!");
            else System.out.println("Invalid username or password");  //  shown even when the DB crashed
        } catch (Exception e) {
            System.out.println("Something went wrong");               // never reached
        }
    }
}