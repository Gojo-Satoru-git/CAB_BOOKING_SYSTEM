import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
    public static void viewAllBookings() throws Exception {
        System.out.println("\n--- Getting All Booking History ---");
        
        URL url = new URL("http://localhost:3000/api/bookings");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml"); // Ask for XML

        if (conn.getResponseCode() != 200) {
            System.err.println("Error fetching bookings.");
            return;
        }

        JAXBContext context = JAXBContext.newInstance(Bookings.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        
        Bookings history = (Bookings) unmarshaller.unmarshal(conn.getInputStream());

        System.out.println("Found " + history.getBookings().size() + " total bookings:");
        for (Booking b : history.getBookings()) {
            System.out.println("  -> Booking ID: " + b.getUserId()); 
        }
    }
    private static Cab searchForCabs(String lat, String lng) throws Exception {
        System.out.println("\n--- Searching for Cabs near (" + lat + ", " + lng + ") ---");
        
        String urlString = "http://localhost:3000/api/cabs/search?lat=" + lat + "&lng=" + lng;
        URL url = new URL(urlString);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml");
        int responseCode = conn.getResponseCode();
        System.out.println("Node.js Server Response Code: " + responseCode);

        if (responseCode != HttpURLConnection.HTTP_OK) { 
            System.err.println("Error searching for cabs.");
            return null;
        }
        JAXBContext context = JAXBContext.newInstance(Cabs.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Cabs cabList = (Cabs) unmarshaller.unmarshal(conn.getInputStream());
        if (cabList.getCabs() == null || cabList.getCabs().isEmpty()) {
            System.out.println("No cabs found.");
            return null;
        }
        System.out.println("Found " + cabList.getCabs().size() + " available cabs:");
        for (Cab cab : cabList.getCabs()) {
            System.out.println("  -> " + cab.toString());
        }
        return cabList.getCabs().get(0);
    }
    public static void main(String[] args) {
        try {
            Cab availableCab = searchForCabs("12.9500", "80.2450");

            if (availableCab == null) {
            System.err.println("\nBooking failed: No available cabs to book.");
            return;
         }
        
        System.out.println("\n--- Proceeding to book cab: " + availableCab.getId() + " ---");
        String testCabId = availableCab.getId(); 
    
        String testUserId = "68fc781eb1a810d218cfbc49";

        Booking booking = new Booking(testUserId, testCabId, "Java Client (XML)", "Node.js API");

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

        BufferedReader reader = new BufferedReader(new InputStreamReader(
            (responseCode == 201) ? conn.getInputStream() : conn.getErrorStream()
        ));

        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        System.out.println(response.toString());

        if (responseCode == 201) {
             System.out.println("\n✅ SUCCESS! Java client SEARCHED and BOOKED a cab!");
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}