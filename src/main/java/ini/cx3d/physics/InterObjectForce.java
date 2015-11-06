package ini.cx3d.physics;

/**
 * Defines the 3D physical interactions between physical objects (cylinders and spheres).
 * @author fredericzubler
 *
 */
public interface InterObjectForce {
	
	public double [] forceOnObject(PhysicalObject o1, PhysicalObject o2);
	/**
	 * Force felt by a sphere (sphere1) due to the presence of another sphere (sphere2)
	 * @param sphere1
	 * @param sphere2
	 * @return
	 */
	public double[] forceOnASphereFromASphere(PhysicalSphere sphere1, PhysicalSphere sphere2);
	
	/**
	 * Force felt by a cylinder due to the presence of a sphere
	 * @param cylinder
	 * @param sphere
	 * @return the 3 first elements represent the force exerted by the sphere onto the cylinder,
	 * the fourth -when it exists- is the proportion of the force that is transmitted to the proximal end
	 * (= the point mass of the mother).
	 * 
	 */
	public double[] forceOnACylinderFromASphere(PhysicalCylinder cylinder, PhysicalSphere sphere);
	
	/**
	 * Force felt by sphere due to the presence of a cylinder
	 * @param sphere
	 * @param cylinder
	 * @return
	 */
	public double[] forceOnASphereFromACylinder(PhysicalSphere sphere, PhysicalCylinder cylinder);
	
	/**
	 * Force felt by a cylinder (cylinder1) due to the presence of another cylinder (cylinder2)
	 * @param cylinder1
	 * @param cylinder2
	 * @return the 3 first elements represent the force exerted by cylinder2 onto cylinder1,
	 * the fourth -when it exists- is the proportion of the force that is transmitted to the proximal end
	 * of cylinder1 (= the point mass of the mother).
	 */
	public double[]forceOnACylinderFromACylinder(PhysicalCylinder cylinder1, PhysicalCylinder cylinder2);

	public double[]forceFromPhysicalBond(PhysicalBond physicalBond,PhysicalObject po);
}
