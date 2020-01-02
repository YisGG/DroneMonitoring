import java.nio.file.*; 

public class test{
    public static void main(String[] args)throws Exception{
        String data = "";
        data = new String(Files.readAllBytes(Paths.get("..\\DroneMonitoring\\InputData\\Squad1.txt")));
        String[] data2 = data.split("\n");
        System.out.println(data);
        for(int i=0; i<data2.length; i++)
            System.out.println("/" + data2[i]);
    }
}