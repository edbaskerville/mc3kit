package mc3kit;

public interface ModelFactory
{
	Model createModel(Chain initialChain) throws MC3KitException;
}
