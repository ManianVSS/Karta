package org.mvss.karta.framework.runtime.jython;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PySystemState;

@SuppressWarnings("unused")
public class JythonObjectFactory {
    private final Class<?> interfaceType;
    private final PyObject pyObjClass;

    // likely want to reuse PySystemState in some clever fashion since expensive to setup...
    public JythonObjectFactory(PySystemState state, Class<?> interfaceType, String moduleName, String className) {
        this.interfaceType = interfaceType;
        PyObject importer = state.getBuiltins().__getitem__(Py.newString("__import__"));
        PyObject module = importer.__call__(Py.newString(moduleName));
        pyObjClass = module.__getattr__(className);
        System.err.println("module=" + module + ", class=" + pyObjClass);
    }

    public JythonObjectFactory(Class<?> interfaceType, String moduleName, String className) {
        this(new PySystemState(), interfaceType, moduleName, className);
    }

    public Object createObject() {
        return pyObjClass.__call__().__tojava__(interfaceType);
    }

    public Object createObject(Object arg1) {
        return pyObjClass.__call__(Py.java2py(arg1)).__tojava__(interfaceType);
    }

    public Object createObject(Object arg1, Object arg2) {
        return pyObjClass.__call__(Py.java2py(arg1), Py.java2py(arg2)).__tojava__(interfaceType);
    }

    public Object createObject(Object arg1, Object arg2, Object arg3) {
        return pyObjClass.__call__(Py.java2py(arg1), Py.java2py(arg2), Py.java2py(arg3)).__tojava__(interfaceType);
    }

    public Object createObject(Object[] args, String[] keywords) {
        PyObject[] convertedArgs = new PyObject[args.length];
        for (int i = 0; i < args.length; i++) {
            convertedArgs[i] = Py.java2py(args[i]);
        }
        return pyObjClass.__call__(convertedArgs, keywords).__tojava__(interfaceType);
    }

    public Object createObject(Object... args) {
        return createObject(args, Py.NoKeywords);
    }

}