package dronemonitoring;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.espertech.esper.runtime.client.EPStatement;

/**
 * @author Juan Boubeta-Puig <juan.boubeta@uca.es>
 * @author David Corral-Plaza <david.corral@uca.es>
 */
public class AddEventPatternToEsperComponent implements Callable {

	@Override
	public Object onCall(final MuleEventContext eventContext) throws Exception {
		final Object payload = eventContext.getMessage().getPayload();

		System.out.println("*** Adding new EPL " + payload.toString());

		final EPStatement pattern = EsperUtils.createEPL(payload.toString());

		pattern.addListener((newComplexEvents, oldComplexEvents, detectedEventPattern, epRuntime) -> {
			if (newComplexEvents != null) {
				// newComplexEvents[0].getUnderlying() is the payload of the
				// complex event detected by this listener.
				System.out.println("=====complex event payload:" + newComplexEvents[0].getUnderlying());

				// detectedEventPattern.getEventType().getName() specifies the
				// event pattern that has created this complex event.
				String eventPatternName = detectedEventPattern.getEventType().getName();
				System.out.println("=====eventPatternName: " + eventPatternName);

				// Create the detected complex event as a Java map
				// (eventPatternName, event properties)
				Map<String, Object> complexEvent = new LinkedHashMap<String, Object>();
				complexEvent.put(eventPatternName, newComplexEvents[0].getUnderlying());

				// Create the Mule message containing the complex event to be
				// sent to the ComplexEventReceptionAndDecisionMaking flow
				MuleMessage message = new DefaultMuleMessage(complexEvent, eventContext.getMuleContext());

				// Add messageProperties, a map containing eventPatternName, to
				// the Mule message
				Map<String, Object> messageProperties = new HashMap<String, Object>();
				messageProperties.put("eventPatternName", eventPatternName);
				message.addProperties(messageProperties, PropertyScope.OUTBOUND);

				// Send the created Mule message to the
				// ComplexEventConsumerGlobalVM connector located in the
				// ComplexEventReceptionAndDecisionMaking flow
				try {
					eventContext.getMuleContext().getClient().dispatch("ComplexEventConsumerGlobalVM", message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		return payload.toString();
	}
}