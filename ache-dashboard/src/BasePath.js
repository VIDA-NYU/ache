/*
This file sets the variable __webpack_public_path__ so that base path of the app
(where the static files of the application will be served from) is configured
during runtime. 'create-react-app' allows to set the variable "homepage" in
the file package.json, but it doesn't work for our use case since we need to
load the base_path in runtime, since it is ultimately configured ache.yml file/

For documentation on __webpack_public_path__ variable see:
https://webpack.js.org/guides/public-path/
For other use cases, see also:
https://github.com/facebookincubator/create-react-app/issues/647
https://github.com/webpack-contrib/file-loader/issues/46#issuecomment-264215576
*/
import {BASE_PATH_ADDRESS} from './Config';
// eslint-disable-next-line
__webpack_public_path__ = BASE_PATH_ADDRESS;
