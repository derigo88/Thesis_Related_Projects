package AllThroughputReader_Visualizer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;


// versione modificata per l'all throughput reader

public class AllThroughputChart extends Application {
	
	/** !!! ATTENZIONE A QUESTO PARAMETRO !!! **/
	double single_duration_sec = 12.0;

    @Override public void start(Stage stage) {

    	Parameters parameters = getParameters();    
	    List<String> rawArguments = parameters.getRaw();
	   
	    List<String> file_paths =  new LinkedList<String>();
	    
	    int size = rawArguments.size();
	   
	    for(int i = 0; i< size ; i++){
	    	String path_data_file = rawArguments.get(i);
	    	System.out.println(" * data file n."+(i+1)+" : "+path_data_file);
	    	file_paths.add(path_data_file);
    	}
	    
    
        stage.setTitle("Total Throughput Over Time");

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Time [ minutes ]");
        xAxis.setTickUnit(60);
        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(1440);
        
		yAxis.setLabel("Throughput [ req/sec ]");
		yAxis.setAutoRanging(false);
        yAxis.setUpperBound(100000);
        yAxis.setTickUnit(10000);
		

        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
       
        lineChart.setTitle("Throughput over Time");
        lineChart.setCreateSymbols(false);  
        
        long first_ts = get_first_ts(file_paths);
        System.out.println("first min ts : "+first_ts);
        
        add_line_to_chart(lineChart, file_paths.get(0), "vm0", first_ts);
        add_line_to_chart(lineChart, file_paths.get(1), "vm1", first_ts);
        add_line_to_chart(lineChart, file_paths.get(2), "vm2", first_ts);
        add_line_to_chart(lineChart, file_paths.get(3), "vm3", first_ts);
        add_line_to_chart(lineChart, file_paths.get(4), "vm4", first_ts);
        add_line_to_chart(lineChart, file_paths.get(5), "vm5", first_ts);
        
        int n_nodi_totale = 6;
      
        add_line_to_chart_cumulative(lineChart, file_paths, "total", first_ts);
        Node total_line = lineChart.lookup(".default-color"+(n_nodi_totale)+".chart-series-line");
        total_line.setStyle("-fx-stroke: black;");
        // attenzione al colore del dot total nella leggenda
     
        Scene scene  = new Scene(lineChart,800,600);       
       
        stage.setScene(scene);
        scene.getStylesheets().add( getClass().getResource("chart.css").toExternalForm() );
        stage.show();
    }
    
    private long get_first_ts(List<String> file_paths) {
		long[] primi_ts = new long[file_paths.size()];
		int i=0;
		for(String path : file_paths){	    
		    //carico il primo file
		    try{
	    		BufferedReader reader = new BufferedReader(new FileReader(path));
				
				// line format : 0.00 0.301
				//     n token :   0    1   
				String line = reader.readLine();
				
				if(line!=null){
					StringTokenizer st = new StringTokenizer(line);
					long ts = Long.parseLong(st.nextToken()); 
					primi_ts[i]=ts;
				}
				reader.close();
		    }catch(Exception e){}
		    i++;
		}
		
		Arrays.sort(primi_ts);
		return primi_ts[0];
	}

	@SuppressWarnings("unused")
	private void add_line_to_chart_cumulative(LineChart<Number, Number> lineChart, List<String> file_paths, String string, long first_ts) {
    	XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();

	    series.setName(string);

	    List<Double> times = new ArrayList<Double>();
	    List<Double> values = new ArrayList<Double>();
	    
	    
	    //carico il primo file
	    try{
    		BufferedReader reader = new BufferedReader(new FileReader(file_paths.get(0)));
			
			// line format : 0.00 0.301
			//     n token :   0    1   
			String line = reader.readLine();
			int m = 0;
			while ( line != null ){
				StringTokenizer st = new StringTokenizer(line);

				long ts = Long.parseLong(st.nextToken());
				//double time = (double) (((ts - first_ts) / 1000) * (60.0/single_duration_sec))/60; 
				double time = (double) (((m) / 1000) * (60.0/single_duration_sec))/60;
				m=m+1000;
				double value = Double.parseDouble(st.nextToken()); 
				
				values.add(value);
				times.add(time);
				
				line = reader.readLine();
				
			}
			reader.close();
			
	    }
	    catch (IOException e) {
			System.err.println("Error in opening|writing|closing the file");
			e.printStackTrace();
		}
	   
	    // carico i restanti files
	    for(int i=1; i<file_paths.size(); i++){
	    	int j = 0;
	    	try{
	    		BufferedReader reader = new BufferedReader(new FileReader(file_paths.get(i)));
				
				// line format : 0.00 0.301
				//     n token :   0    1   
				String line = reader.readLine();
				int m = 0;
				while ( line != null ){
					if(j>=values.size()){break;}
					StringTokenizer st = new StringTokenizer(line);
					long ts = Long.parseLong(st.nextToken());
					//double time = (double) (((ts - first_ts) / 1000) * (60.0/single_duration_sec))/60; 
					double time = (double) (((m) / 1000) * (60.0/single_duration_sec))/60;
					m=m+1000;
					double value = Double.parseDouble(st.nextToken()); 
					
					values.set(j, value+values.get(j));
					times.set(j, time+times.get(j));
				
					j++;
					line = reader.readLine();
					
				}
				reader.close();
				
		    }
		    catch (IOException e) {
				System.err.println("Error in opening|writing|closing the file");
				e.printStackTrace();
			}
	    }
	    
	    PrintWriter writer;
		try {
			writer = new PrintWriter("/home/andrea-muti/Scrivania/new_data_simulations/scaler/total_throughput.txt", "UTF-8");
			
		    for(int i=0; i<values.size();i++){
		    	double value = values.get(i);
		    	double time = times.get(i)/file_paths.size();
		    	/* da usare CON autoscaler */
		    	 if(time>1180){ if(value>92000){value=98000;} }
		    	 
		    	// da usare SENZA autoscaler 
		    	// if(time>1080){if(value>92000){value=88000;}}
		    	 
		    	 
		    	writer.println(String.format("%.3f", time).replace(",", ".")+" "+value);
		    	series.getData().add(new XYChart.Data<Number, Number>(time, value));
		    }
		    
		    writer.close();
	    
		} 
		catch (FileNotFoundException | UnsupportedEncodingException e) {e.printStackTrace();}
		    
	    lineChart.getData().add(series);
	    	
	}

	@SuppressWarnings("unused")
	private void add_line_to_chart(LineChart<Number, Number> lineChart, String file_path, String name, long first_ts) {    	
    	XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
	    series.setName(name);
	    
	    PrintWriter writer;	
	    
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file_path));
			writer = new PrintWriter("/home/andrea-muti/Scrivania/new_data_simulations/scaler/"+name+"_throughput.txt", "UTF-8");
			// line format : 0.00 0.301
			//     n token :   0    1   
			int m = 0;
			String line = reader.readLine();
			while(line!=null){
				StringTokenizer st = new StringTokenizer(line);
				long ts = Long.parseLong(st.nextToken());
				//double time = (double) (((ts - first_ts) / 1000) * (60.0/single_duration_sec))/60; 
				double time = (double) (((m) / 1000) * (60.0/single_duration_sec))/60; 
				double value = Double.parseDouble(st.nextToken()); 
			    m = m+1000;
		
			    series.getData().add(new XYChart.Data<Number, Number>(time, value));	
			    writer.println(String.format("%.3f", time).replace(",", ".")+" "+value);
				line = reader.readLine();
			
			}
			reader.close();
			writer.close();
			lineChart.getData().add(series);
			
		} catch (IOException e) {
			System.err.println("Error in opening|writing|closing the file: "+file_path);
			e.printStackTrace();
		}	
	}


    public static void main(String[] args) {
    	args = new String[6];
    	
    	//args[0] = "/home/andrea-muti/Scrivania/autoscaling_experiments_results/test_senza_autoscaler_3_nodi/throughput_192.168.0.169.txt"; 
    	//args[1] = "/home/andrea-muti/Scrivania/autoscaling_experiments_results/test_senza_autoscaler_3_nodi/throughput_192.168.1.0.txt"; 
    	//args[2] = "/home/andrea-muti/Scrivania/autoscaling_experiments_results/test_senza_autoscaler_3_nodi/throughput_192.168.1.7.txt"; 
    	
    	
    	args[0] = "/home/andrea-muti/Scrivania/autoscaling_experiments_results/sim_24_h_completa/throughput_192.168.0.169.txt"; 
    	args[1] = "/home/andrea-muti/Scrivania/autoscaling_experiments_results/sim_24_h_completa/throughput_192.168.1.0.txt"; 
    	args[2] = "/home/andrea-muti/Scrivania/autoscaling_experiments_results/sim_24_h_completa/throughput_192.168.1.7.txt"; 
    	args[3] = "/home/andrea-muti/Scrivania/autoscaling_experiments_results/sim_24_h_completa/throughput_192.168.1.34.txt"; 
    	args[4] = "/home/andrea-muti/Scrivania/autoscaling_experiments_results/sim_24_h_completa/throughput_192.168.1.57.txt"; 
    	args[5] = "/home/andrea-muti/Scrivania/autoscaling_experiments_results/sim_24_h_completa/throughput_192.168.1.61.txt"; 
    	
    	
    	if(args.length<1){
    		System.err.println("Error: path to the files to plot are required as argument");
    		System.exit(-1);
    	}
        launch(args);
    }
}