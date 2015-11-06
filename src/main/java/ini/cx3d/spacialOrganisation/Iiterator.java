package ini.cx3d.spacialOrganisation;


public interface Iiterator {

	public abstract ObjectReference getCurrent();

	public abstract boolean isCurrentSane();
	
	public abstract boolean isAtEnd();

	public abstract void next();
}