/*
 * BSD License (http://www.galagosearch.org/license)

 */
package org.lemurproject.galago.core.retrieval.iterator;

import org.lemurproject.galago.core.retrieval.processing.DeltaScoringContext;
import org.lemurproject.galago.core.retrieval.processing.ScoringContext;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.tupleflow.Parameters;

/**
 * Not implemented to work over raw counts because it shouldn't blindly
 * be applied to counts - doing that can result in -Infinity scores everywhere.
 * Therefore, this applies to scores, meaning to you had to make a conscious decision
 * to pass a raw count up, and if things go wrong, it's your fault.
 * 
 * @author irmarc
 */
public class LogarithmIterator extends TransformIterator implements MovableScoreIterator {

  MovableScoreIterator scorer;
  Parameters globals;

  public LogarithmIterator(Parameters globalParams, NodeParameters params, MovableScoreIterator svi) {
    super(svi);
    scorer = svi;
    globals = globalParams; // <--- incoming filthy hack
    context = null;
  }

  @Override
  public double score() {
    return Math.log(scorer.score());
  }

  @Override
  public double maximumScore() {
    return Math.log(scorer.maximumScore());
  }

  @Override
  public double minimumScore() {
    return Math.log(scorer.minimumScore());
  }
}