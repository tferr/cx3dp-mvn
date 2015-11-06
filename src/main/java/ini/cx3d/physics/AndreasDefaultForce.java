package ini.cx3d.physics;

import static ini.cx3d.utilities.Matrix.norm;
import static ini.cx3d.utilities.Matrix.normalize;
import static ini.cx3d.utilities.Matrix.scalarMult;
import static ini.cx3d.utilities.Matrix.subtract;
import ini.cx3d.Param;
import ini.cx3d.utilities.Matrix;


public class AndreasDefaultForce implements InterObjectForce{

	// =============================================================================================
	private double forceScaling = 20;
	/** Interaction (repulsive or attractive) between two spheres, based on their distance and radius. 
	 * Even if the two spheres don't interpenetrate, there might be a force. The interaction is based
	 * on : 
	 * <p>Pattana S., <i> Aspects mecaniques de la division cellulaire : Modelisation numerique </i>, 
	 * http://www.lirmm.fr/doctiss04/art/M07.pdf
	 * <p>
	 * I modified the method by using virtual spheres with larger radii, to allow distant interactions,
	 * and get a desired density.
	 */
	public double[] forceOnASphereFromASphere(PhysicalSphere sphere1, PhysicalSphere sphere2) {
		
		double[] c1 = sphere1.getMassLocation();
		double r1 = 0.5*sphere1.getDiameter()*sphere1.getInterObjectForceCoefficient();
		double[] c2 = sphere2.getMassLocation();
		double r2 = 0.5*sphere2.getDiameter()*sphere2.getInterObjectForceCoefficient();

		// the 3 components of the vector c2 -> c1
		//scaling the force
		if(sphere1.getInterObjectForceCoefficient()==0 || sphere2.getInterObjectForceCoefficient()==0) 
			return new double[]{0,0,0};
		return Matrix.scalarMult(forceScaling, computeForce(c1,r1,c2,r2));
		

	}

	// is only used by sphere-cylinder and cylinder-cylinder....
	private double[] computeForce(double[] c1, double r1, double[] c2, double r2){
		if(r1==0|| r2==0) return new double[]{0,0,0};
		double distanceBetweenCenters = Matrix.distance(c1, c2);
		// the overlap distance (how much one penetrates in the other) 
		double delta = r1 + r2 -distanceBetweenCenters;
		// if no overlap : no force
		if(delta<0)
		{
			return new double[] {0,0,0};
		}else{
			// the force itself
			double springconstant = 1;
			double squeeze = delta*delta*springconstant;
			double[] force2on1 = Matrix.scalarMult(squeeze,Matrix.subtract(c1, c2));
			double normOfTheForce = norm(force2on1);
			if(normOfTheForce < Math.min(r1,r2)*0.01){
				force2on1 = scalarMult(Math.min(r1,r2)*0.01, normalize(force2on1));
			}
			return force2on1;
		}
	}


	// =============================================================================================


	public double[] forceOnACylinderFromASphere(PhysicalCylinder cylinder,PhysicalSphere sphere) {
	
		double[] c1 = cylinder.getSoNode().getPosition();
		double r1 = 0.5*cylinder.getDiameter()*cylinder.getInterObjectForceCoefficient();
		double[] c2 = sphere.getMassLocation();
		double r2 = 0.5*sphere.getDiameter()*sphere.getInterObjectForceCoefficient();

		// the 3 components of the vector c2 -> c1
		//scaling the force
		if(cylinder.getInterObjectForceCoefficient()==0 || sphere.getInterObjectForceCoefficient()==0) 
			return new double[]{0,0,0};
		return Matrix.scalarMult(forceScaling, computeForce(c1,r1,c2,r2));

	}

	// =============================================================================================

	public double[] forceOnASphereFromACylinder(PhysicalSphere sphere, PhysicalCylinder cylinder) {
		// it is the opposite of force on a cylinder from sphere:
		double[] temp = forceOnACylinderFromASphere(cylinder, sphere);
		
		return new double[] {-temp[0], -temp[1], -temp[2]};
	}

	// =============================================================================================


	public double[] forceOnACylinderFromACylinder(PhysicalCylinder cylinder1, PhysicalCylinder cylinder2) {
	

		double[] c1 = cylinder1.getMassLocation();
		double r1 = 0.5*cylinder1.getDiameter()*cylinder1.getInterObjectForceCoefficient();
		double[] c2 = cylinder2.getMassLocation();
		double r2 = 0.5*cylinder2.getDiameter()*cylinder2.getInterObjectForceCoefficient();

		// the 3 components of the vector c2 -> c1
		//scaling the force
		if(cylinder1.getInterObjectForceCoefficient()==0 || cylinder2.getInterObjectForceCoefficient()==0) 
			return new double[]{0,0,0};
		return Matrix.scalarMult(forceScaling, computeForce(c1,r1,c2,r2));
		
	}

	@Override
	public double[] forceFromPhysicalBond(PhysicalBond physicalBond,PhysicalObject po) {
		
		PhysicalObject otherPo = physicalBond.getOppositePhysicalObject(po);
		// 2. Find the two insertion points of the bond
		double[] pointOnOtherPo = otherPo.getMassLocation();
		double[] pointOnPo = po.getMassLocation();
		// 3. Compute the force
		double[] forceDirection = subtract(pointOnOtherPo, pointOnPo);
	
		
		double actualLength = norm(forceDirection);

		if(actualLength == 0){  // should never be the case, but who knows... then we avoid division by 0
			return new double[] {0,0,0,0};
		}
		//according to Hookes law:
		double springSpeed = (physicalBond.getRestingLength()-actualLength)/Param.SIMULATION_TIME_STEP;

		double diff = Math.abs(actualLength-physicalBond.getRestingLength());
		double sign = Math.signum(actualLength-physicalBond.getRestingLength());
		if(diff<physicalBond.getRestingLength()/100*10)
		{
			return new double[] {0,0,0,0};
		}
		
		
		double tension = physicalBond.getSpringConstant()*(diff-physicalBond.getRestingLength()/100*10);  // (Note: different than in PhysicalCylinder)
		//double temp = tension/actualLength*forceScaling*100;
		
		double temp = sign*tension/1;
		double[] force = scalarMult(temp, forceDirection);
		

		return force;
	}

	@Override
	public double[] forceOnObject(PhysicalObject o1, PhysicalObject o2) {
		// TODO Auto-generated method stub
		if(o1 instanceof PhysicalSphere && o2 instanceof PhysicalSphere)
		{
			return this.forceOnASphereFromASphere((PhysicalSphere)o1,(PhysicalSphere) o2);
		}
		else if(o1 instanceof PhysicalCylinder && o2 instanceof PhysicalSphere)
		{
			return this.forceOnACylinderFromASphere((PhysicalCylinder)o1, (PhysicalSphere)o2);
		}
		else if(o1 instanceof PhysicalSphere && o2 instanceof PhysicalCylinder)
		{
			return this.forceOnASphereFromACylinder((PhysicalSphere)o1, (PhysicalCylinder)o2);
		}
		else if(o1 instanceof PhysicalCylinder && o2 instanceof PhysicalCylinder)
		{
			return this.forceOnACylinderFromACylinder((PhysicalCylinder)o2,(PhysicalCylinder) o1);
		}
		return null;
	}


	

}
