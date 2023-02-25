package target.exercise1;

public class ForbiddenClass {

    public void forbiddenMethod(int x, String y, boolean z){
        // actual forbidden method
    }

    public void forbiddenMethod(int x){
        // false forbidden method
    }

    public int forbiddenMethod(){
        // false forbidden method
        return 0;
    }

    public void notForbiddenMethod(int x, String y, boolean z){
        // false forbidden method
    }
}
