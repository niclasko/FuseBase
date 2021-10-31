/*
 * Copyright (c) 2018 "Niclas Kjall-Ohlsson, Bjornar Fjoren"
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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.io.Serializable;
import java.io.File;

public class Scheduler implements Serializable, RESTAPIValidValues {
	
	private int threadCount;
	private transient ScheduledExecutorService scheduledExecutorService;
	private HashMap<String, SchedulerJob> schedulerJobs;
	private transient ScriptAPI scriptAPI;
	
	private String validAPIValues;
	
	public final static String SCHEDULER_LOG_DIRECTORY =
		Config.LOG_DIRECTORY + "scheduler/";
	
	public Scheduler(int threadCount) {
		
		this.threadCount =
			threadCount;
		this.schedulerJobs =
			new HashMap<String, SchedulerJob>();
		this.initialize();
		
		this.setValidAPIValues();
		
	}
	
	public void initialize() {
		this.scheduledExecutorService =
			Executors.newScheduledThreadPool(this.threadCount);
	}
	
	public void uninitialize() {
		for(SchedulerJob schedulerJob : this.schedulerJobs.values()) {
			schedulerJob.stop();
		}
	}
	
	public void setScriptAPI(ScriptAPI scriptAPI) {
		this.scriptAPI = scriptAPI;
	}
	
	public void scheduleJob(String name, Script script, long initialDelay, long period, TimeUnit unit) throws Exception {
		
		if(this.schedulerJobs.containsKey(name)) {
			throw new Exception("Scheduler already has job called \"" + name + "\".");
		}
		
		SchedulerJob schedulerJob =
			new SchedulerJob(
				name,
				script,
				initialDelay,
				period,
				unit,
				this
			);
		
		this.schedulerJobs.put(
			name,
			schedulerJob
		);
		
		this.setValidAPIValues();
			
		schedulerJob.schedule();
		
	}
	
	public MapAction addJob(String name, Script script, long initialDelay, long period, TimeUnit unit) throws Exception {
		
		if(this.schedulerJobs.containsKey(name)) {
			
			return MapAction.ALREADY_EXISTS;
			
		}
		
		this.schedulerJobs.put(
			name,
			new SchedulerJob(
				name,
				script,
				initialDelay,
				period,
				unit,
				this
			)
		);
		
		this.setValidAPIValues();
		
		return MapAction.ADD;
		
	}
	
	public MapAction addJob(JSONDataStructure schedulerJobJSON, ScriptManager scriptManager) throws Exception {
		
		if(schedulerJobJSON.get("name") == null) {
			
			return MapAction.NONE;
			
		}
		
		return this.addJob(
			schedulerJobJSON.get("name").getValue().toString(),
			scriptManager.getScript(
				schedulerJobJSON.get("scriptName").getValue().toString()
			),
			Long.parseLong(
				schedulerJobJSON.get("initialDelay").getValue().toString()
			),
			Long.parseLong(
				schedulerJobJSON.get("period").getValue().toString()
			),
			TimeUnit.valueOf(
				schedulerJobJSON.get("timeUnit").getValue().toString()
			)
		);
		
	}
	
	public ScheduledExecutorService getScheduledExecutorService() {
		return this.scheduledExecutorService;
	}
	
	public ScriptAPI scriptAPI() {
		return this.scriptAPI;
	}
	
	public void stopJob(String name) {
		
		if(this.schedulerJobs.containsKey(name)) {
			this.schedulerJobs.get(name).stop();
		}
		
	}
	
	public void startJob(String name) {
		
		if(this.schedulerJobs.containsKey(name)) {
			
			SchedulerJob schedulerJob =
				this.schedulerJobs.get(name);
			
			if(!schedulerJob.isScheduled()) {
				schedulerJob.schedule();
			}
			
		}
		
	}
	
	public void deleteJob(String name) {
		
		if(this.schedulerJobs.containsKey(name)) {
			
			this.schedulerJobs.get(name).stop();
			
			this.schedulerJobs.remove(name);
			
			this.setValidAPIValues();
			
		}
		
	}
	
	public String getFullPathOfSchedulerJobLogFile(String name, String fileName) throws Exception {
		
		String fullPath =
			Scheduler.SCHEDULER_LOG_DIRECTORY + name + "/" + fileName;
		
		File f =
			new File(fullPath);
			
		if(f.exists() && !f.isDirectory()) {
			
			return fullPath;
			
		}
		
		return null;
		
	}
	
	public JSONBuilder getTimeUnitJSONBuilder() {
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(TimeUnit timeUnit : TimeUnit.values()) {
			jb.v(timeUnit);
		}
		
		return jb.$(']');
		
	}
	
	public JSONBuilder getJSONBuilder() {
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(SchedulerJob schedulerJob : this.schedulerJobs.values()) {
			jb.v(schedulerJob.getJSONBuilder());
		}
		
		return jb.$(']');
		
	}
	
	public JSONBuilder getJSONBuilderForExport() {
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(SchedulerJob schedulerJob : this.schedulerJobs.values()) {
			jb.v(schedulerJob.getJSONBuilderForExport());
		}
		
		return jb.$(']');
		
	}
	
	public String toJSON() {
		return this.getJSONBuilder().getJSON();
	}
	
	public void setValidAPIValues() {
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(SchedulerJob schedulerJob : this.schedulerJobs.values()) {
			jb.v(schedulerJob.name());
		}
		
		this.validAPIValues = jb.$(']').getJSON();
	}
	
	public String validAPIValues() {
		
		return this.validAPIValues;
		
	}
	
	public static void main(String args[]) {
		
		Scheduler s = new Scheduler(1);
		
		try {
			s.scheduleJob(
				"Test Job",
				new Script(
					"Test",
					"var test = {'k1': 1, 'k2': 2}; var test2 = JSON.stringify(test) + '\\nhello\\nworld'; output.writeBytes(test2);"
				),
				1,
				2,
				TimeUnit.SECONDS
			);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}