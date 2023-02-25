package analysis;

import soot.Body;
import soot.BodyTransformer;
import java.util.Map;

public class JimplePrinterTransformer extends BodyTransformer {

    //Defines how each method body is transformed.
    @Override
    protected void internalTransform(Body body, String s, Map<String, String> map) {
        System.out.println("Method name:" + body.getMethod().getName());
        System.out.println(body.toString());
    }
}
