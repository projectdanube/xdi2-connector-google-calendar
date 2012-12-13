package xdi2.connector.google.calendar.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.Graph;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.features.multiplicity.Multiplicity;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.XDIReaderRegistry;
import xdi2.core.xri3.impl.XDI3Segment;
import xdi2.core.xri3.impl.XDI3SubSegment;

public class GoogleCalendarMapping {

	public static final XDI3SubSegment XRI_S_GOOGLE_CALENDAR_CONTEXT = new XDI3SubSegment("+(https://www.googleapis.com/calendar/)");

	private static final Logger log = LoggerFactory.getLogger(GoogleCalendarMapping.class);

	private static GoogleCalendarMapping instance;

	private Graph mappingGraph;

	public GoogleCalendarMapping() {

		this.mappingGraph = MemoryGraphFactory.getInstance().openGraph();

		try {

			XDIReaderRegistry.getAuto().read(this.mappingGraph, GoogleCalendarMapping.class.getResourceAsStream("mapping.xdi"));
		} catch (Exception ex) {

			throw new Xdi2RuntimeException(ex.getMessage(), ex);
		}
	}

	public static GoogleCalendarMapping getInstance() {

		if (instance == null) instance = new GoogleCalendarMapping();

		return instance;
	}

	/**
	 * Converts a Google Calender calendar XRI to a native Google Calendar calendar identifier.
	 * Example: $(+calendar)$(!123) --> 123
	 */
	public String calendarXriToCalendarIdentifier(XDI3Segment calendarXri) {

		if (calendarXri == null) throw new NullPointerException();

		// convert

		String calendarIdentifier = (calendarXri.getNumSubSegments() < 2) ? null : Multiplicity.baseArcXri(calendarXri.getSubSegment(1)).toString();
		if (calendarIdentifier != null && calendarIdentifier.startsWith("!")) calendarIdentifier = calendarIdentifier.substring(1);

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + calendarXri + " to " + calendarIdentifier);

		return calendarIdentifier;
	}

	/**
	 * Converts a Google Calender event XRI to a native Google Calendar event identifier.
	 * Example: $(+event)$(!567) --> 567
	 */
	public String eventXriToEventIdentifier(XDI3Segment eventXri) {

		if (eventXri == null) throw new NullPointerException();

		// convert

		String eventIdentifier = (eventXri.getNumSubSegments() < 2) ? null : Multiplicity.baseArcXri(eventXri.getSubSegment(1)).toString();
		if (eventIdentifier != null && eventIdentifier.startsWith("!")) eventIdentifier = eventIdentifier.substring(1);

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + eventXri + " to " + eventIdentifier);

		return eventIdentifier;
	}

	/*
	 * Getters and setters
	 */

	public Graph getMappingGraph() {

		return this.mappingGraph;
	}

	public void setMappingGraph(Graph mappingGraph) {

		this.mappingGraph = mappingGraph;
	}
}
