grammar Mx;
import MxGrammerLexer;
// @header {package Compiler.Src.Grammer;}
program: (funDef | classDef | varDef)* EOF;
type: Int | Bool | String | Void | Identifier;

classDef:
	Class Identifier Lbrace (varDef | classBuild | funDef)* Rbrace Semi;
typeVarDef: type (arrayUnit)*;
varDef: typeVarDef atomVarDef (',' atomVarDef)* Semi;
atomVarDef: Identifier ('=' expression)?;
classBuild: Identifier LParen RParen blockstatement;
blockstatement: Lbrace statement* Rbrace;
funDef:
	typeVarDef Identifier LParen funParaList? RParen blockstatement;
funVarDef: typeVarDef atomVarDef;
funParaList: funVarDef (',' funVarDef)*;

statement:
	blockstatement
	| varDef
	| ifstatement
	| whilestatement
	| forstatement
	| returnstatement
	| breakstatement
	| continuestatement
	| expressionstatement
	| emptystatement;

emptystatement: Semi;

ifstatement:
	If LParen expression RParen statement (Else ( statement))?;
whilestatement: While LParen expression RParen statement;
forinit: (expression? Semi) | varDef;
forstatement:
	For LParen forinit condition = expression? Semi update = expression? RParen statement;

returnstatement: Return expression? Semi;
breakstatement: Break Semi;
continuestatement: Continue Semi;
expressionstatement: expression (Comma expression)* Semi;

constarray: Lbrace (expression)? (Comma expression)* Rbrace;
arrayUnit: Lbracket (expression)? Rbracket;

callArgs: expression (Comma expression)*;

expression:
	New type (arrayUnit)* (LParen RParen)? constarray?	# newExpr
	| LParen expression RParen							# parenExpr
	| expression LParen callArgs? RParen				# callExpr
	| expression op = Member atom						# memberExpr
	| expression arrayUnit								# arrayExpr
	| expression op = (Selfadd | Selfsub)				# unaryExpr
	| <assoc = right>op = (
		Selfadd
		| Selfsub
		| Not
		| LogicNot
		| Sub
	) expression # preunaryExpr

	//BinaryExpr
	| expression op = (Mul | Div | Mod) expression								# binaryExpr
	| expression op = (Add | Sub) expression									# binaryExpr
	| expression op = (LeftShift | RightShift) expression						# binaryExpr
	| expression op = (Less | Greater | LessEqual | GreaterEqual) expression	# binaryExpr
	| expression op = (Equal | UnEqual) expression								# binaryExpr
	| expression op = (And | Xor | Or) expression								# binaryExpr
	| expression op = (LogicAnd | LogicOr) expression							# binaryExpr
	| <assoc = right> expression Question expression Colon expression			# conditionalExpr

	//AssignExpr
	| <assoc = right> expression op = Assign expression # assignExpr

	//AtomExpr
	| atom # atomExpr;

atom:
	Integer
	| True
	| False
	| This
	| Null
	| Identifier
	| StringLiteral
	| fstring
	| constarray;

fstring:
	(FomatStringL expression midfstringUnit* FomatStringR)
	| basestring = FStringLiteral;

midfstringUnit: FomatStringM expression;