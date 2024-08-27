package main.java;

import java.io.*;
import java.util.*;
import java.util.Map;

public class ReadInput {
    public Map<String, Object> data = new HashMap<>(); // Initialize the map here

    public double fitness(double[][] solution) {
    int SizeOfCache = (Integer) data.get("cache_size");
    int[] fileSize = (int[]) data.get("video_size_desc");
    Map<String, String> video_ed_request = (HashMap<String, String>) data.get("video_ed_request");
    //System.out.println(video_ed_request);
    List<Integer> ep_to_dc_latency = (List<Integer>) data.get("ep_to_dc_latency");
    List<List<Integer>> ep_to_cache_latency = (List<List<Integer>>) data.get("ep_to_cache_latency");
    List<List<Integer>> ep_to_cache_list = (List<List<Integer>>) data.get("ed_cache_list");

//for each cache, if there is a file present, add size of file to running total
//iterate each cache
//inner loop  will iterate within each cache
   for(int cache =0; cache < solution.length; cache++){
        int total=0;
            // iterate through the cache, exit when cache is finished
        for(int file =0; file<solution[cache].length; file++){ 
                if(solution[cache][file]==1){
                    total = total + fileSize[file];
                }
                if(total>SizeOfCache){
                    return -1;
                }
            }
        }
// for each file request, check if the cache for the endpoint
// if not present, no time is saved
// else time saved = cost of download from dc - cost of download from cache 
    double totalGain=0;
    int totalRequests=0;
    for (String key : video_ed_request.keySet()){
        String[] parts = key.split(",");
        int videoId = Integer.parseInt(parts[0]);
        int endpointId = Integer.parseInt(parts[1]);
        int request = Integer.parseInt(video_ed_request.get(key));
    
        int dataCenterLatency = ep_to_dc_latency.get(endpointId);
        int cost = dataCenterLatency; //setting the minimum cost as dc to ep for now
       
        for (int cacheId : ep_to_cache_list.get(endpointId)){
            if (solution[cacheId][videoId] == 1) { // If the video is stored in the cache
                int cacheLatency = ep_to_cache_latency.get(endpointId).get(cacheId);
                if (cacheLatency < cost) {
                    cost = cacheLatency; // Found a better (lower) latency
                }
            }
        }
        double gain = dataCenterLatency - cost; // Calculate the gain
        totalGain += gain * request; // Update total gain
        totalRequests += request; // Update total requests
    }
       //  System.out.println((totalGain / totalRequests) * 1000);
         return (totalGain / totalRequests) * 1000;
    }

    public double[][] hillClimbing() {
        double[][] array = new double[(Integer) data.get("number_of_caches")][(Integer) data.get("number_of_videos")];
        double best_score = 0; 
        double[][] bestArray = new double[array.length][array[0].length];
        
        boolean improvement = true;
        while (improvement) {
            improvement = false;
            //for each cache
            for (int i = 0; i < array.length; i++) {
                //modify each file
                for (int j = 0; j < array[i].length; j++) {
                    if (array[i][j] == 0) { 
                        array[i][j] = 1; // test the change
                        double new_score = fitness(array); 
                       // System.out.println(Arrays.deepToString(array));
                       // System.out.println("current score = " + new_score);
                        if (new_score > best_score) {
                            best_score = new_score; // update best score
                            copyArray(bestArray, array); // save best array
                            improvement = true; 
                        //    System.out.println("Current Best" +Arrays.deepToString(bestArray)); 
                        }
                        array[i][j] = 0; // undo change
                    }
                    else if (array[i][j] == 1) { 
                        array[i][j] = 0; // test the change
                        double new_score = fitness(array); 
                       // System.out.println(Arrays.deepToString(array));
                       // System.out.println("current score = " + new_score);
                        if (new_score > best_score) {
                            best_score = new_score; // update best score
                            copyArray(bestArray, array); // save best array
                            improvement = true; 
                        //    System.out.println("Current Best" +Arrays.deepToString(bestArray)); 
                        }
                        array[i][j] = 1; // undo change
                    }
                }
            }
            if (improvement) {
                copyArray(array, bestArray);
            }
        }
      //  System.out.println("Best Array" +Arrays.deepToString(bestArray)); 
        System.out.println("Fitness = " + fitness(bestArray));
        return bestArray; // Return the best configuration found
    }
   //copy all elements of array into bestArray
   public void copyArray(double[][] dest, double[][] src) {
    for (int i = 0; i < src.length; i++) {
        System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
    }
}
    
public double[][] genetic(double[][][] population) {
   // double SizeOfCache = (Integer) data.get("cache_size");
    //int[] fileSize = (int[]) data.get("video_size_desc");
    double[][][] newPopulation = new double[population.length * 2][][];
// copy population into newPopulation
    for (int i = 0; i < population.length; i++) {
        newPopulation[i] = new double[population[i].length][];
        for (int j = 0; j < population[i].length; j++) {
            newPopulation[i][j] = population[i][j].clone(); 
        }
    }
  //  System.out.println("AFTER COPY " + newPopulation.length);
    double crossoverRate = 0.1;
    double mutationRate = 1/population.length;

    for (int generation = 0; generation < 100; generation++) {
        System.out.println("Generation = " + generation + " \n");
        int newPopIndex = population.length; // start inserting where population ends
        for (int i = 0; i < population.length; i++) {
            System.out.println("\n Fitness of Individual" + i + " = " + fitness(population[i]));
           // fitnessScores[i] = fitness(population[i]);
        }
        //crossover
        double fitnessScores[] = new double[population.length];
       // if(generation != 0){
            // System.out.println("\n sorted fitness scores \n");
            // for (int p = 0; p < population.length; p++) {
            //     System.out.format("individual "+ p +" = " + fitness(population[p]) + "\n");
            //     fitnessScores[p] = fitness(population[p]);
            // }
          //}
        for (int i = 0; i < population.length; i += 2) { 
            double random = Math.random();
            if (random < crossoverRate && (i + 1) < population.length) { 
                double[][] parent1 = population[i];
                double[][] parent2 = population[i + 1];
                double[][][] children = crossover(parent1, parent2);
                double[][] child1 = children[0];
                double[][] child2 = children[1];
                double fitness1 = fitness(child1);
                double fitness2 = fitness(child2);
                if (fitness(child1) != -1) {
                     System.out.println("New child added!");
                    newPopulation[newPopIndex++] = child1;     
                    System.out.println("\n fitness of new child = " +fitness(child1));          
                }               
                if (fitness(child2) != -1) {
                    System.out.println("New child added !");
                    newPopulation[newPopIndex++] = child2;
                    System.out.println("\n fitness of new child = " +fitness(child2));          
                   
                }
            }
        }
//mutation
// only mutate the children
for (int i = population.length; i < newPopIndex; i++) {
    mutation(newPopulation[i], mutationRate);
}
//selection
// select the top "populationSize" (50) individuals
population = sortPopulationByFitness(newPopulation, newPopIndex);
//System.out.println("THIS LENGTH SHOULD BE 50! " + population.length);
    }
 System.out.println("fitness = " + fitness(population[0]));
    return population[0];
} 

    public double[][][] crossover(double[][] parent1, double[][] parent2) {
        int individualLength = parent1.length;
        int crossoverPoint = individualLength / 2;
        
        double[][] child1 = new double[individualLength][];
        double[][] child2 = new double[individualLength][];
        
        for (int i = 0; i < crossoverPoint; i++) {
            child1[i] = parent1[i];
            child2[i] = parent2[i];
        }
        for (int i = crossoverPoint; i < individualLength; i++) {
            child1[i] = parent2[i];
            child2[i] = parent1[i];
        }
        
        return new double[][][]{child1, child2}; 
    }

    public void mutation(double[][] individual, double mutationRate) {
        Random random = new Random();
        for (int j = 0; j < individual.length; j++) {
            for (int k = 0; k < individual[j].length; k++) {
                if (random.nextDouble() < mutationRate) {
                    individual[j][k] = individual[j][k] == 0 ? 1 : 0; // Invert
                    System.out.println("\n mutation occured \n");
                }
            }
        }
    }
    
    public double[][][] sortPopulationByFitness(double[][][] population, double newPopIndex) {
        int n = (int)newPopIndex;
        double[] fitnessScores = new double[(int) newPopIndex];
    
        for (int i = 0; i < newPopIndex; i++) {
            System.out.println("Individual " + i + " = " + fitness(population[i]));
             fitnessScores[i] = fitness(population[i]);
        }

        // Selection sort
        for (int i = 0; i < n - 1; i++) {
            int maxIdx = i;
            //get highest fitness
            for (int j = i + 1; j < n; j++) {
                if (fitnessScores[j] > fitnessScores[maxIdx]) {
                    maxIdx = j;
                }
            }
         // Swapping fitnessScores
         double tempFitness = fitnessScores[maxIdx];
         fitnessScores[maxIdx] = fitnessScores[i];
         fitnessScores[i] = tempFitness;
 
         // Swapping the corresponding individuals in population
         double[][] tempIndividual = population[maxIdx];
         population[maxIdx] = population[i];
         population[i] = tempIndividual;
        }
// get only top 50
        double[][][] trimmedPopulation = new double[population.length/2][][];
        for (int i = 0; i < trimmedPopulation.length; i++) {

            System.out.println("Fitness of Individual " + i + " = " +fitness(population[i]));
           // fitnessScores[i] = fitness(population[i]);
            trimmedPopulation[i] = population[i];
        }
       return trimmedPopulation;
         //System.out.println("\n POPULATION LENGTH = " + population.length);
    }
     // FOR GENERATING SOLUTIONS FOR POPULATION
    public double[][][] generatePopulationFromHillClimbing(int populationSize) {
        double[][] baseSolution = hillClimbing();
        System.out.println("Base solution from hill climbing obtained.");
        double[][][] population = new double[populationSize][][];
        population[0] = baseSolution;
        for (int i = 1; i < populationSize; i++) {
            double[][] variant = mutateSolution(cloneSolution(baseSolution));
            population[i] = variant;
        }
        return population;
    }
    // FOR GENERATING SOLUTIONS FOR POPULATION
    private double[][] cloneSolution(double[][] solution) {
        double[][] clone = new double[solution.length][solution[0].length];
        for (int i = 0; i < solution.length; i++) {
            System.arraycopy(solution[i], 0, clone[i], 0, solution[i].length);
        }
        return clone;
    }
     // FOR GENERATING SOLUTIONS FOR POPULATION
    private double[][] mutateSolution(double[][] solution) {
        Random rand = new Random();
        int i = rand.nextInt(solution.length);
        int j = rand.nextInt(solution[0].length);
        solution[i][j] = 1 - solution[i][j]; 
        return solution;
    }
    
    public void readGoogle(String filename) throws IOException {
             
        BufferedReader fin = new BufferedReader(new FileReader(filename));
    
        String system_desc = fin.readLine();
        String[] system_desc_arr = system_desc.split(" ");
        int number_of_videos = Integer.parseInt(system_desc_arr[0]);
        int number_of_endpoints = Integer.parseInt(system_desc_arr[1]);
        int number_of_requests = Integer.parseInt(system_desc_arr[2]);
        int number_of_caches = Integer.parseInt(system_desc_arr[3]);
        int cache_size = Integer.parseInt(system_desc_arr[4]);
    
        Map<String, String> video_ed_request = new HashMap<String, String>();
        String video_size_desc_str = fin.readLine();
        String[] video_size_desc_arr = video_size_desc_str.split(" ");
        int[] video_size_desc = new int[video_size_desc_arr.length];
        for (int i = 0; i < video_size_desc_arr.length; i++) {
            video_size_desc[i] = Integer.parseInt(video_size_desc_arr[i]);
        }
    
        List<List<Integer>> ed_cache_list = new ArrayList<List<Integer>>();
        List<Integer> ep_to_dc_latency = new ArrayList<Integer>();
        List<List<Integer>> ep_to_cache_latency = new ArrayList<List<Integer>>();
        for (int i = 0; i < number_of_endpoints; i++) {
            ep_to_dc_latency.add(0);
            ep_to_cache_latency.add(new ArrayList<Integer>());
    
            String[] endpoint_desc_arr = fin.readLine().split(" ");
            int dc_latency = Integer.parseInt(endpoint_desc_arr[0]);
            int number_of_cache_i = Integer.parseInt(endpoint_desc_arr[1]);
            ep_to_dc_latency.set(i, dc_latency);
    
            for (int j = 0; j < number_of_caches; j++) {
                ep_to_cache_latency.get(i).add(ep_to_dc_latency.get(i) + 1);
            }
    
            List<Integer> cache_list = new ArrayList<Integer>();
            for (int j = 0; j < number_of_cache_i; j++) {
                String[] cache_desc_arr = fin.readLine().split(" ");
                int cache_id = Integer.parseInt(cache_desc_arr[0]);
                int latency = Integer.parseInt(cache_desc_arr[1]);
                cache_list.add(cache_id);
                ep_to_cache_latency.get(i).set(cache_id, latency);
            }
            ed_cache_list.add(cache_list);
        }
    
        for (int i = 0; i < number_of_requests; i++) {
            String[] request_desc_arr = fin.readLine().split(" ");
            String video_id = request_desc_arr[0];
            String ed_id = request_desc_arr[1];
            String requests = request_desc_arr[2];
            video_ed_request.put(video_id + "," + ed_id, requests);
        }
    
        data.put("number_of_videos", number_of_videos);
        data.put("number_of_endpoints", number_of_endpoints);
        data.put("number_of_requests", number_of_requests);
        data.put("number_of_caches", number_of_caches);
        data.put("cache_size", cache_size);
        data.put("video_size_desc", video_size_desc);
        data.put("ep_to_dc_latency", ep_to_dc_latency);
        data.put("ep_to_cache_latency", ep_to_cache_latency);
        data.put("ed_cache_list", ed_cache_list);
        data.put("video_ed_request", video_ed_request);

        fin.close();
     }

     public String toString() {
        String result = "";

        //for each endpoint: 
        for(int i = 0; i < (Integer) data.get("number_of_endpoints"); i++) {
            result += "enpoint number " + i + "\n";
            //latendcy to DC
            int latency_dc = ((List<Integer>) data.get("ep_to_dc_latency")).get(i);
            result += "latency to dc " + latency_dc + "\n";
            //for each cache
            for(int j = 0; j < ((List<List<Integer>>) data.get("ep_to_cache_latency")).get(i).size(); j++) {
                int latency_c = ((List<List<Integer>>) data.get("ep_to_cache_latency")).get(i).get(j); 
                result += "latency to cache number " + j + " = " + latency_c + "\n";
            }
        }
        return result;
    }
    
    public static void main(String[] args) throws IOException {  
        ReadInput ri = new ReadInput();
        ri.readGoogle("input/me_at_the_zoo.in");
        System.out.println(ri.data.get("video_ed_request"));
        System.out.println(ri.toString());

// FITNESS
       //    double[][] solution =
        //    {{0,0,1,0,0}, //cache 0 ; video 2 stored in cache 0
        //     {0,1,0,1,0}, // cache 1 ; videos 1 and 3 stored in cache 1
       //     {1,1,0,0,0}}; //cache 2 ; video 0 and 1 stored in cache 2     
       //   ri.fitness(solution);

// HILL CLIMBING
    //      double[][] solution = ri.hillClimbing();
    //   System.out.println("solution = " +Arrays.deepToString(solution)); 

// GENETIC
    int populationSize = 100;
    int numberOfCaches = (Integer) ri.data.get("number_of_caches");
    int numberOfVideos = (Integer) ri.data.get("number_of_videos");

   Random random = new Random();
   double[][][] population = new double[populationSize][numberOfCaches][numberOfVideos];

    population = ri.generatePopulationFromHillClimbing(populationSize);
    double[][] mostFitArray = ri.genetic(population);
    System.out.println("Most Fit Array = " + Arrays.deepToString(mostFitArray) + " with fitness of " + ri.fitness(mostFitArray));
 }
    }

