import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import java.util.List;
import java.util.Collections;

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

    // --- Our API Client ---
    private CabApiClient client = new CabApiClient();
    private String demoUserId = "68fc781eb1a810d218cfbc49"; // <-- Use your real User ID

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

        Task<Booking> bookingTask = new Task<Booking>() {
            @Override
            protected Booking call() throws Exception {
                // Pass the destination to the client
                return client.createBooking(demoUserId, selectedCab.getId(), destination);
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