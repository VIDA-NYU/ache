// BasePath.js needs to be the first import in order to configure
//  webpack's publicPath correctly
import "./BasePath";

import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import "../node_modules/bootstrap/dist/css/bootstrap.css";
import "../node_modules/react-vis/dist/style.css";
import "../node_modules/instantsearch.css/themes/satellite.css";

import './index.css';

ReactDOM.render(
  <App />,
  document.getElementById('root')
);
