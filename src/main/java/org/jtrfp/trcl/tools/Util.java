/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2022 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.tools;

import java.awt.Color;
import java.lang.ref.Cleaner;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.coll.BulkRemovable;
import org.jtrfp.trcl.coll.Repopulatable;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.math.Vect3D;

import com.ochafik.util.Adapter;

public class Util {
    
    public static final Cleaner CLEANER = Cleaner.create();
    
public static final Color [] DEFAULT_PALETTE = new Color []{
    new Color(0,0,0,0),
    new Color(7,7,7),
    new Color(14,14,14),
    new Color(22,22,22),
    new Color(29,29,29),
    new Color(36,36,36),
    new Color(43,43,43),
    new Color(51,51,51),
    new Color(58,58,58),
    new Color(65,65,65),
    new Color(72,72,72),
    new Color(79,79,79),
    new Color(87,87,87),
    new Color(94,94,94),
    new Color(101,101,101),
    new Color(108,108,108),
    new Color(116,116,116),
    new Color(123,123,123),
    new Color(130,130,130),
    new Color(137,137,137),
    new Color(145,145,145),
    new Color(152,152,152),
    new Color(159,159,159),
    new Color(166,166,166),
    new Color(173,173,173),
    new Color(181,181,181),
    new Color(188,188,188),
    new Color(195,195,195),
    new Color(202,202,202),
    new Color(210,210,210),
    new Color(217,217,217),
    new Color(224,224,224),
    new Color(5,5,5),
    new Color(10,9,9),
    new Color(14,13,13),
    new Color(19,18,18),
    new Color(24,24,23),
    new Color(29,28,26),
    new Color(35,33,30),
    new Color(40,40,35),
    new Color(45,45,38),
    new Color(50,51,42),
    new Color(55,57,46),
    new Color(58,62,49),
    new Color(61,68,53),
    new Color(64,75,56),
    new Color(65,80,59),
    new Color(67,88,63),
    new Color(73,96,69),
    new Color(80,106,75),
    new Color(85,114,81),
    new Color(90,123,86),
    new Color(96,131,92),
    new Color(102,141,98),
    new Color(108,150,103),
    new Color(113,157,110),
    new Color(121,163,118),
    new Color(130,170,127),
    new Color(137,176,135),
    new Color(145,182,143),
    new Color(152,188,151),
    new Color(161,194,161),
    new Color(169,200,169),
    new Color(173,204,173),
    new Color(6,6,6),
    new Color(11,10,10),
    new Color(16,15,15),
    new Color(20,19,19),
    new Color(25,24,24),
    new Color(31,28,28),
    new Color(37,33,32),
    new Color(42,37,37),
    new Color(47,41,40),
    new Color(53,45,44),
    new Color(59,50,48),
    new Color(65,54,52),
    new Color(71,59,56),
    new Color(77,62,58),
    new Color(83,67,62),
    new Color(90,72,65),
    new Color(97,77,70),
    new Color(107,85,76),
    new Color(116,91,81),
    new Color(127,99,86),
    new Color(138,107,91),
    new Color(148,114,95),
    new Color(159,121,100),
    new Color(167,131,108),
    new Color(173,139,116),
    new Color(180,149,125),
    new Color(187,157,134),
    new Color(193,165,142),
    new Color(200,174,151),
    new Color(206,184,161),
    new Color(212,191,169),
    new Color(218,199,179),
    new Color(3,3,16),
    new Color(5,5,28),
    new Color(8,8,39),
    new Color(10,10,51),
    new Color(14,13,60),
    new Color(18,17,70),
    new Color(23,20,81),
    new Color(28,24,91),
    new Color(34,29,100),
    new Color(39,34,109),
    new Color(46,39,118),
    new Color(52,44,127),
    new Color(60,50,133),
    new Color(66,56,141),
    new Color(75,62,149),
    new Color(83,73,156),
    new Color(87,77,164),
    new Color(91,80,171),
    new Color(97,87,176),
    new Color(107,95,180),
    new Color(114,103,182),
    new Color(122,111,186),
    new Color(129,119,188),
    new Color(137,128,191),
    new Color(144,136,195),
    new Color(151,143,198),
    new Color(159,152,201),
    new Color(167,160,205),
    new Color(174,167,208),
    new Color(181,175,212),
    new Color(187,182,215),
    new Color(195,190,219),
    new Color(14,0,0),
    new Color(27,0,0),
    new Color(40,0,1),
    new Color(53,0,1),
    new Color(66,0,1),
    new Color(79,0,1),
    new Color(92,0,2),
    new Color(105,0,2),
    new Color(118,0,2),
    new Color(131,0,2),
    new Color(144,0,3),
    new Color(157,0,3),
    new Color(170,0,3),
    new Color(183,0,3),
    new Color(196,0,4),
    new Color(199,15,7),
    new Color(203,33,9),
    new Color(208,52,11),
    new Color(212,70,13),
    new Color(216,89,16),
    new Color(221,107,18),
    new Color(225,126,20),
    new Color(229,144,22),
    new Color(233,163,24),
    new Color(238,181,26),
    new Color(242,200,29),
    new Color(246,218,31),
    new Color(251,237,33),
    new Color(255,255,35),
    new Color(255,255,108),
    new Color(255,255,182),
    new Color(255,255,255),
    new Color(8,8,32),
    new Color(16,16,64),
    new Color(24,24,96),
    new Color(32,32,128),
    new Color(40,40,160),
    new Color(48,48,192),
    new Color(56,56,224),
    new Color(63,63,255),
    new Color(8,32,32),
    new Color(16,64,64),
    new Color(24,96,96),
    new Color(32,128,128),
    new Color(40,160,160),
    new Color(48,192,192),
    new Color(56,224,224),
    new Color(63,255,255),
    new Color(56,15,5),
    new Color(70,22,7),
    new Color(85,31,10),
    new Color(98,41,13),
    new Color(111,52,16),
    new Color(125,65,20),
    new Color(137,78,24),
    new Color(149,91,28),
    new Color(162,104,33),
    new Color(173,118,38),
    new Color(183,133,44),
    new Color(195,150,50),
    new Color(203,165,58),
    new Color(204,176,73),
    new Color(205,186,90),
    new Color(207,194,105),
    new Color(2,2,37),
    new Color(10,5,44),
    new Color(18,9,51),
    new Color(26,12,58),
    new Color(35,16,65),
    new Color(43,19,72),
    new Color(51,23,79),
    new Color(59,26,86),
    new Color(67,29,93),
    new Color(75,33,100),
    new Color(84,36,107),
    new Color(92,40,114),
    new Color(100,43,121),
    new Color(108,46,128),
    new Color(116,50,135),
    new Color(124,53,142),
    new Color(133,57,148),
    new Color(141,60,155),
    new Color(149,64,162),
    new Color(157,67,169),
    new Color(165,70,176),
    new Color(173,74,183),
    new Color(182,77,190),
    new Color(190,81,197),
    new Color(198,84,204),
    new Color(206,87,211),
    new Color(214,91,218),
    new Color(222,94,225),
    new Color(231,98,232),
    new Color(239,101,239),
    new Color(247,105,246),
    new Color(255,108,253),
    new Color(55,14,4),
    new Color(81,24,6),
    new Color(108,36,7),
    new Color(136,51,9),
    new Color(162,65,11),
    new Color(188,84,13),
    new Color(214,105,15),
    new Color(241,129,16),
    new Color(244,153,43),
    new Color(245,174,70),
    new Color(247,193,96),
    new Color(248,209,123),
    new Color(250,221,149),
    new Color(252,234,177),
    new Color(254,244,203),
    new Color(255,253,232),
    new Color(0,0,0),
    new Color(0,0,0),
    new Color(0,0,0),
    new Color(0,0,0),
    new Color(0,0,0),
    new Color(0,0,0),
    new Color(0,0,0),
    new Color(0,0,0),
    new Color(0,0,0),
    new Color(0,0,0),
    new Color(0,0,0),
    new Color(0,0,0),
    new Color(0,0,0),
    new Color(0,0,0),
    new Color(0,0,0),
    new Color(0,0,0)
    };

@SuppressWarnings("unchecked")
public static <T>void repopulate(Collection<T> dest, Collection<T> src){
       if(dest instanceof Repopulatable)
	    ((Repopulatable<T>)dest).repopulate(src);
       else if(dest instanceof List){
	   final List<T> dst = (List<T>)dest;
	   if(dest.size()>src.size()){
	       Iterator<T> sIt = src.iterator();
	       ListIterator<T> dIt = dst.listIterator();
	       while(sIt.hasNext())
		   {dIt.next();dIt.set(sIt.next());}
	       dst.subList(src.size(), dst.size()).clear();//Truncate
	   } else if(dest.size()<src.size()){
	       Iterator<T> sIt = src.iterator();
	       ListIterator<T> dIt = dst.listIterator();
	       while(dIt.hasNext())
		   {dIt.next();dIt.set(sIt.next());}
	       final ArrayList<T> additional = new ArrayList<T>();
	       while(sIt.hasNext())
		   additional.add(sIt.next());
	       dest.addAll(additional);
	   }else {//Same size
	       Iterator<T> sIt = src.iterator();
	       ListIterator<T> dIt = dst.listIterator();
	       while(sIt.hasNext())
		   {dIt.next();dIt.set(sIt.next());}
	   }
       }else{
	   dest.clear();
	   dest.addAll(src);
       }
   }//end repopulate(...)
   
   public static <U,V> com.ochafik.util.listenable.Adapter<U,V> bidi2Forward(final com.ochafik.util.Adapter<U,V> bidi){
       return new com.ochafik.util.listenable.Adapter<U,V>(){

	@Override
	public V adapt(U value) {
	    return bidi.adapt(value);
	}
       };
   }//end bidi2Forward(...)
   
   public static <U,V> com.ochafik.util.listenable.Adapter<V,U> bidi2Backward(final com.ochafik.util.Adapter<U,V> bidi){
       return new com.ochafik.util.listenable.Adapter<V,U>(){
	@Override
	public U adapt(V value) {
	    return bidi.reAdapt(value);
	}};
   }//end bidi2Backward(...)
   
   public static <U,V> Adapter<V,U> inverse(final Adapter<U,V> adapter){
       return new Adapter<V,U>(){

	@Override
	public U adapt(V value) {
	    return adapter.reAdapt(value);
	}

	@Override
	public V reAdapt(U value) {
	    return adapter.adapt(value);
	}};
   }
   
   /**
    * Remove a single instance (or none) of each supplied element in given Collection.
    * Not the same as removeAll - only one instance removed.
    * @param toRemove
    * @since Jan 11, 2016
    */
   @SuppressWarnings("unchecked")
public static <E> void bulkRemove(Collection<E> toRemove, Collection<E> target){
       if(target instanceof BulkRemovable)
	   ((BulkRemovable<E>)target).bulkRemove(toRemove);
       else
        for(E e:toRemove)
	   target.remove(e);
   }//end bulkRemove(...)
   
   public static double quantize(double value, double interval){
        return Math.rint(value / interval)*interval;
   }
   
   public static void assertPropertiesNotNull(Object bean, String ... propertyNames){
       final Class<?> beanClass = bean.getClass();
       for(String propertyName : propertyNames){
	   Object result;
	   try{
	   final String camelCaseName = Character.toUpperCase(propertyName.charAt(0))+""+propertyName.substring(1);
	   final Method method = beanClass.getMethod("get"+camelCaseName);
	   result = method.invoke(bean);
	   }catch(Exception e){
	       throw new RuntimeException("Could not check property `"+propertyName+"`",e);}
	   if(result == null)
	       throw new IllegalStateException("Property `"+propertyName+" is intolerably null. Did you forget to set it?");
       }//end for(propertyNames)
   }//end assertPropertiesNotNull
   
   public static void relativeHeadingVector(
	   double [] originPos,
	   double [] originHdg,
	   double [] targetPos,
	   double [] dest
	   ){
       final double [] vectorToTargetVar = dest;
       TRFactory.twosComplementSubtract(targetPos, originPos,vectorToTargetVar);
       
       assert !Vect3D.isAnyNaN(vectorToTargetVar);
       assert !Vect3D.isAnyEqual(vectorToTargetVar, Double.POSITIVE_INFINITY);
       assert !Vect3D.isAnyEqual(vectorToTargetVar, Double.NEGATIVE_INFINITY);
       
       vectorToTargetVar[1]=0;
       Vect3D.normalize(vectorToTargetVar,vectorToTargetVar);
       
       Rotation rot = new Rotation(new Vector3D(originHdg[0],0,originHdg[2]),new Vector3D(vectorToTargetVar));
       final Vector3D deltaVector    = rot.applyTo(Vector3D.PLUS_K);
       dest[0] = deltaVector.getX();
       dest[1] = 0;
       dest[2] = deltaVector.getZ();
   }//end relativeHeadingVector()
   
   @SuppressWarnings("unchecked")
public static <T extends DefaultMutableTreeNode> List<T> getLeaves(T root) {
	final ArrayList<T> result = new ArrayList<>();
	
	final Iterator<TreeNode> it = root.depthFirstEnumeration().asIterator();
	while(it.hasNext()) {
	    final TreeNode node = it.next();
	    if(node.isLeaf())
		result.add((T)node);
	}//end while(hasNext)
	return result;
   }//end getLeaves(...)

   /**
    * Root node is not included when searching for object path, but is included in result
    * @param root
    * @param objectPath
    * @return
    * @since Jan 22, 2022
    */
   public static <T extends DefaultMutableTreeNode> List<T> nodePathFromUserObjectPath(
	   T root, Object ... objectPath) {
       final List<Object> objectPathList = Arrays.asList(objectPath);
       final List<T> result = new ArrayList<>(objectPath.length);
       result.add(root);
       T node = root;
       for(Object obj : objectPathList) {
	   final Iterator<TreeNode> children = node.children().asIterator();
	   boolean found = false;
	   while(children.hasNext() && !found) {
	       @SuppressWarnings("unchecked")
	       final T childNode = (T)children.next();
	       if(Objects.equals(childNode.getUserObject(), obj)) {
		   node = childNode;
		   result.add(childNode);
		   found = true;
	       }//end if(matches)
	   }//end while(hasNext)
       }//end for(objectPath)
       return result;
   }//end nodePathFromUserObjectPath
   
   public static List<DefaultMutableTreeNode> nodePathFromUserObjectPath(Object ...objects) {
       final ArrayList<DefaultMutableTreeNode> result = new ArrayList<>();
       DefaultMutableTreeNode node = null;
       for(Object obj : objects) {
	   final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(obj);
	   if(node != null)
	       node.add(newNode);
	   node = newNode;
	   result.add(newNode);
       }
       return result;
   }//end nodePathFromUserObjectPath

   @SuppressWarnings("unchecked")
public static <T extends TreeNode> T getComparatorApproximation(
	   DefaultMutableTreeNode external,
	   DefaultMutableTreeNode reference,
	   Comparator<T> comparator) {
       
       final ArrayDeque<TreeNode> externalPath = Stream.of(external.getPath()).collect(Collectors.toCollection(()->new ArrayDeque<TreeNode>()));
       externalPath.poll();//Skip root since that's implied and this node will be compared to reference root's children anyway.
       return getComparatorApproximationFromRoot(externalPath, (T)(reference.getRoot()), comparator);
   }//end getToStringApproximation()

   private static <T extends TreeNode> T getComparatorApproximationFromRoot(ArrayDeque<TreeNode> externalPath, T reference, Comparator<T> comparator) {
       @SuppressWarnings("unchecked")
    final Iterator<TreeNode> it = (Iterator<TreeNode>) reference.children().asIterator();

       if(!it.hasNext() || externalPath.isEmpty())
	   return reference;

       T best = null;
       int bestScore = Integer.MAX_VALUE;
       @SuppressWarnings("unchecked")
       final T externalNode = (T)externalPath.poll();

       while(it.hasNext()) {
	   @SuppressWarnings("unchecked")
	   final T node = (T)it.next();
	   System.out.println("compare "+node+" to "+externalNode);
	   Objects.requireNonNull(node);
	   Objects.requireNonNull(externalNode);
	   final int thisScore = Math.abs(comparator.compare(node, externalNode));
	   if( thisScore < bestScore ) {
	       bestScore = thisScore;
	       best = node;
	   }//end if(best)
       }//end while(hasNext)
       if(best.isLeaf())
	   return best;
       else
	   return getComparatorApproximationFromRoot(externalPath, best, comparator);
   }//end getToStringApproximationFromRoot(...)
}//end Util
