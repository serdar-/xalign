import java.util.ArrayList;
import org.biojava.nbio.structure.Atom;

/**
 *
 * @author Serdar
 */
public class CAContainer {

    ArrayList<Atom[]> CAArray;
    ArrayList<String> CAnames;

    public void setCAs(ArrayList<Atom[]> caarray){
        this.CAArray = caarray;
    }

    public void setNames(ArrayList<String> names){
        this.CAnames = names;
    }

    public ArrayList<Atom[]> getCAs(){
        return this.CAArray;
    }

    public ArrayList<String> getNames(){
        return this.CAnames;
    }

}