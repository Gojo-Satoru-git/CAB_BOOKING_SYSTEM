import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.JSONObject;

public class LoginController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleCombo;
    @FXML
    private Label statusLabel;

    // Driver cab fields
    @FXML
    private TextField driverNameField;
    @FXML
    private TextField cabModelField;
    @FXML
    private TextField licensePlateField;
    @FXML
    private TextField locationNameField;
    @FXML
    private TextField latitudeField;
    @FXML
    private TextField longitudeField;

    private final CabApiClient client = new CabApiClient();

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();
        statusLabel.setText("Logging in...");
        Task<JSONObject> t = new Task<JSONObject>() {
            @Override
            protected JSONObject call() throws Exception {
                return client.login(email, password);
            }
        };
        t.setOnSucceeded(e -> {
            JSONObject resp = t.getValue();
            if (resp == null) {
                statusLabel.setText("Login failed");
                return;
            }
            JSONObject u = resp.getJSONObject("user");
            Session s = Session.getInstance();
            s.setUserId(u.getString("_id"));
            s.setUserName(u.getString("name"));
            s.setRole(u.getString("role"));
            openMainAndClose();
        });
        t.setOnFailed(e -> {
            statusLabel.setText("Login error");
            t.getException().printStackTrace();
        });
        new Thread(t).start();
    }

    @FXML
    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleCombo.getValue();
        statusLabel.setText("Registering...");
        Task<JSONObject> t = new Task<JSONObject>() {
            @Override
            protected JSONObject call() throws Exception {
                JSONObject payload = new JSONObject();
                payload.put("name", name);
                payload.put("email", email);
                payload.put("password", password);
                if (role != null)
                    payload.put("role", role);
                if ("DRIVER".equals(role)) {
                    payload.put("driverName", driverNameField.getText());
                    payload.put("cabModel", cabModelField.getText());
                    payload.put("licensePlate", licensePlateField.getText());
                    payload.put("locationName", locationNameField.getText());
                    String lat = latitudeField.getText();
                    String lon = longitudeField.getText();
                    if (lat != null && !lat.isEmpty())
                        payload.put("latitude", lat);
                    if (lon != null && !lon.isEmpty())
                        payload.put("longitude", lon);
                }
                return client.registerWithPayload(payload);
            }
        };
        t.setOnSucceeded(e -> {
            JSONObject resp = t.getValue();
            if (resp == null) {
                statusLabel.setText("Register failed");
                return;
            }
            Session s = Session.getInstance();
            s.setUserId(resp.getString("_id"));
            s.setUserName(resp.getString("name"));
            s.setRole(resp.getString("role"));
            openMainAndClose();
        });
        t.setOnFailed(e -> {
            statusLabel.setText("Register error");
            t.getException().printStackTrace();
        });
        new Thread(t).start();
    }

    private void openMainAndClose() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Failed to open main view");
        }
    }
}
