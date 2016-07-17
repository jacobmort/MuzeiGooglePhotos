# Google Photos for Muzei

I love the Android wallpaper switcher [Muzei](https://github.com/romannurik/muzei) but the 
photo selection for Google Photos required you to select photos individually.

Luckily Muzei makes it easy to [build your own wallpaper sources](https://github.com/romannurik/muzei/wiki/API)
 so I created this which fetches meta (ID and created time) for all your Google Photos and serves
 up a random one which hasn't been seen yet each time Muzei asks for a new wallpaper.
 Once you've exhausted all of your photos it starts over. 

Leaned heavily on code from [500px example](https://github.com/romannurik/muzei/tree/master/example-source-500px)
and [Google sample code](https://developers.google.com/admin-sdk/reseller/v1/quickstart/android)

#### TODO
Right now it just bulk fetches the photos on initial run, need to schedule update mechanism to run
every so often.