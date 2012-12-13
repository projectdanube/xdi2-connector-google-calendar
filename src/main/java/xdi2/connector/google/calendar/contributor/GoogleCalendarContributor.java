package xdi2.connector.google.calendar.contributor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.connector.google.calendar.api.GoogleCalendarApi;
import xdi2.connector.google.calendar.mapping.GoogleCalendarMapping;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.features.multiplicity.Multiplicity;
import xdi2.core.xri3.impl.XDI3Segment;
import xdi2.core.xri3.impl.XDI3SubSegment;
import xdi2.messaging.GetOperation;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.ExecutionContext;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.Prototype;
import xdi2.messaging.target.contributor.AbstractContributor;
import xdi2.messaging.target.contributor.ContributorXri;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.messaging.target.interceptor.MessageEnvelopeInterceptor;
import xdi2.messaging.target.interceptor.MessagingTargetInterceptor;

@ContributorXri(addresses={"+(https://www.googleapis.com/calendar/)"})
public class GoogleCalendarContributor extends AbstractContributor implements MessagingTargetInterceptor, MessageEnvelopeInterceptor, Prototype<GoogleCalendarContributor> {

	private static final Logger log = LoggerFactory.getLogger(GoogleCalendarContributor.class);

	private Graph tokenGraph;
	private GoogleCalendarApi googleCalendarApi;
	private GoogleCalendarMapping googleCalendarMapping;

	public GoogleCalendarContributor() {

		super();

		this.getContributors().addContributor(new GoogleCalendarEnabledContributor());
		this.getContributors().addContributor(new GoogleCalendarUserContributor());
	}

	/*
	 * Prototype
	 */

	@Override
	public GoogleCalendarContributor instanceFor(PrototypingContext prototypingContext) throws Xdi2MessagingException {

		// create new contributor

		GoogleCalendarContributor contributor = new GoogleCalendarContributor();

		// set api and mapping

		contributor.setGoogleCalendarApi(this.getGoogleCalendarApi());
		contributor.setGoogleCalendarMapping(this.getGoogleCalendarMapping());

		// done

		return contributor;
	}

	/*
	 * MessagingTargetInterceptor
	 */

	@Override
	public void init(MessagingTarget messagingTarget) throws Exception {

		// set the token graph

		if (this.tokenGraph == null && messagingTarget instanceof GraphMessagingTarget) {

			this.setTokenGraph(((GraphMessagingTarget) messagingTarget).getGraph());
		}
	}

	@Override
	public void shutdown(MessagingTarget messagingTarget) throws Exception {

	}

	/*
	 * MessageEnvelopeInterceptor
	 */

	@Override
	public boolean before(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		GoogleCalendarContributorExecutionContext.resetUsers(executionContext);

		return false;
	}

	@Override
	public boolean after(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		return false;
	}

	@Override
	public void exception(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext, Exception ex) {

	}

	/*
	 * Sub-Contributors
	 */

	@ContributorXri(addresses={"$!(+enabled)"})
	private class GoogleCalendarEnabledContributor extends AbstractContributor {

		@Override
		public boolean getContext(XDI3Segment[] contributorXris, XDI3Segment relativeContextNodeXri, XDI3Segment contextNodeXri, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			messageResult.getGraph().findContextNode(contextNodeXri, true).createLiteral("1");

			return true;
		}
	}

	@ContributorXri(addresses={"($$!)"})
	private class GoogleCalendarUserContributor extends AbstractContributor {

		private GoogleCalendarUserContributor() {

			super();

			this.getContributors().addContributor(new CalendarContributor());
		}

		@Override
		public boolean getContext(XDI3Segment[] contributorXris, XDI3Segment relativeContextNodeXri, XDI3Segment contextNodeXri, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			XDI3Segment googleCalendarContextXri = contributorXris[contributorXris.length - 3];
			XDI3Segment userXri = contributorXris[contributorXris.length - 2];

			log.debug("googleCalendarContextXri: " + googleCalendarContextXri + ", userXri: " + userXri);

			ContextNode contextNode = messageResult.getGraph().findContextNode(contextNodeXri, true);
			contextNode.createContextNode(new XDI3SubSegment("$(+calendar)"));

			// done

			return true;
		}
	}

	@ContributorXri(addresses={"$(+calendar)($)", "$(+calendar)"})
	private class CalendarContributor extends AbstractContributor {

		private CalendarContributor() {

			super();

			this.getContributors().addContributor(new EventContributor());
		}

		@Override
		public boolean getContext(XDI3Segment[] contributorXris, XDI3Segment relativeContextNodeXri, XDI3Segment contextNodeXri, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			XDI3Segment googleCalendarContextXri = contributorXris[contributorXris.length - 3];
			XDI3Segment userXri = contributorXris[contributorXris.length - 2];
			XDI3Segment calendarXri = contributorXris[contributorXris.length - 1];

			log.debug("googleCalendarContextXri: " + googleCalendarContextXri + ", userXri: " + userXri + ", calendarXri: " + calendarXri);

			// filter

			if (relativeContextNodeXri != null) return this.getContextFilter(contributorXris, relativeContextNodeXri, contextNodeXri, operation, messageResult, executionContext);

			// parse identifiers

			String calendarIdentifier = GoogleCalendarContributor.this.googleCalendarMapping.calendarXriToCalendarIdentifier(calendarXri);

			log.debug("calendarIdentifier: " + calendarIdentifier);

			// retrieve the calendar list or calendar

			List<String> calendarList = null;
			Map<String, String> calendar = null;

			try {

				// String accessToken = GraphUtil.retrieveAccessToken(GoogleCalendarContributor.this.getTokenGraph(), userXri);
				// if (accessToken == null) throw new Exception("No access token.");

				// JSONObject user = GoogleCalendarContributor.this.retrieveUser(executionContext, accessToken);
				// if (user == null) throw new Exception("No user.");

				if (calendarIdentifier == null) {

					calendarList = new ArrayList<String> ();
					calendarList.add("1234");
					calendarList.add("1235");
					calendarList.add("1236");
				} else {

					calendar = new HashMap<String, String> ();
					calendar.put("summary", "Summary of Calendar " + calendarIdentifier);
					calendar.put("description", "Description of Calendar " + calendarIdentifier);
				}
			} catch (Exception ex) {

				throw new Xdi2MessagingException("Cannot load user data: " + ex.getMessage(), ex, null);
			}

			// add the calendar list or calendar to the response

			if (calendarList != null) {

				ContextNode contextNode = messageResult.getGraph().findContextNode(contextNodeXri, true);
				contextNode.createContextNode(new XDI3SubSegment("$(+event)"));
				for (String calendarEntry : calendarList) contextNode.createContextNode(Multiplicity.entityMemberArcXri(new XDI3SubSegment("!" + calendarEntry)));
			}

			if (calendar != null) {

				ContextNode contextNode = messageResult.getGraph().findContextNode(contextNodeXri, true);
				for (Map.Entry<String, String> entry : calendar.entrySet()) {

					contextNode.createContextNode(Multiplicity.attributeSingletonArcXri(new XDI3SubSegment("+" + entry.getKey()))).createLiteral(entry.getValue());
				}
			}

			// done

			return true;
		}
	}

	@ContributorXri(addresses={"$(+event)($)", "$(+event)"})
	private class EventContributor extends AbstractContributor {

		private EventContributor() {

			super();
		}

		@Override
		public boolean getContext(XDI3Segment[] contributorXris, XDI3Segment relativeContextNodeXri, XDI3Segment contextNodeXri, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			XDI3Segment googleCalendarContextXri = contributorXris[contributorXris.length - 4];
			XDI3Segment userXri = contributorXris[contributorXris.length - 3];
			XDI3Segment calendarXri = contributorXris[contributorXris.length - 2];
			XDI3Segment eventXri = contributorXris[contributorXris.length - 1];

			log.debug("googleCalendarContextXri: " + googleCalendarContextXri + ", userXri: " + userXri + ", calendarXri: " + calendarXri + ", eventXri: " + eventXri);

			// filter

			if (relativeContextNodeXri != null) return this.getContextFilter(contributorXris, relativeContextNodeXri, contextNodeXri, operation, messageResult, executionContext);

			// parse identifiers

			String calendarIdentifier = GoogleCalendarContributor.this.googleCalendarMapping.calendarXriToCalendarIdentifier(calendarXri);
			String eventIdentifier = GoogleCalendarContributor.this.googleCalendarMapping.eventXriToEventIdentifier(eventXri);

			log.debug("calendarIdentifier: " + calendarIdentifier + ", eventIdentifier: " + eventIdentifier);

			// retrieve the event list or event

			List<String> eventList = null;
			Map<String, String> event = null;

			try {

				// String accessToken = GraphUtil.retrieveAccessToken(GoogleCalendarContributor.this.getTokenGraph(), userXri);
				// if (accessToken == null) throw new Exception("No access token.");

				// JSONObject user = GoogleCalendarContributor.this.retrieveUser(executionContext, accessToken);
				// if (user == null) throw new Exception("No user.");

				if (eventIdentifier == null) {

					eventList = new ArrayList<String> ();
					eventList.add("5678");
					eventList.add("5679");
				} else {

					event = new HashMap<String, String> ();
					event.put("location", eventIdentifier.equals("5678") ? "Vienna" : "Sterling");
					event.put("start", "Start of event " + eventIdentifier);
					event.put("end", "Start of event " + eventIdentifier);
				}
			} catch (Exception ex) {

				throw new Xdi2MessagingException("Cannot load user data: " + ex.getMessage(), ex, null);
			}

			// add the event list or event to the response

			if (eventList != null) {

				ContextNode contextNode = messageResult.getGraph().findContextNode(contextNodeXri, true);
				for (String eventEntry : eventList) contextNode.createContextNode(Multiplicity.entityMemberArcXri(new XDI3SubSegment("!" + eventEntry)));
			}

			if (event != null) {

				ContextNode contextNode = messageResult.getGraph().findContextNode(contextNodeXri, true);
				for (Map.Entry<String, String> entry : event.entrySet()) {

					contextNode.createContextNode(Multiplicity.attributeSingletonArcXri(new XDI3SubSegment("+" + entry.getKey()))).createLiteral(entry.getValue());
				}
			}

			// done

			return true;
		}
	}

	/*
	 * Helper methods
	 */

	private JSONObject retrieveUser(ExecutionContext executionContext, String accessToken) throws IOException, JSONException {

		JSONObject user = GoogleCalendarContributorExecutionContext.getUser(executionContext, accessToken);

		if (user == null) {

			user = this.googleCalendarApi.getUser(accessToken);
			GoogleCalendarContributorExecutionContext.putUser(executionContext, accessToken, user);
		}

		return user;
	}

	/*
	 * Getters and setters
	 */

	public Graph getTokenGraph() {

		return this.tokenGraph;
	}

	public void setTokenGraph(Graph tokenGraph) {

		this.tokenGraph = tokenGraph;
	}

	public GoogleCalendarApi getGoogleCalendarApi() {

		return this.googleCalendarApi;
	}

	public void setGoogleCalendarApi(GoogleCalendarApi googleCalendarApi) {

		this.googleCalendarApi = googleCalendarApi;
	}

	public GoogleCalendarMapping getGoogleCalendarMapping() {

		return this.googleCalendarMapping;
	}

	public void setGoogleCalendarMapping(GoogleCalendarMapping googleCalendarMapping) {

		this.googleCalendarMapping = googleCalendarMapping;
	}
}
