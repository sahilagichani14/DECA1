package analysis.exercise2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import analysis.CallGraph;
import analysis.exercise1.CHAAlgorithm;
import soot.Hierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.internal.JAssignStmt;

//Extended CHA as it is base for RTA
public class RTAAlgorithm extends CHAAlgorithm {
	
	//to find all classes which are instanciated 
	private Set<SootClass> findInstanciatedClasses = new HashSet<>();

	@Override
	protected String getAlgorithm() {
		return "RTA";
	}

	@Override
    	protected void populateCallGraph(Scene scene, CallGraph cg) {
    		// Your implementation goes here, also feel free to add methods as needed
    		// To get your entry points we prepared getEntryPoints(scene) in the superclass
    		// for you

    		Stream<SootMethod> stream = this.getEntryPoints(scene);

    		List<SootMethod> entryPoints = new ArrayList<>();
    		stream.forEach(m -> {
    			if (m.getSignature().contains("exercise2")) {
    				entryPoints.add(m);
    			}
    		});

    		entryPoints.forEach(m -> {
    			findClassesInstanciated(m);
    		});

    		Hierarchy h = scene.getActiveHierarchy();
    		entryPoints.forEach(m -> {
    			addNode(cg, m);
    			findOutAndAddNextNodes(m, h, cg);
    		});

    }

	/**
	 * Find all classes from which objects are created, beginning with method m.
	 * Similar to depth-first search.
	 */
	private void findClassesInstanciated(SootMethod m) {
		if (m.hasActiveBody()) {
			for (Unit unit : m.getActiveBody().getUnits()) {
				SootMethod calledMethod = findOutCalledMethodFromUnit(unit);
				if (calledMethod == null) {
					continue;
				}
				if (calledMethod.isConstructor()) {
					findInstanciatedClasses.add(calledMethod.getDeclaringClass());
				}
				findClassesInstanciated(calledMethod);
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
						if (findInstanciatedClasses.contains(childClass)) {
							for (SootMethod childMethod : childClass.getMethods()) {
								if (childMethod.getSubSignature().equals(rootMethod.getSubSignature())) {
									calledMethods.add(childMethod);
								}
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

	private void addNode(CallGraph cg, SootMethod node) {
		// if call graph doesn't consists of Node
		if (!cg.hasNode(node)) {
			cg.addNode(node);
		}
	}

}
