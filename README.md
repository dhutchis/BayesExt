Please note, I'm still reading up on this topic and will engage in continuing research and learning. While I work on the theory, I will implement what I learn to test my methods in toy scenarios for feasibility checks.

Goals:

1. Add the ability to represent and use soft evidence in Bayesian networks using likelihood ratios.  The soft evidence will be represented as virtual children of the nodes that we have evidence on.  By virtual, I mean that we do not have a complete description of their conditional probability tables.  
For example, [insert example]
2. Add in the concept of belief functions.  Given some evidence e and a set of compatibility relations between states of variables, we can calculate the proportion of time that a variable state (say, A) is 
 * Provable = Belief(A)
 * Possible = the gap... the chance probability is somewhere in here
 * Impossible = 1-Belief(not-A) = Plausibility(A)  
In this view, belief and plausibility provide a lower and upper bound on the true probability of a variable taking a state.  This is helpful when we can't build a complete model and do normal Bayesian Inference.  It also helps in other unordinary situations, such as when we want to calculate the time a certain variable assignment is (im)possible.

Here is the [AIMA 3e Booksite][aima-home] and here is the [AIMA Java Implementation project homepage][aima-java].  

References:

* [Bilmes: On Virtual Evidence and Soft Evidence in Bayesian Networks](https://www.ee.washington.edu/techsite/papers/documents/UWEETR-2004-0016.pdf)
* [Pearl: Probabilistic Reasoning in Intelligent Systems](http://books.google.com/books?id=AvNID7LyMusC)

To consider:

*	Underflow problem with doubles... Currently resolved by rounding to 7 decimal places via [BigDecimal](http://docs.oracle.com/javase/6/docs/api/java/math/BigDecimal.html).
*	Investigate why attributing the mass assigned to the empty set should be divided from all masses.  Can we just add it to the universe set?

[aima-home]: http://aima.cs.berkeley.edu/
[aima-java]: http://code.google.com/p/aima-java/