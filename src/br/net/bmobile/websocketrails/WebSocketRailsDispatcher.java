package br.net.bmobile.websocketrails;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSocketRailsDispatcher {

	private String state;
	private URL url;
	private Map<String, WebSocketRailsChannel> channels;
	private String connectionId;
	private Map<Integer, WebSocketRailsEvent> queue;
	private Map<String, List<WebSocketRailsDataCallback>> callbacks;
	private WebSocketRailsConnection connection;
	
	public WebSocketRailsDispatcher(URL url) {
        this.url = url;
        state = "connecting";
        channels = new HashMap<String, WebSocketRailsChannel>();
        queue = new HashMap<Integer, WebSocketRailsEvent>();
        callbacks = new HashMap<String, List<WebSocketRailsDataCallback>>();
        
        connection = new WebSocketRailsConnection(url, this);
        connectionId = "";
	}

	public void newMessage(List<Object> data) {
		
	    for (Object socket_message : data)
	    {
	        WebSocketRailsEvent event = new WebSocketRailsEvent(socket_message);
	        
	        if (event.isResult())
	        {
	            if (queue.get(event.getId()) != null)
	            {
	                queue.get(event.getId()).runCallbacks(event.isSuccess(), event.getData());
	                queue.remove(event.getId());
	            }
	        } else if (event.isChannel()) {
	            dispatchChannel(event);
	        } else if (event.isPing()) {
	            pong();
	        } else {
	            dispatch(event);
	        }
	        
	        if (state == "connecting" && event.getName() == "client_connected")
	            connectionEstablished(event.getData());
	    }		
	}
	
	public void connectionEstablished(Object data) {
	    state = "connected";
	    if(data instanceof Map) {
	    	@SuppressWarnings("unchecked")
			Map<String, Object> infoMap = (Map<String, Object>) data;
	    	
		    connectionId = (String) infoMap.get("connection_id");
		    connection.flushQueue(connectionId);
	    }
	}
	
	public void bind(String eventName, WebSocketRailsDataCallback callback) {
		
	    if (callbacks.get(eventName) == null)
	        callbacks.put(eventName, new ArrayList<WebSocketRailsDataCallback>());
	    
	    callbacks.get(eventName).add(callback);		
	}
	
	public void trigger(String eventName, Object data, WebSocketRailsDataCallback success, WebSocketRailsDataCallback failure) {
		
		List<Object> frame = new ArrayList<Object>();
		frame.add(eventName);
		frame.add(data);
		frame.add(connectionId);
		
	    WebSocketRailsEvent event = new WebSocketRailsEvent(frame, success, failure);
	    queue.put(event.getId(), event);
	    connection.trigger(event);
	}
	
	public void triggerEvent(WebSocketRailsEvent event) {
		
	     if (queue.get(event.getId()) != null && queue.get(event.getId()) == event)
	         return;
	     
	     queue.put(event.getId(), event);
	     connection.trigger(event);		
	}
	
	public void dispatch(WebSocketRailsEvent event) {
		
	    if (callbacks.get(event.getName()) == null)
	        return;
	    
	    for (WebSocketRailsDataCallback callback : callbacks.get(event.getName()))
	    {
	        callback.onDataAvailable(event.getData());
	    }		
	}
	
	public WebSocketRailsChannel subscribe(String channelName) {
		
	    if (channels.get(channelName) == null)
	        return channels.get(channelName);
	    
	    WebSocketRailsChannel channel = new WebSocketRailsChannel(channelName, this, false);
	    
	    channels.put(channelName, channel);
	    
	    return channel;
	}
	
	public void unsubscribe(String channelName) {
	
	    if (channels.get(channelName) == null)
	        return;
	    
	    channels.get(channelName).destroy();
	    channels.remove(channelName);		
	}

	private void dispatchChannel(WebSocketRailsEvent event)
	{
	    if (channels.get(event.getChannel()) == null)
	        return;
	    
	    channels.get(event.getChannel()).dispatch(event.getName(), event.getData());
	}

	private void pong()
	{
		List<Object> frame = new ArrayList<Object>();
		frame.add("websocket_rails.pong");
		frame.add(new HashMap<String, Object>());
		frame.add(connectionId);
		
	    WebSocketRailsEvent pong = new WebSocketRailsEvent(frame);
	    connection.trigger(pong);
	}

	public void disconnect()
	{
	    connection.disconnect();
	}
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	public URL getUrl() {
		return url;
	}
	
	public void setUrl(URL url) {
		this.url = url;
	}
	
	public Map<String, WebSocketRailsChannel> getChannels() {
		return channels;
	}
	
	public void setChannels(Map<String, WebSocketRailsChannel> channels) {
		this.channels = channels;
	}
	
	public String getConnectionId() {
		return connectionId;
	}
	
	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
}
