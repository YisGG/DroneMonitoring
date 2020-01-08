import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.lang.Math; 
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.*;
import java.util.HashMap;
import java.util.Map;

import java.util.Properties;

public class KafkaProducerTest implements Runnable{
    //Objeto que actuara de productor del topic de kafka
    public static Producer<String, String> producer;
    private String dataPath; //String que almacenara la ruta del archivo de donde leer los datos

    //Constructor
    public KafkaProducerTest(String p){
        this.dataPath = p;
    }

    //Tarea concurrente
    public void run(){
        //Lectura de archivo para los datos de entrada
        try{
            String data;
            data = new String(Files.readAllBytes(Paths.get(dataPath)));
            String[] data2 = data.split("\n");
            
            //Estructura que contendra la informacion de los drones del escuadron
            String[][] droneSquad = new String[5][];
            
            //Variables usadas durante el procesado de los datos del documento
            double[] centerDistance = new double[5];
            int nDronesProcessed = 0;
            int xCenterDrone;
            int yCenterDrone;
            int xSecondDrone;
            int ySecondDrone;
            String[] auxCoord;

            //Bucle que recorrera todas las lineas del documento para ir procesando los datos de entrada
            centerDistance[0] = 0.0;
            for(int i=0; i<data2.length; i++){
                //Split al String para obtener los distintos datos
                droneSquad[nDronesProcessed] = data2[i].split("><");
                if(nDronesProcessed < 4)
                    nDronesProcessed++;
                else{
                    //Reestablecer el contador para volver a empezar por el dron 1
                    nDronesProcessed = 0;
                    String aux = "";
                    //Procesando distancia al centro
                    auxCoord = droneSquad[0][3].split(",");
                    xCenterDrone = Integer.parseInt(auxCoord[0]);
                    yCenterDrone = Integer.parseInt(auxCoord[1]);
                    for(int j=0; j<droneSquad.length; j++){
                        //Calcular la distancia de cada dron hacia el dron central del escuadron (el dron X0)
                        auxCoord = droneSquad[j][3].split(",");
                        xSecondDrone = Integer.parseInt(auxCoord[0]);
                        ySecondDrone = Integer.parseInt(auxCoord[1]);
                        centerDistance[j] = Math.sqrt(((xCenterDrone-xSecondDrone)*(xCenterDrone-xSecondDrone)) + ((yCenterDrone-ySecondDrone)*(yCenterDrone-ySecondDrone)));
                        //System.out.println("Distancia entre el dron central y el dron numero " + j + " = " + centerDistance[j]);

                        //Preparando el mensaje para enfiarlo a kafka
                        //Comprobar si K debe ser 1 o 0
                        for(int k=1; k<droneSquad[j].length; k++){
                            aux += droneSquad[j][k];
                            if(k < droneSquad[j].length-1)
                                aux += "||";
                        }
                        
                        //Envio a kafka
                        producer.send(new ProducerRecord<String, String>("test", Integer.toString(i+j), aux));
                        Thread.sleep(500);
                    }
                    //System.out.println("x=" + xSecondDrone + " y=" + ySecondDrone + " a=" + droneSquad[j][3] + " j=" + j); 

                    




                    //Impresion auxiliar para la comprobacion
                    /*for(int j=0; j<droneSquad.length; j++){
                        for(int k=0; k<droneSquad[j].length; k++){
                            System.out.print(droneSquad[j][k] + " || ");
                        }
                        System.out.println();
                    }*/
                }
            }

        }catch(Exception e){}
    }

    public static void main(String[] args) {
        //Propiedades del conector de Kafka
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        //Objeto productor de kafka
        producer = new KafkaProducer<String, String>(props);

        //Generacion de hilos para procesar de forma concurrente las distintas entradas de datos
        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        threadPool.execute(new KafkaProducerTest("..\\InputData\\Squad1.txt"));
        //threadPool.execute(new KafkaProducerTest("..\\InputData\\Squad2.txt"));
        //threadPool.execute(new KafkaProducerTest("..\\InputData\\Squad3.txt"));

        threadPool.shutdown();
        try{
            threadPool.awaitTermination(1, TimeUnit.DAYS);
        }catch(InterruptedException e){}




        /*for (int i = 0; i < 100; i++)
            producer.send(new ProducerRecord<String, String>("test", Integer.toString(i), "Hello, this is the message number: " + Integer.toString(i)));
        */
        producer.close();
    }
}
