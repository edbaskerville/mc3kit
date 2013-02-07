package mc3kit;

public interface Task
{
	int[] getChainIds();
	void step(Chain[] chains) throws MC3KitException;
}
