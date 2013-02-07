package mc3kit;

import java.util.*;

public interface Step
{
	List<Task> makeTasks(int chainCount) throws MC3KitException;
}
