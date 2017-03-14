import React, { Component } from 'react';
import {
  BrowserRouter as Router,
  Route,
  Link
} from 'react-router-dom'
import {XYPlot, XAxis, YAxis, HorizontalGridLines, LineSeries, DiscreteColorLegend} from 'react-vis';
import logo from '../../ache-logo.png';
import './App.css';
import 'whatwg-fetch'


class SeriesPlot extends React.Component {
  plotsPerLine = 2;
  plotWidth = 1050/this.plotsPerLine;
  plotHeight = 250;
  render() {
    var lines = [];
    for(var i = 0; i < this.props.series.length; i++) {
      lines.push(<LineSeries key={i} data={this.props.series[i]}/>);
    }
    var divClass = 'col-md-'+(12/this.plotsPerLine);
    return (
      <div className={divClass}>
        <XYPlot width={this.plotWidth} height={this.plotHeight}>
          <HorizontalGridLines />
          {lines}
          <XAxis />
          <YAxis />
        </XYPlot>
        <DiscreteColorLegend width={this.plotWidth} height={35*4} items={this.props.titles} orientation="vertical"/>
      </div>
    );
  }
}

class MetricsMonitor extends React.Component {

  maxDataPoints = 100;
  updateInterval = 1000;
  xaxisCount = 0;

  constructor(props) {
    super(props);
    this.charts = [
      // Downloader Fetches
      [
        {
          type: "counters",
          key: "downloader.fetches.successes"
        },
        {
          type: "counters",
          key: "downloader.fetches.aborted",
        },
        {
          type: "counters",
          key: "downloader.http_response.status.2xx",
        },
        {
          type: "counters",
          key: "downloader.fetches.errors",
        }
      ],
      // Downloader Queues
      [
        {
          type: "gauges",
          key: "downloader.dispatch_queue.size"
        },
        {
          type: "gauges",
          key: "downloader.download_queue.size"
        }
      ],
      // Downloader Pending/Running Tasks
      [
        {
          type: "gauges",
          key: "downloader.pending_downloads"
        },
        {
          type: "gauges",
          key: "downloader.running_handlers"
        },
        {
          type: "gauges",
          key: "downloader.running_requests",
        }
      ],
      // Scheduler
      [
        {
          type: "gauges",
          key: "frontier_manager.scheduler.non_expired_domains"
        },
        {
          type: "gauges",
          key: "frontier_manager.scheduler.number_of_links"
        },
        {
          type: "gauges",
          key: "frontier_manager.scheduler.empty_domains"
        }
      ],

    ];
    for(var i = 0; i < this.charts.length; i++) {
      for(var m = 0; m < this.charts[i].length; m++) {
        this.charts[i][m]['series'] = [];
      }
    }
  }

  componentDidMount() {
    this.timerID = setInterval(
      () => this.fetchMetrics(),
      this.updateInterval
    );
  }

  componentWillUnmount() {
    clearInterval(this.timerID);
  }

  fetchMetrics() {
    var url = "http://localhost:8080/metrics";
    //var url = "/metrics";
    fetch(url)
      .then(function(response) {
        return response.json();
      }, function(error) {
        this.serverError = true;
        this.serverMessage = "Failed to get metrics from crawler server.";
      })
      .then(this.updateSeries.bind(this));
  }

  updateSeries(metrics) {
    for(var i = 0; i < this.charts.length; i++) {
      for(var m = 0; m < this.charts[i].length; m++) {
        let metric = this.charts[i][m];
        if(metric.series.length === this.maxDataPoints) {
          metric.series.shift();
        }
        var metricValue;
        if(metric.type === 'gauges') {
          metricValue = metrics[metric.type][metric.key].value;
        }
        else if(metric.type === 'counters') {
          metricValue = metrics[metric.type][metric.key].count;
        }
        else {
          console.log("Invalid metric type: " + metric.type);
        }
        //metricValue = metricValue * Math.random();
        metric.series.push({x: this.xaxisCount, y: metricValue})
      }
    }
    this.xaxisCount += 1;
    this.setState({charts: this.charts});
  }

  render(){
    var plots = [];
    for(var i = 0; i < this.charts.length; i++) {
      var titles = [];
      var series = [];
      for(var m = 0; m < this.charts[i].length; m++) {
        titles.push(this.charts[i][m].key);
        series.push(this.charts[i][m].series);
      }
      var keyName = i + '-' + m;
      plots.push(
        (<SeriesPlot key={keyName} titles={titles} series={series} />)
      );
    }
    return (
      <div className="row">
        {plots}
      </div>
    );
  }

}

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
            <a className="navbar-brand ache-logo" href="#">
              <img src={logo} className="App-logo" alt="logo" />
            </a>
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

const Search = () => (
  <div>
    <h2>Search</h2>
  </div>
)

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
