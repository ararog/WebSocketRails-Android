# WebSocketsRails client port for Android

Port of JavaScript client provided by https://github.com/websocket-rails/websocket-rails

Built on top of AndroidAsync

## Misc

Refer to https://github.com/websocket-rails/websocket-rails to learn more about WebSocketRails

Refer to https://github.com/koush/AndroidAsync to learn more about AndroidAsync

## Download

Download [the latest JAR](https://search.maven.org/remote_content?g=br.net.bmobile&a=websocketrails-android&v=LATEST
) or grab via Maven:

```xml
<dependency>
    <groupId>br.net.bmobile</groupId>
    <artifactId>websocketrails-android</artifactId>
    <version>(insert latest version)</version>
</dependency>
```

Gradle: 
```groovy
dependencies {
    compile 'br.net.bmobile:websocketrails-android:1.+'
}
```

## Example

Since data exchange is JSON based, it's strongly recommended to use Jackson
API to deserialize data.


## Connecting to websocket


```java

private WebSocketRailsDispatcher dispatcher;

...
	try {
		dispatcher = new WebSocketRailsDispatcher(new URL("http://192.168.100.109:3000/websocket"));
		dispatcher.connect();
	} 
	catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
...
```

## Creating and Subscribing a WebSocketRailsChannel

````java

private static WebSocketRailsChannel webSocketRailsChannel;

...

	webSocketRailsChannel = dispatcher.subscribe("chanelName");

...
````

## Trigering a event from WebSocketRailsChannel

````java

private static WebSocketRailsChannel webSocketRailsChannel;

...

	Message message = new Message(); // Create a class with getter and setter as in Jackson API
	
	message.setName("Charles");
	message.setMessage("Hai");
	
	webSocketRailsChannel.trigger("new_message", message);

...
````

## Binding a event to WebSocketRailsChannel

````java

...

	webSocketRailsChannel.bind("new_message", new WebSocketRailsDataCallback() {

		@Override
		public void onDataAvailable(Object data) {
		// Do what you want with the data received.
		
		}
	}

...
````

## Unsubscribing a WebSocketRailsChannel

````java

...

	dispatcher.unSubscribe("chanelName");

...
````

## disconnecting websocket


```java


...
	dispatcher.disconnect();
...
```

## Projects using WebSockerRails-Android

[Ow for Android](https://github.com/ararog/Ow-Android)
