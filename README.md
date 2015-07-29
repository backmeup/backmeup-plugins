# themis-fb
This is a facebook plugin for the Backmeup project, which archives a facebookprofiles data in XML and generates a view in HTML and CSS. It does not require any PHP, PEARL or RUBY.
## Versions
There are two versions available for this plugin, a version, which works as plugin and one which works standalone.
### Standalone
You can find the standalone version in the master branch. You have to run it in a terminal, because it needs some paramters to work.
#### Parameters
##### Generate properties
At the first time, run this:
```sh
$ java -jar {themis-fb}.jar
```
it will only generate the config. 
##### Download
When you want to download the data of your profile run:
```sh
$ java -jar {themis-fb}.jar --download
```
Do not forget to specify the download directory in your properties.xml
##### Generate view
When you want to generate a view of your already downloaded data run:
```sh
$ java -jar {themis-fb}.jar --generate-html
```
Do not forget to specify the HTML directory in your properties.xml. When you want to open the view, go to the HTML directory and open the index.html.

You can combine all given parameter.
#### Config
The configs name is properties.xml.
##### Access Token
You can generate an access token [here](https://developers.facebook.com/tools/explorer).
##### Limit
You can also set a limit for the amount of photos that will be downloaded per album. All numbers below 0 will download all photos. You should mind, that this can generate a lot of traffic.
##### Albumblacklist
You can specify the albums, which should not be downloaded, the regex is ;.
### Plugin
Themis-fb can also work as Backmeup plugin. For this, run the following command in your backmeup-plugins folder:
```sh
$ git clone https://github.com/RStoeckl/themis-fb.git -b bmu-plugin
```
Then add in the ```<modules>``` section of your backmeup-plugins pom.xml the following line:
```
<module>themis-fb<module>
```
Now build it:
```sh
$ ./build.sh
```
And deploy it
```sh
$ sudo ./deploy.sh
```
####Job parameters
see [here](#config)
