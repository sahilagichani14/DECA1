package exercises;

import analysis.JimplePrinterTransformer;
import base.TestSetup;
import org.junit.Test;
import soot.Transformer;
import target.exercise1.SampleClass;


public class Exercise1 extends TestSetup {


    @Override
    protected Transformer createAnalysisTransformer() {
        return new JimplePrinterTransformer();
    }

    // Use of this class is to convert all methods in SampleClass.java to Jimple.
    @Test
    public void testJimplePrint(){
        executeStaticAnalysis(SampleClass.class.getName());
    }

}
