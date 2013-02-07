package mc3kit;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import cern.jet.random.engine.*;

import org.apache.log4j.*;

@SuppressWarnings("serial")
public class MCMC implements Serializable {

  ModelFactory modelFactory;
  Model model;
  RandomEngine rng;
  int chainCount = 1;
  
  boolean initialized;
  private List<Step> steps;
  
  public void setModel(Model model) throws MC3KitException {
    throwIfInitialized();
    this.model = model;
  }
  
  public void setRng(RandomEngine rng) throws MC3KitException {
    throwIfInitialized();
    this.rng = rng;
  }

  public void setModelFactory(ModelFactory modelFactory) throws MC3KitException {
    throwIfInitialized();
    this.modelFactory = modelFactory;
  }

  public int getChainCount() {
    return chainCount;
  }

  public void setChainCount(int chainCount) throws MC3KitException {
    throwIfInitialized();
    this.chainCount = chainCount;
  }
  
  public void addTask(final Task task) throws MC3KitException {
    throwIfInitialized();
    steps.add(new Step() {
      @Override
      public List<Task> makeTasks(int chainCount) throws MC3KitException {
        List<Task> list = new ArrayList<Task>();
        list.add(task);
        return list;
      }
    });
  }
  
  public void addStep(Step step) throws MC3KitException {
    throwIfInitialized();
    steps.add(step);
  }

  private HeatFunction heatFunction;
  private double[] priorHeatExponents;
  private double[] likelihoodHeatExponents;

  public HeatFunction getHeatFunction() {
    return heatFunction;
  }

  public void setHeatFunction(HeatFunction heatFunction) throws MC3KitException {
    throwIfInitialized();
    this.heatFunction = heatFunction;
  }

  private Long randomSeed = null;

  public Long getRandomSeed() {
    return randomSeed;
  }

  public void setRandomSeed(Long randomSeed) {
    this.randomSeed = randomSeed;
  }

  /*** THREAD POOL MANAGEMENT ***/

  ThreadPoolExecutor threadPool;
  ExecutorCompletionService<Object> completionService;

  RandomSeedGenerator seedGen;
  Chain[] chains;

  TaskManager[][] taskManagers;

  public MCMC() {
    steps = new ArrayList<Step>();
  }

  /*** LOGGER ***/

  private ErrorLogger logger;

  public ErrorLogger getLogger() {
    return logger;
  }

  public void setLogger(ErrorLogger logger) {
    this.logger = logger;
  }

  public void setLog4jLogger(Logger log4jLogger) {
    setLogger(new ErrorLogger(log4jLogger));
  }

  private void initialize() throws MC3KitException {
    throwIfInitialized();
    initialized = true;

    if (logger == null)
      logger = new ErrorLogger();

    priorHeatExponents = heatFunction.getPriorHeatExponents(chainCount);
    likelihoodHeatExponents = heatFunction
        .getLikelihoodHeatExponents(chainCount);

    if (logger.isEnabledFor(Level.INFO)) {
      for (int i = 0; i < chainCount; i++) {
        logger.info("chain %d: prior heat exp. %f, like heat exp %f", i,
            priorHeatExponents[i], likelihoodHeatExponents[i]);
      }
    }

    threadPool = new ThreadPoolExecutor(chainCount, 2 * chainCount, 60,
        TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    completionService = new ExecutorCompletionService<Object>(threadPool);
    logger.trace("Thread pool created.");

    // Initialize a top-level RNG to generate start seed-table
    // start locations for each replicate
    Random random = new Random();
    if (randomSeed == null)
      randomSeed = random.nextLong();
    random.setSeed(randomSeed);
    int randomSeedStartLocation = random.nextInt(Integer.MAX_VALUE);
    seedGen = new RandomSeedGenerator(randomSeedStartLocation, 0);

    initializeChains();
    initializeSteps();
  }

  private void initializeChains() throws MC3KitException {
    int chainCount = getChainCount();
    
    chains = new Chain[chainCount];
    for (int i = 0; i < chainCount; i++) {
      RandomEngine rng = makeRandomEngine();
      chains[i] = new Chain(this, i, chainCount, priorHeatExponents[i],
          likelihoodHeatExponents[i], rng);
      Model model = modelFactory.createModel(chains[i]);
      chains[i].setModel(model);
    }
    logger.trace("Chains created.");
  }

  private void initializeSteps() throws MC3KitException {
    Map<Step, List<Task>> taskMap = new HashMap<Step, List<Task>>();

    taskManagers = new TaskManager[steps.size()][];
    TaskManager[][] taskManagersByChain = new TaskManager[steps.size()][getChainCount()];
    for (int i = 0; i < steps.size(); i++) {
      // Get existing tasks for the Step object, or create new ones
      Step step = steps.get(i);
      List<Task> tasks;
      if (taskMap.containsKey(step)) {
        tasks = taskMap.get(step);
      }
      else {
        tasks = step.makeTasks(chainCount);
        taskMap.put(step, tasks);
      }

      // Create TaskManager objects for each task at each step.
      // There will be multiple TaskManagers for a task if a step appears
      // multiple times.
      taskManagers[i] = new TaskManager[tasks.size()];
      for (int j = 0; j < tasks.size(); j++) {
        Task task = tasks.get(j);
        int[] chainIds = task.getChainIds();

        taskManagers[i][j] = new TaskManager(task);

        for (int chainId : chainIds) {
          if (taskManagersByChain[i][chainId] != null) {
            throw new MC3KitException(
                "Two tasks handling chain %d in step %d.\n", chainId, i);
          }
          taskManagersByChain[i][chainId] = taskManagers[i][j];
        }
      }
    }

    // Make sure that every chain is covered in the first step
    for (int chainId = 0; chainId < chainCount; chainId++) {
      if (taskManagersByChain[0][chainId] == null)
        throw new MC3KitException("Chain %d is not covered by first step.\n",
            chainId);
    }

    // Identify forward dependencies for each step method
    for (int i = 0; i < steps.size(); i++) {
      for (TaskManager taskManager : taskManagers[i]) {
        Chain[] handledChains = taskManager.handledChains;
        TaskManager[] nextTaskManagers = new TaskManager[handledChains.length];
        for (int j = 0; j < handledChains.length; j++) {
          int chainId = handledChains[j].getChainId();

          // Find the next step method sequentially that uses this chain,
          // looping back if necessary.
          for (int k = i + 1; k <= steps.size() + i; k++) {
            nextTaskManagers[j] = taskManagersByChain[k % steps.size()][chainId];
            if (nextTaskManagers[j] != null)
              break;
          }
        }

        taskManager.nextTaskManagers = nextTaskManagers;
      }
    }
    logger.trace("Steps set up.");
  }
  
  /**
   * Run MCMC for a single step.
   */
  public void step() {
    runFor(1);
  }
  
  /**
   * Run MCMC for multiple steps.
   * @param iterationCount
   */
  public void runFor(long iterationCount) {
  }
  
  public void run() throws Throwable {
    initialize();

    logger.info("Starting run.");

    try {
      for (TaskManager taskManager : taskManagers[0]) {
        completionService.submit(taskManager);
      }

      while (true) {
        Future<Object> completedTask = completionService.take();
        completedTask.get();
      }
    }
    catch (ExecutionException e) {
      throw e.getCause();
    }
  }
  
  public Chain getChain(int chainId) {
    if (chainId < 0 || chainId >= getChainCount())
      return null;

    return chains[chainId];
  }
  
  public Model getModel() {
    return getChain(0).getModel();
  }
  
  public Model getModel(int chainId) {
    return getChain(chainId).getModel();
  }

  synchronized RandomEngine makeRandomEngine() {
    return new MersenneTwister(seedGen.nextSeed());
  }

  public int getThreadCount() {
    return threadPool.getActiveCount();
  }

  class TaskManager implements Callable<Object> {
    int completedChainCount;
    Task task;
    Chain[] handledChains;

    TaskManager[] nextTaskManagers;

    public TaskManager(Task task) {
      this.task = task;

      int[] chainIds = task.getChainIds();
      handledChains = new Chain[chainIds.length];
      for (int i = 0; i < chainIds.length; i++) {
        handledChains[i] = chains[chainIds[i]];
      }
    }

    public void tallyComplete() {
      boolean shouldSubmit = false;
      synchronized (this) {
        completedChainCount++;

        if (completedChainCount == handledChains.length) {
          shouldSubmit = true;
          completedChainCount = 0;
        }
      }
      if (shouldSubmit) {
        boolean submitted = false;
        int sleepMillis = 1;
        while (!submitted) {
          try {
            completionService.submit(this);
            submitted = true;
          }
          catch (RejectedExecutionException e) {
            try {
              Thread.sleep(sleepMillis);
            }
            catch (InterruptedException e1) {
            }
            sleepMillis *= 2;
          }
        }
      }
    }

    @Override
    public TaskManager call() throws Exception {
      try {
        task.step(handledChains);
      }
      catch (Exception e) {
        throw new MC3KitException(e, "Exception thrown from task");
      }

      // Let the next step for this chain know we're complete;
      // it will start itself up once all its chains are ready
      for (TaskManager manager : nextTaskManagers)
        manager.tallyComplete();

      return this;
    }

    @Override
    public String toString() {
      return task.toString();
    }
  }

  private void throwIfInitialized() throws MC3KitException {
    if (initialized)
      throw new MC3KitException("Already initialized");
  }
}
