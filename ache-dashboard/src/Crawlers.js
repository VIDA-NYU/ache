import React from 'react';

import {Link} from 'react-router-dom'
import {api} from './RestApi';

import {AlertMessage, AlertMessages} from './AlertMessage'

class CrawlersTable extends React.Component {
  render() {
    const crawlerManager = this.props.crawlerManager;
    const crawlerList = this.props.crawlers;
    return (
      <div className="table-responsive">
        <table className="table table-striped table-bordered">
          <thead>
            <tr>
              <th>#</th>
              <th>Crawler ID</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
          { crawlerList.map((crawler, index) =>
              <tr key={crawler.crawlerId+crawler.crawlerState} >
                <td>{index + 1}</td>
                <td>{crawler.crawlerId}</td>
                <td><span className="label label-default">{crawler.crawlerState}</span></td>
                <td>
                  <button type="button" className="btn btn-default btn-sm"
                    disabled={!crawler.crawlerRunning} onClick={(e)=>crawlerManager.stopCrawl(crawler.crawlerId)}>
                      <i className="glyphicon glyphicon-stop"></i> Stop Crawler
                  </button>
                  &nbsp;
                  <Link className="btn btn-default btn-sm" to={'/monitoring/'+crawler.crawlerId}>
                    <i className="glyphicon glyphicon-signal" />&nbsp;Monitoring
                  </Link>
                  &nbsp;
                  <Link className="btn btn-default btn-sm" to={'/search/'+crawler.crawlerId} disabled={!crawler.searchEnabled}>
                    <i className="glyphicon glyphicon-search" />&nbsp;Search
                  </Link>
                </td>
              </tr>
          )}
          </tbody>
        </table>
      </div>
    );
  }
}

class Crawlers extends React.Component {

  constructor(props) {
    super(props);
    this.messages = this.props.messages;
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
          if(!this.state.serverError) {
            this.messages.error('Failed to connect to ACHE server.');
            this.setState({serverError: true});
          }
        } else {
          this.setState({serverError: false, crawlers: response.crawlers});
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
      if(!this.state.serverError) {
        this.messages.error('Failed to connect to ACHE server.');
        this.setState({serverError: true});
      }
    } else {
      this.setState({serverError: false});
      if(response.shutdownInitiated === false) {
        this.messages.error('Failed to initiate crawler shutdown.');
      } else if(response.shutdownInitiated === true) {
        this.messages.success('Crawler shutdown initiated.');
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
          <AlertMessages messages={this.messages.display()} />
          <h2>Crawlers</h2>
          <p>
            <Link className="btn btn-default" to='/start'><i className="glyphicon glyphicon-plus" />&nbsp;Start Crawler</Link>
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
