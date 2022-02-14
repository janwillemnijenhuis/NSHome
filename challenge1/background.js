/**
 * @author Jan Willem Nijenhuis (s2935511)
 * @author Elisa Verhoeven (s1430416)
 */

function onrequest(req) {
  // This function will be called everytime the browser is about to send out an http or https request.
  // The req variable contains all information about the request.
  // If we return {}  the request will be performed, without any further changes
  // If we return {cancel:true} , the request will be cancelled.
  // If we return {requestHeaders:req.requestHeaders} , any modifications made to the requestHeaders (see below) are sent.
  // log what file we're going to fetch:

  console.log("Loading: " + req.method +" "+ req.url + " "+ req.type);
  console.log(req);
  var bigThree = ["facebook", "twitter", "google"];
  for (j = 0; j < bigThree.length; j++) {
    if (req.url.includes(bigThree[j])) {
      return {cancel:true};
    }
  }

  if (req.thirdParty == true) {
    var urlClass = req.urlClassification.thirdParty;
    var i = 0;
    for (i = 0; i < urlClass.length; i++) {
      if (urlClass[i].includes("tracking")) {
        console.log("Blocked Third Party Tracking");
        return {cancel:true};
      }
    }
  }

  // let's do something special if an image is loaded:
  if (req.type=="image") {
     console.log("image!");
  }

  // req also contains an array called requestHeaders containing the name and value of each header.
  // You can access the name and value of the i'th header as req.requestHeaders[i].name and req.requestHeaders[i].value ,
  // with i from 0 up to (but not including) req.requestHeaders.length .
  var headers = req.requestHeaders;
  var i;
  for (i = 0; i < headers.length; i++) {
    var item = headers[i];
    console.log("HEAD: name: [" + item.name + "] value: [" + item.value + "]");
    if (item.name == "User-Agent") {
      headers[i].value = "anonymous";
    }
  }

  return {requestHeaders:req.requestHeaders};
}


// no need to change the following, it just makes sure that the above function is called whenever the browser wants to fetch a file
browser.webRequest.onBeforeSendHeaders.addListener(
  onrequest,
  {urls: ["<all_urls>"]},
  ["blocking", "requestHeaders"]
);

