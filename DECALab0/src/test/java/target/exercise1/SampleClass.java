package target.exercise1;

public class SampleClass {

    Boolean randomMethod(){
        if(10>0)
            return true;
        return false;
    }

    public void instanceMethod(){
        // instance method
    }

    public static void staticMethod(){
        // static method
    }

    public void voidMethod(){
        int a = 3;
        int b = 4;
        int c = a + b;
    }

    public int intMethod(){
        int a = 0;
        return a;
    }

    public int paramIncrementMethod(int param1){
        return param1++;
    }

    public void branchingMethod(boolean param1){
        int a = 0;
        if(param1){
            a = 1;
        }
    }

    public void loopingMethod(){
        int i = 0;
        int max = 10;
        while(i<max){
            i++;
        }
    }

    public void targetMethod(boolean param1){
        ForbiddenClass fc = new ForbiddenClass();
        if(param1){
            fc.forbiddenMethod();
            fc.forbiddenMethod(0, "", false);
        }else{
            fc.forbiddenMethod(0);
            fc.notForbiddenMethod(0, "", false);
        }
    }

}
