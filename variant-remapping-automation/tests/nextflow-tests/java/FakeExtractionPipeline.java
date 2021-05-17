import java.io.FileWriter;
import java.io.IOException;


public class FakeExtractionPipeline {
    
    public static void main(String[] args) {
        String outString = "java -jar extraction.jar";
        String inFile = null;
        for (String arg: args) {
            outString += " " + arg;
            if (arg.startsWith("--spring.config.name="))
            inFile = arg.substring("--spring.config.name=".length());
        }
        System.out.println(outString);

        // real pipeline gets this from properties
        String outFile1 = "_dbsnp" + inFile.substring(0, inFile.indexOf(".")) + ".vcf";
        try {
            FileWriter writer = new FileWriter(outFile1);
            writer.write("remapped dbsnp variants\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
	    }
	    String outFile2 = "_eva" + inFile.substring(0, inFile.indexOf(".")) + ".vcf";
        try {
            FileWriter writer = new FileWriter(outFile2);
            writer.write("remapped eva variants\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
	    }
    }

}
