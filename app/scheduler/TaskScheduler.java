package scheduler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import models.CronTrigger;
import models.TaskTrigger;
import models.User;
import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.cache.Cache;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;
import play.libs.F.Promise;
import play.utils.FastRuntimeException;
import play.utils.Java;
import play.utils.PThreadFactory;


public class TaskScheduler extends PlayPlugin implements TriggerDetailConsumer, TaskStateListener{

	private int threadPoolSize;


	/**
	 * Manages volatile triggers
	 */
	private static TriggerDetailManager volatileTDManager = new VolatileTriggerManager();


	/**
	 * Manages non volatile triggers
	 */
	private static TriggerDetailManager tdManager = new DBTriggerManager();

	private static TaskSchedulerImpl scheduler;

	private static List<String> volatileKeys = new ArrayList<String>();


	public static final List<String> supportedTimeZones = new ArrayList<String>();

	static{
		Logger.info("TaskScheduler:inited");
	}

	@Override
	public void onApplicationStart() {
		threadPoolSize = Integer.parseInt(Play.configuration.getProperty("tasks.pool", "10"));
		scheduler = new DefaultTaskScheduler(threadPoolSize, new SchedulerThreadFactory("tasks", Thread.NORM_PRIORITY), new ThreadPoolExecutor.AbortPolicy());
		Logger.info("TaskScheduler:started");
	}

	public void afterApplicationStart(){
		final TriggerDetailConsumer consumer = this;

		try {
			new Job(){
				public void doJob(){
					loadZones();
				}
			}.call();
		} catch (Exception e1) {
			e1.printStackTrace();
		}


		for (Class clazz : Play.classloader.getAllClasses()) {
			if (Task.class.isAssignableFrom(clazz)) {
				if (clazz.isAnnotationPresent(Schedule.class)) {
					Schedule schedule = (Schedule) clazz.getAnnotation(Schedule.class);
					String triggerKey = clazz.getName();
					String cron = schedule.on();
					String tz = schedule.timeZone();
					boolean repeat = schedule.repeat();
					boolean allTimeZones = schedule.allTimeZones();
					TriggerDetail triggerDetail = null;
					if(cron != null){

						if((tz != null) && tz.equals("NIL")){
							tz = null;
						}

						triggerDetail = new TriggerDetail(triggerKey, clazz.getName(),
								new CronTrigger(cron, tz, allTimeZones, repeat), null);


						scheduleVolatile(triggerDetail);

						volatileKeys.add(triggerKey);

					}
				}

			}
		}

		volatileTDManager.start(consumer);
		tdManager.start(consumer);
		Logger.info("TaskScheduler:running");
	}

	public void onApplicationStop(){
		volatileTDManager.die();
		tdManager.die();
		Logger.info("TaskScheduler:stopped");
	}

	private static boolean isVolatile(String key){
		return volatileKeys.contains(key);
	}

	public static List<TaskInfo> getTaskInfoList(){
		List<TaskInfo> info = new ArrayList<TaskInfo>();
		for(String key : volatileKeys){
			TriggerDetail td = volatileTDManager.get(key);
			if(td != null){
				List<Calendar> cl = td.getTrigger().getNextPlannedExecution(new Date());
				Date next = null;
				if(cl != null){
					next = cl.get(0).getTime();
				}

				String name = td.getTriggerKey();
				name = name.substring(name.lastIndexOf('.') + 1);				

				info.add(new TaskInfo(name, key, next.toString()));
			}
		}
		return info;
	}

	/**
	 * This method is really just for use from the admin utility or testing, it's
	 * not for mainstream use. Use a play Job instead.
	 */
	public static <V> Promise<V> runTask(Class<? extends Task> taskClass, TaskArgs args){
		TriggerDetail triggerDetail = new TriggerDetail(
				taskClass.getName(),
				taskClass.getName(),
				null,
				args);

		return runTask(null, triggerDetail, 0L);
	}

	/**
	 * This method is really just for use from the admin utility, it's
	 * not for mainstream use. Use a play Job instead.
	 * 
	 * @param triggerKey
	 * @return
	 */
	public static <V> Promise<V> runTask(String triggerKey){
		return runTask(triggerKey, null, 0L);
	}

	/**
	 * This method is really just for use from the admin utility, it's
	 * not for mainstream use. Use a play Job instead.
	 * 
	 * @param triggerKey
	 * @param triggerDetail
	 * @param delay
	 * @return
	 */
	public static <V> Promise<V> runTask(String triggerKey, TriggerDetail triggerDetail, long delay){
		final Promise<V> smartFuture = new Promise<V>();
		TriggerDetail td = (triggerDetail == null)?volatileTDManager.get(triggerKey):triggerDetail;
		if(td == null){
			td = tdManager.get(triggerKey);
		}
		if(td != null){
			synchronized(scheduler){
				try {
					Class taskClass = Class.forName(td.getTaskClassName());
					td.setTrigger(CronTrigger.getASAPTrigger());
					List<Calendar> times = td.getTrigger().getNextPlannedExecution(new Date());
					final Task<V> task = (Task) taskClass.getConstructor(TaskExecutionContext.class)
							.newInstance(new TaskExecutionContext(null, null, td, times));
					scheduler.schedule(new Callable<V>(){
						public V call(){
							try {
								V result =  new TaskWrapper<V>(task).call();
								smartFuture.invoke(result);
								return result;
							} catch (Exception e) {
								Logger.error("Failed to schedule task: %s", e.getMessage());
								return null;
							}
						}
					}, delay);
				} catch (Exception e) {
					Logger.error("Failed to run task: %s", e.getMessage());
				}
			}
		}
		return smartFuture;
	}

	private static void scheduleVolatile(TriggerDetail triggerDetail){
		synchronized(volatileTDManager){
			volatileTDManager.offer(triggerDetail);
		}
	}

	public static void schedule(Class<? extends Task> taskClass, TaskArgs args, TaskTrigger trigger){
		if((taskClass == null) || (trigger == null)){
			throw new FastRuntimeException("Invalid schedule. Task class and trigger are mandatory.");
		}
		synchronized(tdManager){
			TriggerDetail triggerDetail = new TriggerDetail(
					taskClass.getName(),
					taskClass.getName(),
					trigger,
					args);
			tdManager.offer(triggerDetail);
		}
	}

	@Override
	public boolean acceptTask(final TriggerDetail triggerDetail) {

		synchronized(scheduler){
			final boolean accepted[] = new boolean[]{false};
			final TaskScheduler me = this;

			try {
				TaskTrigger trigger = triggerDetail.getTrigger();
				Date now = new Date();
				List<Calendar> executionTimes = trigger.getNextPlannedExecution(now);
				long delay = executionTimes.get(0).getTime().getTime() - now.getTime();					
				Class taskClass = Class.forName(triggerDetail.getTaskClassName());
				Task task = (Task) taskClass.getConstructor(TaskExecutionContext.class)
						.newInstance(new TaskExecutionContext(me, me, triggerDetail, executionTimes));
				scheduler.schedule(new TaskWrapper(task), delay);
				scheduled(triggerDetail);
				accepted[0] = true;
			} catch (Exception e) {
				Logger.error("Failed to accept task: %s", e.getMessage());
			}
			return accepted[0];
		}
	}

	private static boolean addSupportedTimeZone(String timeZone){
		if((timeZone != null) && !supportedTimeZones.contains(timeZone)){
			supportedTimeZones.add(timeZone);
			return true;
		}
		return false;
	}

	private static void loadZones(){
		supportedTimeZones.clear();

		//Probably will need to keep supported time zones in a dedicated table
		//but for now...
		List<User> users = User.findAll();
		for(User user : users){
			addSupportedTimeZone(user.timeZone);
		}

	}

	@Override
	public void scheduled(TriggerDetail triggerDetail) {
		TaskStateListener listener =
				isVolatile(triggerDetail.getTriggerKey())?
						volatileTDManager : tdManager;
		//the listener is responsible for synchronization
		listener.scheduled(triggerDetail);
	}

	public void finished(TriggerDetail triggerDetail, boolean success){

		TaskStateListener listener =
				isVolatile(triggerDetail.getTriggerKey())?
						volatileTDManager : tdManager;
		//the listener is responsible for synchronization
		listener.finished(triggerDetail, success);
	}
	
	@Override
    public String getStatus() {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        if (scheduler == null) {
            out.println("Tasks execution pool:");
            out.println("~~~~~~~~~~~~~~~~~~~");
            out.println("(not yet started)");
            return sw.toString();
        }
        out.println("Tasks execution pool:");
        out.println("~~~~~~~~~~~~~~~~~~~");
        out.println("Pool size: " + threadPoolSize);
        out.println("Active count: " + scheduler.getActiveCount());
        out.println("Scheduled task count: " + scheduler.getTaskCount());
        out.println("Queue size: " + scheduler.getQueue().size());

        if (!scheduler.getQueue().isEmpty()) {
            out.println();
            out.println("Waiting tasks:");
            out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            for (Object o : scheduler.getQueue()) {
                ScheduledFuture task = (ScheduledFuture)o;
                out.println(Java.extractUnderlyingCallable((FutureTask)task) + " will run in " + task.getDelay(TimeUnit.SECONDS) + " seconds");        
            }
        }
        return sw.toString();
    }

	public static class TaskInfo{
		public final String name;
		public final String key;
		public final String nextExecution;
		public TaskInfo(String name, String key, String nextExecution) {
			this.name = name;
			this.key = key;
			this.nextExecution = nextExecution;
		}
	}
	

	
	static class SchedulerThreadFactory implements ThreadFactory {
		final int priority;
	    final ThreadGroup group;
	    final AtomicInteger threadNumber = new AtomicInteger(1);
	    final String namePrefix;

	    SchedulerThreadFactory(String poolName, int priority) {
	        SecurityManager s = System.getSecurityManager();
	        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
	        namePrefix = poolName + "-thread-";
	        this.priority = priority;
	    }

	    public Thread newThread(Runnable r) {
	        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
	        if (t.isDaemon()) {
	            t.setDaemon(false);
	        }
	        if (t.getPriority() != priority) {
	            t.setPriority(priority);
	        }
	        return t;
	    }
	}

}
