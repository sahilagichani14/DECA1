package analysis.exercise2;

import java.util.HashSet;
import java.util.Set;

import analysis.FileState;
import analysis.FileStateFact;
import analysis.ForwardAnalysis;
import analysis.VulnerabilityReporter;
import soot.Body;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JNewExpr;
import soot.jimple.internal.JReturnVoidStmt;
import soot.jimple.internal.JVirtualInvokeExpr;

public class TypeStateAnalysis extends ForwardAnalysis<Set<FileStateFact>> {

	public TypeStateAnalysis(Body body, VulnerabilityReporter reporter) {
		super(body, reporter);
	}

	@Override
	protected void flowThrough(Set<FileStateFact> in, Unit unit, Set<FileStateFact> out) {
		copy(in, out);

		// TODO: Implement your flow function here.

		if (unit instanceof JAssignStmt) {

			Value rhsVal = ((JAssignStmt) unit).getRightOp();
			Value lhsVal = ((JAssignStmt) unit).getLeftOp();

			if (rhsVal instanceof JNewExpr) {
				JNewExpr newExpression = (JNewExpr) rhsVal;
				// System.out.println(newExpression);
				if ((newExpression.getBaseType()).getClassName().equals("target.exercise2.File")) {
					Set<Value> aliases = new HashSet<>();
					aliases.add(lhsVal);
					FileStateFact initialFileState = new FileStateFact(aliases, FileState.Init);
					out.add(initialFileState);
				}

			} else if (rhsVal instanceof Local || rhsVal instanceof StaticFieldRef) {
				for (FileStateFact fileStateFact : out) {
					if (fileStateFact.containsAlias(rhsVal)) {
						if (lhsVal instanceof Local || lhsVal instanceof StaticFieldRef) {
							fileStateFact.addAlias(lhsVal);
						}
					}
				}
			}
		}

		if (unit instanceof JInvokeStmt && ((JInvokeStmt) unit).getInvokeExpr() instanceof JVirtualInvokeExpr) {
			JVirtualInvokeExpr expression = (JVirtualInvokeExpr) ((JInvokeStmt) unit).getInvokeExpr();
			String methodName = expression.getMethod().getName();
			// if open() method can be invoked from Init or Close(current state)
			if (methodName.equals("open")) {
				Value base = expression.getBase();
				for (FileStateFact fileStateFact : out) {
					if (fileStateFact.containsAlias(base)) {
						FileState currentState = fileStateFact.getState();

						if (currentState.equals(FileState.Init) || currentState.equals(FileState.Close)) {
							fileStateFact.updateState(FileState.Open);
						}
					}
				}
			}
			// if close() method can be invoked from Open or Init(current state)
			else if (methodName.equals("close")) {
				Value base = expression.getBase();
				for (FileStateFact fileStateFact : out) {
					if (fileStateFact.containsAlias(base)) {
						FileState currentState = fileStateFact.getState();
						if (currentState.equals(FileState.Open) || currentState.equals(FileState.Init)) {
							fileStateFact.updateState(FileState.Close);
						}
					}
				}
			}

		}

		if (unit instanceof JReturnVoidStmt) {
			for (FileStateFact fileStateFact : in) {
				// if file not closed then vulnerability present
				if (!fileStateFact.getState().equals(FileState.Close)) {
					this.reporter.reportVulnerability(this.method.getSignature(), unit);
				}
			}
		}

		prettyPrint(in, unit, out);
	}

	@Override
	protected Set<FileStateFact> newInitialFlow() {
		// TODO: Implement your initialization here.
		// The following line may be just a place holder, check for yourself if
		// it needs some adjustments.

		return new HashSet<FileStateFact>();
	}

	@Override
	protected void copy(Set<FileStateFact> source, Set<FileStateFact> dest) {
		// TODO: Implement the copy function here.

		// copies the source file states to destination file states
		source.stream().forEach(fileStateFact -> dest.add(fileStateFact.copy()));
	}

	@Override
	protected void merge(Set<FileStateFact> in1, Set<FileStateFact> in2, Set<FileStateFact> out) {
		// TODO: Implement the merge function here.

		boolean mergeable = true;
		// if files have same states for input and output then can be merged
		if (in1.size() == in2.size()) {
			for (FileStateFact fileStateFact1 : in1) {
				boolean contains = false;
				for (FileStateFact fileStateFact2 : in2) {
					// if Files in same state
					if (fileStateFact1.equals(fileStateFact2)) {
						contains = true;
						break;
					}
				}
				mergeable &= contains;
			}
		} else {
			mergeable = false;
		}

		// if mergeable then merged
		if (mergeable) {
			in1.stream().forEach(fileStateFact -> out.add(fileStateFact.copy()));
		}
	}

}
