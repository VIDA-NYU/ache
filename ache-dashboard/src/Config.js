let meta = document.getElementsByName("base_path")[0];
let basePath;
if(meta) {
  basePath = meta.content;
  basePath = basePath.startsWith('/') ? basePath : ('/' + basePath);
  basePath = basePath.endsWith('/') ? basePath : (basePath + '/');
} else {
  basePath = '/';
}

const BASE_PATH = basePath;
const BASE_PATH_ADDRESS = window.location.protocol + '//' + window.location.host + BASE_PATH;
const ACHE_API_ADDRESS  = window.location.protocol + '//' + window.location.host + BASE_PATH;

export {BASE_PATH, BASE_PATH_ADDRESS, ACHE_API_ADDRESS};
