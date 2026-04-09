import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Collections;
// --- ADD THESE IMPORTS AT THE TOP ---
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

class Coordinates {
    public String lat;
    public String lon;

    public Coordinates(String lat, String lon) {
        this.lat = lat;
        this.lon = lon;
    }
}

public class CabApiClient {

    // --- Method 1: Search for Cabs ---
    // --- Method 1: Search for Cabs (Corrected) ---
    public List<Cab> searchForCabs(String lat, String lng) throws Exception {
        System.out.println("\n--- Searching for Cabs near (" + lat + ", " + lng + ") ---");

        String urlString = "http://localhost:3000/api/cabs/search?lat=" + lat + "&lng=" + lng;
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml");

        int responseCode = conn.getResponseCode();
        System.out.println("Node.js Server Response Code: " + responseCode);

        if (responseCode != HttpURLConnection.HTTP_OK) {
            System.err.println("Error searching for cabs.");
            // --- FIX #1: Return an empty list instead of null ---
            return Collections.emptyList();
        }

        JAXBContext context = JAXBContext.newInstance(Cabs.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Cabs cabList = (Cabs) unmarshaller.unmarshal(conn.getInputStream());

        if (cabList.getCabs() == null || cabList.getCabs().isEmpty()) {
            System.out.println("No cabs found.");
            // --- FIX #2: Return an empty list instead of null ---
            return Collections.emptyList();
        }

        // This now only runs if cabs were actually found
        return cabList.getCabs();
    }

    // --- Method 2: Create a Booking ---
    // --- Method 2: Create a Booking ---
    // ADD 'String destination' to the parameters here
    public Booking createBooking(String userId, String cabId, String destination) throws Exception {

        // UPDATE this line to print the destination
        System.out.println("\n--- Proceeding to book cab: " + cabId + " to " + destination + " ---");

        // UPDATE this line to use the 'destination' variable
        Booking booking = new Booking(userId, cabId, "JavaFX Client", destination);

        // --- (The rest of the code is perfectly correct) ---

        JAXBContext context = JAXBContext.newInstance(Booking.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        URL url = new URL("http://localhost:3000/api/bookings");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/xml");
        conn.setDoOutput(true);

        System.out.println("Connecting to Node.js API at " + url);
        try (OutputStream os = conn.getOutputStream()) {
            marshaller.marshal(booking, os);
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Node.js Server Response Code: " + responseCode);

        if (responseCode != 201) {
            System.err.println("Booking Failed.");
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        System.out.println("SUCCESS: " + response.toString());

        return booking;
    }

    // --- Method 3: View All Bookings ---
    public List<Booking> viewAllBookings() throws Exception {
        System.out.println("\n--- Getting All Booking History ---");

        URL url = new URL("http://localhost:3000/api/bookings");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml");

        if (conn.getResponseCode() != 200) {
            System.err.println("Error fetching bookings.");
            // --- FIX #1: Return an empty list ---
            return Collections.emptyList();
        }

        JAXBContext context = JAXBContext.newInstance(Bookings.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Bookings history = (Bookings) unmarshaller.unmarshal(conn.getInputStream());

        if (history.getBookings() == null || history.getBookings().isEmpty()) {
            System.out.println("No bookings found.");
            // --- FIX #2: Return an empty list ---
            return Collections.emptyList();
        }

        return history.getBookings();
    }

    public List<Cab> searchForCabsByName(String location) throws Exception {
        System.out.println("\n--- Searching for Cabs by name: " + location + " ---");

        // We must URL-encode the location string
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8.name());
        String urlString = "http://localhost:3000/api/cabs/search-by-name?location=" + encodedLocation;

        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml");

        int responseCode = conn.getResponseCode();
        System.out.println("Node.js Server Response Code: " + responseCode);

        // The rest is the SAME as searchForCabs, since the XML response is the same
        if (responseCode != HttpURLConnection.HTTP_OK) {
            System.err.println("Error searching for cabs.");
            return Collections.emptyList();
        }

        JAXBContext context = JAXBContext.newInstance(Cabs.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Cabs cabList = (Cabs) unmarshaller.unmarshal(conn.getInputStream());

        if (cabList.getCabs() == null || cabList.getCabs().isEmpty()) {
            System.out.println("No cabs found.");
            return Collections.emptyList();
        }

        return cabList.getCabs();
    }

    // --- New: Fetch bookings by user ---
    public List<Booking> viewBookingsByUser(String userId) throws Exception {
        String urlString = "http://localhost:3000/api/bookings/user/"
                + URLEncoder.encode(userId, StandardCharsets.UTF_8.name());
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml");

        if (conn.getResponseCode() != 200) {
            return Collections.emptyList();
        }

        JAXBContext context = JAXBContext.newInstance(Bookings.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Bookings history = (Bookings) unmarshaller.unmarshal(conn.getInputStream());
        if (history.getBookings() == null)
            return Collections.emptyList();
        return history.getBookings();
    }

    // --- New: Cancel booking ---
    public boolean cancelBooking(String bookingId) throws Exception {
        String urlString = "http://localhost:3000/api/bookings/"
                + URLEncoder.encode(bookingId, StandardCharsets.UTF_8.name()) + "/cancel";
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        return conn.getResponseCode() == 200;
    }

    // --- New: Complete booking ---
    public boolean completeBooking(String bookingId) throws Exception {
        String urlString = "http://localhost:3000/api/bookings/"
                + URLEncoder.encode(bookingId, StandardCharsets.UTF_8.name()) + "/complete";
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        return conn.getResponseCode() == 200;
    }

    // --- Auth: Register ---
    public JSONObject register(String name, String email, String password, String role) throws Exception {
        URL url = new URL("http://localhost:3000/api/users/register");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        JSONObject payload = new JSONObject();
        payload.put("name", name);
        payload.put("email", email);
        payload.put("password", password);
        if (role != null)
            payload.put("role", role);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() != 201) {
            return null;
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            sb.append(line);
        return new JSONObject(sb.toString());
    }

    // Overload that accepts a pre-built JSON payload (e.g., with cab details)
    public JSONObject registerWithPayload(JSONObject payload) throws Exception {
        URL url = new URL("http://localhost:3000/api/users/register");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        }
        if (conn.getResponseCode() != 201) {
            return null;
        }
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            sb.append(line);
        return new JSONObject(sb.toString());
    }

    // --- Auth: Login ---
    public JSONObject login(String email, String password) throws Exception {
        URL url = new URL("http://localhost:3000/api/users/login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        JSONObject payload = new JSONObject();
        payload.put("email", email);
        payload.put("password", password);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() != 200) {
            return null;
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            sb.append(line);
        return new JSONObject(sb.toString());
    }

    // --- Driver: Update availability ---
    public boolean setCabAvailability(String cabId, boolean available) throws Exception {
        String urlString = "http://localhost:3000/api/cabs/"
                + URLEncoder.encode(cabId, StandardCharsets.UTF_8.name()) + "/availability";
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        JSONObject payload = new JSONObject();
        payload.put("isAvailable", available);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        }
        return conn.getResponseCode() == 200;
    }
}