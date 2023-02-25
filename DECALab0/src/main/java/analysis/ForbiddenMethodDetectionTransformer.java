package analysis;

import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.Value;
import soot.jimple.InvokeStmt;

import java.util.List;
import java.util.Map;

public class ForbiddenMethodDetectionTransformer extends BodyTransformer {

    String methodName;
    String returnType;


    public ForbiddenMethodDetectionTransformer(String methodName, String returnType){
        this.methodName = methodName;
        this.returnType = returnType;
    }

    //to check if forbiddenMethod" with 3 parameters & return type "void" is invoked in any method in SampleCLass.java or not
    @Override
    protected void internalTransform(Body body, String s, Map<String, String> map) {
        // TODO: modify this method to make it more precise, so that it only finds a call to the actual forbiddenMethod()
        UnitPatchingChain units = body.getUnits();
        for(Unit u : units){
            if(u instanceof InvokeStmt){
                InvokeStmt invoke = (InvokeStmt) u;
                String invokeName = invoke.getInvokeExpr().getMethod().getName();
                if(invokeName.equals(methodName)){
                    String invokeReturnType = invoke.getInvokeExpr().getMethodRef().getReturnType().toString();
                    int paramsCount =  invoke.getInvokeExpr().getArgCount();
                    //List<Value> paramsValues =  invoke.getInvokeExpr().getArgs();

                    //this condition helps to check if name, returntype & number of parameters of actual method
                    if(invokeReturnType.equals(returnType) && paramsCount==3){
                        System.out.println("Found a call to forbidden method! in " + body.getMethod().getName());
                        System.out.println(invoke);
                    }
                }
            }
        }
    }
}
