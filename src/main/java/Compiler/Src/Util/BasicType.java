package Compiler.Src.Util;

import java.util.ArrayList;
import java.util.Arrays;

import Compiler.Src.IR.Type.IRType;
import Compiler.Src.Util.Info.*;
import Compiler.Src.Util.ScopeUtil.GlobalScope;
import Compiler.Src.IR.Node.Def.IRFuncDef;

public interface BasicType {

        // BaseType
        TypeInfo voidType = new TypeInfo("void", 0);
        TypeInfo intType = new TypeInfo("int", 0);
        TypeInfo boolType = new TypeInfo("bool", 0);
        TypeInfo stringType = new TypeInfo("string", 0);
        TypeInfo nullType = new TypeInfo("null", 0);
        TypeInfo thisType = new TypeInfo("this", 0);
        // TypeInfo fstringType = new TypeInfo("fstring", 0);
        // TypeInfo constarrayType = new TypeInfo("constarray", 0);

        // BaseFunc
        FuncInfo printFunc = new FuncInfo("print", voidType, stringType);
        FuncInfo printlnFunc = new FuncInfo("println", voidType, stringType);
        FuncInfo printIntFunc = new FuncInfo("printInt", voidType, intType);
        FuncInfo printlnIntFunc = new FuncInfo("printlnInt", voidType, intType);
        FuncInfo getStringFunc = new FuncInfo("getString", stringType);
        FuncInfo getIntFunc = new FuncInfo("getInt", intType);
        FuncInfo toStringFunc = new FuncInfo("toString", stringType, intType);

        FuncInfo arraySize = new FuncInfo("size", intType);
        FuncInfo stringLengthFunc = new FuncInfo("length", intType);
        FuncInfo stringSubstringFunc = new FuncInfo("substring", stringType, intType, intType);
        FuncInfo stringParseintFunc = new FuncInfo("parseInt", intType);
        FuncInfo stringOrdFunc = new FuncInfo("ord", intType, intType);

        FuncInfo[] BaseFuncs = { printFunc, printlnFunc, printIntFunc, printlnIntFunc, getStringFunc, getIntFunc,
                        toStringFunc };

        // BaseClass
        ClassInfo intClass = new ClassInfo("int");
        ClassInfo boolClass = new ClassInfo("bool");
        ClassInfo stringClass = new ClassInfo("string", stringLengthFunc, stringSubstringFunc, stringParseintFunc,
                        stringOrdFunc);

        ClassInfo[] BaseClasses = { intClass, boolClass, stringClass };

        // IR
        IRType irVoidType = new IRType(GlobalScope.voidType);
        IRType irIntType = new IRType(GlobalScope.intType);
        IRType irBoolType = new IRType(GlobalScope.boolType);
        IRType irPtrType = new IRType("ptr");

        IRFuncDef irPrintFunc = new IRFuncDef("print", irVoidType, new ArrayList<IRType>(Arrays.asList(irPtrType)));
        IRFuncDef irPrintlnFunc = new IRFuncDef("println", irVoidType, new ArrayList<IRType>(Arrays.asList(irPtrType)));
        IRFuncDef irPrintIntFunc = new IRFuncDef("printInt", irVoidType,
                        new ArrayList<IRType>(Arrays.asList(irIntType)));
        IRFuncDef irPrintlnIntFunc = new IRFuncDef("printlnInt", irVoidType,
                        new ArrayList<IRType>(Arrays.asList(irIntType)));
        IRFuncDef irGetStringFunc = new IRFuncDef("getString", irPtrType, new ArrayList<IRType>());
        IRFuncDef irGetIntFunc = new IRFuncDef("getInt", irIntType, new ArrayList<IRType>());
        IRFuncDef irToStringFunc = new IRFuncDef("toString", irPtrType,
                        new ArrayList<IRType>(Arrays.asList(irIntType)));
        IRFuncDef irBooltoStringFunc = new IRFuncDef("Bool_string.toString", irPtrType,
                        new ArrayList<IRType>(Arrays.asList(irBoolType)));
        IRFuncDef irMallocFunc = new IRFuncDef("_malloc", irPtrType, new ArrayList<IRType>(Arrays.asList(irIntType)));
        IRFuncDef irMallocArrayFunc = new IRFuncDef("__malloc_array", irPtrType,
                        new ArrayList<IRType>(Arrays.asList(irIntType, irIntType)));
        IRFuncDef irArraySizeFunc = new IRFuncDef("__builtin_array.size", irIntType,
                        new ArrayList<IRType>(Arrays.asList(irPtrType)));
        IRFuncDef irStringLengthFunc = new IRFuncDef("__string.length", irIntType,
                        new ArrayList<IRType>(Arrays.asList(irPtrType)));
        IRFuncDef irStringSubstringFunc = new IRFuncDef("__string.substring", irPtrType,
                        new ArrayList<IRType>(Arrays.asList(irPtrType, irIntType, irIntType)));
        IRFuncDef irStringParseintFunc = new IRFuncDef("__string.parseInt", irIntType,
                        new ArrayList<IRType>(Arrays.asList(irPtrType)));
        IRFuncDef irStringOrdFunc = new IRFuncDef("__string.ord", irIntType,
                        new ArrayList<IRType>(Arrays.asList(irPtrType, irIntType)));
        IRFuncDef irStringCompareFunc = new IRFuncDef("__string.compare", irIntType,
                        new ArrayList<IRType>(Arrays.asList(irPtrType, irPtrType)));
        IRFuncDef irStringConcatFunc = new IRFuncDef("__string.concat", irPtrType,
                        new ArrayList<IRType>(Arrays.asList(irPtrType, irPtrType)));
        IRFuncDef irStringCopyFunc = new IRFuncDef("__string.copy", irVoidType,
                        new ArrayList<IRType>(Arrays.asList(irPtrType, irPtrType)));

        ArrayList<IRFuncDef> irBuiltInFuncs = new ArrayList<>(Arrays.asList(
                        irPrintFunc, irPrintlnFunc, irPrintIntFunc, irPrintlnIntFunc,
                        irGetStringFunc, irGetIntFunc, irToStringFunc, irBooltoStringFunc, irMallocFunc, irMallocArrayFunc, irArraySizeFunc,
                        irStringLengthFunc,
                        irStringSubstringFunc, irStringParseintFunc, irStringOrdFunc, irStringCompareFunc,
                        irStringConcatFunc,
                        irStringCopyFunc));

        // IRFuncDef irPrintFunc = new IRFuncDef("print", irVoidType, new
        // ArrayList<IRType>(Arrays.asList(irPtrType)));
        // IRFuncDef irPrintlnFunc = new IRFuncDef("println", irVoidType, new
        // ArrayList<IRType>(Arrays.asList(irPtrType)));
        // IRFuncDef irPrintIntFunc = new IRFuncDef("printInt", irVoidType, new
        // ArrayList<IRType>(Arrays.asList(irIntType)));
        // IRFuncDef irPrintlnIntFunc = new IRFuncDef("printlnInt", irVoidType, new
        // ArrayList<IRType>(Arrays.asList(irIntType)));
        // IRFuncDef irGetStringFunc = new IRFuncDef("getString", irPtrType, new
        // ArrayList<IRType>());
        // IRFuncDef irGetIntFunc = new IRFuncDef("getInt", irIntType, new
        // ArrayList<IRType>());
        // IRFuncDef irToStringFunc = new IRFuncDef("toString", irPtrType, new
        // ArrayList<IRType>(Arrays.asList(irIntType)));
        // IRFuncDef irMallocFunc = new IRFuncDef("malloc", irPtrType, new
        // ArrayList<IRType>(Arrays.asList(irIntType)));
        // IRFuncDef irArraySizeFunc = new IRFuncDef("__builtin_array_size", irIntType,
        // new ArrayList<IRType>(Arrays.asList(irPtrType)));
        // IRFuncDef irStringLengthFunc = new IRFuncDef("__string_length", irIntType,
        // new ArrayList<IRType>(Arrays.asList(irPtrType)));
        // IRFuncDef irStringSubstringFunc = new IRFuncDef("__string_substring",
        // irPtrType,
        // new ArrayList<IRType>(irPtrType, irIntType, irIntType));
        // IRFuncDef irStringParseintFunc = new IRFuncDef("__string_parseInt",
        // irIntType,
        // new Array<>(irPtrType));
        // IRFuncDef irStringOrdFunc = new IRFuncDef("__string_ord", irIntType,
        // new Array<>(irPtrType, irIntType));
        // IRFuncDef irStringCompareFunc = new IRFuncDef("__string_compare", irIntType,
        // new Array<>(irPtrType, irPtrType));
        // IRFuncDef irStringConcatFunc = new IRFuncDef("__string_concat", irPtrType,
        // new Array<>(irPtrType, irPtrType));
        // IRFuncDef irStringCopyFunc = new IRFuncDef("__string_copy", irVoidType,
        // new Array<>(irPtrType, irPtrType));
        // Array<IRFuncDef> irBuiltInFuncs = new Array<>(irPrintFunc, irPrintlnFunc,
        // irPrintIntFunc, irPrintlnIntFunc,
        // irGetStringFunc, irGetIntFunc, irToStringFunc, irMallocFunc, irArraySizeFunc,
        // irStringLengthFunc,
        // irStringSubstringFunc, irStringParseintFunc, irStringOrdFunc,
        // irStringCompareFunc, irStringConcatFunc,
        // irStringCopyFunc );
}
