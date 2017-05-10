import React, { Component } from 'react';
import {BrowserRouter as Router, Route, Link} from 'react-router-dom'
import 'whatwg-fetch'

import Search from './Search'
import MetricsMonitor from './MetricsMonitor'

import ache_logo_png from '../../ache-logo.png';

class Header extends React.Component {
  render() {
    return (
      <nav className="navbar navbar-default navbar-fixed-top">
        <div className="container">
          <div className="navbar-header">
            <button type="button" className="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
              <span className="sr-only">Toggle navigation</span>
              <span className="icon-bar"></span>
              <span className="icon-bar"></span>
              <span className="icon-bar"></span>
            </button>
            <div className="navbar-brand ache-logo">
              <img src={ache_logo_png} className="App-logo" alt="logo" />
            </div>
          </div>
          <div id="navbar" className="collapse navbar-collapse">
            <ul className="nav navbar-nav">
              <Navigation to="/" label="Monitoring" activeOnlyWhenExact={true}/>
              <Navigation to="/search" label="Search"/>
            </ul>
          </div>
        </div>
      </nav>
    );
  }
}

const Navigation = ({ label, to, activeOnlyWhenExact }) => (
  <Route path={to} exact={activeOnlyWhenExact} children={({ match }) => (
    <li className={match ? 'active' : ''}>
      <Link to={to}>{label}</Link>
    </li>
  )}/>
)

class App extends Component {
  render() {
    return (
      <Router>
        <div>
          <Header/>
          <div className="container">
            <div className="main-content">
              <Route exact path="/" component={MetricsMonitor}/>
              <Route exact path="/search" component={Search}/>
            </div>
          </div>
        </div>
      </Router>
    );
  }
}

export default App;
