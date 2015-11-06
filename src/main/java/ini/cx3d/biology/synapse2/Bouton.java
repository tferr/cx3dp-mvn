package ini.cx3d.biology.synapse2;

import static ini.cx3d.utilities.Matrix.norm;
import static ini.cx3d.utilities.Matrix.subtract;
import ini.cx3d.biology.Cell;
import ini.cx3d.biology.Synapse;
import ini.cx3d.electrophysiology.ElectroPhysiolgySynapse;
import ini.cx3d.electrophysiology.model.ModelFactory;
import ini.cx3d.electrophysiology.model.simpleRateBased.SynapseImpl;
import ini.cx3d.physics.PhysicalBond;


public class Bouton extends Excrescence{
	public int cellID;
	public Synapse synapseWith(Excrescence otherExcrescence) {
		if (!(otherExcrescence instanceof Spine)) return null;
		if (((Spine)otherExcrescence).cellID == this.cellID) return null;
		if (!((Spine)otherExcrescence).isActive) return null;
		if ((norm(subtract(otherExcrescence.getPosition(),this.getPosition())))>2) return null;

		Boolean isPreSynEx = getPo().getCellularElement().getCell().getNeuroMLType().equalsIgnoreCase(Cell.ExcitatoryCell);
		if (otherExcrescence.getPo()!=null) {
			if (getPo().haveIBondedWith(otherExcrescence.getPo())) {	
				return null;
			}
		}


		PhysicalBond pb = new PhysicalBond(getPo(),otherExcrescence.getPo());
		Synapse s = new Synapse();
		s.setPhysicalBond(pb);
		pb.setSynapse(s);
		ElectroPhysiolgySynapse elphys = new ElectroPhysiolgySynapse();
		elphys.setModel(ModelFactory.getSynapseModel(this.getPo(),otherExcrescence.getPo()));

		if (isPreSynEx) {
			((SynapseImpl)elphys.getModel()).weight=0.001;
		}
		else ((SynapseImpl)elphys.getModel()).weight=-0.01;
		elphys.setParent(pb);
		return s;
	}

}
