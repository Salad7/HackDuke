/* Copyright 2017 Paul Brewer, Economic and Financial Technology Consulting LLC */
/* This file is open source software.  The MIT License applies to this software. */

/* jshint esnext:true,eqeqeq:true,undef:true,lastsemic:true,strict:true,unused:true,node:true */

const intoStream = require('into-stream');
const digestStream = require('digest-stream');
const promiseRetry = require('promise-retry');
const mime = require('mime-types');

const defaultRetryStrategy = {
    retries: 2,
    factor: 1.5,
    minTimeout:  1000,
    maxTimeout: 10000,
    randomize: true
};

function storeOrFail(storage, localStream, bucketName, fileName, wsOptions){
    "use strict";
    return new Promise(function(resolve, reject){
	const myObject = "gs://"+bucketName+"/"+fileName;
	const remote = (storage
			.bucket(bucketName)
			.file(fileName)
			.createWriteStream(wsOptions)
		       );

	// set up an md5 calculator for the streaming data

	let md5,length;	
	const md5buddy = digestStream('md5','base64', function(_md5, _length){
	    md5 = _md5 ;
	    length = _length;
	});

	// writing is finished when .finish is fired on remote
	// https://googlecloudplatform.github.io/google-cloud-node/#/docs/storage/0.8.0/storage/file?method=createWriteStream
	// in theory, .end has also been called on localstream and so the md5 and length are also ready
	
	remote.on('finish', function(){
	    promiseRetry(function(retry){
		return (storage
			.bucket(bucketName)
			.file(fileName)
			.get()
			.then(function(info){
			    if ( !info ||  !(info[1]) || !(info[1].md5Hash) ){
				console.log(JSON.stringify(info));
				throw new Error("can not confirm creation of "+myObject);
			    }
			    return info[1].md5Hash;
			})
		       ).catch(retry);
	    }, defaultRetryStrategy).then(function(uploadedMD5){
		if (uploadedMD5 !== md5)
		    reject(new Error("corrupted md5 hash for "+myObject+" expected: "+md5+" got: "+uploadedMD5));
		else
		    resolve({bucket: bucketName, file: fileName, md5: md5, length: length});		    
	    });		
	});
	localStream.on('error', function(e){
	    remote.end();
	    reject("pipeToStorage: error reading local input stream:"+e);
	});
	remote.on('error', function(e){
	    reject("pipeToStorage: error while writing "+myObject+" : "+e);
	});
	localStream.pipe(md5buddy).pipe(remote);
    });
}


module.exports = function pipeToStorage(storage, _retryStrategy){
    "use strict";
    const retryStrategy = _retryStrategy || defaultRetryStrategy;
    return function(source, bucketName, fileName, opt){
	function contentType(what){
	    return {
		metadata: {
		    contentType: what
		}
	    };
	}
	function isStreamLike(s){
	    return ((s) && (typeof(s)==='object') && (typeof(s.on)==='function') && (typeof(s.pipe)==='function'));
	}
	let meta;
	let wsOptions = {resumable:false};
	let streamer;
	if ((!source) || ((typeof(source)==='object') && (!isStreamLike(source)))){
	    return Promise.reject(new Error("pipeToStorage: source object passed to pipeToStorage is not a readable stream:"+JSON.stringify(source)));
	}
	if (typeof(source)==='string'){
	    streamer = ()=>(intoStream(source));
	} else if (typeof(source)==='function'){
	    streamer = source;
	}
	if (typeof(opt)==='undefined'){
	    const fileExtContentType = mime.lookup(fileName);
	    if (fileExtContentType)
		meta = contentType(fileExtContentType);
	} else if (opt === 'json'){
	    meta = contentType('application/json');
	} else if (opt && (typeof(opt)==='string')){
	    meta = contentType(opt);
	} else if (opt && (typeof(opt)==='object')){
	    meta = opt;
	}
	if (meta)
	    Object.assign(wsOptions, meta);
	if (typeof(streamer)==='function'){
	    return promiseRetry(function(retry){
		const localStream = streamer();
		if (!isStreamLike(localStream))
		    return Promise.reject(new Error("pipeToStorage: stream factory function did not return a readable stream: "+JSON.stringify(localStream)));
		return storeOrFail(storage, localStream, bucketName, fileName, wsOptions).catch(retry);
	    }, retryStrategy);
	}
	return storeOrFail(storage, source, bucketName, fileName, wsOptions);
    };
};
