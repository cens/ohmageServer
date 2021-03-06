//
// Generated by JTB 1.3.2
//

package org.ohmage.config.grammar.syntaxtree;

/**
 * Grammar production:
 * f0 -> Id()
 * f1 -> Condition()
 * f2 -> Value()
 */
public class Expression implements Node {
	/**
	 * Static-random serialVersionUID.
	 */
	private static final long serialVersionUID = -4885544267338638803L;
	public Id f0;
	public Condition f1;
	public Value f2;

	public Expression(Id n0, Condition n1, Value n2) {
		f0 = n0;
		f1 = n1;
		f2 = n2;
	}

	public void accept(org.ohmage.config.grammar.visitor.Visitor v) {
		v.visit(this);
	}
	public <R,A> R accept(org.ohmage.config.grammar.visitor.GJVisitor<R,A> v, A argu) {
		return v.visit(this,argu);
	}
	public <R> R accept(org.ohmage.config.grammar.visitor.GJNoArguVisitor<R> v) {
		return v.visit(this);
	}
	public <A> void accept(org.ohmage.config.grammar.visitor.GJVoidVisitor<A> v, A argu) {
		v.visit(this,argu);
	}
}

