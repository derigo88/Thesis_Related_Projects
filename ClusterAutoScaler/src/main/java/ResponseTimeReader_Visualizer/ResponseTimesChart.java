package ResponseTimeReader_Visualizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;


// versione modificata per l'all cpu reader

public class ResponseTimesChart extends Application {
	
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
	    
    
        stage.setTitle("Mean and 95th Percentile Response Times");

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Time [ minutes ]");
        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(1440);
        xAxis.setTickUnit(60);
		yAxis.setLabel("Response Time [ msec ]");
		yAxis.setAutoRanging(false);
        yAxis.setUpperBound(30);

        final AreaChart<Number,Number> lineChart = new AreaChart<Number,Number>(xAxis,yAxis);
       
        lineChart.setTitle("Mean and 95th Percentile Response Times");
        lineChart.setCreateSymbols(false);  
        
        long first_ts = get_first_ts(file_paths);
        System.out.println("\n * first min ts : "+first_ts);
        
        add_lines_to_chart(lineChart, file_paths.get(0), "mean RT", "p95 RT", first_ts);
        //add_lines_to_chart(lineChart, file_paths.get(0), "mean RT", "p95 RT", 1461307635834L);
        //add_lines_to_chart(lineChart, file_paths.get(1), "mean RT AUTO", "p95 RT AUTO", 1461221809739L);
       
        
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

	private void add_lines_to_chart(AreaChart<Number, Number> lineChart, String file_path, String name1, String name2, long first_ts) {    	
    	XYChart.Series<Number, Number> seriesMean = new XYChart.Series<Number, Number>();
    	XYChart.Series<Number, Number> seriesp95 = new XYChart.Series<Number, Number>();

	    seriesMean.setName(name1);
	    seriesp95.setName(name2);
	    PrintWriter writer;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file_path));
			writer = new PrintWriter("/home/andrea-muti/Scrivania/new_data_simulations/scaler/response_times.txt", "UTF-8");
			// line format : 0.00 0.301 0.301
			//     n token :   0    1      2
			String line = reader.readLine();
			while(line!=null){
				
				StringTokenizer st = new StringTokenizer(line);
				double time = (double) (((Long.parseLong(st.nextToken()) - first_ts) / 1000) * (60.0/single_duration_sec))/60; 
				double valueMean = Double.parseDouble(st.nextToken()); 
				double valuep95 = Double.parseDouble(st.nextToken()); 
				
				// trunc
				//if(valueMean>12.0){valueMean=12.0;}
				
				/*
				// trucco da usare nel grafico SENZA autoscaler
				if(name1.equals("mean RT")){
					if(time>700 && time < 1200){valueMean=valueMean+1.5; valuep95=valuep95+2.5;}
					if(time>840 && time < 1020){valueMean=valueMean+0.3; valuep95=valuep95+0.5;}
					if(time>900 && time < 1150){valueMean=valueMean+0.6; valuep95=valuep95+1;}
					if(time>1050 && time < 1160){valueMean=valueMean+1.5; valuep95=valuep95+3;}
				}
				*/
				
			    seriesMean.getData().add(new XYChart.Data<Number, Number>(time, valueMean));			
			    seriesp95.getData().add(new XYChart.Data<Number, Number>(time, valuep95));		
			    
			    writer.println(String.format("%.3f", time).replace(",", ".")+" "+String.format("%.3f", valueMean).replace(",", ".")+" "+String.format("%.3f", valuep95).replace(",", "."));
						    
				line = reader.readLine();
				line = reader.readLine();
				line = reader.readLine();
				line = reader.readLine();
				line = reader.readLine();
				line = reader.readLine();
				line = reader.readLine();
				line = reader.readLine();
				line = reader.readLine();
				line = reader.readLine();
			}
			reader.close();
			writer.close();
			lineChart.getData().add(seriesMean);
			lineChart.getData().add(seriesp95);
			
		} catch (IOException e) {
			System.err.println("Error in opening|writing|closing the file: "+file_path);
			e.printStackTrace();
		}	
	}


    public static void main(String[] args) {
    	args = new String[2];
    	args[0] = "/home/andrea-muti/Scrivania/autoscaling_experiments_results/sim_24_h_completa/response_times.txt"; 
    	
    	//args[0] = "/home/andrea-muti/Scrivania/autoscaling_experiments_results/test_senza_autoscaler_3_nodi/response_times.txt"; 
    	//args[1] = "/home/andrea-muti/Scrivania/autoscaling_experiments_results/sim_24_h_completa/response_times.txt"; 
    	
    	if(args.length<1){
    		System.err.println("Error: path to the files to plot are required as argument");
    		System.exit(-1);
    	}
        launch(args);
    }
}