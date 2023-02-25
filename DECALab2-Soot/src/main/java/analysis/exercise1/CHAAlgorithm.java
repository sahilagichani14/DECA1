package analysis.exercise1;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import analysis.CallGraph;
import analysis.CallGraphAlgorithm;
import soot.Hierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.internal.JAssignStmt;

public class CHAAlgorithm extends CallGraphAlgorithm {

	@Override
	protected String getAlgorithm() {
		return "CHA";
	}

	@Override
	protected void populateCallGraph(Scene scene, CallGraph cg) {
		// Your implementation goes here, also feel free to add methods as needed
		// To get your entry points we prepared getEntryPoints(scene) in the superclass
		// for you

		Stream<SootMethod> stream = this.getEntryPoints(scene);
		Hierarchy h = scene.getActiveHierarchy();

		stream.forEach(m -> {
			addNode(cg, m);
			findOutAndAddNextNodes(m, h, cg);
		});

	}

	private SootMethod findOutCalledMethodFromUnit(Unit unit) {
		SootMethod method = null;
		if (unit instanceof AssignStmt) {
			AssignStmt ass = (JAssignStmt) unit;
			if (ass.containsInvokeExpr()) {
				method = ass.getInvokeExpr().getMethod();
			}
		} else if (unit instanceof InvokeStmt) {
			InvokeStmt inv = (InvokeStmt) unit;
			method = inv.getInvokeExpr().getMethod();
		}
		return method;
	}

	private void findOutAndAddNextNodes(SootMethod m, Hierarchy h, CallGraph cg) {
		if (m.hasActiveBody()) {
			for (Unit unit : m.getActiveBody().getUnits()) {

				// find the method that is called by unit, if no method is called, go on with
				SootMethod rootMethod = findOutCalledMethodFromUnit(unit);
				if (rootMethod == null) {
					continue;
				}

				// here we find the called methods in List
				List<SootMethod> calledMethods = new ArrayList<>();
				calledMethods.add(rootMethod);

				// There might be a possibility of polymorphic calls, so we need to search for methods in sub classes
				if (!rootMethod.isConstructor()) {
					List<SootClass> childClasses = getDirectAndIndirectSubClasses(rootMethod.getDeclaringClass(), h);

					for (SootClass childClass : childClasses) {
						for (SootMethod childMethod : childClass.getMethods()) {
							if (childMethod.getSubSignature().equals(rootMethod.getSubSignature())) {
								calledMethods.add(childMethod);
							}
						}

					}
				}

				/*
				 * now we have found all methods, so we add the nodes and edges to the call
				 * graph and then make the recursive calls for each of the methods we found
				 */

				for (SootMethod calledMethod : calledMethods) {
					addNode(cg, calledMethod);
					if (!cg.hasEdge(m, calledMethod)) {
						cg.addEdge(m, calledMethod);
					}
					findOutAndAddNextNodes(calledMethod, h, cg);
				}

			}
		}
	}

	//Returns a list of direct and indirect subclasses of the given class c
	private List<SootClass> getDirectAndIndirectSubClasses(SootClass c, Hierarchy h) {
		List<SootClass> childClasses = new ArrayList<>();

		// if c is Interface then find it's implementation class else find subclasses
		if (c.isInterface()) {
			childClasses = h.getDirectImplementersOf(c);
		} else {
			childClasses = h.getDirectSubclassesOf(c);
		}

		// Recursive call for getting all classes inside subclasses and so on..
		List<SootClass> newList = new ArrayList<>();
		newList.addAll(childClasses);
		for (SootClass cc : childClasses) {
			newList.addAll(getDirectAndIndirectSubClasses(cc, h));
		}

		return newList;
	}

	private void addNode(CallGraph cg, SootMethod node) {
		// if call graph doesn't consists of Node
		if (!cg.hasNode(node)) {
			cg.addNode(node);
		}
	}

}
