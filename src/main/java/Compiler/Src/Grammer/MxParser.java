// Generated from Mx.g4 by ANTLR 4.13.1
package Compiler.Src.Grammer;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class MxParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		Add=1, Sub=2, Mul=3, Div=4, Mod=5, Greater=6, Less=7, GreaterEqual=8, 
		LessEqual=9, UnEqual=10, Equal=11, LogicAnd=12, LogicOr=13, LogicNot=14, 
		RightShift=15, LeftShift=16, And=17, Or=18, Xor=19, Not=20, Assign=21, 
		Selfadd=22, Selfsub=23, Member=24, Lbracket=25, Rbracket=26, LParen=27, 
		RParen=28, Question=29, Colon=30, Semi=31, Comma=32, Lbrace=33, Rbrace=34, 
		Void=35, Bool=36, Int=37, String=38, New=39, Class=40, Null=41, True=42, 
		False=43, This=44, If=45, Else=46, For=47, While=48, Break=49, Continue=50, 
		Return=51, WhiteSpace=52, LineComment=53, BlockComment=54, Identifier=55, 
		Integer=56, FomatStringL=57, FomatStringR=58, FomatStringM=59, FStringLiteral=60, 
		StringLiteral=61;
	public static final int
		RULE_program = 0, RULE_type = 1, RULE_classDef = 2, RULE_typeVarDef = 3, 
		RULE_varDef = 4, RULE_atomVarDef = 5, RULE_classBuild = 6, RULE_blockstatement = 7, 
		RULE_funDef = 8, RULE_funVarDef = 9, RULE_funParaList = 10, RULE_statement = 11, 
		RULE_emptystatement = 12, RULE_ifstatement = 13, RULE_whilestatement = 14, 
		RULE_forinit = 15, RULE_forstatement = 16, RULE_returnstatement = 17, 
		RULE_breakstatement = 18, RULE_continuestatement = 19, RULE_expressionstatement = 20, 
		RULE_constarray = 21, RULE_arrayUnit = 22, RULE_callArgs = 23, RULE_expression = 24, 
		RULE_atom = 25, RULE_fstring = 26, RULE_midfstringUnit = 27;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "type", "classDef", "typeVarDef", "varDef", "atomVarDef", 
			"classBuild", "blockstatement", "funDef", "funVarDef", "funParaList", 
			"statement", "emptystatement", "ifstatement", "whilestatement", "forinit", 
			"forstatement", "returnstatement", "breakstatement", "continuestatement", 
			"expressionstatement", "constarray", "arrayUnit", "callArgs", "expression", 
			"atom", "fstring", "midfstringUnit"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'+'", "'-'", "'*'", "'/'", "'%'", "'>'", "'<'", "'>='", "'<='", 
			"'!='", "'=='", "'&&'", "'||'", "'!'", "'>>'", "'<<'", "'&'", "'|'", 
			"'^'", "'~'", "'='", "'++'", "'--'", "'.'", "'['", "']'", "'('", "')'", 
			"'?'", "':'", "';'", "','", "'{'", "'}'", "'void'", "'bool'", "'int'", 
			"'string'", "'new'", "'class'", "'null'", "'true'", "'false'", "'this'", 
			"'if'", "'else'", "'for'", "'while'", "'break'", "'continue'", "'return'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "Add", "Sub", "Mul", "Div", "Mod", "Greater", "Less", "GreaterEqual", 
			"LessEqual", "UnEqual", "Equal", "LogicAnd", "LogicOr", "LogicNot", "RightShift", 
			"LeftShift", "And", "Or", "Xor", "Not", "Assign", "Selfadd", "Selfsub", 
			"Member", "Lbracket", "Rbracket", "LParen", "RParen", "Question", "Colon", 
			"Semi", "Comma", "Lbrace", "Rbrace", "Void", "Bool", "Int", "String", 
			"New", "Class", "Null", "True", "False", "This", "If", "Else", "For", 
			"While", "Break", "Continue", "Return", "WhiteSpace", "LineComment", 
			"BlockComment", "Identifier", "Integer", "FomatStringL", "FomatStringR", 
			"FomatStringM", "FStringLiteral", "StringLiteral"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Mx.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public MxParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(MxParser.EOF, 0); }
		public List<FunDefContext> funDef() {
			return getRuleContexts(FunDefContext.class);
		}
		public FunDefContext funDef(int i) {
			return getRuleContext(FunDefContext.class,i);
		}
		public List<ClassDefContext> classDef() {
			return getRuleContexts(ClassDefContext.class);
		}
		public ClassDefContext classDef(int i) {
			return getRuleContext(ClassDefContext.class,i);
		}
		public List<VarDefContext> varDef() {
			return getRuleContexts(VarDefContext.class);
		}
		public VarDefContext varDef(int i) {
			return getRuleContext(VarDefContext.class,i);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitProgram(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(61);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 36030411926667264L) != 0)) {
				{
				setState(59);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(56);
					funDef();
					}
					break;
				case 2:
					{
					setState(57);
					classDef();
					}
					break;
				case 3:
					{
					setState(58);
					varDef();
					}
					break;
				}
				}
				setState(63);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(64);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeContext extends ParserRuleContext {
		public TerminalNode Int() { return getToken(MxParser.Int, 0); }
		public TerminalNode Bool() { return getToken(MxParser.Bool, 0); }
		public TerminalNode String() { return getToken(MxParser.String, 0); }
		public TerminalNode Void() { return getToken(MxParser.Void, 0); }
		public TerminalNode Identifier() { return getToken(MxParser.Identifier, 0); }
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 36029312415039488L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassDefContext extends ParserRuleContext {
		public TerminalNode Class() { return getToken(MxParser.Class, 0); }
		public TerminalNode Identifier() { return getToken(MxParser.Identifier, 0); }
		public TerminalNode Lbrace() { return getToken(MxParser.Lbrace, 0); }
		public TerminalNode Rbrace() { return getToken(MxParser.Rbrace, 0); }
		public TerminalNode Semi() { return getToken(MxParser.Semi, 0); }
		public List<VarDefContext> varDef() {
			return getRuleContexts(VarDefContext.class);
		}
		public VarDefContext varDef(int i) {
			return getRuleContext(VarDefContext.class,i);
		}
		public List<ClassBuildContext> classBuild() {
			return getRuleContexts(ClassBuildContext.class);
		}
		public ClassBuildContext classBuild(int i) {
			return getRuleContext(ClassBuildContext.class,i);
		}
		public List<FunDefContext> funDef() {
			return getRuleContexts(FunDefContext.class);
		}
		public FunDefContext funDef(int i) {
			return getRuleContext(FunDefContext.class,i);
		}
		public ClassDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classDef; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitClassDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassDefContext classDef() throws RecognitionException {
		ClassDefContext _localctx = new ClassDefContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_classDef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(68);
			match(Class);
			setState(69);
			match(Identifier);
			setState(70);
			match(Lbrace);
			setState(76);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 36029312415039488L) != 0)) {
				{
				setState(74);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
				case 1:
					{
					setState(71);
					varDef();
					}
					break;
				case 2:
					{
					setState(72);
					classBuild();
					}
					break;
				case 3:
					{
					setState(73);
					funDef();
					}
					break;
				}
				}
				setState(78);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(79);
			match(Rbrace);
			setState(80);
			match(Semi);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeVarDefContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public List<ArrayUnitContext> arrayUnit() {
			return getRuleContexts(ArrayUnitContext.class);
		}
		public ArrayUnitContext arrayUnit(int i) {
			return getRuleContext(ArrayUnitContext.class,i);
		}
		public TypeVarDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeVarDef; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitTypeVarDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeVarDefContext typeVarDef() throws RecognitionException {
		TypeVarDefContext _localctx = new TypeVarDefContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_typeVarDef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(82);
			type();
			setState(86);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Lbracket) {
				{
				{
				setState(83);
				arrayUnit();
				}
				}
				setState(88);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VarDefContext extends ParserRuleContext {
		public TypeVarDefContext typeVarDef() {
			return getRuleContext(TypeVarDefContext.class,0);
		}
		public List<AtomVarDefContext> atomVarDef() {
			return getRuleContexts(AtomVarDefContext.class);
		}
		public AtomVarDefContext atomVarDef(int i) {
			return getRuleContext(AtomVarDefContext.class,i);
		}
		public TerminalNode Semi() { return getToken(MxParser.Semi, 0); }
		public List<TerminalNode> Comma() { return getTokens(MxParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(MxParser.Comma, i);
		}
		public VarDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_varDef; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitVarDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VarDefContext varDef() throws RecognitionException {
		VarDefContext _localctx = new VarDefContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_varDef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			typeVarDef();
			setState(90);
			atomVarDef();
			setState(95);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(91);
				match(Comma);
				setState(92);
				atomVarDef();
				}
				}
				setState(97);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(98);
			match(Semi);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AtomVarDefContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(MxParser.Identifier, 0); }
		public TerminalNode Assign() { return getToken(MxParser.Assign, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AtomVarDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atomVarDef; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitAtomVarDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtomVarDefContext atomVarDef() throws RecognitionException {
		AtomVarDefContext _localctx = new AtomVarDefContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_atomVarDef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			match(Identifier);
			setState(103);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Assign) {
				{
				setState(101);
				match(Assign);
				setState(102);
				expression(0);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassBuildContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(MxParser.Identifier, 0); }
		public TerminalNode LParen() { return getToken(MxParser.LParen, 0); }
		public TerminalNode RParen() { return getToken(MxParser.RParen, 0); }
		public BlockstatementContext blockstatement() {
			return getRuleContext(BlockstatementContext.class,0);
		}
		public ClassBuildContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classBuild; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitClassBuild(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassBuildContext classBuild() throws RecognitionException {
		ClassBuildContext _localctx = new ClassBuildContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_classBuild);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			match(Identifier);
			setState(106);
			match(LParen);
			setState(107);
			match(RParen);
			setState(108);
			blockstatement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BlockstatementContext extends ParserRuleContext {
		public TerminalNode Lbrace() { return getToken(MxParser.Lbrace, 0); }
		public TerminalNode Rbrace() { return getToken(MxParser.Rbrace, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public BlockstatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blockstatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitBlockstatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockstatementContext blockstatement() throws RecognitionException {
		BlockstatementContext _localctx = new BlockstatementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_blockstatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(110);
			match(Lbrace);
			setState(114);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3715398200850399236L) != 0)) {
				{
				{
				setState(111);
				statement();
				}
				}
				setState(116);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(117);
			match(Rbrace);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunDefContext extends ParserRuleContext {
		public TypeVarDefContext typeVarDef() {
			return getRuleContext(TypeVarDefContext.class,0);
		}
		public TerminalNode Identifier() { return getToken(MxParser.Identifier, 0); }
		public TerminalNode LParen() { return getToken(MxParser.LParen, 0); }
		public TerminalNode RParen() { return getToken(MxParser.RParen, 0); }
		public BlockstatementContext blockstatement() {
			return getRuleContext(BlockstatementContext.class,0);
		}
		public FunParaListContext funParaList() {
			return getRuleContext(FunParaListContext.class,0);
		}
		public FunDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funDef; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitFunDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunDefContext funDef() throws RecognitionException {
		FunDefContext _localctx = new FunDefContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_funDef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(119);
			typeVarDef();
			setState(120);
			match(Identifier);
			setState(121);
			match(LParen);
			setState(123);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 36029312415039488L) != 0)) {
				{
				setState(122);
				funParaList();
				}
			}

			setState(125);
			match(RParen);
			setState(126);
			blockstatement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunVarDefContext extends ParserRuleContext {
		public TypeVarDefContext typeVarDef() {
			return getRuleContext(TypeVarDefContext.class,0);
		}
		public AtomVarDefContext atomVarDef() {
			return getRuleContext(AtomVarDefContext.class,0);
		}
		public FunVarDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funVarDef; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitFunVarDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunVarDefContext funVarDef() throws RecognitionException {
		FunVarDefContext _localctx = new FunVarDefContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_funVarDef);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(128);
			typeVarDef();
			setState(129);
			atomVarDef();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunParaListContext extends ParserRuleContext {
		public List<FunVarDefContext> funVarDef() {
			return getRuleContexts(FunVarDefContext.class);
		}
		public FunVarDefContext funVarDef(int i) {
			return getRuleContext(FunVarDefContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(MxParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(MxParser.Comma, i);
		}
		public FunParaListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funParaList; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitFunParaList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunParaListContext funParaList() throws RecognitionException {
		FunParaListContext _localctx = new FunParaListContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_funParaList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(131);
			funVarDef();
			setState(136);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(132);
				match(Comma);
				setState(133);
				funVarDef();
				}
				}
				setState(138);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public BlockstatementContext blockstatement() {
			return getRuleContext(BlockstatementContext.class,0);
		}
		public VarDefContext varDef() {
			return getRuleContext(VarDefContext.class,0);
		}
		public IfstatementContext ifstatement() {
			return getRuleContext(IfstatementContext.class,0);
		}
		public WhilestatementContext whilestatement() {
			return getRuleContext(WhilestatementContext.class,0);
		}
		public ForstatementContext forstatement() {
			return getRuleContext(ForstatementContext.class,0);
		}
		public ReturnstatementContext returnstatement() {
			return getRuleContext(ReturnstatementContext.class,0);
		}
		public BreakstatementContext breakstatement() {
			return getRuleContext(BreakstatementContext.class,0);
		}
		public ContinuestatementContext continuestatement() {
			return getRuleContext(ContinuestatementContext.class,0);
		}
		public ExpressionstatementContext expressionstatement() {
			return getRuleContext(ExpressionstatementContext.class,0);
		}
		public EmptystatementContext emptystatement() {
			return getRuleContext(EmptystatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_statement);
		try {
			setState(149);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(139);
				blockstatement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(140);
				varDef();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(141);
				ifstatement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(142);
				whilestatement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(143);
				forstatement();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(144);
				returnstatement();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(145);
				breakstatement();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(146);
				continuestatement();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(147);
				expressionstatement();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(148);
				emptystatement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EmptystatementContext extends ParserRuleContext {
		public TerminalNode Semi() { return getToken(MxParser.Semi, 0); }
		public EmptystatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_emptystatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitEmptystatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EmptystatementContext emptystatement() throws RecognitionException {
		EmptystatementContext _localctx = new EmptystatementContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_emptystatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(151);
			match(Semi);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IfstatementContext extends ParserRuleContext {
		public TerminalNode If() { return getToken(MxParser.If, 0); }
		public TerminalNode LParen() { return getToken(MxParser.LParen, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RParen() { return getToken(MxParser.RParen, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public TerminalNode Else() { return getToken(MxParser.Else, 0); }
		public IfstatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifstatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitIfstatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IfstatementContext ifstatement() throws RecognitionException {
		IfstatementContext _localctx = new IfstatementContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_ifstatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(153);
			match(If);
			setState(154);
			match(LParen);
			setState(155);
			expression(0);
			setState(156);
			match(RParen);
			setState(157);
			statement();
			setState(160);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(158);
				match(Else);
				{
				setState(159);
				statement();
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhilestatementContext extends ParserRuleContext {
		public TerminalNode While() { return getToken(MxParser.While, 0); }
		public TerminalNode LParen() { return getToken(MxParser.LParen, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RParen() { return getToken(MxParser.RParen, 0); }
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public WhilestatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whilestatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitWhilestatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhilestatementContext whilestatement() throws RecognitionException {
		WhilestatementContext _localctx = new WhilestatementContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_whilestatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			match(While);
			setState(163);
			match(LParen);
			setState(164);
			expression(0);
			setState(165);
			match(RParen);
			setState(166);
			statement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ForinitContext extends ParserRuleContext {
		public TerminalNode Semi() { return getToken(MxParser.Semi, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public VarDefContext varDef() {
			return getRuleContext(VarDefContext.class,0);
		}
		public ForinitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forinit; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitForinit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForinitContext forinit() throws RecognitionException {
		ForinitContext _localctx = new ForinitContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_forinit);
		int _la;
		try {
			setState(173);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(169);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3710999636795736068L) != 0)) {
					{
					setState(168);
					expression(0);
					}
				}

				setState(171);
				match(Semi);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(172);
				varDef();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ForstatementContext extends ParserRuleContext {
		public ExpressionContext condition;
		public ExpressionContext update;
		public TerminalNode For() { return getToken(MxParser.For, 0); }
		public TerminalNode LParen() { return getToken(MxParser.LParen, 0); }
		public ForinitContext forinit() {
			return getRuleContext(ForinitContext.class,0);
		}
		public TerminalNode Semi() { return getToken(MxParser.Semi, 0); }
		public TerminalNode RParen() { return getToken(MxParser.RParen, 0); }
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public ForstatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forstatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitForstatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForstatementContext forstatement() throws RecognitionException {
		ForstatementContext _localctx = new ForstatementContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_forstatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(175);
			match(For);
			setState(176);
			match(LParen);
			setState(177);
			forinit();
			setState(179);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3710999636795736068L) != 0)) {
				{
				setState(178);
				((ForstatementContext)_localctx).condition = expression(0);
				}
			}

			setState(181);
			match(Semi);
			setState(183);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3710999636795736068L) != 0)) {
				{
				setState(182);
				((ForstatementContext)_localctx).update = expression(0);
				}
			}

			setState(185);
			match(RParen);
			setState(186);
			statement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReturnstatementContext extends ParserRuleContext {
		public TerminalNode Return() { return getToken(MxParser.Return, 0); }
		public TerminalNode Semi() { return getToken(MxParser.Semi, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ReturnstatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_returnstatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitReturnstatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReturnstatementContext returnstatement() throws RecognitionException {
		ReturnstatementContext _localctx = new ReturnstatementContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_returnstatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(188);
			match(Return);
			setState(190);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3710999636795736068L) != 0)) {
				{
				setState(189);
				expression(0);
				}
			}

			setState(192);
			match(Semi);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BreakstatementContext extends ParserRuleContext {
		public TerminalNode Break() { return getToken(MxParser.Break, 0); }
		public TerminalNode Semi() { return getToken(MxParser.Semi, 0); }
		public BreakstatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_breakstatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitBreakstatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BreakstatementContext breakstatement() throws RecognitionException {
		BreakstatementContext _localctx = new BreakstatementContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_breakstatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(194);
			match(Break);
			setState(195);
			match(Semi);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ContinuestatementContext extends ParserRuleContext {
		public TerminalNode Continue() { return getToken(MxParser.Continue, 0); }
		public TerminalNode Semi() { return getToken(MxParser.Semi, 0); }
		public ContinuestatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_continuestatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitContinuestatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ContinuestatementContext continuestatement() throws RecognitionException {
		ContinuestatementContext _localctx = new ContinuestatementContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_continuestatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(197);
			match(Continue);
			setState(198);
			match(Semi);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionstatementContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Semi() { return getToken(MxParser.Semi, 0); }
		public List<TerminalNode> Comma() { return getTokens(MxParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(MxParser.Comma, i);
		}
		public ExpressionstatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionstatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitExpressionstatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionstatementContext expressionstatement() throws RecognitionException {
		ExpressionstatementContext _localctx = new ExpressionstatementContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_expressionstatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			expression(0);
			setState(205);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(201);
				match(Comma);
				setState(202);
				expression(0);
				}
				}
				setState(207);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(208);
			match(Semi);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstarrayContext extends ParserRuleContext {
		public TerminalNode Lbrace() { return getToken(MxParser.Lbrace, 0); }
		public TerminalNode Rbrace() { return getToken(MxParser.Rbrace, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(MxParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(MxParser.Comma, i);
		}
		public ConstarrayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constarray; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitConstarray(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstarrayContext constarray() throws RecognitionException {
		ConstarrayContext _localctx = new ConstarrayContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_constarray);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(210);
			match(Lbrace);
			setState(212);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3710999636795736068L) != 0)) {
				{
				setState(211);
				expression(0);
				}
			}

			setState(218);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(214);
				match(Comma);
				setState(215);
				expression(0);
				}
				}
				setState(220);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(221);
			match(Rbrace);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArrayUnitContext extends ParserRuleContext {
		public TerminalNode Lbracket() { return getToken(MxParser.Lbracket, 0); }
		public TerminalNode Rbracket() { return getToken(MxParser.Rbracket, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ArrayUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayUnit; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitArrayUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayUnitContext arrayUnit() throws RecognitionException {
		ArrayUnitContext _localctx = new ArrayUnitContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_arrayUnit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(223);
			match(Lbracket);
			setState(225);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3710999636795736068L) != 0)) {
				{
				setState(224);
				expression(0);
				}
			}

			setState(227);
			match(Rbracket);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CallArgsContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(MxParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(MxParser.Comma, i);
		}
		public CallArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_callArgs; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitCallArgs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CallArgsContext callArgs() throws RecognitionException {
		CallArgsContext _localctx = new CallArgsContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_callArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
			expression(0);
			setState(234);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(230);
				match(Comma);
				setState(231);
				expression(0);
				}
				}
				setState(236);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NewExprContext extends ExpressionContext {
		public TerminalNode New() { return getToken(MxParser.New, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public List<ArrayUnitContext> arrayUnit() {
			return getRuleContexts(ArrayUnitContext.class);
		}
		public ArrayUnitContext arrayUnit(int i) {
			return getRuleContext(ArrayUnitContext.class,i);
		}
		public TerminalNode LParen() { return getToken(MxParser.LParen, 0); }
		public TerminalNode RParen() { return getToken(MxParser.RParen, 0); }
		public ConstarrayContext constarray() {
			return getRuleContext(ConstarrayContext.class,0);
		}
		public NewExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitNewExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PreunaryExprContext extends ExpressionContext {
		public Token op;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Selfadd() { return getToken(MxParser.Selfadd, 0); }
		public TerminalNode Selfsub() { return getToken(MxParser.Selfsub, 0); }
		public TerminalNode Not() { return getToken(MxParser.Not, 0); }
		public TerminalNode LogicNot() { return getToken(MxParser.LogicNot, 0); }
		public TerminalNode Sub() { return getToken(MxParser.Sub, 0); }
		public PreunaryExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitPreunaryExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UnaryExprContext extends ExpressionContext {
		public Token op;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Selfadd() { return getToken(MxParser.Selfadd, 0); }
		public TerminalNode Selfsub() { return getToken(MxParser.Selfsub, 0); }
		public UnaryExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitUnaryExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ArrayExprContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ArrayUnitContext arrayUnit() {
			return getRuleContext(ArrayUnitContext.class,0);
		}
		public ArrayExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitArrayExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MemberExprContext extends ExpressionContext {
		public Token op;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AtomContext atom() {
			return getRuleContext(AtomContext.class,0);
		}
		public TerminalNode Member() { return getToken(MxParser.Member, 0); }
		public MemberExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitMemberExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AtomExprContext extends ExpressionContext {
		public AtomContext atom() {
			return getRuleContext(AtomContext.class,0);
		}
		public AtomExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitAtomExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BinaryExprContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Mul() { return getToken(MxParser.Mul, 0); }
		public TerminalNode Div() { return getToken(MxParser.Div, 0); }
		public TerminalNode Mod() { return getToken(MxParser.Mod, 0); }
		public TerminalNode Add() { return getToken(MxParser.Add, 0); }
		public TerminalNode Sub() { return getToken(MxParser.Sub, 0); }
		public TerminalNode LeftShift() { return getToken(MxParser.LeftShift, 0); }
		public TerminalNode RightShift() { return getToken(MxParser.RightShift, 0); }
		public TerminalNode Less() { return getToken(MxParser.Less, 0); }
		public TerminalNode Greater() { return getToken(MxParser.Greater, 0); }
		public TerminalNode LessEqual() { return getToken(MxParser.LessEqual, 0); }
		public TerminalNode GreaterEqual() { return getToken(MxParser.GreaterEqual, 0); }
		public TerminalNode Equal() { return getToken(MxParser.Equal, 0); }
		public TerminalNode UnEqual() { return getToken(MxParser.UnEqual, 0); }
		public TerminalNode And() { return getToken(MxParser.And, 0); }
		public TerminalNode Xor() { return getToken(MxParser.Xor, 0); }
		public TerminalNode Or() { return getToken(MxParser.Or, 0); }
		public TerminalNode LogicAnd() { return getToken(MxParser.LogicAnd, 0); }
		public TerminalNode LogicOr() { return getToken(MxParser.LogicOr, 0); }
		public BinaryExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitBinaryExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CallExprContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode LParen() { return getToken(MxParser.LParen, 0); }
		public TerminalNode RParen() { return getToken(MxParser.RParen, 0); }
		public CallArgsContext callArgs() {
			return getRuleContext(CallArgsContext.class,0);
		}
		public CallExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitCallExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AssignExprContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Assign() { return getToken(MxParser.Assign, 0); }
		public AssignExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitAssignExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ParenExprContext extends ExpressionContext {
		public TerminalNode LParen() { return getToken(MxParser.LParen, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RParen() { return getToken(MxParser.RParen, 0); }
		public ParenExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitParenExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ConditionalExprContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Question() { return getToken(MxParser.Question, 0); }
		public TerminalNode Colon() { return getToken(MxParser.Colon, 0); }
		public ConditionalExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitConditionalExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 48;
		enterRecursionRule(_localctx, 48, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(260);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case New:
				{
				_localctx = new NewExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(238);
				match(New);
				setState(239);
				type();
				setState(243);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(240);
						arrayUnit();
						}
						} 
					}
					setState(245);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
				}
				setState(248);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
				case 1:
					{
					setState(246);
					match(LParen);
					setState(247);
					match(RParen);
					}
					break;
				}
				setState(251);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
				case 1:
					{
					setState(250);
					constarray();
					}
					break;
				}
				}
				break;
			case LParen:
				{
				_localctx = new ParenExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(253);
				match(LParen);
				setState(254);
				expression(0);
				setState(255);
				match(RParen);
				}
				break;
			case Sub:
			case LogicNot:
			case Not:
			case Selfadd:
			case Selfsub:
				{
				_localctx = new PreunaryExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(257);
				((PreunaryExprContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 13647876L) != 0)) ) {
					((PreunaryExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(258);
				expression(11);
				}
				break;
			case Lbrace:
			case Null:
			case True:
			case False:
			case This:
			case Identifier:
			case Integer:
			case FomatStringL:
			case FStringLiteral:
			case StringLiteral:
				{
				_localctx = new AtomExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(259);
				atom();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(307);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(305);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
					case 1:
						{
						_localctx = new BinaryExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(262);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(263);
						((BinaryExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 56L) != 0)) ) {
							((BinaryExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(264);
						expression(11);
						}
						break;
					case 2:
						{
						_localctx = new BinaryExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(265);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(266);
						((BinaryExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==Add || _la==Sub) ) {
							((BinaryExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(267);
						expression(10);
						}
						break;
					case 3:
						{
						_localctx = new BinaryExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(268);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(269);
						((BinaryExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==RightShift || _la==LeftShift) ) {
							((BinaryExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(270);
						expression(9);
						}
						break;
					case 4:
						{
						_localctx = new BinaryExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(271);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(272);
						((BinaryExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 960L) != 0)) ) {
							((BinaryExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(273);
						expression(8);
						}
						break;
					case 5:
						{
						_localctx = new BinaryExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(274);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(275);
						((BinaryExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==UnEqual || _la==Equal) ) {
							((BinaryExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(276);
						expression(7);
						}
						break;
					case 6:
						{
						_localctx = new BinaryExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(277);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(278);
						((BinaryExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 917504L) != 0)) ) {
							((BinaryExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(279);
						expression(6);
						}
						break;
					case 7:
						{
						_localctx = new BinaryExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(280);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(281);
						((BinaryExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==LogicAnd || _la==LogicOr) ) {
							((BinaryExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(282);
						expression(5);
						}
						break;
					case 8:
						{
						_localctx = new ConditionalExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(283);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(284);
						match(Question);
						setState(285);
						expression(0);
						setState(286);
						match(Colon);
						setState(287);
						expression(3);
						}
						break;
					case 9:
						{
						_localctx = new AssignExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(289);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(290);
						((AssignExprContext)_localctx).op = match(Assign);
						setState(291);
						expression(2);
						}
						break;
					case 10:
						{
						_localctx = new CallExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(292);
						if (!(precpred(_ctx, 15))) throw new FailedPredicateException(this, "precpred(_ctx, 15)");
						setState(293);
						match(LParen);
						setState(295);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3710999636795736068L) != 0)) {
							{
							setState(294);
							callArgs();
							}
						}

						setState(297);
						match(RParen);
						}
						break;
					case 11:
						{
						_localctx = new MemberExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(298);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(299);
						((MemberExprContext)_localctx).op = match(Member);
						setState(300);
						atom();
						}
						break;
					case 12:
						{
						_localctx = new ArrayExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(301);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(302);
						arrayUnit();
						}
						break;
					case 13:
						{
						_localctx = new UnaryExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(303);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(304);
						((UnaryExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==Selfadd || _la==Selfsub) ) {
							((UnaryExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					}
					} 
				}
				setState(309);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AtomContext extends ParserRuleContext {
		public TerminalNode Integer() { return getToken(MxParser.Integer, 0); }
		public TerminalNode True() { return getToken(MxParser.True, 0); }
		public TerminalNode False() { return getToken(MxParser.False, 0); }
		public TerminalNode This() { return getToken(MxParser.This, 0); }
		public TerminalNode Null() { return getToken(MxParser.Null, 0); }
		public TerminalNode Identifier() { return getToken(MxParser.Identifier, 0); }
		public TerminalNode StringLiteral() { return getToken(MxParser.StringLiteral, 0); }
		public FstringContext fstring() {
			return getRuleContext(FstringContext.class,0);
		}
		public ConstarrayContext constarray() {
			return getRuleContext(ConstarrayContext.class,0);
		}
		public AtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atom; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitAtom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtomContext atom() throws RecognitionException {
		AtomContext _localctx = new AtomContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_atom);
		try {
			setState(319);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Integer:
				enterOuterAlt(_localctx, 1);
				{
				setState(310);
				match(Integer);
				}
				break;
			case True:
				enterOuterAlt(_localctx, 2);
				{
				setState(311);
				match(True);
				}
				break;
			case False:
				enterOuterAlt(_localctx, 3);
				{
				setState(312);
				match(False);
				}
				break;
			case This:
				enterOuterAlt(_localctx, 4);
				{
				setState(313);
				match(This);
				}
				break;
			case Null:
				enterOuterAlt(_localctx, 5);
				{
				setState(314);
				match(Null);
				}
				break;
			case Identifier:
				enterOuterAlt(_localctx, 6);
				{
				setState(315);
				match(Identifier);
				}
				break;
			case StringLiteral:
				enterOuterAlt(_localctx, 7);
				{
				setState(316);
				match(StringLiteral);
				}
				break;
			case FomatStringL:
			case FStringLiteral:
				enterOuterAlt(_localctx, 8);
				{
				setState(317);
				fstring();
				}
				break;
			case Lbrace:
				enterOuterAlt(_localctx, 9);
				{
				setState(318);
				constarray();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FstringContext extends ParserRuleContext {
		public Token basestring;
		public TerminalNode FomatStringL() { return getToken(MxParser.FomatStringL, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode FomatStringR() { return getToken(MxParser.FomatStringR, 0); }
		public List<MidfstringUnitContext> midfstringUnit() {
			return getRuleContexts(MidfstringUnitContext.class);
		}
		public MidfstringUnitContext midfstringUnit(int i) {
			return getRuleContext(MidfstringUnitContext.class,i);
		}
		public TerminalNode FStringLiteral() { return getToken(MxParser.FStringLiteral, 0); }
		public FstringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fstring; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitFstring(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FstringContext fstring() throws RecognitionException {
		FstringContext _localctx = new FstringContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_fstring);
		int _la;
		try {
			setState(332);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FomatStringL:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(321);
				match(FomatStringL);
				setState(322);
				expression(0);
				setState(326);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FomatStringM) {
					{
					{
					setState(323);
					midfstringUnit();
					}
					}
					setState(328);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(329);
				match(FomatStringR);
				}
				}
				break;
			case FStringLiteral:
				enterOuterAlt(_localctx, 2);
				{
				setState(331);
				((FstringContext)_localctx).basestring = match(FStringLiteral);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MidfstringUnitContext extends ParserRuleContext {
		public TerminalNode FomatStringM() { return getToken(MxParser.FomatStringM, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public MidfstringUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_midfstringUnit; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MxVisitor ) return ((MxVisitor<? extends T>)visitor).visitMidfstringUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MidfstringUnitContext midfstringUnit() throws RecognitionException {
		MidfstringUnitContext _localctx = new MidfstringUnitContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_midfstringUnit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(334);
			match(FomatStringM);
			setState(335);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 24:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 10);
		case 1:
			return precpred(_ctx, 9);
		case 2:
			return precpred(_ctx, 8);
		case 3:
			return precpred(_ctx, 7);
		case 4:
			return precpred(_ctx, 6);
		case 5:
			return precpred(_ctx, 5);
		case 6:
			return precpred(_ctx, 4);
		case 7:
			return precpred(_ctx, 3);
		case 8:
			return precpred(_ctx, 2);
		case 9:
			return precpred(_ctx, 15);
		case 10:
			return precpred(_ctx, 14);
		case 11:
			return precpred(_ctx, 13);
		case 12:
			return precpred(_ctx, 12);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001=\u0152\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0005\u0000<\b\u0000\n\u0000\f\u0000"+
		"?\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0005\u0002"+
		"K\b\u0002\n\u0002\f\u0002N\t\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0005\u0003U\b\u0003\n\u0003\f\u0003X\t\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0005\u0004^\b\u0004"+
		"\n\u0004\f\u0004a\t\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0003\u0005h\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0005\u0007q\b\u0007"+
		"\n\u0007\f\u0007t\t\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0003\b|\b\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001"+
		"\t\u0001\n\u0001\n\u0001\n\u0005\n\u0087\b\n\n\n\f\n\u008a\t\n\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u0096\b\u000b\u0001\f"+
		"\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003"+
		"\r\u00a1\b\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000f\u0003\u000f\u00aa\b\u000f\u0001\u000f\u0001\u000f"+
		"\u0003\u000f\u00ae\b\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0003\u0010\u00b4\b\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u00b8\b"+
		"\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0003"+
		"\u0011\u00bf\b\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0005\u0014\u00cc\b\u0014\n\u0014\f\u0014\u00cf\t\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0015\u0001\u0015\u0003\u0015\u00d5\b\u0015\u0001\u0015"+
		"\u0001\u0015\u0005\u0015\u00d9\b\u0015\n\u0015\f\u0015\u00dc\t\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0003\u0016\u00e2\b\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017\u00e9"+
		"\b\u0017\n\u0017\f\u0017\u00ec\t\u0017\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0005\u0018\u00f2\b\u0018\n\u0018\f\u0018\u00f5\t\u0018\u0001"+
		"\u0018\u0001\u0018\u0003\u0018\u00f9\b\u0018\u0001\u0018\u0003\u0018\u00fc"+
		"\b\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0003\u0018\u0105\b\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0003\u0018\u0128\b\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018\u0132"+
		"\b\u0018\n\u0018\f\u0018\u0135\t\u0018\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0003\u0019\u0140\b\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0005\u001a"+
		"\u0145\b\u001a\n\u001a\f\u001a\u0148\t\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0003\u001a\u014d\b\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0000\u00010\u001c\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.0246\u0000\n\u0002\u0000"+
		"#&77\u0004\u0000\u0002\u0002\u000e\u000e\u0014\u0014\u0016\u0017\u0001"+
		"\u0000\u0003\u0005\u0001\u0000\u0001\u0002\u0001\u0000\u000f\u0010\u0001"+
		"\u0000\u0006\t\u0001\u0000\n\u000b\u0001\u0000\u0011\u0013\u0001\u0000"+
		"\f\r\u0001\u0000\u0016\u0017\u0173\u0000=\u0001\u0000\u0000\u0000\u0002"+
		"B\u0001\u0000\u0000\u0000\u0004D\u0001\u0000\u0000\u0000\u0006R\u0001"+
		"\u0000\u0000\u0000\bY\u0001\u0000\u0000\u0000\nd\u0001\u0000\u0000\u0000"+
		"\fi\u0001\u0000\u0000\u0000\u000en\u0001\u0000\u0000\u0000\u0010w\u0001"+
		"\u0000\u0000\u0000\u0012\u0080\u0001\u0000\u0000\u0000\u0014\u0083\u0001"+
		"\u0000\u0000\u0000\u0016\u0095\u0001\u0000\u0000\u0000\u0018\u0097\u0001"+
		"\u0000\u0000\u0000\u001a\u0099\u0001\u0000\u0000\u0000\u001c\u00a2\u0001"+
		"\u0000\u0000\u0000\u001e\u00ad\u0001\u0000\u0000\u0000 \u00af\u0001\u0000"+
		"\u0000\u0000\"\u00bc\u0001\u0000\u0000\u0000$\u00c2\u0001\u0000\u0000"+
		"\u0000&\u00c5\u0001\u0000\u0000\u0000(\u00c8\u0001\u0000\u0000\u0000*"+
		"\u00d2\u0001\u0000\u0000\u0000,\u00df\u0001\u0000\u0000\u0000.\u00e5\u0001"+
		"\u0000\u0000\u00000\u0104\u0001\u0000\u0000\u00002\u013f\u0001\u0000\u0000"+
		"\u00004\u014c\u0001\u0000\u0000\u00006\u014e\u0001\u0000\u0000\u00008"+
		"<\u0003\u0010\b\u00009<\u0003\u0004\u0002\u0000:<\u0003\b\u0004\u0000"+
		";8\u0001\u0000\u0000\u0000;9\u0001\u0000\u0000\u0000;:\u0001\u0000\u0000"+
		"\u0000<?\u0001\u0000\u0000\u0000=;\u0001\u0000\u0000\u0000=>\u0001\u0000"+
		"\u0000\u0000>@\u0001\u0000\u0000\u0000?=\u0001\u0000\u0000\u0000@A\u0005"+
		"\u0000\u0000\u0001A\u0001\u0001\u0000\u0000\u0000BC\u0007\u0000\u0000"+
		"\u0000C\u0003\u0001\u0000\u0000\u0000DE\u0005(\u0000\u0000EF\u00057\u0000"+
		"\u0000FL\u0005!\u0000\u0000GK\u0003\b\u0004\u0000HK\u0003\f\u0006\u0000"+
		"IK\u0003\u0010\b\u0000JG\u0001\u0000\u0000\u0000JH\u0001\u0000\u0000\u0000"+
		"JI\u0001\u0000\u0000\u0000KN\u0001\u0000\u0000\u0000LJ\u0001\u0000\u0000"+
		"\u0000LM\u0001\u0000\u0000\u0000MO\u0001\u0000\u0000\u0000NL\u0001\u0000"+
		"\u0000\u0000OP\u0005\"\u0000\u0000PQ\u0005\u001f\u0000\u0000Q\u0005\u0001"+
		"\u0000\u0000\u0000RV\u0003\u0002\u0001\u0000SU\u0003,\u0016\u0000TS\u0001"+
		"\u0000\u0000\u0000UX\u0001\u0000\u0000\u0000VT\u0001\u0000\u0000\u0000"+
		"VW\u0001\u0000\u0000\u0000W\u0007\u0001\u0000\u0000\u0000XV\u0001\u0000"+
		"\u0000\u0000YZ\u0003\u0006\u0003\u0000Z_\u0003\n\u0005\u0000[\\\u0005"+
		" \u0000\u0000\\^\u0003\n\u0005\u0000][\u0001\u0000\u0000\u0000^a\u0001"+
		"\u0000\u0000\u0000_]\u0001\u0000\u0000\u0000_`\u0001\u0000\u0000\u0000"+
		"`b\u0001\u0000\u0000\u0000a_\u0001\u0000\u0000\u0000bc\u0005\u001f\u0000"+
		"\u0000c\t\u0001\u0000\u0000\u0000dg\u00057\u0000\u0000ef\u0005\u0015\u0000"+
		"\u0000fh\u00030\u0018\u0000ge\u0001\u0000\u0000\u0000gh\u0001\u0000\u0000"+
		"\u0000h\u000b\u0001\u0000\u0000\u0000ij\u00057\u0000\u0000jk\u0005\u001b"+
		"\u0000\u0000kl\u0005\u001c\u0000\u0000lm\u0003\u000e\u0007\u0000m\r\u0001"+
		"\u0000\u0000\u0000nr\u0005!\u0000\u0000oq\u0003\u0016\u000b\u0000po\u0001"+
		"\u0000\u0000\u0000qt\u0001\u0000\u0000\u0000rp\u0001\u0000\u0000\u0000"+
		"rs\u0001\u0000\u0000\u0000su\u0001\u0000\u0000\u0000tr\u0001\u0000\u0000"+
		"\u0000uv\u0005\"\u0000\u0000v\u000f\u0001\u0000\u0000\u0000wx\u0003\u0006"+
		"\u0003\u0000xy\u00057\u0000\u0000y{\u0005\u001b\u0000\u0000z|\u0003\u0014"+
		"\n\u0000{z\u0001\u0000\u0000\u0000{|\u0001\u0000\u0000\u0000|}\u0001\u0000"+
		"\u0000\u0000}~\u0005\u001c\u0000\u0000~\u007f\u0003\u000e\u0007\u0000"+
		"\u007f\u0011\u0001\u0000\u0000\u0000\u0080\u0081\u0003\u0006\u0003\u0000"+
		"\u0081\u0082\u0003\n\u0005\u0000\u0082\u0013\u0001\u0000\u0000\u0000\u0083"+
		"\u0088\u0003\u0012\t\u0000\u0084\u0085\u0005 \u0000\u0000\u0085\u0087"+
		"\u0003\u0012\t\u0000\u0086\u0084\u0001\u0000\u0000\u0000\u0087\u008a\u0001"+
		"\u0000\u0000\u0000\u0088\u0086\u0001\u0000\u0000\u0000\u0088\u0089\u0001"+
		"\u0000\u0000\u0000\u0089\u0015\u0001\u0000\u0000\u0000\u008a\u0088\u0001"+
		"\u0000\u0000\u0000\u008b\u0096\u0003\u000e\u0007\u0000\u008c\u0096\u0003"+
		"\b\u0004\u0000\u008d\u0096\u0003\u001a\r\u0000\u008e\u0096\u0003\u001c"+
		"\u000e\u0000\u008f\u0096\u0003 \u0010\u0000\u0090\u0096\u0003\"\u0011"+
		"\u0000\u0091\u0096\u0003$\u0012\u0000\u0092\u0096\u0003&\u0013\u0000\u0093"+
		"\u0096\u0003(\u0014\u0000\u0094\u0096\u0003\u0018\f\u0000\u0095\u008b"+
		"\u0001\u0000\u0000\u0000\u0095\u008c\u0001\u0000\u0000\u0000\u0095\u008d"+
		"\u0001\u0000\u0000\u0000\u0095\u008e\u0001\u0000\u0000\u0000\u0095\u008f"+
		"\u0001\u0000\u0000\u0000\u0095\u0090\u0001\u0000\u0000\u0000\u0095\u0091"+
		"\u0001\u0000\u0000\u0000\u0095\u0092\u0001\u0000\u0000\u0000\u0095\u0093"+
		"\u0001\u0000\u0000\u0000\u0095\u0094\u0001\u0000\u0000\u0000\u0096\u0017"+
		"\u0001\u0000\u0000\u0000\u0097\u0098\u0005\u001f\u0000\u0000\u0098\u0019"+
		"\u0001\u0000\u0000\u0000\u0099\u009a\u0005-\u0000\u0000\u009a\u009b\u0005"+
		"\u001b\u0000\u0000\u009b\u009c\u00030\u0018\u0000\u009c\u009d\u0005\u001c"+
		"\u0000\u0000\u009d\u00a0\u0003\u0016\u000b\u0000\u009e\u009f\u0005.\u0000"+
		"\u0000\u009f\u00a1\u0003\u0016\u000b\u0000\u00a0\u009e\u0001\u0000\u0000"+
		"\u0000\u00a0\u00a1\u0001\u0000\u0000\u0000\u00a1\u001b\u0001\u0000\u0000"+
		"\u0000\u00a2\u00a3\u00050\u0000\u0000\u00a3\u00a4\u0005\u001b\u0000\u0000"+
		"\u00a4\u00a5\u00030\u0018\u0000\u00a5\u00a6\u0005\u001c\u0000\u0000\u00a6"+
		"\u00a7\u0003\u0016\u000b\u0000\u00a7\u001d\u0001\u0000\u0000\u0000\u00a8"+
		"\u00aa\u00030\u0018\u0000\u00a9\u00a8\u0001\u0000\u0000\u0000\u00a9\u00aa"+
		"\u0001\u0000\u0000\u0000\u00aa\u00ab\u0001\u0000\u0000\u0000\u00ab\u00ae"+
		"\u0005\u001f\u0000\u0000\u00ac\u00ae\u0003\b\u0004\u0000\u00ad\u00a9\u0001"+
		"\u0000\u0000\u0000\u00ad\u00ac\u0001\u0000\u0000\u0000\u00ae\u001f\u0001"+
		"\u0000\u0000\u0000\u00af\u00b0\u0005/\u0000\u0000\u00b0\u00b1\u0005\u001b"+
		"\u0000\u0000\u00b1\u00b3\u0003\u001e\u000f\u0000\u00b2\u00b4\u00030\u0018"+
		"\u0000\u00b3\u00b2\u0001\u0000\u0000\u0000\u00b3\u00b4\u0001\u0000\u0000"+
		"\u0000\u00b4\u00b5\u0001\u0000\u0000\u0000\u00b5\u00b7\u0005\u001f\u0000"+
		"\u0000\u00b6\u00b8\u00030\u0018\u0000\u00b7\u00b6\u0001\u0000\u0000\u0000"+
		"\u00b7\u00b8\u0001\u0000\u0000\u0000\u00b8\u00b9\u0001\u0000\u0000\u0000"+
		"\u00b9\u00ba\u0005\u001c\u0000\u0000\u00ba\u00bb\u0003\u0016\u000b\u0000"+
		"\u00bb!\u0001\u0000\u0000\u0000\u00bc\u00be\u00053\u0000\u0000\u00bd\u00bf"+
		"\u00030\u0018\u0000\u00be\u00bd\u0001\u0000\u0000\u0000\u00be\u00bf\u0001"+
		"\u0000\u0000\u0000\u00bf\u00c0\u0001\u0000\u0000\u0000\u00c0\u00c1\u0005"+
		"\u001f\u0000\u0000\u00c1#\u0001\u0000\u0000\u0000\u00c2\u00c3\u00051\u0000"+
		"\u0000\u00c3\u00c4\u0005\u001f\u0000\u0000\u00c4%\u0001\u0000\u0000\u0000"+
		"\u00c5\u00c6\u00052\u0000\u0000\u00c6\u00c7\u0005\u001f\u0000\u0000\u00c7"+
		"\'\u0001\u0000\u0000\u0000\u00c8\u00cd\u00030\u0018\u0000\u00c9\u00ca"+
		"\u0005 \u0000\u0000\u00ca\u00cc\u00030\u0018\u0000\u00cb\u00c9\u0001\u0000"+
		"\u0000\u0000\u00cc\u00cf\u0001\u0000\u0000\u0000\u00cd\u00cb\u0001\u0000"+
		"\u0000\u0000\u00cd\u00ce\u0001\u0000\u0000\u0000\u00ce\u00d0\u0001\u0000"+
		"\u0000\u0000\u00cf\u00cd\u0001\u0000\u0000\u0000\u00d0\u00d1\u0005\u001f"+
		"\u0000\u0000\u00d1)\u0001\u0000\u0000\u0000\u00d2\u00d4\u0005!\u0000\u0000"+
		"\u00d3\u00d5\u00030\u0018\u0000\u00d4\u00d3\u0001\u0000\u0000\u0000\u00d4"+
		"\u00d5\u0001\u0000\u0000\u0000\u00d5\u00da\u0001\u0000\u0000\u0000\u00d6"+
		"\u00d7\u0005 \u0000\u0000\u00d7\u00d9\u00030\u0018\u0000\u00d8\u00d6\u0001"+
		"\u0000\u0000\u0000\u00d9\u00dc\u0001\u0000\u0000\u0000\u00da\u00d8\u0001"+
		"\u0000\u0000\u0000\u00da\u00db\u0001\u0000\u0000\u0000\u00db\u00dd\u0001"+
		"\u0000\u0000\u0000\u00dc\u00da\u0001\u0000\u0000\u0000\u00dd\u00de\u0005"+
		"\"\u0000\u0000\u00de+\u0001\u0000\u0000\u0000\u00df\u00e1\u0005\u0019"+
		"\u0000\u0000\u00e0\u00e2\u00030\u0018\u0000\u00e1\u00e0\u0001\u0000\u0000"+
		"\u0000\u00e1\u00e2\u0001\u0000\u0000\u0000\u00e2\u00e3\u0001\u0000\u0000"+
		"\u0000\u00e3\u00e4\u0005\u001a\u0000\u0000\u00e4-\u0001\u0000\u0000\u0000"+
		"\u00e5\u00ea\u00030\u0018\u0000\u00e6\u00e7\u0005 \u0000\u0000\u00e7\u00e9"+
		"\u00030\u0018\u0000\u00e8\u00e6\u0001\u0000\u0000\u0000\u00e9\u00ec\u0001"+
		"\u0000\u0000\u0000\u00ea\u00e8\u0001\u0000\u0000\u0000\u00ea\u00eb\u0001"+
		"\u0000\u0000\u0000\u00eb/\u0001\u0000\u0000\u0000\u00ec\u00ea\u0001\u0000"+
		"\u0000\u0000\u00ed\u00ee\u0006\u0018\uffff\uffff\u0000\u00ee\u00ef\u0005"+
		"\'\u0000\u0000\u00ef\u00f3\u0003\u0002\u0001\u0000\u00f0\u00f2\u0003,"+
		"\u0016\u0000\u00f1\u00f0\u0001\u0000\u0000\u0000\u00f2\u00f5\u0001\u0000"+
		"\u0000\u0000\u00f3\u00f1\u0001\u0000\u0000\u0000\u00f3\u00f4\u0001\u0000"+
		"\u0000\u0000\u00f4\u00f8\u0001\u0000\u0000\u0000\u00f5\u00f3\u0001\u0000"+
		"\u0000\u0000\u00f6\u00f7\u0005\u001b\u0000\u0000\u00f7\u00f9\u0005\u001c"+
		"\u0000\u0000\u00f8\u00f6\u0001\u0000\u0000\u0000\u00f8\u00f9\u0001\u0000"+
		"\u0000\u0000\u00f9\u00fb\u0001\u0000\u0000\u0000\u00fa\u00fc\u0003*\u0015"+
		"\u0000\u00fb\u00fa\u0001\u0000\u0000\u0000\u00fb\u00fc\u0001\u0000\u0000"+
		"\u0000\u00fc\u0105\u0001\u0000\u0000\u0000\u00fd\u00fe\u0005\u001b\u0000"+
		"\u0000\u00fe\u00ff\u00030\u0018\u0000\u00ff\u0100\u0005\u001c\u0000\u0000"+
		"\u0100\u0105\u0001\u0000\u0000\u0000\u0101\u0102\u0007\u0001\u0000\u0000"+
		"\u0102\u0105\u00030\u0018\u000b\u0103\u0105\u00032\u0019\u0000\u0104\u00ed"+
		"\u0001\u0000\u0000\u0000\u0104\u00fd\u0001\u0000\u0000\u0000\u0104\u0101"+
		"\u0001\u0000\u0000\u0000\u0104\u0103\u0001\u0000\u0000\u0000\u0105\u0133"+
		"\u0001\u0000\u0000\u0000\u0106\u0107\n\n\u0000\u0000\u0107\u0108\u0007"+
		"\u0002\u0000\u0000\u0108\u0132\u00030\u0018\u000b\u0109\u010a\n\t\u0000"+
		"\u0000\u010a\u010b\u0007\u0003\u0000\u0000\u010b\u0132\u00030\u0018\n"+
		"\u010c\u010d\n\b\u0000\u0000\u010d\u010e\u0007\u0004\u0000\u0000\u010e"+
		"\u0132\u00030\u0018\t\u010f\u0110\n\u0007\u0000\u0000\u0110\u0111\u0007"+
		"\u0005\u0000\u0000\u0111\u0132\u00030\u0018\b\u0112\u0113\n\u0006\u0000"+
		"\u0000\u0113\u0114\u0007\u0006\u0000\u0000\u0114\u0132\u00030\u0018\u0007"+
		"\u0115\u0116\n\u0005\u0000\u0000\u0116\u0117\u0007\u0007\u0000\u0000\u0117"+
		"\u0132\u00030\u0018\u0006\u0118\u0119\n\u0004\u0000\u0000\u0119\u011a"+
		"\u0007\b\u0000\u0000\u011a\u0132\u00030\u0018\u0005\u011b\u011c\n\u0003"+
		"\u0000\u0000\u011c\u011d\u0005\u001d\u0000\u0000\u011d\u011e\u00030\u0018"+
		"\u0000\u011e\u011f\u0005\u001e\u0000\u0000\u011f\u0120\u00030\u0018\u0003"+
		"\u0120\u0132\u0001\u0000\u0000\u0000\u0121\u0122\n\u0002\u0000\u0000\u0122"+
		"\u0123\u0005\u0015\u0000\u0000\u0123\u0132\u00030\u0018\u0002\u0124\u0125"+
		"\n\u000f\u0000\u0000\u0125\u0127\u0005\u001b\u0000\u0000\u0126\u0128\u0003"+
		".\u0017\u0000\u0127\u0126\u0001\u0000\u0000\u0000\u0127\u0128\u0001\u0000"+
		"\u0000\u0000\u0128\u0129\u0001\u0000\u0000\u0000\u0129\u0132\u0005\u001c"+
		"\u0000\u0000\u012a\u012b\n\u000e\u0000\u0000\u012b\u012c\u0005\u0018\u0000"+
		"\u0000\u012c\u0132\u00032\u0019\u0000\u012d\u012e\n\r\u0000\u0000\u012e"+
		"\u0132\u0003,\u0016\u0000\u012f\u0130\n\f\u0000\u0000\u0130\u0132\u0007"+
		"\t\u0000\u0000\u0131\u0106\u0001\u0000\u0000\u0000\u0131\u0109\u0001\u0000"+
		"\u0000\u0000\u0131\u010c\u0001\u0000\u0000\u0000\u0131\u010f\u0001\u0000"+
		"\u0000\u0000\u0131\u0112\u0001\u0000\u0000\u0000\u0131\u0115\u0001\u0000"+
		"\u0000\u0000\u0131\u0118\u0001\u0000\u0000\u0000\u0131\u011b\u0001\u0000"+
		"\u0000\u0000\u0131\u0121\u0001\u0000\u0000\u0000\u0131\u0124\u0001\u0000"+
		"\u0000\u0000\u0131\u012a\u0001\u0000\u0000\u0000\u0131\u012d\u0001\u0000"+
		"\u0000\u0000\u0131\u012f\u0001\u0000\u0000\u0000\u0132\u0135\u0001\u0000"+
		"\u0000\u0000\u0133\u0131\u0001\u0000\u0000\u0000\u0133\u0134\u0001\u0000"+
		"\u0000\u0000\u01341\u0001\u0000\u0000\u0000\u0135\u0133\u0001\u0000\u0000"+
		"\u0000\u0136\u0140\u00058\u0000\u0000\u0137\u0140\u0005*\u0000\u0000\u0138"+
		"\u0140\u0005+\u0000\u0000\u0139\u0140\u0005,\u0000\u0000\u013a\u0140\u0005"+
		")\u0000\u0000\u013b\u0140\u00057\u0000\u0000\u013c\u0140\u0005=\u0000"+
		"\u0000\u013d\u0140\u00034\u001a\u0000\u013e\u0140\u0003*\u0015\u0000\u013f"+
		"\u0136\u0001\u0000\u0000\u0000\u013f\u0137\u0001\u0000\u0000\u0000\u013f"+
		"\u0138\u0001\u0000\u0000\u0000\u013f\u0139\u0001\u0000\u0000\u0000\u013f"+
		"\u013a\u0001\u0000\u0000\u0000\u013f\u013b\u0001\u0000\u0000\u0000\u013f"+
		"\u013c\u0001\u0000\u0000\u0000\u013f\u013d\u0001\u0000\u0000\u0000\u013f"+
		"\u013e\u0001\u0000\u0000\u0000\u01403\u0001\u0000\u0000\u0000\u0141\u0142"+
		"\u00059\u0000\u0000\u0142\u0146\u00030\u0018\u0000\u0143\u0145\u00036"+
		"\u001b\u0000\u0144\u0143\u0001\u0000\u0000\u0000\u0145\u0148\u0001\u0000"+
		"\u0000\u0000\u0146\u0144\u0001\u0000\u0000\u0000\u0146\u0147\u0001\u0000"+
		"\u0000\u0000\u0147\u0149\u0001\u0000\u0000\u0000\u0148\u0146\u0001\u0000"+
		"\u0000\u0000\u0149\u014a\u0005:\u0000\u0000\u014a\u014d\u0001\u0000\u0000"+
		"\u0000\u014b\u014d\u0005<\u0000\u0000\u014c\u0141\u0001\u0000\u0000\u0000"+
		"\u014c\u014b\u0001\u0000\u0000\u0000\u014d5\u0001\u0000\u0000\u0000\u014e"+
		"\u014f\u0005;\u0000\u0000\u014f\u0150\u00030\u0018\u0000\u01507\u0001"+
		"\u0000\u0000\u0000 ;=JLV_gr{\u0088\u0095\u00a0\u00a9\u00ad\u00b3\u00b7"+
		"\u00be\u00cd\u00d4\u00da\u00e1\u00ea\u00f3\u00f8\u00fb\u0104\u0127\u0131"+
		"\u0133\u013f\u0146\u014c";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}