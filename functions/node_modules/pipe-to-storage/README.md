# pipe-to-storage

[![Greenkeeper badge](https://badges.greenkeeper.io/DrPaulBrewer/pipe-to-storage.svg)](https://greenkeeper.io/)

[![Build Status](https://travis-ci.org/DrPaulBrewer/pipe-to-storage.svg?branch=master)](https://travis-ci.org/DrPaulBrewer/pipe-to-storage)

## Importing and Setup

On Google platforms:

    const storage = require('@google-cloud/storage')();

On other platforms: set up your API key, see [relevant docs](https://www.npmjs.com/package/@google-cloud/storage)

**Pass the storage object** when setting up pipeToStorage for usage

    const pipeToStorage = require('pipe-to-storage')(storage);

## Usage

    pipeToStorage(source, bucketName, fileName , optional )
    .then(function(what){ 
       console.log("hooray! I wrote "+what.file+" to bucket "+what.bucket+" and it should have md5 "+what.md5);
     })
     .catch(function(e){
       console.log("oh no! an error occurred. here it is:");
       console.log(e);
     });

`source` must be either:

* a string, which will be sent as-is to the new file [retry will use the same string]
* a function that returns a readable stream [retry will call the function to get a fresh readable stream]
* a readable stream [can not retry]

`npm:promise-retry` will be used to retry failed storage promises in the first two cases.  A retry strategy
is used that is appropriate for a cloud back-end environment: up to 3 retries per call within 30 seconds, then fail.

`bucketname` and `filename` are the Google Cloud Storage[tm] bucket and file names.  `gs://` is **not** needed in `bucketName`

`optional` is optional and may be:

* absent or `undefined` -- `pipeToStorage` should try to look up the `content-type` from the fileName extension
* the string `'json'` -- shorthand to set metadata `content-type: application/json`
* other strings --  sets the metadata `content-type` manually
* an object, to set any `writeStream.options` in the internal storage `createWriteStream` call

returns a Promise that resolves to {bucket, file, md5, length} after saving the contents of `source` to 
`storage.bucket(bucketName).file(fileName)` or rejects with any errors not caught in retrys

md5 is in base64

## Tests

This module is tested on Travis CI, but you won't be able to run the same tests yourself without some adjustments.

To run the tests in your own environment, change the storage API credentials (projectId, keyFilename) and the bucket name referenced in `./test/index.js`.  

## Copyright

Copyright 2017 Paul Brewer, Economic and Financial Technology Consulting LLC <drpaulbrewer@eaftc.com>

## License

The MIT License

### No relationship to Google, Inc. 

This is third party software, not a product of Google Inc.

The author(s) have no relationship to Google, Inc. 
