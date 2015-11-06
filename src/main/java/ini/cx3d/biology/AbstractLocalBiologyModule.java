package ini.cx3d.biology;

import ini.cx3d.electrophysiology.ElectroPhysiolgyAxon;
import ini.cx3d.electrophysiology.ElectroPhysiolgyDendrite;
import ini.cx3d.electrophysiology.model.AxonELModel;
import ini.cx3d.electrophysiology.model.DendriteELModel;
import ini.cx3d.electrophysiology.model.SomaELModel;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalSphere;



public abstract class AbstractLocalBiologyModule implements LocalBiologyModule{

	protected CellElement cellElement; // "protected" so subclasses can access it
	
	public CellElement getCellElement() {return cellElement;}

	public void setCellElement(CellElement cellElement) {
		this.cellElement = cellElement;
	}
	
	public PhysicalSphere getSphere()
	{
		if(cellElement instanceof SomaElement)
			return ((SomaElement)cellElement).getPhysicalSphere();
		else
			 return null;
		
	}
	

	
	public PhysicalCylinder getCylinder()
	{
		if(cellElement instanceof NeuriteElement)
			return ((NeuriteElement)cellElement).getPhysicalCylinder();
		else
			 return null;
		
	}
	
	public SomaElement getSoma()
	{
		if(cellElement instanceof SomaElement)
			return ((SomaElement)cellElement);
		else
			 return null;
		
	}
	
	public NeuriteElement getNeurite()
	{
		if(cellElement instanceof NeuriteElement)
			return ((NeuriteElement)cellElement);
		else
			 return null;
		
	}
	
	public double [] getLocation()
	{
		return cellElement.getLocation();
	}
	
	
	public SomaELModel getSomaELModel()
	{
		if(cellElement instanceof SomaElement)
			return ((SomaElement)cellElement).getPhysicalSphere().getElectroPhysiolgy().getModel();
		else
			 return null;
		
	}
	
	public boolean isInAxon()
	{
		return getNeurite().isAnAxon();
	}
	
	public AxonELModel getAxonELModel()
	{
		if(cellElement instanceof NeuriteElement)
			return ((ElectroPhysiolgyAxon)((NeuriteElement)cellElement).getPhysicalCylinder().getElectroPhysiolgy()).getModel();
		else
			 return null;
		
	}
	
	public DendriteELModel getDendriteELModel()
	{
		if(cellElement instanceof NeuriteElement)
			return ((ElectroPhysiolgyDendrite)((NeuriteElement)cellElement).getPhysicalCylinder().getElectroPhysiolgy()).getModel();
		else
			 return null;
		
	}
	
	public boolean isCopiedWhenNeuriteBranches() {return false;}
	
	public boolean isCopiedWhenNeuriteElongates() {return false;}
	
	public boolean isCopiedWhenNeuriteExtendsFromSoma() {return false;}
	
	public boolean isCopiedWhenSomaDivides() {return false;}
	
	public boolean isDeletedAfterNeuriteHasBifurcated() {return false;}
	
	public abstract AbstractLocalBiologyModule getCopy();
	
	public abstract void run();
}
