package analysis;

import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;

import java.util.Map;

public class StatementClassifierTransformer extends BodyTransformer {

    //provides body transformation implementation to imply which units statements are present in each method in SampleClass.java
    //here we have just checked for Invoke, Assignment & Return Statements
    @Override
    protected void internalTransform(Body body, String s, Map<String, String> map) {
        System.out.println("Method: " + body.getMethod().getName());
        UnitPatchingChain units = body.getUnits();
        for(Unit u: units){
            if(u instanceof InvokeStmt){
                InvokeStmt invoke = (InvokeStmt) u;
                System.out.println(invoke.toString() + " is an InvokeStmt");
            }else if(u instanceof AssignStmt){
                System.out.println(u + " is an AssignStmt");
            }
			/*
			 * added the code below
			 */            
            else if(u instanceof ReturnStmt){
            	System.out.println(u + " is an ReturnStmt");
            }
            // TODO: what other types of statements can you find?
        }
    }
}
