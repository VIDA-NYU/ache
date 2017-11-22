import React from 'react';
import {XYPlot, XAxis, YAxis, HorizontalGridLines, LineSeries, DiscreteColorLegend} from 'react-vis';
import {api} from './RestApi';

class SeriesPlot extends React.Component {

  plotsPerLine = 2;
  plotWidth = 1000/this.plotsPerLine;
  plotHeight = 250;

  render() {
    var lines = [];
    for(var i = 0; i < this.props.series.length; i++) {
      lines.push(<LineSeries key={i} data={this.props.series[i]}/>);
    }
    var divClass = 'col-md-'+(12/this.plotsPerLine);
    return (
      <div className={divClass}>
        <XYPlot width={this.plotWidth} height={this.plotHeight} margin={{left: 100}}>
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
    this.crawlerId = this.props.match.params.crawler_id;
    this.state = {
      metrics: {}
    };
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
    this.fetchMetrics();
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
    api.get('/crawls/' + this.crawlerId + '/metrics')
       .then(this.updateSeries.bind(this));
  }

  updateSeries(metrics) {
    if(metrics === 'FETCH_ERROR') {
      this.serverError = true;
      this.setState({
        serverError: true,
        serverMessage: "Failed to connect to ACHE API to get metrics."
      });
      return;
    }

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
    this.setState({charts: this.charts, metrics: metrics});
  }

  render(){
    if(this.state.serverError === true) {
      return (
        <div className="alert alert-danger message">
          <span className="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> {this.state.serverMessage}
        </div>
      );
    }

    var plots = [];
    var table;
    if(this.state.metrics !== null &&
       this.state.metrics.gauges != null) {
      var metrics = this.state.metrics;
      // Create metrics table
      table =
        <div className="row">
          <div className="col-md-8 col-md-offset-2">
            <div className="panel panel-default">
              <div className="panel-heading"><b>Crawl Metrics</b></div>
                <table className="table table-striped">
                  <tbody>
                    <tr>
                      <td>Uncrawled Pages in Frontier</td>
                      <td>{metrics['gauges']['frontier_manager.last_load.uncrawled'].value.toLocaleString()}</td>
                    </tr>
                    <tr>
                      <td>Successfull Page Fetches (HTTP 2XX)</td>
                      <td>{metrics['counters']['downloader.http_response.status.2xx'].count.toLocaleString()}</td>
                    </tr>
                    <tr>
                      <td>Successfull Page Fetches</td>
                      <td>{metrics['counters']['downloader.fetches.successes'].count.toLocaleString()}</td>
                    </tr>
                    <tr>
                      <td>Page Fetch Errors </td>
                      <td>{metrics['counters']['downloader.fetches.errors'].count.toLocaleString()}</td>
                    </tr>

                    <tr>
                      <td>Fetch Time (mean) in secs </td>
                      <td>{metrics['timers']['downloader.fetch.time'].mean.toFixed(3)}</td>
                    </tr>
                    <tr>
                      <td>Fetch Time (p95) in secs </td>
                      <td>{metrics['timers']['downloader.fetch.time'].p95.toFixed(3)}</td>
                    </tr>
                  </tbody>
                </table>
            </div>
          </div>
        </div>

      // Create time series charts
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

    }

    return (
      <div className="row">
        {table}
        {plots}
      </div>
    );
  }

}

export default MetricsMonitor;
