import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import java.util.List;
import java.util.Collections;
import org.json.JSONObject;

public class MainController {

    // --- FXML UI Elements ---
    @FXML
    private TextField addressField;

    @FXML
    private ListView<Cab> cabListView;

    @FXML
    private ListView<Booking> bookingListView;

    // This is the new field for destination
    @FXML
    private TextField destinationField;

    @FXML
    private Label currentUserLabel;

    @FXML
    private Button markAvailableBtn;
    @FXML
    private Button markUnavailableBtn;
    @FXML
    private Button bookCabBtn;
    @FXML
    private Button refreshBookingsBtn;
    @FXML
    private Button myBookingsBtn;
    @FXML
    private Button completeBookingBtn;
    @FXML
    private Button cancelBookingBtn;

    // --- Our API Client ---
    private CabApiClient client = new CabApiClient();
    private String currentUserId = Session.getInstance().getUserId();
    private String currentRole = Session.getInstance().getRole(); // USER or DRIVER
    private String currentUserName = Session.getInstance().getUserName();

    @FXML
    private void initialize() {
        if (currentUserName != null && currentRole != null) {
            currentUserLabel.setText("Logged in as " + currentUserName + " (" + currentRole + ")");
        } else {
            currentUserLabel.setText("Logged in");
        }

        boolean isDriver = "DRIVER".equals(currentRole);
        // Driver-only controls
        if (markAvailableBtn != null)
            markAvailableBtn.setVisible(isDriver);
        if (markUnavailableBtn != null)
            markUnavailableBtn.setVisible(isDriver);
        if (completeBookingBtn != null)
            completeBookingBtn.setVisible(isDriver);

        // User-only controls
        boolean isUser = !isDriver;
        if (bookCabBtn != null)
            bookCabBtn.setVisible(isUser);
        if (myBookingsBtn != null)
            myBookingsBtn.setVisible(isUser);
        if (cancelBookingBtn != null)
            cancelBookingBtn.setVisible(isUser);
    }

    @FXML
    private void handleSearchCabs() {
        String address = addressField.getText();
        if (address == null || address.trim().isEmpty()) {
            showAlert("Error", "Please enter an address.");
            return;
        }

        Task<List<Cab>> searchTask = new Task<List<Cab>>() {
            @Override
            protected List<Cab> call() throws Exception {
                return client.searchForCabsByName(address);
            }
        };

        searchTask.setOnSucceeded(event -> {
            List<Cab> cabs = searchTask.getValue();
            cabListView.getItems().setAll(cabs);

            if (cabs.isEmpty()) {
                System.out.println("UI Updated: No cabs found.");
            } else {
                System.out.println("UI Updated with cabs.");
            }
        });

        searchTask.setOnFailed(event -> {
            searchTask.getException().printStackTrace();
            showAlert("Error", "Could not find cabs.");
        });

        new Thread(searchTask).start();
    }

    // --- THIS IS THE UPDATED METHOD ---
    @FXML
    private void handleBookCab() {
        if ("DRIVER".equals(currentRole)) {
            showAlert("Not allowed", "Drivers cannot book rides from this UI.");
            return;
        }
        Cab selectedCab = cabListView.getSelectionModel().getSelectedItem();

        // Read from the new destination field
        String destination = destinationField.getText();

        if (selectedCab == null) {
            showAlert("Error", "Please select a cab from the list first.");
            return;
        }

        if (destination == null || destination.trim().isEmpty()) {
            showAlert("Error", "Please enter a destination.");
            return;
        }

        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            showAlert("Error", "Please login first.");
            return;
        }

        Task<Booking> bookingTask = new Task<Booking>() {
            @Override
            protected Booking call() throws Exception {
                // Pass the destination to the client
                return client.createBooking(currentUserId, selectedCab.getId(), destination);
            }
        };

        bookingTask.setOnSucceeded(event -> {
            Booking booking = bookingTask.getValue();
            if (booking != null) {
                showAlert("Success", "Booking Confirmed!");
                handleSearchCabs(); // Refresh cab list
                handleViewBookings(); // Refresh booking list
            }
        });

        bookingTask.setOnFailed(event -> {
            bookingTask.getException().printStackTrace();
            showAlert("Error", "Booking failed.");
        });

        new Thread(bookingTask).start();
    }

    // This is the new method for viewing bookings
    @FXML
    private void handleViewBookings() {
        Task<List<Booking>> viewTask = new Task<List<Booking>>() {
            @Override
            protected List<Booking> call() throws Exception {
                return client.viewAllBookings();
            }
        };

        viewTask.setOnSucceeded(event -> {
            List<Booking> bookings = viewTask.getValue();
            bookingListView.getItems().setAll(bookings);
            System.out.println("UI Updated with bookings.");
        });

        viewTask.setOnFailed(event -> {
            viewTask.getException().printStackTrace();
            showAlert("Error", "Could not fetch bookings.");
        });

        new Thread(viewTask).start();
    }

    @FXML
    private void handleViewMyBookings() {
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            showAlert("Error", "Please login first.");
            return;
        }

        Task<List<Booking>> viewTask = new Task<List<Booking>>() {
            @Override
            protected List<Booking> call() throws Exception {
                return client.viewBookingsByUser(currentUserId);
            }
        };
        viewTask.setOnSucceeded(event -> {
            List<Booking> bookings = viewTask.getValue();
            bookingListView.getItems().setAll(bookings);
        });
        viewTask.setOnFailed(event -> {
            viewTask.getException().printStackTrace();
            showAlert("Error", "Could not fetch my bookings.");
        });
        new Thread(viewTask).start();
    }

    @FXML
    private void handleCancelBooking() {
        if ("DRIVER".equals(currentRole)) {
            showAlert("Not allowed", "Drivers cannot cancel bookings here.");
            return;
        }
        Booking selected = bookingListView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) {
            showAlert("Error", "Select a booking first.");
            return;
        }
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return client.cancelBooking(selected.getId());
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                showAlert("Success", "Booking cancelled");
                handleViewBookings();
                handleSearchCabs();
            } else {
                showAlert("Error", "Failed to cancel booking");
            }
        });
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            showAlert("Error", "Failed to cancel booking");
        });
        new Thread(task).start();
    }

    @FXML
    private void handleCompleteBooking() {
        if (!"DRIVER".equals(currentRole)) {
            showAlert("Not allowed", "Only drivers can complete bookings.");
            return;
        }
        Booking selected = bookingListView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) {
            showAlert("Error", "Select a booking first.");
            return;
        }
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return client.completeBooking(selected.getId());
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                showAlert("Success", "Booking completed");
                handleViewBookings();
                handleSearchCabs();
            } else {
                showAlert("Error", "Failed to complete booking");
            }
        });
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            showAlert("Error", "Failed to complete booking");
        });
        new Thread(task).start();
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().clear();
        currentUserId = null;
        currentRole = null;
        currentUserName = null;
        currentUserLabel.setText("Logged out");
    }

    @FXML
    private void handleMarkAvailable() {
        if (!"DRIVER".equals(currentRole)) {
            showAlert("Error", "Only drivers can change availability");
            return;
        }
        Cab selected = cabListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select your cab");
            return;
        }
        Task<Boolean> t = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return client.setCabAvailability(selected.getId(), true);
            }
        };
        t.setOnSucceeded(e -> {
            if (t.getValue()) {
                showAlert("Success", "Marked available");
                handleSearchCabs();
            } else
                showAlert("Error", "Failed");
        });
        t.setOnFailed(e -> {
            t.getException().printStackTrace();
            showAlert("Error", "Failed");
        });
        new Thread(t).start();
    }

    @FXML
    private void handleMarkUnavailable() {
        if (!"DRIVER".equals(currentRole)) {
            showAlert("Error", "Only drivers can change availability");
            return;
        }
        Cab selected = cabListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select your cab");
            return;
        }
        Task<Boolean> t = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return client.setCabAvailability(selected.getId(), false);
            }
        };
        t.setOnSucceeded(e -> {
            if (t.getValue()) {
                showAlert("Success", "Marked unavailable");
                handleSearchCabs();
            } else
                showAlert("Error", "Failed");
        });
        t.setOnFailed(e -> {
            t.getException().printStackTrace();
            showAlert("Error", "Failed");
        });
        new Thread(t).start();
    }

    // This helper method is unchanged
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}