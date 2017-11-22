import React from 'react';

import {Link} from 'react-router-dom'
import {api} from './RestApi';

import AlertMessage from './AlertMessage'

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

    const message = this.state.message ? <AlertMessage message={this.state.message} /> : null;

    const hasCrawlers = (this.state.crawlers && this.state.crawlers.length > 0);

    let crawlersTable;
    if(hasCrawlers) {
      crawlersTable = (
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
            { this.state.crawlers.map((crawler, index) =>
              <tbody>
                <tr>
                  <td>{index + 1}</td>
                  <td>{crawler.crawlerId}</td>
                  <td>{crawler.crawlerState}</td>
                  <td>
                    <button type="button" className="btn btn-default btn-xs" disabled={!crawler.crawlerRunning} onClick={(e)=>this.stopCrawl(crawler.crawlerId)}><i className="glyphicon glyphicon-stop"></i> Stop Crawler</button>
                    &nbsp;
                    <button type="button" className="btn btn-default btn-xs" disabled={!crawler.crawlerRunning}><i className="glyphicon glyphicon-info-sign"></i> Monitor</button>
                    &nbsp;
                    <Link className="btn btn-default btn-xs" to={'/monitoring/'+crawler.crawlerId}><i className="glyphicon glyphicon-signal" />&nbsp;Monitor</Link>
                    &nbsp;
                    <button type="button" className="btn btn-default btn-xs" disabled={!crawler.searchEnabled}><i className="glyphicon glyphicon-search"></i> Search</button>
                    &nbsp;
                    <Link className="btn btn-default btn-xs" to={'/search/'+crawler.crawlerId}><i className="glyphicon glyphicon-signal" />&nbsp;Search</Link>
                    </td>
                </tr>
              </tbody>
            )}
          </table>
        </div>
      );
    }

    return (
      <div>
        {
          !hasCrawlers &&
          <AlertMessage message={{type: 'warn', message: 'No crawlers available. Click on "Start Crawl" to start a crawler.'}} />
        }
        { message && <div className="row">{message}</div> }
        <div className="row">
            <Link className="btn btn-default" to='/crawlers/start'><i className="glyphicon glyphicon-plus" />&nbsp;Start Crawler</Link>
        </div>
        {
          hasCrawlers &&
          <div className="row">{crawlersTable}</div>
        }
      </div>
    );

  }

}

export default Crawlers;
