import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// @XmlRootElement tells JAXB this is the main <booking> tag
@XmlRootElement(name = "booking")
public class Booking {

    private String _id;
    private String userId;
    private String cabId;
    private String pickupLocation;
    private String dropLocation;
    private String status;

    // JAXB needs a no-arg constructor
    public Booking() {
    }

    public Booking(String userId, String cabId, String pickupLocation, String dropLocation) {
        this.userId = userId;
        this.cabId = cabId;
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
    }

    @XmlElement(name = "_id")
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    // --- Getters and Setters with @XmlElement ---
    @XmlElement
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @XmlElement
    public String getCabId() {
        return cabId;
    }

    public void setCabId(String cabId) {
        this.cabId = cabId;
    }

    @XmlElement
    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    @XmlElement
    public String getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(String dropLocation) {
        this.dropLocation = dropLocation;
    }

    @XmlElement
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        // This is the text that will appear in the ListView
        return "#" + (_id != null ? _id.substring(Math.max(0, _id.length() - 6)) : "") +
                " [" + (status != null ? status : "?") + "] " +
                "From: " + pickupLocation + " -> To: " + dropLocation;
    }
}