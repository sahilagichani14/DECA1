package analysis.exercise1;

import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import analysis.AbstractAnalysis;
import analysis.VulnerabilityReporter;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.Value;
import soot.jimple.InvokeStmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JStaticInvokeExpr;
import soot.tagkit.Tag;

public class MisuseAnalysis extends AbstractAnalysis {
	public MisuseAnalysis(Body body, VulnerabilityReporter reporter) {
		super(body, reporter);	
	}

	@Override
	protected void flowThrough(Unit unit) {
		
		// TODO: Implement your analysis here.
		
		if (unit instanceof JAssignStmt && ((JAssignStmt) unit).getRightOp() instanceof JStaticInvokeExpr) {
			JStaticInvokeExpr invokedExpression = (JStaticInvokeExpr)((JAssignStmt) unit).getRightOp();
			//System.out.println(invokedExpression + "expresssion");
			String invokedClassName = invokedExpression.getMethodRef().getDeclaringClass().getName();
			//System.out.println(invokedClassName);
			if (invokedClassName.equals("javax.crypto.Cipher")) {
				StringConstant str = StringConstant.v("AES");
				if (invokedExpression.getArgs().get(0).equals(str)) {
					this.reporter.reportVulnerability(this.method.getSignature(), unit);
				}
			}
		}
				
	}
}
