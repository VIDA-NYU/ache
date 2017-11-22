import React from 'react';

import {Link} from 'react-router-dom'
import {api} from './RestApi';

import AlertMessage from './AlertMessage'

class CrawlersTable extends React.Component {
  render() {
    const crawlerManager = this.props.crawlerManager;
    const crawlerList = this.props.crawlers;
    return (
      <div className="table-responsive">
        <table className="table table-striped">
          <thead>
            <tr>
              <th>#</th>
              <th>Crawler ID</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          { crawlerList.map((crawler, index) =>
            <tbody>
              <tr>
                <td>{index + 1}</td>
                <td>{crawler.crawlerId}</td>
                <td>{crawler.crawlerState}</td>
                <td>
                  <button type="button" className="btn btn-default btn-xs" disabled={!crawler.crawlerRunning} onClick={(e)=>crawlerManager.stopCrawl(crawler.crawlerId)}><i className="glyphicon glyphicon-stop"></i> Stop Crawler</button>
                  &nbsp;
                  <Link className="btn btn-default btn-xs" to={'/monitoring/'+crawler.crawlerId}><i className="glyphicon glyphicon-signal" />&nbsp;Monitoring</Link>
                  &nbsp;
                  <Link className="btn btn-default btn-xs" to={'/search/'+crawler.crawlerId} disabled={!crawler.searchEnabled}><i className="glyphicon glyphicon-search" />&nbsp;Search</Link>
                  </td>
              </tr>
            </tbody>
          )}
        </table>
      </div>
    );
  }
}

class Crawlers extends React.Component {

  constructor(props) {
    super(props);
    this.state = {};
    this.fetchCrawlersList();
  }

  componentDidMount() {
    this.timerID = setInterval(
      () => this.fetchCrawlersList(),
      2000 // update status every 2s
    );
  }

  componentWillUnmount() {
    clearInterval(this.timerID);
  }

  fetchCrawlersList() {
    api.get('/crawls')
      .then( (response) => {
        if (response === 'FETCH_ERROR') {
          this.setState({
            message: {
              type: 'error',
              message: 'Failed to connect to ACHE API to list of crawlers.'
            }
          });
        } else {
          this.setState({ crawlers: response.crawlers });
        }
      });
  }

  stopCrawl(crawlerId) {
    var config = {mode: 'cors'};
    api.get('/crawls/' + crawlerId + '/stopCrawl', config)
       .then(this.updateResponse.bind(this));
  }

  updateResponse(response) {
    if('FETCH_ERROR' === response) {
      this.setState({
        message: {
          type: 'error',
          message: 'Failed to connect to ACHE server to start the crawler.'
        }
      });
    } else {
      if(response.shutdownInitiated === true) {
        this.setState({
          stopping: true,
          message: {
            type: 'success',
            message: 'Crawler shutdown initiated.'
          }
        });
      } else if(response.crawlerStarted === false) {
        this.setState({
          starting: false,
          message: {
            type: 'error',
            message: 'Failed to start the crawler.'
          }
        });
      } else if(response.crawlerStarted === true) {
        this.setState({
          crawlerRunning: true,
          message: {
            type: 'success',
            message: 'Crawler started successfully.'
          }
        });
      }
    }
  }

  render(){

    const hasCrawlers = (this.state.crawlers && this.state.crawlers.length > 0);

    let crawlersTable;
    if(hasCrawlers) {
      crawlersTable = <CrawlersTable crawlers={this.state.crawlers} crawlerManager={this} />;
    }

    return (
      <div className="row">
        <div className="col-md-12">
          { this.state.message && <AlertMessage message={this.state.message} /> }
          <p>
            <Link className="btn btn-default" to='/crawlers/start'><i className="glyphicon glyphicon-plus" />&nbsp;Start Crawler</Link>
          </p>
          { hasCrawlers && crawlersTable }
          {
            !hasCrawlers &&
            <AlertMessage message={{type: 'warn', message: 'No crawlers available. Click on "Start Crawl" to start a crawler.'}} />
          }
        </div>
      </div>
    );

  }

}

export default Crawlers;
