# xFx
Design and Development of xFx protocol discussed in CSC 3374

<h1>xFx Protocol Design</h1>

<h4>Purpose:</h4>
	xFx is a client/server application that allows the client to download and upload files to the xFx server. The application allows you to see all the files shareable by the server, the ability to resume a download in the case of network connectivity issues, as well as not downloading already downloaded files (unless they have been changed in the server) to save network bandwidth and time.
<h4>Protocol:</h4>
	Methods:<br>
-Show<br>
The client sends this verb to the server in order to get all the files shareable by the server:<br>
show[line feed]<br>
The server will then responds with this header:<br>
OK[one space](number of files) [line feed]<br>
Alongside a list of all the file paths.<br>
-Download<br>
The client sends this verb in order to download a file of their choice, the header will be as such:<br>
download[one space](file path)[one space](string) [line feed]<br>
The string is “0” by default in the case of a normal download. In the case where the client is already in possession of the file they requested to download then a hashed string will be sent with the header in order to check if the file is an older version or not.<br>
The server will then either respond with:<br>
NOT[one space]FOUND[line feed]<br>
In the case of the file not existing in the server, or with:<br>
EXISTING[line feed]<br>
In the case of a file already owned by the client that does not need to be redownloaded, or with:<br>
OK[one space](file size)[line feed]<br>
In the case of a normal download.<br>
-Resume<br>
When the client has failed to complete the download of a file, they can send this verb in order to resume the download where it stopped in the form of this header:<br>
resume[one space](file path)[one space](size of file already downloaded) [line feed]<br>
The server then could respond with 3 headers:<br>
NOT[one space]FOUND[line feed]<br>
In the case where the file does not exist, or:<br>
COMPLETED[line feed]<br>
In the case where the file has already finished downloading.<br>
RESUMING[one space](remaining size to be downloaded) [line feed]<br>
In the case of a successful download resuming.<br>
-Upload<br>
Upload is simpler, the client will send a header such as:<br>
upload[one space](file name)[one space](file size)[line feed]<br>
The server will then respond with 3 potential headers:<br>
The first one is:<br>
OK[line feed]<br>
In the case of uploading a new file successfully to the server<br>
The second one is:<br>
OVERRIDDEN[line feed]<br>
In the case of uploading a file with the same name as another in the server, which will get replaced.<br>
The third one is:<br>
ERROR[line feed]<br>
In the case of any internal server errors happening, for example: no space.<br><br>
<h4>Client:</h4>
	Interacting with the client is simple, you use the first lowercase letter of each command followed by the instructions told by the interface.<br>
s: show, d: download, u: upload, r: resume
