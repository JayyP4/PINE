import java.sql.*;
import java.util.Scanner;

public class PINE {
    private static Connection connection;

    public static void main(String[] args) throws SQLException {
        try{
            openDatabaseConnection();
            Scanner myObj = new Scanner(System.in);
            System.out.println("Welcome to spChat:\n" +
                    "\t>Login\n" +
                    "\t>Registration"
            );
            String answer = myObj.nextLine();

            if (answer.equals("login")) {
                login();
            }else if(answer.equals("registration")) {
                registration();
            }else{
                System.out.println("Enter valid option.");
            }

        }finally {

            closeDatabaseConnection();
        }
    }

    public static void login() throws SQLException {
        Scanner myObj = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String username = myObj.nextLine();
        System.out.println("Enter your password: ");
        String password = myObj.nextLine();

        try {
            PreparedStatement statement = connection.prepareStatement("""
                SELECT id, username, password
                FROM users
                WHERE username = ?
            """);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String passwordDB = resultSet.getString(3);
                int userId = resultSet.getInt(1);
                if (password.equals(passwordDB)) {
                    System.out.println("Logged in successfully.");
                    chat(userId);
                }else{
                    System.out.println("Incorrect password.");
                    login();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void chat(int user) throws SQLException {
        boolean chat = true;
        while(chat){
            Scanner myObj = new Scanner(System.in);
            System.out.println("[Your message]: \t>");
            String message = myObj.nextLine();

            if (message.equals("exit")) {
                chat = false;
            }else {
                try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO messages(sentBy, message)
                    VALUES (?, ?)
                """)) {
                    statement.setInt(1, user);
                    statement.setString(2, message);
                    statement.executeUpdate();
                }
                getMessages();
            }
        }
    }

    public static void getMessages() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("""
                SELECT *
                FROM messages
                ORDER BY msgid ASC
            """);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            String message = resultSet.getString(3);
            Time time = resultSet.getTime(4);
            System.out.println("["+ time +"]\n" + "\t" + message + "\n");
        }
    }

    public static void registration() throws SQLException {
        Scanner myObj = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String username = myObj.nextLine();
        System.out.println("Enter your password: ");
        String password = myObj.nextLine();
        System.out.println("Repeat your password: ");
        String passwordRP = myObj.nextLine();

        if (password.equals(passwordRP)) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO users(username, password)
                    VALUES (?, ?)
                """)) {
                statement.setString(1, username);
                statement.setString(2, password);
                statement.executeUpdate();
            }
            System.out.println("You have successfully registered.");
            login();
        }else{
            System.out.println("Please enter the same password.");
        }

    }

    private static void openDatabaseConnection() throws SQLException {
        System.out.println("Connecting to database...");
        connection = DriverManager.getConnection(
                "jdbc:mariadb://", //--> hier muss connection zu XAMMP hinzugefügt werden
                "" , "" //-> DB username und passwort
        );
        connection.isValid(5);
        System.out.println("Connection successful: " + connection.isValid(5));
    }

    private static void closeDatabaseConnection() throws SQLException {
        System.out.println("Closing connection...");
        connection.close();
        System.out.println("Connection successful: " + connection.isValid(5));
    }
}
