package com.gallantrealm.modsynth.module;

import java.io.StreamTokenizer;
import java.io.StringBufferInputStream;

public class Function extends Module {
	private static final long serialVersionUID = 1L;

	public String expressionString;
	public double a, b, c;
	transient double m, n, x, y, z, u, v;
	public CC aCC, bCC, cCC;
	double t;

	public transient Node expression;

	public interface Node {
		public double evaluate();
	}

	public class IfNode implements Node {
		Node condition;
		Node ifexp;
		Node elseexp;
		public double evaluate() {
			double c = condition.evaluate();
			if (c > 0.0) {
				return ifexp.evaluate();
			} else {
				if (elseexp == null) {
					return 0.0;
				}
				return elseexp.evaluate();
			}
		}
	}

	public enum Op {
		ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, NEGATE, POSATE, AND, OR, NOT, INVERT, EQUALS, NOT_EQUAL, GREATER, LESS, GREATER_EQUAL, LESS_EQUAL
	}

	public abstract class OpNode implements Node {
		Node left;
		Node right;
	}

	public class EqualNode extends OpNode {
		public double evaluate() {
			double l = left.evaluate();
			double r = right.evaluate();
			return l == r ? 1.0 : 0.0;
		}
	}

	public class NotEqualNode extends OpNode {
		public double evaluate() {
			double l = left.evaluate();
			double r = right.evaluate();
			return l != r ? 1.0 : 0.0;
		}
	}

	public class GreaterNode extends OpNode {
		public double evaluate() {
			double l = left.evaluate();
			double r = right.evaluate();
			return l > r ? 1.0 : 0.0;
		}
	}

	public class GreaterOrEqualNode extends OpNode {
		public double evaluate() {
			double l = left.evaluate();
			double r = right.evaluate();
			return l >= r ? 1.0 : 0.0;
		}
	}

	public class LessNode extends OpNode {
		public double evaluate() {
			double l = left.evaluate();
			double r = right.evaluate();
			return l < r ? 1.0 : 0.0;
		}
	}

	public class LessOrEqualNode extends OpNode {
		public double evaluate() {
			double l = left.evaluate();
			double r = right.evaluate();
			return l <= r ? 1.0 : 0.0;
		}
	}

	public class AndNode extends OpNode {
		public double evaluate() {
			double l = left.evaluate();
			double r = right.evaluate();
			return l > 0 ? (r > 0 ? 1.0 : 0.0) : 0.0;
		}
	}

	public class OrNode extends OpNode {
		public double evaluate() {
			double l = left.evaluate();
			double r = right.evaluate();
			return l > 0 ? 1.0 : (r > 0 ? 1.0 : 0.0);
		}
	}

	public class AddNode extends OpNode {
		public double evaluate() {
			double l = left.evaluate();
			double r = right.evaluate();
			return l + r;
		}
	}

	public class SubtractNode extends OpNode {
		public double evaluate() {
			double l = left.evaluate();
			double r = right.evaluate();
			return l - r;
		}
	}

	public class MultiplyNode extends OpNode {
		public double evaluate() {
			double l = left.evaluate();
			double r = right.evaluate();
			return l * r;
		}
	}

	public class DivideNode extends OpNode {
		public double evaluate() {
			double l = left.evaluate();
			double r = right.evaluate();
			return l / r;
		}
	}

	public class ModuloNode extends OpNode {
		public double evaluate() {
			double l = left.evaluate();
			double r = right.evaluate();
			return l % r;
		}
	}

	public class NotNode extends OpNode {
		public double evaluate() {
			double r = right.evaluate();
			return r > 0.0 ? 0.0 : 1.0;
		}
	}

	public class InvertNode extends OpNode {
		public double evaluate() {
			double r = right.evaluate();
			return 1 / r;
		}
	}

	public class NegateNode extends OpNode {
		public double evaluate() {
			double r = right.evaluate();
			return -r;
		}
	}

	public class VarNode implements Node {
		char var;
		public double evaluate() {
			if (var == 'a') {
				return a;
			} else if (var == 'b') {
				return b;
			} else if (var == 'c') {
				return c;
			} else if (var == 'm') {
				return m;
			} else if (var == 'n') {
				return n;
			} else if (var == 't') {
				return t;
			} else if (var == 'u') {
				return u;
			} else if (var == 'v') {
				return v;
			} else if (var == 'x') {
				return x;
			} else if (var == 'y') {
				return y;
			} else if (var == 'z') {
				return z;
			}
			return 0.0;
		}
	}

	public class ValueNode implements Node {
		double value;
		public double evaluate() {
			return value;
		}
	}

	public enum Func {
		sin, cos, sqrt, abs, round, signum
	}

	public class FuncNode implements Node {
		Func func;
		Node arg;
		public double evaluate() {
			if (func == Func.sin) {
				return Math.sin(arg.evaluate() * Math.PI);
			} else if (func == Func.cos) {
				return Math.cos(arg.evaluate() * Math.PI);
			} else if (func == Func.sqrt) {
				return Math.sqrt(arg.evaluate());
			} else if (func == Func.abs) {
				return Math.abs(arg.evaluate());
			} else if (func == Func.round) {
				return Math.round(arg.evaluate());
			} else if (func == Func.signum) {
				return Math.signum(arg.evaluate());
			}
			return 0.0;
		}
	}

	/**
	 * Parse the expression. This is a C/Java style expression with ?: conditionals
	 */
	public Node parse(String string) throws Exception {
		System.out.println("Parse: " + string);
		StreamTokenizer tokenizer = new StreamTokenizer(new StringBufferInputStream(string));
		tokenizer.lowerCaseMode(true);
		tokenizer.ordinaryChar('/');
		tokenizer.nextToken();
		Node node = parseExpression(tokenizer);
		if (tokenizer.ttype != StreamTokenizer.TT_EOF) {
			throw new Exception("Excess text after end of formula");
		}
		return node;
	}

	private Node parseExpression(StreamTokenizer tokenizer) throws Exception {
		return parseCompoundCondition(tokenizer);
	}

	private Node parseCompoundCondition(StreamTokenizer tokenizer) throws Exception {
		Node node = parseCondition(tokenizer);
		boolean conditions = true;
		while (conditions) {
			if (tokenizer.ttype == '&') {
				OpNode nnode = new AndNode();
				nnode.left = node;
				tokenizer.nextToken();
				nnode.right = parseCondition(tokenizer);
				node = nnode;
			} else if (tokenizer.ttype == '|') {
				OpNode nnode = new OrNode();
				nnode.left = node;
				tokenizer.nextToken();
				nnode.right = parseCondition(tokenizer);
				node = nnode;
			} else {
				conditions = false;
			}
		}
		return node;
	}

	private Node parseCondition(StreamTokenizer tokenizer) throws Exception {
		Node node = parseComparison(tokenizer);
		if (tokenizer.ttype == '?') {
			IfNode nnode = new IfNode();
			nnode.condition = node;
			tokenizer.nextToken();
			nnode.ifexp = parsePolynomial(tokenizer);
			if (tokenizer.ttype == ':') {
				tokenizer.nextToken();
				nnode.elseexp = parsePolynomial(tokenizer);
			}
			node = nnode;
		}
		return node;
	}

	private Node parseComparison(StreamTokenizer tokenizer) throws Exception {
		Node node = parsePolynomial(tokenizer);
		if (tokenizer.ttype == '=') {
			tokenizer.nextToken();
			if (tokenizer.ttype == '=') {
				tokenizer.nextToken();
			}
			OpNode nnode = new EqualNode();
			nnode.left = node;
			nnode.right = parsePolynomial(tokenizer);
			node = nnode;
		} else if (tokenizer.ttype == '!') {
			tokenizer.nextToken();
			if (tokenizer.ttype == '=') {
				tokenizer.nextToken();
			}
			OpNode nnode = new NotEqualNode();
			nnode.left = node;
			nnode.right = parsePolynomial(tokenizer);
			node = nnode;
		} else if (tokenizer.ttype == '<') {
			tokenizer.nextToken();
			if (tokenizer.ttype == '=') {
				tokenizer.nextToken();
				OpNode nnode = new LessOrEqualNode();
				nnode.left = node;
				nnode.right = parsePolynomial(tokenizer);
				node = nnode;
			} else {
				OpNode nnode = new LessNode();
				nnode.left = node;
				nnode.right = parsePolynomial(tokenizer);
				node = nnode;
			}
		} else if (tokenizer.ttype == '>') {
			tokenizer.nextToken();
			if (tokenizer.ttype == '=') {
				tokenizer.nextToken();
				OpNode nnode = new GreaterOrEqualNode();
				nnode.left = node;
				nnode.right = parsePolynomial(tokenizer);
				node = nnode;
			} else {
				OpNode nnode = new GreaterNode();
				nnode.left = node;
				nnode.right = parsePolynomial(tokenizer);
				node = nnode;
			}
		}
		return node;
	}

	private Node parsePolynomial(StreamTokenizer tokenizer) throws Exception {
		Node node = parseTerm(tokenizer);
		boolean terms = true;
		while (terms) {
			if (tokenizer.ttype == '+') {
				OpNode nnode = new AddNode();
				nnode.left = node;
				tokenizer.nextToken();
				nnode.right = parseTerm(tokenizer);
				node = nnode;
			} else if (tokenizer.ttype == '-') {
				OpNode nnode = new SubtractNode();
				nnode.left = node;
				tokenizer.nextToken();
				nnode.right = parseTerm(tokenizer);
				node = nnode;
			} else {
				terms = false;
			}
		}
		return node;
	}

	private Node parseTerm(StreamTokenizer tokenizer) throws Exception {
		Node node = parseFactor(tokenizer);
		boolean factors = true;
		while (factors) {
			if (tokenizer.ttype == '*') {
				OpNode nnode = new MultiplyNode();
				nnode.left = node;
				tokenizer.nextToken();
				nnode.right = parseFactor(tokenizer);
				node = nnode;
			} else if (tokenizer.ttype == '/') {
				OpNode nnode = new DivideNode();
				nnode.left = node;
				tokenizer.nextToken();
				nnode.right = parseFactor(tokenizer);
				node = nnode;
			} else if (tokenizer.ttype == '%') {
				OpNode nnode = new ModuloNode();
				nnode.left = node;
				tokenizer.nextToken();
				nnode.right = parseFactor(tokenizer);
				node = nnode;
			} else {
				factors = false;
			}
		}
		return node;
	}

	private Node parseFactor(StreamTokenizer tokenizer) throws Exception {
		if (tokenizer.ttype == '!') {
			OpNode node = new NotNode();
			tokenizer.nextToken();
			node.right = parseFactor(tokenizer);
			return node;
		} else if (tokenizer.ttype == '~') {
			OpNode node = new InvertNode();
			tokenizer.nextToken();
			node.right = parseFactor(tokenizer);
			return node;
		} else if (tokenizer.ttype == '+') {
			tokenizer.nextToken();
			return parseFactor(tokenizer);
		} else if (tokenizer.ttype == '-') {
			OpNode node = new NegateNode();
			tokenizer.nextToken();
			node.right = parseFactor(tokenizer);
			return node;
		} else if (tokenizer.ttype == '(') {
			tokenizer.nextToken();
			Node node = parseExpression(tokenizer);
			if (tokenizer.ttype != ')') {
				throw new Exception("Expected ')'");
			}
			tokenizer.nextToken();
			return node;
		} else if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
			ValueNode node = new ValueNode();
			node.value = tokenizer.nval;
			tokenizer.nextToken();
			return node;
		} else if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
			if (tokenizer.sval.length() == 1) {
				char v = tokenizer.sval.charAt(0);
				VarNode node = new VarNode();
				node.var = v;
				tokenizer.nextToken();
				return node;
			} else {
				Func func = Func.valueOf(tokenizer.sval);
				if (func == null) {
					throw new Exception("Unrecognized: " + tokenizer.sval);
				}
				FuncNode node = new FuncNode();
				node.func = func;
				tokenizer.nextToken();
				node.arg = parseArg(tokenizer);
				return node;
			}
		} else {
			throw new Exception("Unrecognized: " + tokenizer.sval);
		}
	}

	private Node parseArg(StreamTokenizer tokenizer) throws Exception {
		if (tokenizer.ttype != '(') {
			throw new Exception("Expected (");
		}
		tokenizer.nextToken();
		Node node = parseExpression(tokenizer);
		if (tokenizer.ttype != ')') {
			throw new Exception("Expected )");
		}
		tokenizer.nextToken();
		return node;
	}

	@Override
	public int getInputCount() {
		return 3;
	}

	@Override
	public int getModCount() {
		return 2;
	}

	@Override
	public int getOutputCount() {
		return 1;
	}

	@Override
	public String getInputName(int n) {
		if (n == 0) {
			return "x";
		} else if (n == 1) {
			return "y";
		} else {
			return "z";
		}
	}

	@Override
	public String getModName(int n) {
		if (n == 0) {
			return "m";
		} else {
			return "n";
		}
	}

	@Override
	public String getOutputName(int n) {
		return "f()";
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		lastm = new double[voices];
		lastn = new double[voices];
		lastu = new double[voices];
		lastv = new double[voices];
		try {
			expression = parse(expressionString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (aCC == null) {
			aCC = new CC();
		}
		if (bCC == null) {
			bCC = new CC();
		}
		if (cCC == null) {
			cCC = new CC();
		}
	}
	
	private transient double timeIncrement;
	private transient double[] lastm, lastn, lastu, lastv;

	@Override
	public void doEnvelope(int voice) {
		timeIncrement = 1.0 / (double)sampleRate;
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		m = 0.0;
		n = 0.0;
		x = 0.0;
		y = 0.0;
		z = 0.0;
		for (int voice = startVoice; voice <= endVoice; voice++) {
			if (mod1 != null) {
				m = mod1.value[voice];
				u = lastu[voice];
				if (lastm[voice] <= 0.0 && m > 0.0) {
					lastu[voice] = 0.0;
				} else {
					lastu[voice] += timeIncrement;
				}
				lastm[voice] = m;
			}
			if (mod2 != null) {
				n = mod2.value[voice];
				v = lastv[voice];
				if (lastn[voice] <= 0.0 && n > 0.0) {
					lastv[voice] = 0.0;
				} else {
					lastv[voice] += timeIncrement;
				}
				lastn[voice] = n;
			}
			if (input1 != null) {
				x = input1.value[voice];
			}
			if (input2 != null) {
				y = input2.value[voice];
			}
			if (input3 != null) {
				z = input3.value[voice];
			}
			float value = 0.0f;
			try {
				if (expression != null) {
					value = (float)expression.evaluate();
				}
				output1.value[voice] = value;
			} catch (ArithmeticException e) {
				// protect from arithmetic exceptions that can happen
			}
		}
		t += timeIncrement;
	}

	@Override
	public void updateCC(int cc, double value) {
		if (aCC.cc == cc) {
			a = aCC.range(value);
		}
		if (bCC.cc == cc) {
			b = bCC.range(value);
		}
		if (cCC.cc == cc) {
			c = cCC.range(value);
		}
	}

}
