/*
 * Copyright (c) 2018 "Niclas Kjäll-Ohlsson, Bjørnar Fjøren"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.Date;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.DataOutputStream;
import java.io.FileOutputStream;

public class SchedulerJob implements Runnable, Serializable {
	
	private String name;
	private Script script;
	private long initialDelay;
	private long period;
	private TimeUnit unit;
	private boolean isScheduled;
	private boolean isRunning;
	
	private Date firstExecutionTime;
	private Date lastExecutionTime;
	private int executionCount;
	
	private Scheduler scheduler;
	
	private transient ScheduledFuture<?> schedulerJobHandle;
	
	public SchedulerJob(String name, Script script, long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {
		
		this.name = name;
		this.script = script;
		this.initialDelay = initialDelay;
		this.period = period;
		this.unit = unit;
		this.isScheduled = false;
		this.isRunning = false;
		
		this.firstExecutionTime = null;
		this.lastExecutionTime = null;
		this.executionCount = 0;
		
		this.scheduler = scheduler;
		
	}
	
	public String name() {
		return this.name;
	}
	
	public Script script() {
		return this.script;
	}
	
	public long initialDelay() {
		return this.initialDelay;
	}
	
	public long period() {
		return this.period;
	}
	
	public TimeUnit unit() {
		return this.unit;
	}
	
	public boolean isScheduled() {
		return this.isScheduled;
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
	
	public Date firstExecutionTime() {
		return this.firstExecutionTime;
	}
	
	public Date lastExecutionTime() {
		return this.lastExecutionTime;
	}
	
	public int executionCount() {
		return this.executionCount;
	}
	
	public void stop() {
		
		if(this.schedulerJobHandle != null) {
			
			this.schedulerJobHandle.cancel(true);
			
		}
		
		this.isScheduled = false;
		this.isRunning = false;
	}
	
	public void schedule() {
		
		this.schedulerJobHandle =
			scheduler.getScheduledExecutorService().scheduleAtFixedRate(
				this,
				this.initialDelay(),
				this.period(),
				this.unit()
			);
			
		this.isScheduled = true;
		
	}
	
	public void run() {
		
		this.isRunning = true;
		
		try {
			
			this.script.init(
				this.scheduler.scriptAPI()
			);

			this.isRunning = true;
			
			if(this.firstExecutionTime == null) {
				this.firstExecutionTime = new Date();
			}
			
			String logFileName =
				Scheduler.SCHEDULER_LOG_DIRECTORY +
					this.name + "/" +
					Utils.getCurrentTimeStampFileNameFriendly() +
					".txt";
			
			Utils.createDirectoryIfNotExists(
				logFileName
			);
			
			DataOutputStream output =
				new DataOutputStream(
					new FileOutputStream(
						logFileName
					)
				);
			
			this.script.run(
				output
			);
			
			output.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		this.isRunning = false;
		this.lastExecutionTime = new Date();
		this.executionCount++;

	}
	
	public JSONBuilder getJSONBuilder() {
		return
			JSONBuilder.f().
				$('{').
					k("name").v(this.name()).
					k("initialDelay").v(this.initialDelay()).
					k("period").v(this.period()).
					k("timeUnit").v(this.unit()).
					k("isScheduled").v(this.isScheduled()).
					k("isRunning").v(this.isRunning()).
					k("firstExecutionTime").v(this.firstExecutionTime()).
					k("lastExecutionTime").v(this.lastExecutionTime()).
					k("executionCount").v(this.executionCount()).
					k("scriptName").v(this.script.name()).
				$('}');
	}
	
	public JSONBuilder getJSONBuilderForExport() {
		return
			JSONBuilder.f().
				$('{').
					k("name").v(this.name()).
					k("initialDelay").v(this.initialDelay()).
					k("period").v(this.period()).
					k("timeUnit").v(this.unit()).
					k("scriptName").v(this.script.name()).
				$('}');
	}
	
	
	public String toJSON() {
		return this.getJSONBuilder().getJSON();
	}
	
}