package dronemonitoring;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

public class KafkaMessageToDroneEventTransformer extends AbstractMessageTransformer{

	@Override
	public synchronized Map<String, Object> transformMessage(MuleMessage message, String outputEncoding) 
	throws TransformerException {
		// LinkedHashMap will iterate in the order in which the entries were put into the map
		//eventMap es el objeto que sera enviado al payload
		Map<String, Object> eventMap = new LinkedHashMap<String, Object>();
		//eventPayLoad es un Map que contendra los distintos atributos del evento simple con su nombre
		Map<String, Object> eventPayLoad = new LinkedHashMap<String, Object>();
		String kafkaMessage = "EMPTY";
		
		try {
			//Obtenemos los datos del mensaje de kafka que viene desde el payload
			kafkaMessage = message.getPayloadAsString();
			String[] droneData = kafkaMessage.split("><");

			//Introducimos los datos en el Map que ire al PayLoad
			eventPayLoad.put("droneId", droneData[0]);
			eventPayLoad.put("timeStamp", Integer.parseInt(droneData[1].trim()));
			String[] coord = droneData[2].split(",");
			eventPayLoad.put("posX", Integer.parseInt(coord[0].trim()));
			eventPayLoad.put("posY", Integer.parseInt(coord[1].trim()));
			String[] distanceSensors = droneData[3].split(",");
			eventPayLoad.put("distanceUp", Integer.parseInt(distanceSensors[0].trim()));
			eventPayLoad.put("distanceDown", Integer.parseInt(distanceSensors[1].trim()));
			eventPayLoad.put("distanceLeft", Integer.parseInt(distanceSensors[2].trim()));
			eventPayLoad.put("distanceRigth", Integer.parseInt(distanceSensors[3].trim()));
			
			eventMap.put("droneEvent", eventPayLoad);
			
		}catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }		
		
		System.out.println("===droneEvent created: " + eventMap);
		return eventMap;
	}
}
