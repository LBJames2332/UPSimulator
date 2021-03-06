package upsimulator.interfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import upsimulator.exceptions.TunnelNotExistException;
import upsimulator.exceptions.UnpredictableDimensionException;
import upsimulator.interfaces.Tunnel.TunnelType;

/**
 * Represents a membrane which contains objects, rules, dimensions
 * 
 * @author quan
 *
 */
public interface Membrane extends Name, Cloneable {

	public static HashMap<String, Membrane> membraneClass = new HashMap<>();
	public static HashMap<String, Boolean> membraneClassStatus = new HashMap<>();

	/**
	 * Register a Membrane Class
	 * 
	 * @param name
	 *                       name of membrane class
	 * @param membrane
	 *                       template of membrane class
	 * @param predefined
	 *                       if it is a predefined membrane class. Predefined
	 *                       membrane classes can be membranes which use Java code
	 *                       to implement <tt>Membrane</tt> interface, other than
	 *                       use <tt>UPLanguage</tt> to define a membrane class.
	 */
	public static void registMemClass(String name, Membrane membrane, boolean predefined) {
		membraneClass.put(name, membrane);
		membraneClassStatus.put(name, predefined);
	}

	/**
	 * Create an instance of a membrane class
	 * 
	 * @param membraneName
	 *                         the name of membrane class
	 * @return an instance or {@code null} if membrane class required does not exist
	 */
	public static Membrane getMemInstanceOf(String membraneName) {
		if (membraneClass.containsKey(membraneName)) {
			return membraneClass.get(membraneName).deepClone();
		} else {
			return null;
		}
	}

	/**
	 * Check if a name is the name of one predefined membrane class
	 * 
	 * @param membraneName
	 *                         the name used to check
	 * @return return {@code true} if the name is registered as a predefined
	 *         membrane class, else return {@code false}
	 */
	public static boolean isPredefinedMem(String membraneName) {
		Boolean fBoolean = membraneClassStatus.get(membraneName);
		if (fBoolean == null) {
			return false;
		} else
			return fBoolean;
	}

	/**
	 * Get the template of one membrane class
	 * 
	 * @param name
	 *                 the name of membrane class
	 * @return template membrane
	 */
	public static Membrane getMemClass(String name) {
		return membraneClass.get(name);
	}

	/**
	 * Remove runtime properties whose name starts with {@code $} and initialize
	 * this membrane for a new simulation
	 */
	public void newStepInit();

	/**
	 * Get the quantity of object
	 * 
	 * @param object
	 *                   P object
	 * @return the quantity of object
	 */
	public Number getNumOf(Obj object);

	/**
	 * Add object to this membrane
	 * 
	 * @param object
	 *                   the new added object
	 * @param num
	 *                   the quantity of object
	 */
	public void addObject(Obj object, Number num);

	/**
	 * Reduce object in this membrane
	 * 
	 * @param object
	 *                   the object which will be reduced
	 * @param num
	 *                   the reduced quantity
	 * @return if the object's quantity is greater than num, then reduce it and
	 *         return {@code true}
	 */
	public boolean reduceObject(Obj object, Number num);

	/**
	 * Get all the objects contained in this membrane
	 * 
	 * @return objects and their quantity
	 */
	public Map<Obj, Number> getObjects();

	/**
	 * Add a new rule
	 * 
	 * @param rule
	 *                 A new rule
	 */
	public void addRule(Rule rule);

	/**
	 * Get all the rules contained in this membrane
	 * 
	 * @return All the rules
	 */
	public List<Rule> getRules();

	/**
	 * Add a tunnel between two membranes.
	 * 
	 * @param t
	 *              the tunnel between two membrane, through it the results of rules
	 *              can be set.
	 */
	public void addTunnel(Tunnel t);

	/**
	 * Remove a tunnel from this membranes.
	 * 
	 * @param t
	 *              the tunnel between two membrane, through it the results of rules
	 *              can be set.
	 */
	public default void removeTunnel(Tunnel t) {
		getTunnels().remove(t);
	}

	/**
	 * Get the tunnel from current to target
	 * 
	 * @param type
	 *                   the type of the tunnel
	 * @param target
	 *                   the name of the target membrane. It can be null if the type
	 *                   is Out, Here, in/go all/random.
	 * @return the tunnel to target. Null will be returned if not found.
	 */
	public Tunnel getTunnel(TunnelType type, String target);

	/**
	 * Get all the tunnels of current membrane
	 * 
	 * @return All the tunnels of current membrane
	 */
	public List<Tunnel> getTunnels();

	/**
	 * Get all the tunnel from current to name
	 * 
	 * @param type
	 *                       the type of the tunnel
	 * @param name
	 *                       the name of the target membrane. It cannot be null.
	 * @param compareDim
	 *                       whether to compare the dimensions of name
	 * @return the tunnels to name. Void list will be returned if not found.
	 */
	public List<Tunnel> getTunnels(TunnelType type, Name name, boolean compareDim);

	/**
	 * Delete a membrane.
	 * <p>
	 * Close all the tunnels and set current membrane deleted
	 */
	public void delete();

	/**
	 * Return if this membrane is deleted.
	 * 
	 * @return if this membrane is deleted, return {@code true}
	 */
	public boolean isDeleted();

	/**
	 * Check all the rules inside if they are satisfied, and return all the usable
	 * rules.
	 * <ul>
	 * <li>Maximum parallelism and minimum parallelism can be controlled by editing
	 * the returned list.</li>
	 * <li>Inhibitors and promoters and other nonconsumption conditions can be
	 * checked first.</li>
	 * </ul>
	 * 
	 * @return All the usable rules and satisfied times
	 * @throws CloneNotSupportedException
	 *                                             if some rules, objects, cannot be
	 *                                             cloned
	 * @throws UnpredictableDimensionException
	 *                                             if there exist unpredictable
	 *                                             dimension, such as the dimension
	 *                                             of one rule only appears at a
	 *                                             inhibitor
	 */
	public Map<Rule, Integer> getUsableRules() throws UnpredictableDimensionException, CloneNotSupportedException;

	/**
	 * All the satisfied rules try to fetch the objects they need
	 * 
	 * @return the quantity of rules which have fetched the objects
	 * @throws TunnelNotExistException
	 *                                     If one rule want to get a neighbor of
	 *                                     this membrane and no tunnel was found
	 */
	public Map<Rule, Integer> fetch() throws TunnelNotExistException;

	/**
	 * Set products of rules
	 * 
	 * @return the rules have set their product successfully
	 */
	public Map<Rule, Integer> setProducts();

	/**
	 * 
	 * @param space
	 *                            put in the front of items
	 * @param withObject
	 *                            whether show objects or not
	 * @param withProp
	 *                            whether show properties or not
	 * @param withRule
	 *                            whether show rules or not
	 * @param withSubmembrane
	 *                            whether show sub-membranes or not
	 * @param withTunnel
	 *                            whether show tunnels
	 * @return a string which represents this membrane
	 */
	public String toString(String space, boolean withObject, boolean withProp, boolean withRule,
			boolean withSubmembrane, boolean withTunnel);

	/**
	 * Deep clone a membrane
	 * 
	 * @return the cloned membrane
	 */
	@Override
	public Membrane deepClone();

	/**
	 * Set property of membrane
	 * 
	 * @param propertyName
	 *                          the name of property
	 * @param propertyValue
	 *                          the value of property
	 */
	public void setProperty(String propertyName, Object propertyValue);

	/**
	 * Get the property's value of this membrane
	 * 
	 * @param propertyName
	 *                         the name of property
	 * @return property's value. If property doesn't exist in this membrane,
	 *         {@code null} will be returned.
	 */
	public Object getProperty(String propertyName);

	/**
	 * 
	 * @return all the properties of this membrane
	 */
	public Map<String, Object> getProperties();

	/**
	 * Add a listener to this membrane
	 * 
	 * @param listener
	 *                     a membrane listener
	 */
	public void addListener(MembraneListener listener);

	/**
	 * Remove a listener from this membrane
	 * 
	 * @param listener
	 *                     the listener you want to remove
	 */
	public void removeListener(MembraneListener listener);

	/**
	 * Extend a template
	 * 
	 * @param template
	 *                     template membrane
	 */
	public void extend(Membrane template);

	/**
	 * Get all the children to whom this membrane has a <tt>in</tt> tunnel
	 * 
	 * @return all the children
	 */
	public default List<Membrane> getChildren() {
		ArrayList<Membrane> children = new ArrayList<Membrane>();
		for (Tunnel tunnel : getTunnels()) {
			if (tunnel.getType() == TunnelType.In && !children.contains(tunnel.getTargets().get(0))) {
				children.add(tunnel.getTargets().get(0));
			}
		}
		return children;
	}

	/**
	 * Get all the neighbors to whom this membrane has a <tt>go</tt> tunnel
	 * 
	 * @return all the neighbors
	 */
	public default List<Membrane> getNeighbors() {
		ArrayList<Membrane> neighbors = new ArrayList<Membrane>();
		for (Tunnel tunnel : getTunnels()) {
			if (tunnel.getType() == TunnelType.Go && !neighbors.contains(tunnel.getTargets().get(0))) {
				neighbors.add(tunnel.getTargets().get(0));
			}
		}
		return neighbors;
	}

	/**
	 * Get the parent membrane of this membrane
	 * 
	 * @return parent membrane or {@code null} if parent membrane does not exist
	 */
	public default Membrane getParent() {
		Tunnel tunnelOut = getTunnel(TunnelType.Out, null);
		if (tunnelOut != null) {
			return tunnelOut.getTargets().get(0);
		} else {
			return null;
		}
	}

	/**
	 * Add a child to this membrane
	 * 
	 * @param child
	 *                        child membrane
	 * @param tunnelClass
	 *                        the tunnel class of the two membrane
	 */
	public default void addChild(Membrane child, Class<?> tunnelClass) {
		try {
			Tunnel in = (Tunnel) tunnelClass.newInstance();
			in.setType(TunnelType.In);
			in.setSource(this);
			in.addTarget(child);
			addTunnel(in);

			Tunnel out = (Tunnel) tunnelClass.newInstance();
			out.setType(TunnelType.Out);
			out.setSource(child);
			out.addTarget(this);
			child.addTunnel(out);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public default void removeChild(Membrane child) {
		Iterator<Tunnel> tIter = getTunnels().iterator();
		for (; tIter.hasNext();) {
			Tunnel t = tIter.next();

			if (t.getTargets().contains(child)) {
				t.pushResult();
				t.close();
			}
		}

		Tunnel tunnel = child.getTunnel(TunnelType.Out, null);
		tunnel.pushResult();
		tunnel.close();
	}

	public default void addNeighbor(Membrane neighbor, Class<?> tunnelClass) {
		try {
			Tunnel go = (Tunnel) tunnelClass.newInstance();
			go.setType(TunnelType.In);
			go.setSource(this);
			go.addTarget(neighbor);
			addTunnel(go);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}