package exercises;

import analysis.ForbiddenMethodDetectionTransformer;
import analysis.StatementClassifierTransformer;
import base.TestSetup;
import org.junit.Test;
import soot.Transformer;
import target.exercise1.SampleClass;

public class Exercise4 extends TestSetup {

    @Override
    protected Transformer createAnalysisTransformer() {
        return new ForbiddenMethodDetectionTransformer("forbiddenMethod", "void");
    }

    @Test
    public void testStatementCount(){
        executeStaticAnalysis(SampleClass.class.getName());
    }

}