package br.net.bmobile.websocketrails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSocketRailsChannel {

	private Map<String, List<WebSocketRailsDataCallback>> callbacks;
	private String channelName;
	private String token;
	private WebSocketRailsDispatcher dispatcher;
	
	public WebSocketRailsChannel(String channelName, WebSocketRailsDispatcher dispatcher, boolean isPrivate)
	{
        String eventName = null;
        if (isPrivate)
            eventName = "websocket_rails.subscribe_private";
        else
            eventName = "websocket_rails.subscribe";
        
        this.channelName = channelName;
        this.dispatcher = dispatcher;

        List<Object> frame = new ArrayList<Object>();
        frame.add(eventName);

        Map<String, Object> data = new HashMap<String, Object>();
        
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("channel", channelName);
        
        data.put("data", info);
        
        frame.add(data);
        frame.add(dispatcher.getConnectionId());
        
        WebSocketRailsEvent event = new WebSocketRailsEvent(frame, null, null);
        
        callbacks = new HashMap<String, List<WebSocketRailsDataCallback>>();
    
        dispatcher.triggerEvent(event);
	}
	
	public void bind(String eventName, WebSocketRailsDataCallback callback) {
		
	    if (callbacks.get(eventName) == null)
	        callbacks.put(eventName, new ArrayList<WebSocketRailsDataCallback>());
	    
	    callbacks.get(eventName).add(callback);
	}

	public void trigger(String eventName, Object message) {
		
	    List<Object> frame = new ArrayList<Object>();
        frame.add(eventName);

        Map<String, Object> info = new HashMap<String, Object>();
        info.put("channel", channelName);
        info.put("data", message);
        info.put("token", token);
        
        frame.add(info);
        frame.add(dispatcher.getConnectionId());
        
        WebSocketRailsEvent event = new WebSocketRailsEvent(frame, null, null);
		
	    dispatcher.triggerEvent(event);		
	}
	
	public void dispatch(String eventName, Object message) {
		
	    if("websocket_rails.channel_token".equals(eventName)) {
	        
	        Map<String, Object> info = (Map<String, Object>) message;
	        this.token = (String) info.get("token");
	    }
	    else {
	        if (callbacks.get(eventName) == null)
	            return;
	        
	        for (WebSocketRailsDataCallback callback : callbacks.get(eventName))
	        {
	            callback.onDataAvailable(message);
	        }
	    }		
	}
	
	public void destroy() {
		
	    String eventName = "websocket_rails.unsubscribe";
        
	    List<Object> frame = new ArrayList<Object>();
        frame.add(eventName);

        Map<String, Map<String, Object>> data = new HashMap<String, Map<String, Object>>();
        
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("channel", channelName);
        
        data.put("data", info);
        
        frame.add(data);
        
        frame.add(dispatcher.getConnectionId());
        
        WebSocketRailsEvent event = new WebSocketRailsEvent(frame, null, null);
	    
	    dispatcher.triggerEvent(event);
	    callbacks.clear();		
	}
}
