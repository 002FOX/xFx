# xFx
Design and Development of xFx protocol discussed in CSC 3374

<h4>xFx Protocol Design</h4>

<h1>Purpose:</h1>
	xFx is a client/server application that allows the client to download and upload files to the xFx server. The application allows you to see all the files shareable by the server, the ability to resume a download in the case of network connectivity issues, as well as not downloading already downloaded files (unless they have been changed in the server) to save network bandwidth and time.
<h1>Protocol:</h1>
	Methods:
-Show
The client sends this verb to the server in order to get all the files shareable by the server:
show[line feed]
The server will then responds with this header:
OK[one space](number of files) [line feed]
Alongside a list of all the file paths.
-Download
The client sends this verb in order to download a file of their choice, the header will be as such:
download[one space](file path)[one space](string) [line feed]
The string is “0” by default in the case of a normal download. In the case where the client is already in possession of the file they requested to download then a hashed string will be sent with the header in order to check if the file is an older version or not.
The server will then either respond with:
NOT[one space]FOUND[line feed]
In the case of the file not existing in the server, or with:
EXISTING[line feed]
In the case of a file already owned by the client that does not need to be redownloaded, or with:
OK[one space](file size)[line feed]
In the case of a normal download.
-Resume
When the client has failed to complete the download of a file, they can send this verb in order to resume the download where it stopped in the form of this header:
resume[one space](file path)[one space](size of file already downloaded) [line feed]
The server then could respond with 3 headers:
NOT[one space]FOUND[line feed]
In the case where the file does not exist, or:
COMPLETED[line feed]
In the case where the file has already finished downloading.
RESUMING[one space](remaining size to be downloaded) [line feed]
In the case of a successful download resuming.
-Upload
Upload is simpler, the client will send a header such as:
upload[one space](file name)[one space](file size)[line feed]
The server will then respond with 3 potential headers:
The first one is:
OK[line feed]
In the case of uploading a new file successfully to the server
The second one is:
OVERRIDDEN[line feed]
In the case of uploading a file with the same name as another in the server, which will get replaced.
The third one is:
ERROR[line feed]
In the case of any internal server errors happening, for example: no space.
<h1>Client:</h1>
	Interacting with the client is simple, you use the first lowercase letter of each command followed by the instructions told by the interface.
s: show, d: download, u: upload, r: resume
