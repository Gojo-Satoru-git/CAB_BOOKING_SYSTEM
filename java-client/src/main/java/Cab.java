import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

// This class will map to a <cab> element
@XmlRootElement(name = "cab")
@XmlAccessorType(XmlAccessType.FIELD) // Tells JAXB to use fields directly
public class Cab {

    // These names must match the keys from our Node.js object
    @XmlElement(name = "_id")
    private String _id;
    
    @XmlElement
    private String driverName;
    
    @XmlElement
    private String cabModel;
    
    @XmlElement
    private String licensePlate;
    
    @XmlElement
    private boolean isAvailable;

    // A 'toString' method to print the cab details nicely
    @Override
    public String toString() {
        return "Cab [id=" + _id + ", driver=" + driverName + ", model=" + cabModel + "]";
    }
    
    // --- Getters for the data we need ---
    public String getId() { return _id; }
    public String getDriverName() { return driverName; }
}