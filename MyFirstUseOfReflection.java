package reflection.api;
import java.lang.reflect.*;
import java.net.InterfaceAddress;
import java.util.*;


public class MyFirstUseOfReflection implements Investigator{

    //private Class reflectClass;
    private Object objectInstance;

    @Override
    public void load(Object anInstanceOfSomething) {
        //reflectClass= anInstanceOfSomething.getClass();
        objectInstance = anInstanceOfSomething;
    }

    @Override
    public int getTotalNumberOfMethods(){
        //return all methods of the current class with all access modifiers
        return objectInstance.getClass().getDeclaredMethods().length;
    }

    @Override
    public int getTotalNumberOfConstructors(){
        return objectInstance.getClass().getDeclaredConstructors().length;
    }

    @Override
    public int getTotalNumberOfFields(){
        return objectInstance.getClass().getDeclaredFields().length;
    }

    @Override
    public Set<String> getAllImplementedInterfaces() {
        Set<String> res = new HashSet<>();
        Class[] interfaces = objectInstance.getClass().getInterfaces();
        for (Class c : interfaces)
            res.add(c.getSimpleName());

        return res;
    }

    @Override
    public int getCountOfConstantFields() {
        int numOfFinalMembers = 0;
        Field[] declaredFields = objectInstance.getClass().getDeclaredFields();
        for (Field field : declaredFields)
            if (Modifier.isFinal(field.getModifiers()))
                ++numOfFinalMembers;

        return numOfFinalMembers;
    }

    @Override
    public int getCountOfStaticMethods() {
        int numOfStaticMethods = 0;
        Method[] declaredMethods = objectInstance.getClass().getDeclaredMethods();
        for (Method method : declaredMethods)
            if (Modifier.isStatic(method.getModifiers()))
                ++numOfStaticMethods;

        return numOfStaticMethods;
    }

    @Override
    public boolean isExtending(){
        //if my name is not object then i extends and return true else return false (because i extends)
        return (objectInstance.getClass().getSuperclass().getSimpleName().compareTo(Object.class.getSimpleName()) != 0);
    }

    @Override
    public String getParentClassSimpleName() {
        if(objectInstance.getClass().getSuperclass() != null)
            return objectInstance.getClass().getSuperclass().getSimpleName();
        return null;
    }

    @Override
    public boolean isParentClassAbstract(){
        if(isExtending())
            return Modifier.isAbstract(objectInstance.getClass().getSuperclass().getModifiers());
        //because object is not abstract
        return false;
    }

    @Override
    public Set<String> getNamesOfAllFieldsIncludingInheritanceChain() {
        Set<String> res = new HashSet<>();
        Class currentClass = objectInstance.getClass();

        while(currentClass != null){
            Field[] fields = currentClass.getDeclaredFields();
            for(Field field : fields){
                res.add(field.getName());
            }
            currentClass = currentClass.getSuperclass();
        }
        return res;
    }

   @Override
    public int invokeMethodThatReturnsInt(String methodName, Object... args) {
       int numOfCorrectParams = 0;
       int res = 0;
       try {
           Method[] allMethods = objectInstance.getClass().getDeclaredMethods();
           for (Method method : allMethods) {
               //if the function name is valid and appears in the class + the return value is int as requested
               if (method.getName().compareTo(methodName) == 0 && method.getGenericReturnType().equals(int.class)) {
                   //extract all the current function parameters
                   Type[] currentMethodParameters = method.getGenericParameterTypes();
                   //make sure that the given args is at the same size as the method parameters (length perspective);
                   if (args.length == currentMethodParameters.length) {
                       numOfCorrectParams = 0;
                       //compare each one of the parameters to check if the args fits correctly to the method parameters
                       for (int i = 0; i < args.length; ++i) {
                           if (args[i].getClass().getTypeName().compareTo(currentMethodParameters[i].getTypeName()) == 0)
                               numOfCorrectParams++;
                       }
                       if (numOfCorrectParams == args.length) {
                           try {
                               //handle the case if the method is private
                               method.setAccessible(true);
                               int methodModifier = method.getModifiers();
                               if (Modifier.isStatic(methodModifier))
                                   //i saw in some places that for static methods we need to invoke with null
                                   res = (int) method.invoke(null, args);
                               else
                                   res = (int) method.invoke(objectInstance, args);
                           } catch (IllegalAccessException e) {
                               System.out.println("Currently executing method does not have access to the definition of the specified class, field, method or constructor.");
                           } catch (InvocationTargetException e) {
                               System.out.println("Error invoking the function.");
                           }
                       }
                   }
               }
           }
       } catch (SecurityException e) {
           System.out.println("Security violation.");
       }

       return res;
   }

    @Override
    public Object createInstance(int numberOfArgs, Object... args) {
        int numOfCorrectParams = 0;
        Object res = null;
        Constructor[] constructors = objectInstance.getClass().getDeclaredConstructors();
        for (Constructor ctor : constructors) {
            Type[] currentCtorParameters = ctor.getGenericParameterTypes();
            if (ctor.getParameterCount() == numberOfArgs) {
                ctor.setAccessible(true);
                try {
                    ctor.setAccessible(true);
                    res = ctor.newInstance(args);
                } catch (InstantiationException e) {
                    System.out.println("The specified class object cannot be instantiated.");
                } catch (IllegalAccessException e) {
                    System.out.println("Currently executing method does not have access to the definition of the specified class, field, method or constructor.");
                } catch (InvocationTargetException e) {
                    System.out.println("Error invoking the constructor.");
                }
            }
        }
        return res;
    }

    @Override
    public Object elevateMethodAndInvoke(String name, Class<?>[] parametersTypes, Object... args) {
        if (objectInstance == null)
            return null;

        Object returnValue = null;
        Method[] allMethods = objectInstance.getClass().getDeclaredMethods();
        //Set<Type> currentMethodTypesOfParametersSet = new HashSet<>();
        int numOfMatchesParameters = 0;
        int numOfMatchesTypes = 0;

        for (Method method : allMethods) {
            //if its the correct name
            if (method.getName().compareTo(name) == 0) {
                //check if the function get the same parameters using the given "parametersTypes"
                Type[] currentMethodParameters = method.getGenericParameterTypes();
                if (currentMethodParameters.length == args.length) {
                    int methodModifier = method.getModifiers();
                    if (Modifier.isPrivate(methodModifier))
                        method.setAccessible(true);
                    try {
                        if (Modifier.isStatic(methodModifier))
                            //i saw in some places that for static methods we need to invoke with null
                            returnValue = (int) method.invoke(null, args);
                        else
                            returnValue = (int) method.invoke(objectInstance, args);
                    } catch (IllegalAccessException e) {
                        System.out.println("Currently executing method does not have access to the definition of the specified class, field, method or constructor.");
                    } catch (InvocationTargetException e) {
                        System.out.println("Error invoking the constructor.");
                    }
                }
            }
        }

        return returnValue;
    }

    @Override
    public String getInheritanceChain(String delimiter){
        Class currentClass = objectInstance.getClass();
        StringBuffer sb = new StringBuffer();
        ArrayList<String> listOfInheritence= new ArrayList<String>();
        listOfInheritence.add(currentClass.getSimpleName());
        currentClass = currentClass.getSuperclass();
        while(currentClass != null){
            listOfInheritence.add(currentClass.getSimpleName());
            currentClass = currentClass.getSuperclass();
        }
        Collections.reverse(listOfInheritence);
        for(String str : listOfInheritence){
            if(str.compareTo(objectInstance.getClass().getSimpleName()) != 0)
                sb.append(str + delimiter);
            else
                sb.append(str);
        }

        return sb.toString();
    }
}
