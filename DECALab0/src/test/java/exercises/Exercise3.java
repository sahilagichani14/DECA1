package exercises;

import analysis.StatementClassifierTransformer;
import base.TestSetup;
import org.junit.Test;
import soot.Transformer;
import target.exercise1.SampleClass;

public class Exercise3 extends TestSetup {

    @Override
    protected Transformer createAnalysisTransformer() {
        return new StatementClassifierTransformer();
    }

    @Test
    public void testStatementCount(){
        executeStaticAnalysis(SampleClass.class.getName());
    }

}