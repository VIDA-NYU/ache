var ACHE_API_ADDRESS = ".";
if (process.env.NODE_ENV !== 'production') {
  ACHE_API_ADDRESS = "http://localhost:8080";
}

export {ACHE_API_ADDRESS};