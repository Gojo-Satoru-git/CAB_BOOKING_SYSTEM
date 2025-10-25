import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// This class maps to the root <cabs> element
@XmlRootElement(name = "cabs")
public class Cabs {

    // JAXB will map all <cab> elements into this list
    private List<Cab> cabs;

    @XmlElement(name = "cab")
    public List<Cab> getCabs() {
        return cabs;
    }

    public void setCabs(List<Cab> cabs) {
        this.cabs = cabs;
    }
}