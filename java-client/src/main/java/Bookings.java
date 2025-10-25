import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// This maps to the root <bookings> tag
@XmlRootElement(name = "bookings")
public class Bookings {

    // This maps to a list of <booking> tags
    private List<Booking> bookingList;

    @XmlElement(name = "booking")
    public List<Booking> getBookings() {
        return bookingList;
    }

    public void setBookings(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }
}