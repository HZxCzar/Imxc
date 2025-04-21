lexer grammar MxGrammerLexer;
Add: '+';
Sub: '-';
Mul: '*';
Div: '/';
Mod: '%';

Greater: '>';
Less: '<';
GreaterEqual: '>=';
LessEqual: '<=';
UnEqual: '!=';
Equal: '==';

LogicAnd: '&&';
LogicOr: '||';
LogicNot: '!';

RightShift: '>>';
LeftShift: '<<';
And: '&';
Or: '|';
Xor: '^';
Not: '~';

Assign: '=';

Selfadd: '++';
Selfsub: '--';

Member: '.';

Lbracket: '[';
Rbracket: ']';

LParen: '(';
RParen: ')';

Question: '?';
Colon: ':';

Semi: ';';
Comma: ',';
Lbrace: '{';
Rbrace: '}';

// Keywords
Void: 'void';
Bool: 'bool';
Int: 'int';
String: 'string';
New: 'new';
Class: 'class';
Null: 'null';
True: 'true';
False: 'false';
This: 'this';
If: 'if';
Else: 'else';
For: 'for';
While: 'while';
Break: 'break';
Continue: 'continue';
Return: 'return';

WhiteSpace: [ \t\r\n]+ -> skip;

// Comment
LineComment: '//' ~[\r\n]* -> skip;
BlockComment: '/*' .*? '*/' -> skip;

// Characters
Identifier: [a-zA-Z] [a-zA-Z_0-9]*; //注意长度限制64

//Integer
Integer: '0' | [1-9] [0-9]*;

//FomatString
FomatStringL: 'f"' Fomatstring* '$';
FomatStringR: '$' Fomatstring* '"';
FomatStringM: '$' Fomatstring* '$';
FStringLiteral: 'f"' ( Fomatstring)*? '"';
fragment Fomatstring: (
		'\\n' | '\\\\' | '\\"'|'$$'|~["$] 
	) ( '\\n' | '\\\\' | '\\"'|'$$'|~["$] )*;

//String
StringLiteral: '"' (Stringchar)*? '"';
fragment Stringchar: '\\n' | '\\\\' | '\\"' | [ -~];