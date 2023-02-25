package analysis;

import soot.Body;
import soot.BodyTransformer;

import java.util.Map;

public class StatementCounterTransformer extends BodyTransformer {

    //provides body transformation implementation to count number of units in each method in SampleClass.java
    @Override
    protected void internalTransform(Body body, String s, Map<String, String> map) {
        long count = body.getUnits().stream().count();
        System.out.println("Method: " + body.getMethod().getName());
        System.out.println("has " + count + " statements");

    }
}
