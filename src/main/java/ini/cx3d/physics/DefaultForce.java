package ini.cx3d.physics;

import static ini.cx3d.utilities.Matrix.add;
import static ini.cx3d.utilities.Matrix.distance;
import static ini.cx3d.utilities.Matrix.norm;
import static ini.cx3d.utilities.Matrix.scalarMult;
import static ini.cx3d.utilities.Matrix.subtract;
import ini.cx3d.Param;
import ini.cx3d.utilities.Matrix;


public class DefaultForce implements InterObjectForce{

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
		}
		else if (distanceBetweenCenters<0.00000001){  
			double[] force2on1 = ini.cx3d.utilities.Matrix.randomNoise(3,3);
			return force2on1;
		}else{
			// the force itself
			double R = (r1*r2)/(r1+r2);
			double gamma = 1;			// attraction coeff
			double k = 2;     			// repulsion coeff
			double F = k*delta - gamma*Math.sqrt(R*delta);


			double module = F/distanceBetweenCenters;
			double[] force2on1 = Matrix.scalarMult(module,Matrix.subtract(c1, c2));
			return force2on1;
		}
	}


	// =============================================================================================


	public double[] forceOnACylinderFromASphere(PhysicalCylinder cylinder,PhysicalSphere sphere) {
	
		double[] pP = cylinder.proximalEnd(); 
		double[] pD = cylinder.distalEnd();
		double[] axis = cylinder.getSpringAxis();
		double actualLength = norm(axis);
		double r1 = cylinder.getDiameter()*0.5*cylinder.interObjectForceCoefficient;
		double[] c = sphere.getMassLocation(); 
		double r2 = 0.5*sphere.getDiameter()*sphere.interObjectForceCoefficient;

		// I. If the cylinder is small with respect to the sphere: 
		// we only consider the interaction between the sphere and the point mass 
		// (i.e. distal point) of the cylinder - that we treat as a sphere.
		if (actualLength < r2){
			return computeForce(pD, r1, c, r2); 
		}

		// II. If the cylinder is of the same scale or bigger than the sphere,
		// we look at the interaction between the sphere and the closest point 
		// (to the sphere center) on the cylinder. This interaction is distributed to
		// the two ends of the cylinder: the distal (point mass of the segment) and
		// the proximal (point mass of the mother of the segment).

		// 1) 	Finding cc : the closest point to c on the line pPpD ("line" and not "segment")
		// 		It is the projection of the vector pP->c onto the vector pP->pD (=axis)
		double[] pPc = subtract(c,pP);

		// 		projection of pPc onto axis = (pPc.axis)/norm(axis)^2  * axis
		// 		length of the projection = (pPc.axis)/norm(axis)

		double pPcDotAxis = pPc[0]*axis[0] + pPc[1]*axis[1] + pPc[2]*axis[2];
		double K = pPcDotAxis/(actualLength*actualLength);
		//		cc = pP + K* axis 
		double[] cc  = new double[] {pP[0]+K*axis[0], pP[1]+K*axis[1], pP[2]+K*axis[2]}; 


		// 2)	Look if c -and hence cc- is (a) between pP and pD, (b) before pP or (c) after pD
		double proportionTransmitedToProximalEnd;
		if(K<=1.0 && K>=0.0){
			// 		a) 	if cc (the closest point to c on the line pPpD) is between pP and pD
			//			the force is distributed to the two nodes
			proportionTransmitedToProximalEnd = 1.0 - K;
		}else if(K<0){
			// 		b) 	if the closest point to c on the line pPpD is before pP
			//			the force is only on the proximal end (the mother point mass)
			proportionTransmitedToProximalEnd = 1.0;
			cc = pP;
		}else {   	// if(K>1)
			// 		c) if cc is after pD, the force is only on the distal end (the segment's point mass).	 
			proportionTransmitedToProximalEnd = 0.0;
			cc = pD;
		}

		// 3) 	If the smallest distance between the cylinder and the center of the sphere
		//		is larger than the radius of the two objects , there is no interaction:
		double penetration = r1 + r2 -distance(c,cc);		 
		if(penetration<=0) {
			return new double[] {0.0, 0.0, 0.0};
		}
		double[] force = Matrix.scalarMult(forceScaling,computeForce(cc, r1, c, r2));
		if(cylinder.getInterObjectForceCoefficient()==0 || sphere.getInterObjectForceCoefficient()==0)
			force= new double[]{0,0,0};
		return new double[] {force[0], force[1], force[2], proportionTransmitedToProximalEnd};

	}

	// =============================================================================================

	public double[] forceOnASphereFromACylinder(PhysicalSphere sphere, PhysicalCylinder cylinder) {
		// it is the opposite of force on a cylinder from sphere:
		double[] temp = forceOnACylinderFromASphere(cylinder, sphere);
		
		return new double[] {-temp[0], -temp[1], -temp[2]};
	}

	// =============================================================================================


	public double[] forceOnACylinderFromACylinder(PhysicalCylinder cylinder1, PhysicalCylinder cylinder2) {
	

		// define some geometrical values
		double[] A = cylinder1.proximalEnd();
		double[] B = cylinder1.getMassLocation();
		double d1 = cylinder1.getDiameter();
		double[] C = cylinder2.proximalEnd();
		double[] D = cylinder2.getMassLocation();
		double d2 = cylinder2.getDiameter();

		double K = 0.5; // part devoted to the distal node

		//	looking for closest point on them
		// (based on http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline3d/)
		double p13x = A[0]-C[0];
		double p13y = A[1]-C[1];
		double p13z = A[2]-C[2];
		double p43x = D[0]-C[0];
		double p43y = D[1]-C[1];
		double p43z = D[2]-C[2];
		double p21x = B[0]-A[0];
		double p21y = B[1]-A[1];
		double p21z = B[2]-A[2]; 

		double d1343 = p13x*p43x + p13y*p43y + p13z*p43z;
		double d4321 = p21x*p43x + p21y*p43y + p21z*p43z;
		double d1321 = p21x*p13x + p21y*p13y + p21z*p13z;
		double d4343 = p43x*p43x + p43y*p43y + p43z*p43z;
		double d2121 = p21x*p21x + p21y*p21y + p21z*p21z;

		double[] P1, P2;

		double denom = d2121*d4343 - d4321*d4321;

		//if the two segments are not ABSOLUTLY parallel
		if(denom > 0.000000000001){
			double numer = d1343*d4321 - d1321*d4343;

			double mua = numer/denom;
			double mub = (d1343 + mua*d4321)/d4343;

			if(mua<0){
				P1 = A;
				K = 1;
			}else if(mua>1){
				P1 = B;
				K = 0;
			}else{
				P1 = new double[] {A[0]+mua*p21x, A[1]+mua*p21y, A[2]+mua*p21z };
				K = 1-mua;
			}

			if(mub<0){
				P2 = C;
			}else if(mub>1){
				P2 = D;
			}else{
				P2 = new double[] {C[0]+mub*p43x, C[1]+mub*p43y, C[2]+mub*p43z };
			}

		}else{
			P1 = add(A,scalarMult(0.5, subtract(B,A) ));
			P2 = add(C,scalarMult(0.5, subtract(D,C) ));
		}

		// W put a virtual sphere on the two cylinders
		double[] force =  Matrix.scalarMult(forceScaling,computeForce(P1,d1+0,P2,d2+0));
		if(cylinder1.getInterObjectForceCoefficient()==0 || cylinder2.getInterObjectForceCoefficient()==0) 
			force= new double[]{0,0,0};
		return new double[] {force[0], force[1], force[2], K};
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

		double tension = physicalBond.getSpringConstant()*(actualLength-physicalBond.getRestingLength()) + physicalBond.getDumpingConstant()*springSpeed;  // (Note: different than in PhysicalCylinder)
		double[] force = scalarMult(tension/actualLength*forceScaling*100, forceDirection);
		

		return force;
	}

	@Override
	public double[] forceOnObject(PhysicalObject o1, PhysicalObject o2) {
		// TODO Auto-generated method stub
		return null;
	}


	

}
