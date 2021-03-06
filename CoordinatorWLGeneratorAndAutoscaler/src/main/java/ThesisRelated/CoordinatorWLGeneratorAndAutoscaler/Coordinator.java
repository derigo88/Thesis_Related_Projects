package ThesisRelated.CoordinatorWLGeneratorAndAutoscaler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import AllCPUReader_Visualizer.AllCPUReader;
import AllThroughputReader_Visualizer.AllThroughputReader;
import ResponseTimeReader_Visualizer.ResponseTimeReader;
import ThesisRelated.ClusterWorkloadGenerator.WorkloadGenerator;
import node_number_monitor.NodeNumberMonitor;
import ThesisRelated.ClusterAutoScaler.AutoScaler;

/**
 * Coordinator that coordinates the execution of 
 * 	 - the WorkloadGenerator 
 * 	 - the AutoScaler
 * 	 - the monitor of the number of nodes in the cluster
 *   - the monitor of the throughput of the nodes
 *   - the monitor of the CPU utilization of the nodes
 *   - the monitor of the Response Times
 * 
 * @author andrea-muti
 * @since 06-04-2016
 */

public class Coordinator {
    public static void main( String[] args ){
    	System.out.println(" *----------------------*");
        System.out.println(" *-- TEST COORDINATOR --*");
        System.out.println(" *----------------------*\n");
        
        int number_hour_initial_shift = 16;
        String contact_point = "192.168.0.169";
        String jmx_port = "7199";
        String result_dir_path = "/home/andrea-muti/Scrivania/autoscaling_experiments_results/";
        
        final  CountDownLatch latch = new CountDownLatch(1);
        GeneratorExecutorRunnable generatorExecutor = new GeneratorExecutorRunnable(latch, number_hour_initial_shift);
        AutoscalerExecutorRunnable autoscalerExecutor = new AutoscalerExecutorRunnable(latch, number_hour_initial_shift);
        NumberMonitorRunnable numberMonitorExeutor = new NumberMonitorRunnable(latch, contact_point, jmx_port, result_dir_path);
        ThroughputMonitorRunnable throughputMonitorExeutor = new ThroughputMonitorRunnable(latch, contact_point, jmx_port, result_dir_path);
        CpuMonitorRunnable cpuMonitorExeutor = new CpuMonitorRunnable(latch, contact_point, jmx_port, result_dir_path);
        RTMonitorRunnable rtMonitorExeutor = new RTMonitorRunnable(latch, contact_point, jmx_port, result_dir_path);
        
        Thread generatorModule = new Thread(generatorExecutor);
        Thread autoscalerModule = new Thread(autoscalerExecutor);
        Thread numberMonitor = new Thread(numberMonitorExeutor);
        Thread throughputMonitor = new Thread(throughputMonitorExeutor);
        Thread cpuMonitor = new Thread(cpuMonitorExeutor);
        Thread rtMonitor = new Thread(rtMonitorExeutor);
        
        try{ Thread.sleep(4000); }
        catch( Exception e ){}
        System.out.println("");

        generatorModule.start();
        autoscalerModule.start();
        numberMonitor.start();
        throughputMonitor.start();
        cpuMonitor.start();
        rtMonitor.start();
        
        try{ Thread.sleep(4000); }
        catch( Exception e ){}
        System.out.println("");
        
        // solo dopo che il latch è andato a 0 i vari thread, che sono in attesa sul latch, partiranno davvero
        latch.countDown();  
     }
}

/*--------------------------------------------------------------------------------------------------------------*/

/**
 * Executor Thread for the monitor of the Response Times
 */
class RTMonitorRunnable implements Runnable{
	
	private final CountDownLatch latch;
    private ResponseTimeReader monitor;
	
	public RTMonitorRunnable(CountDownLatch latch, String contact_point, String jmx_port, String result_dir_path){
		this.latch=latch;
		this.monitor=new ResponseTimeReader(contact_point, jmx_port, result_dir_path);
	}

	@Override
	public void run() {
		try {
        	System.out.println(" - [RTMonitor Executor] RTMonitor awaiting start");
            latch.await();          //The thread keeps waiting till it is informed
        } catch (InterruptedException e) {
        	System.err.println(" - [RTMonitor Executor] RTMonitor thread awaiting to start has been interrupet");
            e.printStackTrace();
            System.err.println(" - [RTMonitor Executor] ABORTING EXECUTION");
            System.exit(0);
        }
        System.out.println(" - [RTMonitor Executor] start monitoring Response Times of nodes");
        this.monitor.start_read_RT();
        System.out.println(" - [RTMonitorExecutor] stop monitoring Response Times of nodes");
	}
	
	public void terminate(){
    	this.monitor.stop_read_RT();
    	System.out.println(" - [RTMonitorExecutor] RTMonitor terminated");
    	Thread.currentThread().interrupt();
    }
	
}

/*--------------------------------------------------------------------------------------------------------------*/

/**
 * Executor Thread for the monitor of nodes CPU Utilization
 */
class CpuMonitorRunnable implements Runnable{
	
	private final CountDownLatch latch;
    private AllCPUReader monitor;
	
	public CpuMonitorRunnable(CountDownLatch latch, String contact_point, String jmx_port, String result_dir_path){
		this.latch=latch;
		this.monitor=new AllCPUReader(contact_point, jmx_port, result_dir_path);
	}

	@Override
	public void run() {
		try {
        	System.out.println(" - [CpuMonitor Executor] CpuMonitor awaiting start");
            latch.await();          //The thread keeps waiting till it is informed
        } catch (InterruptedException e) {
        	System.err.println(" - [CpuMonitor Executor] CpuMonitor thread awaiting to start has been interrupet");
            e.printStackTrace();
            System.err.println(" - [CpuMonitor Executor] ABORTING EXECUTION");
            System.exit(0);
        }
        System.out.println(" - [CpuMonitor Executor] start monitoring CPU utilization of nodes");
        this.monitor.start_read_cpu();
        System.out.println(" - [CpuMonitor Executor] stop monitoring CPU utilization of nodes");
	}
	
	public void terminate(){
    	this.monitor.stop_read_cpu();
    	System.out.println(" - [CpuMonitor Executor] CpuMonitor terminated");
    	Thread.currentThread().interrupt();
    }
	
}


/*--------------------------------------------------------------------------------------------------------------*/ 

/**
 * Executor Thread for the Monitor of the Throughput
 */
class ThroughputMonitorRunnable implements Runnable{
	
	private final CountDownLatch latch;
    private AllThroughputReader monitor;
	
	public ThroughputMonitorRunnable(CountDownLatch latch, String contact_point, String jmx_port, String result_dir_path){
		this.latch=latch;
		this.monitor=new AllThroughputReader(contact_point, jmx_port, result_dir_path);
	}

	@Override
	public void run() {
		try {
        	System.out.println(" - [ThroughputMonitor Executor] ThroughputMonitor awaiting start");
            latch.await();          //The thread keeps waiting till it is informed
        } catch (InterruptedException e) {
        	System.err.println(" - [ThroughputMonitor Executor] ThroughputMonitor thread awaiting to start has been interrupet");
            e.printStackTrace();
            System.err.println(" - [ThroughputMonitor Executor] ABORTING EXECUTION");
            System.exit(0);
        }
        System.out.println(" - [ThroughputMonitor Executor] start monitoring throughput of nodes");
        this.monitor.start_read_throughput();
        System.out.println(" - [ThroughputMonitor Executor] stop monitoring throughput of nodes");
	}
	
	public void terminate(){
    	this.monitor.stop_read_throughput();
    	System.out.println(" - [ThroughputMonitor Executor] ThroughputMonitor terminated");
    	Thread.currentThread().interrupt();
    }
}

/*--------------------------------------------------------------------------------------------------------------*/ 

/**
 * Executor Thread for the Monitor of the Number of Nodes
 */
class NumberMonitorRunnable implements Runnable{
	
	private final CountDownLatch latch;
    private NodeNumberMonitor monitor;
	
	public NumberMonitorRunnable(CountDownLatch latch, String contact_point, String jmx_port, String result_dir_path){
		this.latch=latch;
		this.monitor=new NodeNumberMonitor(contact_point, jmx_port, result_dir_path);
	}

	@Override
	public void run() {
		try {
        	System.out.println(" - [NodeNumberMonitor Executor] NodeNumberMonitor awaiting start");
            latch.await();          //The thread keeps waiting till it is informed
        } catch (InterruptedException e) {
        	System.err.println(" - [NodeNumberMonitor Executor] NodeNumberMonitor thread awaiting to start has been interrupet");
            e.printStackTrace();
            System.err.println(" - [NodeNumberMonitor Executor] ABORTING EXECUTION");
            System.exit(0);
        }
        System.out.println(" - [NodeNumberMonitor Executor] start monitoring the number of nodes");
        this.monitor.start_monitor();
        System.out.println(" - [NodeNumberMonitor Executor] stop monitoring the number of nodes");
	}
	
	public void terminate(){
    	this.monitor.stop_monitor();
    	System.out.println(" - [NodeNumberMonitor Executor] NodeNumberMonitor terminated");
    	Thread.currentThread().interrupt();
    }
}


//---------------------------------------------------------------------------------------------------------

/**
 * Executor Thread for the WORKLOAD GENERATOR
 */
class GeneratorExecutorRunnable implements Runnable{
    private final CountDownLatch latch;
    private WorkloadGenerator generator;
    private int number_hours_initial_shift;
 
    // reading various required properties
    String generator_properties_path = "resources/properties_files/generator.props";   
    String jmeter_properties_path = "resources/properties_files/jmeter.props";
     
    public GeneratorExecutorRunnable(CountDownLatch latch, int number_hours_initial_shift){
        this.latch = latch;
        this.number_hours_initial_shift=number_hours_initial_shift;
        this.generator = new WorkloadGenerator(generator_properties_path, jmeter_properties_path);
        
        // creazione del JMeter execution plan che verrà usato dal JMRunner (JMeterController)
        System.out.print(" - [WorkloadGenerator Executor] generation JMeter execution plan : ");
    	boolean plan_creation = generator.generate_jmeter_plan_with_initial_shift(this.number_hours_initial_shift);
        if(!plan_creation){
        	System.err.println(" FAILED");
        	System.exit(0);
        }
        else{System.out.println(" DONE"); }
        
        System.out.println(" - [WorkloadGenerator Executor] WorkloadGenerator initialized");
        System.out.println(" - [WorkloadGenerator Executor] start of generated workload will be shifted by "+this.number_hours_initial_shift+" hours with respect to the workload file");
        long approx_total_dur_sec = generator.get_avg_workload_duration_sec() - ((60*this.number_hours_initial_shift)*generator.get_single_duration_sec());
        int hours = (int) (approx_total_dur_sec / 3600); // get the amount of hours from the seconds
        int minutes = (int) ((approx_total_dur_sec - (hours*3600))/ 60)  ; // convert seconds (saved in "time") to minutes
		int seconds = (int) (approx_total_dur_sec) - (hours*3600) - minutes*60; // get the rest
		String disHour = (hours < 10 ? "0" : "") + hours; // get hours and add "0" before if lower than 10
		String disMinu = (minutes < 10 ? "0" : "") + minutes; // get minutes and add "0" before if lower than 10
		String disSec = (seconds < 10 ? "0" : "") + seconds; // get seconds and add "0" before if lower than 10
		String formattedTime = disHour+":"+disMinu + ":" + disSec; //get the whole time
        double approx_dur_min = approx_total_dur_sec/60.0;
        String form_dur_min = String.format("%.0f", approx_dur_min);
        System.out.println(" - [WorkloadGenerator Executor] 1 minute of workload will be generated in "+generator.get_single_duration_sec()+" sec of real time");
        System.out.println(" - [WorkloadGenerator Executor] 1 minute of real time corresponds to "+(60/generator.get_single_duration_sec())+" minutes of workload time");
        System.out.println(" - [WorkloadGenerator Executor] total approx. simulation duration: "+formattedTime+"  ("+form_dur_min+":"+disSec+" min) ("+approx_total_dur_sec+" sec)");
    }
 
    @Override
    public void run() {
        try {
        	System.out.println(" - [WorkloadGenerator Executor] WorkloadGenerator awaiting start");
            latch.await();          //The thread keeps waiting till it is informed
        } catch (InterruptedException e) {
        	System.err.println(" - [WorkloadGenerator Executor] WorkloadGenerator thread awaiting to start has been interrupet");
            e.printStackTrace();
            System.err.println(" - [WorkloadGenerator Executor] ABORTING EXECUTION");
            System.exit(0);
        }
        System.out.println(" - [WorkloadGenerator Executor] start generating workload");
        this.generator.generateWorkload();
        System.out.println(" - [WorkloadGenerator Executor] workload generation completed");
    }
}

// -------------------------------------------------------------------------------------

/**
 * Executor Thread for the AUTOSCALER
 */
class AutoscalerExecutorRunnable implements Runnable{
	
    private final CountDownLatch latch;
    private AutoScaler autoscaler;
    private int single_duration_sec;
    private int scaling_factor;
    private int number_hours_initial_shift;
    
    String autoscaler_props_file = "resources/properties_files/autoscaler.properties";
    String predictor_props_file = "resources/properties_files/predictor.properties";
    String generator_pros_file = "resources/properties_files/generator.props";
    String configuration_manager_props_file = "resources/properties_files/propertiesCM.properties";
    
    public AutoscalerExecutorRunnable(CountDownLatch latch,  int number_hours_initial_shift){
    	this.number_hours_initial_shift=number_hours_initial_shift;
        this.latch = latch;
    	this.single_duration_sec = get_single_duration_sec();
    	this.scaling_factor = get_scaling_factor();
        this.autoscaler = new AutoScaler( autoscaler_props_file, predictor_props_file, configuration_manager_props_file,
        		this.single_duration_sec,  this.scaling_factor, this.number_hours_initial_shift);
        System.out.println(" - [AutoScaler Executor] AutoScaler initialized");
    }
 
    @Override
    public void run() {
        try {
            System.out.println(" - [AutoScaler Executor] AutoScaler awaiting start");
            latch.await();          //The thread keeps waiting till it is informed
        } catch (InterruptedException e) {
        	System.err.println(" - [AutoScaler Executor] the AutoScaler thread awaiting to start has been interrupet");
            e.printStackTrace();
            System.err.println(" - [AutoScaler Executor] ABORTING EXECUTION");
            System.exit(0);
        }
        System.out.println(" - [AutoScaler Executor] AutoScaler starts execution");
        this.autoscaler.start();
        System.out.println(" - [AutoScaler Executor] AutoScaler execution end");
    }
    
    
    private int get_single_duration_sec(){
		File propFile = new File(generator_pros_file);
		Properties properties = new Properties();
		try { properties.load(new FileReader(propFile)); } 
		catch (IOException e) {
			System.err.println(" - [AutoScaler Executor] ERROR reading generator properties file");
			System.exit(0);
		}		
		return Integer.parseInt(properties.getProperty("single_duration_sec"));
    }
    private int get_scaling_factor(){
		File propFile = new File(generator_pros_file);
		Properties properties = new Properties();
		try { properties.load(new FileReader(propFile)); } 
		catch (IOException e) {
			System.err.println(" - [AutoScaler Executor] ERROR reading generator properties file");
			System.exit(0);
		}		
		return Integer.parseInt(properties.getProperty("scaling_factor").trim());
    }
    
    public void terminate(){
    	this.autoscaler.stop();
    	System.out.println(" - [AutoScaler Executor] AutoScaler terminated");
    	Thread.currentThread().interrupt();
    }
}