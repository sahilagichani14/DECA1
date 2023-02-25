package exercises;

import analysis.StatementCounterTransformer;
import base.TestSetup;
import org.junit.Test;
import soot.Transformer;
import target.exercise1.SampleClass;

public class Exercise2 extends TestSetup {

    @Override
    protected Transformer createAnalysisTransformer() {
        return new StatementCounterTransformer();
    }

    @Test
    public void testStatementCount(){
        executeStaticAnalysis(SampleClass.class.getName());
    }

}
