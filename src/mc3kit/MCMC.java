package mc3kit;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.io.*;
import static java.lang.String.format;

import cern.jet.random.engine.*;

@SuppressWarnings("serial")
public class MCMC implements Serializable {

  
  /*** SETTABLE FIELDS ***/
  
  ModelFactory modelFactory;
  int chainCount = 1;

  private HeatFunction heatFunction;
  private double[] priorHeatExponents;
  private double[] likelihoodHeatExponents;

  private Long randomSeed = null;
  
  private List<Step> steps;
  
  /*** STATE ***/
  
  Logger logger;
  
  boolean initialized;
  long iterationCount;
  long terminationCount;
  TerminationManager terminationManager;

  /*** THREAD POOL MANAGEMENT ***/

  ThreadPoolExecutor threadPool;
  ExecutorCompletionService<Object> completionService;

  RandomSeedGenerator seedGen;
  Chain[] chains;

  TaskManager[][] taskManagers;

  public MCMC() {
    steps = new ArrayList<Step>();
    logger = Logger.getLogger("mc3kit.MCMC");
  }

  private void initialize() throws Throwable {
    if(initialized) {
      return;
    }
    
    if(heatFunction == null) {
      heatFunction = new ConstantHeatFunction();
    }
     
    priorHeatExponents = heatFunction.getPriorHeatExponents(chainCount);
    likelihoodHeatExponents = heatFunction.getLikelihoodHeatExponents(chainCount);

    for (int i = 0; i < chainCount; i++) {
      logger.info(format("chain %d: prior heat exp. %f, like heat exp %f", i,
          priorHeatExponents[i], likelihoodHeatExponents[i]));
    }

    threadPool = new ThreadPoolExecutor(chainCount, 2 * chainCount, 60,
        TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    completionService = new ExecutorCompletionService<Object>(threadPool);
    logger.info("Thread pool created.");

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
    
    initialized = true;
  }

  private void initializeChains() throws Throwable {
    if(chainCount < 1) {
      throw new MC3KitException("There must be at least one chain.");
    }

    if(modelFactory == null) {
      throw new MC3KitException("modelFactory must be set."); 
    }
    
    // Make chains
    int chainCount = getChainCount();
    chains = new Chain[chainCount];
    for(int i = 0; i < chainCount; i++) {
      RandomEngine rng = makeRandomEngine();
      chains[i] = new Chain(this, i, chainCount, priorHeatExponents[i],
          likelihoodHeatExponents[i], rng);
    }
    
    // Use thread pool to construct models for different chains
    for(int i = 0; i < chainCount; i++) {
      final int chainId = i;
      safeSubmit(new Callable<Object>() {
        @Override
        public Object call() throws Exception {
          Model chainModel = modelFactory.createModel(chains[chainId]);
          chains[chainId].setModel(chainModel);
          chainModel.setChain(chains[chainId]);
          return this;
        }
      });
    }
    
    // Wait for completion
    for(int i = 0; i < chainCount; i++) {
      completionService.take();
    }
    
    logger.info("Chains created.");
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

        taskManagers[i][j] = new TaskManager(task, i == 0);

        for (int chainId : chainIds) {
          if (taskManagersByChain[i][chainId] != null) {
            throw new MC3KitException(
              String.format("Two tasks handling chain %d in step %d.", chainId, i)
            );
          }
          taskManagersByChain[i][chainId] = taskManagers[i][j];
        }
      }
    }

    // Make sure that every chain is covered in the first step
    for (int chainId = 0; chainId < chainCount; chainId++) {
      if (taskManagersByChain[0][chainId] == null)
        throw new MC3KitException(
          String.format("Chain %d is not covered by first step.", chainId)
        );
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
    logger.info("Steps set up.");
  }
  
  /**
   * Run MCMC for a single step.
   * @throws MC3KitException 
   */
  public void step() throws Throwable {
    runFor(1);
  }
  
  /**
   * Run MCMC for multiple steps.
   * @param iterationCount
   * @throws MC3KitException 
   */
  public synchronized void runFor(long runForCount) throws Throwable {
    if(runForCount == 0)
      return;
    
    initialize();
    terminationCount = iterationCount + runForCount;
    run();
  }
  
  public synchronized long getIterationCount() {
    return iterationCount;
  }
  
  private synchronized void run() throws Throwable {
    assert(terminationCount > iterationCount);
    initialize();
    
    logger.fine(format("Running until %d", terminationCount));
    
    terminationManager = new TerminationManager();
    
    logger.fine("Starting run.");

    try {
      for (TaskManager taskManager : taskManagers[0]) {
        assert(taskManager.iterationCount == iterationCount);
        safeSubmit(taskManager);
      }

      boolean done = false;
      while(!done) {
        Future<Object> completedTask = completionService.take();
        Object result = completedTask.get();
        if(result == terminationManager) {
          logger.fine("Got termination task.");
          done = true;
          assert(iterationCount == terminationCount);
        }
        else {
          logger.finer("Got non-termination task.");
        }
      }
    }
    catch (ExecutionException e) {
      throw e.getCause();
    }
    
    terminationManager = null;
  }
  
  Chain getChain(int chainId) {
    if (chainId < 0 || chainId >= getChainCount())
      return null;

    return chains[chainId];
  }
  
  public synchronized Model getModel() {
    return getChain(0).getModel();
  }
  
  public synchronized Model getModel(int chainId) {
    return getChain(chainId).getModel();
  }

  synchronized RandomEngine makeRandomEngine() {
    return new MersenneTwister(seedGen.nextSeed());
  }
  
  class TerminationManager implements Callable<Object> {
    BitSet completedChains;
    
    public TerminationManager() {
      completedChains = new BitSet();
    }
    
    public void tallyComplete(int[] chainIds) {
      boolean complete = false;
      
      synchronized(completedChains) {
        for(int chainId : chainIds) {
          assert(!completedChains.get(chainId));
          
          completedChains.set(chainId);
        }
        
        if(completedChains.cardinality() == chainCount) {
          complete = true;
        }
      }
      
      if(complete) {
        safeSubmit(this);
      }
    }

    @Override
    public Object call() throws Exception {
      iterationCount = terminationCount;
      return this;
    }
  }
  
  class TaskManager implements Callable<Object> {
    long iterationCount;
    int completedChainCount;
    Task task;
    boolean isTerminationHandler;
    Chain[] handledChains;

    TaskManager[] nextTaskManagers;

    public TaskManager(Task task, boolean isTerminationHandler) {
      this.task = task;
      this.isTerminationHandler = isTerminationHandler;

      int[] chainIds = task.getChainIds();
      handledChains = new Chain[chainIds.length];
      for (int i = 0; i < chainIds.length; i++) {
        handledChains[i] = chains[chainIds[i]];
      }
    }

    public void tallyComplete() {
      boolean allComplete = false;
      synchronized (this) {
        completedChainCount++;

        if (completedChainCount == handledChains.length) {
          allComplete = true;
          completedChainCount = 0;
        }
      }
      if (allComplete) {
        iterationCount++;
        if(isTerminationHandler && iterationCount == terminationCount) {
          terminationManager.tallyComplete(task.getChainIds());
        }
        else {
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
    }

    @Override
    public TaskManager call() throws Exception {
      try {
        task.step(handledChains);
      }
      catch (Exception e) {
        throw new MC3KitException("Exception thrown from task", e);
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
  
  private Future<Object> safeSubmit(Callable<Object> task) {

    boolean submitted = false;
    Future<Object> result = null;
    int sleepMillis = 1;
    while (!submitted) {
      try {
        result = completionService.submit(task);
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
    return result;
  }
  
  /*** ACCESSORS ***/

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

  public HeatFunction getHeatFunction() {
    return heatFunction;
  }

  public void setHeatFunction(double heatPower) throws MC3KitException {
    throwIfInitialized();
    setHeatFunction(new PowerHeatFunction(heatPower, 0.0));
  }
  
  public void setHeatFunction(double heatPower, double minHeatExponent) throws MC3KitException {
    throwIfInitialized();
    setHeatFunction(new PowerHeatFunction(heatPower, minHeatExponent));
  }
  
  public void setHeatFunction(HeatFunction heatFunction) throws MC3KitException {
    throwIfInitialized();
    this.heatFunction = heatFunction;
  }

  public Long getRandomSeed() {
    return randomSeed;
  }

  public void setRandomSeed(Long randomSeed) {
    this.randomSeed = randomSeed;
  }
  
  public static void setLogLevel(Level level) {
    Logger logger = Logger.getLogger("");
    logger.setLevel(level);
    for(Handler handler : logger.getHandlers()) {
      handler.setLevel(level);
    }
  }
  
  public static void addLogHandler(Handler handler) {
    Logger.getLogger("").addHandler(handler);
  }
}
