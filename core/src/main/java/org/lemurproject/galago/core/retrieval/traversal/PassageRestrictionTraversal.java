/*
 *  BSD License (http://lemurproject.org/galago-license)
 */
package org.lemurproject.galago.core.retrieval.traversal;

import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.iterator.MovableExtentIterator;
import org.lemurproject.galago.core.retrieval.iterator.MovableLengthsIterator;
import org.lemurproject.galago.core.retrieval.iterator.PassageFilterIterator;
import org.lemurproject.galago.core.retrieval.iterator.PassageLengthIterator;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.NodeType;
import org.lemurproject.galago.tupleflow.Parameters;

/**
 * Inserts global passage restriction (length/extent) iterators in a node tree.
 * 
 * Inserts at the the appropriate layer of the iterator tree;
 *   just below scoring iterators, top of extent/length iterators.
 * 
 *  -- All other nodes are ignored --
 * 
 * @author sjh
 */
public class PassageRestrictionTraversal extends Traversal {  
  private Retrieval retrieval;
  private Parameters queryParams;
  private Parameters globalParams;
  private boolean passageQuery;
  private boolean wrapLengths;
  private boolean wrapExtents;

  public PassageRestrictionTraversal(Retrieval retrieval, Parameters queryParameters){
    this.retrieval = retrieval;
    this.globalParams = retrieval.getGlobalParameters();
    this.queryParams = queryParameters;
    this.passageQuery = this.globalParams.get("passageQuery", false) || this.globalParams.get("extentQuery", false);
    this.passageQuery = this.queryParams.get("passageQuery", passageQuery) || this.queryParams.get("extentQuery", passageQuery);
    
    // if a field index is being used, it might be important to ignore this wrapper.
    this.wrapLengths = this.queryParams.get("wrapPassageLengths", this.globalParams.get("wrapPassageLengths", true));
    // if some special index is being used, it might be important to ignore this wrapper.
    this.wrapExtents = this.queryParams.get("wrapPassageLengths", this.globalParams.get("wrapPassageLengths", true));
  }
  
  @Override
  public Node afterNode(Node original) throws Exception {
    if(!passageQuery){
      return original;
    }
    // check if the node returns extents or lengths
    NodeType nodeType = retrieval.getNodeType(original);

    // check for a lengths node, that is not already a passagelengths node
    if(wrapLengths && nodeType != null && MovableLengthsIterator.class.isAssignableFrom(nodeType.getIteratorClass())
            && ! PassageLengthIterator.class.isAssignableFrom(nodeType.getIteratorClass())){
      Node parent = original.getParent();
      // check if parent node is a neither extents nor lengths node (e.g. scoring or other), even null (original == root), do nothing (?)
      NodeType parType = (parent!=null)? retrieval.getNodeType(parent): null;
      if(parType != null && !MovableLengthsIterator.class.isAssignableFrom(parType.getIteratorClass())){
        // if so : wrap in passage restriction 
        Node replacement = new Node("passagelengths");
        replacement.addChild(original);
        return replacement;
      }
    }

    // check for an extents node that is not already a restriction node
    if(wrapExtents && nodeType != null 
            && MovableExtentIterator.class.isAssignableFrom(nodeType.getIteratorClass())
            && ! PassageFilterIterator.class.isAssignableFrom(nodeType.getIteratorClass())){

      Node parent = original.getParent();

      // check if parent node is a neither extents nor lengths node (e.g. scoring or other), if null (original == root), do nothing (?)
      NodeType parType = (parent!=null)? retrieval.getNodeType(parent): null;
      if(parType != null && !MovableExtentIterator.class.isAssignableFrom(parType.getIteratorClass())){
        // if so : wrap in passage restriction 
        Node replacement = new Node("passagefilter");
        replacement.addChild(original);
        return replacement;
      }
    }
    
    return original;
  }

  @Override
  public void beforeNode(Node object) throws Exception {
    // do nothing
  }
}