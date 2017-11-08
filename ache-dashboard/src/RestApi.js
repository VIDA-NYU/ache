import {ACHE_API_ADDRESS} from './Config';

class RestApi {

  constructor(address, authorization) {
    this.address = address;
    this.authorization = authorization;
  }

  get(endpoint, props) {
    return this.fetch('GET', endpoint, props);
  }

  put(endpoint, props) {
    return this.fetch('PUT', endpoint, props);
  }
  
  post(endpoint, props) {
    return this.fetch('POST', endpoint, props);
  }
  
  fetch(method, endpoint, props) {
    return fetch(this.address + endpoint, this.createProperties(method, props))
              .then(function(response) {
                return response.json();
              }, function(error) {
                return 'FETCH_ERROR';
              });
  }

  createProperties(method, properties) {
    let props = properties || {};
    props.method = method || props.method || 'GET';
    if(this.authorization !== undefined) {
      props.headers = props.headers || {};
      props.headers['Authorization'] = this.authorization;
    }
    return props;
  }

}

let meta = document.getElementsByName("authorization")[0];
let authHeader;
if(meta !== undefined) {
  authHeader = meta.content;
}

const api = new RestApi(ACHE_API_ADDRESS, authHeader);

export {api};
