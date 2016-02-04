<p align="center" >
  <img src="https://avatars2.githubusercontent.com/u/11334935?v=3&s=200" alt="Donky Networks LTD" title="Donky Network SDK">
</p>

# Donky GeoFence SDK (Beta 1)

The Donky iOS Geofencing Module allows you to add monitoring of up to 10,000 radial geo-fences to your app. It must be used in conjunction with the Donky Modular SDK to allow it to track location, and send messages to users. For detailed documentation, tutorials and guides, visit our [online documentation](http://docs.mobiledonky.com/docs/start-here).

While we will not stop you from using it in your production apps, the SDK's public beta status should be taken into account. 

Please feedback to [sdk@mobiledonky.com](mailto:sdk@mobiledonky.com) if you experience issues with integrating or using the module.

## Requirements

<ul>
<li>Android 4.0+</li>
</ul>

## Author

<ul>
<li>Donky Networks Ltd, sdk@mobiledonky.com</li>
</ul>

## License

<ul>
<li>Donky-Core-SDK is available under the MIT license. See the LICENSE file for more info.</li>
</ul>

##Support

<ul>
<li>Please contact sdk@mobiledonky.com if you have any issues with integrating or using this SDK.</li>
</ul>

Installation

To install please use one of the following methods:

Cloning the Git Repo:

git clone git@github.com:Donky-Network/Donky-GeoFence-Android.git 

## Source

You can find the source code of Donky Core in [*Donky/geofence*](https://github.com/Donky-Network/Donky-GeoFence-Android/tree/master/src/Donky/geofence) Android Studio project.
You can use 'File->New->Import Module' option to easily include the source code to your project.

In order to use this module, you must get dependencies from the Android Modular Repo on Github: [Core](https://github.com/Donky-Network/DonkySDK-Android-Modular/tree/master/src/Donky/core) and [Location](https://github.com/Donky-Network/DonkySDK-Android-Modular/tree/master/src/Donky/location)

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/start-here) and for details how to use this module [here](http://docs.mobiledonky.com/docs/start-here).

Initialise this module before initailising Donky Core in onCreate method of your application class.

```java
public class MyApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
		
        DonkyGeoFence.initialiseDonkyGeoFences(this, 
			new DonkyListener() /*set callback handlers*/);
      
		DonkyCore.initialiseDonkySDK(this,">>ENTER API KEY HERE<<", 
        	new DonkyListener() /*set callback handlers*/);
    		
	}
}
```