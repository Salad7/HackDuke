const functions = require('firebase-functions');
const express = require('express');
const app = express();
const timeout = require('connect-timeout')
var https = require('https');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
// Imports the Google Cloud client library
const speech = require('@google-cloud/speech');

const fs = require('fs');
app.use(express.static(__dirname))
app.use(function(req, res, next) {
    res.header('X-XSS-Protection', 0);
      res.header("Access-Control-Allow-Origin", '*');
       res.header("Access-Control-Allow-Credentials", true);
       res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS');
       res.header("Access-Control-Allow-Headers", 'Origin,X-Requested-With,Content-Type,Accept,content-type,application/json');
    next();
});

// var admin = require("firebase-admin");
// var serviceAccount = require("path/to/serviceAccountKey.json");
// admin.initializeApp({
//     credential: admin.credential.cert(serviceAccount),
//     storageBucket: "<BUCKET_NAME>.appspot.com"
// });

// var bucket = admin.storage().bucket();
// Your Google Cloud Platform project ID
const projectId = 'react-932c4';

// Creates a client
const client = new speech.SpeechClient({
  projectId: projectId,
});

app.get('/sample', function(req,res){

  res.send("Sending stuff!!!!!@")
})



function dlFile(url, dest){
var file = fs.createWriteStream(dest);
return new Promise((resolve, reject) => {
//  var responseSent = false; // flag to make sure that response is sent only once.
  https.get(url, response => {
    response.pipe(file);
    file.on('finish', () =>{
      file.close(() => {
        if(responseSent)  return;
        responseSent = true;
        resolve();
      });
    });
  }).on('error', err => {
     if(responseSent)  return;
     responseSent = true;
      reject(err);
  });
});

}
app.get('/test/:timestamp/:user', function (req, res) {
    const encoding = 'LINEAR16';
    const sampleRateHertz = 16000;
    const languageCode = 'en-US'
    const enableWordTimeOffsets = true;
    const config = {
  encoding: encoding,
  sampleRateHertz: sampleRateHertz,
  languageCode: languageCode,
  enableWordTimeOffsets: enableWordTimeOffsets
};
    const audio = {
    uri: "gs://react-932c4.appspot.com/Sound/recorded_audio"+(req.params.timestamp)+(req.params.user)+".wav",
  };

  const request = {
    config: config,
    audio: audio,
  };

  //Detects speech in the audio file
  client
    .recognize(request)
    .then(data => {
      const response = data[0];
      const transcription = response.results
        .map(result => result.alternatives[0].transcript)
        .join('\n');
      res.send(`Transcription: `+ transcription);
      console.log(`Transcription: `+ transcription)
    })
    .catch(err => {
      console.error('ERROR:', err);
      res.send("Error!: "+err)
    });
 });

 app.get('/longrun/:timestamp/:user', function (req, res) {
  setTimeout(function(){
     const encoding = 'LINEAR16';
     const sampleRateHertz = 16000;
     const languageCode = 'en-US'
     const enableWordTimeOffsets = true;
     const config = {
   encoding: encoding,
   sampleRateHertz: sampleRateHertz,
   languageCode: languageCode,
   enableWordTimeOffsets: enableWordTimeOffsets
 };
     const audio = {
     uri: "gs://react-932c4.appspot.com/Sound/recorded_audio"+(req.params.timestamp)+(req.params.user)+".wav",
   };

   const request = {
     config: config,
     audio: audio,
   };

   client
  .longRunningRecognize(request)
  .then(data => {
    const operation = data[0];
    // Get a Promise representation of the final result of the job
    return operation.promise();
  })
  .then(data => {
    const response = data[0];
    const transcription = response.results
      .map(result => result.alternatives[0].transcript)
      .join('\n');
    console.log(`Transcription: ${transcription}`);
    res.send(`Transcription: ${transcription}`)
  })
  .catch(err => {
    console.error('ERROR:', err);
    //res.send("FAILLLL")
  });
},0);
  });



  app.get('/test/:timestamp/:user/:speechContexts', function (req, res) {
      //var speechContexts = req.params.speechContexts
      const encoding = 'LINEAR16';
      const sampleRateHertz = 16000;
      const languageCode = 'en-US'
      const enableWordTimeOffsets = true;
      const config = {
    encoding: encoding,
    sampleRateHertz: sampleRateHertz,
    languageCode: languageCode,
    enableWordTimeOffsets: enableWordTimeOffsets,
    speechContext: {'phrases':convertSpeechContexts(req.params.speechContexts)}
  };
      const audio = {
      uri: "gs://react-932c4.appspot.com/Sound/recorded_audio"+(req.params.timestamp)+(req.params.user)+".wav",
    };

    const request = {
      config: config,
      audio: audio,
    };

    //Detects speech in the audio file
    client
      .recognize(request)
      .then(data => {
        const response = data[0];
        const transcription = response.results
          .map(result => result.alternatives[0].transcript)
          .join('\n');
        res.send(`Transcription: `+ transcription+"Speech contexts is: ");
        console.log(`Transcription: `+ transcription)
      })
      .catch(err => {
        console.error('ERROR:', err);
        res.send("Error!: "+err)
      });
   });

   app.get('/longrun/:timestamp/:user/:speechContexts', function (req, res) {
     //var speechContexts = req.params.speechContexts
    setTimeout(function(){
       const encoding = 'LINEAR16';
       const sampleRateHertz = 16000;
       const languageCode = 'en-US'
       const enableWordTimeOffsets = true;
       const config = {
     encoding: encoding,
     sampleRateHertz: sampleRateHertz,
     languageCode: languageCode,
     enableWordTimeOffsets: enableWordTimeOffsets,
     speechContext: {'phrases':convertSpeechContexts(req.params.speechContexts)}
   };
       const audio = {
       uri: "gs://react-932c4.appspot.com/Sound/recorded_audio"+(req.params.timestamp)+(req.params.user)+".wav",
     };

     const request = {
       config: config,
       audio: audio,
     };

     client
    .longRunningRecognize(request)
    .then(data => {
      const operation = data[0];
      // Get a Promise representation of the final result of the job
      return operation.promise();
    })
    .then(data => {
      const response = data[0];
      const transcription = response.results
        .map(result => result.alternatives[0].transcript)
        .join('\n');
      console.log(`Transcription: ${transcription}`);
      res.send(`Transcription: ${transcription}`)
    })
    .catch(err => {
      console.error('ERROR:', err);
      //res.send("FAILLLL")
    });
  },0);
    });

    function convertSpeechContexts(sc){
      return sc.split("-")
    }
exports.app = functions.https.onRequest(app)
