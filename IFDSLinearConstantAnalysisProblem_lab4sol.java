package analysis;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import heros.DefaultSeeds;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Identity;
import heros.solver.Pair;
import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.internal.*;
import soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem;

public class IFDSLinearConstantAnalysisProblem extends DefaultJimpleIFDSTabulationProblem<Pair<Local, Integer>, InterproceduralCFG<Unit, SootMethod>> {

    protected final static int LOWER_BOUND = -1000;

    protected final static int UPPER_BOUND = 1000;

    private Map<Unit, Set<Pair<Local, Integer>>> unitPairs;

    protected InterproceduralCFG<Unit, SootMethod> icfg;

    public IFDSLinearConstantAnalysisProblem(InterproceduralCFG<Unit, SootMethod> icfg) {
        super(icfg);
        this.icfg = icfg;
        unitPairs = initialSeeds();
    }

    @Override
    public Map<Unit, Set<Pair<Local, Integer>>> initialSeeds() {
        for (SootClass c : Scene.v().getApplicationClasses()) {
            for (SootMethod m : c.getMethods()) {
                if (!m.hasActiveBody()) {
                    continue;
                }
                if (m.getName().equals("entryPoint")) {
                    return DefaultSeeds.make(Collections.singleton(m.getActiveBody().getUnits().getFirst()), zeroValue());
                }
            }
        }
        throw new IllegalStateException("scene does not contain 'entryPoint'");
    }

    // TODO: You have to implement the FlowFunctions interface.
    // Use Pair<Local, Integer> as type for the data-flow facts.
    @Override
    protected FlowFunctions<Unit, Pair<Local, Integer>, SootMethod> createFlowFunctionsFactory() {
        return new FlowFunctions<Unit, Pair<Local, Integer>, SootMethod>() {


            private synchronized void addNewPair(Set<Pair<Local, Integer>> allPairs, Local local, int result) {
                if (result >= LOWER_BOUND && result <= UPPER_BOUND) {
                    Pair<Local, Integer> newPair = new Pair<Local, Integer>(local, result);
                    allPairs.add(newPair);
                }
                if (result >= UPPER_BOUND) {
                    Pair<Local, Integer> newPair = new Pair<Local, Integer>(local, UPPER_BOUND);
                    allPairs.add(newPair);

                }
                if (result <= LOWER_BOUND) {
                    Pair<Local, Integer> newPair = new Pair<Local, Integer>(local, UPPER_BOUND);
                    allPairs.add(newPair);

                }
            }

            private synchronized void addItemsToUnit(Unit unit, Set<Pair<Local, Integer>> finalSet) {
                if (unitPairs.containsKey(unit)) {
                    Set<Pair<Local, Integer>> newSet = unitPairs.get(unit);
                    finalSet.addAll(newSet);
                }
                unitPairs.put(unit, finalSet);
            }

            private Set<Pair<Local, Integer>> getDataFlowFacts(Unit unit) {
                Set<Pair<Local, Integer>> set = unitPairs.get(unit);
                if (set != null) {
                    return unitPairs.get(unit);
                } else {
                    return new CopyOnWriteArraySet<>();
                }
            }

            private synchronized List<Integer> getDataFlowValue(Unit unit, Local local) {
                Set<Pair<Local, Integer>> set = getDataFlowFacts(unit);
                List<Integer> integerSet = new ArrayList<>();
                for (Pair<Local, Integer> s : set) {
                    if (Objects.equals(s.getO1().getName(), local.getName())) {
                        integerSet.add(s.getO2());
                    }
                }
                return integerSet;
            }

            private synchronized List<Integer> calculateExpression(AbstractJimpleFloatBinopExpr expression, Unit unit) {
                Value var1 = (expression).getOp1();
                Value var2 = (expression).getOp2();
                int var1Value = 0;
                int var2Value = 0;
                List<Integer> valueList1 = new ArrayList<>();
                List<Integer> valueList2 = null;
                List<Integer> resultList = new ArrayList<>();
                int result = 0;
                if (var1 instanceof Local) {
                    valueList1 = getDataFlowValue(unit, (Local) var1);

                } else {
                    if (var1 instanceof IntConstant)
                        var1Value = ((IntConstant) var1).value;
                }

                if (var2 instanceof IntConstant)
                    var2Value = ((IntConstant) var2).value;


                if (valueList1.size() > 0) {
                    for (Integer val : valueList1) {
                        if (expression instanceof JMulExpr) {
                            result = val * var2Value;
                        }
                        if (expression instanceof JAddExpr) {
                            result = val + var2Value;
                        }
                        resultList.add(result);
                    }
                } else {
                    if (expression instanceof JMulExpr) {
                        result = var1Value * var2Value;
                    } else if (expression instanceof JAddExpr) {
                        result = var1Value + var2Value;
                    }
                    resultList.add(result);
                }
                return resultList;
            }


            @Override
            public FlowFunction<Pair<Local, Integer>> getNormalFlowFunction(Unit curr, Unit next) {
                // TODO: Implement this flow function factory to obtain an intra-procedural data-flow analysis.
                FlowFunction<Pair<Local, Integer>> flowFunction = new FlowFunction<Pair<Local, Integer>>() {
                    @Override
                    public Set<Pair<Local, Integer>> computeTargets(Pair<Local, Integer> localIntegerPair) {
                        Set<Pair<Local, Integer>> finalSet = new CopyOnWriteArraySet<>();
                        finalSet.addAll(getDataFlowFacts(curr));

                        if (curr instanceof JAssignStmt) {
                            Value rightOP = ((JAssignStmt) curr).getRightOp();
                            Value leftOP = ((JAssignStmt) curr).getLeftOp();
                            if (leftOP instanceof Local) {
                                if (rightOP instanceof IntConstant) {
                                    int result = ((IntConstant) rightOP).value;
                                    addNewPair(finalSet, (Local) leftOP, result);
                                }
                                if (rightOP instanceof AbstractJimpleFloatBinopExpr) {
                                    List<Integer> result = calculateExpression((AbstractJimpleFloatBinopExpr) rightOP, curr);

                                    for (Integer res : result) {
                                        addNewPair(finalSet, (Local) leftOP, res);

                                    }
                                }
                            }
                        }
                        addItemsToUnit(next, finalSet);
                        return finalSet;

                    }
                };
                return flowFunction;
            }

            @Override
            public FlowFunction<Pair<Local, Integer>> getCallFlowFunction(Unit callsite, SootMethod dest) {
                Set<Pair<Local, Integer>> finalSet = new CopyOnWriteArraySet<>();
                finalSet.add(createZeroValue());

                FlowFunction<Pair<Local, Integer>> flowFunction = new FlowFunction<Pair<Local, Integer>>() {
                    @Override
                    public Set<Pair<Local, Integer>> computeTargets(Pair<Local, Integer> localIntegerPair) {
                        Unit next = dest.getActiveBody().getUnits().getFirst();
                        if (callsite instanceof JAssignStmt) {

                            if (((JAssignStmt) callsite).containsInvokeExpr()) {
                                Value right = ((JAssignStmt) callsite).getRightOp();
                                if (right instanceof JVirtualInvokeExpr) {
                                    List<Value> sentArgs = ((JVirtualInvokeExpr) right).getArgs();
                                    for (int i = 0; i < sentArgs.size(); i++) {
                                        Local localVar = dest.getActiveBody().getParameterLocal(i);
                                        Value sent_var = sentArgs.get(i);
                                        int sentConstant = 0;
                                        if (sent_var instanceof Local) {
                                            List<Integer> sentConsts = getDataFlowValue(callsite, (Local) sent_var);
                                            for (Integer cons : sentConsts) {
                                                addNewPair(finalSet, localVar, cons);
                                            }
                                        } else {
                                            sentConstant = ((IntConstant) sent_var).value;
                                            addNewPair(finalSet, localVar, sentConstant);

                                        }

                                    }
                                }
                            }

                        }
                        addItemsToUnit(next, finalSet);
                        return finalSet;
                    }
                };
                return flowFunction;
            }

            @Override
            public FlowFunction<Pair<Local, Integer>> getReturnFlowFunction(Unit callsite, SootMethod callee, Unit
                    exit, Unit retsite) {
                // TODO: Map the return value back into the caller's context if applicable.
                // Since Java has pass-by-value semantics for primitive data types, you do not have to map the formals
                // back to the actuals at the exit of the callee.
                Set<Pair<Local, Integer>> finalSet = new CopyOnWriteArraySet<>();
                FlowFunction<Pair<Local, Integer>> flowFunction = new FlowFunction<Pair<Local, Integer>>() {
                    @Override
                    public Set<Pair<Local, Integer>> computeTargets(Pair<Local, Integer> localIntegerPair) {
                        if (exit instanceof JReturnStmt) {
                            Value formalValue = ((JReturnStmt) exit).getOp();

                            if (formalValue instanceof Local) {
                                Local actualVar = (Local) ((JAssignStmt) callsite).getLeftOp();
                                List<Integer> formalConst = getDataFlowValue(exit, (Local) formalValue);
                                for (Integer cons : formalConst) {
                                    addNewPair(finalSet, actualVar, cons);
                                }
                            } else if (formalValue instanceof IntConstant) {
                                Local actualVar = (Local) ((JAssignStmt) callsite).getLeftOp();
                                addNewPair(finalSet, actualVar, ((IntConstant) formalValue).value);
                            }
                        }
                        Set<Pair<Local, Integer>> newSet = unitPairs.get(retsite);
                        if (newSet == null) {
                            newSet = new CopyOnWriteArraySet<>();
                        }
                        newSet.addAll(getDataFlowFacts(exit));
                        newSet.addAll(finalSet);
                        addItemsToUnit(retsite, newSet);
                        return newSet;
                    }
                };
                return flowFunction;
            }

            @Override
            public FlowFunction<Pair<Local, Integer>> getCallToReturnFlowFunction(Unit callsite, Unit retsite) {
                // TODO: getCallToReturnFlowFunction can be left to return id in many analysis; this time as well?

                return Identity.v();
            }
        };
    }

    @Override
    protected Pair<Local, Integer> createZeroValue() {
        return new Pair<>(new JimpleLocal("<<zero>>", NullType.v()), Integer.MIN_VALUE);
    }


}