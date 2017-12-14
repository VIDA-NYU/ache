import React from 'react';
import {BrowserRouter as Router, Route, Link} from 'react-router-dom'
import 'whatwg-fetch'

import Crawlers from './Crawlers'
import StartCrawler from './StartCrawler'
import Search from './Search'
import MetricsMonitor from './MetricsMonitor'
import {Messages} from './AlertMessage'
import {BASE_PATH} from './Config'
import ache_logo_png from './img/ache-logo.png';

class Header extends React.Component {
  render() {
    return (
      <nav className="navbar navbar-default navbar-fixed-top">
        <div className="container">
          <div className="navbar-header">
            <button type="button" className="navbar-toggle collapsed"
              data-toggle="collapse" data-target="#navbar" aria-expanded="false"
              aria-controls="navbar">
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
              <Navigation to="/" label="Crawlers" activeOnlyWhenExact={true} />
              <Navigation to="/start" label="Start Crawl" activeOnlyWhenExact={true} />
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

class App extends React.Component {

  messages = new Messages();

  render() {
    return (
      <Router basename={BASE_PATH}>
        <div>
          <Header/>
          <div className="container">
            <div className="main-content">
              <Route exact path="/" component={(props) => <Crawlers {...props} messages={this.messages} />} />
              <Route exact path="/start" component={(props) => <StartCrawler {...props} messages={this.messages} />} />
              <Route exact path="/monitoring/:crawler_id" component={MetricsMonitor} />
              <Route exact path="/search/:crawler_id" component={Search}/>
            </div>
          </div>
        </div>
      </Router>
    );
  }
}

export default App;
