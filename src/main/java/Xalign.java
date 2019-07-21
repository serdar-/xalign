import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.Number;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.StructureAlignmentFactory;
import org.biojava.nbio.structure.align.fatcat.FatCatFlexible;
import org.biojava.nbio.structure.align.fatcat.FatCatRigid;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.biojava.nbio.core.util.InputStreamProvider;
//import org.biojava3.core.util.InputStreamProvider;

/**
 *
 * @author Serdar
 */
public class Xalign {

    private static String RESULT_DIR;

    public static void setResultDir(String dir){
        RESULT_DIR = dir;
    }

    private static String getResultDir(){
        return RESULT_DIR;
    }

    private static CAContainer getCAsfromZip(String filename) throws IOException{
        ZipFile zipFile = new ZipFile(filename);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ArrayList<Atom[]> CAArray = new ArrayList<Atom[]>();
        ArrayList<String> pdbnames = new ArrayList<String>();
        PDBFileReader pdbreader = new PDBFileReader();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            if(entry.isDirectory() == false){
                InputStream is = zipFile.getInputStream(entry);
                pdbnames.add(FilenameUtils.getName(entry.getName()));
                System.out.println(entry.getName() + " added");
                File pdbfile = new File("pdb");
                OutputStream os = new FileOutputStream(pdbfile);
                IOUtils.copy(is, os);
                os.close();
                CAArray.add(StructureTools.getAtomCAArray(pdbreader.getStructure(pdbfile)));
            }
        }
        CAContainer cas = new CAContainer();
        cas.setCAs(CAArray);
        cas.setNames(pdbnames);
        return cas;
    }

    public static StructureAlignment setAlgorithm(int index) throws StructureException{

        StructureAlignment algorithm =
                StructureAlignmentFactory.getAlgorithm(FatCatRigid.algorithmName);
        if(index == 1){
            algorithm =
                    StructureAlignmentFactory.getAlgorithm(FatCatRigid.algorithmName);
        } else if (index == 2){
            algorithm =
                    StructureAlignmentFactory.getAlgorithm(FatCatFlexible.algorithmName);
        } else if (index == 3){
            algorithm =
                    StructureAlignmentFactory.getAlgorithm(FatCatFlexible.algorithmName);
        }
        return algorithm;
    }

    public static void crossAlign(ArrayList<Atom[]> ca1, ArrayList<Atom[]> ca2,
                                  ArrayList<String> label1, ArrayList<String> label2,
                                  int alg) throws IOException, StructureException, WriteException{

        FileWriter results = new FileWriter(new File(getResultDir(),
                "xalign-results.txt"));
        System.setProperty(InputStreamProvider.CACHE_PROPERTY, "true");
        StructureAlignment algorithm = setAlgorithm(alg);
        NumberFormat decimalpoints = new NumberFormat("0.000");
        WritableCellFormat format = new WritableCellFormat(decimalpoints);
        WorkbookSettings set = new WorkbookSettings();
        set.setUseTemporaryFileDuringWrite(true);
        WritableWorkbook workbook =
                Workbook.createWorkbook(new File(getResultDir(),"/alignment.xls"), set);
        WritableSheet sheet = workbook.createSheet("RMSDs", alg);

        // Create labels
        for(int i = 0; i < label1.size(); i++){
            for(int j = 0; j < label2.size(); j++){
                Label lab1 = new Label(0,i+1,label1.get(i));
                Label lab2 = new Label(j+1,0,label2.get(j));
                sheet.addCell(lab1);
                sheet.addCell(lab2);
            }
        }
        // Align structures
        for(int i=0; i < ca1.size(); i++){
            for(int j = 0; j < ca2.size(); j++){
                AFPChain afpchain = algorithm.align(ca1.get(i), ca2.get(j));
                results.write(label1.get(i) + " <--> " + label2.get(j) + " " +
                        afpchain.getChainRmsd() + "\n");
                System.out.println(label1.get(i) + " <--> " + label2.get(j) + " " +
                        afpchain.getChainRmsd()+ " " + " Calc. time: " + afpchain.getCalculationTime());
                Number rmsd = new Number(j+1,i+1,afpchain.getChainRmsd(),format);
                sheet.addCell(rmsd);
                results.flush();
            }
        }
        results.close();
        workbook.write();
        workbook.close();
        System.out.println("Done.");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException,
            StructureException, WriteException{
        // TODO code application logic here
        // java -jar xalign.jar algorithm_index first_file second_file results_dir (or)
        // java -jar xalign.jar algorithm_index first_file results_dir
        if(args.length == 4){
            int algorithm_index = Integer.parseInt(args[0]);
            String file1 = args[1];
            String file2 = args[2];
            setResultDir(args[3]);
            CAContainer cas1 = getCAsfromZip(file1);
            CAContainer cas2 = getCAsfromZip(file2);
            ArrayList<Atom[]> ca1 = cas1.getCAs();
            ArrayList<Atom[]> ca2 = cas2.getCAs();
            ArrayList<String> name1 = cas1.getNames();
            ArrayList<String> name2 = cas2.getNames();
            crossAlign(ca1,ca2,name1,name2,algorithm_index);
        } else if(args.length == 3) {
            int algorithm_index = Integer.parseInt(args[0]);
            String file1 = args[1];
            setResultDir(args[2]);
            CAContainer cas = getCAsfromZip(file1);
            ArrayList<Atom[]> ca1 = cas.getCAs();
            ArrayList<String> name1 = cas.getNames();
            crossAlign(ca1,ca1,name1,name1,algorithm_index);
        }
    }
}
