import React from 'react';

import {AlertMessages} from './AlertMessage'
import {api} from './RestApi';

class StartCrawler extends React.Component {

  DEFAULT_STATE = {
    crawlerId: '',
    crawlType: null,
    seeds: null,
    seedsContent: null,
    modelFile: null,
    invalidModel: null,
    starting: false
  };

  constructor(props) {
    super(props);
    this.messages = this.props.messages;
    this.state = Object.assign({}, this.DEFAULT_STATE);
  }

  cancelCrawl() {
    this.setState(this.DEFAULT_STATE);
    this.parseUrlQueryParameters();
  }

  componentWillMount() {
    this.parseUrlQueryParameters();
  }

  parseUrlQueryParameters() {
    if (this.props.location.search) {
      const queryParams = this.props.location.search.substr(1).split('&');
      for (let param of queryParams) {
        let kv = param.split('=');
        if(kv && kv.length === 2) {
          if('crawlerId' === kv[0]) {
            this.setState({crawlerId: kv[1], crawlerIdDisabled: true});
          }
          else if('crawlType' === kv[0]) {
            this.setState({crawlType: kv[1], crawlTypeDisabled: true});
          }
        }
      }
    }
  }

  startCrawl(crawlerId) {
    this.setState({starting: true});
    var config = {
      method: 'POST',
      mode: 'cors',
      body: JSON.stringify({
        crawlType: this.state.crawlType,
        seeds: this.state.seeds,
        model: this.state.modelFile
      })
    };
    api.post('/crawls/' + crawlerId + '/startCrawl', config)
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
      if(response.crawlerStarted === false) {
        this.messages.error('Failed to start the crawler.');
        this.setState({starting: false});
      } else if(response.crawlerStarted === true) {
        this.messages.success('Crawler started successfully.');
        this.props.history.push('/')
      }
    }
  }

  handleSelectModelFile(e) {
    const reader = new FileReader();
    const file = e.target.files[0];
    if('application/zip' === file.type) {
      reader.onload = (upload) => {
        this.setState({
          modelFile: reader.result.split(',')[1],
          invalidModel: false
        });
      };
      reader.readAsDataURL(file);
    } else {
      this.setState({
        invalidModel: true
      });
    }
  }

  handleSelectSeedFile(e) {
    const reader = new FileReader();
    const file = e.target.files[0];
    reader.onload = (upload) => {
      //TODO validate reader.result not empty and filetype
      let seedsContent = reader.result.replace(/\r\n/g, '\n');
      var seeds = seedsContent.split('\n');

      if(seeds !== null && seeds.length > 0) {
        let validUrls = []
        for(var i = 0; i < seeds.length; i++) {
          if(/^https?:\/\/.+/i.test(seeds[i])) {
            validUrls.push(seeds[i]);
          }
        }
        this.setState({
          seeds: validUrls,
          seedsContent: validUrls.join('\n')
        });
      }
    };
    reader.readAsText(file);
  }

  handleCrawlTypeChange(newCrawlType) {
    this.setState({crawlType: newCrawlType});
  }

  handleCrawlerIdChange(event) {
    this.setState({crawlerId: event.target.value});
  }

  isSelected(crawlType) {
    return this.state.crawlType === crawlType ? 'active' : '';
  }

  hasValidCrawlerId(crawlerId) {
    const validChars = /^[0-9A-Za-z_-]+$/;
    return this.state.crawlerId && validChars.test(this.state.crawlerId);
  }

  showErrorCrawlerId() {
    return this.state.crawlerId && !this.hasValidCrawlerId();
  }

  formIsValid() {
    if (!this.state.crawlType)
      return false;
    const hasCrawlerId = this.hasValidCrawlerId();
    if (this.state.crawlType === 'DeepCrawl') {
      const hasValidSeeds = this.state.seeds && this.state.seeds.length > 0;
      return hasValidSeeds && hasCrawlerId;
    } else if (this.state.crawlType === 'FocusedCrawl') {
      return this.state.modelFile && !this.state.invalidModel && hasCrawlerId;
    } else {
      return false;
    }
  }

  render() {
    // render launch Crawler pages
    const isStarting = this.state.starting;
    const enableStart = this.formIsValid() && !isStarting;

    let crawlDescription;
    if(this.state.crawlType === 'DeepCrawl') {
      crawlDescription = 'Only relevant pages within the web sites listed in the seeds will be crawled.'
    } else if(this.state.crawlType === 'FocusedCrawl') {
      crawlDescription = 'Relevant pages from any web site on the web will be crawled.'
    }

    return (
      <div className="row">
        <div className="col-md-12">
          <AlertMessages messages={this.messages.display()} />
          <h2>Start Crawler</h2>
          <form>

            <div className="form-group">
              <label htmlFor="crawlerType">Select a Crawler Type:</label><br/>
              <div className="btn-group" data-toggle="buttons" id="crawlerType"
                aria-label="Choose a crawler type">
                <label className={'btn btn-primary ' + this.isSelected('DeepCrawl')}
                  onClick={()=>this.handleCrawlTypeChange('DeepCrawl')}>
                  <input type="radio" /> Deep Crawl
                </label>
                <label className={'btn btn-primary ' + this.isSelected('FocusedCrawl')}
                  onClick={()=>this.handleCrawlTypeChange('FocusedCrawl')}>
                  <input type="radio" checked={this.isSelected('FocusedCrawl')}/> Focused Crawl
                </label>
              </div>
               <p className="help-block"><small>{crawlDescription}</small></p>
            </div>

            <div className={'form-group' + (this.showErrorCrawlerId() ? ' has-error': '')} >
              <label htmlFor="crawlerId">Crawler ID:</label>
              <input type="text" className="form-control" id="crawlerId"
                placeholder="my-crawler-name-1"
                onChange={(e)=>this.handleCrawlerIdChange(e)}
                style={{'maxWidth':'400px'}} value={this.state.crawlerId}
                disabled={this.state.crawlerIdDisabled} />
            </div>

            {(this.state.crawlType === 'DeepCrawl') &&
              <div>
                <div className="form-group">
                  <label htmlFor="seedsInputFile">Seeds file:</label>
                  <input type="file" className="form-control-file" id="seedsInputFile"
                    aria-describedby="seedsFileHelp" onChange={(e)=>this.handleSelectSeedFile(e)} />
                  <small id="seedsFileHelp" className="form-text text-muted">
                    Please select a text file containing a list of URLs to start the crawl.
                    The file should contain one URL per line, and each URL should start with "http://" or "https://".
                  </small>
                </div>
              </div>
            }
            {(this.state.crawlType === 'FocusedCrawl') &&
              <div className="form-group">
                <label htmlFor="seedsInputFile">Model package:</label>
                <input type="file" className="form-control-file" id="modelInputFile"
                  aria-describedby="modelFileHelp" onChange={(e)=>this.handleSelectModelFile(e)} />
                <small id="modelFileHelp" className="form-text text-muted">
                  Please select the model file downloaded from DDT (&lt;domain-name&gt;_model.zip).
                </small>
                { this.state.invalidModel && <p className="text-danger">Invalid model file. Select a zip file.</p>}
              </div>
            }

            <div className="form-group">
              <div className="btn-toolbar">
                <button type="button" className="btn btn-default btn-md"
                  onClick={()=>this.cancelCrawl()}>
                  <span className="glyphicon glyphicon-remove"></span>&nbsp;Cancel
                </button>
                <button disabled={!enableStart} type="button"
                  className="btn btn-primary btn-md"
                  onClick={(e)=>this.startCrawl(this.state.crawlerId)}>
                  <span className="glyphicon glyphicon-play"></span>&nbsp;Start Crawl
                </button>
              </div>
              { isStarting && <small className="form-text text-muted"><br/>Starting crawler... Hang tight.</small> }
            </div>
            { this.state.seeds && <p>Loaded {this.state.seeds.length} URLs from file.</p>}
            { this.state.seedsContent && <div><pre>{this.state.seedsContent}</pre></div> }
          </form>
        </div>
      </div>
    );
  }

}

export default StartCrawler;
